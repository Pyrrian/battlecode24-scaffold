package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.Random;

import static SimpleFlagCaptureRobot.BattleService.battleBotLogic;
import static SimpleFlagCaptureRobot.GatherService.gatherBotLogic;
import static SimpleFlagCaptureRobot.RoleService.determineRole;
import static SimpleFlagCaptureRobot.SeekerService.flagSeekerLogic;
import static battlecode.common.GameConstants.ATTACK_RADIUS_SQUARED;
import static battlecode.common.GameConstants.VISION_RADIUS_SQUARED;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    static MapLocation lastLocation;

    static Direction lastDirection;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        turnCount += 1;
        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            ;  // We have now been alive for one more turn!
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // Make sure you spawn your robot in before you attempt to take any actions!
                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
                if(!spawnRobotIfNeeded(rc)) {

                  Role role = determineRole(rc);
                  switch (role) {
                    case SEEKER:
                      flagSeekerLogic(rc);
                    case BATTLE:
                      battleBotLogic(rc);
                    case GATHERER:
                      gatherBotLogic(rc);
                  }
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    public static boolean spawnRobotIfNeeded(RobotController rc) throws GameActionException {
      if (!rc.isSpawned()){
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        // Pick a random spawn location to attempt spawning in.
        MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
        if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
        return true;
      }
      return false;
    }

    public static boolean targetAndAttackEnemyBot(RobotController rc) throws GameActionException {
      RobotInfo[] enemyRobotsInAttackRange = rc.senseNearbyRobots(ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
      if(enemyRobotsInAttackRange.length > 0) {
        MapLocation location = enemyRobotsInAttackRange[0].getLocation();
        if(rc.canAttack(location)) {
          rc.attack(location);
          return true;
        }
      }
      return false;
    }

    public static void moveTowardsGoal(RobotController rc, Direction goal, String log) {
      if(!rc.isMovementReady()) {
        return;
      }
      System.out.println(log);

      try {
        if (rc.canMove(goal)) {
          rc.move(goal);
          lastLocation = rc.getLocation();
          lastDirection = goal;
          return;
        }
        if (rc.canMove(goal.rotateLeft())) {
          rc.move(goal.rotateLeft());
          lastLocation = rc.getLocation();
          lastDirection = goal.rotateLeft();
          return;
        }
        if(rc.canMove(goal.rotateRight())) {
          rc.move(goal.rotateRight());
          lastLocation = rc.getLocation();
          lastDirection = goal.rotateRight();
        }
        if(rc.canMove(goal.rotateRight().rotateRight())){
          rc.move(goal.rotateRight().rotateRight());
          lastLocation = rc.getLocation();
          lastDirection = goal.rotateRight().rotateRight();
          return;
        }
        if(rc.canMove(goal.rotateLeft().rotateLeft())){
          rc.move(goal.rotateLeft().rotateLeft());
          lastLocation = rc.getLocation();
          lastDirection = goal.rotateLeft().rotateLeft();
          return;
        }
      }
      catch (GameActionException gae) {
        return;
      }
    }



    public static void moveTowardsGoal(RobotController rc, MapLocation goal) throws GameActionException {
      //We cannot see the location
      //Lets pathfind to the location we can see that is closest to the goal.
      //We need to find the closest location
      MapInfo[] allWeCanSee = rc.senseNearbyMapInfos(-1);
      MapLocation closest = allWeCanSee[0].getMapLocation();
      int closetDistance = closest.distanceSquaredTo(goal);
      for(MapInfo info: allWeCanSee) {
        int dist = info.getMapLocation().distanceSquaredTo(goal);
        if(dist < closetDistance && info.isPassable()) {
          closetDistance = dist;
          closest = info.getMapLocation();
        }
      }
      Direction direction = rc.getLocation().directionTo(closest);
      if (!isValidDirection(rc, direction)) {
        boolean isValid = false;
        Direction[] directions = {direction.rotateLeft(), direction.rotateRight(), direction.rotateLeft().rotateLeft(), direction.rotateRight().rotateRight()};
        for (Direction dir : directions) {
          if (isValidDirection(rc, dir)) {
            direction = dir;
            isValid = true;
          }
        }
      }

      moveTowardsGoal(rc, direction, "Move closest to goal");

    }

  public static boolean isValidDirection(RobotController rc, Direction direction) throws GameActionException {
    RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(direction));
    return rc.senseMapInfo(rc.getLocation().add(direction)).isPassable() && robot == null && (lastDirection == null || direction != lastDirection.opposite());
  }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)){
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }
}
