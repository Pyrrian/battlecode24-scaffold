package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.DirectionService.determineClosestLocationDirection;
import static SimpleFlagCaptureRobot.DirectionService.getRandomLocation;
import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.performGenericAction;
import static SimpleFlagCaptureRobot.RobotPlayer.role;
import static SimpleFlagCaptureRobot.RobotPlayer.spawnRobotIfNeeded;
import static SimpleFlagCaptureRobot.Role.FLAG_CARRIER;
import static SimpleFlagCaptureRobot.Role.FLAG_PROTECTOR;
import static SimpleFlagCaptureRobot.RoleService.determineRole;
import static SimpleFlagCaptureRobot.RoleService.setRole;

public class SeekerService {

    public static void flagSeekerLogic(RobotController rc) {
        // Seek flag and switch to carrier or protector after capture.
        while (true) {
            rc.setIndicatorString("Role: " + role);

            try {
                if (!spawnRobotIfNeeded(rc)) {
                    FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
                    if (flags.length != 0) {
                        for(FlagInfo flag : flags) {
                            if(rc.canPickupFlag(flag.getLocation()) && !rc.hasFlag()) {
                                rc.pickupFlag(flag.getLocation());
                            }
                        }
                    }
                    if(rc.hasFlag()) {
                        flagCarrierLogic(rc);
                        return;
                    }

                    int flagCarriers = rc.readSharedArray(FLAG_CARRIER.getIndex());
                    if (flagCarriers < 3) {
                        if (flags.length != 0) {
                            //Move towards enemy flag
                            for (FlagInfo flag : flags) {
                                if (flag.isPickedUp()) {
                                    continue;
                                }
                                moveTowardsGoal(rc, flag.getLocation());
                            }
                        }

                        if (rc.isMovementReady()) {
                            MapLocation direction = getRandomLocation(rc);
                            MapLocation[] broadcastFlags = rc.senseBroadcastFlagLocations();
                            MapLocation location = determineClosestLocationDirection(rc, broadcastFlags, direction);
                            moveTowardsGoal(rc, location);
                        }
                    } else {
                        performGenericAction(rc);
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println("GameActionException");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    public static void flagCarrierLogic(RobotController rc) throws GameActionException {
        int flagCarriers = rc.readSharedArray(FLAG_CARRIER.getIndex());
        rc.writeSharedArray(FLAG_CARRIER.getIndex(), flagCarriers + 1);

        while (true) {
            if (!rc.hasFlag()) {
                rc.writeSharedArray(FLAG_CARRIER.getIndex(), Math.max(rc.readSharedArray(FLAG_CARRIER.getIndex()) - 1, 0));
                return;
            }
            rc.setIndicatorString("Role: " + FLAG_CARRIER);

            try {
                if (rc.isMovementReady()) {
                   MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                   MapLocation closest = determineClosestLocationDirection(rc, spawnLocs, spawnLocs[0]);
                   RobotPlayer.moveTowardsGoal(rc, closest);
                } else {
                    if (!RobotPlayer.targetAndAttackEnemyBot(rc)) {
                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                        MapLocation firstLoc = spawnLocs[0];
                        Direction dir = rc.getLocation()
                                .directionTo(firstLoc);
                        MapInfo mapInfo = rc.senseMapInfo(rc.getLocation().add(dir));
                        if (mapInfo.isWater()) {
                            rc.fill(mapInfo.getMapLocation());
                        }
                    }
                }
            } catch (GameActionException e) {
            } finally {
                Clock.yield();
            }
        }
    }
}
