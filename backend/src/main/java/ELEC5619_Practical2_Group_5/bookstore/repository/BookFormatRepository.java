package ELEC5619_Practical2_Group_5.bookstore.repository;

import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormat;
import ELEC5619_Practical2_Group_5.bookstore.entity.BookFormatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookFormatRepository extends JpaRepository<BookFormat, Integer> {

    // search all format by bookId
    List<BookFormat> findByBookBookId(Integer bookId);

    // search by bookId and format(for the cart)
    Optional<BookFormat> findByBookBookIdAndFormat(Integer bookId, BookFormatType format);

    // --------------------- fetch join method ---------------------
    @Query("SELECT bf FROM BookFormat bf JOIN FETCH bf.book b " +
            "WHERE b.bookId = :bookId AND bf.format = :format")
    Optional<BookFormat> findByBookBookIdAndFormatFetchBook(
            @Param("bookId") Integer bookId,
            @Param("format") BookFormatType format
    );
}
