package team259;

import battlecode.common.*;

public class RobotPlayer {
	public static RobotController rc;
	public static String map;
	
	public static void run(RobotController robotcontroller) {
		rc = robotcontroller;
		
		if (rc.getRoundNum() == 0) {
			map = identify(rc.getInitialArchonLocations(rc.getTeam()));
		} else {
			map = ""; // we should be listening for a signal
		}
		
		team259.turtlepull.RobotPlayer.run(rc);
//		team259.guardrush.RobotPlayer.run(rc); < run this once its workable
	}
	
	public static String identify(MapLocation[] archons) {
		int d = archons[0].distanceSquaredTo(archons[archons.length-1]);
		switch (d) {
			case 289:
				return "6147";
			case 130:
				return "arena";
			case 260:
				return "backbencher";
			case 986:
				return "barred";
			case 0:
				ZombieSpawnSchedule z = rc.getZombieSpawnSchedule();
				switch (z.getScheduleForRound(z.getRounds()[0])[0].getCount()) {
					case 6:
						return "barricade";
					case 4:
						for (Direction c : Direction.values())
							if (c.isDiagonal() && !rc.canMove(c)) return "seamed";
						return "scouting";
					case 8:
						if (!rc.canMove(Direction.NORTH)) return "spiral";
						if (!rc.canMove(Direction.SOUTH)) return "spiral"; 
						return "swamp";
					default:
						return ""+d;
				}
			case 170:
				return "boxy";
			case 32:
				return "boxy_armageddon";
			case 225:
				if (archons.length == 3) return "castle";
				if (archons.length == 2) return "forts";
				return ""+d;
			case 2:
				return "caverns";
			case 442:
				return "chairs";
			case 2738:
				return "channel";
			case 16:
				return "checkers";
			case 841:
				return "closequarters";
			case 98:
				return "collision";
			case 810:
				return "crater";
			case 256:
			case 281:
				if (archons.length == 4) {
					int t = archons[0].y - archons[1].y;
					if (t == 4 || t == -5) return "crumble";
					if (t == 0) return "frogger";  
				}
				return ""+d;
			case 8:
				return "desert";
			case 2601:
				return "diffusion";
			case 193:
				return "factory";
			case 1156:
				return "farm";
			case 89:
			case 185:
				if (archons.length == 4) return "forest";
				if (archons.length == 2) return "molasses";
				return ""+d;
			case 121:
				return "fortifications";
			case 242:
				return "goodies";
			case 90:
				return "helloworld";
			case 49:
				return "industrial";
			case 64:
				return "lockdown";
			case 148:
				return "needlestone";
			case 596:
				return "nexus";
			case 313:
				return "pants";
			case 157:
	    		if (archons.length == 4) return "placard";
	    		if (archons.length == 2) return "zigzag";
				return ""+d;
			case 261:
				return "presents";
			case 9:
				return "prisons";
			case 290:
				return "quadrants";
			case 1874:
				return "quarry";
			case 317:
				return "quartiles";
			case 626:
				return "river";
			case 325:
				return "slow";
			case 2722:
				return "space";
			case 833:
				return "spaghetti";
			case 2116:
				return "streets";
			case 202:
				return "treasure";
			case 625:
				return "tunnels";
			case 1:
				return "turtle";
			case 353:
				return "voluted";
			case 3314:
				return "vortex";
			case 520:
				return "wormy";
			default:
				return ""+d;
		}
	}
}