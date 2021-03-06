package bigturtle;

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
		int pullStatus = 0; // 0 = scout, 1 = pull, 2 = moving towards den, -1 = vision
		MapLocation denLoc = null;
		MapLocation pullTo = null;
		MapLocation pullFrom = null;
		MapLocation lastArchonLoc = null;
		MapLocation spiralTo = null;
		MapLocation vision = null;
		Team myTeam, otherTeam = null;
		TreeSet<Integer> sentDens = new TreeSet<Integer>();
//		TreeSet<Integer> sentNeutrals = new TreeSet<Integer>();
//		TreeSet<Integer> sentParts = new TreeSet<Integer>();
		// double prevHealth;
		try {
			myTeam = rc.getTeam();
			otherTeam = myTeam.opponent();
			lastArchonLoc = rc.getLocation();
			Signal[] sigs = rc.emptySignalQueue();
			
			for (Signal s : sigs) {
				if (s.getTeam() == rc.getTeam()) {
					int[] msg = s.getMessage();
					if (msg != null) {
//						System.out.println(Arrays.toString(msg));
						if (msg[0] % Utils.SCOUT_PULL_TO == 0 && msg[0] / Utils.SCOUT_PULL_TO == rc.getID()) {
							lastArchonLoc = s.getLocation();
							int[] loc = Utils.unpack2(msg[1]);
							pullTo = new MapLocation(loc[0], loc[1]);
							pullStatus = 2;
//							System.out.println("got pull to");
						} else if (msg[0] % Utils.SCOUT_PULL_FROM == 0 && msg[0] / Utils.SCOUT_PULL_FROM == rc.getID()) {
							lastArchonLoc = s.getLocation();
							int[] loc = Utils.unpack2(msg[1]);
							denLoc = new MapLocation(loc[0], loc[1]);
							pullStatus = 2;
//							System.out.println("got pull from");
						} else if(msg[0] % Utils.SCOUT_SCOUT == 0 && msg[0] / Utils.SCOUT_SCOUT == rc.getID()){
							lastArchonLoc = s.getLocation();
//							System.out.println("got it");
						} else if(msg[0] % Utils.SCOUT_VISION == 0 && msg[0] / Utils.SCOUT_VISION == rc.getID()){
							int[] loc = Utils.unpack2(msg[1]);
							vision = new MapLocation(loc[0], loc[1]);
							pullStatus = -1;
						}
//						break;
					}
				}
			}
			spiralTo = rc.getLocation().add(Direction.EAST, stepSize);
			if(denLoc == null && pullStatus == 2){
				pullStatus = 1;
			}
			if (pullStatus > 0 && denLoc != null) {
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
//			System.out.println(pullStatus);
//			pullTo = new MapLocation(rc.getLocation().x + stepSize, rc.getLocation().y);
			// prevHealth = rc.getHealth();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		while (true) {
			try {
                if(Utils.surrounded(rc))
                	rc.disintegrate();
				AllyInfo allyInfo = new AllyInfo(rc);
                EnemyInfo enemyInfo = new EnemyInfo(rc);
                ZombieInfo zombieInfo = new ZombieInfo(rc);
                RobotInfo bestTarget = null;
                double maxExpected = 0;
                if(enemyInfo.bestTarget != null){
                	bestTarget = enemyInfo.bestTarget;
                	maxExpected = enemyInfo.maxExpected;
                }
                if(zombieInfo.bestTarget != null && (bestTarget == null || zombieInfo.maxExpected > maxExpected)){
                	bestTarget = zombieInfo.bestTarget;
                	maxExpected = enemyInfo.maxExpected;
                }
                if(allyInfo.totalTurrets > 0 && bestTarget != null){
                	rc.broadcastMessageSignal(Utils.TURRET_ATTACK,
                			Utils.pack2(bestTarget.location.x, bestTarget.location.y), 53);
                }
                
				RobotInfo[] nearZombies = rc.senseNearbyRobots(25, Team.ZOMBIE);
				
				MapLocation currLoc = rc.getLocation();

				boolean safe;
				boolean hasMoved = false;
				switch (pullStatus) {
				case 2:
					safe = true;
					for (RobotInfo i : nearZombies) {
						if(!i.type.equals(RobotType.ZOMBIEDEN)){
							safe = false;
							break;
						} 
					}

					if (!safe) {
						// move towards destination, if possible
						Utils.kamikaze(rc, home, pullTo);
					}
					
					// move towards den
					hasMoved = Utils.safeMove(rc, pullFrom, enemyInfo.enemies, zombieInfo.zombies);
					if (currLoc.equals(pullFrom) || enemyInfo.enemies.length >= 5) {
						pullStatus = 1;
					}

					break;
				case 1:
					safe = true;
					for (RobotInfo i : nearZombies) {
						if(!i.type.equals(RobotType.ZOMBIEDEN)){
							safe = false;
							break;
						} 
					}

					if (!safe) {
						// move towards destination, if possible
						Utils.kamikaze(rc, home, pullTo);
					}
					hasMoved = Utils.safeMove(rc, pullTo, enemyInfo.enemies, zombieInfo.zombies);
					if (denLoc != null && zombieInfo.zombies.length == 0 && !currLoc.equals(pullFrom))
						pullStatus = 2;
					if(currLoc.equals(pullTo) && enemyInfo.enemies.length == 0){
						pullTo = currLoc.add(currLoc.directionTo(lastArchonLoc).opposite());
					}
					break;
				case -1:
					if(!currLoc.equals(vision)){
						Utils.straightMove(rc, vision, enemyInfo.enemies, zombieInfo.zombies);
					}
					break;
				default:
					// scout
					if(!rc.onTheMap(currLoc.add(currLoc.directionTo(spiralTo))) || (!hasMoved && currLoc.distanceSquaredTo(spiralTo) <= 53))
						spiralTo = currLoc;
//					System.out.println("im gonna move");
					if (rc.getLocation().equals(spiralTo)) {
						int delta = ((spiralCount + 3) / 2) * stepSize;
						if ((spiralCount / 2) % 2 == 0)
							delta *= -1;
						if (spiralCount % 2 == 0) {
							spiralTo = new MapLocation(spiralTo.x, spiralTo.y + delta);
						} else {
							spiralTo = new MapLocation(spiralTo.x + delta, spiralTo.y);
						}
						spiralCount++;
					}
					
					boolean temp = Utils.safestMove(rc, spiralTo, enemyInfo.enemies, zombieInfo.zombies);
//					System.out.println(temp);

					break;
				}

				int range = Math.min(424, rc.getLocation().distanceSquaredTo(lastArchonLoc));
//				System.out.println(range);

				for (int i = 0; i < zombieInfo.zombies.length && rc.getMessageSignalCount() <= 5; i++) {
					if (zombieInfo.zombies[i].type.equals(RobotType.ZOMBIEDEN)) {
						int pack = Utils.pack2(zombieInfo.zombies[i].location.x, zombieInfo.zombies[i].location.y);
						if(!sentDens.contains(pack)){
							rc.broadcastMessageSignal(Utils.SEE_DEN, pack, rc.getLocation().distanceSquaredTo(lastArchonLoc));
							sentDens.add(pack);
						}
					}
				}

				for (int i = 0; i < enemyInfo.enemies.length && rc.getMessageSignalCount() <= 5; i++) {
					if (enemyInfo.enemies[i].type.equals(RobotType.ARCHON))
						rc.broadcastMessageSignal(Utils.SEE_ARCHON * enemyInfo.enemies[i].ID,
								Utils.pack2(enemyInfo.enemies[i].location.x, enemyInfo.enemies[i].location.y), range);
					if (enemyInfo.enemies[i].type.equals(RobotType.TURRET) || enemyInfo.enemies[i].type.equals(RobotType.TTM))
						rc.broadcastMessageSignal(Utils.SEE_TURRET * enemyInfo.enemies[i].ID,
								Utils.pack2(enemyInfo.enemies[i].location.x, enemyInfo.enemies[i].location.y), range);
				}

//				RobotInfo[] neutrals = rc.senseNearbyRobots(53, Team.NEUTRAL);
//				for (int i = 0; i < neutrals.length && rc.getMessageSignalCount() <= 5; i++) {
//					int loc = Utils.pack2(neutrals[i].location.x, neutrals[i].location.y);
//					if (sentNeutrals.contains(loc))
//						continue;
//					switch (neutrals[i].type) {
//					case ARCHON:
//						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_ARCHON, loc, range);
//						break;
//					case SCOUT:
//						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_SCOUT, loc, range);
//						break;
//					case SOLDIER:
//						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_SOLDIER, loc, range);
//						break;
//					case GUARD:
//						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_GUARD, loc, range);
//						break;
//					case VIPER:
//						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_VIPER, loc, range);
//						break;
//					default:
//						rc.broadcastMessageSignal(Utils.SEE_NEUTRAL_TURRET, loc, range);
//					}
//					sentNeutrals.add(loc);
//				}
//
//				MapLocation[] parts = rc.sensePartLocations(106);
//				for (int i = 0; i < parts.length && rc.getMessageSignalCount() <= 20; i++) {
//					int loc = Utils.pack2(parts[i].x, parts[i].y);
//					if (sentParts.contains(loc))
//						continue;
//					if (rc.senseParts(parts[i]) < 25) // not enough to build
//														// anything
//						rc.broadcastMessageSignal(Utils.SEE_SMALL_PARTS, loc, range);
//					else if (rc.senseParts(parts[i]) >= 130) // enough to build
//																// anything
//						rc.broadcastMessageSignal(Utils.SEE_BIG_PARTS, loc, range);
//					else
//						rc.broadcastMessageSignal(Utils.SEE_MED_PARTS, loc, range);
//				}

				Clock.yield();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
