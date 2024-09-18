package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.GlobalUpgrade;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.SkillType;
import battlecode.common.TrapType;
import battlecode.world.Trap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static SimpleFlagCaptureRobot.BattleService.battleBotLogic;
import static SimpleFlagCaptureRobot.DirectionService.determineClosestLocationDirection;
import static SimpleFlagCaptureRobot.DirectionService.getRandomLocation;
import static SimpleFlagCaptureRobot.GatherService.gatherBotLogic;
import static SimpleFlagCaptureRobot.Role.GATHERER;
import static SimpleFlagCaptureRobot.RoleService.determineRole;
import static SimpleFlagCaptureRobot.SeekerService.flagSeekerLogic;
import static battlecode.common.GameConstants.ATTACK_RADIUS_SQUARED;
import static battlecode.common.GameConstants.DEFAULT_HEALTH;

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
    static Role role = Role.GATHERER;

    static Set<MapLocation> lastLocation = new HashSet<>();

    static Direction lastDirection;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /**
     * Array containing all the possible movement directions.
     */
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
     * @param rc The RobotController object. You use it to perform actions from this robot, and to get
     *           information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(GATHERER.getIndex()) == 0) {
            rc.writeSharedArray(GATHERER.getIndex(), 50);
        }

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.
            if (rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) {
                rc.buyGlobal(GlobalUpgrade.CAPTURING);
            } else if (rc.canBuyGlobal(GlobalUpgrade.ATTACK)) {
                rc.buyGlobal(GlobalUpgrade.ATTACK);
            } else if (rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
                rc.buyGlobal(GlobalUpgrade.HEALING);
            }
            ;  // We have now been alive for one more turn!
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // Make sure you spawn your robot in before you attempt to take any actions!
                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
                if (!spawnRobotIfNeeded(rc)) {

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
        if (!rc.isSpawned()) {
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
        if (enemyRobotsInAttackRange.length > 0) {
            MapLocation location = getClosestBotLocation(rc, enemyRobotsInAttackRange);
            if (rc.canAttack(location)) {
                rc.attack(location);
                return true;
            }
        }
        return false;
    }

    public static boolean dropTrap(RobotController rc) throws GameActionException {
        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(lastDirection.opposite());
        TrapType[] trapTypes = {TrapType.EXPLOSIVE, TrapType.STUN};
        TrapType randomTrap = trapTypes[rng.nextInt(trapTypes.length)];
        if (rc.canBuild(randomTrap, prevLoc) && rng.nextInt() % 37 == 1) {
            rc.build(randomTrap, prevLoc);
            return true;
        }
        return false;
    }

    public static MapLocation getClosestBotLocation(RobotController rc, RobotInfo[] enemyRobotsInAttackRange) throws GameActionException {
        ArrayList<MapLocation> locations = new ArrayList<>();
        for (RobotInfo info : enemyRobotsInAttackRange) {
            locations.add(info.getLocation());
        }
        MapLocation[] array = locations.toArray(new MapLocation[0]);
        MapLocation randomLocation = getRandomLocation(rc);
        return determineClosestLocationDirection(rc, array, randomLocation);
    }

    public static boolean healAllyBot(RobotController rc) throws GameActionException {
        RobotInfo[] allyRobotsInAttackRange = rc.senseNearbyRobots(ATTACK_RADIUS_SQUARED, rc.getTeam());
        ArrayList<RobotInfo> injuredRobots = new ArrayList<>();
        if (allyRobotsInAttackRange.length > 0) {
            for (RobotInfo robot : allyRobotsInAttackRange) {
                if (robot.health < DEFAULT_HEALTH) {
                    injuredRobots.add(robot);
                }
            }
            RobotInfo[] injuredRobotsArray = injuredRobots.toArray(new RobotInfo[0]);
            MapLocation location = getClosestBotLocation(rc, injuredRobotsArray);
            if (rc.canHeal(location)) {
                rc.heal(location);
                return true;
            }
        }
        return false;
    }

    private static void moveTowardsGoal(RobotController rc, Direction goal, String log) {
        if (!rc.isMovementReady()) {
            return;
        }
        try {
            rc.move(goal);
            lastLocation.add(rc.getLocation());
            lastDirection = goal;
        } catch (GameActionException gae) {
        }
    }

    public static void moveTowardsGoal(RobotController rc, MapLocation goal) throws GameActionException {
        Direction direction = weightedDirectionChoice(rc, goal);
        moveTowardsGoal(rc, direction, "Move closest to goal");
    }

    public static Direction weightedDirectionChoice(RobotController rc, MapLocation goal) throws GameActionException {
        Direction bestDir = Direction.NORTH;
        int maxPoints = 0;
        Direction goalDirection = rc.getLocation().directionTo(goal);

        for (Direction dir : directions) {
            int points = 0;

            MapLocation targetLocation = rc.getLocation().add(dir);

            if (checkIfRobotIsAtLocation(rc, targetLocation)) {
                points = Integer.MIN_VALUE;
            }
            if (!checkIfPassable(rc, targetLocation) && (!checkIfWater(rc, targetLocation))) {
                points = Integer.MIN_VALUE;
            }
            if (checkIfWater(rc, targetLocation)) {
                points -= 4;
            }
            if (dir.equals(goalDirection)) {
                points += 7;
            }
            if (dir.equals(goalDirection.rotateLeft()) | dir.equals(goalDirection.rotateRight())) {
                points += 5;
            }
            if (dir.equals(goalDirection.rotateLeft().rotateLeft()) | dir.equals(goalDirection.rotateRight().rotateRight())) {
                points += 3;
            }
            if (dir.equals(goalDirection.rotateLeft().rotateLeft().rotateLeft()) | dir.equals(goalDirection.rotateRight().rotateRight().rotateRight())) {
                points += 1;
            }
            if (checkIfPassable(rc, targetLocation.add(dir))) {
                points += 2;
                if (checkIfPassable(rc, targetLocation.add(dir).add(dir))) {
                    points += 2;
                }
            }

            if (lastDirection != null && dir.equals(lastDirection.opposite())) {
                points -= 5;
            }
            if (lastLocation.contains(targetLocation)) {
                points -= 5;
            }
            if (points > maxPoints) {
                maxPoints = points;
                bestDir = dir;
            }
        }
        if (checkIfWater(rc, rc.getLocation().add(bestDir))) {
            rc.fill(rc.getLocation().add(bestDir));
        }
        return bestDir;
    }

    private static boolean checkIfRobotIsAtLocation(RobotController rc, MapLocation location) {
        try {
            return rc.senseRobotAtLocation(location) != null;
        } catch (GameActionException e) {
            return false;
        }
    }

    private static boolean checkIfPassable(RobotController rc, MapLocation location) {
        try {
            return rc.senseMapInfo(location)
                    .isPassable();
        } catch (GameActionException e) {
            return false;
        }
    }

    private static boolean checkIfWater(RobotController rc, MapLocation location) {
        try {
            return rc.senseMapInfo(location)
                    .isWater();
        } catch (GameActionException e) {
            return false;
        }
    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException {
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0) {
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++) {
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)) {
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }

    public static void performGenericAction(RobotController rc) throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        if (flags.length > 0 && rc.canPickupFlag(rc.getLocation())) {
            rc.pickupFlag(rc.getLocation());
            MapLocation closestSpawnLocation = determineClosestLocationDirection(rc, rc.getAllySpawnLocations(), getRandomLocation(rc));
            moveTowardsGoal(rc, closestSpawnLocation);
            return;
        }
        RobotInfo myself = rc.senseRobot(rc.getID());

        if (myself.attackLevel <= 3 && myself.healLevel <= 3 && myself.buildLevel <= 3) {
            performRandomAction(rc);
            return;
        }

        SkillType bestSkill = determineBestSkill(myself);
        switch (bestSkill) {
            case ATTACK:
                if (targetAndAttackEnemyBot(rc)) {
                    return;
                } else {
                    performRandomAction(rc);
                    break;
                }
            case HEAL:
                if (healAllyBot(rc)) {
                    return;
                } else {
                    performRandomAction(rc);
                    break;
                }
            case BUILD:
                dropTrap(rc);
                break;
        }
        moveTowardsGoal(rc, getRandomLocation(rc));
    }

    private static void performRandomAction(RobotController rc) throws GameActionException {
        SkillType[] skillTypes = SkillType.values();
        SkillType randomSkill = skillTypes[rng.nextInt(skillTypes.length)];

        switch (randomSkill) {
            case ATTACK:
                if (targetAndAttackEnemyBot(rc)) {
                    break;
                } else {
                    randomSkill = SkillType.HEAL;
                }
            case HEAL:
                if (healAllyBot(rc)) {
                    break;
                } else {
                    randomSkill = SkillType.BUILD;
                }
            case BUILD:
                dropTrap(rc);
                break;
        }
    }

    private static SkillType determineBestSkill(RobotInfo myself) {
        SkillType bestSkill = null;
        int bestLevel = 0;
        for (SkillType skill : SkillType.values()) {
            switch (skill) {
                case ATTACK:
                    if (myself.attackLevel > bestLevel) {
                        bestSkill = skill;
                        bestLevel = myself.attackLevel;
                    }
                    break;
                case HEAL:
                    if (myself.healLevel > bestLevel) {
                        bestSkill = skill;
                        bestLevel = myself.healLevel;
                    }
                    break;
                case BUILD:
                    if (myself.buildLevel > bestLevel) {
                        bestSkill = skill;
                        bestLevel = myself.buildLevel;
                    }
                    break;
            }
        }
        return bestSkill;
    }
}
