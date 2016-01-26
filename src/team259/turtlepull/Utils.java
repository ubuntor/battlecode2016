package team259.turtlepull;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

/**
 * Created by allen on 1/19/16.
 */
public class Utils {

	public static final Direction[] DIRS = { Direction.NORTH, Direction.NORTH_EAST, Direction.EAST,
			Direction.SOUTH_EAST, Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
	public static final int[] SEARCH = { 0, 1, -1, 2, -2, 3, -3, 4 };    
    
	public static final int ARCHON_HELP = -293848;
	public static final int ARCHON_TEMP_HOME = 120983;
	public static final int ARCHON_FINAL_HOME = 120984;
	public static final int ACTIVATED_NEUTRAL = 108236;
	
	//scout signaling
	public static final int SCOUT_SCOUT = 12983;
	public static final int SCOUT_PULL_TO = 6000;
	public static final int SCOUT_PULL_FROM = 5999;
	public static final int SCOUT_PATROL = 23941;
	
	public static final int SEE_DEN = -678922;
	
	public static final int SEE_ARCHON = -230845;
	public static final int SEE_TURRET = -230846;
	
	public static final int SEE_NEUTRAL_ARCHON = 92341;
	public static final int SEE_NEUTRAL_SCOUT = 92342;
	public static final int SEE_NEUTRAL_SOLDIER = 92343;
	public static final int SEE_NEUTRAL_GUARD = 92344;
	public static final int SEE_NEUTRAL_VIPER = 92345;
	public static final int SEE_NEUTRAL_TURRET = 92346;
	
	public static final int SEE_SMALL_PARTS = 8370;
	public static final int SEE_MED_PARTS = 8371;
	public static final int SEE_BIG_PARTS = 8372;
    
	//turret signaling
	public static final int TURRET_ATTACK = -129;
	
	//soldier signaling
	public static final int SOLDIER_PATROL = 123810;
	public static final int SOLDIER_MOVE = 124081;
	public static final int SOLDIER_ATTACK = 60921;
	
	
	public static int pack2(int a, int b) {
        // -32768 <= a,b <= 32767
        return (a << 16) | (b & 0xFFFF);
    }
    public static int[] unpack2(int a) {
        return new int[]{a >> 16, (short)(a & 0xFFFF)};
    }
    
    public static boolean straightMove(RobotController rc, MapLocation loc, RobotInfo[] enemies, RobotInfo[] zombies) throws Exception{
    	
    	if(!rc.isCoreReady())
			return false;
		
		Direction currDir = rc.getLocation().directionTo(loc);

		if(rc.canMove(currDir)){
			rc.move(currDir);
			return true;
		} else if(rc.canMove(currDir.rotateLeft())){
			rc.move(currDir.rotateLeft());
			return true;
		} else if(rc.canMove(currDir.rotateRight())){
			rc.move(currDir.rotateRight());
			return true;
		}
		int start = 0;
		for (int i = 0; i < DIRS.length; i++) {
			if (DIRS[i].equals(currDir)) {
				start = i;
				break;
			}
		}
		int best = -1;
		double minRubble = 0;
		for(int i = 0; i <= 2; i++){
			Direction dir = DIRS[(start + 8 + SEARCH[i]) % 8];
			MapLocation temp = rc.getLocation().add(dir);
			if(!rc.onTheMap(temp) || rc.senseRobotAtLocation(temp) != null)
				continue;
			double rubble = rc.senseRubble(temp);
			if(best < 0 || rubble < minRubble){
				best = i;
				minRubble = rubble;
			}
			
		}
		if(best < 0)
			return safestMove(rc, loc, enemies, zombies);
		else {
			rc.clearRubble(DIRS[(start + 8 + SEARCH[best]) % 8]);
		}
		
		return false;
    	
    }

	public static boolean safeMove(RobotController rc, MapLocation loc, RobotInfo[] enemies, RobotInfo[] zombies) throws Exception {

		if(!rc.isCoreReady())
			return false;
		
		Direction currDir = rc.getLocation().directionTo(loc);
		int start = 0;
		for (int i = 0; i < DIRS.length; i++) {
			if (DIRS[i].equals(currDir)) {
				start = i;
				break;
			}
		}
		for (int i = 0; i < 8; i++) {
			Direction dir = DIRS[(start + 8 + SEARCH[i]) % 8];
			MapLocation tempLoc = rc.getLocation().add(dir);
			if (!rc.canMove(dir))
				continue;
			boolean safe = true;
			for(RobotInfo r : enemies){
				if (r.type.equals(RobotType.SOLDIER) && r.location.distanceSquaredTo(tempLoc) <= 13)
					safe = false;
				else if (r.type.equals(RobotType.GUARD) && r.location.distanceSquaredTo(tempLoc) <= 2)
					safe = false;
				else if (r.type.equals(RobotType.VIPER) && r.location.distanceSquaredTo(tempLoc) <= 20)
					safe = false;
				else if (r.type.equals(RobotType.TURRET) && r.location.distanceSquaredTo(tempLoc) <= 48)
					safe = false;
			}
			
			for (RobotInfo r : zombies) {
				if(!safe)
					break;
				if (r.type.equals(RobotType.RANGEDZOMBIE) && r.location.distanceSquaredTo(tempLoc) <= 13) {
					safe = false;
				} else if (r.location.distanceSquaredTo(tempLoc) <= 2) {
					safe = false;
				}
			}
			if (!safe)
				continue;
			rc.move(dir);
			return true;
		}
		return false;

	}
	
	public static boolean safestMove(RobotController rc, MapLocation loc, RobotInfo[] enemies, RobotInfo[] zombies) throws Exception {

		if(!rc.isCoreReady())
			return false;
		
		MapLocation currLoc = rc.getLocation();
		Direction currDir = currLoc.directionTo(loc);
		int start = 0;
		for (int i = 0; i < DIRS.length; i++) {
			if (DIRS[i].equals(currDir)) {
				start = i;
				break;
			}
		}
		int bestDir = -1;
		double leastDamage = 0;
		for(RobotInfo r : enemies){
			if (r.type.equals(RobotType.SOLDIER) && r.location.distanceSquaredTo(currLoc) <= 13)
				leastDamage += 4;
			else if (r.type.equals(RobotType.GUARD) && r.location.distanceSquaredTo(currLoc) <= 2)
				leastDamage += 1.5;
			else if (r.type.equals(RobotType.VIPER) && r.location.distanceSquaredTo(currLoc) <= 20)
				leastDamage += 42;
			else if (r.type.equals(RobotType.TURRET) && r.location.distanceSquaredTo(currLoc) <= 48)
				leastDamage += 13;
		}
		
		for (RobotInfo r : zombies) {
			if (r.type.equals(RobotType.RANGEDZOMBIE) && r.location.distanceSquaredTo(currLoc) <= 13) {
				leastDamage += 3;
			} else if (r.type.equals(RobotType.STANDARDZOMBIE) && r.location.distanceSquaredTo(currLoc) <= 2) {
				leastDamage += 2.5;
			} else if (r.type.equals(RobotType.FASTZOMBIE) && r.location.distanceSquaredTo(currLoc) <= 2){
				leastDamage += 3;
			} else if(r.type.equals(RobotType.BIGZOMBIE) && r.location.distanceSquaredTo(currLoc) <= 2){
				leastDamage += 25;
			}
		}
		for (int i = 0; i < 8; i++) {
			Direction dir = DIRS[(start + SEARCH[i] + 8) % 8];
			MapLocation tempLoc = rc.getLocation().add(dir);
			if (!rc.canMove(dir))
				continue;
			double damageTaken = 0;
			for(RobotInfo r : enemies){
				if (r.type.equals(RobotType.SOLDIER) && r.location.distanceSquaredTo(tempLoc) <= 13)
					damageTaken += 4;
				else if (r.type.equals(RobotType.GUARD) && r.location.distanceSquaredTo(tempLoc) <= 2)
					damageTaken += 1.5;
				else if (r.type.equals(RobotType.VIPER) && r.location.distanceSquaredTo(tempLoc) <= 20)
					damageTaken += 42;
				else if (r.type.equals(RobotType.TURRET) && r.location.distanceSquaredTo(tempLoc) <= 48)
					damageTaken += 13;
			}
			
			for (RobotInfo r : zombies) {
				if (r.type.equals(RobotType.RANGEDZOMBIE) && r.location.distanceSquaredTo(tempLoc) <= 13) {
					damageTaken += 3;
				} else if (r.type.equals(RobotType.STANDARDZOMBIE) && r.location.distanceSquaredTo(tempLoc) <= 2) {
					damageTaken += 2.5;
				} else if (r.type.equals(RobotType.FASTZOMBIE) && r.location.distanceSquaredTo(tempLoc) <= 2){
					damageTaken += 3;
				} else if(r.type.equals(RobotType.BIGZOMBIE) && r.location.distanceSquaredTo(tempLoc) <= 2){
					damageTaken += 25;
				}
			}
			
			if(damageTaken == 0){
				bestDir = i;
				break;
			} else if(damageTaken < leastDamage)
				bestDir = i;
		}
		
//		System.out.println();
		if(bestDir < 0)
			return false;
		Direction toMove = DIRS[(start + 8+ SEARCH[bestDir]) % 8]; 

		rc.move(toMove);
		return true;

	}
	
	public static boolean kamikaze(RobotController rc, MapLocation home, MapLocation loc) throws Exception {

		if(!rc.isCoreReady())
			return false;
		
		Direction currDir = home.directionTo(loc);
		int start = 0;
		for (int i = 0; i < DIRS.length; i++) {
			if (DIRS[i].equals(currDir)) {
				start = i;
			}
		}
		for (int i = 0; i < 8; i++) {
			Direction dir = DIRS[(start + 8 + SEARCH[i]) % 8];
			if (rc.canMove(dir)){
				rc.move(dir);
				return true;
			}
		}
		return false;

	}
	
	
}