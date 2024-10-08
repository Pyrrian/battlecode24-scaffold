package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.ArrayList;

import static SimpleFlagCaptureRobot.DirectionService.getRandomLocation;
import static SimpleFlagCaptureRobot.DirectionService.determineClosestLocationDirection;
import static SimpleFlagCaptureRobot.RobotPlayer.getClosestBotLocation;
import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.performGenericAction;
import static SimpleFlagCaptureRobot.RobotPlayer.role;
import static SimpleFlagCaptureRobot.RobotPlayer.spawnRobotIfNeeded;
import static battlecode.common.GameConstants.VISION_RADIUS_SQUARED;

public class BattleService {

    public static void battleBotLogic(RobotController rc) {
        while (true) {
            rc.setIndicatorString("Role: " + role);

            try {
                if(!spawnRobotIfNeeded(rc)) {

                    RobotInfo[] robots = rc.senseNearbyRobots(VISION_RADIUS_SQUARED, rc.getTeam().opponent());
                    ArrayList<MapLocation> locations = new ArrayList<>();
                    RobotInfo flagCarrier = null;
                    for(RobotInfo info: robots) {
                        locations.add(info.getLocation());
                        if(info.hasFlag()) {
                            flagCarrier = info;
                        }
                    }
                    MapLocation[] array = locations.toArray(new MapLocation[0]);
                    MapLocation randomLocation = getRandomLocation(rc);
                    MapLocation location = determineClosestLocationDirection(rc, array, randomLocation);
                    if (rc.canAttack(location)) {
                        rc.attack(location);
                    } else {
                        if(flagCarrier != null) {
                            moveTowardsGoal(rc, flagCarrier.getLocation());
                        }
                        else {
                            moveTowardsGoal(rc, location);
                        }
                    }

                    if(robots.length == 0) {
                        performGenericAction(rc);
                        return;
                    }
                }
            } catch (GameActionException e) {

            } finally {
                Clock.yield();
            }
        }
    }

}
