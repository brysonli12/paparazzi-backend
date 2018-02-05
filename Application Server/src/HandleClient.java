import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HandleClient implements Runnable{
	private Socket client;
	
	HandleClient(Socket client){
		this.client = client;
	}
	
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String get = "";
			try {
				get = in.readLine();
			}catch(SocketTimeoutException e) {
				//e.printStackTrace();
				in.close();
				client.close();
				System.out.println("Client failed to respond in time...\n");
				return;
			}
			
			JSONParser parse = new JSONParser();
			JSONObject request;
			
			try {
				request = (JSONObject) parse.parse(get);
			}catch(ParseException e) {
				//e.printStackTrace();
				request = null;
			}
			
			//Handle request by sending to JDBC
			int requestType = readRequest(request);
			//data = handleRequest(requestType);
			//below is a hard coded to handle login response
			JSONObject data = HelperTestClasses.randomLoginResponseClass(request);
			
			//Send back JSONObject or a push notification
			JSONObject response = buildResponse(requestType, data);
			
			OutputStreamWriter os = new OutputStreamWriter(client.getOutputStream());
			os.write(response.toString() + "\n");
			os.flush();
			
			System.out.println("Sending Response: " + response.toString() + "\n");

			in.close();
			os.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();		
		}
	}
	
	private int readRequest(JSONObject request) {
		if(request == null || request.keySet().size() != 1) {
			return -1;
		}
	
		switch((String) request.keySet().iterator().next()) {
			case "Player":
				return 1;
			default:
				return -1;	
		}
	}
	
	private JSONObject buildResponse(int requestType, JSONObject data) {
		JSONObject response = new JSONObject();
		
		if(data == null) {
			response.put("error", "Invalid Request");
			return response;
		}
		
		switch(requestType) {
			case -1:
				response.put("error", "Invalid Request");
				return response;
			case 1:
				response.put("LoginResponse", data);
				return response;
			default:
				response.put("error", "Invalid Request");
				return response;
		}

	}
}
