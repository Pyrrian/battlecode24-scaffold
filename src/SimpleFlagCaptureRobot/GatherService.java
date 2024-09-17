package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.ArrayList;

import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.turnCount;
import static SimpleFlagCaptureRobot.Role.GATHERER;
import static battlecode.common.GameConstants.VISION_RADIUS_SQUARED;

public class GatherService {

    public static void gatherBotLogic(RobotController rc) throws GameActionException {
        while (true) {
            if (turnCount > 400) {
                break;
            }
            try {
                if (turnCount > 200) {
                    // If we have more than 20 gatherers, redetermine role.
                    int nGatherers = rc.readSharedArray(GATHERER.getIndex());
                    if (nGatherers > 20) {
                        rc.writeSharedArray(GATHERER.getIndex(), nGatherers - 1);
                        break;
                    }
                }

//                System.out.println("Gatherer logic - turn: " + turnCount);


                MapLocation[] crumbLocations = rc.senseNearbyCrumbs(VISION_RADIUS_SQUARED);
                Direction direction = getRandomDirection(rc);
                direction = moveToCrumb(rc, crumbLocations, direction);
                if (!isValidDirection(rc, direction)) {
                    boolean isValid = false;
                    Direction[] directions = {direction.rotateLeft(), direction.rotateRight(), direction.rotateLeft().rotateLeft(), direction.rotateRight().rotateRight()};
                    for (Direction dir : directions) {
                        if (isValidDirection(rc, dir)) {
                            direction = dir;
                            isValid = true;
                        }
                    }
//                    if (!isValid) {
//                        rc.fill(rc.getLocation().add(direction));
//                    }
                }
                moveTowardsGoal(rc, direction, "Move towards crumb");
            } catch (Exception e) {

            } finally {
                Clock.yield();
                turnCount += 1;
            }
        }
    }

    private static Direction moveToCrumb(RobotController rc, MapLocation[] crumbLocations, Direction direction) {
        if (crumbLocations.length > 0) {
            MapLocation closest = crumbLocations[0];
            int maxDistance = Integer.MAX_VALUE;
            for (MapLocation crumb : crumbLocations) {
                if (crumb.distanceSquaredTo(rc.getLocation()) < maxDistance) {
                    maxDistance = crumb.distanceSquaredTo(rc.getLocation());
                    closest = crumb;
                }
            }
            direction = rc.getLocation().directionTo(closest);
        }
        return direction;
    }

    private static Direction getRandomDirection(RobotController rc) {
        ArrayList<Direction> directions = new ArrayList<>();
        directions.add(whereIsX(rc).opposite());
        directions.add(whereIsY(rc).opposite());
        return directions.get((int) (Math.random() * 2));
    }

    private static Direction whereIsX(RobotController rc) {
        // stel start: x=3,y=23 -> x=44, y=30 22

        int x = rc.getLocation().x;
        int half = rc.getMapWidth() / 2;
        if (x > half) {
            return Direction.EAST;
        } else {
            return Direction.WEST;
        }
    }

    private static Direction whereIsY(RobotController rc) {
        // stel start: x=3,y=23 -> x=44, y=30 22

        int y = rc.getLocation().y;
        int half = rc.getMapHeight() / 2;
        if (y > half) {
            return Direction.NORTH;
        } else {
            return Direction.SOUTH;
        }
    }

    private static boolean isValidDirection(RobotController rc, Direction direction) throws GameActionException {
        RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(direction));
        return rc.senseMapInfo(rc.getLocation().add(direction)).isPassable() && robot == null;
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
