import org.json.simple.JSONObject;

public class HelperBuilderClass {
	public static int readRequest(JSONObject request) {
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
	
	public static JSONObject buildResponse(int requestType, JSONObject data) {
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
				//response.put("LoginResponse", data);
				return data;
			default:
				response.put("error", "Invalid Request");
				return response;
		}
	}
}
