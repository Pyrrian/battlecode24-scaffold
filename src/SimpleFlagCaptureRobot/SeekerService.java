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
import static SimpleFlagCaptureRobot.DirectionService.getRandomDirection;
import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.role;
import static SimpleFlagCaptureRobot.RobotPlayer.spawnRobotIfNeeded;
import static SimpleFlagCaptureRobot.Role.FLAG_CARRIER;
import static SimpleFlagCaptureRobot.Role.FLAG_PROTECTOR;
import static SimpleFlagCaptureRobot.RoleService.setRole;

public class SeekerService {

    public static void flagSeekerLogic(RobotController rc) {
        // Seek flag and switch to carrier or protector after capture.
        while (true) {
            rc.setIndicatorString("Role: " + role);

            try {
                if (!spawnRobotIfNeeded(rc)) {
                    FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam()
                            .opponent());
                    if (flags.length != 0 && rc.canPickupFlag(rc.getLocation())) {
                        flagCarrierLogic(rc);
                    }

                    int flagCarriers = rc.readSharedArray(FLAG_CARRIER.getIndex());
                    if (flagCarriers >= 3) {
                        int seekers = rc.readSharedArray(Role.SEEKER.getIndex());
                        rc.writeSharedArray(Role.SEEKER.getIndex(), seekers - 1);
                        int flagProtectors = rc.readSharedArray(Role.FLAG_PROTECTOR.getIndex());
                        rc.writeSharedArray(Role.FLAG_PROTECTOR.getIndex(), flagProtectors + 1);
                        flagCarrierProtectorLogic(rc);
                    }

                    FlagInfo[] flagInfos = rc.senseNearbyFlags(20, rc.getTeam()
                            .opponent());
                    if (flagInfos.length != 0) {
                        //Move towards enemy flag
                        for (FlagInfo flag : flagInfos) {
                            if (flag.isPickedUp()) {
                                continue;
                            }
                            moveTowardsGoal(rc, flag.getLocation());
                        }
                    }

                    if (rc.isMovementReady()) {
                        Direction direction = getRandomDirection(rc);
                        MapLocation[] broadcastFlags = rc.senseBroadcastFlagLocations();
                        MapLocation location = determineClosestLocationDirection(rc, broadcastFlags, direction);
                        moveTowardsGoal(rc, location);
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
        setRole(rc, FLAG_CARRIER);
        rc.pickupFlag(rc.getLocation());
        rc.setIndicatorString("Holding a flag!");
        int seekers = rc.readSharedArray(Role.SEEKER.getIndex());
        rc.writeSharedArray(Role.SEEKER.getIndex(), seekers - 1);
        int flagCarriers = rc.readSharedArray(FLAG_CARRIER.getIndex());
        rc.writeSharedArray(FLAG_CARRIER.getIndex(), flagCarriers + 1);

        while (true) {
            rc.setIndicatorString("Role: " + role);

            try {
                if (rc.isMovementReady()) {
                    // If we are holding an enemy flag, singularly focus on moving towards
                    // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
                    // to make sure setup phase has ended.
                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                        int distance = Integer.MAX_VALUE;
                        MapLocation targetLocation = spawnLocs[0];
                        for (MapLocation ml : spawnLocs) {
                            if (rc.getLocation().distanceSquaredTo(ml) < distance) {
                                distance = rc.getLocation().distanceSquaredTo(ml);
                                targetLocation = ml;
                            }
                        }
                        RobotPlayer.moveTowardsGoal(rc, targetLocation);
                    }
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

    private static void flagCarrierProtectorLogic(RobotController rc) throws GameActionException {
        setRole(rc, FLAG_PROTECTOR);

        while (true) {
            rc.setIndicatorString("Role: " + role);

            try {
                //TODO
                // Stay close to flag carrier, destroy enemy bots near. Fill holes around flag carrier.
            } finally {
                Clock.yield();
            }
        }
    }


}
