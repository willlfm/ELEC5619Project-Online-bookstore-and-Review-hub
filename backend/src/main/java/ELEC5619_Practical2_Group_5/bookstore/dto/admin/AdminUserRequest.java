package ELEC5619_Practical2_Group_5.bookstore.dto.admin;

import lombok.Data;

@Data
public class AdminUserRequest {
    private String username;
    private String email;
    private String password;
    private String name;
    private String phone;
    private String role;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String gender;
    private String securityQuestion;
    private String securityAnswer;
}
