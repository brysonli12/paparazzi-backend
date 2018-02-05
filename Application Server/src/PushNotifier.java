import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class PushNotifier {
	
	public void sendPush() throws IOException{
		final String apiKey = "MyPrivateKey";
		URL url = new URL("https://fcm.googleapis.com/fcm/send");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "key=" + apiKey);

		conn.setDoOutput(true);

		JSONObject notificationMessage = new JSONObject();
		JSONObject notification = new JSONObject();
		JSONObject data = new JSONObject();
		JSONObject dataMessage = new JSONObject();
		
		notification.put("to", "/topics/AllUsers");
		notificationMessage.put("body", "This is an automated message.");
		notificationMessage.put("title", "New Update");
		notification.put("notification", notificationMessage);
		
		data.put("to", "/topics/AllUsers");
		dataMessage.put("message", "Hello world");
		data.put("data",dataMessage);
		
		OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
		os.write(notification.toString());
		os.flush();
		os.close();
		
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post Parameters Notify : " + notification.toString());
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		String response = "";

		while ((inputLine = in.readLine()) != null) {
		    response+=inputLine;
		}
		in.close();

		// print result
		System.out.println(response.toString());
		
		url = new URL("https://fcm.googleapis.com/fcm/send");
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", "key=" + apiKey);

		os = new OutputStreamWriter(conn.getOutputStream());
		os.write(data.toString());
		os.flush();
		os.close();
		
		responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post Parameters Data : " + data.toString());
		System.out.println("Response Code : " + responseCode);

		in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		response = "";

		while ((inputLine = in.readLine()) != null) {
		    response+=inputLine;
		}
		in.close();

		// print result
		System.out.println(response.toString());
	}
}
