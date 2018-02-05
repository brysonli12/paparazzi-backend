import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ClientTester {
	public static void main(String[] args){
		launchTest();
	}
	
	private static void launchTest() {
		try {
			Socket connect = new Socket("127.0.0.1", 5555);
			
			JSONObject playerJSON = HelperTestClasses.randomPlayerClass();
			Thread.sleep(1000 * HelperTestClasses.randomSleep());
			
			OutputStreamWriter os = new OutputStreamWriter(connect.getOutputStream());
			os.write(playerJSON.toString() + "\n");
			os.flush();
			
			System.out.println("Sent: " + playerJSON.toString());
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			String get = in.readLine();
			
			JSONParser parse = new JSONParser();
			JSONObject request = (JSONObject) parse.parse(get);	
			
			System.out.println("Server Responded: " + request.toString());
			
			os.close();
			in.close();
			connect.close();
		} catch (IOException | ParseException | InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Test case finished");
	}
	

}
