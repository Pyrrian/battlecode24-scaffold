package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.DirectionService.determineClosestLocationDirection;
import static SimpleFlagCaptureRobot.DirectionService.getRandomLocation;
import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.role;
import static SimpleFlagCaptureRobot.RobotPlayer.spawnRobotIfNeeded;
import static SimpleFlagCaptureRobot.Role.GATHERER;
import static battlecode.common.GameConstants.VISION_RADIUS_SQUARED;

public class GatherService {

    public static void gatherBotLogic(RobotController rc) throws GameActionException {
        while (true) {
            rc.setIndicatorString("Role: " + role);
            if (rc.getRoundNum() > 400) {
                break;
            }
            try {
                if(!spawnRobotIfNeeded(rc)) {

                    if (rc.getRoundNum() > 200) {
                        // If we have more than 20 gatherers, redetermine role.
                        int nGatherers = rc.readSharedArray(GATHERER.getIndex());
                        if (nGatherers > 20) {
                            return;
                        }
                    }
                    MapLocation[] crumbLocations = rc.senseNearbyCrumbs(VISION_RADIUS_SQUARED);
                    MapLocation direction = getRandomLocation(rc);
                    MapLocation location = determineClosestLocationDirection(rc, crumbLocations, direction);
                    moveTowardsGoal(rc, location);



                }
            } catch (Exception e) {

            } finally {
                Clock.yield();
            }
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
