package ELEC5619_Practical2_Group_5.bookstore.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig newConfig() throws Exception {
        var ctor = java.util.Arrays.stream(SecurityConfig.class.getDeclaredConstructors())
                .findFirst().orElseThrow();
        ctor.setAccessible(true);
        Object[] args = java.util.Arrays.stream(ctor.getParameterTypes())
                .map(t -> org.mockito.Mockito.mock(t))
                .toArray();
        return (SecurityConfig) ctor.newInstance(args);
    }

    private static void setAllowedOrigins(SecurityConfig cfg, String value) throws Exception {
        Field f = SecurityConfig.class.getDeclaredField("allowedOrigins");
        f.setAccessible(true);
        f.set(cfg, value);
    }

    private static CorsConfiguration extractCors(CorsConfigurationSource src, String path, String origin) {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", path);
        if (origin != null) req.addHeader(HttpHeaders.ORIGIN, origin);
        return src.getCorsConfiguration(req);
    }

    @Test
    void corsConfigurationSource_ReturnsEmptyOrigins_AndNoCredentials_WhenPropertyBlank() throws Exception {
        SecurityConfig cfg = newConfig();
        setAllowedOrigins(cfg, "   ");

        CorsConfigurationSource source = cfg.corsConfigurationSource();
        assertNotNull(source);
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);

        CorsConfiguration cc = extractCors(source, "/api/books", "https://x.test");
        assertNotNull(cc);

        List<String> origins = cc.getAllowedOrigins();
        assertNotNull(origins);
        assertTrue(origins.isEmpty());

        assertNull(cc.getAllowCredentials());

        assertEquals(List.of("Authorization", "Content-Type", "Accept"), cc.getAllowedHeaders());
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"), cc.getAllowedMethods());
    }

    @Test
    void corsConfigurationSource_EnablesCredentials_AndSingleOrigin() throws Exception {
        SecurityConfig cfg = newConfig();
        setAllowedOrigins(cfg, "https://a.com");

        CorsConfigurationSource source = cfg.corsConfigurationSource();
        CorsConfiguration cc = extractCors(source, "/api/books", "https://a.com");

        assertNotNull(cc);
        assertEquals(List.of("https://a.com"), cc.getAllowedOrigins());
        assertEquals(Boolean.TRUE, cc.getAllowCredentials());
    }

    @Test
    void corsConfigurationSource_SplitsCommaSeparatedOrigins_WithTrim() throws Exception {
        SecurityConfig cfg = newConfig();
        setAllowedOrigins(cfg, "https://a.com ,https://b.com, https://c.com");

        CorsConfigurationSource source = cfg.corsConfigurationSource();
        CorsConfiguration cc = extractCors(source, "/anything", "https://a.com");

        assertNotNull(cc);
        assertEquals(List.of("https://a.com", "https://b.com", "https://c.com"), cc.getAllowedOrigins());
        assertEquals(Boolean.TRUE, cc.getAllowCredentials());
    }

    @Test
    void authenticationManager_DelegatesToAuthenticationConfiguration() throws Exception {
        SecurityConfig cfg = newConfig();

        AuthenticationManager fake = mock(AuthenticationManager.class);
        AuthenticationConfiguration ac = mock(AuthenticationConfiguration.class);
        when(ac.getAuthenticationManager()).thenReturn(fake);

        AuthenticationManager out = cfg.authenticationManager(ac);
        assertSame(fake, out, "The authenticationManager should be delegated directly to the authenticationConfiguration.");
        verify(ac, times(1)).getAuthenticationManager();
    }

    private SecurityConfig makeConfig() throws Exception {
        var ctor = java.util.Arrays.stream(SecurityConfig.class.getDeclaredConstructors())
                .findFirst().orElseThrow();
        ctor.setAccessible(true);
        Object[] args = java.util.Arrays.stream(ctor.getParameterTypes())
                .map(t -> org.mockito.Mockito.mock(t))
                .toArray();
        return (SecurityConfig) ctor.newInstance(args);
    }

    @org.junit.jupiter.api.Test
    void passwordEncoder_IsBCrypt_AndMatches() throws Exception {
        SecurityConfig cfg = makeConfig();
        var enc = cfg.passwordEncoder();
        String raw = "s3cret!";
        String hash = enc.encode(raw);

        org.junit.jupiter.api.Assertions.assertTrue(enc.matches(raw, hash));
        org.junit.jupiter.api.Assertions.assertFalse(enc.matches("wrong", hash));
    }

    @org.junit.jupiter.api.Test
    void userDetailsService_MapsEntityToUserDetails_AndThrowsWhenNotFound() throws Exception {
        SecurityConfig cfg = makeConfig();

        ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository repo =
                org.mockito.Mockito.mock(ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository.class);

        ELEC5619_Practical2_Group_5.bookstore.entity.User u =
                new ELEC5619_Practical2_Group_5.bookstore.entity.User();
        u.setUsername("alice");
        u.setPasswordHash(cfg.passwordEncoder().encode("pw"));
        u.setRole("USER");

        org.mockito.Mockito.when(repo.findByUsername("alice"))
                .thenReturn(java.util.Optional.of(u));
        org.mockito.Mockito.when(repo.findByUsername("ghost"))
                .thenReturn(java.util.Optional.empty());

        var uds = cfg.userDetailsService(repo);

        var details = uds.loadUserByUsername("alice");
        org.junit.jupiter.api.Assertions.assertEquals("alice", details.getUsername());
        org.junit.jupiter.api.Assertions.assertTrue(
                details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> uds.loadUserByUsername("ghost")
        );
    }

    @org.junit.jupiter.api.Test
    void authenticationProvider_AuthenticatesWithUdsAndEncoder() throws Exception {
        SecurityConfig cfg = makeConfig();
        var encoder = cfg.passwordEncoder();

        org.springframework.security.core.userdetails.UserDetailsService uds = username -> {
            if (!"alice".equals(username)) {
                throw new org.springframework.security.core.userdetails.UsernameNotFoundException(username);
            }
            return org.springframework.security.core.userdetails.User
                    .withUsername("alice")
                    .password(encoder.encode("pw"))
                    .authorities("ROLE_USER")
                    .build();
        };

        var provider = (org.springframework.security.authentication.dao.DaoAuthenticationProvider)
                cfg.authenticationProvider(uds, encoder);

        var ok = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("alice", "pw");
        var auth = provider.authenticate(ok);
        org.junit.jupiter.api.Assertions.assertNotNull(auth);
        org.junit.jupiter.api.Assertions.assertTrue(auth.isAuthenticated());
        org.junit.jupiter.api.Assertions.assertEquals("alice", auth.getName());
        org.junit.jupiter.api.Assertions.assertTrue(
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
        );

        var bad = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("alice", "oops");
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.authentication.BadCredentialsException.class,
                () -> provider.authenticate(bad)
        );
    }
}
