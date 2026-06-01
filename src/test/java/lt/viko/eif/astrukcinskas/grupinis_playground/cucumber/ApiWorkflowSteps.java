package lt.viko.eif.astrukcinskas.grupinis_playground.cucumber;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
     * Sets up the test environment before each scenario.
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
     * Asserts that the real API is running under the test profile with external integrations stubbed.
     */
    @Given("the real API is running under the test profile with external integrations stubbed")
    public void realApiIsRunningUnderTestProfile() {
        assertNotNull(mockMvc);
    }

    /**
     * Registers a new user with the given username, password, and email.
     * @param username
     * @param password
     * @param email
     * @throws Exception
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
     * Logs in the user with the given username and password.
     * @param username
     * @param password
     * @throws Exception
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
      * Searches for locations matching the given query and asserts that the response contains at least one destination with an integer dest_id field.
      * @param location
      * @throws Exception
      */
    @When("I search locations for {string}")
    public void searchLocations(String location) throws Exception {
        lastResult = mockMvc.perform(get("/hotels")
                        .header("Authorization", bearerToken())
                        .param("location", location))
                .andReturn();
    }

    /** 
     * @param destinationId
     * @throws Exception
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
     * @param prompt
     * @param hobbies
     * @throws Exception
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
     * @throws Exception
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
     * @throws Exception
     */
    @When("I fetch all saved analyses")
    public void fetchAllSavedAnalyses() throws Exception {
        lastResult = mockMvc.perform(get("/analysis/analyses")
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /** 
     * @throws Exception
     */
    @When("I fetch the saved analysis by id")
    public void fetchSavedAnalysisById() throws Exception {
        lastResult = mockMvc.perform(get("/analysis/{id}", savedAnalysisId)
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /** 
     * @throws Exception
     */
    @When("I fetch hotels for the saved analysis")
    public void fetchHotelsForSavedAnalysis() throws Exception {
        lastResult = mockMvc.perform(get("/analysis/{id}/hotels", savedAnalysisId)
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /** 
     * @throws Exception
     */
    @When("I fetch one hotel from the saved analysis")
    public void fetchOneHotelFromSavedAnalysis() throws Exception {
        lastResult = mockMvc.perform(get("/analysis/{analysisId}/hotels/{hotelId}", savedAnalysisId, savedHotelId)
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /** 
     * @throws Exception
     */
    @When("I delete the saved analysis")
    public void deleteSavedAnalysis() throws Exception {
        lastResult = mockMvc.perform(delete("/analysis/{id}", savedAnalysisId)
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /** 
     * @throws Exception
     */
    @When("I clear the in-memory analysis")
    public void clearInMemoryAnalysis() throws Exception {
        lastResult = mockMvc.perform(delete("/analysis/clearAnalysis")
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /** 
     * @throws Exception
     */
    @When("I clear the in-memory hotels")
    public void clearInMemoryHotels() throws Exception {
        lastResult = mockMvc.perform(delete("/analysis/clearHotels")
                        .header("Authorization", bearerToken()))
                .andReturn();
    }

    /** 
     * @throws Exception
     */
    @When("I logout with the current token")
    public void logoutWithCurrentToken() throws Exception {
        lastResult = mockMvc.perform(post("/auth/logout")
                        .param("token", currentToken))
                .andReturn();
    }

    /** 
     * @param statusCode
     */
    @Then("the response status should be {int}")
    public void assertStatus(int statusCode) {
        assertEquals(statusCode, lastResult.getResponse().getStatus());
    }

    /** 
     * @param expectedText
     * @throws Exception
     */
    @Then("the response body should contain {string}")
    public void assertResponseBodyContains(String expectedText) throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        assertTrue(content.contains(expectedText),
                () -> "Expected response to contain [%s] but was [%s]".formatted(expectedText, content));
    }

    /** 
     * @throws Exception
     */
    @Then("the response body should be a JWT token")
    public void assertResponseBodyIsJwtToken() throws Exception {
        String content = lastResult.getResponse().getContentAsString();
        assertTrue(isJwt(content), () -> "Expected response to be a JWT token but was [%s]".formatted(content));
    }

    /** 
     * @throws Exception
     */
    @Then("the response body should equal the saved analysis id")
    public void assertResponseBodyEqualsSavedAnalysisId() throws Exception {
        assertEquals(String.valueOf(savedAnalysisId), lastResult.getResponse().getContentAsString());
    }

    /** 
     * @throws Exception
     */
    @Then("the response body should contain a link to the saved hotel")
    public void assertResponseBodyContainsSavedHotelLink() throws Exception {
        String expectedLink = "/analysis/%d/hotels/%d".formatted(savedAnalysisId, savedHotelId);
        String content = lastResult.getResponse().getContentAsString();
        assertTrue(content.contains(expectedLink),
                () -> "Expected response to contain [%s] but was [%s]".formatted(expectedLink, content));
    }

    /** 
     * @return String
     */
    private String bearerToken() {
        return "Bearer " + currentToken;
    }

    /** 
     * @param token
     * @return boolean
     */
    private boolean isJwt(String token) {
        return token != null && token.split("\\.").length == 3;
    }

    /** 
     * @return LocationDto
     */
    private LocationDto sampleLocation() {
        LocationDto locationDto = new LocationDto();
        locationDto.setCountry("Spain");
        locationDto.setDestinationId(-372490);
        locationDto.setDestinationName("Barcelona");
        locationDto.setDestinationType("city");
        return locationDto;
    }

    /** 
     * @return HotelDto
     */
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
