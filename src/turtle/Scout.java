package turtle;

import battlecode.common.*;
import java.util.*;

/**
 * Created by allen on 14 JAN 2016
 */
public class Scout {

	public static void run(RobotController rc) {
		MapLocation home = rc.getLocation();
		int spiralCount = 1;
		int stepSize = 8;
		int pullStatus = 0; // 0 = scout, 1 = pull, 2 = moving towards den;
		MapLocation denLoc = null;
		MapLocation pullTo = null;
		MapLocation pullFrom = null;
		MapLocation lastArchonLoc = new MapLocation(0, 0);
		MapLocation spiralTo = null;
		Team myTeam, otherTeam = null;
		TreeSet<Integer> sentNeutrals = new TreeSet<Integer>();
		TreeSet<Integer> sentParts = new TreeSet<Integer>();
		// double prevHealth;
		try {
			myTeam = rc.getTeam();
			otherTeam = myTeam.opponent();
			Signal[] sigs = rc.emptySignalQueue();
			for (Signal s : sigs) {
				if (s.getTeam() == rc.getTeam()) {
					int[] msg = s.getMessage();
					if (msg != null) {
						if (msg[0] % Utils.SCOUT_PULL == 0 && msg[0] / Utils.SCOUT_PULL == rc.getID()) {
							lastArchonLoc = s.getLocation();
							int[] loc = Utils.unpack2(msg[1]);
							if (denLoc == null) {
								denLoc = new MapLocation(loc[0], loc[1]);
							} else {
								pullTo = new MapLocation(loc[0], loc[1]);
							}
							pullStatus = 2;
						}
						break;
					}
				}
			}
			spiralTo = rc.getLocation().add(Direction.EAST, stepSize);
			if (pullStatus > 0) {
				switch (denLoc.directionTo(pullTo)) {
				case NORTH:
					pullFrom = new MapLocation(denLoc.x, denLoc.y - 4);
					break;
				case NORTH_EAST:
					pullFrom = new MapLocation(denLoc.x + 3, denLoc.y - 3);
					break;
				case EAST:
					pullFrom = new MapLocation(denLoc.x + 4, denLoc.y);
					break;
				case SOUTH_EAST:
					pullFrom = new MapLocation(denLoc.x + 3, denLoc.y + 3);
					break;
				case SOUTH:
					pullFrom = new MapLocation(denLoc.x, denLoc.y + 4);
					break;
				case SOUTH_WEST:
					pullFrom = new MapLocation(denLoc.x - 3, denLoc.y + 3);
					break;
				case WEST:
					pullFrom = new MapLocation(denLoc.x - 4, denLoc.y);
					break;
				case NORTH_WEST:
					pullFrom = new MapLocation(denLoc.x - 3, denLoc.y - 3);
					break;
				default:
					System.out.println("uwut");
				}
			} 
			pullTo = new MapLocation(rc.getLocation().x + stepSize, rc.getLocation().y);
			// prevHealth = rc.getHealth();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		while (true) {
			try {
				Signal[] sigs = rc.emptySignalQueue();
				for (Signal s : sigs) {
					if (s.getTeam() == rc.getTeam()) {
						int[] msg = s.getMessage();
						if (msg != null) {
							if (msg[0] % Utils.SCOUT_PULL == 0 && msg[0] / Utils.SCOUT_PULL == rc.getID()) {
								lastArchonLoc = s.getLocation();
								int[] loc = Utils.unpack2(msg[1]);
								pullTo = new MapLocation(loc[0], loc[1]);
								pullStatus = 1;
							}
							break;
						}
						switch (denLoc.directionTo(pullTo)) {
						case NORTH:
							pullFrom = new MapLocation(denLoc.x, denLoc.y - 4);
							break;
						case NORTH_EAST:
							pullFrom = new MapLocation(denLoc.x + 3, denLoc.y - 3);
							break;
						case EAST:
							pullFrom = new MapLocation(denLoc.x + 4, denLoc.y);
							break;
						case SOUTH_EAST:
							pullFrom = new MapLocation(denLoc.x + 3, denLoc.y + 3);
							break;
						case SOUTH:
							pullFrom = new MapLocation(denLoc.x, denLoc.y + 4);
							break;
						case SOUTH_WEST:
							pullFrom = new MapLocation(denLoc.x - 3, denLoc.y + 3);
							break;
						case WEST:
							pullFrom = new MapLocation(denLoc.x - 4, denLoc.y);
							break;
						case NORTH_WEST:
							pullFrom = new MapLocation(denLoc.x - 3, denLoc.y - 3);
							break;
						default:
							System.out.println("uwut");
						}
					}
				}

				RobotInfo[] enemies = rc.senseNearbyRobots(53, otherTeam);
				RobotInfo[] zombies = rc.senseNearbyRobots(53, Team.ZOMBIE);
				RobotInfo[] nearZombies = rc.senseNearbyRobots(13, Team.ZOMBIE);
				
				MapLocation currLoc = rc.getLocation();

				boolean safe;
				boolean hasMoved = false;
				switch (pullStatus) {
				case 2:
					// move towards den
					hasMoved = Utils.safeMove(rc, pullFrom, enemies, zombies);
					if (currLoc.equals(pullFrom) || enemies.length >= 5) {
						pullStatus = 1;
					}

					break;
				case 1:
					safe = true;
					for (RobotInfo i : nearZombies) {
						if (i.type.equals(RobotType.RANGEDZOMBIE)) {
							safe = false;
							break;
						} else if (i.location.distanceSquaredTo(currLoc) <= 2) {
							safe = false;
							break;
						}
					}

					if (!safe) {
						// move towards destination, if possible
						hasMoved = Utils.safeMove(rc, pullTo, enemies, zombies);
					}
					if (zombies.length == 0 && !currLoc.equals(pullFrom))
						pullStatus = 2;
					if(currLoc.equals(pullTo) && enemies.length == 0){
						pullTo = currLoc.add(currLoc.directionTo(lastArchonLoc).opposite());
					}
					break;
				default:
					// scout
					if(currLoc.distanceSquaredTo(pullTo) <= 2 && !rc.canMove(currLoc.directionTo(spiralTo))
							&& rc.senseRobotAtLocation(currLoc.add(currLoc.directionTo(spiralTo))) == null) // at wall
						spiralTo = currLoc;
					Utils.safestMove(rc, spiralTo, enemies, zombies);

					if (rc.getLocation().equals(spiralTo)
							|| (!hasMoved && currLoc.distanceSquaredTo(spiralTo) <= 53)) {
						int delta = ((spiralCount + 3) / 2) * stepSize;
						if ((spiralCount / 2) % 2 == 0)
							delta *= -1;
						if (spiralCount % 2 == 0) {
							spiralTo = new MapLocation(spiralTo.x, spiralTo.y + delta);
						} else {
							spiralTo = new MapLocation(spiralTo.x + delta, spiralTo.y);
						}
					}

					break;
				}

				int range = Math.min(424, rc.getLocation().distanceSquaredTo(lastArchonLoc));

				for (int i = 0; i < zombies.length && rc.getMessageSignalCount() <= 20; i++) {
					if (zombies[i].type.equals(RobotType.ZOMBIEDEN)) {
						rc.broadcastMessageSignal(Utils.SEE_DEN,
								Utils.pack2(zombies[i].location.x, zombies[i].location.y), range);
					}
				}

				for (int i = 0; i < enemies.length && rc.getMessageSignalCount() <= 20; i++) {
					if (enemies[i].type.equals(RobotType.ARCHON))
						rc.broadcastMessageSignal(Utils.SEE_ARCHON * enemies[i].ID,
								Utils.pack2(enemies[i].location.x, enemies[i].location.y), range);
					if (enemies[i].type.equals(RobotType.TURRET) || enemies[i].type.equals(RobotType.TTM))
						rc.broadcastMessageSignal(Utils.SEE_TURRET * enemies[i].ID,
								Utils.pack2(enemies[i].location.x, enemies[i].location.y), range);
				}

				RobotInfo[] neutrals = rc.senseNearbyRobots(53, Team.NEUTRAL);
				for (int i = 0; i < neutrals.length && rc.getMessageSignalCount() <= 20; i++) {
					int loc = Utils.pack2(neutrals[i].location.x, neutrals[i].location.y);
					if (sentNeutrals.contains(loc))
						continue;
					switch (neutrals[i].type) {
					case ARCHON:
						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_ARCHON, loc, range);
						break;
					case SCOUT:
						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_SCOUT, loc, range);
						break;
					case SOLDIER:
						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_SOLDIER, loc, range);
						break;
					case GUARD:
						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_GUARD, loc, range);
						break;
					case VIPER:
						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_VIPER, loc, range);
						break;
					default:
						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_TURRET, loc, range);
					}
					sentNeutrals.add(loc);
				}

				MapLocation[] parts = rc.sensePartLocations(106);
				for (int i = 0; i < parts.length && rc.getMessageSignalCount() <= 20; i++) {
					int loc = Utils.pack2(parts[i].x, parts[i].y);
					if (sentParts.contains(loc))
						continue;
					if (rc.senseParts(parts[i]) < 25) // not enough to build
														// anything
						rc.broadcastMessageSignal(Utils.SEE_SMALL_PARTS, loc, range);
					else if (rc.senseParts(parts[i]) >= 130) // enough to build
																// anything
						rc.broadcastMessageSignal(Utils.SEE_BIG_PARTS, loc, range);
					else
						rc.broadcastMessageSignal(Utils.SEE_MED_PARTS, loc, range);
				}

				Clock.yield();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
