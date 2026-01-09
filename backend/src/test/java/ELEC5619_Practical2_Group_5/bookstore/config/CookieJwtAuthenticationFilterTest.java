package ELEC5619_Practical2_Group_5.bookstore.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.Mockito.*;

class CookieJwtAuthenticationFilterTest {

    private CookieJwtAuthenticationFilter newFilter() throws Exception {
        Class<CookieJwtAuthenticationFilter> c = CookieJwtAuthenticationFilter.class;
        Constructor<?> ctor = Arrays.stream(c.getDeclaredConstructors()).findFirst().orElseThrow();
        ctor.setAccessible(true);
        Object[] args = Arrays.stream(ctor.getParameterTypes()).map(t -> (Object) null).toArray();
        return (CookieJwtAuthenticationFilter) ctor.newInstance(args);
    }

    private String invokeExtract(HttpServletRequest req) throws Exception {
        CookieJwtAuthenticationFilter f = newFilter();
        Method m = CookieJwtAuthenticationFilter.class
                .getDeclaredMethod("extractTokenFromCookies", HttpServletRequest.class);
        m.setAccessible(true);
        return (String) m.invoke(f, req);
    }

    @Test
    void extractToken_ReturnsNull_WhenNoCookies() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertNull(invokeExtract(req));
    }

    @Test
    void extractToken_ReturnsNull_WhenTargetCookieMissing() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("OTHER", "x"), new Cookie("SESSION", "y"));
        assertNull(invokeExtract(req));
    }

    @Test
    void extractToken_ReturnsValue_WhenTargetCookiePresent() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("OTHER", "x"),
                new Cookie(JwtUtils.COOKIE_NAME, "jwt-123"));
        assertEquals("jwt-123", invokeExtract(req));
    }

    @Test
    void extractToken_PicksFirstMatch_WhenDuplicateCookiesExist() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie(JwtUtils.COOKIE_NAME, "first"),
                new Cookie(JwtUtils.COOKIE_NAME, "second"));
        assertEquals("first", invokeExtract(req));
    }

    private CookieJwtAuthenticationFilter newFilterWithJwt(JwtUtils jwt) throws Exception {
        Constructor<?> ctor = Arrays.stream(CookieJwtAuthenticationFilter.class.getDeclaredConstructors())
                .findFirst().orElseThrow();
        ctor.setAccessible(true);
        Object[] args = Arrays.stream(ctor.getParameterTypes()).map(t -> (Object) null).toArray();
        CookieJwtAuthenticationFilter filter =
                (CookieJwtAuthenticationFilter) ctor.newInstance(args);

        Field f = CookieJwtAuthenticationFilter.class.getDeclaredField("jwtUtils");
        f.setAccessible(true);
        f.set(filter, jwt);
        return filter;
    }

    @AfterEach
    void clearCtx() {
        SecurityContextHolder.clearContext();
    }


    private MockHttpServletRequest req(String path) {
        MockHttpServletRequest r = new MockHttpServletRequest("GET", path);
        r.setServletPath(path);
        return r;
    }

    @Test
    void shouldNotFilter_ReturnsTrue_ForAuthEndpoints() throws Exception {
        JwtUtils jwt = mock(JwtUtils.class);
        CookieJwtAuthenticationFilter filter = newFilterWithJwt(jwt);

        for (String p : List.of(
                "/api/auth/signin",
                "/api/auth/signup",
                "/api/auth/signout",
                "/error",
                "/actuator/health"
        )) {
            assertTrue(filter.shouldNotFilter(req(p)), "path should be skipped: " + p);
        }
    }

    @Test
    void shouldNotFilter_ReturnsFalse_ForNonAuthEndpoints() throws Exception {
        JwtUtils jwt = mock(JwtUtils.class);
        CookieJwtAuthenticationFilter filter = newFilterWithJwt(jwt);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/books/123");
        assertFalse(filter.shouldNotFilter(req));
    }


    @Test
    void doFilterInternal_ShortCircuits_WhenAlreadyAuthenticated() throws Exception {
        JwtUtils jwt = mock(JwtUtils.class);
        CookieJwtAuthenticationFilter filter = newFilterWithJwt(jwt);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("bob", null, List.of())
        );

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/books");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, resp, chain);

        verify(chain, times(1)).doFilter(req, resp);
        verifyNoInteractions(jwt);
    }

    @Test
    void doFilterInternal_ContinuesChain_WhenNoToken() throws Exception {
        JwtUtils jwt = mock(JwtUtils.class);
        CookieJwtAuthenticationFilter filter = newFilterWithJwt(jwt);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/books");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, resp, chain);

        verify(chain, times(1)).doFilter(req, resp);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwt);
    }

    @Test
    void doFilterInternal_SetsAuthentication_WhenTokenValid() throws Exception {
        JwtUtils jwt = mock(JwtUtils.class);
        CookieJwtAuthenticationFilter filter = newFilterWithJwt(jwt);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/books");
        req.setCookies(new Cookie(JwtUtils.COOKIE_NAME, "jwt-123"));
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        @SuppressWarnings("unchecked")
        Jws<Claims> jws = (Jws<Claims>) mock(Jws.class);
        Claims claims = mock(Claims.class);
        when(jws.getBody()).thenReturn(claims);
        when(jwt.parseAndValidate("jwt-123")).thenReturn(jws);

        try (MockedStatic<JwtUtils> statics = mockStatic(JwtUtils.class)) {
            statics.when(() -> JwtUtils.getUsername(claims)).thenReturn("alice");
            statics.when(() -> JwtUtils.getRoles(claims)).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));

            filter.doFilterInternal(req, resp, chain);

            verify(chain, times(1)).doFilter(req, resp);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals("alice", auth.getPrincipal());
            assertEquals(2, auth.getAuthorities().size());
            assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
            assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            assertNotNull(auth.getDetails());
        }
    }

    @Test
    void doFilterInternal_CatchesJwtException_AndContinuesChain() throws Exception {
        JwtUtils jwt = mock(JwtUtils.class);
        CookieJwtAuthenticationFilter filter = newFilterWithJwt(jwt);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/books");
        req.setCookies(new Cookie(JwtUtils.COOKIE_NAME, "bad-token"));
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwt.parseAndValidate("bad-token")).thenThrow(new JwtException("bad"));

        try (MockedStatic<JwtUtils> ignored = mockStatic(JwtUtils.class)) {
            filter.doFilterInternal(req, resp, chain);
        }

        verify(chain, times(1)).doFilter(req, resp);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
