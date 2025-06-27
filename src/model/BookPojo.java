package model;

import java.util.Objects;

public class BookPojo {
	private int bookId;
	private String bookTitle;
	private AuthorPojo author;
	private int bookPrice;
	private String bookGenre;
	private String bookImageUrl;
	
	public BookPojo(int bookId, String bookTitle, AuthorPojo author, int bookPrice, String bookGenre,
			String bookImageUrl) {
		super();
		this.bookId = bookId;
		this.bookTitle = bookTitle;
		this.author = author;
		this.bookPrice = bookPrice;
		this.bookGenre = bookGenre;
		this.bookImageUrl = bookImageUrl;
	}

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public String getBookTitle() {
		return bookTitle;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}

	public AuthorPojo getAuthor() {
		return author;
	}

	public void setAuthor(AuthorPojo author) {
		this.author = author;
	}

	public int getBookPrice() {
		return bookPrice;
	}

	public void setBookPrice(int bookPrice) {
		this.bookPrice = bookPrice;
	}

	public String getBookGenre() {
		return bookGenre;
	}

	public void setBookGenre(String bookGenre) {
		this.bookGenre = bookGenre;
	}

	public String getBookImageUrl() {
		return bookImageUrl;
	}

	public void setBookImageUrl(String bookImageUrl) {
		this.bookImageUrl = bookImageUrl;
	}

	@Override
	public int hashCode() {
		return Objects.hash(author, bookGenre, bookId, bookImageUrl, bookPrice, bookTitle);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BookPojo other = (BookPojo) obj;
		return Objects.equals(author, other.author) && Objects.equals(bookGenre, other.bookGenre)
				&& bookId == other.bookId && Objects.equals(bookImageUrl, other.bookImageUrl)
				&& bookPrice == other.bookPrice && Objects.equals(bookTitle, other.bookTitle);
	}

	@Override
	public String toString() {
		return "BookPojo [bookId=" + bookId + ", bookTitle=" + bookTitle + ", author=" + author + ", bookPrice="
				+ bookPrice + ", bookGenre=" + bookGenre + ", bookImageUrl=" + bookImageUrl + "]";
	}
}
