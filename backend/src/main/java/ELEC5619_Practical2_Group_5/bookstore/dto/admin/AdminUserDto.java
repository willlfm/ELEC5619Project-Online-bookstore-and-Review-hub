package ELEC5619_Practical2_Group_5.bookstore.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    private Integer userId;
    private String username;
    private String email;
    private String name;
    private String phone;
    private String role;
    private String address;
    private String city;
    private String country;
    private String gender;
    private LocalDate dateOfBirth;
}
