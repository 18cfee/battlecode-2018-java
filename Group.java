import bc.*;

import java.util.HashSet;

public class Group {
    static final int DEF_ARMY_SIZE = 100;
    GameController gc;
    int [] moveAbles = new int[DEF_ARMY_SIZE];
    int movableIndex = 0;
    HashSet<Integer> ids;
    Path p;
    MapLocation target;
    GenericStates state;
    protected short[][] hill;
    protected int groupRound = 0;
    Group(GameController gc, Path p){
        this.p = p;
        this.gc = gc;
        state = GenericStates.RandomMove;
        ids = new HashSet<>(DEF_ARMY_SIZE);
    }
    public void add(int id) throws Exception{
        if(groupRound != p.round){
            groupRound = p.round;
            movableIndex = 0;
            ids.clear();
        }
        ids.add(id);
        if(gc.isMoveReady(id)){
            // if the array is too small
            if(movableIndex == moveAbles.length){
                int[] temp = moveAbles;
                moveAbles = new int[temp.length*2];
                for (int i = 0; i < temp.length; i++) {
                    moveAbles[i] = temp[i];
                }
            }
            moveAbles[movableIndex++] = id;
        }
    }
    public int size(){
        return ids.size();
    }
    public void conductTurn() throws Exception{
        if(noUnits())return;
        roamRandom();
    }
    protected void roamRandom(){
        if(noUnits()) return;
        for (int i = 0; i < movableIndex; i++) {
            int id = moveAbles[i];
            p.moveIfPossible(id);
        }
    }
    protected boolean shouldContinueRoamingRandomly(){
        return gc.round() < 100;
    }
    protected void changeToTargetMap(short[][] hill) throws Exception{
        state = GenericStates.TargetDestination;
        this.hill = hill;
    }
    protected void moveToTarget(short[][] hill) throws Exception{
        if(noUnits()) return;
        for (int i = 0; i < movableIndex; i++) {
            int id = moveAbles[i];
            moveDownHill(id,hill);
        }
    }
    // calling this basically has units move to the set target
    protected void moveDownHill(int id, short[][] hill) throws Exception{
        Unit unit = gc.unit(id);
        Location loc = unit.location();
        if(loc.isInGarrison() || loc.isInSpace()) return;
        MapLocation cur = loc.mapLocation();
        if(hill == null) {
            return;
        }
        short dirVal = hill[cur.getX()][cur.getY()];
        short min = p.greatestPathNum;
        Direction topChoice = null;
        for (int i = 0; i < 8; i++) {
            Direction dir = p.directions[i];
            if(p.canMove(id,dir)){
                int[] dirR = p.numsDirections[i];
                MapLocation newLoc = new MapLocation(p.planet,dirR[0]+ cur.getX(),dirR[1]+cur.getY());
                short grad = hill[newLoc.getX()][newLoc.getY()];
                if (grad < min) {
                    min = grad;
                    topChoice = dir;
                }
            }
        }
        if(topChoice != null){ //min <= dirVal
            if(gc.isMoveReady(id)) gc.moveRobot(id,topChoice);
        }
    }
    protected boolean noUnits(){
        if(ids.size() == 0) return true;
        if(p.round != groupRound){
            movableIndex = 0;
            ids.clear();
            groupRound = p.round;
            return true;
        }
        return false;
    }
}

enum GenericStates {
    RandomMove, Not, TargetDestination;
}