package lt.viko.eif.astrukcinskas.grupinis_playground.service;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.Analysis;
import lt.viko.eif.astrukcinskas.grupinis_playground.model.AppUser;
import lt.viko.eif.astrukcinskas.grupinis_playground.model.Hotel;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.AnalysisRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.HotelsRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.UsersRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AnalysisRequestDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.InvalidObjectException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {

    @Autowired
    private final AnalysisRepository analysisRepository;

    @Autowired
    private final HotelsService hotelsService;

    @Autowired
    private final OllamaService ollamaService;

    @Autowired
    private final UsersRepository usersRepository;

    private Analysis analysis;

    public AnalysisService(AnalysisRepository analysisRepository,
                           HotelsService hotelsService,
                           OllamaService ollamaService, UsersRepository usersRepository) {
        this.analysisRepository = analysisRepository;
        this.hotelsService = hotelsService;
        this.ollamaService = ollamaService;
        this.usersRepository = usersRepository;
    }

    /**
     * Generates analysis from data in analysis request data transfer object and from hotels
     * received from APi by user request
     * @param analysisRequestDto Analysis data transfer object
     * @return returns Analysis object.
     * @throws InvalidObjectException
     */
    public Analysis generateAnalysis(AnalysisRequestDto analysisRequestDto) throws InvalidObjectException {

        if (analysisRequestDto == null){
            throw new InvalidObjectException("Analysis service: missing analysis data");
        }

        Analysis analysis = new Analysis();

        String aiResponse = ollamaService.getResponse(analysisRequestDto.getUserPrompt(),
                                                      analysisRequestDto.getUserHobbies());

        List<HotelDto> hotelsDto = hotelsService.getHotels();

        if (hotelsDto == null) {
            hotelsDto = new ArrayList<>();
        }

        List<Hotel> hotels = new ArrayList<>();

        for(HotelDto hotelDto : hotelsDto){
            Hotel hotel = hotelsService.converterDtoToHotel(hotelDto);
            hotels.add(hotel);
        }
        analysis.setAnalysis(aiResponse);
        analysis.setHotels(hotels);

        this.analysis = analysis;
        return analysis;
    }


    /**
     * Function for saving analysis in database
     * @return id of saved analysis
     * @throws InvalidObjectException
     */
    public int saveAnalysisInDb() throws InvalidObjectException {

        if(analysisRepository.existsById(analysis.getId())){
            throw new InvalidObjectException("Analysis service: analysis is already saved.");
        }

        hotelsService.addHotelsToDb(this.analysis.getHotels());

        var response = analysisRepository.save(this.analysis);

        AppUser user = usersRepository.findByUsername(getUsernameFromAuth()).orElseThrow();

        user.getAnalyses().add(analysis);

        usersRepository.save(user);

        return analysis.getId();
    }

    /**
     * Gets analysis by analysis id from databas
     * @param id analysis id
     * @return Analysis object
     */
    public Analysis getAnalysisById(int id) {

        if (!usersRepository.existsById(id)){
            throw new InvalidParameterException("Analysis service: such analysis do not exists");
        }

        Analysis analysis = analysisRepository.getReferenceById(id);

        return analysis;
    }

    /**
     * Gets all user generated and saved analysis
     * @return List of made analysis
     */
    public List<Analysis> getAllUserAnalysis(){

        var user = usersRepository.findByUsername(getUsernameFromAuth());

        return user.get().getAnalyses();
    }

    /**
     * Removes anlysis from database by analysis id
     * @param id analysis id
     * @return id of removed analysis
     * @throws InvalidObjectException
     */
    public int removeAnalysisFromDb(int id) throws InvalidObjectException {

        if (id <= 0){
            throw new InvalidParameterException("Analysis service: such analysis do not exists");
        }

        Analysis analysis = getAnalysisById(id);

        AppUser user = usersRepository.findByUsername(getUsernameFromAuth()).orElseThrow();
        user.getAnalyses().removeIf(a -> a.getId() == id);
        usersRepository.save(user);

        List<Integer> hotelsIds = new ArrayList<>();
        List<Hotel> hotels = analysis.getHotels();

        if(hotels == null || hotels.isEmpty()) {
            hotels = new ArrayList<>();
        }

        for(Hotel hotel : hotels){
            hotelsIds.add(hotel.getId());
        }

        analysisRepository.deleteById(id);

        for(Integer hotelId : hotelsIds){
            hotelsService.removeHotelFromDb(hotelId);
        }

        return id;
    }


    /**
     * Clears analysis in memory
     * @return Message about process.
     */
    public String clearAnalysis(){
        this.analysis = new Analysis();

        return "Analysis service: analysis cleared";
    }


    /**
     * Method for getting user username from token in spring security
     * @return user username
     */
    private String getUsernameFromAuth(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        return username;
    }
}
