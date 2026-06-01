package lt.viko.eif.astrukcinskas.grupinis_playground.externalcucumber;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Supplies the minimal Spring context required by external Cucumber step definitions.
 */
@CucumberContextConfiguration
@ContextConfiguration(classes = ExternalCucumberSpringConfiguration.Config.class)
public class ExternalCucumberSpringConfiguration {

    /**
     * Empty configuration marker so Cucumber Spring can create a test context.
     */
    @Configuration
    static class Config {
    }
}
