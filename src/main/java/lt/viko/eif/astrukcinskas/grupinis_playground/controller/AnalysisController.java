package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.Analysis;
import lt.viko.eif.astrukcinskas.grupinis_playground.model.Hotel;
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

import javax.swing.text.html.parser.Entity;
import java.io.InvalidObjectException;
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

    @PostMapping("/analyze")
    public ResponseEntity<Analysis> generateAnalysis(@RequestBody AnalysisRequestDto analysisRequestDto){

        var response = analysisService.generateAnalysis(analysisRequestDto);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<String> saveAnalysis() throws InvalidObjectException {
        var response = analysisService.saveAnalysisInDb();

        return ResponseEntity.ok("Analysis with id: %d, saved".formatted(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AnalysisDto>> getAnalysisById(@PathVariable int id){

        var response = analysisService.getAnalysisById(id);

        AnalysisDto analysisDto = new AnalysisDto(response);

        EntityModel<AnalysisDto> model = EntityModel.of(analysisDto);

        model.add(linkTo(methodOn(AnalysisController.class)
                .getAnalysisHotels(id))
                .withRel("hotels"));

        return ResponseEntity.ok(model);
    }

    @GetMapping("/analyses")
    public ResponseEntity<CollectionModel<EntityModel<AnalysisDto>>> getAllUserAnalysis(){

        var response = analysisService.getAllUserAnalysis();

        List<EntityModel<AnalysisDto>> models = response.stream()
                .map(analysis -> {

                    AnalysisDto analysisDto = new AnalysisDto(analysis);

                    EntityModel<AnalysisDto> model = EntityModel.of(analysisDto);

                    model.add(linkTo(methodOn(AnalysisController.class)
                            .getAnalysisHotels(analysis.getId()))
                            .withRel("hotels"));

                    return model;
                })
                .toList();

        return ResponseEntity.ok(CollectionModel.of(models));
    }

    @GetMapping("/{id}/hotels")
    public ResponseEntity<CollectionModel<Link>> getAnalysisHotels(@PathVariable int id){

        var analysis = analysisService.getAnalysisById(id);

        List<Link> hotels = analysis.getHotels()
                .stream()
                .map(hotel ->

                    linkTo(methodOn(AnalysisController.class)
                            .getAnalysisHotel(id, hotel.getId()))
                            .withRel("Hotel"))
                .toList();


        return ResponseEntity.ok(CollectionModel.of(hotels));
    }

    @GetMapping("/{analysisId}/hotels/{hotelId}")
    public ResponseEntity<EntityModel<HotelDto>> getAnalysisHotel(@PathVariable int analysisId, @PathVariable int hotelId){

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> removeAnalysisFromDb(@PathVariable int id){

        var response = analysisService.removeAnalysisFromDb(id);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clearAnalysis")
    public ResponseEntity<String> clearAnalysisInMemmory(){
        var response = analysisService.clearAnalysis();

        return ResponseEntity.ok("clear");
    }

    @DeleteMapping("/clearHotels")
    public ResponseEntity<String> clearHotelsInMemmory(){
        var response = hotelsService.clearHotelsFromMemmory();

        return ResponseEntity.ok(response);
    }
}
