package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.RequestDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.ExternalApiService;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ollama")
public class OllamaController {

    @Autowired
    private final OllamaService ollamaService;

    @Autowired
    private final ExternalApiService externalApiService;


    public OllamaController(OllamaService ollamaService, ExternalApiService externalApiService) {
        this.ollamaService = ollamaService;
        this.externalApiService = externalApiService;
    }

    @GetMapping
    public ResponseEntity<String> getResponse(@RequestParam String userPrompt,
                                              @RequestParam String interestsAndHobbies){

        String response = ollamaService.getResponse(userPrompt, interestsAndHobbies);

        return ResponseEntity.ok(response);
    }
}
