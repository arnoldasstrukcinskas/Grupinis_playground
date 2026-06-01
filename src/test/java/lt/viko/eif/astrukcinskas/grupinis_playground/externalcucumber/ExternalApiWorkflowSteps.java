package lt.viko.eif.astrukcinskas.grupinis_playground.externalcucumber;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
     * @throws Exception
     */
    @Given("the external API is available")
    public void externalApiIsAvailable() throws Exception {
        lastResponse = send(get("/api-docs").build());
        assertTrue(lastResponse.statusCode() < 500,
                () -> "Expected the running API to respond, but got: " + lastResponse.statusCode());
    }

    /** 
     * @throws Exception
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
     * @throws Exception
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
     * @param location
     * @throws Exception
     */
    @When("I search live locations for {string}")
    public void searchLiveLocations(String location) throws Exception {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        lastResponse = send(get("/hotels?location=" + encodedLocation)
                .header("Authorization", bearerToken())
                .build());
    }

    /** 
     * @throws Exception
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
     * @param prompt
     * @param hobbies
     * @throws Exception
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
     * @param statusCode
     */
    @Then("the external response status should be {int}")
    public void assertExternalStatus(int statusCode) {
        assertEquals(statusCode, lastResponse.statusCode(), lastResponse.body());
    }

    /** 
     * @param expectedText
     */
    @Then("the external response body should contain {string}")
    public void assertExternalBodyContains(String expectedText) {
        assertTrue(lastResponse.body().contains(expectedText),
                () -> "Expected response to contain [%s] but was [%s]".formatted(expectedText, lastResponse.body()));
    }

    @Then("the external response body should be a JWT token")
    public void assertExternalBodyIsJwtToken() {
        assertTrue(isJwt(lastResponse.body()),
                () -> "Expected response to be a JWT token but was [%s]".formatted(lastResponse.body()));
    }

    /** 
     * @throws Exception
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
     * Asserts that the live hotel response contains at least one hotel with a hotelName field. 
     * @throws Exception
     */
    @Then("the live hotel response should contain at least one hotel")
    public void assertLiveHotelResponseContainsHotel() throws Exception {
        JsonNode response = readResponseJson();
        assertTrue(response.isArray() && !response.isEmpty(), () -> "Expected a non-empty hotel array: " + response);
        assertTrue(response.get(0).has("hotelName"), () -> "Expected hotel item to contain hotelName: " + response);
    }

    /** 
     * @throws Exception
     */
    @Then("the live analysis response should contain non-empty analysis text")
    public void assertLiveAnalysisResponseContainsText() throws Exception {
        JsonNode response = readResponseJson();
        JsonNode analysis = response.get("analysis");
        assertTrue(analysis != null && analysis.isTextual(), () -> "Expected analysis text field: " + response);
        assertFalse(analysis.asText().isBlank(), () -> "Expected non-empty analysis text: " + response);
    }

    /** 
     * @return JsonNode
     * @throws IOException
     */
    private JsonNode readResponseJson() throws IOException {
        return objectMapper.readTree(lastResponse.body());
    }

    /** 
     * @param path
     * @return Builder
     */
    private HttpRequest.Builder get(String path) {
        return request(path).GET();
    }

    /** 
     * @param path
     * @param body
     * @return Builder
     */
    private HttpRequest.Builder postJson(String path, String body) {
        return request(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
    }

    /** 
     * @param path
     * @return Builder
     */
    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(requestTimeout)
                .header("Accept", "application/json");
    }

    /** 
     * @param request
     * @return HttpResponse<String>
     * @throws IOException
     * @throws InterruptedException
     */
    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** 
     * @return String
     */
    private String bearerToken() {
        return "Bearer " + token;
    }

    /** 
     * @param token
     * @return boolean
     */
    private boolean isJwt(String token) {
        return token != null && token.split("\\.").length == 3;
    }

    /** 
     * @return String
     */
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
