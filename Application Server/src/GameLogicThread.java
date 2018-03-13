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
				
				//TODO: TIME IS UP
				//SELECT NEW PAPARAZZI
				JSONArray playerBeenPaparazzi = (JSONArray) game.get(Database.PAPHISTORY);
				long totalTurns = 0;
				long max = 0;
				for(int i = 0; i < playerBeenPaparazzi.size(); i++){
					totalTurns += (Long) playerBeenPaparazzi.get(i);
					if(max < (Long)playerBeenPaparazzi.get(i)){
						max = (Long) playerBeenPaparazzi.get(i);
					}
				}
				
				JSONObject gameInfo = (JSONObject) game.get(Database.GAME_INFO);
				JSONArray playerArray = (JSONArray) game.get(Database.PLAYER_PLURAL);
				long value = System.currentTimeMillis() - (((Long)game.get(Database.START_TIME)) + totalTurns * ((Long)game.get(Database.TIME_PER_PERSON)));
				
				System.out.println("TIME " + System.currentTimeMillis());
				System.out.println("VALUE " + value);
				System.out.println("TOTAL TURN STUFF " + totalTurns + " " + max + " "  + ((Long)game.get(Database.START_TIME)) + " " + totalTurns * ((Long)game.get(Database.TIME_PER_PERSON)));
				System.out.println("GET START TIME " + ((Long)game.get(Database.START_TIME)));
				System.out.println("GET DURATION TIME " + ((Long)game.get(Database.TIME_PER_PERSON)));
				System.out.println("MAX TURNS " + ((Long)game.get(Database.MAX_TURNS)));
				System.out.println("PLAYER ARRAY " + playerArray.size());
				
				if(System.currentTimeMillis() - ((Long)game.get(Database.START_TIME) + totalTurns * (Long)game.get(Database.TIME_PER_PERSON)) > 0
				 	&& totalTurns < (Long)game.get(Database.MAX_TURNS) * playerArray.size()) {
				 	
					System.out.println("NEWTARGET NEWTARGET NEWTARGET");
					
					JSONArray playerInGame = (JSONArray) game.get(Database.PLAYER_PLURAL);
					
					Random rand = new Random();
					int index = 0;
					JSONObject paparazziPlayer = (JSONObject) playerInGame.get(rand.nextInt(playerInGame.size()));
					boolean allSame = true;
					int compare = ((Long) playerBeenPaparazzi.get(0)).intValue();
					ArrayList<Integer> indicesToSelect = new ArrayList<Integer>();
					
					for(int i = 1; i < playerBeenPaparazzi.size(); i++){
						if(((Long) playerBeenPaparazzi.get(i)).intValue() != compare){
							allSame = false;
							indicesToSelect.add(i);
						}
					}
					
					if(allSame == true){
						index = rand.nextInt(playerInGame.size());
					}else{
						index = rand.nextInt(indicesToSelect.size());
					}
					
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
