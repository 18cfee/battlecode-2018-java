import bc.Direction;
import bc.GameController;
import bc.MapLocation;
import bc.Unit;

public class Group {
    static final int DEF_ARMY_SIZE = 100;
    GameController gc;
    int [] ids = new int[DEF_ARMY_SIZE];
    int [] moveAbles = new int[DEF_ARMY_SIZE];
    int movableIndex = 0;
    int index = 0;
    Path p;
    MapLocation target;
    GenericStates state;
    protected short[][] hill;
    private int moveRound = 0;
    Group(GameController gc, Path p){
        this.p = p;
        this.gc = gc;
        state = GenericStates.RandomMove;
    }
    public boolean add(int id) throws Exception{
        if(moveRound != p.round){
            moveRound = p.round;
            movableIndex = 0;
        }
        if(index != DEF_ARMY_SIZE){
            ids[index++] = id;
            if(gc.isMoveReady(id)){
                moveAbles[movableIndex++] = id;
            }
            return true;
        }
        return false;
    }
    public void conductTurn() throws Exception{
        roamRandom();
        index = 0;
    }
    protected void roamRandom(){
        if(noMovables()) return;
        for (int i = 0; i < movableIndex; i++) {
            int id = moveAbles[i];
            Direction toMove = p.getRandDirection();
            if(gc.isMoveReady(id) && gc.canMove(id,toMove)){
                gc.moveRobot(id,toMove);
            }
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
        if(noMovables()) return;
        for (int i = 0; i < movableIndex; i++) {
            int id = moveAbles[i];
            moveDownHill(id,hill);
        }
    }
    // calling this basically has units move to the set target
    protected void moveDownHill(int id, short[][] hill) throws Exception{
        Unit unit = gc.unit(id);
        MapLocation cur = unit.location().mapLocation();
        if(hill == null) {
            System.out.println("problem");
            return;
        }
        short dirVal = hill[cur.getX()][cur.getY()];
        short min = p.greatestPathNum;
        Direction topChoice = null;
        for (int i = 0; i < 8; i++) {
            Direction dir = p.directions[i];
            if(gc.canMove(id,dir)){
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
    private boolean noMovables(){
        if(movableIndex == 0) return true;
        if(p.round != moveRound){
            movableIndex = 0;
            moveRound = p.round;
            return true;
        }
        return false;
    }
}

enum GenericStates {
    RandomMove, Not, TargetDestination;
}