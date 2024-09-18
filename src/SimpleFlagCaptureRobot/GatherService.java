package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TrapType;

import static SimpleFlagCaptureRobot.DirectionService.determineClosestLocationDirection;
import static SimpleFlagCaptureRobot.DirectionService.getRandomLocation;
import static SimpleFlagCaptureRobot.DirectionService.whereIsX;
import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.performGenericAction;
import static SimpleFlagCaptureRobot.RobotPlayer.role;
import static SimpleFlagCaptureRobot.RobotPlayer.spawnRobotIfNeeded;
import static SimpleFlagCaptureRobot.Role.FLAG_HIDER;
import static SimpleFlagCaptureRobot.Role.GATHERER;
import static battlecode.common.GameConstants.INTERACT_RADIUS_SQUARED;
import static battlecode.common.GameConstants.VISION_RADIUS_SQUARED;

public class GatherService {

    private static boolean isDoneBuilding = true;

    public static void gatherBotLogic(RobotController rc) throws GameActionException {
        while (true) {
            rc.setIndicatorString("Role: " + role);
            if (rc.getRoundNum() > 300) {
                return;
            }
            try {
                if(!spawnRobotIfNeeded(rc)) {

                    FlagInfo[] flags = rc.senseNearbyFlags(VISION_RADIUS_SQUARED, rc.getTeam());

                    if (rc.getRoundNum() > 200) {
                        // If we have more than 20 gatherers, redetermine role.
                        int nGatherers = rc.readSharedArray(GATHERER.getIndex());
                        if (nGatherers > 20) {
                            return;
                        }
                    }
                    int flagCarriers = rc.readSharedArray(FLAG_HIDER.getIndex());
                    if(flagCarriers < 10) {
                        for (FlagInfo flag : flags) {
                            if (rc.canPickupFlag(flag.getLocation()) && !flag.isPickedUp()) {
                                rc.pickupFlag(flag.getLocation());
                                doNastyFlagProtectionStuff(rc);
                                //early game flag protection logic
                            }
                        }
                    }
                    MapLocation[] crumbLocations = rc.senseNearbyCrumbs(VISION_RADIUS_SQUARED);
                    MapLocation direction = getRandomLocation(rc);
                    MapLocation location = determineClosestLocationDirection(rc, crumbLocations, direction);
                    moveTowardsGoal(rc, location);

                    if(crumbLocations.length == 0) {
                        performGenericAction(rc);
                        return;
                    }

                }
            } catch (Exception e) {

            } finally {
                Clock.yield();
            }
        }
    }


    private static void doNastyFlagProtectionStuff(RobotController rc) {
        isDoneBuilding = false;
        while(!isDoneBuilding) {
            rc.setIndicatorString("Role: " + FLAG_HIDER);

            try {
                int flagCarriers = rc.readSharedArray(FLAG_HIDER.getIndex());
                rc.writeSharedArray(FLAG_HIDER.getIndex(), flagCarriers + 1);
                //First we move to edge of map

                if(rc.hasFlag()) {
                    Direction dir = whereIsX(rc);
                    if (dir.equals(Direction.WEST)) {
                        moveTowardsGoal(rc, new MapLocation(0, rc.getLocation().y));
                    } else if (dir.equals(Direction.EAST)) {
                        moveTowardsGoal(rc, new MapLocation(rc.getMapWidth(), rc.getLocation().y));
                    }
                    FlagInfo[] flags = rc.senseNearbyFlags(36, rc.getTeam());
                    if(flags.length != 1) {
                        //Flags are too close. Move apart.
                        Direction moveAway = Direction.CENTER;

                        for(FlagInfo flag : flags) {
                            if(flag.getLocation().equals(rc.getLocation())) {
                                continue;
                            }
                            else {
                                moveAway = rc.getLocation().directionTo(flag.getLocation()).opposite();
                            }
                        }
                        moveTowardsGoal(rc, rc.getLocation().add(moveAway));

                    }
                    else {
                        if (!checkIfPassable(rc, rc.getLocation()
                                                   .add(dir))) {
                            rc.dropFlag(rc.getLocation());

                        }
                    }
                }
                else {
                    MapInfo[] nearbyLocs = rc.senseNearbyMapInfos(INTERACT_RADIUS_SQUARED);
                    boolean anyDiggableLocationsLeft = false;
                    for(MapInfo loc : nearbyLocs) {
                        if(loc.getMapLocation().equals(rc.getLocation()) || loc.isWater())
                        {
                            continue;
                        }
                        if(rc.canBuild(TrapType.WATER, loc.getMapLocation())) {
                            rc.build(TrapType.WATER, loc.getMapLocation());
                            anyDiggableLocationsLeft = true;
                        }
                    }
                    if (!anyDiggableLocationsLeft) {
                        //We need to suicide when done.
                        isDoneBuilding = true;
                    }
                }
            }
            catch (GameActionException e) {

            }

            finally {
                Clock.yield();
            }
        }
    }

    private static boolean checkIfPassable(RobotController rc, MapLocation location) {
        try {
            return rc.senseMapInfo(location)
                     .isPassable();
        } catch (GameActionException e) {
            return false;
        }
    }

}
