package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.Arrays;

import static SimpleFlagCaptureRobot.DirectionService.changeDirectionIfNeeded;
import static SimpleFlagCaptureRobot.DirectionService.getRandomDirection;
import static SimpleFlagCaptureRobot.DirectionService.moveToClosestLocation;
import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;

public class BattleService {

    public static void battleBotLogic(RobotController rc) {
        while (true) {

            try {
                //TODO
                // Attack enemies and set traps?
                // Search and destroy enemy flag carriers

                RobotInfo[] robots = rc.senseNearbyRobots();
                MapLocation[] robotLocations = (MapLocation[]) Arrays.stream(robots).map(r -> new MapLocation(r.getLocation().x, r.getLocation().y)).toArray();
                Direction direction = getRandomDirection(rc);
                direction = moveToClosestLocation(rc, robotLocations, direction);
                direction = changeDirectionIfNeeded(rc, direction);
                MapLocation location = rc.getLocation().add(direction);
                if (rc.canAttack(location)) {
                    rc.attack(location);
                    System.out.println("Take that! Damaged an enemy that was in our way!");
                }
                moveTowardsGoal(rc, direction, "Move towards enemy");
            } catch (GameActionException e) {
                throw new RuntimeException(e);
            } finally {
                Clock.yield();
            }
        }
    }

}
