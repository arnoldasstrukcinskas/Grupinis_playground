package lt.viko.eif.astrukcinskas.grupinis_playground.service.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
public class JwtService {

    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250655368566D5971";
    private final Set<String> blacklistedTokens = new HashSet<>();

    private Key key() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username){

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(key())
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void toBlackList(String token){
        blacklistedTokens.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
