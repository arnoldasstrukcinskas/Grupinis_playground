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
import java.security.Key;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class ExternalApiWorkflowSteps {

    private static final String JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250655368566D5971";

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

    @Given("the external API is available")
    public void externalApiIsAvailable() throws Exception {
        lastResponse = send(get("/api-docs").build());
        assertTrue(lastResponse.statusCode() < 500,
                () -> "Expected the running API to respond, but got: " + lastResponse.statusCode());
    }

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
            token = generateToken(username);
        }
    }

    @When("I search live locations for {string}")
    public void searchLiveLocations(String location) throws Exception {
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        lastResponse = send(get("/hotels?location=" + encodedLocation)
                .header("Authorization", bearerToken())
                .build());
    }

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

    @Then("the external response status should be {int}")
    public void assertExternalStatus(int statusCode) {
        assertEquals(statusCode, lastResponse.statusCode(), lastResponse.body());
    }

    @Then("the external response body should contain {string}")
    public void assertExternalBodyContains(String expectedText) {
        assertTrue(lastResponse.body().contains(expectedText),
                () -> "Expected response to contain [%s] but was [%s]".formatted(expectedText, lastResponse.body()));
    }

    @Then("the live location response should contain at least one destination")
    public void assertLiveLocationResponseContainsDestination() throws Exception {
        JsonNode response = readResponseJson();
        assertTrue(response.isArray() && !response.isEmpty(), () -> "Expected a non-empty location array: " + response);

        JsonNode firstDestination = response.get(0).get("dest_id");
        assertTrue(firstDestination != null && firstDestination.canConvertToInt(),
                () -> "Expected first location to contain an integer dest_id: " + response);
        destinationId = firstDestination.asInt();
    }

    @Then("the live hotel response should contain at least one hotel")
    public void assertLiveHotelResponseContainsHotel() throws Exception {
        JsonNode response = readResponseJson();
        assertTrue(response.isArray() && !response.isEmpty(), () -> "Expected a non-empty hotel array: " + response);
        assertTrue(response.get(0).has("hotelName"), () -> "Expected hotel item to contain hotelName: " + response);
    }

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

    private String generateToken(String subject) {
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key)
                .compact();
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
