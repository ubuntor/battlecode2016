package turtlepull;

import battlecode.common.*;

public class EnemyInfo {
	
	double dps;
	double health;
	int nearest;
	RobotInfo bestTarget;
	double maxExpected;
	RobotInfo[] enemies;
	
	public EnemyInfo(RobotController rc) throws Exception{
		
		dps = 0;
		health = 0;
		nearest = -1;
		bestTarget = null;
		maxExpected = 0;
		enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
		
		MapLocation currLoc = rc.getLocation();
		
		for(int i = 0; i < enemies.length; i++){
			int dist = currLoc.distanceSquaredTo(enemies[i].location);
			double tempExpected = 0;
			if(enemies[i].type.equals(RobotType.ARCHON)){
				tempExpected = 100000000;
			} else if (enemies[i].type.equals(RobotType.SOLDIER) && dist <= 13){
				tempExpected += 2 / enemies[i].health;
				health += enemies[i].health;
				dps += 2;
			} else if (enemies[i].type.equals(RobotType.GUARD) && dist <= 2){
				tempExpected += 1.5 / enemies[i].health;
				health += enemies[i].health;
				dps += 1.5;
			} else if (enemies[i].type.equals(RobotType.VIPER) && dist <= 20){
				tempExpected += (8.0/3) / enemies[i].health;
				health += enemies[i].health;
				dps += 2 + 2.0/3;
			} else if ((enemies[i].type.equals(RobotType.TURRET) || enemies[i].type.equals(RobotType.TTM)) && dist <= 40){
				tempExpected += (13.0/3) / enemies[i].health;
				health += enemies[i].health;
				dps += 13.0/3;
			} else if(enemies[i].type.equals(RobotType.SCOUT)){
				tempExpected += 0.01;
			}
			if(nearest < 0 || dist < currLoc.distanceSquaredTo(enemies[nearest].location))
				nearest = i;
			if(tempExpected > maxExpected || bestTarget == null ||
					(tempExpected == maxExpected && dist < bestTarget.location.distanceSquaredTo(currLoc))){
				maxExpected = tempExpected;
				bestTarget = enemies[i];
			}
		}
		
	}
	
}
