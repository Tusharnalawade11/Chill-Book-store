package api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.AuthorPojo;
import model.BookPojo;
import service.BookService;
import service.BookServiceImpl;

public class BookApiServer {
	private static final String BASE_PATH = "/api/books";
	private static final String ALLOWED_ORIGIN = "*";

	private final Object lock = new Object();
	private final BookService bookService;
	private final HttpServer server;

	public BookApiServer(int port) throws IOException {
		bookService = new BookServiceImpl();
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext(BASE_PATH, new BookHandler());
		server.setExecutor(Executors.newCachedThreadPool());
	}

	public void start() {
		server.start();
		System.out.println("Book API server started on http://localhost:" + server.getAddress().getPort() + BASE_PATH);
		System.out.println("Allowed CORS origin: " + ALLOWED_ORIGIN);
	}

	public void stop(int delaySeconds) {
		server.stop(delaySeconds);
	}

	private final class BookHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			try (exchange) {
				String method = exchange.getRequestMethod();
				if ("OPTIONS".equalsIgnoreCase(method)) {
					sendEmpty(exchange, 204);
					return;
				}

				String rawPath = exchange.getRequestURI().getRawPath();
				String relativePath = rawPath.substring(BASE_PATH.length());

				if (relativePath.isEmpty() || "/".equals(relativePath)) {
					handleCollection(exchange, method);
					return;
				}
				if (!relativePath.startsWith("/")) {
					sendJson(exchange, 404, "{\"error\":\"Not found\"}");
					return;
				}

				if (relativePath.startsWith("/genre/")) {
					String encodedGenre = relativePath.substring("/genre/".length());
					handleGenre(exchange, method, encodedGenre);
					return;
				}

