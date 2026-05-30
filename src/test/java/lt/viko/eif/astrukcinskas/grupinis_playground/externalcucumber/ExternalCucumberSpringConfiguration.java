package lt.viko.eif.astrukcinskas.grupinis_playground.externalcucumber;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = ExternalCucumberSpringConfiguration.Config.class)
public class ExternalCucumberSpringConfiguration {

    @Configuration
    static class Config {
    }
}
