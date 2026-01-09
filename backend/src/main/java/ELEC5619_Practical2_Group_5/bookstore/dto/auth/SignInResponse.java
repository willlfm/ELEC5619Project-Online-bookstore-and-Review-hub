package ELEC5619_Practical2_Group_5.bookstore.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInResponse {
    private Integer id;
    private String username;
    private String name;
    private List<String> roles;
}
