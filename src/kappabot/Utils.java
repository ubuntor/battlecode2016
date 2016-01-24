package kappabot;

import java.util.LinkedList;

import battlecode.common.*;

public class Utils {
	public static RobotController rc = RobotPlayer.rc;
	public static LinkedList<MapLocation> locHis = new LinkedList<MapLocation>();
//	public static int[] dirTry = {0,7,1,6,2,5,3,4};
	public static int[] dirTry = {0,7,1,6,2};
	
	public static void moveTowards(MapLocation p) throws GameActionException {
		if (rc.isCoreReady()) {
			Direction d = rc.getLocation().directionTo(p);
			for (int i : dirTry) {
				Direction attempt = Direction.values()[(d.ordinal()+i)%8];
				if (!locHis.contains(rc.getLocation().add(attempt)) && rc.canMove(attempt)) {
					locHis.add(rc.getLocation());
					if (locHis.size() > 5) locHis.removeFirst();
					rc.move(attempt);
					return;
				}
			}
			if (rc.senseRubble(rc.getLocation().add(d)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
				rc.clearRubble(d);
			}
		}
	}
	
	public static String identify(MapLocation[] archons) {
		int d = archons[0].distanceSquaredTo(archons[archons.length-1]);
		int z;
		switch (d) {
			case 289:
				return "6147";
			case 130:
				return "arena";
			case 260:
				return "backbencher";
			case 986:
				return "barred";
			case 0:
				z = firstSpawn();
				switch (z) {
					case 6:
						return "barricade";
					case 4:
						for (Direction c : Direction.values())
							if (c.isDiagonal() && !rc.canMove(c)) return "seamed";
						return "scouting";
					case 8:
						if (!rc.canMove(Direction.NORTH)) return "spiral";
						if (!rc.canMove(Direction.SOUTH)) return "spiral"; 
						return "swamp";
					default:
						return ""+d;
				}
			case 170:
				return "boxy";
			case 32:
				return "boxy_armageddon";
			case 225:
				if (archons.length == 3) return "castle";
				if (archons.length == 2) return "forts";
				return ""+d;
			case 2:
				return "caverns";
			case 442:
				return "chairs";
			case 2738:
				return "channel";
			case 16:
				return "checkers";
			case 841:
				return "closequarters";
			case 98:
				return "collision";
			case 810:
				return "crater";
			case 256:
			case 281:
				if (archons.length == 4) {
					int t = archons[0].y - archons[1].y;
					if (t == 4 || t == -5) return "crumble";
					if (t == 0) return "frogger";  
				}
				return ""+d;
			case 8:
				return "desert";
			case 2601:
				return "diffusion";
			case 193:
				return "factory";
			case 1156:
				return "farm";
			case 89:
			case 185:
				if (archons.length == 4) return "forest";
				if (archons.length == 2) return "molasses";
				return ""+d;
			case 121:
				return "fortifications";
			case 242:
				return "goodies";
			case 90:
				return "helloworld";
			case 49:
				return "industrial";
			case 64:
				return "lockdown";
			case 148:
				return "needlestone";
			case 596:
				return "nexus";
			case 313:
				return "pants";
			case 157:
	    		if (archons.length == 4) return "placard";
	    		if (archons.length == 2) return "zigzag";
				return ""+d;
			case 261:
				return "presents";
			case 9:
				return "prisons";
			case 290:
				return "quadrants";
			case 1874:
				return "quarry";
			case 317:
				return "quartiles";
			case 626:
				return "river";
			case 325:
				return "slow";
			case 2722:
				return "space";
			case 833:
				return "spaghetti";
			case 2116:
				return "streets";
			case 202:
				return "treasure";
			case 625:
				return "tunnels";
			case 1:
				return "turtle";
			case 353:
				return "voluted";
			case 3314:
				return "vortex";
			case 520:
				return "wormy";
			default:
				return ""+d;
		}
	}
	
	private static int firstSpawn() {
		ZombieSpawnSchedule z = rc.getZombieSpawnSchedule();
		return z.getScheduleForRound(z.getRounds()[0])[0].getCount();
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