package kappabot;

import java.util.LinkedList;

import battlecode.common.*;

public class Utils {
	public static RobotController rc = RobotPlayer.rc;
	public static LinkedList<MapLocation> locHis = new LinkedList<MapLocation>();
	public static int[] dirTry = {0,7,1,6,2,5,3,4};
	
	public static void moveTowards(MapLocation p) throws GameActionException {
		if (rc.isCoreReady()) {
			Direction d = rc.getLocation().directionTo(p);
			for (int i : dirTry) {
				Direction attempt = Direction.values()[(d.ordinal()+i)%8];
				if (!locHis.contains(rc.getLocation().add(attempt)) && rc.canMove(attempt)) {
					locHis.add(rc.getLocation());
					if (locHis.size() > 10) locHis.removeFirst();
					rc.move(attempt);
					return;
				}
			}
		}
	}
	
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