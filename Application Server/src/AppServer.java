import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppServer implements Runnable{
	private int port;
	private AtomicBoolean exit;
	
	AppServer(int port, AtomicBoolean exit){
		this.port = port;
		this.exit = exit;
	}
	
	public void run() {
		try {
			ServerSocket server = new ServerSocket(port);
			server.setSoTimeout(1000);
			System.out.println("Application server ready for Paparazzi clients...");
			
			while(exit.get() == false) {
				Socket client;
				
				try {
					client = server.accept();
					client.setSoTimeout(10000);
				}catch(SocketTimeoutException e) {
					continue;
				}
				
				new Thread(new HandleClient(client)).start();
				System.out.println("Client connected. IP: " + client.getInetAddress().getHostAddress());
			}
			
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Closing Application server...");
	}
}
