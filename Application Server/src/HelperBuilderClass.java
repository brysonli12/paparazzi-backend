import java.util.Iterator;

import org.json.simple.JSONObject;

public class HelperBuilderClass {
	//FIX THIS FUNCTION
	public static int readRequest(JSONObject request) {
		if(request == null) {
			return -1;
		}
		
		if(request.containsKey("Player") || request.containsKey("Message")) {
			return 0;
		}
		return -1;
	}
	
	public static JSONObject buildResponse(int requestType, JSONObject data) {
		JSONObject response = new JSONObject();
		
		if(data == null) {
			response.put("error", "Invalid Request");
			return response;
		}
		
		switch(requestType) {
			case -1:
				response.put("error", "Invalid Request");
				break;
			case 1:
				response = data;
				break;
			case 2:
				response = data;
				break;
			default:
				response.put("error", "Invalid Request");
				break;
		}
		return response;
	}
}
