package lt.viko.eif.astrukcinskas.grupinis_playground.service.authentication;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.AppUser;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.UsersRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.LoginDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.RegisterDto;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.sasl.AuthenticationException;
import java.io.InvalidObjectException;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthenticationService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Function for registering user in the system andsaving user in database
     * @param registerDto registration data transfer object
     * @return username of registered user
     * @throws InvalidObjectException
     */
    public String register(RegisterDto registerDto) throws InvalidObjectException {

        if (registerDto == null){
            throw new InvalidObjectException("Authentication service: Missing data for registration");
        }

        AppUser user = new AppUser();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());

        usersRepository.save(user);

        return user.getUsername();
    }

    /**
     * Logins user to the system
     * @param loginDto login data transfer object
     * @return return username of logged-in user
     * @throws AuthenticationException
     */
    public String login(LoginDto loginDto) throws AuthenticationException, InvalidObjectException {

        if (loginDto == null){
            throw new InvalidObjectException("Authentication service: missing login data");
        }

        var user = usersRepository.findByUsername(loginDto.getUsername());

        if(user.isEmpty()){
            throw new AuthenticationException("Authentication service: Such username not found");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        System.out.println(jwtService.generateToken(loginDto.getUsername()));

        return jwtService.generateToken(loginDto.getUsername());
    }


    /**
     * Logs out user from system
     * @param token bearer token
     * @return Logged-out user username
     */
    public String logout(String token){
        if(token.isEmpty()){
            throw new IllegalArgumentException("Authentivation service: there is no token");
        }

        blacklist(token);
        String username = jwtService.extractUsername(token);
        return username;
    }

    /**
     * Function for adding token to blacklist
     * @param token bearer token
     */
    private void blacklist(String token) {
        jwtService.toBlackList(token);
    }
}
