package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.LocationDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.HotelRequestDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.ExternalApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
public class ExternalApiController {

    @Autowired
    private ExternalApiService externalApiService;

    @GetMapping
    public ResponseEntity<List<LocationDto>> getLocations(@RequestParam String location){
        List<LocationDto> response = externalApiService.getLocations(location);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<List<HotelDto>> getHotels(@RequestBody HotelRequestDto requestDto){
        List<HotelDto> hotels = externalApiService.getHotels(requestDto);

        return ResponseEntity.ok(hotels);
    }
}
