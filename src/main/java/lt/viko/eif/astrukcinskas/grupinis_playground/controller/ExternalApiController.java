package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

import lt.viko.eif.astrukcinskas.grupinis_playground.service.ExternalApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hotels")
public class ExternalApiController {

    @Autowired
    private ExternalApiService externalApiService;

    @GetMapping
    public ResponseEntity<String> getHotels(){
        var response = externalApiService.gerResponse();

        return ResponseEntity.ok(response);
    }
}
