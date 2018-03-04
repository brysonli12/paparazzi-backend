import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameLogicThread implements Runnable{
	List<String> startedGames;
	AtomicBoolean stop;

	GameLogicThread(ArrayList<String> startedGames, AtomicBoolean stop){
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
			//util.fetch(startedGames);
			
			for(Iterator<String> get = startedGames.iterator(); get.hasNext();) {
				String gameRoomName = get.next();
				//BELOW ARE PLACE HOLDERS
				/*
				//TODO: CHECK WHO IS PAPARAZZI
				//IF THERE IS NONE RANDOMLY SELECT ONE
				if(util.getPaparazzi(gameRoomName) == "") {
					util.setPaparazzi(gameRoomName);
					notify.sendPush(message);
				}
				
				//TODO: TIME IS UP
				//SELECT NEW PAPARAZZI
				if(util.getStartTime(gameRoomName) > 0 && util.getTotalTurns(gameRoomName) < 0) {
					util.setPaparazzi(gameRoomName);
					notify.sendPush(message);
				}
				
				//TODO: IF EVERYONE HAS BEEN PAPARAZZI
				//END GAME AND COUNT SCORES
				else {
					util.endGame(gameRoomName);
					notify.sendPush(message);
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
