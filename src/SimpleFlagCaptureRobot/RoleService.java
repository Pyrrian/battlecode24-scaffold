package SimpleFlagCaptureRobot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.RobotPlayer.turnCount;
import static SimpleFlagCaptureRobot.Role.BATTLE;
import static SimpleFlagCaptureRobot.Role.GATHERER;
import static SimpleFlagCaptureRobot.Role.SEEKER;

public class RoleService {

    public static Role determineRole(RobotController rc) throws GameActionException {
        // Determine roll counts based on turn count
        // Write role assignments to shared memory. And use shared memory to determine available roles.

        int currentGathers = rc.readSharedArray(GATHERER.getIndex());
        int currentBattle = rc.readSharedArray(BATTLE.getIndex());
        int currentSeekers = rc.readSharedArray(SEEKER.getIndex());

        if(turnCount <= 200) {
            //Mostly gather
            int gatherers = 50;
            return setRole(rc, GATHERER);
        }
        else if(turnCount <= 400) {
            // midgame roles
            int gatherers = 20;
            int seekers = 20;
            int battle = 10;

            if (currentGathers < gatherers) {
                return setRole(rc, GATHERER);
            }
            else if(currentSeekers < seekers) {
                return setRole(rc, SEEKER);
            }
            return setRole(rc, BATTLE);
        }
        else {
            // endgame roles
            int seekers = 25;
            int battle = 25;
            if(currentSeekers < seekers) {
                return setRole(rc, SEEKER);
            }
            return setRole(rc, BATTLE);
        }
    }

    private static Role setRole(RobotController rc, Role role) throws GameActionException {
        int currentBots = rc.readSharedArray(role.getIndex()) + 1;
        rc.writeSharedArray(role.getIndex(), currentBots);
        return role;
    }
}
