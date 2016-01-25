package team259.turtlepull;

import battlecode.common.*;
/**
 * Created by allen on 1/19/16.
 */
public class Turret {
        
    public static void run(RobotController rc) {
        boolean neutral = false;
    	try {
            Signal[] sigs = rc.emptySignalQueue();
            for(Signal s : sigs){
            	if(s.getTeam() != rc.getTeam())
            		continue;
            	int[] msg = s.getMessage();
            	if(msg != null && msg[0] == Utils.ACTIVATED_NEUTRAL && msg[1] == Utils.ACTIVATED_NEUTRAL){
            		neutral = true;
            		break;
            	}
            }
            if(neutral){
            	neutralRun(rc);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                // may waste bytecodes???
                switch (rc.getType()) {
                    case TURRET:
                        Signal[] sigs = rc.emptySignalQueue();
                        MapLocation attackLoc = null;;
                        for(Signal s : sigs){
                           if(s.getTeam() == rc.getTeam()){
                              int[] msg = s.getMessage();
                              if(msg != null && msg[0] == Utils.TURRET_ATTACK){
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
                        		MapLocation currLoc = rc.getLocation();
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
                        break;
                    case TTM:
                        rc.unpack();
                        break;
                    default:
                        System.out.println("uwotm8");
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private static void neutralRun(RobotController rc){
    	
    	try{
    		if(rc.getType().equals(RobotType.TURRET))
    			rc.pack();
    	} catch (Exception e){
    		System.out.println(e.getMessage());
            e.printStackTrace();
    	}
    	while (true) {
            try {
                switch (rc.getType()) {
                    case TURRET:

                        if(rc.isWeaponReady()){
                        		MapLocation currLoc = rc.getLocation();
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
            					if(bestTarget != null && rc.isWeaponReady())
            						rc.attackLocation(bestTarget.location);
                        	}
                        }
                        break;
                    case TTM:
                        RobotInfo[] allied = rc.senseNearbyRobots(24, rc.getTeam());
                        MapLocation currLoc = rc.getLocation();
                        int nearest = -1;
                        int dist = 0;
                        for(int i = 0; i < allied.length; i++){
                        	if(nearest < 0 || currLoc.distanceSquaredTo(allied[i].location) < dist){
                        		nearest = i;
                        		dist = currLoc.distanceSquaredTo(allied[i].location);
                        	}
                        }
                        if(nearest < 0 && rc.isCoreReady())
                        	rc.unpack();
                        else if(rc.isCoreReady()){
                        	Direction dir = allied[nearest].location.directionTo(currLoc);
                        	int start = 0;
                        	for(int i = 0; i < 8; i++){
                        		if(Utils.DIRS[i].equals(dir)){
                        			start = i;
                        			break;
                        		}
                        	}
                        	for(int i = 0; i < 8; i++){
                        		Direction temp = Utils.DIRS[(start + 8 + Utils.SEARCH[i]) % 8];
                        		if(rc.canMove(temp)){
                        			rc.move(temp);
                        			break;
                        		}
                        	}
                        } break;
                    default:
                        System.out.println("uwotm8");
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
    	}
    	
    	
    }
}
