package turtle;

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
        Map<Integer, MapLocation> enemyArchons = new HashMap<Integer, MapLocation>();
        MapLocation tlArchon = null;
        MapLocation moveTo = null;
        boolean soldierNext = true;
        boolean hasMoved = false;
        try {
        	myTeam = rc.getTeam();
			otherTeam = myTeam.opponent();
        	MapLocation[] allied = rc.getInitialArchonLocations(myTeam);
        	numArchons = allied.length;
        	for(int i = 0; i < numArchons; i++){
        		if(rc.getLocation().equals(allied[i])){
        			archonID = i;
        			break;
        		}
        	}
        	MapLocation[] enemies = rc.getInitialArchonLocations(otherTeam);
        	for(int i = 0; i < enemies.length; i++){
        		enemyArchons.put(-i - 1, enemies[i]);
        	}
//            Direction dirToBuild = Direction.NORTH;
//            for (int i = 0; archonID < 2 && i < 8; i++) {
//                // If possible, build in this direction
//                if (rc.hasBuildRequirements(RobotType.SCOUT) && rc.canBuild(dirToBuild, RobotType.SCOUT)) {
//                    rc.build(dirToBuild, RobotType.SCOUT);
//                    break;
//                } else {
//                    // Rotate the direction to try
//                    dirToBuild = dirToBuild.rotateRight();
//                }
//            }
//        	rc.broadcastMessageSignal(Utils.SCOUT_SCOUT, 0, 2);

        	tlArchon = allied[0];      
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
            }
