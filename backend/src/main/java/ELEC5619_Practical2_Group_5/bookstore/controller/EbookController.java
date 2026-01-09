package ELEC5619_Practical2_Group_5.bookstore.controller;

import ELEC5619_Practical2_Group_5.bookstore.config.JwtUtils;
import ELEC5619_Practical2_Group_5.bookstore.dto.order.EbookItemResponse;
import ELEC5619_Practical2_Group_5.bookstore.repository.UserRepository;
import ELEC5619_Practical2_Group_5.bookstore.service.EbookService;


import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ebooks")
@RequiredArgsConstructor
public class EbookController {

    private final EbookService ebookService;

    @GetMapping("/me")
    public List<EbookItemResponse> getMyEbooks(@AuthenticationPrincipal String username) {
        // If your @AuthenticationPrincipal is a UserDetails, map to username accordingly.
        return ebookService.getMyEbooks(username);
    }

    @GetMapping("/{orderItemId}/download")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal String username,
            @PathVariable Integer orderItemId) {
        return ebookService.download(username, orderItemId);
    }
}