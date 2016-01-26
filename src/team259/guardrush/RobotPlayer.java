package team259.guardrush;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {
    public static void run(RobotController rc) {
        switch (rc.getType()) {
            case ARCHON:
                Archon.run(rc);
                break;
            case GUARD:
                Guard.run(rc);
                break;
            case SCOUT:
                Scout.run(rc);
                break;
            case SOLDIER:
                Soldier.run(rc);
                break;
            case TTM:
            case TURRET:
                Turret.run(rc);
                break;
            case VIPER:
                Viper.run(rc);
                break;
            default:
                System.out.println("uwotm8");
        }
    }
}
