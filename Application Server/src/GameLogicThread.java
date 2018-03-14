import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
			//DO NOT DUPLICATE GAMES ALREADY IN THE LIST
			//THAT LOGIC MUST BE DONE IN DATABASE UTILS
			Database util = new Database();
			PushNotifier notify = new PushNotifier();
			util.fetchStarted(startedGames);
			
			for(Iterator<JSONObject> get = startedGames.iterator(); get.hasNext();) {
				JSONObject game = get.next();
				
				//TODO: CHECK WHO IS PAPARAZZI
				//IF THERE IS NONE RANDOMLY SELECT ONE
				JSONArray playerBeenPaparazzi = (JSONArray) game.get(Database.PAPHISTORY);
				JSONObject gameInfo = (JSONObject) game.get(Database.GAME_INFO);
				JSONArray playerInGame = (JSONArray) game.get(Database.PLAYER_PLURAL);
				
				if(game.get(Database.PAPARAZZI).equals("")
						&& (Long) gameInfo.get(Database.STATE) == 1) {
					Random rand = new Random();
					int index = rand.nextInt(playerInGame.size());
					JSONObject paparazziPlayer = (JSONObject) playerInGame.get(index);
					
					int index2 = rand.nextInt(playerInGame.size());
					while(index == index2){
						index2 = rand.nextInt(playerInGame.size());
					}
					JSONObject targetPlayer = (JSONObject) playerInGame.get(index2);
					
					util.setPapTarget((String) gameInfo.get(Database.GAME_RM_NAME),paparazziPlayer,targetPlayer);
					playerBeenPaparazzi.set(index, (Long) playerBeenPaparazzi.get(index)+1);
					//Cannot use remove since SQL update is not synchronous
					//get.remove();
					//notify.sendPush(message);
					continue;
				}
				
				//TODO: TIME IS UP
				//SELECT NEW PAPARAZZI
				long totalTurns = 0;
				long max = 0;
				for(int i = 0; i < playerBeenPaparazzi.size(); i++){
					totalTurns += (Long) playerBeenPaparazzi.get(i);
					if(max < (Long)playerBeenPaparazzi.get(i)){
						max = (Long) playerBeenPaparazzi.get(i);
					}
				}
				
				long value = System.currentTimeMillis() - (((Long)game.get(Database.START_TIME)) + totalTurns * ((Long)game.get(Database.TIME_PER_PERSON)));
				
				System.out.println("TIME " + System.currentTimeMillis());
				System.out.println("VALUE " + value);
				System.out.println("TOTAL TURN STUFF " + totalTurns + " " + max + " "  + ((Long)game.get(Database.START_TIME)) + " " + totalTurns * ((Long)game.get(Database.TIME_PER_PERSON)));
				System.out.println("GET START TIME " + ((Long)game.get(Database.START_TIME)));
				System.out.println("GET DURATION TIME " + ((Long)game.get(Database.TIME_PER_PERSON)));
				System.out.println("MAX TURNS " + ((Long)game.get(Database.MAX_TURNS)));
				System.out.println("PLAYER ARRAY " + playerInGame.size());
				
				if(System.currentTimeMillis() - ((Long)game.get(Database.START_TIME) + totalTurns * (Long)game.get(Database.TIME_PER_PERSON)) > 0
				 	&& totalTurns < (Long)game.get(Database.MAX_TURNS) * playerInGame.size()
				 	&& (Long) gameInfo.get(Database.STATE) == 1) {
				 	
					System.out.println("NEWTARGET NEWTARGET NEWTARGET");
					
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
					playerBeenPaparazzi.set(index, (Long) playerBeenPaparazzi.get(index)+1);
					
					//Cannot use remove since SQL update is not synchronous
					//get.remove();
					//notify.sendPush(message);
					continue;
				}
				/*
				//TODO: IF EVERYONE HAS BEEN PAPARAZZI
				//END GAME AND START RATING TIMER DURATION
				if(System.currentTimeMillis() - ((Long)game.get(Database.START_TIME) + totalTurns * (Long)game.get(Database.TIME_PER_PERSON)) > 0
				 	&& totalTurns == (Long) game.get(Database.MAX_TURNS) * playerInGame.size() 
				 	&& (Long) game.get(Database.STATE) != 2) {
				 	
					util.setState((String) gameInfo.get(Database.GAME_RM_NAME),2);
					game.put(Database.STATE,2);
					game.put(Database.START_LAST_RATING, System.currentTimeMillis());
					
					//Cannot use remove since SQL update is not synchronous
					//get.remove();
					//notify.sendPush(message);
					continue;
				}
				*/
				/*
				//TODO: GET ALL IMAGES IN MESSAGES OF THAT GAME AND SUM THE RATINGS
				//SUM THE RATINGS
				if(System.currentTimeMillis() - (Long) game.get(Database.START_LAST_RATING) > TimeUnit.MINUTES.toMillis(10) 
						&& (Long) game.get(Database.STATE) == 2) {
				 	
					JSONArray getAllImageMessages = new JSONArray();
					util.fetchImages((String) gameInfo.get(Database.GAME_RM_NAME), getAllImageMessages);
					long scoreTally[] = new long[playerInGame.size()];
					
					for(int i = 0; i < playerInGame.size(); i++) {
						scoreTally[i] = 0;
						JSONObject player = (JSONObject) playerInGame.get(i);
						String playerId = (String) player.get(Database.FB_USER_ID);
						
						for(int j = 0; j < getAllImageMessages.size(); j++) {
							JSONObject imageMessage = (JSONObject) getAllImageMessages.get(j);
							JSONObject sentFromPlayer = (JSONObject) imageMessage.get(Database.SENTFROM);
							String sentFromPlayerId = (String) sentFromPlayer.get(Database.FB_USER_ID);
							
							if(playerId.equals(sentFromPlayerId)) {
								JSONObject image = (JSONObject) imageMessage.get(Database.IMAGE);
								JSONArray imageRating = (JSONArray) image.get(Datbase.RATINGS);
								for(int k = 0; k < playerInGame.size(); k++) {
									scoreTally[i] += (Long) imageRating.get(k);
								}
							}
						}
					}
					
					long maxScoreIndex = 0;
					long maxScore = 0;
					for(int i = 0; i < scoreTally.length; i++){
						if(maxScore < scoreTally[i]){
							maxScoreIndex = i;
							maxScore = scoreTally[i];
						}
					}
					
					util.setWinner((String) gameInfo.get(Database.GAME_RM_NAME),(JSONObject) playerInGame.get((int) maxScoreIndex));
					util.setState((String) gameInfo.get(Database.GAME_RM_NAME),0);
					game.put(Database.STATE,0);
					 
					//Cannot use remove since SQL update is not synchronous
					//get.remove();
					//notify.sendPush(message);
					continue;
				}
				*/
				/*
				//TODO: CHECK IF GAME HAS BEEN SET TO STATE 0 IN SQL THEN REMOVE
				if((long) game.get(Database.START_TIME) == 0 && util.isStateZero((String) gameInfo.get(Database.GAME_RM_NAME)) == true) {
					get.remove();
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
