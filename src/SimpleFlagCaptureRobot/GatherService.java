package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.RobotPlayer.turnCount;
import static SimpleFlagCaptureRobot.Role.GATHERER;

public class GatherService {

    public static void gatherBotLogic(RobotController rc) throws GameActionException {
        while(true) {

            try {
                //TODO
                // Temporary role
                // Gather resources in first 300-400 turns for sure.
                if(turnCount > 200) {
                    int current = rc.readSharedArray(GATHERER.getIndex());
                    rc.writeSharedArray(GATHERER.getIndex(), current -1);
                    return;
                }

            }
            catch (Exception e) {

            }
            finally {
                Clock.yield();
                turnCount += 1;
            }
        }
    }

}
