package dao;

import java.util.List;
import java.util.Optional;

import model.BookPojo;

public interface BookDao {
	List<BookPojo> fetchAllBook();
	Optional<BookPojo> fetchABook(int bookId);
	List<BookPojo> fetchByBookGenre(String genre);
	void removeBook(int bookId);
	BookPojo addBook(BookPojo newBook);
	BookPojo updateBook(BookPojo updateBook);
}
