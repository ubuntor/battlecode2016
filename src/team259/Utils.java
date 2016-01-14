package team259;

import battlecode.common.*;

/**
 * Created by samuel on 1/11/16.
 */
public class Utils {
    public static int packint2(int a, int b) {
        // -32768 <= a,b <= 32767
        return (a << 16) | (b & 0xFFFF);
    }
    public static int[] unpackint2(int a) {
        return new int[]{a >> 16, (short)(a & 0xFFFF)};
    }
    public static void broadcast4(RobotController rc, int a, int b, int c, int d, int range) throws GameActionException {
        rc.broadcastMessageSignal(packint2(a, b), packint2(c, d), range);
    }
    public static int[] unpack4(Signal s) throws GameActionException {
        int[] a = unpackint2(s.getMessage()[0]);
        int[] b = unpackint2(s.getMessage()[1]);
        return new int[]{a[0], a[1], b[0], b[1]};
    }
}