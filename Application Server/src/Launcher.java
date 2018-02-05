import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Launcher {

	public static void main(String[] args) {
		AtomicBoolean exit = new AtomicBoolean(false);
		
		AppServer server = new AppServer(5555,exit);
		new Thread(server).start();
		
		Scanner getConsoleInput = new Scanner(System.in);
		String get;
		
		while((get = getConsoleInput.nextLine()).equals("exit") == false) {
			System.out.println(get);
		}

		getConsoleInput.close();
		exit.set(true);
		System.out.println("Launcher exiting...");
	}

}
