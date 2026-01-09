package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminUserDto;
import ELEC5619_Practical2_Group_5.bookstore.dto.admin.AdminUserRequest;
import org.springframework.data.domain.Page;

public interface AdminUserService {
    Page<AdminUserDto> listUsers(int page, int size, String keyword);

    AdminUserDto createUser(AdminUserRequest request);

    AdminUserDto updateUser(Integer userId, AdminUserRequest request);

    void deleteUser(Integer userId);
}
