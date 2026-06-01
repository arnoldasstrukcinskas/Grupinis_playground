package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.LocationDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelRequestDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.ExternalApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InvalidObjectException;
import java.util.List;

@RestController
@RequestMapping("/hotels")
public class ExternalApiController {

    @Autowired
    private ExternalApiService externalApiService;

    /**
     * Gets locations from api
     * @param location location name
     * @return List of locations
     */
    @GetMapping
    public ResponseEntity<List<LocationDto>> getLocations(@RequestParam String location){

        if (location.isBlank() || location.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        List<LocationDto> response = externalApiService.getLocations(location);

        return ResponseEntity.ok(response);
    }

    /**
     * Gets hotels from API
     * @param requestDto request data transfer object
     * @return list of hotels
     */
    @PostMapping
    public ResponseEntity<List<HotelDto>> getHotels(@RequestBody HotelRequestDto requestDto){

        if (requestDto == null){
           return ResponseEntity.badRequest().build();
        }

        List<HotelDto> hotels = null;
        try {
            hotels = externalApiService.getHotels(requestDto);
        } catch (InvalidObjectException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(hotels);
    }
}
