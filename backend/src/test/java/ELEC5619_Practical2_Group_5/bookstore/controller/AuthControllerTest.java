package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.ResetPasswordRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.SignInRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.SignInResponse;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.SignUpRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.VerifyAnswerRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.VerifyUserRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService auth;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    // signup
    @Test
    void signup_Returns201_WithLocationAndBody() throws Exception {
        User saved = new User();
        saved.setUserId(10);
        saved.setUsername("alice");
        saved.setEmail("a@a.com");
        when(auth.register(any(SignUpRequest.class))).thenReturn(saved);

        SignUpRequest req = new SignUpRequest();
        req.setUsername("alice");
        req.setEmail("a@a.com");
        req.setPassword("Password1");
        req.setConfirmPassword("Password1");
        req.setName("Alice");
        req.setPhone("0400000000");
        req.setSecurityQuestion("Color?");
        req.setSecurityAnswer("Blue");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/auth/users/10"))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("a@a.com"));
    }

    @Test
    void signup_PropagatesConflict_FromService() throws Exception {
        when(auth.register(any(SignUpRequest.class)))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Username already exists"
                ));

        SignUpRequest req = new SignUpRequest();
        req.setUsername("dup");
        req.setEmail("d@d.com");
        req.setPassword("Password1");
        req.setConfirmPassword("Password1");
        req.setName("Dup");
        req.setPhone("0400000000");
        req.setSecurityQuestion("Q");
        req.setSecurityAnswer("A");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isConflict());
    }

    // signin
    @Test
    void signin_SetsCookie_AndReturnsUserInfo() throws Exception {

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authenticated = new UsernamePasswordAuthenticationToken("alice", null, authorities);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticated);

        User u = new User();
        u.setUserId(7);
        u.setUsername("alice");
        u.setName("Alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        when(jwtUtils.generateToken(eq("alice"), eq(List.of("ROLE_USER")))).thenReturn("jwt-token");
        ResponseCookie cookie = ResponseCookie.from("BOOKSTORE_AUTH", "jwt-token")
                .httpOnly(true).path("/").build();
        when(jwtUtils.buildAuthCookie(eq("jwt-token"), anyBoolean())).thenReturn(cookie);

        SignInRequest req = new SignInRequest();
        req.setUsername("alice");
        req.setPassword("Password1");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, cookie.toString()))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void signin_Returns401_OnBadCredentials() throws Exception {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad"));

        SignInRequest req = new SignInRequest();
        req.setUsername("x");
        req.setPassword("invalid1");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isUnauthorized());
    }

    // forgot: verify user
    @Test
    void verifyUser_Returns200_WhenFound() throws Exception {
        var res = new ELEC5619_Practical2_Group_5.bookstore.dto.auth.VerifyUserResponse("flow-123", "Color?");
        when(auth.startForgotFlow(any(VerifyUserRequest.class))).thenReturn(res);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/forgot/verify-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "username","alice","email","a@a.com"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowId").value("flow-123"))
                .andExpect(jsonPath("$.securityQuestion").value("Color?"));
    }

    @Test
    void verifyUser_Returns400_WhenMismatch() throws Exception {
        when(auth.startForgotFlow(any(VerifyUserRequest.class))).thenReturn(null);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/forgot/verify-user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "username","alice","email","wrong@a.com"
                                )))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect username or email"));
    }

    // forgot: verify answer
    @Test
    void verifyAnswer_Returns200_WhenTrue() throws Exception {
        when(auth.verifySecurityAnswer(any(VerifyAnswerRequest.class))).thenReturn(true);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/forgot/verify-answer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "flowId","flow-123","securityAnswer","Blue"
                                )))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"));
    }

    @Test
    void verifyAnswer_Returns400_WhenFalse() throws Exception {
        when(auth.verifySecurityAnswer(any(VerifyAnswerRequest.class))).thenReturn(false);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/forgot/verify-answer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "flowId","flow-123","securityAnswer","Wrong"
                                )))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect Security answer"));
    }

    // forgot: reset password
    @Test
    void resetPassword_Returns200_WhenTrue() throws Exception {
        when(auth.resetPassword(any(ResetPasswordRequest.class))).thenReturn(true);

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setFlowId("flow-123");
        req.setNewPassword("NewPassw1");
        req.setConfirmPassword("NewPassw1");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/forgot/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_Returns400_WhenFalse() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setFlowId("flow-123");
        req.setNewPassword("NewPassw1");
        req.setConfirmPassword("NewPassw2");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/forgot/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());
    }

    // signout
    @Test
    void signOut_Returns204_AndClearsCookie() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/auth/signout")
                )
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("BOOKSTORE_AUTH=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Max-Age=0")));
    }

    @Test
    void me_Returns401_WhenUnauthenticated() {
        var resp = authController.me("alice", null);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void userProfile_Returns401_WhenUnauthenticated() {
        var resp = authController.currentUserProfile("alice", null);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void userProfile_Returns200_WhenAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Map<String, Object> profile = Map.of("username","alice","name","Alice");
        when(auth.getUserProfile("alice")).thenReturn(profile);

        var resp = authController.currentUserProfile("alice", authentication);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("alice", ((Map<?,?>)resp.getBody()).get("username"));
    }

    @Test
    void editUserProfile_Returns401_WhenUnauthenticated() {
        var resp = authController.updateCurrentUserProfile("alice", null, Map.of());
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void editUserProfile_Returns200_WhenAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        Map<String, Object> updated = Map.of("name","Alice Zhang");
        when(auth.updateUserProfile(eq("alice"), anyMap())).thenReturn(updated);

        var resp = authController.updateCurrentUserProfile("alice", authentication, Map.of("name","Alice Zhang"));
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("Alice Zhang", ((Map<?,?>)resp.getBody()).get("name"));
    }

    @Test
    void me_Returns401_WhenAuthObjectPresentButNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        var resp = authController.me("alice", authentication);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void signOut_AddsSecureFlag_WhenRequestIsSecure() {
        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        req.setSecure(true);

        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

        var resp = authController.signOut(req);
        assertEquals(204, resp.getStatusCodeValue());
        String cookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookie);
        assertTrue(cookie.contains("BOOKSTORE_AUTH="));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.toLowerCase().contains("secure"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void signOut_AddsSecureFlag_WhenXForwardedProtoIsHttps() {
        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        req.setSecure(false);
        req.addHeader("X-Forwarded-Proto", "https");

        var resp = authController.signOut(req);
        String cookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookie);
        assertTrue(cookie.toLowerCase().contains("secure"));
    }

    @Test
    void signOut_DoesNotAddSecureFlag_WhenHttp() {
        org.springframework.mock.web.MockHttpServletRequest req = new org.springframework.mock.web.MockHttpServletRequest();
        req.setSecure(false);
        req.addHeader("X-Forwarded-Proto", "http");

        var resp = authController.signOut(req);
        String cookie = resp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookie);
        assertFalse(cookie.toLowerCase().contains("secure"));
    }

    @Test
    void userProfile_Returns401_WhenAuthPresentButNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        var resp = authController.currentUserProfile("alice", authentication);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    void editUserProfile_Returns401_WhenAuthPresentButNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);

        var resp = authController.updateCurrentUserProfile("alice", authentication, Map.of("name", "Alice"));
        assertEquals(401, resp.getStatusCodeValue());
    }

}
