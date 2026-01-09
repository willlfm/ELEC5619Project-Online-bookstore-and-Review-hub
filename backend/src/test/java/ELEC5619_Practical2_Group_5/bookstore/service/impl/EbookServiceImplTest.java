package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.EbookItemResponse;
import ELEC5619_Practical2_Group_5.bookstore.repository.EbookItemRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.EbookRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EbookServiceImplTest {

    @Mock
    private EbookItemRepository ebookItemRepository;

    @InjectMocks
    private EbookServiceImpl ebookService;

    private EbookRow testEbookRow;

    @BeforeEach
    void setUp() {
        testEbookRow = new EbookRow() {
            @Override
            public Integer getOrderId() { return 1; }

            @Override
            public Integer getOrderItemId() { return 1; }

            @Override
            public LocalDateTime getOrderDate() { return LocalDateTime.now(); }

            @Override
            public String getStatus() { return "completed"; }

            @Override
            public Integer getBookId() { return 1; }

            @Override
            public String getBookTitle() { return "Test Ebook"; }

            @Override
            public String getAuthor() { return "Test Author"; }

            @Override
            public String getFormat() { return "ebook"; }

            @Override
            public String getSourceUrl() { return "test-ebook.pdf"; }
        };
    }

    @Test
    void getMyEbooks_Success() {
        when(ebookItemRepository.findMyEbooks("testuser")).thenReturn(Arrays.asList(testEbookRow));

        List<EbookItemResponse> result = ebookService.getMyEbooks("testuser");

        assertNotNull(result);
        assertEquals(1, result.size());
        EbookItemResponse response = result.get(0);
        assertEquals(1, response.getOrderId());
        assertEquals(1, response.getOrderItemId());
        assertEquals("completed", response.getStatus());
        assertEquals(1, response.getBookId());
        assertEquals("Test Ebook", response.getBookTitle());
        assertEquals("Test Author", response.getAuthor());
        assertEquals("ebook", response.getFormat());
        assertEquals("test-ebook.pdf", response.getSourceUrl());
        verify(ebookItemRepository).findMyEbooks("testuser");
    }

    @Test
    void getMyEbooks_EmptyList() {
        when(ebookItemRepository.findMyEbooks("testuser")).thenReturn(Collections.emptyList());

        List<EbookItemResponse> result = ebookService.getMyEbooks("testuser");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(ebookItemRepository).findMyEbooks("testuser");
    }

    @Test
    void download_EbookNotFound() {
        when(ebookItemRepository.findMyEbookByOrderItemId("testuser", 1))
                .thenReturn(Optional.empty());

        ResponseEntity<Resource> result = ebookService.download("testuser", 1);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(ebookItemRepository).findMyEbookByOrderItemId("testuser", 1);
    }

    @Test
    void download_StatusNotDownloadable() {
        EbookRow nonDownloadableRow = new EbookRow() {
            @Override
            public Integer getOrderId() { return 1; }

            @Override
            public Integer getOrderItemId() { return 1; }

            @Override
            public LocalDateTime getOrderDate() { return LocalDateTime.now(); }

            @Override
            public String getStatus() { return "pending"; }

            @Override
            public Integer getBookId() { return 1; }

            @Override
            public String getBookTitle() { return "Test Ebook"; }

            @Override
            public String getAuthor() { return "Test Author"; }

            @Override
            public String getFormat() { return "ebook"; }

            @Override
            public String getSourceUrl() { return "test-ebook.pdf"; }
        };

        when(ebookItemRepository.findMyEbookByOrderItemId("testuser", 1))
                .thenReturn(Optional.of(nonDownloadableRow));

        ResponseEntity<Resource> result = ebookService.download("testuser", 1);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        verify(ebookItemRepository).findMyEbookByOrderItemId("testuser", 1);
    }

    @Test
    void download_InvalidFileName() {
        EbookRow invalidFileRow = new EbookRow() {
            @Override
            public Integer getOrderId() { return 1; }

            @Override
            public Integer getOrderItemId() { return 1; }

            @Override
            public LocalDateTime getOrderDate() { return LocalDateTime.now(); }

            @Override
            public String getStatus() { return "completed"; }

            @Override
            public Integer getBookId() { return 1; }

            @Override
            public String getBookTitle() { return "Test Ebook"; }

            @Override
            public String getAuthor() { return "Test Author"; }

            @Override
            public String getFormat() { return "ebook"; }

            @Override
            public String getSourceUrl() { return "../malicious-file.pdf"; }
        };

        when(ebookItemRepository.findMyEbookByOrderItemId("testuser", 1))
                .thenReturn(Optional.of(invalidFileRow));

        ResponseEntity<Resource> result = ebookService.download("testuser", 1);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(ebookItemRepository).findMyEbookByOrderItemId("testuser", 1);
    }

    @Test
    void download_NullFileName() {
        EbookRow nullFileRow = new EbookRow() {
            @Override
            public Integer getOrderId() { return 1; }

            @Override
            public Integer getOrderItemId() { return 1; }

            @Override
            public LocalDateTime getOrderDate() { return LocalDateTime.now(); }

            @Override
            public String getStatus() { return "completed"; }

            @Override
            public Integer getBookId() { return 1; }

            @Override
            public String getBookTitle() { return "Test Ebook"; }

            @Override
            public String getAuthor() { return "Test Author"; }

            @Override
            public String getFormat() { return "ebook"; }

            @Override
            public String getSourceUrl() { return null; }
        };

        when(ebookItemRepository.findMyEbookByOrderItemId("testuser", 1))
                .thenReturn(Optional.of(nullFileRow));

        ResponseEntity<Resource> result = ebookService.download("testuser", 1);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(ebookItemRepository).findMyEbookByOrderItemId("testuser", 1);
    }

    @Test
    void download_FileNotExists() {
        when(ebookItemRepository.findMyEbookByOrderItemId("testuser", 1))
                .thenReturn(Optional.of(testEbookRow));

        ResponseEntity<Resource> result = ebookService.download("testuser", 1);

        // Since the file doesn't exist in classpath, it should return NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(ebookItemRepository).findMyEbookByOrderItemId("testuser", 1);
    }

    @Test
    void download_DownloadableStatuses() {
        // Test all downloadable statuses
        String[] downloadableStatuses = {"paid", "completed", "ready"};
        
        for (String status : downloadableStatuses) {
            EbookRow statusRow = new EbookRow() {
                @Override
                public Integer getOrderId() { return 1; }

                @Override
                public Integer getOrderItemId() { return 1; }

                @Override
                public LocalDateTime getOrderDate() { return LocalDateTime.now(); }

                @Override
                public String getStatus() { return status; }

                @Override
                public Integer getBookId() { return 1; }

                @Override
                public String getBookTitle() { return "Test Ebook"; }

                @Override
                public String getAuthor() { return "Test Author"; }

                @Override
                public String getFormat() { return "ebook"; }

                @Override
                public String getSourceUrl() { return "test-ebook.pdf"; }
            };

            when(ebookItemRepository.findMyEbookByOrderItemId("testuser", 1))
                    .thenReturn(Optional.of(statusRow));

            ResponseEntity<Resource> result = ebookService.download("testuser", 1);

            // File doesn't exist, so it should be NOT_FOUND, but status check should pass
            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        }
    }

    @Test
    void download_NonDownloadableStatuses() {
        // Test non-downloadable statuses
        String[] nonDownloadableStatuses = {"pending", "cancelled", "failed"};
        
        for (String status : nonDownloadableStatuses) {
            EbookRow statusRow = new EbookRow() {
                @Override
                public Integer getOrderId() { return 1; }

                @Override
                public Integer getOrderItemId() { return 1; }

                @Override
                public LocalDateTime getOrderDate() { return LocalDateTime.now(); }

                @Override
                public String getStatus() { return status; }

                @Override
                public Integer getBookId() { return 1; }

                @Override
                public String getBookTitle() { return "Test Ebook"; }

                @Override
                public String getAuthor() { return "Test Author"; }

                @Override
                public String getFormat() { return "ebook"; }

                @Override
                public String getSourceUrl() { return "test-ebook.pdf"; }
            };

            when(ebookItemRepository.findMyEbookByOrderItemId("testuser", 1))
                    .thenReturn(Optional.of(statusRow));

            ResponseEntity<Resource> result = ebookService.download("testuser", 1);

            assertEquals(HttpStatus.CONFLICT, result.getStatusCode());
        }
    }
}