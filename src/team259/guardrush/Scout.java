package team259.guardrush;

import battlecode.common.*;

/**
 * Created by samuel on 1/11/16.
 */
public class Scout {
    public static void run(RobotController rc) {
        Direction dirToMove;
        try {
            // init stuff
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        while (true) {
            try {
                for(int i = 0; i < 5; i++){
                    rc.broadcastSignal(10000);
                }
                for(int i = 0; i < 20; i++){
                    rc.broadcastMessageSignal(0,0,10000);
                }
                //run the fuck away if you see an enemy
                if (rc.isCoreReady()) {
                    RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), 53);
                    if (enemies.length > 0) {
                        int enemyDistance = 999;
                        RobotInfo closestEnemy = null;
                        for (int i = 0; i < enemies.length; i++) {
                            if (enemies[i].location.distanceSquaredTo(rc.getLocation()) < enemyDistance && enemies[i].type.canAttack()) {
                                closestEnemy = enemies[i];
                                enemyDistance = enemies[i].location.distanceSquaredTo(rc.getLocation());
                            }
                        }
                        if (closestEnemy != null) {
                            dirToMove = closestEnemy.location.directionTo(rc.getLocation());
                            if (rc.canMove(dirToMove)) {
                                // Move away
                                rc.move(dirToMove);
                            } else {
                                if (rc.canMove(dirToMove.rotateLeft()))
                                    rc.move(dirToMove.rotateLeft());
                                else if (rc.canMove(dirToMove.rotateRight()))
                                    rc.move(dirToMove.rotateRight());
                                else if (rc.canMove(dirToMove.rotateLeft().rotateLeft()))
                                    rc.move(dirToMove.rotateLeft().rotateLeft());
                                else if (rc.canMove(dirToMove.rotateRight().rotateRight()))
                                    rc.move(dirToMove.rotateRight().rotateRight());
                                else if (rc.canMove(dirToMove.rotateLeft().rotateLeft().rotateLeft()))
                                    rc.move(dirToMove.rotateLeft().rotateLeft().rotateLeft());
                                else if (rc.canMove(dirToMove.rotateRight().rotateRight().rotateRight()))
                                    rc.move(dirToMove.rotateRight().rotateRight().rotateRight());
                                // if we still can't move then we're fucked lol
                            }
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
}
