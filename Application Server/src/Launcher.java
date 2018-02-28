import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

import com.sun.net.httpserver.HttpServer;

public class Launcher {

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(HelperTestClasses.port), 0);
		server.createContext(HelperTestClasses.context[0], new LoginHandler());
		server.createContext(HelperTestClasses.context[1], new RetrieveGameHandler());
		server.createContext(HelperTestClasses.context[2], new SendMessageHandler());
		server.createContext(HelperTestClasses.context[3], new CreateGameHandler());
		server.createContext(HelperTestClasses.context[4], new GetMessagesHandler());
		server.createContext(HelperTestClasses.context[5], new JoinGameHandler());
		server.createContext(HelperTestClasses.context[6], new RateImageHandler());
		server.createContext(HelperTestClasses.context[7], new PlayerExistHandler());
		server.setExecutor(null);
		server.start();

		System.out.println("HTTP Server started...");

		Scanner getConsoleInput = new Scanner(System.in);

		while (getConsoleInput.nextLine().equals("exit") == false) {
			System.out.println("Unrecognized command");
			System.out.println("Recognized commands: exit");
		}

		System.out.println("Server will stop after 10 seconds...");
		server.stop(10);
		getConsoleInput.close();
		System.out.println("Launcher exited...");
	}

}
