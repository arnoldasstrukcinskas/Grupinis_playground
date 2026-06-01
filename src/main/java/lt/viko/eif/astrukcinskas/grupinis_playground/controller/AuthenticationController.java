package lt.viko.eif.astrukcinskas.grupinis_playground.controller;

import javax.security.sasl.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.LoginDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.RegisterDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.authentication.AuthenticationService;

import java.io.InvalidObjectException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationManager authenticationManager, AuthenticationService authenticationService) {
        this.authenticationManager = authenticationManager;
        this.authenticationService = authenticationService;
    }

    /**
     * Function for user registration in the system
     * @param registerDto register data transfer object
     * @return Message with user username if registration successful
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto)
    {
        if (registerDto == null){
            return ResponseEntity.badRequest().build();
        }
        String response = null;

        try {
            response = authenticationService.register(registerDto);
        } catch (InvalidObjectException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("User registered: %s".formatted(response));
    }

    /**
     * Function for user to log in to the system
     * @param loginDto login data transfer object
     * @return returns logged-in username of user
     * @throws AuthenticationException
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto loginDto) throws AuthenticationException {

        if(loginDto == null){
            return  ResponseEntity.badRequest().build();
        }

        String response = null;

        try {
            response = authenticationService.login(loginDto);
        } catch (InvalidObjectException e) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(response);
    }


    /**
     * Logs out user from the system
     * @param token bearer token
     * @return logged oud user username
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String token)
    {

        if (token.isEmpty() || token.isBlank())
        {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token.");
        }
        var response = authenticationService.logout(token);

        return ResponseEntity.ok("User with username: %s, logged out.".formatted(response));
    }
}
