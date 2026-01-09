package ELEC5619_Practical2_Group_5.bookstore.service;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.EbookItemResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EbookService {
    /**
     * Returns ebooks purchased by the current user.
     */
    List<EbookItemResponse> getMyEbooks(String username);

    /**
     * Resolve download for a specific order item of the current user.
     * May return:
     *  - 302 redirect if item has downloadUrl
     *  - 200 with file stream if item has downloadPath
     *  - 404 if not found or not owned by user
     *  - 409 if status is not downloadable
     */
    ResponseEntity<Resource> download(String username, Integer orderItemId);
}
