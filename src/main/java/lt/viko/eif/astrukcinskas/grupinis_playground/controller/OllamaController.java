package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

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

    public OllamaController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @GetMapping
    public ResponseEntity<String> getResponse(@RequestParam String prompt){
        String response = ollamaService.getResponse(prompt);

        return ResponseEntity.ok(response);
    }
}
