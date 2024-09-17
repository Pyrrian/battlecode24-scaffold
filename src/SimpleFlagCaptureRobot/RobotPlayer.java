package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static SimpleFlagCaptureRobot.Role.*;

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

    static Integer FLAG_SEEKER_ROLE_INDEX = 1;

    static Integer BATTLE_BOT_ROLE_INDEX = 2;

    static Integer GATHERER_BOT_ROLE_INDEX = 3;

    static Integer CARRIER_BOT_ROLE_INDEX = 4;

    static HashMap<Integer, Integer> roleCounts = new HashMap<>();

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

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

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
                if (!rc.isSpawned()){
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    // Pick a random spawn location to attempt spawning in.
                    MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
                    if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
                }
                else{

                  Role role = determineRole(rc);

                  switch (role) {
                    case SEEKER: flagSeekerLogic(rc);
                    case BATTLE: battleBotLogic(rc);
                    case GATHERER: gatherBotLogic(rc);
                  }

                  // Move and attack randomly if no objective.
                  Direction dir = directions[rng.nextInt(directions.length)];
                  MapLocation nextLoc = rc.getLocation().add(dir);
                  moveTowardsGoal(rc, dir, "Moving random direction");
                  if (rc.canAttack(nextLoc)){
                      rc.attack(nextLoc);
                      System.out.println("Take that! Damaged an enemy that was in our way!");
                  }

                  // Rarely attempt placing traps behind the robot.
                  MapLocation prevLoc = rc.getLocation().subtract(dir);
                  if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1)
                      rc.build(TrapType.EXPLOSIVE, prevLoc);
                  // We can also move our code into different methods or classes to better organize it!
                  updateEnemyRobots(rc);
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

    private static Role determineRole(RobotController rc) throws GameActionException {
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

    private static void flagSeekerLogic(RobotController rc) {
      // Seek flag and switch to carrier or protector after capture.
      while (true) {
        try {
          if (rc.canPickupFlag(rc.getLocation())) {
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");
          }
          // If we are holding an enemy flag, singularly focus on moving towards
          // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
          // to make sure setup phase has ended.
          if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
            MapLocation firstLoc = spawnLocs[0];
            Direction dir = rc.getLocation()
                              .directionTo(firstLoc);
            if (rc.canMove(dir)) rc.move(dir);
          }
          FlagInfo[] flagInfos = rc.senseNearbyFlags(20, rc.getTeam()
                                                           .opponent());
          if (flagInfos.length != 0) {
            //Move towards enemy flag
            for (FlagInfo flag : flagInfos) {
              if (flag.isPickedUp()) {
                continue;
              }
              Direction dir = rc.getLocation()
                                .directionTo(flag.getLocation());
              moveTowardsGoal(rc, dir, "Sensed flag, going there!");
            }
          }

          if (rc.isMovementReady()) {
            MapLocation[] broadcastFlags = rc.senseBroadcastFlagLocations();
            if (broadcastFlags.length != 0) {
              Direction dir = rc.getLocation()
                                .directionTo(broadcastFlags[0]);
              moveTowardsGoal(rc, dir, "Sensed Broadcast flag, going there!");
            }
          }

          MapLocation[] loc = rc.getAllySpawnLocations();
          MapLocation closest = loc[0];
          int maxDistance = Integer.MAX_VALUE;
          for (MapLocation l : loc) {
            if (l.distanceSquaredTo(rc.getLocation()) < maxDistance) {
              maxDistance = l.distanceSquaredTo(rc.getLocation());
              closest = l;
            }
          }
          Direction dir = rc.getLocation()
                            .directionTo(closest)
                            .opposite();
          moveTowardsGoal(rc, dir, "Moving away from spawnlocation");

        }catch (Exception e) {
          System.out.println("GameActionException");
          e.printStackTrace();
        }
        finally {
          Clock.yield();;
          turnCount += 1;
        }
      }
    }

    private static void flagCarrierLogic(RobotController rc) {
      while(true) {

        try {
          //TODO
          // Return home logic
        }
        finally {
          Clock.yield();
          turnCount += 1;
        }
      }
    }

    private static void flagCarrierProtectorLogic(RobotController rc) {
      while(true) {

        try {
          //TODO
          // Stay close to flag carrier, destroy enemy bots near. Fill holes around flag carrier.
        }
        finally {
          Clock.yield();
          turnCount += 1;
        }
      }
    }

    private static void battleBotLogic(RobotController rc) {
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

    private static void gatherBotLogic(RobotController rc) throws GameActionException {
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

    private static void randomBotLogic(RobotController rc) {
      while(true) {

        try {
          // RANDOM
        }
        finally {
          Clock.yield();
          turnCount += 1;
        }
      }
    }


    private static void moveTowardsGoal(RobotController rc, Direction goal, String log) {
      if(!rc.isMovementReady()) {
        return;
      }
      System.out.println(log);
      try {
        if (rc.canMove(goal)) {
          rc.move(goal);
          return;
        }
        if (rc.canMove(goal.rotateLeft())) {
          rc.move(goal.rotateLeft());
          return;
        }
        if(rc.canMove(goal.rotateRight())) {
          rc.move(goal.rotateRight());
        }
      }
      catch (GameActionException gae) {
        return;
      }
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
