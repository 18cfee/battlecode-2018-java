import bc.Direction;
import bc.GameController;
import bc.MapLocation;
import bc.Unit;

public class CarlRangers extends Group {
    CarlRangers(GameController gc, Path p){
        super(gc,p);
    }
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
            moveToTarget(hill);// the hill is set above, in p.generateHill(MapLocation);
        }
        movableIndex = 0;
        index = 0;
    }

}
