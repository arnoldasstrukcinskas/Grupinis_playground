package lt.viko.eif.astrukcinskas.grupinis_playground.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.AnalysisRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.HotelsRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.UsersRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelRequestDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.LocationDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.ExternalApiService;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.HotelsService;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.OllamaService;

/**
 * Step definitions for the internal API acceptance workflow.
 *
 * <p>The scenarios use real Spring MVC controllers and an in-memory test database,
 * while external hotel and AI integrations are replaced with deterministic stubs.</p>
 */
public class ApiWorkflowSteps {

    private static final Pattern ANALYSIS_ID_PATTERN = Pattern.compile("Analysis with id: (\\d+), saved");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelsService hotelsService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private HotelsRepository hotelsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ExternalApiService externalApiService;

    @Autowired
    private OllamaService ollamaService;

    private MvcResult lastResult;
    private String currentUsername;
    private String currentToken;
    private int savedAnalysisId;
    private int savedHotelId;

    /**
     * Resets test state and prepares deterministic responses for external services before each scenario.
     */
    @Before
    public void setUp() {
        currentUsername = null;
        currentToken = null;
        savedAnalysisId = 0;
        savedHotelId = 0;

        hotelsService.clearHotelsFromMemmory();
        usersRepository.deleteAll();
        analysisRepository.deleteAll();
        hotelsRepository.deleteAll();

        when(externalApiService.getLocations("Barcelona"))
                .thenReturn(List.of(sampleLocation()));
        when(externalApiService.getHotels(any(HotelRequestDto.class)))
                .thenAnswer(invocation -> {
                    List<HotelDto> hotels = List.of(sampleHotelDto());
                    hotelsService.setHotels(hotels);
                    return hotels;
                });
        when(ollamaService.getResponse(any(), any()))
                .thenReturn("City break analysis");
    }

    /**
     * Verifies that the Spring MVC test client was created for the acceptance scenario.
     */
    @Given("the real API is running under the test profile with external integrations stubbed")
    public void realApiIsRunningUnderTestProfile() {
        assertNotNull(mockMvc);
    }

    /**
     * Registers a user through the real authentication endpoint.
     *
     * @param username username used by the scenario
     * @param password password used by the scenario
     * @param email email used by the scenario
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I register user {string} with password {string} and email {string}")
    public void registerUser(String username, String password, String email) throws Exception {
        currentUsername = username;
        lastResult = mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "email": "%s"
                                }
                                """.formatted(username, password, email)))
                .andReturn();
    }

    /**
     * Logs in through the authentication endpoint and stores the returned JWT for later requests.
     *
     * @param username username used by the scenario
     * @param password password used by the scenario
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I login with username {string} and password {string}")
    public void login(String username, String password) throws Exception {
        currentUsername = username;
        lastResult = mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(username, password)))
                .andReturn();

        if (lastResult.getResponse().getStatus() == 200) {
            currentToken = lastResult.getResponse().getContentAsString();
        }
    }

    /**
     * Searches for locations using the authenticated hotel location endpoint.
     *
     * @param location city or place name to search for
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I search locations for {string}")
    public void searchLocations(String location) throws Exception {
        lastResult = mockMvc.perform(get("/hotels")
                        .header("Authorization", bearerToken())
                        .param("location", location))
                .andReturn();
    }

    /**
     * Requests hotels for the destination returned by the location search.
     *
     * @param destinationId destination identifier used by the hotel search endpoint
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I request hotels for destination {int}")
    public void requestHotels(int destinationId) throws Exception {
        lastResult = mockMvc.perform(post("/hotels")
                        .header("Authorization", bearerToken())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "hobbiesAndInterests": "architecture, museums, local food, walking",
                                  "promptToOllama": "Recommend hotels for a curious traveler who likes history and walkable neighborhoods.",
                                  "destinationId": %d,
                                  "checkInDate": "2026-06-15",
                                  "checkOutDate": "2026-06-18",
                                  "roomNumber": 1,
                                  "adultsNumber": 2
                                }
                                """.formatted(destinationId)))
                .andReturn();
    }

    /**
     * Requests an AI analysis for the current hotel list.
     *
     * @param prompt user travel request sent to the analysis endpoint
     * @param hobbies user hobbies sent to the analysis endpoint
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I request an analysis for {string} and hobbies {string}")
    public void requestAnalysis(String prompt, String hobbies) throws Exception {
        lastResult = mockMvc.perform(post("/analysis/analyze")
                        .header("Authorization", bearerToken())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "userPrompt": "%s",
                                  "userHobbies": "%s"
                                }
                                """.formatted(prompt, hobbies)))
                .andReturn();
    }

    /**
     * Saves the generated analysis and remembers generated database identifiers for later checks.
     *
     * @throws Exception if the mock HTTP request cannot be executed or the saved id cannot be parsed
     */
    @When("I save the generated analysis")
    public void saveGeneratedAnalysis() throws Exception {
        lastResult = mockMvc.perform(post("/analysis")
                        .header("Authorization", bearerToken()))
                .andReturn();

        String response = lastResult.getResponse().getContentAsString();
        Matcher matcher = ANALYSIS_ID_PATTERN.matcher(response);
        assertTrue(matcher.find(), () -> "Could not extract saved analysis id from: " + response);
        savedAnalysisId = Integer.parseInt(matcher.group(1));
        savedHotelId = hotelsRepository.findAll().get(0).getId();
    }

