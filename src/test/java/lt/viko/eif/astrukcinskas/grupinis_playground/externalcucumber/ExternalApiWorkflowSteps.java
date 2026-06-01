package lt.viko.eif.astrukcinskas.grupinis_playground.externalcucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for the optional external API acceptance workflow.
 *
 * <p>These scenarios call a separately running application through HTTP and use
 * the real configured external services, so they are intentionally run only when requested.</p>
 */
public class ExternalApiWorkflowSteps {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String baseUrl = externalBaseUrl();
    private final Duration requestTimeout = Duration.ofSeconds(Long.getLong("external.timeout.seconds", 180L));

    private HttpResponse<String> lastResponse;
    private String username;
    private String password;
    private String token;
    private int destinationId;

    /**
     * Checks that the configured external application base URL responds before running the scenario.
     *
     * @throws Exception if the HTTP request cannot be executed
     */
    @Given("the external API is available")
    public void externalApiIsAvailable() throws Exception {
        lastResponse = send(get("/api-docs").build());
        assertTrue(lastResponse.statusCode() < 500,
                () -> "Expected the running API to respond, but got: " + lastResponse.statusCode());
    }

    /**
     * Registers a unique user against the live application so repeated runs do not collide.
     *
     * @throws Exception if the HTTP request cannot be executed
     */
    @When("I register a unique external user")
    public void registerUniqueExternalUser() throws Exception {
        username = "external-bdd-" + System.currentTimeMillis();
        password = "Test1234!";

        String body = """
                {
                  "username": "%s",
                  "password": "%s",
                  "email": "%s@example.com"
                }
                """.formatted(username, password, username);

        lastResponse = send(postJson("/auth/register", body).build());
    }

    /**
     * Logs in as the unique external user and stores the returned JWT.
     *
     * @throws Exception if the HTTP request cannot be executed
     */
    @When("I login as the external user")
    public void loginAsExternalUser() throws Exception {
        String body = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        lastResponse = send(postJson("/auth/login", body).build());

        if (lastResponse.statusCode() == 200) {
            token = lastResponse.body();
        }
    }

    /**
     * Searches live external locations through the running application.
     *
     * @param location city or place name to search for
     * @throws Exception if the HTTP request cannot be executed
     */
    @When("I search live locations for {string}")
    public void searchLiveLocations(String location) throws Exception {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        lastResponse = send(get("/hotels?location=" + encodedLocation)
                .header("Authorization", bearerToken())
                .build());
    }

    /**
     * Requests live hotels for the first destination id captured from the location response.
     *
     * @throws Exception if the HTTP request cannot be executed
     */
    @When("I request live hotels for the first destination")
    public void requestLiveHotelsForFirstDestination() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(3);

        String body = """
                {
                  "hobbiesAndInterests": "architecture, museums, local food, walking",
                  "promptToOllama": "Recommend hotels for a curious traveler who likes history and walkable neighborhoods.",
                  "destinationId": %d,
                  "checkInDate": "%s",
                  "checkOutDate": "%s",
                  "roomNumber": 1,
                  "adultsNumber": 2
                }
                """.formatted(destinationId, checkIn, checkOut);

        lastResponse = send(postJson("/hotels", body)
                .header("Authorization", bearerToken())
                .build());
    }

    /**
     * Requests live AI analysis for the hotels currently held by the running application.
     *
     * @param prompt user travel request sent to the analysis endpoint
     * @param hobbies user hobbies sent to the analysis endpoint
     * @throws Exception if the HTTP request cannot be executed
     */
    @When("I request live analysis for {string} and hobbies {string}")
    public void requestLiveAnalysis(String prompt, String hobbies) throws Exception {
        String body = """
                {
                  "userPrompt": "%s",
                  "userHobbies": "%s"
                }
                """.formatted(prompt, hobbies);

        lastResponse = send(postJson("/analysis/analyze", body)
                .header("Authorization", bearerToken())
                .build());
    }

    /**
     * Asserts the status code from the last live HTTP response.
     *
     * @param statusCode expected HTTP status code
     */
    @Then("the external response status should be {int}")
    public void assertExternalStatus(int statusCode) {
        assertEquals(statusCode, lastResponse.statusCode(), lastResponse.body());
    }

    /**
     * Asserts that the last live HTTP response contains expected text.
     *
     * @param expectedText text expected in the response body
     */
    @Then("the external response body should contain {string}")
    public void assertExternalBodyContains(String expectedText) {
        assertTrue(lastResponse.body().contains(expectedText),
                () -> "Expected response to contain [%s] but was [%s]".formatted(expectedText, lastResponse.body()));
    }

    /**
     * Asserts that the last live response body has the structure of a JWT.
     */
    @Then("the external response body should be a JWT token")
    public void assertExternalBodyIsJwtToken() {
        assertTrue(isJwt(lastResponse.body()),
                () -> "Expected response to be a JWT token but was [%s]".formatted(lastResponse.body()));
    }

    /**
     * Asserts that the location response contains a destination and stores its id for hotel search.
     *
     * @throws Exception if the response JSON cannot be parsed
     */
    @Then("the live location response should contain at least one destination")
    public void assertLiveLocationResponseContainsDestination() throws Exception {
        JsonNode response = readResponseJson();
        assertTrue(response.isArray() && !response.isEmpty(), () -> "Expected a non-empty location array: " + response);

        JsonNode firstDestination = response.get(0).get("dest_id");
        assertTrue(firstDestination != null && firstDestination.canConvertToInt(),
                () -> "Expected first location to contain an integer dest_id: " + response);
        destinationId = firstDestination.asInt();
    }

    /**
     * Asserts that the live hotel response contains at least one hotel item.
     *
     * @throws Exception if the response JSON cannot be parsed
     */
    @Then("the live hotel response should contain at least one hotel")
    public void assertLiveHotelResponseContainsHotel() throws Exception {
        JsonNode response = readResponseJson();
        assertTrue(response.isArray() && !response.isEmpty(), () -> "Expected a non-empty hotel array: " + response);
        assertTrue(response.get(0).has("hotelName"), () -> "Expected hotel item to contain hotelName: " + response);
    }

    /**
     * Asserts that the live analysis response contains a non-empty analysis field.
     *
     * @throws Exception if the response JSON cannot be parsed
     */
    @Then("the live analysis response should contain non-empty analysis text")
    public void assertLiveAnalysisResponseContainsText() throws Exception {
        JsonNode response = readResponseJson();
        JsonNode analysis = response.get("analysis");
        assertTrue(analysis != null && analysis.isTextual(), () -> "Expected analysis text field: " + response);
        assertFalse(analysis.asText().isBlank(), () -> "Expected non-empty analysis text: " + response);
    }

    private JsonNode readResponseJson() throws IOException {
        return objectMapper.readTree(lastResponse.body());
    }

    private HttpRequest.Builder get(String path) {
        return request(path).GET();
    }

    private HttpRequest.Builder postJson(String path, String body) {
        return request(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(requestTimeout)
                .header("Accept", "application/json");
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String bearerToken() {
        return "Bearer " + token;
    }

    private boolean isJwt(String token) {
        return token != null && token.split("\\.").length == 3;
    }

    private static String externalBaseUrl() {
        String systemProperty = System.getProperty("external.baseUrl");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String environmentVariable = System.getenv("EXTERNAL_BASE_URL");
        if (environmentVariable != null && !environmentVariable.isBlank()) {
            return environmentVariable;
        }

        return "http://localhost:8085";
    }
}
