package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.auth.ResetPasswordRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.SignInRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.SignUpRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.VerifyAnswerRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.VerifyUserRequest;
import ELEC5619_Practical2_Group_5.bookstore.dto.auth.VerifyUserResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.User;

import java.util.Map;

public interface AuthService {

    User register(SignUpRequest req);

    Map<String, Object> signin(SignInRequest req);

    // Forgot password flow - step 1
    VerifyUserResponse startForgotFlow(VerifyUserRequest req);

    // step 2: Security answer
    boolean verifySecurityAnswer(VerifyAnswerRequest req);

    // step 3: reset password
    boolean resetPassword(ResetPasswordRequest req);

    Map<String, Object> getUserProfile(String username);

    Map<String, Object> updateUserProfile(String username, Map<String, Object> body);
}
