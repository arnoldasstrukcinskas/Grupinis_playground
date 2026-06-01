package lt.viko.eif.astrukcinskas.grupinis_playground.service.authentication;

import java.util.Optional;

import javax.security.sasl.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import lt.viko.eif.astrukcinskas.grupinis_playground.model.AppUser;
import lt.viko.eif.astrukcinskas.grupinis_playground.repository.UsersRepository;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.LoginDto;
import lt.viko.eif.astrukcinskas.grupinis_playground.service.DTO.AuthDTO.RegisterDto;

/**
 * Unit tests for registration, login, and logout behavior in the authentication service.
 */
class AuthenticationServiceTest {

    private UsersRepository usersRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        usersRepository = mock(UsersRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        authenticationService = new AuthenticationService(
                usersRepository,
                passwordEncoder,
                authenticationManager,
                jwtService);
    }
    /** 
     * @throws Exception
     */
    @Test
    void registerSavesUserWithEncodedPassword() {
        RegisterDto registerDto = registerDto("alice", "plain-password", "alice@example.com");
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        String username = authenticationService.register(registerDto);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(usersRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();
        assertEquals("alice", username);
        assertEquals("alice", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("alice@example.com", savedUser.getEmail());
    }

    /** 
     * @throws Exception
     */
    @Test
    void loginAuthenticatesUserAndReturnsJwtToken() throws Exception {
        LoginDto loginDto = loginDto("alice", "password");
        AppUser user = new AppUser();
        user.setUsername("alice");

        when(usersRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("alice")).thenReturn("jwt-token");

        String token = authenticationService.login(loginDto);

        assertEquals("jwt-token", token);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, atLeastOnce()).generateToken("alice");
    }

    /** 
     * @throws Exception
     */
    @Test
    void loginThrowsWhenUsernameDoesNotExist() {
        LoginDto loginDto = loginDto("missing-user", "password");
        when(usersRepository.findByUsername("missing-user")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authenticationService.login(loginDto));

        verifyNoInteractions(authenticationManager);
        verify(jwtService, never()).generateToken("missing-user");
    }

    /** 
     * @throws Exception
     */
    @Test
    void logoutBlacklistsTokenAndReturnsUsername() {
        when(jwtService.extractUsername("jwt-token")).thenReturn("alice");

        String username = authenticationService.logout("jwt-token");

        assertEquals("alice", username);
        verify(jwtService).toBlackList("jwt-token");
        verify(jwtService).extractUsername("jwt-token");
    }

    /** 
     * @throws Exception
     */
    @Test
    void logoutThrowsWhenTokenIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> authenticationService.logout(""));
    }

    /** 
     * @param username
     * @param password
     * @param email
     * @return RegisterDto
     */
    private RegisterDto registerDto(String username, String password, String email) {
        RegisterDto registerDto = new RegisterDto();
        ReflectionTestUtils.setField(registerDto, "username", username);
        ReflectionTestUtils.setField(registerDto, "password", password);
        ReflectionTestUtils.setField(registerDto, "email", email);
        return registerDto;
    }

    /** 
     * @param username
     * @param password
     * @return LoginDto
     */
    private LoginDto loginDto(String username, String password) {
        LoginDto loginDto = new LoginDto();
        ReflectionTestUtils.setField(loginDto, "username", username);
        ReflectionTestUtils.setField(loginDto, "password", password);
        return loginDto;
    }
}
