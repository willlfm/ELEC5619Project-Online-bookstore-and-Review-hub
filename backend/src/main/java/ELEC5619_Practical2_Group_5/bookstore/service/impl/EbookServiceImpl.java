package ELEC5619_Practical2_Group_5.bookstore.service.impl;

import ELEC5619_Practical2_Group_5.bookstore.dto.order.EbookItemResponse;
import ELEC5619_Practical2_Group_5.bookstore.entity.EbookItem;
import ELEC5619_Practical2_Group_5.bookstore.repository.EbookItemRepository;
import ELEC5619_Practical2_Group_5.bookstore.repository.EbookRow;
import ELEC5619_Practical2_Group_5.bookstore.service.EbookService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.CacheControl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EbookServiceImpl implements EbookService {

    private final EbookItemRepository ebookItemRepository;

    private static final Set<String> DOWNLOADABLE_STATUSES = Set.of("paid", "completed", "ready");

    private boolean isDownloadable(String status) {
        return status != null
                && DOWNLOADABLE_STATUSES.contains(status.toLowerCase(Locale.ROOT));
    }

    @Override
    public List<EbookItemResponse> getMyEbooks(String username) {
        return ebookItemRepository.findMyEbooks(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(String username, Integer orderItemId) {
        Optional<EbookRow> opt = ebookItemRepository.findMyEbookByOrderItemId(username, orderItemId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        EbookRow row = opt.get();

        if (!isDownloadable(row.getStatus())) {
            // 订单状态不允许下载
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // sourceUrl 存的是文件名，例如: Thinking_in_Java(4th).txt
        String fileName = sanitizeFileName(row.getSourceUrl());
        if (fileName == null) {
            return ResponseEntity.notFound().build();
        }

        // 从 classpath: /book/ 下读取
        ClassPathResource cpr = new ClassPathResource("book/" + fileName);
        if (!cpr.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Content-Type：根据扩展名推断，默认为 application/octet-stream
        MediaType mediaType = MediaTypeFactory.getMediaType(fileName)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)   // 处理括号/空格/中文等
                .build());
        headers.setContentType(mediaType);
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
        headers.setPragma("no-cache");

        try {
            long len = cpr.contentLength(); // 在打包成 jar 时也可获取
            InputStreamResource body = new InputStreamResource(cpr.getInputStream());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(len)
                    .body(body);
        } catch (IOException e) {
            // 读取失败时返回 404 或 500，视需求而定
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null) return null;
        String n = name.trim();
        if (n.isEmpty()) return null;
        if (n.contains("..") || n.contains("/") || n.contains("\\"))
            return null;
        return n;
    }

    private EbookItemResponse toResponse(EbookRow r) {
        return EbookItemResponse.builder()
                .orderId(r.getOrderId())
                .orderItemId(r.getOrderItemId())
                .orderDate(r.getOrderDate() == null ? null : r.getOrderDate().toString())
                .status(r.getStatus())
                .bookId(r.getBookId())
                .bookTitle(r.getBookTitle())
                .author(r.getAuthor())
                .format(r.getFormat() == null ? "ebook" : r.getFormat())
                .sourceUrl(r.getSourceUrl())
                .build();
    }
}