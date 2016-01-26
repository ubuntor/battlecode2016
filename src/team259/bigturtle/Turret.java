package team259.bigturtle;

import battlecode.common.*;
/**
 * Created by allen on 1/19/16.
 */
public class Turret {
        
    public static void run(RobotController rc) {
        boolean neutral = false;
        boolean hasExpanded = false;
        MapLocation tlArchon = null;
        int started = 0;
    	try {
            Signal[] sigs = rc.emptySignalQueue();
            for(Signal s : sigs){
            	if(s.getTeam() != rc.getTeam())
            		continue;
            	int[] msg = s.getMessage();
				if (msg != null) {
					if(msg[0] == Utils.TURRET_BUILT){
						int[] loc = Utils.unpack2(msg[1]);
						tlArchon = new MapLocation(loc[0], loc[1]);
					} else if (msg[0] == Utils.ACTIVATED_NEUTRAL && msg[1] == rc.getID()) {
						neutral = true;
						break;
					}
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
                        hasExpanded = false;
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
						if (rc.isWeaponReady()) {
							MapLocation currLoc = rc.getLocation();
							AllyInfo allyInfo = new AllyInfo(rc);
							EnemyInfo enemyInfo = new EnemyInfo(rc);
							ZombieInfo zombieInfo = new ZombieInfo(rc);
							RobotInfo bestTarget = null;
							double maxExpected = 0;
							if (enemyInfo.bestTarget != null) {
								bestTarget = enemyInfo.bestTarget;
								maxExpected = enemyInfo.maxExpected;
							}
							if (zombieInfo.bestTarget != null
									&& (bestTarget == null || zombieInfo.maxExpected > maxExpected)) {
								bestTarget = zombieInfo.bestTarget;
								maxExpected = enemyInfo.maxExpected;
							}
							if (bestTarget != null) {
								if (rc.canAttackLocation(bestTarget.location))
									rc.attackLocation(bestTarget.location);
							} else if (attackLoc != null) {
								if (rc.canAttackLocation(attackLoc))
									rc.attackLocation(attackLoc);
							} else if (rc.isCoreReady() && dirToExpand(rc, tlArchon,
									rc.getInitialArchonLocations(rc.getTeam()).length) != null) {
								rc.pack();
								started = rc.getRoundNum();
							}
						}
                        break;
                    case TTM:
                        if(hasExpanded && rc.isCoreReady() || (rc.getRoundNum() - started > 75))
                        	rc.unpack();
                        else {
                        	Direction expand = dirToExpand(rc, tlArchon, rc.getInitialArchonLocations(rc.getTeam()).length);
                        	if(expand != null && rc.isCoreReady() && rc.canMove(expand)){
                        		rc.move(expand);
                        		hasExpanded = true;
                        	}
                        }
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
            						double tempExpected = Utils.priority(hostile[i]);
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
    
    private static Direction dirToExpand(RobotController rc, MapLocation tlArchon, int numArchons) throws Exception{
    	
    	Direction dir = null;
    	MapLocation currLoc = rc.getLocation();
		dir = tlArchon.directionTo(currLoc);
    	switch(numArchons){
    	case 1:
    		break;
    	case 2:
    		if(currLoc.x - tlArchon.x == 1 && currLoc.y - tlArchon.y == -1)
    			dir = Direction.NORTH;
    		if(currLoc.x - tlArchon.x == 2){
    			if(currLoc.y - tlArchon.y == -1)
    				dir = Direction.NORTH_EAST;
    			if(currLoc.y - tlArchon.y == 0)
    				dir = Direction.EAST;
    			if(currLoc.y - tlArchon.y == 1)
    				dir = Direction.SOUTH_EAST;
    		}
    		break;
    	default:
    		if(currLoc.x - tlArchon.x == -1){
    			if(currLoc.y - tlArchon.y == 1)
    				dir = Direction.WEST;
    			if(currLoc.y - tlArchon.y == 2)
    				dir = Direction.SOUTH_WEST;
    		}
    		if(currLoc.x - tlArchon.x == 1 && currLoc.y - tlArchon.y == -1)
    			dir = Direction.NORTH;
    		if(currLoc.x - tlArchon.x == 2){
    			if(currLoc.y - tlArchon.y == -1)
    				dir = Direction.NORTH_EAST;
    			if(currLoc.y - tlArchon.y == 0)
    				dir = Direction.EAST;
    			if(currLoc.y - tlArchon.y == 1)
    				dir = Direction.EAST;
    			if(currLoc.y - tlArchon.y == 2)
    				dir = Direction.SOUTH_EAST;
    		}
    		break;
    	}
    	if(dir.isDiagonal() && !(numArchons == 3 && currLoc.equals(tlArchon.add(Direction.SOUTH_EAST)))){
    		//try left
    		MapLocation tryLoc = currLoc.add(dir.rotateLeft());
    		if(rc.senseRobotAtLocation(tryLoc) == null && withinTurtle(tryLoc, tlArchon, numArchons))
    			return dir.rotateLeft();
    		//else try right
    		tryLoc = currLoc.add(dir.rotateRight());
    		if(rc.senseRobotAtLocation(tryLoc) == null && withinTurtle(tryLoc, tlArchon, numArchons))
    			return dir.rotateRight();
    	} else {
    		MapLocation tryLoc = currLoc.add(dir);
    		if(rc.senseRobotAtLocation(tryLoc) == null && withinTurtle(tryLoc, tlArchon, numArchons))
    			return dir;
    	}
    	return null;
    	
    }
    
    private static boolean withinTurtle(MapLocation test, MapLocation tlArchon, int numArchons){
    	
    	if(numArchons >= 3)
    		numArchons = 4;
    	MapLocation[] center = new MapLocation[numArchons];
    	center[0] = tlArchon;
    	if(numArchons >= 2)
    		center[1] = tlArchon.add(Direction.EAST);
    	if(numArchons >= 3)
    		center[2] = tlArchon.add(Direction.SOUTH);
    	if(numArchons == 4)
    		center[3] = tlArchon.add(Direction.SOUTH_EAST);
    	for(MapLocation loc : center){
    		if(loc.distanceSquaredTo(test) <= 7)
    			return true;
    	}
    	return false;
    	
    }
}
