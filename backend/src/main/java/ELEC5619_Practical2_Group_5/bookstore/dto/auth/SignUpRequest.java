package ELEC5619_Practical2_Group_5.bookstore.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-()\\s]{6,20}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be 8–128 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 8, max = 20, message = "Confirm password must be 8–128 characters")
    private String confirmPassword;

    @NotBlank(message = "Security question is required")
    @Size(max = 255, message = "Security question must be ≤ 200 characters")
    private String securityQuestion;

    @NotBlank(message = "Security answer is required")
    @Size(max = 255, message = "Security answer must be ≤ 200 characters")
    private String securityAnswer;
}
