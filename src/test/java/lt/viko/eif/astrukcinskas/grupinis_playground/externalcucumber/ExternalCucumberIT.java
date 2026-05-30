package lt.viko.eif.astrukcinskas.grupinis_playground.externalcucumber;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("external-features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "lt.viko.eif.astrukcinskas.grupinis_playground.externalcucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class ExternalCucumberIT {
}
