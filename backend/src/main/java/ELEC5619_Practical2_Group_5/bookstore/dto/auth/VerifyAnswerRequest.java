package ELEC5619_Practical2_Group_5.bookstore.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAnswerRequest {
    @NotBlank
    private String flowId;

    @NotBlank
    @Size(max = 255)
    private String securityAnswer;
}
