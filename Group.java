import bc.Direction;
import bc.GameController;
import bc.MapLocation;

import java.util.HashMap;
import java.util.Stack;

public class Group {
    static final int MAX_ARMY_SIZE = 100;
    GameController gc;
    int [] ids = new int[MAX_ARMY_SIZE];
    int [] moveAbles = new int[MAX_ARMY_SIZE];
    int movableIndex = 0;
    int index = 0;
    Path p;
    MapLocation target;
    GenericStates state;
    Group(GameController gc, Path p){
        this.p = p;
        this.gc = gc;
        state = GenericStates.RandomMove;
    }
    public void add(int id){
        if(index != MAX_ARMY_SIZE){
            ids[index++] = id;
            if(gc.isMoveReady(id)){
                moveAbles[movableIndex++] = id;
            }
        }
    }
    protected void changeToTargetDestinationState(MapLocation target){
        state = GenericStates.TargetDestination;
        this.target = target;
        paths = new HashMap<>();
    }
    public void conductTurn(){
        if(state == GenericStates.RandomMove){
            if(shouldContinueRoamingRandomly()){
                roamRandom();
            } else{
                changeToTargetDestinationState(p.closestStartLocation);
            }
        }
        if(state == GenericStates.TargetDestination){
            aquireIndividualPaths();
            moveToTarget();
        }
        index = 0;
    }
    protected HashMap<Integer,Stack<MapLocation>> paths;
    protected void aquireIndividualPaths(){
        for (int i = 0; i < index; i++) {
            int id = ids[i];
            // add the ones not already calculated
            if(!paths.containsValue(id)){
                Stack<MapLocation> unitPath = p.genShortestRouteBFS(gc.unit(id).location().mapLocation(),
                        p.closestStartLocation);
                paths.put(id,unitPath);
            }
        }
    }
    protected void moveToTarget(){
        for (int i = 0; i < index; i++) {
            int id = ids[i];
            if(!tryMoveNextRoute(id)){
                p.moveInRandomAvailableDirection(id);
                Stack<MapLocation> unitPath = p.genShortestRouteBFS(gc.unit(id).location().mapLocation(),
                        p.closestStartLocation);
                paths.put(id,unitPath);
            }
        }
    }
    protected boolean tryMoveNextRoute(int id){
        Stack<MapLocation> route = paths.get(id);
        if(route == null || route.empty() || !gc.isMoveReady(id)){
            return false;
        }
        MapLocation durGoal = route.peek();
        Direction curDirection = gc.unit(id).location().mapLocation().directionTo(durGoal);
        System.out.println("cur direction: " + curDirection);
        if(!gc.canMove(id,curDirection)){
            return false;
        } else {
            gc.moveRobot(id,curDirection);
            route.pop();
            return true;
        }
    }
    protected void roamRandom(){
        for (int i = 0; i < index; i++) {
            int id = ids[i];
            Direction toMove = p.getRandDirection();
            if(gc.isMoveReady(id) && gc.canMove(id,toMove)){
                gc.moveRobot(id,toMove);
            }
        }
    }
    protected boolean shouldContinueRoamingRandomly(){
        return gc.round() < 300;
    }

}

enum GenericStates {
    RandomMove, Not, TargetDestination;
}