    /**
     * Fetches all analyses visible to the authenticated scenario user.
     *
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I fetch all saved analyses")
    public void fetchAllSavedAnalyses() throws Exception {
        lastResult = mockMvc.perform(get("/analysis/analyses")
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /**
     * Fetches the saved analysis by the identifier captured after saving.
     *
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I fetch the saved analysis by id")
    public void fetchSavedAnalysisById() throws Exception {
        lastResult = mockMvc.perform(get("/analysis/{id}", savedAnalysisId)
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /**
     * Fetches hotel links for the saved analysis.
     *
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I fetch hotels for the saved analysis")
    public void fetchHotelsForSavedAnalysis() throws Exception {
        lastResult = mockMvc.perform(get("/analysis/{id}/hotels", savedAnalysisId)
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /**
     * Fetches one concrete hotel from the saved analysis.
     *
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I fetch one hotel from the saved analysis")
    public void fetchOneHotelFromSavedAnalysis() throws Exception {
        lastResult = mockMvc.perform(get("/analysis/{analysisId}/hotels/{hotelId}", savedAnalysisId, savedHotelId)
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /**
     * Attempts to delete the saved analysis.
     *
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I delete the saved analysis")
    public void deleteSavedAnalysis() throws Exception {
        lastResult = mockMvc.perform(delete("/analysis/{id}", savedAnalysisId)
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /**
     * Clears the in-memory analysis value through the maintenance endpoint.
     *
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I clear the in-memory analysis")
    public void clearInMemoryAnalysis() throws Exception {
        lastResult = mockMvc.perform(delete("/analysis/clearAnalysis")
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /**
     * Clears the in-memory hotel list through the maintenance endpoint.
     *
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I clear the in-memory hotels")
    public void clearInMemoryHotels() throws Exception {
        lastResult = mockMvc.perform(delete("/analysis/clearHotels")
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /**
     * Logs out using the token captured during login.
     *
     * @throws Exception if the mock HTTP request cannot be executed
     */
    @When("I logout with the current token")
    public void logoutWithCurrentToken() throws Exception {
        lastResult = mockMvc.perform(post("/auth/logout")
                        .param("token", currentToken))
                .andReturn();
    }

    /**
     * Asserts the status code from the previous API response.
     *
     * @param statusCode expected HTTP status code
     */
    @Then("the response status should be {int}")
    public void assertStatus(int statusCode) {
        assertEquals(statusCode, lastResult.getResponse().getStatus());
    }

    /**
     * Asserts that the previous API response contains expected text.
     *
     * @param expectedText text expected in the response body
     * @throws Exception if the response body cannot be read
     */
    @Then("the response body should contain {string}")
    public void assertResponseBodyContains(String expectedText) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        assertTrue(content.contains(expectedText),
                () -> "Expected response to contain [%s] but was [%s]".formatted(expectedText, content));
    }

    /**
     * Asserts that the previous API response body has the structure of a JWT.
     *
     * @throws Exception if the response body cannot be read
     */
    @Then("the response body should be a JWT token")
    public void assertResponseBodyIsJwtToken() throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        assertTrue(isJwt(content), () -> "Expected response to be a JWT token but was [%s]".formatted(content));
    }

    /**
     * Asserts that the delete endpoint returns the id of the saved analysis.
     *
     * @throws Exception if the response body cannot be read
     */
    @Then("the response body should equal the saved analysis id")
    public void assertResponseBodyEqualsSavedAnalysisId() throws Exception {
        assertEquals(String.valueOf(savedAnalysisId), lastResult.getResponse().getContentAsString());
    }

    /**
     * Asserts that the previous response includes a link to the saved hotel resource.
     *
     * @throws Exception if the response body cannot be read
     */
    @Then("the response body should contain a link to the saved hotel")
    public void assertResponseBodyContainsSavedHotelLink() throws Exception {
        String expectedLink = "/analysis/%d/hotels/%d".formatted(savedAnalysisId, savedHotelId);
        String content = lastResult.getResponse().getContentAsString();
        assertTrue(content.contains(expectedLink),
                () -> "Expected response to contain [%s] but was [%s]".formatted(expectedLink, content));
    }

    private String bearerToken() {
        return "Bearer " + currentToken;
    }

    private boolean isJwt(String token) {
        return token != null && token.split("\\.").length == 3;
    }

    private LocationDto sampleLocation() {
        LocationDto locationDto = new LocationDto();
        locationDto.setCountry("Spain");
        locationDto.setDestinationId(-372490);
        locationDto.setDestinationName("Barcelona");
        locationDto.setDestinationType("city");
        return locationDto;
    }

    private HotelDto sampleHotelDto() {
        HotelDto hotelDto = new HotelDto();
        hotelDto.setHotelName("Catalonia Sagrada Familia");
        hotelDto.setAccomodationType("Hotel");
        hotelDto.setHotelStars(3);
        hotelDto.setDistrict("Sant Marti");
        hotelDto.setDistanceToCenter("2.6 km");
        hotelDto.setPrice("481.20");
        hotelDto.setPriceAllInclusive("481.20");
        hotelDto.setAddress("Arago 577-579");
        hotelDto.setReviewScoreNumber(8.4);
        hotelDto.setReviewScoreWord("Very good");
        hotelDto.setReviewNumber(1000);
        hotelDto.setAdditionals("Close to Sagrada Familia");
        return hotelDto;
    }
}
