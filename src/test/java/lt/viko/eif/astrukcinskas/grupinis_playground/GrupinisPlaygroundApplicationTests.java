package lt.viko.eif.astrukcinskas.grupinis_playground;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring application context starts successfully with the isolated test profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class GrupinisPlaygroundApplicationTests {

    @Test
    void contextLoads() {
    }

}
