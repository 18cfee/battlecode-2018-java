import bc.Direction;
import bc.GameController;
import bc.MapLocation;
import bc.Unit;

public class CarlRangers extends Group {
    CarlRangers(GameController gc, Path p){
        super(gc,p);
    }
    private short[][] hill;
    @Override
    public void conductTurn(){
        if(state == GenericStates.RandomMove){
            if(shouldContinueRoamingRandomly()){
                roamRandom();
            } else{
                changeToTargetMap(p.hillToBase);
            }
        }
        if(state == GenericStates.TargetDestination){
            moveToTarget();
        }
        movableIndex = 0;
        index = 0;
    }
    protected void changeToTargetMap(short[][] hill){
        state = GenericStates.TargetDestination;
        this.hill = hill;
    }
    @Override
    protected void moveToTarget(){
        for (int i = 0; i < movableIndex; i++) {
            int id = moveAbles[i];
            moveDownHill(id);
        }
    }
    protected void moveDownHill(int id){
        Unit unit = gc.unit(id);
        MapLocation cur = unit.location().mapLocation();
        short min = p.greatestPathNum;
        Direction topChoice = null;
        for (int i = 0; i < 8; i++) {
            Direction dir = p.directions[i];
            if(gc.canMove(id,dir)){
                MapLocation newLoc = cur.add(dir);
                short grad = hill[newLoc.getX()][newLoc.getY()];
                if (grad < min) {
                    grad = min;
                    topChoice = dir;
                }
            }
        }
        if(topChoice != null){
            gc.moveRobot(id,topChoice);
        }
    }
}
