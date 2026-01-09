package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminUserDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminUserRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
// Encapsulates admin-only account management logic (validation + password hygiene).
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserDto> listUsers(int page, int size, String keyword) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<User> users;
        if (StringUtils.hasText(keyword)) {
            String search = keyword.trim();
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
                    search, search, search, pageable
            );
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(this::toDto);
    }

    @Override
    public AdminUserDto createUser(AdminUserRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getEmail())) {
            throw new IllegalArgumentException("username and email are required");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("password is required");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("email already exists");
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(StringUtils.hasText(request.getName()) ? request.getName().trim() : request.getUsername())
                .phone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : "0000000000")
                .securityQuestion(StringUtils.hasText(request.getSecurityQuestion()) ? request.getSecurityQuestion().trim() : "Default question")
                .securityAnswer(passwordEncoder.encode(
                        StringUtils.hasText(request.getSecurityAnswer()) ? request.getSecurityAnswer().trim() : "default"
                ))
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(StringUtils.hasText(request.getCountry()) ? request.getCountry() : "Unknown")
                .gender(request.getGender())
                .role(StringUtils.hasText(request.getRole()) ? request.getRole() : "normal")
                .build();

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Override
    public AdminUserDto updateUser(Integer userId, AdminUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail().trim());
        }
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone().trim());
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole().trim());
        }
        if (StringUtils.hasText(request.getAddress())) {
            user.setAddress(request.getAddress());
        }
        if (StringUtils.hasText(request.getCity())) {
            user.setCity(request.getCity());
        }
        if (StringUtils.hasText(request.getState())) {
            user.setState(request.getState());
        }
        if (StringUtils.hasText(request.getPostalCode())) {
            user.setPostalCode(request.getPostalCode());
        }
        if (StringUtils.hasText(request.getCountry())) {
            user.setCountry(request.getCountry());
        }
        if (StringUtils.hasText(request.getGender())) {
            user.setGender(request.getGender());
        }
        if (StringUtils.hasText(request.getSecurityQuestion())) {
            user.setSecurityQuestion(request.getSecurityQuestion());
        }
        if (StringUtils.hasText(request.getSecurityAnswer())) {
            user.setSecurityAnswer(passwordEncoder.encode(request.getSecurityAnswer()));
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private AdminUserDto toDto(User user) {
        return AdminUserDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .address(user.getAddress())
                .city(user.getCity())
                .country(user.getCountry())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .build();
    }
}
