package bigturtle;

import battlecode.common.*;

public class ZombieInfo {
	
	double dps;
	double health;
	RobotInfo bestTarget;
	double maxExpected;
	RobotInfo[] zombies;
	
	public ZombieInfo(RobotController rc) throws Exception{
		
		dps = 0;
		health = 0;
		bestTarget = null;
		maxExpected = 0;
		zombies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
		MapLocation currLoc = rc.getLocation();
		
		for(int i = 0; i < zombies.length; i++){
			int dist = currLoc.distanceSquaredTo(zombies[i].location);
			double tempExpected = Utils.priority(zombies[i]);
			if(!zombies[i].type.equals(RobotType.ZOMBIEDEN) && dist <= 13){
				health += zombies[i].health;
			}
			if (zombies[i].type.equals(RobotType.RANGEDZOMBIE) && dist <= 13){
				health += zombies[i].health;
				dps += 3;
			} else if (zombies[i].type.equals(RobotType.STANDARDZOMBIE) && dist <= 2){
				health += zombies[i].health;
				dps += 1.25;
			} else if (zombies[i].type.equals(RobotType.FASTZOMBIE) && dist <= 2){
				health += zombies[i].health;
				dps += 3;
			} else if (zombies[i].type.equals(RobotType.BIGZOMBIE) && dist <= 2){
				health += zombies[i].health;
				dps += 25.0/3;
			} else if (zombies[i].type.equals(RobotType.ZOMBIEDEN)){
			}
			if(tempExpected > maxExpected || bestTarget == null ||
					(tempExpected == maxExpected && dist < bestTarget.location.distanceSquaredTo(currLoc))){
				maxExpected = tempExpected;
				bestTarget = zombies[i];
			}
		}
		
	}
	
}
