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

/**
 * Provides the Spring context used by internal Cucumber tests.
 *
 * <p>The configuration keeps the application behavior real while replacing slow
 * or unstable outside integrations with mocks.</p>
 */
@CucumberContextConfiguration
@SpringBootTest(classes = {GrupinisPlaygroundApplication.class, CucumberSpringConfiguration.AcceptanceTestOverrides.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    /**
     * Test-only bean overrides for external hotel and AI clients.
     */
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
