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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.InvalidObjectException;
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
                           HotelsRepository hotelsRepository,
                           HotelsService hotelsService,
                           OllamaService ollamaService, UsersRepository usersRepository) {
        this.analysisRepository = analysisRepository;
        this.hotelsService = hotelsService;
        this.ollamaService = ollamaService;
        this.usersRepository = usersRepository;
    }

    public Analysis generateAnalysis(AnalysisRequestDto analysisRequestDto){
        Analysis analysis = new Analysis();

        String aiResponse = ollamaService.getResponse(analysisRequestDto.getUserPrompt(),
                                                      analysisRequestDto.getUserHobbies());

        List<HotelDto> hotelsDto = hotelsService.getHotels();

        //For testing
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

    public int saveAnalysisInDb() throws InvalidObjectException {

        hotelsService.addHotelsToDb(this.analysis.getHotels());

        if(analysisRepository.getReferenceById(analysis.getId()) != null){
            throw new InvalidObjectException("Analysis service: analysis is already saved.");
        }

        var response = analysisRepository.save(this.analysis);

        AppUser user = usersRepository.findByUsername(getUsernameFromAuth()).orElseThrow();

        user.getAnalyses().add(analysis);

        usersRepository.save(user);

        return analysis.getId();
    }


    public Analysis getAnalysisById(int id){
        Analysis analysis = analysisRepository.getReferenceById(id);

        return analysis;
    }

    public List<Analysis> getAllUserAnalysis(){
        var user = usersRepository.findByUsername(getUsernameFromAuth());

        return user.get().getAnalyses();
    }

    public int removeAnalysisFromDb(int id){
        Analysis analysis = getAnalysisById(id);

        // Remove from user's analyses list FIRST (fixes foreign key constraint)
        AppUser user = usersRepository.findByUsername(getUsernameFromAuth()).orElseThrow();
        user.getAnalyses().removeIf(a -> a.getId() == id);
        usersRepository.save(user);

        // Now safe to delete hotels and analysis
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

    public String clearAnalysis(){
        this.analysis = new Analysis();

        return "Analysis service: analysis cleared";
    }

    private String getUsernameFromAuth(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        return username;
    }
}
