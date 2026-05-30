package lt.viko.eif.astrukcinskas.grupinis_playground.cucumber;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;

import io.cucumber.spring.CucumberContextConfiguration;
import lt.viko.eif.astrukcinskas.grupinis_playground.GrupinisPlaygroundApplication;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.ExternalApiService;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.OllamaService;

@CucumberContextConfiguration
@SpringBootTest(classes = {GrupinisPlaygroundApplication.class, CucumberSpringConfiguration.AcceptanceTestOverrides.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @TestConfiguration
    static class AcceptanceTestOverrides {

        @Bean
        @Primary
        ExternalApiService externalApiService() {
            return mock(ExternalApiService.class);
        }

        @Bean
        @Primary
        OllamaService ollamaService() {
            return mock(OllamaService.class);
        }
    }
}
