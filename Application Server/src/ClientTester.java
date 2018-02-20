import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.json.simple.JSONObject;

public class ClientTester {
	private static final int cases = 1;
	
	public static void main(String[] args){
		launchTest();
	}
	
	private static void launchTest() {
		try {
			URL url = new URL("http://" + HelperTestClasses.ipAddress +":" + HelperTestClasses.port + HelperTestClasses.context[0]);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setDoOutput(true);
			connect.setRequestMethod("POST");
			connect.setRequestProperty("Content-Type", "application/json");
			connect.setRequestProperty("Authorization", "key=Michael");

			connect.setDoOutput(true);
			
			JSONObject send = new JSONObject();
			
			switch(cases) {
				case 1:
					send.put("Player", HelperTestClasses.randomPlayerClass());
				case 3:
					send = HelperTestClasses.randomClientMessage();
					send.put("GameID", new Random().nextInt(1001));
				default:
					send.put("Player", HelperTestClasses.randomPlayerClass());
			}
			
			Thread.sleep(1000 * HelperTestClasses.randomSleep());
			OutputStreamWriter os = new OutputStreamWriter(connect.getOutputStream());
			os.write(send.toString());
			os.flush();
			
			int responseCode = connect.getResponseCode();
			System.out.println("Sending 'POST' request to URL : " + url);
			System.out.println("Sent: " + send.toString());
			System.out.println("Response Code : " + responseCode);

			BufferedReader in;
			if(responseCode != 200) {
				in = new BufferedReader(new InputStreamReader(connect.getErrorStream()));
			}else {
				in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			}
			
			String inputLine;
			String response = "";

			while ((inputLine = in.readLine()) != null) {
			    response+=inputLine;
			}

			// print result
			System.out.println(response.toString());
			
			os.close();
			in.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("Test case finished");
	}
	

}
