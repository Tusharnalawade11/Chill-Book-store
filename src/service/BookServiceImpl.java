package service;

import java.util.List;
import java.util.Optional;

import dao.BookDao;
import dao.BookDaoCollectionImpl;
import model.BookPojo;

public class BookServiceImpl implements BookService{

	BookDao bookDao;
	
	public BookServiceImpl() {
		bookDao = new BookDaoCollectionImpl();
	}

	@Override
	public List<BookPojo> fetchAllBook() {
		List<BookPojo> allBooks = bookDao.fetchAllBook();
		return allBooks;
	}

	@Override
	public Optional<BookPojo> fetchABook(int bookId) {
		Optional<BookPojo> bookPojoOptional = bookDao.fetchABook(bookId);
		return bookPojoOptional;
	}

	@Override
	public List<BookPojo> fetchByBookGenre(String genre) {
		List<BookPojo> allBookGenres = bookDao.fetchByBookGenre(genre);
		return allBookGenres;
	}

	@Override
	public void removeBook(int bookId) {
		bookDao.removeBook(bookId);
	}

	@Override
	public BookPojo addBook(BookPojo newBook) {
		BookPojo book = bookDao.addBook(newBook);
		return book;
	}

	@Override
	public BookPojo updateBook(BookPojo updateBook) {
		BookPojo book = bookDao.updateBook(updateBook);
		return book;
	}
}
