package SimpleFlagCaptureRobot;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static SimpleFlagCaptureRobot.RobotPlayer.moveTowardsGoal;
import static SimpleFlagCaptureRobot.RobotPlayer.spawnRobotIfNeeded;

public class SeekerService {

    public static void flagSeekerLogic(RobotController rc) {
        // Seek flag and switch to carrier or protector after capture.
        while (true) {
            try {
                if(!spawnRobotIfNeeded(rc)) {
                    FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam()
                                                                 .opponent());
                    if (flags.length != 0 && rc.canPickupFlag(rc.getLocation())) {
                        rc.pickupFlag(rc.getLocation());
                        rc.setIndicatorString("Holding a flag!");
                        int seekers = rc.readSharedArray(Role.SEEKER.getIndex());
                        rc.writeSharedArray(Role.SEEKER.getIndex(), seekers - 1);
                        int flagCarriers = rc.readSharedArray(Role.FLAG_CARRIER.getIndex());
                        rc.writeSharedArray(Role.FLAG_CARRIER.getIndex(), flagCarriers + 1);
                        flagCarrierLogic(rc);
                    }

                    int flagCarriers = rc.readSharedArray(Role.FLAG_CARRIER.getIndex());
                    if (flagCarriers >= 3) {
                        int seekers = rc.readSharedArray(Role.SEEKER.getIndex());
                        rc.writeSharedArray(Role.SEEKER.getIndex(), seekers - 1);
                        int flagProtectors = rc.readSharedArray(Role.FLAG_PROTECTOR.getIndex());
                        rc.writeSharedArray(Role.FLAG_PROTECTOR.getIndex(), flagProtectors + 1);
                        flagCarrierProtectorLogic(rc);
                    }

                    FlagInfo[] flagInfos = rc.senseNearbyFlags(20, rc.getTeam()
                                                                     .opponent());
                    if (flagInfos.length != 0) {
                        //Move towards enemy flag
                        for (FlagInfo flag : flagInfos) {
                            if (flag.isPickedUp()) {
                                continue;
                            }
                            moveTowardsGoal(rc, flag.getLocation());
                        }
                    }

                    if (rc.isMovementReady()) {
                        MapLocation[] broadcastFlags = rc.senseBroadcastFlagLocations();
                        if (broadcastFlags.length != 0) {
                            moveTowardsGoal(rc, broadcastFlags[0]);
                        }
                    }
                    moveTowardsGoal(rc, towardsMiddle(rc), "Moving away from spawnlocation");

                }
            }catch (Exception e) {
                System.out.println("GameActionException");
                e.printStackTrace();
            }
            finally {
                Clock.yield();}
        }
    }

    private static Direction towardsMiddle(RobotController rc) {

        int midHeight = rc.getMapHeight()/2;
        int midWidth = rc.getMapWidth()/2;
        int x = rc.getLocation().x;
        int y = rc.getLocation().y;

        if(x < midWidth) {
            if(y < midHeight) {
                return Direction.NORTHEAST;
            }
            return Direction.SOUTHEAST;
        }
        else if(x == midWidth) {
            if(y < midHeight) {
                return Direction.NORTH;
            }
            else {
                return Direction.SOUTH;
            }
        }
        else {
            if(y < midHeight) {
                return Direction.NORTHWEST;
            }
            return Direction.SOUTHWEST;
        }



    }

    private static void flagCarrierLogic(RobotController rc) {
        while(true) {

            try {
                if(rc.isMovementReady()) {
                    // If we are holding an enemy flag, singularly focus on moving towards
                    // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
                    // to make sure setup phase has ended.
                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                        int distance = Integer.MAX_VALUE;
                        MapLocation targetLocation = spawnLocs[0];
                        for(MapLocation ml : spawnLocs) {
                            if(rc.getLocation().distanceSquaredTo(ml) < distance) {
                                distance = rc.getLocation().distanceSquaredTo(ml);
                                targetLocation = ml;
                            }
                        }
                        RobotPlayer.moveTowardsGoal(rc, targetLocation);
                    }
                }
                else {
                    if(!RobotPlayer.targetAndAttackEnemyBot(rc)) {
                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                        MapLocation firstLoc = spawnLocs[0];
                        Direction dir = rc.getLocation()
                                          .directionTo(firstLoc);
                        MapInfo mapInfo = rc.senseMapInfo(rc.getLocation().add(dir));
                        if(mapInfo.isWater()) {
                            rc.fill(mapInfo.getMapLocation());
                        }
                    }
                }


            } catch (GameActionException e) {
            } finally {
                Clock.yield();
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
            }
        }
    }


}
