import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class HelperTestClasses {
	//Server related variables
	public static final String ipAddress = "127.0.0.1";
	public static final int port = 8000;
	public static final String context[] = {"/login","/getgames"};
	
	//JSON Response related variables
	private static final String letterList = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String numberList = "0123456789";
	private static final String playerStatus[] = {"returningPlayer","newPlayer","failed"};
	private static final int maxUserId = 31;
	private static final int maxFirstName = 21;
	private static final int maxLastName = 21;
	private static final int maxGameName = 26;
	private static final int maxPlayerCount = 6;
	private static final int maxMessages = 11;
	private static final int maxChatSize = 31;
	private static final int maxImageSize = 51;
	private static final int maxSleepSeconds = 5;
	private static final int maxDurationDays = 11;
	private static final int maxGames = 3;

	public static JSONObject randomPlayerClass() {
		JSONObject testerPlayer = new JSONObject();
		JSONObject player = new JSONObject();
		int randomValue;
		Random rand = new Random();

		String facebookUserId = "";
		String firstName = "";
		String lastName = "";

		randomValue = rand.nextInt(maxUserId);
		for(int i = 0; i < randomValue; i++) {
			if(rand.nextInt(2) == 0) {
				facebookUserId += letterList.charAt(rand.nextInt(26));
			}else {
				facebookUserId += numberList.charAt(rand.nextInt(10));
			}
		}

		randomValue = rand.nextInt(maxFirstName);
		for(int i = 0; i < randomValue; i++) {
			firstName += letterList.charAt(rand.nextInt(26));
		}

		randomValue = rand.nextInt(maxLastName);
		for(int i = 0; i < randomValue; i++) {
			lastName += letterList.charAt(rand.nextInt(26));
		}

		player.put("facebookUserId", facebookUserId);
		player.put("firstName", firstName);
		player.put("lastName", lastName);
		//testerPlayer.put("Player", player);
		
		return player;//testerPlayer;

	}

	public static JSONObject randomGameInfoClass(int passedMaxPlayerCount) {
		JSONObject game = new JSONObject();
		JSONObject gameInfo = new JSONObject();
		int randomValue;
		Random rand = new Random();

		String gameRoomName = "";

		randomValue = rand.nextInt(maxGameName);
		for(int i = 0; i < randomValue; i++) {
			gameRoomName += letterList.charAt(rand.nextInt(26));
		}

		gameInfo.put("gameRoomName", gameRoomName);
		gameInfo.put("playerCount", passedMaxPlayerCount);
		gameInfo.put("gameDuration", 24*60*60*rand.nextInt(maxDurationDays));
		//game.put("GameInfo", gameInfo);
		
		return gameInfo;
	}

	public static JSONArray randomMessageClass() {
		//JSONObject message = new JSONObject();
		JSONArray messageList = new JSONArray();
		int randomValue;
		Random rand = new Random();
		
		randomValue = rand.nextInt(maxMessages);
		for(int i = 0; i < randomValue; i++) {
			JSONObject messageData = new JSONObject();
			JSONObject player = HelperTestClasses.randomPlayerClass();
			String chatMessage = "";
			//For now bitmap is a string
			String image = "";
			int randomValue2;

			randomValue2 = rand.nextInt(maxChatSize);
			for(int j = 0; j < randomValue; j++) {
				chatMessage += letterList.charAt(rand.nextInt(26));
			}

			randomValue2 = rand.nextInt(maxImageSize);
			for(int j= 0; j < randomValue; j++) {
				image += letterList.charAt(rand.nextInt(26));
			}

			messageData.put("sentFrom", player);
			messageData.put("message", chatMessage);
			messageData.put("image", image);

			messageList.add(messageData);
		}
		//message.put("Message",messasgeList);
		return messageList;
	}

	public static JSONObject randomLoginResponseClass(JSONObject player) {
		JSONObject data = new JSONObject();
		JSONArray games = HelperTestClasses.randomGamesClass();
		Random rand = new Random();

		String getStatus = playerStatus[rand.nextInt(3)];
	
		if(getStatus.equals("failed") == false) {
			data.put("loginStatus", getStatus);
			data.put("player",player);
			data.put("games", games);
		}else {
			data.put("loginStatus", getStatus);
			data.put("player","null");
			data.put("games", "null");
		}

		return data;
	}

	public static JSONArray randomGamesClass() {
		JSONObject game = new JSONObject();
		JSONArray gamesArray = new JSONArray();
		Random rand = new Random();
		int randomValue;

		randomValue = rand.nextInt(maxGames);
		for(int i = 0; i < randomValue; i++) {
			JSONObject gamesData = new JSONObject();
			//JSONObject players = new JSONObject();
			JSONArray playersList = new JSONArray();

			int randomValue2 = rand.nextInt(maxPlayerCount);			
			for(int j = 0; j < randomValue2; j++) {
				playersList.add(HelperTestClasses.randomPlayerClass());
			}

			//players.put("Player",playersList);
			gamesData.put("gameId", rand.nextInt(1001));
			gamesData.put("gameInfo", HelperTestClasses.randomGameInfoClass(randomValue2));
			gamesData.put("players", playersList);
			gamesData.put("messages", HelperTestClasses.randomMessageClass());

			gamesArray.add(gamesData);
		}
		
		//game.put("Game", gamesArray);

		return gamesArray;
	}

	public static int randomSleep() {
		Random rand = new Random();
		return rand.nextInt(maxSleepSeconds);
	}
}
