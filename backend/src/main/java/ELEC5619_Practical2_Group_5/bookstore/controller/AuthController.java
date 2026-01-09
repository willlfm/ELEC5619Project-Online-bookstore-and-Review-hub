package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.*;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public AuthController(AuthService auth, AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository) {
       this.auth = auth;
       this.authenticationManager = authenticationManager;
       this.jwtUtils = jwtUtils;
       this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> register(@Valid @RequestBody SignUpRequest req) {
        User u = auth.register(req);
        return ResponseEntity
                .created(URI.create("/api/auth/users/" + u.getUserId()))
                .body(new SignUpResponse(u.getUserId(), u.getUsername(), u.getEmail()));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest req, HttpServletRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            String username = auth.getName();
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String token = jwtUtils.generateToken(username, roles);
            boolean secure = request.isSecure()
                    || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
            ResponseCookie cookie = jwtUtils.buildAuthCookie(token, secure);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new SignInResponse(user.getUserId(), username, user.getName(), roles)); // 不包含 token
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }

    @PostMapping("/forgot/verify-user")
    public ResponseEntity<?> verifyUser(@Valid @RequestBody VerifyUserRequest req) {
        VerifyUserResponse res = auth.startForgotFlow(req);
        if (res == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Incorrect username or email"));
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/forgot/verify-answer")
    public ResponseEntity<?> verifyAnswer(@Valid @RequestBody VerifyAnswerRequest req) {
        boolean ok = auth.verifySecurityAnswer(req);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", "Incorrect Security answer"));
        }
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    @PostMapping("/forgot/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        boolean ok = auth.resetPassword(req);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match"));
        }
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal String username, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(new SignInResponse(user.getUserId(), username, user.getName(), roles));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOut(HttpServletRequest req) {
        boolean secure = req.isSecure()
                || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));

        ResponseCookie clear = ResponseCookie.from("BOOKSTORE_AUTH", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .build();
    }

    @GetMapping("/userprofile")
    public ResponseEntity<Map<String, Object>> currentUserProfile(
            @AuthenticationPrincipal String username,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> profile = auth.getUserProfile(username);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/edituserprofile")
    public ResponseEntity<?> updateCurrentUserProfile(
            @AuthenticationPrincipal String username,
            Authentication authentication,
            @RequestBody Map<String, Object> body
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        Map<String, Object> updated = auth.updateUserProfile(username, body);
        return ResponseEntity.ok(updated);
    }

}
