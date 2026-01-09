package ELEC5619_Practical2_Group_5.bookstore.repository;

import ELEC5619_Practical2_Group_5.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<Book> findByCategory(String category, Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.formats WHERE b.bookId = :id")
    Optional<Book> findByIdWithFormats(@Param("id") Integer id);

    List<Book> findByBookIdIn(Collection<Integer> bookIds);

}
