package ELEC5619_Practical2_Group_5.bookstore.repository;

import java.time.LocalDateTime;

public interface EbookRow {
    Integer getOrderId();
    Integer getOrderItemId();
    LocalDateTime getOrderDate();
    String getStatus();
    Integer getBookId();
    String getBookTitle();
    String getAuthor();
    String getFormat();
    String getSourceUrl();
}
