package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.Analysis;
import lt.viko.eif.astrukcinskas.grupinis_playground.model.AppUser;
import lt.viko.eif.astrukcinskas.grupinis_playground.model.Hotel;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.AnalysisRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.UsersRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AnalysisRequestDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;

/**
 * Unit tests for analysis generation and saving behavior without calling real external services.
 */
class AnalysisServiceTest {

    private AnalysisRepository analysisRepository;
    private HotelsService hotelsService;
    private OllamaService ollamaService;
    private UsersRepository usersRepository;
    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisRepository = mock(AnalysisRepository.class);
        hotelsService = mock(HotelsService.class);
        ollamaService = mock(OllamaService.class);
        usersRepository = mock(UsersRepository.class);
        analysisService = new AnalysisService(
                analysisRepository,
                hotelsService,
                ollamaService,
                usersRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateAnalysisUsesOllamaResponseAndConvertedHotels() throws Exception {
        AnalysisRequestDto requestDto = analysisRequestDto("Plan a trip", "museums");
        HotelDto hotelDto = new HotelDto();
        Hotel hotel = new Hotel();
        hotel.setHotelName("Catalonia Sagrada Familia");

        when(ollamaService.getResponse("Plan a trip", "museums")).thenReturn("AI response");
        when(hotelsService.getHotels()).thenReturn(List.of(hotelDto));
        when(hotelsService.converterDtoToHotel(hotelDto)).thenReturn(hotel);

        Analysis analysis = analysisService.generateAnalysis(requestDto);

        assertEquals("AI response", analysis.getAnalysis());
        assertEquals(1, analysis.getHotels().size());
        assertSame(hotel, analysis.getHotels().get(0));
    }

    @Test
    void generateAnalysisHandlesMissingHotelsList() throws Exception {
        AnalysisRequestDto requestDto = analysisRequestDto("Plan a trip", "museums");

        when(ollamaService.getResponse("Plan a trip", "museums")).thenReturn("AI response");
        when(hotelsService.getHotels()).thenReturn(null);

        Analysis analysis = analysisService.generateAnalysis(requestDto);

        assertEquals("AI response", analysis.getAnalysis());
        assertTrue(analysis.getHotels().isEmpty());
    }

    @Test
    void saveAnalysisSavesCurrentAnalysisForAuthenticatedUser() throws Exception {
        AnalysisRequestDto requestDto = analysisRequestDto("Plan a trip", "museums");
        Hotel hotel = new Hotel();
        List<Hotel> hotels = List.of(hotel);
        AppUser user = new AppUser();
        user.setUsername("alice");
        user.setAnalyses(new ArrayList<>());

        when(ollamaService.getResponse("Plan a trip", "museums")).thenReturn("AI response");
        when(hotelsService.getHotels()).thenReturn(List.of(new HotelDto()));
        when(hotelsService.converterDtoToHotel(any(HotelDto.class))).thenReturn(hotel);
        when(analysisRepository.save(any(Analysis.class))).thenAnswer(invocation -> {
            Analysis savedAnalysis = invocation.getArgument(0);
            savedAnalysis.setId(42);
            return savedAnalysis;
        });
        when(usersRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Analysis analysis = analysisService.generateAnalysis(requestDto);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "password"));

        int savedId = analysisService.saveAnalysisInDb();

        assertEquals(42, savedId);
        assertTrue(user.getAnalyses().contains(analysis));
        verify(hotelsService).addHotelsToDb(hotels);
        verify(analysisRepository).save(analysis);
        verify(usersRepository).save(user);
    }

    /** 
     * @param prompt
     * @param hobbies
     * @return AnalysisRequestDto
     */
    private AnalysisRequestDto analysisRequestDto(String prompt, String hobbies) {
        AnalysisRequestDto requestDto = new AnalysisRequestDto();
        ReflectionTestUtils.setField(requestDto, "userPrompt", prompt);
        ReflectionTestUtils.setField(requestDto, "userHobbies", hobbies);
        return requestDto;
    }
}
