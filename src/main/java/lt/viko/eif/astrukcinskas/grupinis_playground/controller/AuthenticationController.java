package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

import lt.viko.eif.astrukcinskas.grupinis_playground.service.authentication.AuthenticationService;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.LoginDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.RegisterDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.security.sasl.AuthenticationException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationManager authenticationManager, AuthenticationService authenticationService) {
        this.authenticationManager = authenticationManager;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto)
    {
        var response = authenticationService.register(registerDto);
        return ResponseEntity.ok("User registered: %s".formatted(response));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto loginDto) throws AuthenticationException {
        var response = authenticationService.login(loginDto);

        return ResponseEntity.ok("User logged in");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String token)
    {
        var response = authenticationService.logout(token);

        return ResponseEntity.ok("User with username: %s, logged out.".formatted(response));
    }
}
