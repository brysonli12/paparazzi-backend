import org.json.simple.JSONObject;

public class HelperBuilderClass {
	//FIX THIS FUNCTION
	public static int readRequest(JSONObject request) {
		//if(request == null || request.keySet().size() != 1) {
		//	return -1;
		//}
	
		switch((String) request.keySet().iterator().next()) {
			case "Player":
				return 0;
			case "Message":
				return 0;
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
			case 2:
				return data;
			default:
				response.put("error", "Invalid Request");
				return response;
		}
	}
}
