package controller;

import java.util.List;
import java.util.Optional;

import model.BookPojo;
import presentation.Presentation;
import service.BookService;
import service.BookServiceImpl;

// we do not keep any input/output in the controller
public class BookController {
	BookService bookService;
	
	public BookController() {
		bookService = new BookServiceImpl();
	}
	
	public void start() {
		
		// will call the display menu for books
		while(true) {
			int option = Presentation.displayMenu();
			// depending on the menu option selected, controller will call the respective method of service
			switch(option) {
				case 1:
					List<BookPojo> allBooks = bookService.fetchAllBook();
					Presentation.displayFetchAllBooks(allBooks);
					break;
				case 2:
					// get bookId as input from the user - ask the presentation layer to do it
					int bookId = Presentation.scanBookId();
					// then call bookService.fetchABook
					Optional<BookPojo> bookPojoOptional = bookService.fetchABook(bookId);
					// send the optional to be displayed in Presentaion
					Presentation.displayOptionalBookPojo(bookPojoOptional);
					break;
				case 3:
					// get the genre as input from the user
					String genre = Presentation.scanGenre();
					// fetch books by genre
					List<BookPojo> allBooksGenre = bookService.fetchByBookGenre(genre);
					// send the collection to be displayed in presentation
					if(allBooksGenre.isEmpty()) {
						Presentation.displayNoGenreFound(genre);
					}else {
						Presentation.displayFetchAllBooks(allBooksGenre);
					}
					break;
				case 4:
					BookPojo book = Presentation.scanBookInput();
					BookPojo addedBook = bookService.addBook(book);
					Presentation.displayBookPojo(addedBook);
					break;
				case 5:
				case 6:
					// 1. fetch the book with the id, and send to presentaion layer to be displayed
					int deleteBookId = Presentation.scanBookId();
					Optional<BookPojo> deleteBookPojoOptional = bookService.fetchABook(deleteBookId);
					Presentation.displayOptionalBookPojo(deleteBookPojoOptional);
					// 2. ask for confirmation to delete
					if(deleteBookPojoOptional.isPresent()) {
						char ch = Presentation.displayConfirmation();
						if(ch == 'y') {
							// 3. go ahead and delete the book
							bookService.removeBook(deleteBookId);
							Presentation.displayDeleteConfirmation(deleteBookId);
						}
					}
					break;
				case 7:
					Presentation.displayExitMessage();
					System.exit(0);
				default:
					Presentation.displayInvalidOptionMessage();
					
			}
			
		}
	 
	}
	
}
