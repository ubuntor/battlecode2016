package kappabot;

import battlecode.common.*;

public class RobotPlayer {
	public static RobotController rc;
	
    public static void run(RobotController robotcontroller) {
    	rc = robotcontroller;
    	
    	guardRush.RobotPlayer.run(rc);
    	
    	while (true) {
			try {
				switch (rc.getType()) {
					case ARCHON:
						Archon.run();
						break;
					case GUARD:
						Guard.run();
						break;
					case SCOUT:
						Scout.run();
						break;
					case SOLDIER:
						Soldier.run();
						break;
					case TTM:
					case TURRET:
						Turret.run();
						break;
					case VIPER:
						Viper.run();
						break;
					default:
				}
				Clock.yield(); 
			} catch (GameActionException e) {
				e.printStackTrace();
			}
    	}
    }
}
