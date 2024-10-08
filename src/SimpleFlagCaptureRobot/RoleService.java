package SimpleFlagCaptureRobot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.Role.BATTLE;
import static SimpleFlagCaptureRobot.Role.GATHERER;
import static SimpleFlagCaptureRobot.Role.SEEKER;
import static SimpleFlagCaptureRobot.RobotPlayer.role;

public class RoleService {

    public static Role determineRole(RobotController rc) throws GameActionException {
        // Determine roll counts based on turn count
        // Write role assignments to shared memory. And use shared memory to determine available roles.

        int currentGathers = rc.readSharedArray(GATHERER.getIndex());
        int currentBattle = rc.readSharedArray(BATTLE.getIndex());
        int currentSeekers = rc.readSharedArray(SEEKER.getIndex());

        if(rc.hasFlag()) {
            setRole(rc, SEEKER);
            return role;
        }

        if(rc.getRoundNum() < 200) {
            return role;
        }

        int nRoles = rc.readSharedArray(role.getIndex());
        rc.writeSharedArray(role.getIndex(), Math.max(nRoles - 1, 0));

        if(rc.getRoundNum() <= 250) {
            // midgame roles
            int gatherers = 5;
            int seekers = 35;
            int battle = 10;

            if (currentGathers <= gatherers) {
                return setRole(rc, GATHERER);
            }
            else if(currentSeekers < seekers) {
                return setRole(rc, SEEKER);
            }
            return setRole(rc, BATTLE);
        }
        else {
            // endgame roles
            int seekers = 35;
            int battle = 15;
            if(currentSeekers < seekers) {
                return setRole(rc, SEEKER);
            }
            return setRole(rc, BATTLE);
        }
    }

    public static Role setRole(RobotController rc, Role newRole) throws GameActionException {
        int currentBots = rc.readSharedArray(newRole.getIndex()) + 1;
        rc.writeSharedArray(newRole.getIndex(), currentBots);
        role = newRole;
        return newRole;
    }
}
