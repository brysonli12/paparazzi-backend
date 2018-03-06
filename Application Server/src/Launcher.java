import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpServer;

public class Launcher {
	// Server related variables
	public static final String IPADDRESS = "127.0.0.1";
	public static final int PORT = 8000;
	public static final String CONTEXT[] = { "/login", "/getgames", "/sendmessage", "/creategame", "/getmessages",
				"/joingame", "/rateimage", "/playerexists", "/startgame" };
	
	public static void main(String[] args) throws IOException {
		AtomicBoolean stop = new AtomicBoolean(false);
		ArrayList<JSONObject> startedGames = new ArrayList<JSONObject>();
		
		Thread gameLogic = new Thread(new GameLogicThread(startedGames,stop));
		gameLogic.start();
		
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
		server.createContext(CONTEXT[0], new LoginHandler());
		server.createContext(CONTEXT[1], new RetrieveGameHandler());
		server.createContext(CONTEXT[2], new SendMessageHandler());
		server.createContext(CONTEXT[3], new CreateGameHandler());
		server.createContext(CONTEXT[4], new GetMessagesHandler());
		server.createContext(CONTEXT[5], new JoinGameHandler());
		server.createContext(CONTEXT[6], new RateImageHandler());
		server.createContext(CONTEXT[7], new PlayerExistHandler());
		server.setExecutor(null);
		server.start();

		System.out.println("HTTP Server started...");

		Scanner getConsoleInput = new Scanner(System.in);

		while (getConsoleInput.nextLine().equals("exit") == false) {
			System.out.println("Unrecognized command");
			System.out.println("Recognized commands: exit");
		}

		System.out.println("Server will stop after 10 seconds...");
		stop.set(true);
		server.stop(10);
		getConsoleInput.close();
		System.out.println("Launcher exited...");
	}

}
