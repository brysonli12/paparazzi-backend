import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GameLogicThread implements Runnable{
	List<JSONObject> startedGames;
	AtomicBoolean stop;

	GameLogicThread(ArrayList<JSONObject> startedGames, AtomicBoolean stop){
		this.startedGames = startedGames;
		this.stop = stop;
	}
	
	public void run() {
		while(stop.get() == false) {
			//TODO: ADD INTERFACE TO CONSTANTLY GET NEW STARTED GAMES
			//DO NOT SEND ALREADY FETCHED NEW STARTED GAMES
			//THAT LOGIC MUST BE DONE IN DATABASE UTILS
			Database util = new Database();
			PushNotifier notify = new PushNotifier();
			util.fetchStarted(startedGames); // don't return games already in the list
			
			for(Iterator<JSONObject> get = startedGames.iterator(); get.hasNext();) {
				JSONObject game = get.next();
				//BELOW ARE PLACE HOLDERS
				
				//TODO: CHECK WHO IS PAPARAZZI
				//IF THERE IS NONE RANDOMLY SELECT ONE
				if(game.get(Database.PAPARAZZI).equals("")) {
					System.out.println(game.toString());
					
					JSONArray playerInGame = (JSONArray) game.get(Database.PLAYER_PLURAL);
					JSONArray playerBeenPaparazzi = (JSONArray) game.get(Database.PAPHISTORY);
					JSONObject gameInfo = (JSONObject) game.get(Database.GAME_INFO);
					
					Random rand = new Random();
					int index = rand.nextInt(playerInGame.size());
					JSONObject paparazziPlayer = (JSONObject) playerInGame.get(index);
					
					int index2 = rand.nextInt(playerInGame.size());
					while(index == index2){
						index2 = rand.nextInt(playerInGame.size());
					}
					JSONObject targetPlayer = (JSONObject) playerInGame.get(index2);
					
					util.setPapTarget((String) gameInfo.get(Database.GAME_RM_NAME),paparazziPlayer,targetPlayer);
					get.remove();
					//notify.sendPush(message);
					continue;
				}
				/*
				//TODO: TIME IS UP
				//SELECT NEW PAPARAZZI
				JSONArray playerBeenPaparazzi = (JSONArray) game.get(Database.PAPHISTORY);
				int totalTurns = 0;
				int max = 0;
				for(int i = 0; i < playerBeenPaparazzi.size(); i++){
					totalTurns += playerBeenPaparazzi.get(i);
					if(max < playerBeenPaparazzi[i]){
						max = playerBeenPaparazzi[i];
					}
				}
				
				if(System.currentTimeMillis() - (game.get(STARTTIME) + totalTurns * game.get(TIMEDURATIONPERPERSON)) > 0
				 	&& totalTurns < game.get(MAXTURNS) * game.get(PLAYERCOUNT)) {
				 	
					JSONArray playerInGame = game.get(PLAYERS);
					
					Random rand = new Random();
					int index = 0;
					String paparazziPlayer = playerInGame[rand.nextInt(0,playerInGame.size())];
					boolean allSame = true;
					int compare = playerBeenPaparazzi[0];
					ArrayList<Integer> indicesToSelect = new ArrayList<Integer>();
					
					for(int i = 1; i < playerBeenPaparazzi.size(); i++){
						if(playerBeenPaparazzi[i] != compare){
							allSame = false;
							indiciesToSelect.add(i);
						}
					}
					
					if(allSame == true){
						index = rand.nextInt(0,playerInGame.size());
					}else{
						index = rand.nextInt(0,indicesToSelect.size());
					}
				
					String paparazziPlayer = playerInGame[index];
					playerBeenPaparazzi[index]++;
				
					util.setPapTarget((String) gameInfo.get(Database.GAME_RM_NAME),paparazziPlayer,targetPlayer);
					get.remove();
					//notify.sendPush(message);
					continue;
				}
				/*
				//TODO: IF EVERYONE HAS BEEN PAPARAZZI
				//END GAME AND START RATING TIMER DURATION
				if(System.currentTimeMillis() - (game.get(STARTTIME) + totalTurns * game.get(TIMEDURATIONPERPERSON)) > 0
				 	&& totalTurns == game.get(MAXTURNS) * game.get(PLAYERCOUNT)) {
				 	
					util.endGame(gameRoomName,state);
					notify.sendPush(message);
					get.remove();
					continue;
				}
				
				if(System.currentTimeMillis() - game.get(STARTLASTRATING) < 1000*60*10) {
				 	
				 	//TODO: GET ALL IMAGES IN MESSAGES OF THAT GAME AND SUM THE RATINGS
				 	
					util.endGame(gameRoomName,state);
					notify.sendPush(message);
					get.remove();
					continue;
				}
				*/
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
