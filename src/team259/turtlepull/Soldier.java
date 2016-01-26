package team259.turtlepull;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by allen on 1/19/16.
 */
public class Soldier {
    public static void run(RobotController rc) {
        Random rand = new Random(rc.getID());
        boolean neutral = false;
        MapLocation[] patrol = new MapLocation[4];
        int nextDest = 0;
        Team myTeam = null;
        Team otherTeam = null;
        MapLocation tlArchon = null;
        try {
        	myTeam = rc.getTeam();
			otherTeam = myTeam.opponent();
        	int numArchons = rc.getInitialArchonLocations(myTeam).length;

            Signal[] sigs = rc.emptySignalQueue();
            for(Signal s : sigs){
            	if(s.getTeam().equals(myTeam)){
            		int[] message = s.getMessage();
            		if(message != null && message[0] == Utils.SOLDIER_PATROL){
            			int[] temp = Utils.unpack2(message[1]);
            			tlArchon = new MapLocation(temp[0], temp[1]);
            		}
            		if(message != null && message[0] == Utils.ACTIVATED_NEUTRAL && message[1] == Utils.ACTIVATED_NEUTRAL){
                		neutral = true;
                		break;
                	}
            	}
            }
            if(neutral){
            	neutralRun(rc);
            }
            if(tlArchon == null){
                for(RobotInfo r : rc.senseNearbyRobots(24, myTeam)){
                	if(r.type.equals(RobotType.ARCHON)){
                		if(tlArchon == null)
                			tlArchon = r.location;
                		else {
                			Direction tempDir = r.location.directionTo(tlArchon);
                			if(tempDir.equals(Direction.EAST) || tempDir.equals(Direction.SOUTH_EAST) || tempDir.equals(Direction.SOUTH))
                				tlArchon = r.location;
                		}
                	}
                }
                if(tlArchon == null)
                	tlArchon = rc.getLocation();
//            	System.out.println("!!");
            }

            patrol[0] = tlArchon.add(Direction.NORTH_WEST, 2);
            patrol[1] = tlArchon.add(Direction.NORTH_EAST, 2);
            patrol[2] = tlArchon.add(Direction.SOUTH_EAST, 2);
            patrol[3] = tlArchon.add(Direction.SOUTH_WEST, 2);
            if(numArchons > 1){
            	patrol[1] = patrol[1].add(Direction.EAST);
            	patrol[2] = patrol[2].add(Direction.EAST);
            }
            if(numArchons > 2){
            	patrol[2] = patrol[2].add(Direction.SOUTH);
            	patrol[3] = patrol[3].add(Direction.SOUTH);
            }
//            System.out.println(Arrays.toString(patrol));
            MapLocation currLoc = rc.getLocation();
            if(currLoc.x - patrol[0].x <= 1){
            	nextDest = 0;
            } else if(currLoc.y - patrol[1].y <= 1){
            	nextDest = 1;
            } else if(patrol[2].x - currLoc.x <= 1){
            	nextDest = 2;
            } else if(patrol[3].y - currLoc.y <= 1){
            	nextDest = 3;
            } else {
            	for(int i = 1; i < patrol.length; i++){
            		if(currLoc.distanceSquaredTo(patrol[i]) < currLoc.distanceSquaredTo(patrol[nextDest]))
            			nextDest = i;
            	}
            }
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
            	MapLocation currLoc = rc.getLocation();
            	Signal[] sigs = rc.emptySignalQueue();
                MapLocation attackLoc = null;;
                for(Signal s : sigs){
                   if(s.getTeam() == rc.getTeam()){
                      int[] msg = s.getMessage();
                      if(msg != null && msg[0] == Utils.SOLDIER_ATTACK){
                         int[] loc = Utils.unpack2(msg[1]);
                    	 attackLoc = new MapLocation(loc[0], loc[1]); 
                         break;
                      }
                   }
                }
                if(rc.isWeaponReady()){
                	if(attackLoc != null && rc.canAttackLocation(attackLoc))
                		rc.attackLocation(attackLoc);
                	else {
                		RobotInfo[] hostile = rc.senseHostileRobots(currLoc, 24);
                		RobotInfo bestTarget = null;
    					double maxExpected = 0;
    					for(int i = 0; i < hostile.length; i++){
    						if(!rc.canAttackLocation(hostile[i].location))
    							continue;
    						int dist = currLoc.distanceSquaredTo(hostile[i].location);
    						double tempExpected = 0;
    						if (hostile[i].type.equals(RobotType.SOLDIER) && dist <= 13){
    							tempExpected += 2 / hostile[i].health;
    						} else if (hostile[i].type.equals(RobotType.GUARD) && dist <= 2){
    							tempExpected += 1.5 / hostile[i].health;
    						} else if (hostile[i].type.equals(RobotType.VIPER) && dist <= 20){
    							tempExpected += (8.0/3) / hostile[i].health;
    						} else if (hostile[i].type.equals(RobotType.TURRET) && dist <= 48){
    							tempExpected += (13.0/3) / hostile[i].health;
    						} else if (hostile[i].type.equals(RobotType.RANGEDZOMBIE) && dist <= 13){
    							tempExpected += 3 / hostile[i].health;
    						} else if (hostile[i].type.equals(RobotType.STANDARDZOMBIE) && dist <= 2){
    							tempExpected += 1.25 / hostile[i].health;
    						} else if (hostile[i].type.equals(RobotType.FASTZOMBIE) && dist <= 2){
    							tempExpected += 3 / hostile[i].health;
    						} else if (hostile[i].type.equals(RobotType.BIGZOMBIE) && dist <= 2){
    							tempExpected += (25.0/3) / hostile[i].health;
    						}
    						if(tempExpected > maxExpected || bestTarget == null ||
    								(tempExpected == maxExpected && dist > bestTarget.location.distanceSquaredTo(currLoc))){
    							maxExpected = tempExpected;
    							bestTarget = hostile[i];
    						}
    					}
    					if(bestTarget != null)
    						rc.attackLocation(bestTarget.location);
                	}
                }
                if((currLoc.x - tlArchon.x)*(patrol[nextDest].y - tlArchon.y) - (currLoc.y - tlArchon.y)*(patrol[nextDest].x - tlArchon.x) <= 0){
                	nextDest = (nextDest + 1) % patrol.length;
                }
                
                if(rc.isCoreReady()){
//                	System.out.println(rc.getID());
                	Direction moveDir = currLoc.directionTo(patrol[nextDest]);
//                	System.out.println(moveDir);
					if (rc.senseRubble(currLoc.add(moveDir)) > 0){
						rc.clearRubble(moveDir);
//						System.out.println("" + rc.getID() + ": clearing rubble");
					} else if(!rc.canMove(moveDir)){
						RobotInfo inWay = rc.senseRobotAtLocation(currLoc.add(moveDir));
                		if(inWay == null){
                			if(rc.senseRubble(currLoc.add(moveDir)) > 0)
                				rc.clearRubble(moveDir);
//                			System.out.println("" + rc.getID() + ":  nutin in way, going for " + patrol[nextDest]);
                		} else if(rc.isCoreReady() && inWay.team.equals(myTeam) && !inWay.type.equals(RobotType.SOLDIER)){
                			if(!Utils.straightMove(rc, patrol[nextDest], new RobotInfo[0], new RobotInfo[0]) && rc.isCoreReady()){
                				 if(rc.canMove(moveDir.rotateLeft().rotateLeft()))
                     				rc.move(moveDir.rotateLeft().rotateLeft());
                     			else if(rc.canMove(moveDir.rotateRight().rotateRight()))
                     				rc.move(moveDir.rotateRight().rotateRight());
                			}
                		} else if(!inWay.team.equals(myTeam) && rc.isWeaponReady() && rc.canAttackLocation(inWay.location)){
                			rc.attackLocation(inWay.location);
                		}
                	} else if(rc.isCoreReady())
                		rc.move(moveDir);
                }
                if(rc.getLocation().equals(patrol[nextDest])){
//                	System.out.println(myTeam);
                	nextDest = (nextDest + 1) % patrol.length;
                }
                if(currLoc.x < patrol[0].x)
                	nextDest = 0;
                else if(currLoc.y < patrol[1].y)
                	nextDest = 1;
                else if(currLoc.x > patrol[2].x)
                	nextDest = 2;
                else if(currLoc.y > patrol[3].y)
                	nextDest = 3;

                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private static void neutralRun(RobotController rc){
    	Random rand = new Random(rc.getID());
        int distance;
        int cycle = 0;
        Direction dirToMove;
        RobotInfo closestEnemy = null;
        RobotInfo[] attackable;
        RobotInfo[] enemies;
        MapLocation toAttack;
        MapLocation originalTarget = rc.getLocation();
        MapLocation[] targets = rc.getInitialArchonLocations(rc.getTeam().opponent());
        int targetNum = 0;
        Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
        try {
            distance = 999;
            for (int i = 0; i < targets.length; i++) {
                if (targets[i].distanceSquaredTo(rc.getLocation()) < distance) {
                    targetNum = i;
                    distance = targets[i].distanceSquaredTo(rc.getLocation());
                }
            }
            originalTarget = targets[targetNum];
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                //attack
                if(rc.isWeaponReady()){
                    attackable = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
                    if (attackable.length > 0) {
                        toAttack = findWeakest(attackable);
                        rc.attackLocation(toAttack);
                    }
                }

                if(rc.isCoreReady()) {
                    //micro
                    enemies = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
                    if (enemies.length > 0) {
                        distance = 999;
                        for (RobotInfo r : enemies) {
                            if (r.location.distanceSquaredTo(rc.getLocation()) < distance) {
                                closestEnemy = r;
                                distance = r.location.distanceSquaredTo(rc.getLocation());
                            }
                        }

                        //run away
                        if (rc.isCoreReady() && distance <= rc.getType().attackRadiusSquared) {
                            dirToMove = closestEnemy.location.directionTo(rc.getLocation());
                            if (rc.canMove(dirToMove)) {
                                // Move away
                                rc.move(dirToMove);
                            } else if (rc.canMove(dirToMove.rotateLeft())) {
                                rc.move(dirToMove.rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight())){
                                rc.move(dirToMove.rotateRight());
                            } else if (rc.canMove(dirToMove.rotateLeft().rotateLeft())){
                                rc.move(dirToMove.rotateLeft().rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight().rotateRight())){
                                rc.move(dirToMove.rotateRight().rotateRight());
                            } else if (rc.canMove(dirToMove.rotateLeft().rotateLeft().rotateLeft())) {
                                rc.move(dirToMove.rotateLeft().rotateLeft().rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight().rotateRight().rotateRight())) {
                                rc.move(dirToMove.rotateRight().rotateRight().rotateRight());
                            } else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                // Too much rubble, so I should clear it
                                rc.clearRubble(dirToMove);
                            }
                            // if we still can't move then we're fucked lol
                        }

                        //run to
                        if (rc.isCoreReady() && distance > rc.getType().attackRadiusSquared) {
                            dirToMove = rc.getLocation().directionTo(closestEnemy.location);
                            if (rc.canMove(dirToMove)) {
                                // Move
                                rc.move(dirToMove);
                            } else if (rc.canMove(dirToMove.rotateLeft())) {
                                rc.move(dirToMove.rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight())) {
                                rc.move(dirToMove.rotateRight());
                            } else if (rc.canMove(dirToMove.rotateLeft().rotateLeft())){
                                rc.move(dirToMove.rotateLeft().rotateLeft());
                            } else if (rc.canMove(dirToMove.rotateRight().rotateRight())){
                                rc.move(dirToMove.rotateRight().rotateRight());
                            } else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                                // Too much rubble, so I should clear it
                                rc.clearRubble(dirToMove);
                                // Check if I can move in this direction
                            }
                        }
                    }

                    //patrol/move
                    if (rc.isCoreReady()) {
                        dirToMove = directions[rand.nextInt(8)];
                        //move to target
                        if(cycle == 0) {
                            if (rc.getLocation().distanceSquaredTo(targets[targetNum]) <= 2) {
                                targetNum++;
                                targetNum = targetNum % targets.length;
                                if (originalTarget == targets[targetNum]) {
                                    cycle = 1;
                                }
                            } else if (targetNum < targets.length) {
                                dirToMove = rc.getLocation().directionTo(targets[targetNum]);
                            }
                        }
                        if (rc.canMove(dirToMove)) {
                            // Move
                            rc.move(dirToMove);
                        } else if (rc.canMove(dirToMove.rotateLeft())) {
                            rc.move(dirToMove.rotateLeft());
                        } else if (rc.canMove(dirToMove.rotateRight())) {
                            rc.move(dirToMove.rotateRight());
                        } else if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                            // Too much rubble, so I should clear it
                            rc.clearRubble(dirToMove);
                            // Check if I can move in this direction
                        }
                    }
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static MapLocation findWeakest(RobotInfo[] listOfRobots) {
        double weakestSoFar = -100;
        MapLocation weakestLocation = null;
        for (RobotInfo r : listOfRobots) {
            double weakness = r.maxHealth - r.health;
            if (r.type == RobotType.TURRET){
                weakness *= 6;
            }
            else if (r.type == RobotType.VIPER){
                weakness *= 5;
            }
            else if (r.type == RobotType.SOLDIER){
                weakness *= 4;
            }
            else if (r.type == RobotType.GUARD){
                weakness *= 3;
            }
            else if (r.type == RobotType.ARCHON || r.type ==  RobotType.SCOUT){
                weakness *= 2;
            }
            if(r.viperInfectedTurns == 0 && r.team != Team.ZOMBIE){
                weakness *=5;
            }
            if (weakness > weakestSoFar) {
                weakestLocation = r.location;
                weakestSoFar = weakness;
            }
        }
        return weakestLocation;
    }
}
