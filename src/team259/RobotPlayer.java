package team259;

import battlecode.common.*;

public class RobotPlayer {
	public static RobotController rc;
	public static String map;
	
	public static void run(RobotController robotcontroller) {
		rc = robotcontroller;
		
		// optimally, we'd only decide on a plan for the first round,
		// and all later robots will have to find out what the plan is.
		// but i dont feel comfortable enough poking around in the
		// other code, if i miss a signal somewhere then the new robot
		// will just kill itself if it has no orders
		/*
		if (rc.getRoundNum() != 0) {
			Signal[] signals = rc.emptySignalQueue();
			if (signals.length > 0) {
				for (Signal s : signals) {
					if (s.getTeam() == rc.getTeam())
						if (s.getMessage() != null)
							// check if it fits guardrush or turtlepull
							// if it does then just run it
				}
			}
			map = ""; // we should be listening for a signal
		} else {
			map = identify(rc.getInitialArchonLocations(rc.getTeam()));
		}
		*/
		map = identify(rc.getInitialArchonLocations(rc.getTeam()));
		if (!map.equals("")) {
			// map identification successful
			switch (map) {
				case "boxy":
				case "caverns":
				case "desert":
				case "factory":
				case "placard":
				case "prisons":
				case "seamed":
				case "spiral":
				case "streets":
				case "swamp":
				case "vortex":
				case "wormy":
					team259.turtlepull.RobotPlayer.run(rc);
//					team259.guardrush.RobotPlayer.run(rc);
				default:
			}
		} else {
			// fuck we gotta do this ad hoc
			if (rc.getTeam() == Team.A)
				team259.turtlepull.RobotPlayer.run(rc);
			team259.guardrush.RobotPlayer.run(rc);
		}
		
		
		team259.turtlepull.RobotPlayer.run(rc); // when in doubt run turtle

//		also run in the directory (to set up): sed -i 's/package .*/package team259.guardrush;/g' *.java
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
						return "";
				}
			case 170:
				return "boxy";
			case 32:
				return "boxy_armageddon";
			case 225:
				if (archons.length == 3) return "castle";
				if (archons.length == 2) return "forts";
				return "";
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
				return "";
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
				return "";
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
				return "";
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
				return "";
		}
	}
}