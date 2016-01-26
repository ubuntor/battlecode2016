package bigturtle;

import battlecode.common.*;

public class AllyInfo {
	int mostHurt;
    int nearArchons;
    int soldierCount;
    int turretCount;
    int totalTurrets;
    double allyDPS;
    RobotInfo[] allies;
    
    public AllyInfo(RobotController rc) throws Exception{
    	
    	mostHurt = -1;
        nearArchons = 0;
        soldierCount = 0;
        turretCount = 0;
        totalTurrets = 0;
        allyDPS = 0;
        allies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
        MapLocation currLoc = rc.getLocation();
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
    			totalTurrets++;
    			break;
    		default:
    			;
    		}
        	if(allies[i].ID == rc.getID() || allies[i].location.distanceSquaredTo(currLoc) > 24)
        		continue;
        	if(allies[i].type.equals(RobotType.ARCHON) && allies[i].location.distanceSquaredTo(currLoc) <= 2 && !allies[i].location.equals(currLoc))
        		nearArchons++;
        	if(allies[i].type != RobotType.ARCHON && allies[i].health < allies[i].maxHealth &&
        			(mostHurt == -1 || allies[i].health < allies[mostHurt].health))
        		mostHurt = i;
        }
    }
    
}
