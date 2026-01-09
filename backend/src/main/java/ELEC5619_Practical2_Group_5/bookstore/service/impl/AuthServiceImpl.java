package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.auth.*;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService{
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User register(SignUpRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Two passwords are not the same.");
        }

        if (users.existsByUsername(req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (users.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setName(req.getName());
        u.setPhone(req.getPhone());
        u.setSecurityQuestion(req.getSecurityQuestion());

        // bcrypt save hashing
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setSecurityAnswer(encoder.encode(req.getSecurityAnswer()));

        return users.save(u);
    }

    public Map<String, Object> signin(SignInRequest req) {
        // username exists or not
        User user = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Incorrect username or password"
                ));

        // verify password
        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("username", user.getUsername());
        response.put("message", "Login successful");
        return response;
    }

    private final ConcurrentHashMap<String, ResetFlow> resetFlows = new ConcurrentHashMap<>();
    private static final long FLOW_TTL_SECONDS = 600L; // 10 min

    private static class ResetFlow {
        final String username;
        final String securityQuestion;
        final Instant createdAt = Instant.now();
        ResetFlow(String username, String securityQuestion) {
            this.username = username;
            this.securityQuestion = securityQuestion;
        }
    }

    private boolean expired(ResetFlow f) {
        return f == null || Instant.now().isAfter(f.createdAt.plusSeconds(FLOW_TTL_SECONDS));
    }

    // step 1: email + username verify
    public VerifyUserResponse startForgotFlow(VerifyUserRequest req) {
        Optional<User> opt = users.findByUsername(req.getUsername());
        if (opt.isEmpty()) return null;
        User u = opt.get();
        if (u.getEmail() == null || !u.getEmail().equalsIgnoreCase(req.getEmail())) {
            return null;
        }
        String flowId = UUID.randomUUID().toString();
        resetFlows.put(flowId, new ResetFlow(u.getUsername(), u.getSecurityQuestion()));
        return new VerifyUserResponse(flowId, u.getSecurityQuestion());
    }

    // step 2: Security answer
    public boolean verifySecurityAnswer(VerifyAnswerRequest req) {
        ResetFlow f = resetFlows.get(req.getFlowId());
        if (expired(f)) { resetFlows.remove(req.getFlowId()); return false; }

        Optional<User> opt = users.findByUsername(f.username);
        if (opt.isEmpty()) return false;
        User u = opt.get();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(req.getSecurityAnswer(), u.getSecurityAnswer());
    }

    // step 3: reset password
    @Transactional
    public boolean resetPassword(ResetPasswordRequest req) {
        ResetFlow f = resetFlows.get(req.getFlowId());
        if (expired(f)) { resetFlows.remove(req.getFlowId()); return false; }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return false;
        }

        Optional<User> opt = users.findByUsername(f.username);
        if (opt.isEmpty()) return false;
        User u = opt.get();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        u.setPasswordHash(encoder.encode(req.getNewPassword()));
        users.save(u);

        resetFlows.remove(req.getFlowId());
        return true;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserProfile(String username) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Map<String, Object> r = new HashMap<>();
        r.put("username", u.getUsername());
        r.put("name",     u.getName());
        r.put("gender",   getViaGetters(u, "getGender"));
        r.put("email",    u.getEmail());
        r.put("phone",    getViaGetters(u, "getPhone", "getPhoneNumber"));
        r.put("address",  getViaGetters(u, "getAddress"));
        r.put("city",     getViaGetters(u, "getCity"));
        r.put("state",    getViaGetters(u, "getState"));
        r.put("postal_code", getViaGetters(u, "getPostalCode", "getPostcode", "getZip"));
        r.put("country",  getViaGetters(u, "getCountry"));

        Object dob = getViaGetters(u, "getDateOfBirth", "getBirthDate", "getDob");
        r.put("date_of_birth", formatDob(dob));
        return r;
    }

    private static Object getViaGetters(Object target, String... getters) {
        for (String name : getters) {
            try {
                Method m = target.getClass().getMethod(name);
                if (m.getParameterCount() == 0) {
                    return m.invoke(target);
                }
            } catch (Exception ignore) {}
        }
        return null;
    }

    private static String formatDob(Object v) {
        if (v == null) return null;
        if (v instanceof java.time.LocalDate ld) return ld.toString(); // yyyy-MM-dd
        if (v instanceof java.util.Date d) {
            return d.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toString(); // yyyy-MM-dd
        }
        return String.valueOf(v);
    }

    @Transactional
    public Map<String, Object> updateUserProfile(String username, Map<String, Object> body) {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        setIfPresent(u, body.get("address"),  true, "setAddress");
        setIfPresent(u, body.get("city"),     true, "setCity");
        setIfPresent(u, body.get("state"),    true, "setState");
        setIfPresent(u, body.get("country"),  true, "setCountry");
        setIfPresent(u, body.get("postal_code"), true, "setPostalCode", "setPostcode", "setZip");

        setIfPresent(u, body.get("name"),     false, "setName");
        setIfPresent(u, body.get("gender"),   false, "setGender");
        setIfPresent(u, body.get("email"),    false, "setEmail");
        setIfPresent(u, body.get("phone"),    false, "setPhone", "setPhoneNumber");

        setDateIfPresent(u, body.get("date_of_birth"), "setDateOfBirth", "setBirthDate", "setDob");

        users.save(u);
        return getUserProfile(username);
    }



    private static void setIfPresent(Object target, Object rawVal, boolean allowNull, String... setterCandidates) {
        Object val = normalizeBlankToNull(rawVal);

        if (!allowNull && val == null) return;

        for (String setter : setterCandidates) {
            Method m = findSetter(target.getClass(), setter);
            if (m == null) continue;

            try {
                Class<?> pt = m.getParameterTypes()[0];
                Object converted = convertToType(val, pt);
                m.invoke(target, converted);
                //System.out.println("Set field via " + setter + " = " + converted);
                return;
            } catch (Exception e) {
                //System.err.println("Failed to set via " + setter + ": " + e.getMessage());
            }
        }
    }


    private static void setDateIfPresent(Object target, Object rawVal, String... setterCandidates) {
        Object v = normalizeBlankToNull(rawVal);
        for (String setter : setterCandidates) {
            Method m = findSetter(target.getClass(), setter);
            if (m == null) continue;
            try {
                Class<?> pt = m.getParameterTypes()[0];
                Object converted = null;
                if (v != null) {
                    String s = String.valueOf(v).trim();
                    if (pt == java.time.LocalDate.class)
                        converted = java.time.LocalDate.parse(s);
                    else if (pt == java.util.Date.class)
                        converted = java.util.Date.from(
                                java.time.LocalDate.parse(s)
                                        .atStartOfDay(java.time.ZoneId.systemDefault())
                                        .toInstant()
                        );
                }
                m.invoke(target, converted);
                return;
            } catch (Exception e) {
                //System.err.println("Failed to set date via " + setter + ": " + e.getMessage());
            }
        }
    }



    private static Method findSetter(Class<?> clazz, String name) {
        try {
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 1) return m;
            }
        } catch (Exception ignore) { }
        return null;
    }

    private static Object convertToType(Object val, Class<?> pt) {
        if (val == null) return null;
        if (pt == String.class) return String.valueOf(val).trim();
        return val;
    }

    private static Object normalizeBlankToNull(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        return s.trim().isEmpty() ? null : s;
    }


}
