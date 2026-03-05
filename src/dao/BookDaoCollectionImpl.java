package dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import model.AuthorPojo;
import model.BookPojo;

public class BookDaoCollectionImpl implements BookDao{

	final private List<BookPojo> bookDataStore;
	
	public BookDaoCollectionImpl() {
		bookDataStore = new ArrayList<>();
		AuthorPojo author = new AuthorPojo(501, "J.K", "Rowling");
		bookDataStore.add(new BookPojo(101, "Harry Potter and the Chamber of Secrets", author, 300, "Fantasy", ""));
		bookDataStore.add(new BookPojo(102, "Harry Potter and the Deathly Hallows", author, 400, "Fantasy", ""));
		bookDataStore.add(new BookPojo(103, "Harry Potter and the Order of Phoenix", author, 350, "Fantasy", ""));
	}
	
	@Override
	public List<BookPojo> fetchAllBook() {
		List<BookPojo> allBooks = new ArrayList<>(bookDataStore.size());
		allBooks.addAll(bookDataStore);
		return allBooks;
	}

	@Override
	public Optional<BookPojo> fetchABook(int bookId) {
		// traverse the collection
		// check if each item's bookId matches the bookId
		// return the item and break
		BookPojo fetchedBook = null;
		List<BookPojo> searchedBook = bookDataStore.stream().filter((eachBook) -> eachBook.getBookId() == bookId).toList();
		if(!searchedBook.isEmpty()) {
			fetchedBook = searchedBook.get(0);
		}
		if(fetchedBook == null) return Optional.empty();
		return Optional.of(fetchedBook);
	}

	@Override
	public List<BookPojo> fetchByBookGenre(String genre) {
		List<String> requestedGenres = normalizeGenres(genre);
		if (requestedGenres.isEmpty()) {
			return List.of();
		}
		List<BookPojo> searchedBooks = bookDataStore.stream()
				.filter((eachBook) -> hasAnyGenreMatch(eachBook.getBookGenre(), requestedGenres))
				.toList();
		return searchedBooks;
	}

	private boolean hasAnyGenreMatch(String bookGenre, List<String> requestedGenres) {
		List<String> bookGenres = normalizeGenres(bookGenre);
		return bookGenres.stream().anyMatch(requestedGenres::contains);
	}

	private List<String> normalizeGenres(String genreValue) {
		if (genreValue == null || genreValue.isBlank()) {
			return List.of();
		}
		return Arrays.stream(genreValue.split("/"))
				.map(String::trim)
				.filter((genre) -> !genre.isBlank())
				.map(String::toLowerCase)
				.distinct()
				.toList();
	}

	@Override
	public void removeBook(int bookId) {
		bookDataStore.removeIf((eachBook) -> eachBook.getBookId() == bookId);
	}

	@Override
	public BookPojo addBook(BookPojo newBook) {
		// logic to calculate the bookId for the newBook
		newBook.setBookId(bookDataStore.get(bookDataStore.size()-1).getBookId() + 1); //O(1)
		bookDataStore.add(newBook);
		return newBook;
	}

	@Override
	public BookPojo updateBook(BookPojo updateBook) {
		for(int i=0; i<bookDataStore.size();i++) {
			if(bookDataStore.get(i).getBookId() == updateBook.getBookId()) {
				bookDataStore.set(i, updateBook);
				return updateBook;
			}
		}
		return updateBook;
	}

}
