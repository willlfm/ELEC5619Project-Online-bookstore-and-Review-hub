package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminUserDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminUserRequest;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .userId(1)
                .username("john")
                .email("john@example.com")
                .name("John Doe")
                .phone("123456789")
                .role("admin")
                .build();
    }

    @Test
    void listUsers_withoutKeyword_returnsAll() {
        Page<User> page = new PageImpl<>(List.of(sampleUser));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<AdminUserDto> result = adminUserService.listUsers(0, 5, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john");
        verify(userRepository).findAll(PageRequest.of(0, 5));
    }

    @Test
    void listUsers_withKeyword_filters() {
        Page<User> page = new PageImpl<>(List.of(sampleUser));
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
                anyString(), anyString(), anyString(), any(PageRequest.class))).thenReturn(page);

        Page<AdminUserDto> result = adminUserService.listUsers(1, 10, "john");

        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrNameContainingIgnoreCase(
                eq("john"), eq("john"), eq("john"), eq(PageRequest.of(1, 10)));
    }

    @Test
    void createUser_successfullyPersistsUser() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        request.setName("Alice");
        request.setPhone("111222333");
        request.setRole("admin");
        request.setSecurityQuestion("Q");
        request.setSecurityAnswer("A");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setUserId(5);
            return toSave;
        });

        AdminUserDto dto = adminUserService.createUser(request);

        assertThat(dto.getUserId()).isEqualTo(5);
        assertThat(dto.getUsername()).isEqualTo("alice");
        verify(passwordEncoder, times(2)).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_missingPassword_throwsException() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password is required");
    }

    @Test
    void createUser_missingUsername_throwsException() {
        AdminUserRequest request = new AdminUserRequest();
        request.setEmail("alice@example.com");
        request.setPassword("pass");

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("username and email are required");
    }

    @Test
    void createUser_missingEmail_throwsException() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("alice");
        request.setPassword("pass");

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("username and email are required");
    }

    @Test
    void createUser_existingUsername_throwsException() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("pass");

        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("username already exists");
    }

    @Test
    void createUser_existingEmail_throwsException() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("pass");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email already exists");
    }

    @Test
    void updateUser_updatesFields_andEncodesPasswordWhenProvided() {
        AdminUserRequest request = new AdminUserRequest();
        request.setEmail("new@example.com");
        request.setName("New Name");
        request.setPhone("999");
        request.setRole("normal");
        request.setSecurityAnswer("answer");
        request.setPassword("newPass");
        request.setAddress("Addr");
        request.setCity("City");
        request.setState("State");
        request.setPostalCode("2000");
        request.setCountry("Country");
        request.setGender("F");
        request.setSecurityQuestion("Question");

        User existing = User.builder()
                .userId(10)
                .username("bob")
                .email("old@example.com")
                .name("Old")
                .phone("000")
                .role("admin")
                .build();

        when(userRepository.findById(10)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserDto dto = adminUserService.updateUser(10, request);

        assertThat(dto.getEmail()).isEqualTo("new@example.com");
        assertThat(dto.getName()).isEqualTo("New Name");
        assertThat(dto.getPhone()).isEqualTo("999");
        verify(passwordEncoder, times(2)).encode(anyString());
        verify(userRepository).save(existing);
    }

    @Test
    void createUser_defaultsAreAppliedWhenOptionalBlank() {
        AdminUserRequest request = new AdminUserRequest();
        request.setUsername("bob");
        request.setEmail("bob@example.com");
        request.setPassword("secret");

        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(userRepository.existsByEmail("bob@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminUserService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("bob");
        assertThat(saved.getPhone()).isEqualTo("0000000000");
        assertThat(saved.getSecurityQuestion()).isEqualTo("Default question");
        verify(passwordEncoder, times(2)).encode(anyString());
    }

    @Test
    void updateUser_withoutPasswordOrSecurityAnswer_doesNotEncode() {
        AdminUserRequest request = new AdminUserRequest();
        request.setEmail("new@example.com");

        User existing = User.builder()
                .userId(5)
                .username("sam")
                .email("sam@old.com")
                .build();

        when(userRepository.findById(5)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminUserService.updateUser(5, request);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_notFound_throwsException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.updateUser(99, new AdminUserRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_whenExists_deletes() {
        when(userRepository.existsById(1)).thenReturn(true);

        adminUserService.deleteUser(1);

        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteUser_whenMissing_throws() {
        when(userRepository.existsById(2)).thenReturn(false);

        assertThatThrownBy(() -> adminUserService.deleteUser(2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