//            System.out.println(tlArchon);
            //int ID = rc.getID();
            //MapLocation loc = home.add(Direction.NORTH);
            //RobotInfo scoutInfo = rc.senseRobotAtLocation(loc);
            //int scoutID = scoutInfo.ID;
            // Any code here gets executed exactly once at the beginning of the game.
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                RobotInfo[] enemies = rc.senseNearbyRobots(35, otherTeam);
                RobotInfo[] zombies = rc.senseNearbyRobots(35, Team.ZOMBIE);
                RobotInfo[] allies = rc.senseNearbyRobots(35, myTeam);
                MapLocation currLoc = rc.getLocation();

				//neutrals
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
            	if(bestToActivate != null && rc.isCoreReady())
            		rc.activate(bestToActivate.location);

				//healing
                int mostHurt = -1;
                int nearArchons = 0;
                int soldierCount = 0;
                int turretCount = 0;
                double allyDPS = 0;
                for(int i = 0; i < allies.length; i++){
					switch (allies[i].type) {
					case SOLDIER:
						allyDPS += 2;
						soldierCount++;
						break;
					case GUARD:
						allyDPS += 1.5;
						break;
					case VIPER:
						allyDPS += 2 + 2.0 / 3;
						break;
					case TURRET:
						allyDPS += 13.0 / 3;
						if(allies[i].location.distanceSquaredTo(currLoc) <= 2)
							turretCount++;
						break;
					default:
						;
					}
                	if(allies[i].ID == rc.getID() || allies[i].location.distanceSquaredTo(currLoc) > 24)
                		continue;
                	if(allies[i].type.equals(RobotType.ARCHON) && allies[i].location.distanceSquaredTo(currLoc) <= 2)
                		nearArchons++;
                	if(allies[i].type != RobotType.ARCHON && allies[i].health < allies[i].maxHealth &&
                			(mostHurt == -1 || allies[i].health < allies[mostHurt].health))
                		mostHurt = i;
                }
                if(mostHurt >= 0 && rc.isCoreReady())
                	rc.repair(allies[mostHurt].location);
                
                if(status == 0){
                	hasMoved = true;
                	boolean nClear = rc.onTheMap(currLoc.add(Direction.NORTH, 2));
                	boolean eClear = rc.onTheMap(currLoc.add(Direction.EAST, 2));
                	if(eClear && numArchons > 1)
                		eClear = rc.onTheMap(currLoc.add(Direction.EAST, 3));
                	boolean sClear = rc.onTheMap(currLoc.add(Direction.SOUTH, 2));
                	if(sClear && numArchons > 2)
                		sClear = rc.onTheMap(currLoc.add(Direction.SOUTH, 3));
                	boolean wClear = rc.onTheMap(currLoc.add(Direction.WEST, 2));
                	if(!nClear && rc.isCoreReady()){
                		if(!eClear){
                			if(rc.canMove(Direction.SOUTH_WEST))
                				rc.move(Direction.SOUTH_WEST);
                		} else if(!wClear){
                			if(rc.canMove(Direction.SOUTH_EAST))
                				rc.move(Direction.SOUTH_EAST);
                		} else {
                			if(rc.canMove(Direction.SOUTH))
                				rc.move(Direction.SOUTH);
                		}
                	}
                	else if(!sClear && rc.isCoreReady()){
                		if(!eClear){
                			if(rc.canMove(Direction.SOUTH_WEST))
                				rc.move(Direction.SOUTH_WEST);
                		} else if(!wClear){
                			if(rc.canMove(Direction.SOUTH_EAST))
                				rc.move(Direction.SOUTH_EAST);
                		} else {
                			if(rc.canMove(Direction.SOUTH))
                				rc.move(Direction.SOUTH);
                		}
                	} else if (!eClear && rc.isCoreReady() && rc.canMove(Direction.WEST))
                		rc.move(Direction.WEST);
                	else if(!wClear && rc.isCoreReady() && rc.canMove(Direction.EAST))
                		rc.move(Direction.EAST);
                	else if(nClear && eClear && sClear && wClear){
                		status = 2;
                		tlArchon = rc.getLocation();
                		hasMoved = false;
                	}
                } 
                
              //todo: add scout signals
                Signal[] sigs = rc.emptySignalQueue();
                for (Signal s : sigs) {
					if (s.getTeam() == rc.getTeam()) {
						int[] msg = s.getMessage();
						if (msg != null) {
							if (msg[0] == Utils.ARCHON_HOME) {
								status = 1;
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
							}
							break;
						}
					}
                }
                
            	if(status == 1){
            		if(Utils.safestMove(rc, moveTo, enemies, zombies) && rc.getLocation().equals(moveTo)){
            			status = 0;
            		}
                } else {
                	//if enemies kill me before i kill them, run
					if(hasMoved && nearArchons < numArchons - 1){
						rc.broadcastMessageSignal(Utils.ARCHON_HOME, 0, 70);
					}
                	
                	double enemyDPS = 0;
					double enemyHealth = 0;
					int nearestEnemy = -1;
					RobotInfo bestTarget = null;
					double maxExpected = 0;
					for(int i = 0; i < enemies.length; i++){
						int dist = currLoc.distanceSquaredTo(enemies[i].location);
						double tempExpected = 0;
						if(enemies[i].type.equals(RobotType.ARCHON)){
							tempExpected = 100000000;
						} else if (enemies[i].type.equals(RobotType.SOLDIER) && dist <= 13){
							tempExpected += 2 / enemies[i].health;
							enemyDPS += 2;
						} else if (enemies[i].type.equals(RobotType.GUARD) && dist <= 2){
							tempExpected += 1.5 / enemies[i].health;
							enemyDPS += 1.5;
						} else if (enemies[i].type.equals(RobotType.VIPER) && dist <= 20){
							tempExpected += (8.0/3) / enemies[i].health;
							enemyDPS += 2 + 2.0/3;
						} else if ((enemies[i].type.equals(RobotType.TURRET) || enemies[i].type.equals(RobotType.TTM)) && dist <= 48){
							tempExpected += (13.0/3) / enemies[i].health;
							enemyDPS += 13.0/3;
						} else if(enemies[i].type.equals(RobotType.SCOUT)){
							tempExpected += 0.01;
						}
						if(nearestEnemy < 0 || dist < currLoc.distanceSquaredTo(enemies[nearestEnemy].location))
							nearestEnemy = i;
						if(tempExpected > maxExpected || bestTarget == null ||
								(tempExpected == maxExpected && dist > bestTarget.location.distanceSquaredTo(currLoc))){
							maxExpected = tempExpected;
							bestTarget = enemies[i];
						}
					}
					for(int i = 0; i < zombies.length; i++){
						int dist = currLoc.distanceSquaredTo(zombies[i].location);
						double tempExpected = 0;
						if (zombies[i].type.equals(RobotType.RANGEDZOMBIE) && dist <= 13){
							tempExpected += 3 / zombies[i].health;
							enemyDPS += 3;
						} else if (zombies[i].type.equals(RobotType.STANDARDZOMBIE) && dist <= 2){
							tempExpected += 1.25 / zombies[i].health;
							enemyDPS += 1.25;
						} else if (zombies[i].type.equals(RobotType.FASTZOMBIE) && dist <= 2){
							tempExpected += 3 / zombies[i].health;
							enemyDPS += 3;
						} else if (zombies[i].type.equals(RobotType.BIGZOMBIE) && dist <= 2){
							tempExpected += (25.0/3) / zombies[i].health;
							enemyDPS += 25.0/3;
						}
						if(tempExpected > maxExpected || bestTarget == null ||
								(tempExpected == maxExpected && dist > bestTarget.location.distanceSquaredTo(currLoc))){
							maxExpected = tempExpected;
							bestTarget = zombies[i];
						}
					}

					if (bestTarget != null) {
						rc.broadcastMessageSignal(Utils.TURRET_ATTACK, Utils.pack2(bestTarget.location.x, bestTarget.location.y), 18);
						rc.broadcastMessageSignal(Utils.SOLDIER_ATTACK, Utils.pack2(bestTarget.location.x, bestTarget.location.y), 18);
					}
					if((allyDPS <= 0 && enemyDPS > 0) || (enemyDPS > 0 && rc.getHealth() / enemyDPS < enemyHealth / allyDPS)){
						Direction opDir = Direction.NORTH;
						if(nearestEnemy >= 0)
							opDir = enemies[nearestEnemy].location.directionTo(currLoc);
						Utils.safestMove(rc, currLoc.add(opDir), enemies, zombies);
						rc.broadcastSignal(70);
					}
                	//build turtle setup, alternating soldiers then turrets
					boolean enoughTurrets = (numArchons == 1 && turretCount >= 7) || (numArchons == 2 && turretCount >= 6)
							|| (numArchons == 3 && turretCount >= 5) || (numArchons == 4 && turretCount >= 4);
					boolean enoughSoldiers = (numArchons == 1 && soldierCount >= 9) || (numArchons == 2 && soldierCount >= 9)
							|| (numArchons >= 3 && soldierCount >= 10);
					if(!enoughSoldiers && (soldierNext || enoughTurrets)){
						if(rc.hasBuildRequirements(RobotType.SOLDIER) && rc.isCoreReady()){
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
							if(hasBuilt && !enoughTurrets)
								soldierNext = false;
						}
					} else if(!enoughTurrets){
						if(rc.hasBuildRequirements(RobotType.TURRET) && rc.isCoreReady()){
							Direction dirToBuild = null;
							if(numArchons == 1){
								while(dirToBuild == null || !rc.canBuild(dirToBuild, RobotType.TURRET)){
									int r = rand.nextInt(7) + 1;
									dirToBuild = Utils.DIRS[r];
								}
							} else if(numArchons == 2){
								while(dirToBuild == null || !rc.canBuild(dirToBuild, RobotType.TURRET)){
									int r = rand.nextInt(6);
									if(r > 1 && archonID == 0)
										r += 2;
									else if(r > 3 && archonID == 1)
										r = 2 * r - 3;
									dirToBuild = Utils.DIRS[r];
								}
							} else if(numArchons == 3){
								while(dirToBuild == null || !rc.canBuild(dirToBuild, RobotType.TURRET)){
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
								while(dirToBuild == null || !rc.canBuild(dirToBuild, RobotType.TURRET)){
									int r = rand.nextInt(4);
									if(archonID == 0){
										r += 4;
										if(r == 4)
											r = 1;
									} else if(archonID == 2){ //ID 1 is no problem
										r += 1;
										if(r == 4)
											r = 5;
									} else if(archonID == 3){
										r += 4;
									}
									dirToBuild = Utils.DIRS[r];
								}
							}
							rc.build(dirToBuild, RobotType.TURRET);
							if(!enoughSoldiers)
								soldierNext = true;
						}
					}
					if(rc.isCoreReady()){
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
                	//todo: add pulling
                }

                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
