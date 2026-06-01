package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.Analysis;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.AnalysisService;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AnalysisDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AnalysisRequestDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.HotelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InvalidObjectException;
import java.security.InvalidParameterException;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    @Autowired
    private final AnalysisService analysisService;

    @Autowired
    private final HotelsService hotelsService;


    public AnalysisController(AnalysisService analysisService, HotelsService hotelsService) {
        this.analysisService = analysisService;
        this.hotelsService = hotelsService;
    }


    /**
     * Method for generating analysis on user data and hotels
     * @param analysisRequestDto analysis data transfer object
     * @return Analysis object with made analysis and analysed hotels
     */
    @PostMapping("/analyze")
    public ResponseEntity<Analysis> generateAnalysis(@RequestBody AnalysisRequestDto analysisRequestDto){

        if (analysisRequestDto == null)
        {
            return ResponseEntity.badRequest().build();
        }
        Analysis response = null;

        try {
            response = analysisService.generateAnalysis(analysisRequestDto);
        } catch (InvalidObjectException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Function for saving analysis in database
     * @return Message with saved analysis id
     */
    @PostMapping
    public ResponseEntity<String> saveAnalysis() {
        try {
            var response = analysisService.saveAnalysisInDb();

            return ResponseEntity.ok("Analysis with id: %d, saved".formatted(response));

        } catch (InvalidObjectException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    /**
     * Function for getting analysis by id
     * @param id analysis id
     * @return Analysis data transfer object
     * @throws InvalidObjectException
     */
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AnalysisDto>> getAnalysisById(@PathVariable int id) throws InvalidObjectException {

        if (id <= 0){
            return ResponseEntity.badRequest().build();
        }

        Analysis response;

        try {
            response = analysisService.getAnalysisById(id);

        } catch (InvalidParameterException e){
            return ResponseEntity.badRequest().build();

        }

        AnalysisDto analysisDto = new AnalysisDto(response);

        EntityModel<AnalysisDto> model = EntityModel.of(analysisDto);

        model.add(linkTo(methodOn(AnalysisController.class)
                .getAnalysisHotels(id))
                .withRel("hotels"));

        return ResponseEntity.ok(model);

    }

    /**
     * Function for getting all user analysis
     * @return List of made analysis
     */
    @GetMapping("/analyses")
    public ResponseEntity<CollectionModel<EntityModel<AnalysisDto>>> getAllUserAnalysis(){

        var response = analysisService.getAllUserAnalysis();

        List<EntityModel<AnalysisDto>> models = response.stream()
                .map(analysis -> {

                    AnalysisDto analysisDto = new AnalysisDto(analysis);

                    EntityModel<AnalysisDto> model = EntityModel.of(analysisDto);

                    try {
                        model.add(linkTo(methodOn(AnalysisController.class)
                                .getAnalysisHotels(analysis.getId()))
                                .withRel("hotels"));
                    } catch (InvalidObjectException e) {
                        throw new RuntimeException(e);
                    }


                    return model;
                })
                .toList();

        return ResponseEntity.ok(CollectionModel.of(models));
    }

    /**
     * Function for generating links using HATEOAS to hotels in analysis
     * @param id analysis id
     * @return Links of hotels in analysis
     * @throws InvalidObjectException
     */
    @GetMapping("/{id}/hotels")
    public ResponseEntity<CollectionModel<Link>> getAnalysisHotels(@PathVariable int id) throws InvalidObjectException {

        var analysis = analysisService.getAnalysisById(id);

        List<Link> hotels = analysis.getHotels()
                .stream()
                .map(hotel ->

                {
                    try {
                        return linkTo(methodOn(AnalysisController.class)
                                .getAnalysisHotel(id, hotel.getId()))
                                .withRel("Hotel");
                    } catch (InvalidObjectException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();


        return ResponseEntity.ok(CollectionModel.of(hotels));
    }


    /**
     * Function for getting link for hotel in analysis using HATEOAS
     * @param analysisId analysis id
     * @param hotelId hotel id
     * @return returns List of hotel data transfer object
     * @throws InvalidObjectException
     */
    @GetMapping("/{analysisId}/hotels/{hotelId}")
    public ResponseEntity<EntityModel<HotelDto>> getAnalysisHotel(@PathVariable int analysisId, @PathVariable int hotelId) throws InvalidObjectException {

        var analysis = analysisService.getAnalysisById(analysisId);

        var hotel = analysis.getHotels()
                .stream()
                .filter(h -> h.getId() == hotelId)
                .findFirst()
                .orElse(null);

        HotelDto hotelDto = new HotelDto(hotel);


        EntityModel<HotelDto> model = EntityModel.of(hotelDto);

        model.add(linkTo(methodOn(AnalysisController.class)
                .getAnalysisHotels(analysisId))
                .withRel("hotels"));

        return ResponseEntity.ok(model);
    }

    /**
     * Removes analysis from databse
     * @param id analysis id
     * @return returns id of removed analysis
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> removeAnalysisFromDb(@PathVariable int id){

        if (id <= 0){
            return ResponseEntity.badRequest().build();
        }

        int response = 0;
        try {
            response = analysisService.removeAnalysisFromDb(id);
        } catch (InvalidObjectException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Clears analysis data in memory
     * @return Message of success
     */
    @DeleteMapping("/clearAnalysis")
    public ResponseEntity<String> clearAnalysisInMemmory(){

        var response = analysisService.clearAnalysis();

        return ResponseEntity.ok("clear");
    }

    /**
     * Clears hotels in memory
     * @return Message of success
     */
    @DeleteMapping("/clearHotels")
    public ResponseEntity<String> clearHotelsInMemmory(){
        var response = hotelsService.clearHotelsFromMemmory();

        return ResponseEntity.ok(response);
    }
}
