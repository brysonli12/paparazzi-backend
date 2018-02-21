import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CreateGameHandler implements HttpHandler {
	public void handle(HttpExchange t) {
		try {		
			BufferedReader in = new BufferedReader(new InputStreamReader(t.getRequestBody()));
			String inputLine;
			String requestBodyText = "";

			while ((inputLine = in.readLine()) != null) {
				requestBodyText+=inputLine;
			}
			
			JSONParser parse = new JSONParser();
			JSONObject request;
			int requestType;
			JSONObject data;
			
			try {
				request = (JSONObject) parse.parse(requestBodyText);
				//requestType = HelperBuilderClass.readRequest(request);
				System.out.println("RECEIVED CREATEGAME: " + request.toString());
				//Handle request by sending to JDBC
				//Ex: data = handleRequest(requestType,code);
				
				//if(requestType == -1) {
				//	data = null;
				//}else {
					Database x = new Database();
					data = x.handleRequest(request, 4);
					requestType = 0;
					//data = new JSONObject();
				//}
				
			}catch(ParseException e) {
				data = null;
				request = null;
				requestType = -1;
			}
			
			//Send back JSONObject or a push notification
			JSONObject response = new JSONObject();
			byte[] responseBytes;
			
			if(requestType == -1 || data == null) {
				response.put("messagestatus","Game room name already taken.");
				responseBytes = response.toString().getBytes();
				t.sendResponseHeaders(200, responseBytes.length);
			}else {
				response.put("messagestatus", "success");
				responseBytes = response.toString().getBytes();
				t.sendResponseHeaders(200, responseBytes.length);
			}
			
			OutputStream os = t.getResponseBody();
			os.write(responseBytes);
			os.flush();
			
			System.out.println("Sending Response: " + response.toString() + "\n");

			in.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();		
		}
	}
}
