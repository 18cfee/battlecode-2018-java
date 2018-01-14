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
                short[][] toEnemy = p.generateHill(p.closestStartLocation);
                Debug.printHill(toEnemy);
                changeToTargetMap(toEnemy);
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
        System.out.println(hill[4][3]);
    }
    @Override
    protected void moveToTarget(){
        for (int i = 0; i < movableIndex; i++) {
            int id = moveAbles[i];
            moveDownHill(id);
        }
    }
    // calling this basically has units move to the set target
    protected void moveDownHill(int id){
        Unit unit = gc.unit(id);
        MapLocation cur = unit.location().mapLocation();
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
            gc.moveRobot(id,topChoice);
        }
    }
}
