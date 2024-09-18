package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.ArrayList;

import static SimpleFlagCaptureRobot.DirectionService.getRandomLocation;
import static SimpleFlagCaptureRobot.DirectionService.determineClosestLocationDirection;
import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.role;
import static SimpleFlagCaptureRobot.RobotPlayer.spawnRobotIfNeeded;
import static battlecode.common.GameConstants.VISION_RADIUS_SQUARED;

public class BattleService {

    public static void battleBotLogic(RobotController rc) {
        while (true) {
            rc.setIndicatorString("Role: " + role);

            try {
                if(!spawnRobotIfNeeded(rc)) {

                    //TODO
                    // Attack enemies and set traps?
                    // Search and destroy enemy flag carriers

                    RobotInfo[] robots = rc.senseNearbyRobots(VISION_RADIUS_SQUARED, rc.getTeam().opponent());
                    ArrayList<MapLocation> locations = new ArrayList<>();
                    for(RobotInfo info: robots) {
                        locations.add(info.getLocation());
                    }
                    MapLocation[] array = locations.toArray(new MapLocation[0]);
                    MapLocation randomLocation = getRandomLocation(rc);
                    MapLocation location = determineClosestLocationDirection(rc, array, randomLocation);
                    if (rc.canAttack(location)) {
                        rc.attack(location);
                        System.out.println("Take that! Damaged an enemy that was in our way!");
                    } else {
                        moveTowardsGoal(rc, location);
                    }
                }
            } catch (GameActionException e) {
                throw new RuntimeException(e);
            } finally {
                Clock.yield();
            }
        }
    }

}
