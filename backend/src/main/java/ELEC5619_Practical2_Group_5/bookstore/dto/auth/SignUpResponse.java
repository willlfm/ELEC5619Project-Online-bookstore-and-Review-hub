package ELEC5619_Practical2_Group_5.bookstore.dto.auth;

import lombok.Value;

@Value
public class SignUpResponse {
    Integer userId;
    String username;
    String email;
}
