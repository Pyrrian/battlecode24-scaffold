package SimpleFlagCaptureRobot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.ArrayList;

import static SimpleFlagCaptureRobot.RobotPlayer.lastDirection;

public class DirectionService {

    public static MapLocation determineClosestLocationDirection(RobotController rc, MapLocation[] locations, Direction direction) {
        if (locations.length > 0) {
            MapLocation closest = locations[0];
            int maxDistance = Integer.MAX_VALUE;
            for (MapLocation loc : locations) {
                if (loc.distanceSquaredTo(rc.getLocation()) < maxDistance) {
                    maxDistance = loc.distanceSquaredTo(rc.getLocation());
                    closest = loc;
                }
            }
            return closest;
        }
        return rc.getLocation().add(direction);
    }

    public static Direction getRandomDirection(RobotController rc) {
        ArrayList<Direction> directions = new ArrayList<>();
        directions.add(whereIsX(rc).opposite());
        directions.add(whereIsY(rc).opposite());
        return directions.get((int) (Math.random() * 2));
    }

    public static Direction whereIsX(RobotController rc) {
        // stel start: x=3,y=23 -> x=44, y=30 22

        int x = rc.getLocation().x;
        int half = rc.getMapWidth() / 2;
        if (x > half) {
            return Direction.EAST;
        } else {
            return Direction.WEST;
        }
    }

    public static Direction whereIsY(RobotController rc) {
        // stel start: x=3,y=23 -> x=44, y=30 22

        int y = rc.getLocation().y;
        int half = rc.getMapHeight() / 2;
        if (y > half) {
            return Direction.NORTH;
        } else {
            return Direction.SOUTH;
        }
    }

//    public static Direction changeDirectionIfNeeded(RobotController rc, Direction direction) throws GameActionException {
//        if (!isValidDirection(rc, direction)) {
//            boolean isValid = false;
//            Direction[] directions = {direction.rotateLeft(), direction.rotateRight(), direction.rotateLeft().rotateLeft(), direction.rotateRight().rotateRight()};
//            for (Direction dir : directions) {
//                if (isValidDirection(rc, dir)) {
//                    direction = dir;
//                    isValid = true;
//                }
//            }
//            if (!isValid) {
//                rc.fill(rc.getLocation().add(direction));
//            }
//        }
//        return direction;
//    }

    public static boolean isValidDirection(RobotController rc, Direction direction) throws GameActionException {
        RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(direction));
        return rc.senseMapInfo(rc.getLocation().add(direction)).isPassable() && robot == null && (lastDirection == null || direction != lastDirection.opposite());
    }

    public static Direction towardsMiddle(RobotController rc) {

        int midHeight = rc.getMapHeight()/2;
        int midWidth = rc.getMapWidth()/2;
        int x = rc.getLocation().x;
        int y = rc.getLocation().y;

        if(x < midWidth) {
            if(y < midHeight) {
                return Direction.NORTHEAST;
            }
            return Direction.SOUTHEAST;
        }
        else if(x == midWidth) {
            if(y < midHeight) {
                return Direction.NORTH;
            }
            else {
                return Direction.SOUTH;
            }
        }
        else {
            if(y < midHeight) {
                return Direction.NORTHWEST;
            }
            return Direction.SOUTHWEST;
        }
    }
}
