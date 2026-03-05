import api.BookApiServer;
import controller.BookController;
import java.io.IOException;

public class BMSMain {

	public static void main(String[] args) {
		if (args.length > 0 && "cli".equalsIgnoreCase(args[0])) {
			BookController controller = new BookController();
			controller.start();
			return;
		}

		try {
			BookApiServer apiServer = new BookApiServer(8080);
			apiServer.start();
		} catch (IOException ex) {
			System.err.println("Failed to start API server on port 8080.");
			System.out.println(ex.getLocalizedMessage());
		}
	}

}
