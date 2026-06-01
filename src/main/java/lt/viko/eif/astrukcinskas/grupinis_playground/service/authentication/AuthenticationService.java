package lt.viko.eif.astrukcinskas.grupinis_playground.service.authentication;

import javax.security.sasl.AuthenticationException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.AppUser;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.UsersRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.LoginDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.RegisterDto;

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

    public String register(RegisterDto registerDto)
    {
        AppUser user = new AppUser();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());

        usersRepository.save(user);

        return user.getUsername();
    }

    public String login(LoginDto loginDto) throws AuthenticationException {
        var user = usersRepository.findByUsername(loginDto.getUsername());

        if(user.isEmpty()){
            throw new AuthenticationException("Authentication service: Such username not found");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        System.out.println(jwtService.generateToken(loginDto.getUsername()));
        // String token = jwtService.generateToken(loginDto.getUsername());

        // return new LoginResponseDto("User logged in", token);
        return jwtService.generateToken(loginDto.getUsername());
    }

    public String logout(String token){
        if(token.isEmpty()){
            throw new IllegalArgumentException("Authentivation service: there is no token");
        }

        blacklist(token);
        String username = jwtService.extractUsername(token);
        return username;
    }

    private void blacklist(String token) {
        jwtService.toBlackList(token);
    }
}
