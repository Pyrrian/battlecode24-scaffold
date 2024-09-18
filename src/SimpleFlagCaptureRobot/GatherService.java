package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.DirectionService.determineClosestLocationDirection;
import static SimpleFlagCaptureRobot.DirectionService.getRandomLocation;
import static SimpleFlagCaptureRobot.DirectionService.whereIsX;
import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.performGenericAction;
import static SimpleFlagCaptureRobot.RobotPlayer.role;
import static SimpleFlagCaptureRobot.RobotPlayer.spawnRobotIfNeeded;
import static SimpleFlagCaptureRobot.Role.FLAG_HIDER;
import static SimpleFlagCaptureRobot.Role.GATHERER;
import static battlecode.common.GameConstants.VISION_RADIUS_SQUARED;

public class GatherService {

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
                    for(FlagInfo flag : flags) {
                        if(rc.canPickupFlag(flag.getLocation()) && !flag.isPickedUp()) {
                            rc.pickupFlag(flag.getLocation());
                            doNastyFlagProtectionStuff(rc);
                            //early game flag protection logic
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
        while(true) {
            rc.setIndicatorString("Role: " + FLAG_HIDER);
            try {
                //First we move to edge of map

                if(rc.hasFlag()) {
                    Direction dir = whereIsX(rc);
                    if (dir.equals(Direction.WEST)) {
                        moveTowardsGoal(rc, new MapLocation(0, rc.getLocation().y));
                    } else if (dir.equals(Direction.EAST)) {
                        moveTowardsGoal(rc, new MapLocation(rc.getMapWidth(), rc.getLocation().y));
                    }

                    if (!checkIfPassable(rc, rc.getLocation()
                                               .add(dir))) {
                        rc.dropFlag(rc.getLocation());
                    }
                }
                else {
                    //MapInfo nearbyLocs = rc.senseNearbyMapInfos(2);

                }

                //we drop the flag

                //Then we dig in around us.

                //We need to suicide when done.




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



//    private static Direction directionToAllySpawnLocation(RobotController rc) {
//        MapLocation[] loc = rc.getAllySpawnLocations();
//        MapLocation closest = loc[0];
//        int maxDistance = Integer.MAX_VALUE;
//        for (MapLocation l : loc) {
//            if (l.distanceSquaredTo(rc.getLocation()) < maxDistance) {
//                maxDistance = l.distanceSquaredTo(rc.getLocation());
//                closest = l;
//            }
//        }
//        return rc.getLocation().directionTo(closest);
//    }
//
//    private static Direction getCrumbDirection(RobotController rc) throws GameActionException {
//        int radius = 1;
//        MapLocation[] crumbs;
//        do {
//            crumbs = rc.senseNearbyCrumbs(radius);
//            radius++;
//        } while (crumbs.length == 0);
//        return rc.getLocation().directionTo(crumbs[0]);
//    }
//
//    private static Direction moveToMiddle(RobotController rc) throws GameActionException {
//        Direction direction = directionToAllySpawnLocation(rc); // towards your own spawn location
//        if (rc.senseMapInfo(rc.getLocation()).getTeamTerritory() == rc.getTeam()) { // if you're in your own territory
//            direction = direction.opposite();
//        }
//        if (!isValidDirection(rc, direction)) {
//            Direction[] directions = {direction.rotateLeft(), direction.rotateRight(), direction.rotateLeft().rotateLeft(), direction.rotateRight().rotateRight()};
//            for (Direction dir : directions) {
//                if (isValidDirection(rc, dir)) {
//                    return dir;
//                }
//            }
//            rc.fill(rc.getLocation().add(direction));
//        }
//        return direction;
//    }

}
