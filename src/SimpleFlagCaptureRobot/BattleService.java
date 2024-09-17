package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.RobotPlayer.turnCount;

public class BattleService {

    public static void battleBotLogic(RobotController rc) {
        while(true) {

            try {
                //TODO
                // Attack enemies and set traps?
                // Search and destroy enemy flag carriers
            }
            finally {
                Clock.yield();
                turnCount += 1;
            }
        }
    }

}
