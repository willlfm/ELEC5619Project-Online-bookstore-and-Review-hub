package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.auth.*;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private AuthServiceImpl authService;


    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository);
    }

    // --- register() ---
    @Test
    void register_ThrowsBadRequest_WhenPasswordsMismatch() {
        SignUpRequest req = mock(SignUpRequest.class);
        when(req.getPassword()).thenReturn("p1");
        when(req.getConfirmPassword()).thenReturn("p2");

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void register_ThrowsConflict_WhenUsernameExists() {
        SignUpRequest req = mock(SignUpRequest.class);
        when(req.getPassword()).thenReturn("pw");
        when(req.getConfirmPassword()).thenReturn("pw");
        when(req.getUsername()).thenReturn("alice");
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ThrowsConflict_WhenEmailExists() {
        SignUpRequest req = mock(SignUpRequest.class);
        when(req.getPassword()).thenReturn("pw");
        when(req.getConfirmPassword()).thenReturn("pw");
        when(req.getUsername()).thenReturn("alice");
        when(req.getEmail()).thenReturn("a@a.com");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("a@a.com")).thenReturn(true);

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_SavesUser_WithHashedPasswordAndAnswer() {
        SignUpRequest req = mock(SignUpRequest.class);
        when(req.getPassword()).thenReturn("pw123");
        when(req.getConfirmPassword()).thenReturn("pw123");
        when(req.getUsername()).thenReturn("alice");
        when(req.getEmail()).thenReturn("a@a.com");
        when(req.getName()).thenReturn("Alice");
        when(req.getPhone()).thenReturn("13000000000");
        when(req.getSecurityQuestion()).thenReturn("Color?");
        when(req.getSecurityAnswer()).thenReturn("Blue");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("a@a.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(1);
            return u;
        });

        User out = authService.register(req);

        verify(userRepository).save(saved.capture());
        User u = saved.getValue();

        assertEquals("alice", u.getUsername());
        assertEquals("a@a.com", u.getEmail());
        assertEquals("Alice", u.getName());
        assertNotEquals("pw123", u.getPasswordHash());
        assertNotEquals("Blue", u.getSecurityAnswer());

        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        assertTrue(enc.matches("pw123", u.getPasswordHash()));
        assertTrue(enc.matches("Blue", u.getSecurityAnswer()));

        assertEquals(1, out.getUserId());
    }

    // --- signin() ---
    @Test
    void signin_ThrowsUnauthorized_WhenUserNotFound() {
        SignInRequest req = mock(SignInRequest.class);
        when(req.getUsername()).thenReturn("nope");
        when(userRepository.findByUsername("nope")).thenReturn(Optional.empty());

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.signin(req));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void signin_ThrowsUnauthorized_WhenPasswordWrong() {
        SignInRequest req = mock(SignInRequest.class);
        when(req.getUsername()).thenReturn("alice");
        when(req.getPassword()).thenReturn("wrong");

        User u = new User();
        u.setUsername("alice");
        u.setPasswordHash(new BCryptPasswordEncoder().encode("correct"));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.signin(req));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void signin_ReturnsMap_WhenCredentialsOk() {
        SignInRequest req = mock(SignInRequest.class);
        when(req.getUsername()).thenReturn("alice");
        when(req.getPassword()).thenReturn("pw");

        User u = new User();
        u.setUserId(7);
        u.setUsername("alice");
        u.setPasswordHash(new BCryptPasswordEncoder().encode("pw"));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        Map<String, Object> res = authService.signin(req);
        assertEquals(7, res.get("userId"));
        assertEquals("alice", res.get("username"));
        assertEquals("Login successful", res.get("message"));
    }

    // --- forgot password flow ---
    @Test
    void startForgotFlow_ReturnsNull_WhenUserMissing() {
        VerifyUserRequest req = mock(VerifyUserRequest.class);
        when(req.getUsername()).thenReturn("ghost");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        VerifyUserResponse res = authService.startForgotFlow(req);
        assertNull(res);
    }

    @Test
    void startForgotFlow_ReturnsNull_WhenEmailMismatch() {
        VerifyUserRequest req = mock(VerifyUserRequest.class);
        when(req.getUsername()).thenReturn("alice");
        when(req.getEmail()).thenReturn("X@x.com");

        User u = new User();
        u.setUsername("alice");
        u.setEmail("A@a.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        VerifyUserResponse res = authService.startForgotFlow(req);
        assertNull(res);
    }

    @Test
    void startForgotFlow_ReturnsFlowId_AndSecurityQuestion() {
        VerifyUserRequest req = mock(VerifyUserRequest.class);
        when(req.getUsername()).thenReturn("alice");
        when(req.getEmail()).thenReturn("a@a.com");

        User u = new User();
        u.setUsername("alice");
        u.setEmail("a@a.com");
        u.setSecurityQuestion("Color?");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        VerifyUserResponse res = authService.startForgotFlow(req);
        assertNotNull(res);
        assertNotNull(res.getFlowId());
        assertEquals("Color?", res.getSecurityQuestion());
    }

    @Test
    void verifySecurityAnswer_ReturnsFalse_WhenFlowMissingOrExpired() {
        VerifyAnswerRequest req = mock(VerifyAnswerRequest.class);
        when(req.getFlowId()).thenReturn("nope");
        assertFalse(authService.verifySecurityAnswer(req));
    }

    @Test
    void verifySecurityAnswer_Works_WithCorrectAnswer() {
        // 1) start flow
        VerifyUserRequest vreq = mock(VerifyUserRequest.class);
        when(vreq.getUsername()).thenReturn("alice");
        when(vreq.getEmail()).thenReturn("a@a.com");
        User u = new User();
        u.setUsername("alice");
        u.setEmail("a@a.com");
        u.setSecurityQuestion("Color?");
        u.setSecurityAnswer(new BCryptPasswordEncoder().encode("Blue"));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        VerifyUserResponse flow = authService.startForgotFlow(vreq);
        assertNotNull(flow);

        // 2) answer
        VerifyAnswerRequest areq = mock(VerifyAnswerRequest.class);
        when(areq.getFlowId()).thenReturn(flow.getFlowId());
        when(areq.getSecurityAnswer()).thenReturn("Blue");

        assertTrue(authService.verifySecurityAnswer(areq));

        // wrong answer
        VerifyAnswerRequest wrong = mock(VerifyAnswerRequest.class);
        when(wrong.getFlowId()).thenReturn(flow.getFlowId());
        when(wrong.getSecurityAnswer()).thenReturn("Green");
        assertFalse(authService.verifySecurityAnswer(wrong));
    }

    @Test
    void resetPassword_ReturnsFalse_WhenFlowMissing() {
        ResetPasswordRequest req = mock(ResetPasswordRequest.class);
        when(req.getFlowId()).thenReturn("nope");
        assertFalse(authService.resetPassword(req));
    }

    @Test
    void resetPassword_ReturnsFalse_WhenPasswordsMismatch() {
        // prepare flow
        VerifyUserRequest vreq = mock(VerifyUserRequest.class);
        when(vreq.getUsername()).thenReturn("alice");
        when(vreq.getEmail()).thenReturn("a@a.com");
        User u = new User();
        u.setUsername("alice");
        u.setEmail("a@a.com");
        u.setSecurityQuestion("Q");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));
        VerifyUserResponse flow = authService.startForgotFlow(vreq);

        ResetPasswordRequest r = mock(ResetPasswordRequest.class);
        when(r.getFlowId()).thenReturn(flow.getFlowId());
        when(r.getNewPassword()).thenReturn("p1");
        when(r.getConfirmPassword()).thenReturn("p2");

        assertFalse(authService.resetPassword(r));
    }

    @Test
    void resetPassword_UpdatesHash_WhenOk() {
        // prepare flow + user
        VerifyUserRequest vreq = mock(VerifyUserRequest.class);
        when(vreq.getUsername()).thenReturn("alice");
        when(vreq.getEmail()).thenReturn("a@a.com");
        User u = new User();
        u.setUsername("alice");
        u.setEmail("a@a.com");
        u.setSecurityQuestion("Q");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));
        VerifyUserResponse flow = authService.startForgotFlow(vreq);

        ResetPasswordRequest r = mock(ResetPasswordRequest.class);
        when(r.getFlowId()).thenReturn(flow.getFlowId());
        when(r.getNewPassword()).thenReturn("newPw");
        when(r.getConfirmPassword()).thenReturn("newPw");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));
        boolean ok = authService.resetPassword(r);
        assertTrue(ok);

        assertTrue(new BCryptPasswordEncoder().matches("newPw", u.getPasswordHash()));
        verify(userRepository).save(u);
    }

    // --- getUserProfile / updateUserProfile ---
    @Test
    void getUserProfile_ThrowsUnauthorized_WhenUserMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.getUserProfile("ghost"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void getUserProfile_ReturnsProjectedMap() {
        User u = new User();
        u.setUsername("alice");
        u.setName("Alice");
        u.setEmail("a@a.com");
        u.setPhone("13000000000");
        u.setAddress("addr");
        u.setCity("Sydney");
        u.setState("NSW");
        u.setPostalCode("2000");
        u.setCountry("AU");
        u.setDateOfBirth(LocalDate.of(2000, 1, 2));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        Map<String, Object> m = authService.getUserProfile("alice");
        assertEquals("alice", m.get("username"));
        assertEquals("Alice", m.get("name"));
        assertEquals("a@a.com", m.get("email"));
        assertEquals("13000000000", m.get("phone"));
        assertEquals("addr", m.get("address"));
        assertEquals("Sydney", m.get("city"));
        assertEquals("NSW", m.get("state"));
        assertEquals("2000", m.get("postal_code"));
        assertEquals("AU", m.get("country"));
        assertEquals("2000-01-02", m.get("date_of_birth"));
    }

    @Test
    void updateUserProfile_ThrowsUnauthorized_WhenUserMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        ResponseStatusException ex =
                assertThrows(ResponseStatusException.class, () -> authService.updateUserProfile("ghost", Map.of()));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void updateUserProfile_UpdatesSelectedFields_AndNormalizesBlank() {
        User u = new User();
        u.setUsername("alice");
        u.setEmail("a@a.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        Map<String, Object> body = Map.of(
                "name", "Alice Zhang",
                "phone", " 13000000000 ",
                "address", " ",
                "city", "Sydney",
                "state", "NSW",
                "postal_code", "2000",
                "country", "AU",
                "date_of_birth", "2000-01-02",
                "gender", "female"
        );

        Map<String, Object> result = authService.updateUserProfile("alice", body);

        verify(userRepository).save(u);
        assertEquals("Alice Zhang", result.get("name"));
        assertEquals("13000000000", result.get("phone"));
        assertNull(u.getAddress()); // blank -> null
        assertEquals("Sydney", u.getCity());
        assertEquals("NSW", u.getState());
        assertEquals("2000", u.getPostalCode());
        assertEquals("AU", u.getCountry());
        assertEquals(LocalDate.of(2000,1,2), u.getDateOfBirth());
        assertEquals("female", u.getGender());
    }

    private static Object invokeGetViaGetters(Object target, String... names) throws Exception {
        Method m = AuthServiceImpl.class.getDeclaredMethod("getViaGetters", Object.class, String[].class);
        m.setAccessible(true);
        return m.invoke(null, target, (Object) names);
    }

    private static String invokeFormatDob(Object v) throws Exception {
        Method m = AuthServiceImpl.class.getDeclaredMethod("formatDob", Object.class);
        m.setAccessible(true);
        return (String) m.invoke(null, v);
    }

    public static class Bean {
        public String getName() { return "Alice"; }
        public String getBoom() { throw new RuntimeException("boom"); }
        public String getParam(String x) { return "X"; }
    }

    @Test
    void getViaGetters_ReturnsFirstZeroArgGetter() throws Exception {
        Object out = invokeGetViaGetters(new Bean(), "getName");
        assertEquals("Alice", out);
    }

    @Test
    void getViaGetters_SkipsThrowingAndReturnsNext() throws Exception {
        Object out = invokeGetViaGetters(new Bean(), "getBoom", "getName");
        assertEquals("Alice", out, "The getter that throws an exception should be skipped; if it is hit, a subsequent getter can be used.");
    }

    @Test
    void getViaGetters_ReturnsNull_WhenNoZeroArgGetterFoundOrMissing() throws Exception {
        Object out = invokeGetViaGetters(new Bean(), "getParam", "missingMethod");
        assertNull(out, "The getter should return null if it has no zero parameters or if the method does not exist.");
    }

    @Test
    void formatDob_ReturnsNull_OnNullInput() throws Exception {
        assertNull(invokeFormatDob(null));
    }

    @Test
    void formatDob_FormatsLocalDate_AsIso() throws Exception {
        LocalDate ld = LocalDate.of(2020, 2, 29);
        assertEquals("2020-02-29", invokeFormatDob(ld));
    }

    @Test
    void formatDob_FormatsUtilDate_AsIso_UsingSystemZone() throws Exception {
        LocalDate ld = LocalDate.of(2021, 12, 3);
        Date util = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertEquals(ld.toString(), invokeFormatDob(util));
    }

    @Test
    void formatDob_FallsBackToStringValue_ForOtherTypes() throws Exception {
        assertEquals("42", invokeFormatDob(42));
        assertEquals("true", invokeFormatDob(true));
    }

    @Test
    void getUserProfile_ExtractsFields_AndFormatsDob_LocalDate() {
        User u = new User();
        u.setUsername("alice");
        u.setName("Alice");
        u.setEmail("a@a.com");
        u.setPhone("13000000000");
        u.setAddress("addr");
        u.setCity("Sydney");
        u.setState("NSW");
        u.setPostalCode("2000");
        u.setCountry("AU");
        u.setDateOfBirth(LocalDate.of(2000, 1, 2));
        u.setGender("female");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        Map<String, Object> m = authService.getUserProfile("alice");
        assertEquals("alice", m.get("username"));
        assertEquals("Alice", m.get("name"));
        assertEquals("a@a.com", m.get("email"));
        assertEquals("13000000000", m.get("phone"));
        assertEquals("addr", m.get("address"));
        assertEquals("Sydney", m.get("city"));
        assertEquals("NSW", m.get("state"));
        assertEquals("2000", m.get("postal_code"));
        assertEquals("AU", m.get("country"));
        assertEquals("2000-01-02", m.get("date_of_birth"));
        assertEquals("female", m.get("gender"));
    }

    public static class DateUser extends User {
        private final java.util.Date dob;
        public DateUser(java.util.Date dob) { this.dob = dob; }

        @Override
        public java.time.LocalDate getDateOfBirth() {
            throw new RuntimeException("simulate failure to hit alternate getter");
        }

        public java.util.Date getBirthDate() { return dob; }
    }

    @Test
    void getUserProfile_FormatsDob_WhenProvidedByAlternateGetterAsUtilDate() {
        LocalDate ld = LocalDate.of(1999, 5, 6);
        DateUser u = new DateUser(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        u.setUsername("alice");
        u.setName("Alice");
        u.setEmail("a@a.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(u));

        Map<String, Object> m = authService.getUserProfile("alice");
        assertEquals("1999-05-06", m.get("date_of_birth")); // java.util.Date -> yyyy-MM-dd
    }

    private static void callSetDateIfPresent(Object target, Object rawVal, String... setters) throws Exception {
        java.lang.reflect.Method m = AuthServiceImpl.class
                .getDeclaredMethod("setDateIfPresent", Object.class, Object.class, String[].class);
        m.setAccessible(true);
        m.invoke(null, target, rawVal, (Object) setters);
    }

    private static java.lang.reflect.Method callFindSetter(Class<?> clazz, String name) throws Exception {
        java.lang.reflect.Method m = AuthServiceImpl.class
                .getDeclaredMethod("findSetter", Class.class, String.class);
        m.setAccessible(true);
        return (java.lang.reflect.Method) m.invoke(null, clazz, name);
    }

    private static Object callConvertToType(Object val, Class<?> pt) throws Exception {
        java.lang.reflect.Method m = AuthServiceImpl.class
                .getDeclaredMethod("convertToType", Object.class, Class.class);
        m.setAccessible(true);
        return m.invoke(null, val, pt);
    }

    private static Object callNormalizeBlankToNull(Object v) throws Exception {
        java.lang.reflect.Method m = AuthServiceImpl.class
                .getDeclaredMethod("normalizeBlankToNull", Object.class);
        m.setAccessible(true);
        return m.invoke(null, v);
    }

    static class TargetDateBean {
        private java.time.LocalDate dateOfBirth;
        private java.util.Date birthDate;

        public void setDateOfBirth(java.time.LocalDate d) { this.dateOfBirth = d; }
        public void setBirthDate(java.util.Date d) { this.birthDate = d; }

        public void setBoom(java.time.LocalDate d) { throw new RuntimeException("boom"); }

        public void setTwoArgs(String a, String b) {}

        public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
        public java.util.Date getBirthDate() { return birthDate; }
    }


    @org.junit.jupiter.api.Test
    void setDateIfPresent_SetsLocalDate_WhenIsoString() throws Exception {
        TargetDateBean t = new TargetDateBean();
        callSetDateIfPresent(t, "2020-02-29", "setDateOfBirth");
        org.junit.jupiter.api.Assertions.assertEquals(java.time.LocalDate.of(2020,2,29), t.getDateOfBirth());
        org.junit.jupiter.api.Assertions.assertNull(t.getBirthDate());
    }

    @org.junit.jupiter.api.Test
    void setDateIfPresent_SetsUtilDate_WhenIsoString() throws Exception {
        TargetDateBean t = new TargetDateBean();
        callSetDateIfPresent(t, "2021-12-03", "setBirthDate");
        java.time.LocalDate ld = t.getBirthDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        org.junit.jupiter.api.Assertions.assertEquals(java.time.LocalDate.of(2021,12,3), ld);
        org.junit.jupiter.api.Assertions.assertNull(t.getDateOfBirth());
    }

    @org.junit.jupiter.api.Test
    void setDateIfPresent_SetsNull_WhenBlank() throws Exception {
        TargetDateBean t = new TargetDateBean();
        t.setDateOfBirth(java.time.LocalDate.of(2001,1,1));
        callSetDateIfPresent(t, "   ", "setDateOfBirth");
        org.junit.jupiter.api.Assertions.assertNull(t.getDateOfBirth());
    }

    @org.junit.jupiter.api.Test
    void setDateIfPresent_SkipsThrowingSetter_ThenUsesNext() throws Exception {
        TargetDateBean t = new TargetDateBean();
        callSetDateIfPresent(t, "1999-05-06", "setBoom", "setDateOfBirth");
        org.junit.jupiter.api.Assertions.assertEquals(java.time.LocalDate.of(1999,5,6), t.getDateOfBirth());
    }


    @org.junit.jupiter.api.Test
    void findSetter_ReturnsMethod_WhenExists_AndOneParam() throws Exception {
        java.lang.reflect.Method m = callFindSetter(TargetDateBean.class, "setDateOfBirth");
        org.junit.jupiter.api.Assertions.assertNotNull(m);
        org.junit.jupiter.api.Assertions.assertEquals(1, m.getParameterCount());
        org.junit.jupiter.api.Assertions.assertEquals(java.time.LocalDate.class, m.getParameterTypes()[0]);
    }

    @org.junit.jupiter.api.Test
    void findSetter_ReturnsNull_WhenNotFound() throws Exception {
        org.junit.jupiter.api.Assertions.assertNull(callFindSetter(TargetDateBean.class, "setDoesNotExist"));
    }

    @org.junit.jupiter.api.Test
    void findSetter_ReturnsNull_WhenParamCountNotOne() throws Exception {
        org.junit.jupiter.api.Assertions.assertNull(callFindSetter(TargetDateBean.class, "setTwoArgs"));
    }


    @org.junit.jupiter.api.Test
    void convertToType_ReturnsNull_WhenValNull() throws Exception {
        org.junit.jupiter.api.Assertions.assertNull(callConvertToType(null, String.class));
    }

    @org.junit.jupiter.api.Test
    void convertToType_TrimsToString_WhenTargetIsString() throws Exception {
        org.junit.jupiter.api.Assertions.assertEquals("abc", callConvertToType("abc", String.class));
        org.junit.jupiter.api.Assertions.assertEquals("42",  callConvertToType(42, String.class));
    }

    @org.junit.jupiter.api.Test
    void convertToType_ReturnsVal_WhenTargetNotString() throws Exception {
        Object obj = 123;
        org.junit.jupiter.api.Assertions.assertSame(obj, callConvertToType(obj, Integer.class));
    }


    @org.junit.jupiter.api.Test
    void normalizeBlankToNull_ReturnsNull_OnNull() throws Exception {
        org.junit.jupiter.api.Assertions.assertNull(callNormalizeBlankToNull(null));
    }

    @org.junit.jupiter.api.Test
    void normalizeBlankToNull_ReturnsNull_OnWhitespace() throws Exception {
        org.junit.jupiter.api.Assertions.assertNull(callNormalizeBlankToNull(" \t  "));
    }

    @org.junit.jupiter.api.Test
    void normalizeBlankToNull_ReturnsTrimmedString_OnNonBlank() throws Exception {
        org.junit.jupiter.api.Assertions.assertEquals("abc", callNormalizeBlankToNull("abc"));
        org.junit.jupiter.api.Assertions.assertEquals("42",  callNormalizeBlankToNull(42));
    }
}
