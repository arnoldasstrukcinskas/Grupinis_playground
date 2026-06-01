package lt.viko.eif.astrukcinskas.grupinis_playground.service.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for JWT generation, username extraction, and blacklist tracking.
 */
class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateTokenCreatesTokenForUsername() {
        String token = jwtService.generateToken("alice");

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
        assertEquals("alice", jwtService.extractUsername(token));
    }

    @Test
    void blacklistedTokenIsReportedAsBlacklisted() {
        String token = "jwt-token";

        assertFalse(jwtService.isBlacklisted(token));

        jwtService.toBlackList(token);

        assertTrue(jwtService.isBlacklisted(token));
    }
}
