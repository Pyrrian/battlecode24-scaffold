package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.turnCount;

public class SeekerService {

    public static void flagSeekerLogic(RobotController rc) {
        // Seek flag and switch to carrier or protector after capture.
        while (true) {

            try {
                if (rc.canPickupFlag(rc.getLocation())) {
                    rc.pickupFlag(rc.getLocation());
                    rc.setIndicatorString("Holding a flag!");
                }
                // If we are holding an enemy flag, singularly focus on moving towards
                // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
                // to make sure setup phase has ended.
                if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    MapLocation firstLoc = spawnLocs[0];
                    Direction dir = rc.getLocation()
                            .directionTo(firstLoc);
                    if (rc.canMove(dir)) rc.move(dir);
                }
                FlagInfo[] flagInfos = rc.senseNearbyFlags(20, rc.getTeam()
                        .opponent());
                if (flagInfos.length != 0) {
                    //Move towards enemy flag
                    for (FlagInfo flag : flagInfos) {
                        if (flag.isPickedUp()) {
                            continue;
                        }
                        Direction dir = rc.getLocation()
                                .directionTo(flag.getLocation());
                        moveTowardsGoal(rc, dir, "Sensed flag, going there!");
                    }
                }

                if (rc.isMovementReady()) {
                    MapLocation[] broadcastFlags = rc.senseBroadcastFlagLocations();
                    if (broadcastFlags.length != 0) {
                        Direction dir = rc.getLocation()
                                .directionTo(broadcastFlags[0]);
                        moveTowardsGoal(rc, dir, "Sensed Broadcast flag, going there!");
                    }
                }

                MapLocation[] loc = rc.getAllySpawnLocations();
                MapLocation closest = loc[0];
                int maxDistance = Integer.MAX_VALUE;
                for (MapLocation l : loc) {
                    if (l.distanceSquaredTo(rc.getLocation()) < maxDistance) {
                        maxDistance = l.distanceSquaredTo(rc.getLocation());
                        closest = l;
                    }
                }
                Direction dir = rc.getLocation()
                        .directionTo(closest)
                        .opposite();
                moveTowardsGoal(rc, dir, "Moving away from spawnlocation");

            }catch (Exception e) {
                System.out.println("GameActionException");
                e.printStackTrace();
            }
            finally {
                Clock.yield();
                turnCount += 1;
            }
        }
    }

    private static void flagCarrierLogic(RobotController rc) {
        while(true) {

            try {
                //TODO
                // Return home logic
            }
            finally {
                Clock.yield();
                turnCount += 1;
            }
        }
    }

    private static void flagCarrierProtectorLogic(RobotController rc) {
        while(true) {

            try {
                //TODO
                // Stay close to flag carrier, destroy enemy bots near. Fill holes around flag carrier.
            }
            finally {
                Clock.yield();
                turnCount += 1;
            }
        }
    }


}