				String idPath = relativePath.substring(1);
				if (idPath.contains("/")) {
					sendJson(exchange, 404, "{\"error\":\"Not found\"}");
					return;
				}
				handleSingleBook(exchange, method, idPath);
			} catch (IllegalArgumentException ex) {
				sendJson(exchange, 400, "{\"error\":\"" + escape(ex.getMessage()) + "\"}");
			} catch (Exception ex) {
				sendJson(exchange, 500, "{\"error\":\"Internal server error\"}");
			}
		}
	}

	private void handleCollection(HttpExchange exchange, String method) throws IOException {
		if ("GET".equalsIgnoreCase(method)) {
			List<BookPojo> books;
			synchronized (lock) {
				books = bookService.fetchAllBook();
			}
			sendJson(exchange, 200, toJson(books));
			return;
		}

		if ("POST".equalsIgnoreCase(method)) {
			String body = readBody(exchange);
			BookPojo payload = parseBookFromJson(body);
			BookPojo addedBook;
			synchronized (lock) {
				addedBook = bookService.addBook(payload);
			}
			sendJson(exchange, 201, toJson(addedBook));
			return;
		}

		sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
	}

	private void handleGenre(HttpExchange exchange, String method, String encodedGenre) throws IOException {
		if (!"GET".equalsIgnoreCase(method)) {
			sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
			return;
		}
		String genre = URLDecoder.decode(encodedGenre, StandardCharsets.UTF_8);
		List<BookPojo> books;
		synchronized (lock) {
			books = bookService.fetchByBookGenre(genre);
		}
		sendJson(exchange, 200, toJson(books));
	}

	private void handleSingleBook(HttpExchange exchange, String method, String rawId) throws IOException {
		int bookId;
		try {
			bookId = Integer.parseInt(rawId);
		} catch (NumberFormatException ex) {
			sendJson(exchange, 400, "{\"error\":\"Invalid book id\"}");
			return;
		}

		if ("GET".equalsIgnoreCase(method)) {
			Optional<BookPojo> book;
			synchronized (lock) {
				book = bookService.fetchABook(bookId);
			}
			if (book.isPresent()) {
				sendJson(exchange, 200, toJson(book.get()));
			} else {
				sendJson(exchange, 404, "{\"error\":\"Book not found\"}");
			}
			return;
		}

		if ("PUT".equalsIgnoreCase(method)) {
			Optional<BookPojo> existing;
			synchronized (lock) {
				existing = bookService.fetchABook(bookId);
			}
			if (existing.isEmpty()) {
				sendJson(exchange, 404, "{\"error\":\"Book not found\"}");
				return;
			}

			BookPojo payload = parseBookFromJson(readBody(exchange));
			payload.setBookId(bookId);

			BookPojo updated;
			synchronized (lock) {
				updated = bookService.updateBook(payload);
			}
			sendJson(exchange, 200, toJson(updated));
			return;
		}

		if ("DELETE".equalsIgnoreCase(method)) {
			Optional<BookPojo> existing;
			synchronized (lock) {
				existing = bookService.fetchABook(bookId);
				if (existing.isPresent()) {
					bookService.removeBook(bookId);
				}
			}
			if (existing.isPresent()) {
				sendEmpty(exchange, 204);
			} else {
				sendJson(exchange, 404, "{\"error\":\"Book not found\"}");
			}
			return;
		}

		sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
	}

	private String readBody(HttpExchange exchange) throws IOException {
		InputStream requestBody = exchange.getRequestBody();
		byte[] bytes = requestBody.readAllBytes();
		return new String(bytes, StandardCharsets.UTF_8);
	}

	private void sendJson(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
		byte[] body = jsonBody.getBytes(StandardCharsets.UTF_8);
		Headers headers = exchange.getResponseHeaders();
		addCorsHeaders(headers);
		headers.set("Content-Type", "application/json; charset=utf-8");
		exchange.sendResponseHeaders(statusCode, body.length);
		exchange.getResponseBody().write(body);
	}

	private void sendEmpty(HttpExchange exchange, int statusCode) throws IOException {
		Headers headers = exchange.getResponseHeaders();
		addCorsHeaders(headers);
		exchange.sendResponseHeaders(statusCode, -1);
	}

	private void addCorsHeaders(Headers headers) {
		headers.set("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
		headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
		headers.set("Access-Control-Allow-Headers", "Content-Type");
	}

	private String toJson(List<BookPojo> books) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < books.size(); i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(toJson(books.get(i)));
		}
		sb.append("]");
		return sb.toString();
	}

	private String toJson(BookPojo book) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"bookId\":").append(book.getBookId()).append(",");
		sb.append("\"bookTitle\":\"").append(escape(book.getBookTitle())).append("\",");
		sb.append("\"bookGenre\":\"").append(escape(book.getBookGenre())).append("\",");
		sb.append("\"bookPrice\":").append(book.getBookPrice()).append(",");
		sb.append("\"bookImageUrl\":\"").append(escape(book.getBookImageUrl())).append("\",");
		sb.append("\"author\":").append(toJson(book.getAuthor()));
		sb.append("}");
		return sb.toString();
	}

	private String toJson(AuthorPojo author) {
		if (author == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"authorId\":").append(author.getAuthorId()).append(",");
		sb.append("\"authorFirstName\":\"").append(escape(author.getAuthorFirstName())).append("\",");
		sb.append("\"authorLastName\":\"").append(escape(author.getAuthorLastName())).append("\"");
		sb.append("}");
		return sb.toString();
	}

	private BookPojo parseBookFromJson(String json) {
		String bookTitle = extractString(json, "bookTitle", "");
		String bookGenre = extractString(json, "bookGenre", "");
		String bookImageUrl = extractString(json, "bookImageUrl", "");
		int bookPrice = extractInt(json, "bookPrice", 0);

		int authorId = extractInt(json, "authorId", 0);
		String authorFirstName = extractString(json, "authorFirstName", "");
		String authorLastName = extractString(json, "authorLastName", "");
		AuthorPojo author = new AuthorPojo(authorId, authorFirstName, authorLastName);

		return new BookPojo(0, bookTitle, author, bookPrice, bookGenre, bookImageUrl);
	}

	private int extractInt(String json, String key, int defaultValue) {
		Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
		Matcher matcher = pattern.matcher(json);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return defaultValue;
	}

	private String extractString(String json, String key, String defaultValue) {
		Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
		Matcher matcher = pattern.matcher(json);
		if (matcher.find()) {
			return unescape(matcher.group(1));
		}
		return defaultValue;
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}

	private String unescape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\\"", "\"").replace("\\\\", "\\");
	}
}
