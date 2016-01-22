package turtle;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by allen on 1/19/16.
 */
public class Soldier {
    public static void run(RobotController rc) {
        Random rand = new Random(rc.getID());
        int fate = rand.nextInt(1000);
        MapLocation[] patrol = new MapLocation[4];
        int nextDest = 0;
        Team myTeam = null;
        Team otherTeam = null;
        try {
        	myTeam = rc.getTeam();
			otherTeam = myTeam.opponent();
        	MapLocation tlArchon = null;
        	int numArchons = rc.getInitialArchonLocations(myTeam).length;
//            for(RobotInfo r : rc.senseNearbyRobots(24, myTeam)){
//            	if(r.type.equals(RobotType.ARCHON)){
//            		if(tlArchon == null)
//            			tlArchon = r.location;
//            		else {
//            			Direction tempDir = r.location.directionTo(tlArchon);
//            			if(tempDir.equals(Direction.EAST) || tempDir.equals(Direction.SOUTH_EAST) || tempDir.equals(Direction.SOUTH))
//            				tlArchon = r.location;
//            		}
//            	}
//            }
            Signal[] sigs = rc.emptySignalQueue();
            for(Signal s : sigs){
            	if(s.getTeam().equals(myTeam)){
            		int[] message = s.getMessage();
            		if(message != null && message[0] == Utils.SOLDIER_PATROL){
            			int[] temp = Utils.unpack2(message[1]);
            			tlArchon = new MapLocation(temp[0], temp[1]);
            		}
            	}
            		
            }
            if(tlArchon == null){
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
                		} else if(rc.isCoreReady() && (!inWay.team.equals(myTeam) || !inWay.type.equals(RobotType.SOLDIER))){
                			if(rc.canMove(moveDir.rotateLeft()))
                				rc.move(moveDir.rotateLeft());
                			else if(rc.canMove(moveDir.rotateRight()))
                				rc.move(moveDir.rotateRight());
                			else if(rc.canMove(moveDir.rotateLeft().rotateLeft()))
                				rc.move(moveDir.rotateLeft().rotateLeft());
                			else if(rc.canMove(moveDir.rotateRight().rotateRight()))
                				rc.move(moveDir.rotateRight().rotateRight());
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
}
