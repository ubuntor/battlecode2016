package team259.bigturtle;

import battlecode.common.*;

import java.util.*;

/**
 * Created by allen on 1/19/16.
 */

/*
Ideal turtling setups:
1 Archon: 8 soldiers, 7 turrets (1 space for more production)
S S S
 T T
STATS
 TTT
S S S

2 Archon: 9 soldiers, 9 turrets
S S S
 TTTTS
STAAT
 TT TS
S S S

3 Archon: 10 soldiers, 10 turrets
S S S
 T TTS
STAAT
 TATTS
STT 
 S S S
 
4 Archon: 10 soldiers, 10 turrets 
S S S
 T TTS
STAAT
 TAATS
STT T
 S S S

 */

/*
ST TS
TT TT
TTATT
TTTTT
STTTS

STTTTS
TTTTTT
TTAATT
TTT TT
STT TS

ST TTS
TT TTT
TTAATT
TTATTT
TTT TT
STT TS

ST TTS
TT TTT
TTAATT
TTAATT
TTT TT
STT TS

 */

public class Archon {
    public static void run(RobotController rc) {
//        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
//                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        Random rand = new Random(rc.getID());
        int status = 0; //0 = undetermined, 1 = move to turtlespot, 2 = turtling
        Team myTeam = null;
        Team otherTeam = null;
        int numArchons = 1;
        int archonID = -1;
        Map<Integer, MapLocation> enemyLocs = new HashMap<Integer, MapLocation>();
        Map<Integer, MapLocation> scoutsBuilt = new HashMap<Integer, MapLocation>();
        Set<MapLocation> denLocs = null;
        MapLocation tlArchon = null;
        MapLocation moveTo = null;
        boolean soldierNext = true;
        boolean hasMoved = false;
        boolean receivedHome = false;
        try {
        	myTeam = rc.getTeam();
			otherTeam = myTeam.opponent();
        	MapLocation[] allied = rc.getInitialArchonLocations(myTeam);
        	MapLocation[] enemies = rc.getInitialArchonLocations(otherTeam);
        	numArchons = allied.length;
        	int farthest = -1;
        	int maxMinDist = 0;
        	for(int i = 0; i < numArchons; i++){
        		int minDist = Integer.MAX_VALUE;
        		for(MapLocation loc : enemies){
        			if(loc.distanceSquaredTo(allied[i]) < minDist)
        				minDist = loc.distanceSquaredTo(allied[i]);
        		}
        		if(minDist > maxMinDist){
        			maxMinDist = minDist;
        			farthest = i;
        		}
        	}
        	for(int i = 0; i < numArchons; i++){
        		if(rc.getLocation().equals(allied[i])){
        			archonID = (i - farthest + numArchons) % numArchons;
        			break;
        		}
        	}
        	
        	for(int i = 0; i < enemies.length; i++){
        		enemyLocs.put(-i - 1, enemies[i]);
        	}
            
            if(archonID < 2)
            	sendScout(rc);

        	tlArchon = allied[farthest];      
            if(archonID > 0){
            	status = 1;
            	switch(archonID){
            	case 1:
            		moveTo = tlArchon.add(Direction.EAST);
            		break;
            	case 2:
            		moveTo = tlArchon.add(Direction.SOUTH);
            		break;
            	case 3:
            		moveTo = tlArchon.add(Direction.SOUTH_EAST);
            		break;
            	default:
            		moveTo = tlArchon;
            	}
            } else {
            	moveTo = tlArchon;
            }
            DistanceComparator.base = tlArchon;
//            System.out.println("Top left archon: " + tlArchon + ", moving to " + moveTo);
            denLocs = new TreeSet<MapLocation>(new DistanceComparator());
        	
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                MapLocation currLoc = rc.getLocation();
                
                activateNeutrals(rc);
                
                AllyInfo allyInfo = new AllyInfo(rc);
                EnemyInfo enemyInfo = new EnemyInfo(rc);
                ZombieInfo zombieInfo = new ZombieInfo(rc);
                double enemyDPS = enemyInfo.dps + zombieInfo.dps;
                double enemyHealth = enemyInfo.health + zombieInfo.health;
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
                                
				if (status == 0) {
					if (enoughSpace(rc, numArchons, currLoc)) {
						status = 2;
						tlArchon = rc.getLocation();
						rc.broadcastMessageSignal(Utils.ARCHON_FINAL_HOME, 0, 70);
					}
				}
				if (archonID == 0) {
					if (status == 2) {
						if (allyInfo.nearArchons < numArchons - 1) {
							rc.broadcastMessageSignal(Utils.ARCHON_FINAL_HOME, 0, 70);
						} else {
							RobotInfo r2 = rc.senseRobotAtLocation(rc.getLocation().add(Direction.EAST));
							RobotInfo r3 = rc.senseRobotAtLocation(rc.getLocation().add(Direction.SOUTH));
							RobotInfo r4 = rc.senseRobotAtLocation(rc.getLocation().add(Direction.SOUTH_EAST));
							if (numArchons >= 2 && (r2 == null || !r2.type.equals(RobotType.ARCHON)))
								rc.broadcastMessageSignal(Utils.ARCHON_FINAL_HOME, 0, 70);
							else if (numArchons >= 3 && (r3 == null || !r3.type.equals(RobotType.ARCHON)))
								rc.broadcastMessageSignal(Utils.ARCHON_FINAL_HOME, 0, 70);
							else if (numArchons >= 4 && (r4 == null || !r4.type.equals(RobotType.ARCHON)))
								rc.broadcastMessageSignal(Utils.ARCHON_FINAL_HOME, 0, 70);
						}
					} else {
						rc.broadcastMessageSignal(Utils.ARCHON_TEMP_HOME, 0, 70);
					}
				}
				
                ArrayList<MapLocation> scoutCovered = new ArrayList<MapLocation>();
                for(int i : scoutsBuilt.keySet()){
                	scoutCovered.add(scoutsBuilt.get(i));
                }
                Signal[] sigs = rc.emptySignalQueue();
                for (Signal s : sigs) {
                	if (s.getTeam() == rc.getTeam()) {
						int[] msg = s.getMessage();
						if (msg != null) {
							if (msg[0] == Utils.ARCHON_TEMP_HOME || msg[0] == Utils.ARCHON_FINAL_HOME) {
				            	tlArchon = s.getLocation();
				            	switch(archonID){
				            	case 1:
				            		moveTo = tlArchon.add(Direction.EAST);
				            		break;
				            	case 2:
				            		moveTo = tlArchon.add(Direction.SOUTH);
				            		break;
				            	case 3:
				            		moveTo = tlArchon.add(Direction.SOUTH_EAST);
				            		break;
				            	default:
				            		;
				            	}
				            	receivedHome = msg[0] == Utils.ARCHON_FINAL_HOME;
							} else if(msg[0] % Utils.SEE_DEN == 0){
								int[] den = Utils.unpack2(msg[1]);
								denLocs.add(new MapLocation(den[0], den[1]));
							} else if(msg[0] % Utils.SEE_ARCHON == 0){
								int[] arch = Utils.unpack2(msg[1]);
								enemyLocs.put(msg[0] / Utils.SEE_ARCHON, new MapLocation(arch[0], arch[1]));
							} else if(msg[0] % Utils.SEE_TURRET == 0){
								int[] turret = Utils.unpack2(msg[1]);
								enemyLocs.put(msg[0] / Utils.SEE_TURRET, new MapLocation(turret[0], turret[1]));
							} else if(msg[0] == Utils.ARCHON_COVERED){
								int[] scout = Utils.unpack2(msg[1]);
								scoutCovered.add(new MapLocation(scout[0], scout[1]));
//								System.out.println("know not to build scout at " + scoutCovered);
							}
						}
					}
                }
                
            	//if enemies kill me before i kill them, run

            	if(status == 1){
            		Utils.straightMove(rc, moveTo, enemyInfo.enemies, zombieInfo.zombies);
            		if(rc.getLocation().equals(moveTo) && receivedHome){
            			status = 2;
            			System.out.println("Goal reached! " + rc.getLocation() + " = " + moveTo);
            		}
                } else if(status == 2){
					
                	if (bestTarget != null) {
						rc.broadcastMessageSignal(Utils.TURRET_ATTACK, Utils.pack2(bestTarget.location.x, bestTarget.location.y), 18);
						rc.broadcastMessageSignal(Utils.SOLDIER_ATTACK, Utils.pack2(bestTarget.location.x, bestTarget.location.y), 18);
					}
					if((allyInfo.allyDPS <= 0 && enemyDPS > 0) || (enemyDPS > 0 && rc.getHealth() / enemyDPS < enemyHealth / allyInfo.allyDPS)){
						Direction opDir = Direction.NORTH;
						if(enemyInfo.nearest >= 0)
							opDir = enemyInfo.enemies[enemyInfo.nearest].location.directionTo(currLoc);
						Utils.safestMove(rc, currLoc.add(opDir), enemyInfo.enemies, zombieInfo.zombies);
						rc.broadcastSignal(70);
					} else {
						if(archonID > 0 && !currLoc.equals(moveTo)){
							status = 1;
							if(Utils.straightMove(rc, moveTo, enemyInfo.enemies, zombieInfo.zombies) && rc.getLocation().equals(moveTo))
		            			status = 2;
						}
						
						// build turtle setup, alternating soldiers then turrets
						boolean enoughTurrets = enoughTurrets(numArchons, allyInfo);
						boolean enoughTotalTurrets = enoughTotalTurrets(numArchons, allyInfo);
						boolean enoughSoldiers = enoughSoldiers(numArchons, allyInfo);

						if (!enoughSoldiers) {
							if (rc.hasBuildRequirements(RobotType.SOLDIER) && rc.isCoreReady()) {
								boolean built = buildSoldier(rc, archonID, numArchons, tlArchon);
								if (!built) {
									clearRubble(rc, currLoc);
								}
								if (built && !enoughTurrets)
									soldierNext = false;
							}
						}
						if (enoughSoldiers) {
							// System.out.println("scout?");
							// System.out.println("i remember to not build at "
							// + scoutCovered);
							if (rc.hasBuildRequirements(RobotType.SCOUT) && rc.isCoreReady()) {
								MapLocation built = buildVisionScout(rc, numArchons, tlArchon, scoutCovered);
								if (built != null) {
									scoutsBuilt.put(rc.getRoundNum(), built);
								}
								// System.out.println("scout!");
							}
						}
						if (!enoughTurrets) {
							// System.out.println("test!");
							if (rc.hasBuildRequirements(RobotType.TURRET) && rc.isCoreReady() && rc.getRoundNum() % numArchons == archonID) {
								boolean built = buildTurret(rc, archonID, numArchons, rand);
								if (!built) {
									clearRubble(rc, currLoc);
								} else {
									rc.broadcastMessageSignal(Utils.TURRET_BUILT, Utils.pack2(tlArchon.x, tlArchon.y),
											20);
								}
								if (built && !enoughSoldiers)
									soldierNext = true;
							}
						}
						if (enoughSoldiers && enoughTotalTurrets) {
							// System.out.println("pulling");
							if (allyInfo.mostHurt < 0 && rc.isCoreReady()) {
								clearRubble(rc, currLoc);
							}
//							if (allyInfo.mostHurt < 0 && rc.hasBuildRequirements(RobotType.SCOUT) && rc.isCoreReady()) {
//								scoutPull(rc, archonID, numArchons, tlArchon, enemyLocs, denLocs);
//							}
							if (allyInfo.mostHurt < 0 && rc.hasBuildRequirements(RobotType.GUARD) && rc.isCoreReady()) {
								buildGuard(rc, archonID, numArchons, tlArchon);
							}

						}
						if (allyInfo.mostHurt < 0 && rc.isCoreReady()) {
							clearRubble(rc, currLoc);
						}

						Iterator<Integer> it = scoutsBuilt.keySet().iterator();
						while (it.hasNext()) {
							Integer key = it.next();
							if (rc.getRoundNum() - key > 80)
								it.remove();
							else {
								rc.broadcastMessageSignal(Utils.ARCHON_COVERED,
										Utils.pack2(scoutsBuilt.get(key).x, scoutsBuilt.get(key).y), 20);
							}
						}

					}
					if (allyInfo.mostHurt >= 0 && rc.isCoreReady())
						rc.repair(allyInfo.allies[allyInfo.mostHurt].location);
				}
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

	private static void sendScout(RobotController rc) throws Exception{
    	Direction dirToBuild = Direction.NORTH;
        for (int i = 0; i < 8; i++) {
            // If possible, build in this direction
            if (rc.hasBuildRequirements(RobotType.SCOUT) && rc.canBuild(dirToBuild, RobotType.SCOUT)) {
                rc.build(dirToBuild, RobotType.SCOUT);
                break;
            } else {
//                 Rotate the direction to try
                dirToBuild = dirToBuild.rotateRight();
            }
        }
        rc.broadcastMessageSignal(rc.senseRobotAtLocation(rc.getLocation().add(dirToBuild)).ID * Utils.SCOUT_SCOUT, 0, 20);
    }
    
    private static void activateNeutrals(RobotController rc) throws Exception{
    	
    	RobotInfo[] neutrals = rc.senseNearbyRobots(2, Team.NEUTRAL);
		RobotInfo bestToActivate = null;
		int prio = -1;
    	for(RobotInfo r : neutrals){
    		if(!rc.isCoreReady())
    			break;
    		if(r.type.equals(RobotType.ARCHON))
    			rc.activate(r.location);
    		else if(r.type.equals(RobotType.TURRET) || r.type.equals(RobotType.TTM)){
    			bestToActivate = r;
    			prio = 5;
    		} else if(r.type.equals(RobotType.VIPER) && prio < 4){
    			bestToActivate = r;
    			prio = 4;
    		} else if(r.type.equals(RobotType.SOLDIER) && prio < 3){
    			bestToActivate = r;
    			prio = 3;
    		} else if(r.type.equals(RobotType.GUARD) && prio < 2){
    			bestToActivate = r;
    			prio = 2;
    		} else if(prio < 1){
    			bestToActivate = r;
    			prio = 1;
    		}
    	}
    	if(bestToActivate != null && rc.isCoreReady()){
    		rc.activate(bestToActivate.location);
    		rc.broadcastMessageSignal(Utils.ACTIVATED_NEUTRAL,
    				rc.senseRobotAtLocation(bestToActivate.location).ID, 20);
    	}
    	
    }
    
    private static boolean enoughSpace(RobotController rc, int numArchons, MapLocation currLoc) throws Exception{
    	
    	boolean nClear = rc.onTheMap(currLoc.add(Direction.NORTH, 4));
    	boolean eClear = rc.onTheMap(currLoc.add(Direction.EAST, 4));
    	if(eClear && numArchons > 1)
    		eClear = rc.onTheMap(currLoc.add(Direction.EAST, 5));
    	boolean sClear = rc.onTheMap(currLoc.add(Direction.SOUTH, 4));
    	if(sClear && numArchons > 2)
    		sClear = rc.onTheMap(currLoc.add(Direction.SOUTH, 5));
    	boolean wClear = rc.onTheMap(currLoc.add(Direction.WEST, 4));
//    	System.out.format("North: %b, East, %b, South: %b, West: %b\n", nClear, eClear, sClear, wClear);
    	Direction toMove = null;
    	if(!nClear){
    		if(!eClear){
    			toMove = Direction.SOUTH_WEST;
    		} else if(!wClear){
    			toMove = Direction.SOUTH_EAST;
    		} else {
    			toMove = Direction.SOUTH;
    		}
    	} else if(!sClear){
    		if(!eClear){
    			toMove = Direction.NORTH_WEST;
    		} else if(!wClear){
    			toMove = Direction.NORTH_EAST;
    		} else {
    			toMove = Direction.NORTH;
    		}
    	} else if (!eClear){
    		toMove = Direction.WEST;
		} else if (!wClear && rc.isCoreReady() && rc.canMove(Direction.EAST)) {
			toMove = Direction.EAST;
		} else if (nClear && eClear && sClear && wClear) {
//    		rc.broadcastMessageSignal(Utils.ARCHON_FINAL_HOME, 0, 70);
    		return true;
    	}
    	if(rc.isCoreReady() && toMove != null){
    		if(rc.canMove(toMove)){
    			rc.move(toMove);
    		} else if(rc.canMove(toMove.rotateLeft())){
    			rc.move(toMove.rotateLeft());
    		} else if(rc.canMove(toMove.rotateRight())){
    			rc.move(toMove.rotateRight());
    		}
    	}
//    	rc.broadcastMessageSignal(Utils.ARCHON_TEMP_HOME, 0, 70);
    	return false;
    }
    
    
	private static boolean enoughTurrets(int numArchons, AllyInfo allyInfo) throws Exception {
		return (numArchons == 1 && allyInfo.turretCount >= 7) || (numArchons == 2 && allyInfo.turretCount >= 6)
				|| (numArchons == 3 && allyInfo.turretCount >= 5) || (numArchons == 4 && allyInfo.turretCount >= 4);
	}
	
	private static boolean enoughTotalTurrets(int numArchons, AllyInfo allyInfo) throws Exception {
		boolean ans = (numArchons == 1 && allyInfo.totalTurrets >= 18) || (numArchons == 2 && allyInfo.totalTurrets>= 22)
				|| (numArchons == 3 && allyInfo.totalTurrets >= 25) || (numArchons == 4 && allyInfo.totalTurrets >= 24);
		if(allyInfo.nearArchons == 0)
			ans = enoughTurrets(numArchons, allyInfo);
		else if(allyInfo.nearArchons == 1 && numArchons == 3)
			ans = allyInfo.totalTurrets >= 7;
		else if (allyInfo.nearArchons == 1 && numArchons == 4)
			ans = allyInfo.totalTurrets >= 6;
		else if (allyInfo.nearArchons == 2 && numArchons == 4)
			ans = allyInfo.totalTurrets >= 8;
		return ans;
	}
	
	private static boolean enoughSoldiers(int numArchons, AllyInfo allyInfo) throws Exception {
		return (numArchons == 1 && allyInfo.soldierCount >= 14) || (numArchons == 2 && allyInfo.soldierCount >= 15)
				|| (numArchons >= 3 && allyInfo.soldierCount >= 17);
	}
	
	private static boolean buildSoldier(RobotController rc, int archonID, int numArchons, MapLocation tlArchon) throws Exception{ 
		
		boolean hasBuilt = false;
		Direction dirToBuild = Direction.NORTH;
		if(archonID == 0 && numArchons == 2)
			dirToBuild = Direction.SOUTH_EAST;
		else if(archonID == 1 && numArchons == 2)
			dirToBuild = Direction.SOUTH;
		else if(archonID == 1 && numArchons == 3)
			dirToBuild = Direction.NORTH_WEST;
		else if(archonID == 2 && numArchons == 3)
			dirToBuild = Direction.SOUTH_EAST;
		else if(archonID == 1 && numArchons == 4)
			dirToBuild = Direction.NORTH_WEST;
		else if(archonID == 2 && numArchons == 4)
			dirToBuild = Direction.SOUTH_EAST;
		else if(archonID == 3 && numArchons == 4)
			dirToBuild = Direction.SOUTH;
        for (int i = 0; i < 8; i++) {
            if (rc.canBuild(dirToBuild, RobotType.SOLDIER)) {
                rc.build(dirToBuild, RobotType.SOLDIER);
                hasBuilt = true;
                break;
            } else {
                // Rotate the direction to try
                dirToBuild = dirToBuild.rotateRight();
            }
        }
        if(hasBuilt){
        	rc.broadcastMessageSignal(Utils.SOLDIER_PATROL, Utils.pack2(tlArchon.x, tlArchon.y), 20);
        }
        return hasBuilt;
		
	}
	
	private static MapLocation buildVisionScout(RobotController rc, int numArchons, MapLocation tlArchon, ArrayList<MapLocation> covered) throws Exception {
		
		MapLocation[] scoutLocs = new MapLocation[]{tlArchon.add(Direction.NORTH_WEST, 2),
				tlArchon.add(Direction.NORTH_EAST, 2), tlArchon.add(Direction.SOUTH_EAST, 2), tlArchon.add(Direction.SOUTH_WEST, 2)};
		if(numArchons >= 2){
			scoutLocs[1] = scoutLocs[1].add(Direction.EAST);
			scoutLocs[2] = scoutLocs[2].add(Direction.EAST);
		}
		if(numArchons >= 3){
			scoutLocs[2] = scoutLocs[2].add(Direction.SOUTH);
			scoutLocs[3] = scoutLocs[3].add(Direction.SOUTH);
		}
		boolean built = false;
//		System.out.println("im not building at " + covered);
		for(MapLocation loc : scoutLocs){
			if(rc.getLocation().distanceSquaredTo(loc) > rc.getType().sensorRadiusSquared)
				continue;
			if((rc.senseRobotAtLocation(loc) == null || !rc.senseRobotAtLocation(loc).type.equals(RobotType.SCOUT)) && !covered.contains(loc)){
				Direction dirToBuild = Direction.NORTH;
		        for (int i = 0; i < 8; i++) {
		            // If possible, build in this direction
		            if (rc.hasBuildRequirements(RobotType.SCOUT) && rc.canBuild(dirToBuild, RobotType.SCOUT)) {
		                rc.build(dirToBuild, RobotType.SCOUT);
		                built = true;
		                break;
		            } else {
		                // Rotate the direction to try
		                dirToBuild = dirToBuild.rotateRight();
		            }
		        }
		        if(built){
		        	rc.broadcastMessageSignal(rc.senseRobotAtLocation(rc.getLocation().add(dirToBuild)).ID * Utils.SCOUT_VISION,
		        		Utils.pack2(loc.x, loc.y), 20);
		        	return loc;
		        }
			}
		}
		return null;
		
	}
	
	private static boolean buildTurret(RobotController rc, int archonID, int numArchons, Random rand) throws Exception{
		Direction dirToBuild = null;
		if(numArchons == 1){
			for(int i = 0; i < 100 && (dirToBuild == null || !rc.canBuild(dirToBuild, RobotType.TURRET)); i++){
				int r = rand.nextInt(7) + 1;
				dirToBuild = Utils.DIRS[r];
			}
		} else if(numArchons == 2){
			for(int i = 0; i < 100 && (dirToBuild == null || !rc.canBuild(dirToBuild, RobotType.TURRET)); i++){
				int r = rand.nextInt(6);
				if(r > 1 && archonID == 0)
					r += 2;
				else if(r > 3 && archonID == 1)
					r = 2 * r - 3;
				dirToBuild = Utils.DIRS[r];
			}
		} else if(numArchons == 3){
			for(int i = 0; i < 100 && (dirToBuild == null || !rc.canBuild(dirToBuild, RobotType.TURRET)); i++){
				int r = rand.nextInt(5);
				if(archonID == 0){
					if(r > 1)
						r += 3;
					else
						r = 2 * r + 1;
				} else if(archonID == 2){ //ID 1 is no problem
					r += 3;
					if(r == 3)
						r--;
				}
				dirToBuild = Utils.DIRS[r];
			}
		} else {
			for(int i = 0; i < 100 && (dirToBuild == null || !rc.canBuild(dirToBuild, RobotType.TURRET)); i++){
				int r = rand.nextInt(4);
				if(archonID == 0){
					r += 4;
					if(r == 4)
						r = 1;
				} else if(archonID == 3){ //ID 1 is no problem
					r += 1;
					if(r == 4)
						r = 5;
				} else if(archonID == 2){
					r += 4;
				}
//				System.out.println(r);
				dirToBuild = Utils.DIRS[r];
			}
		}
		if(rc.canBuild(dirToBuild, RobotType.TURRET)){
			rc.build(dirToBuild, RobotType.TURRET);
			return true;
		}
		return false;
	}
	
	private static void clearRubble(RobotController rc, MapLocation currLoc) throws Exception{
		
		Direction clearDir = null;
		double minRubble = Double.MAX_VALUE;
		for(Direction dir : Utils.DIRS){
			 double rubble = rc.senseRubble(currLoc.add(dir));
			 if(rubble > 0 && rubble < minRubble)
				 clearDir = dir;
		}
		if(clearDir != null)
			rc.clearRubble(clearDir);
		
	}
	
	private static boolean buildGuard(RobotController rc, int archonID, int numArchons, MapLocation tlArchon) throws Exception{ 
		
		boolean hasBuilt = false;
		Direction dirToBuild = Direction.NORTH;
		if(archonID == 0 && numArchons == 2)
			dirToBuild = Direction.SOUTH_EAST;
		else if(archonID == 1 && numArchons == 2)
			dirToBuild = Direction.SOUTH;
		else if(archonID == 1 && numArchons == 3)
			dirToBuild = Direction.NORTH_WEST;
		else if(archonID == 2 && numArchons == 3)
			dirToBuild = Direction.SOUTH_EAST;
		else if(archonID == 1 && numArchons == 4)
			dirToBuild = Direction.NORTH_WEST;
		else if(archonID == 2 && numArchons == 4)
			dirToBuild = Direction.SOUTH_EAST;
		else if(archonID == 3 && numArchons == 4)
			dirToBuild = Direction.SOUTH;
        for (int i = 0; i < 8; i++) {
            if (rc.canBuild(dirToBuild, RobotType.GUARD)) {
                rc.build(dirToBuild, RobotType.GUARD);
                hasBuilt = true;
                break;
            } else {
                // Rotate the direction to try
                dirToBuild = dirToBuild.rotateRight();
            }
        }
        if(hasBuilt){
        	rc.broadcastMessageSignal(Utils.GUARD_PATROL, Utils.pack2(tlArchon.x, tlArchon.y), 20);
        }
        return hasBuilt;
		
	}
	
	private static void scoutPull(RobotController rc, int archonID, int numArchons, MapLocation tlArchon,
			Map<Integer, MapLocation> enemyLocs, Set<MapLocation> denLocs) throws Exception {

		boolean hasBuilt = false;
		Direction dirToBuild = Direction.NORTH;
		if (archonID == 0 && numArchons == 2)
			dirToBuild = Direction.SOUTH_EAST;
		else if (archonID == 1 && numArchons == 2)
			dirToBuild = Direction.SOUTH;
		else if (archonID == 1 && numArchons == 3)
			dirToBuild = Direction.NORTH_WEST;
		else if (archonID == 2 && numArchons == 3)
			dirToBuild = Direction.SOUTH_EAST;
		else if (archonID == 1 && numArchons == 4)
			dirToBuild = Direction.NORTH_WEST;
		else if (archonID == 2 && numArchons == 4)
			dirToBuild = Direction.SOUTH_EAST;
		else if (archonID == 3 && numArchons == 4)
			dirToBuild = Direction.SOUTH;
		for (int i = 0; i < 8; i++) {
			if (rc.canBuild(dirToBuild, RobotType.SCOUT)) {
				rc.build(dirToBuild, RobotType.SCOUT);
				hasBuilt = true;
				break;
			} else {
				// Rotate the direction to try
				dirToBuild = dirToBuild.rotateRight();
			}
		}
		if (hasBuilt) {
			long xMean = 0;
			long yMean = 0;
			Set<Integer> keySet = enemyLocs.keySet();
			for (int i : keySet) {
				xMean += enemyLocs.get(i).x;
				yMean += enemyLocs.get(i).y;
			}
			MapLocation pullTo = new MapLocation((int) (xMean / keySet.size()), (int) (yMean / keySet.size()));
			MapLocation pullFrom = null;
			for (MapLocation m : denLocs) {
				int num = tlArchon.x * (pullTo.y - m.y) - tlArchon.y * (pullTo.x - m.x) + pullTo.x * m.y
						- pullTo.y * m.x;
				int dist = num * num / ((m.y - pullTo.y) * (m.y - pullTo.y) + (m.x - pullTo.x) * (m.x - pullTo.x));
				int dot = (pullTo.x - tlArchon.x) * (m.x - tlArchon.x) + (pullTo.y - tlArchon.y) * (m.y - tlArchon.y);
				// System.out.println("Distance to line: " + dist + ", dot
				// product: " + dot);
				if (dist > 50 || dot > 0) {
					pullFrom = m;
					break;
				}
			}
			rc.broadcastMessageSignal(
					rc.senseRobotAtLocation(rc.getLocation().add(dirToBuild)).ID * Utils.SCOUT_PULL_TO,
					Utils.pack2(pullTo.x, pullTo.y), 20);
			if (pullFrom != null) {
				rc.broadcastMessageSignal(
						rc.senseRobotAtLocation(rc.getLocation().add(dirToBuild)).ID * Utils.SCOUT_PULL_FROM,
						Utils.pack2(pullFrom.x, pullFrom.y), 20);
				// System.out.println("Message: [" +
				// rc.senseRobotAtLocation(rc.getLocation().add(dirToBuild)).ID
				// * Utils.SCOUT_PULL_FROM + ", " + Utils.pack2(pullFrom.x,
				// pullFrom.y) + "]");
				// System.out.println("sent den at " + pullFrom);
			}

		}
		
	}
}

class DistanceComparator implements Comparator<MapLocation>{
	
	static MapLocation base;
	public int compare(MapLocation a, MapLocation b){
		return a.distanceSquaredTo(base) - b.distanceSquaredTo(base);
	}
	
}