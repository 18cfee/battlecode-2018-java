import bc.Direction;
import bc.GameController;
import bc.MapLocation;
import bc.Unit;

public class Fighter extends Group {
    int[] canShoot = new int[MAX_ARMY_SIZE];
    int indexShooters = 0;
    int[] enemy = new int[MAX_ARMY_SIZE];
    int[] enemyClone = new int[MAX_ARMY_SIZE];
    int indexEnemy = 0;
    Fighter(GameController gc, Path p){
        super(gc,p);
    }
    public boolean addEnemy(int id){
        if(indexEnemy != MAX_ARMY_SIZE){
            enemy[indexEnemy++] = id;
            return true;
        }
        return false;
    }
    @Override
    public boolean add(int id){

        if(super.add(id)) {
            if(gc.isAttackReady(id)){
                canShoot[indexShooters++] = id;
            }
            return true;
        }
        return false;
    }
    static int oldEnIndex = 0;
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
        shootAtSomething();
        indexShooters = 0;
        indexEnemy = 0;
        movableIndex = 0;
        index = 0;
    }
    public void shootAtSomething(){
        for (int i = 0; i < indexShooters; i++) {
            for (int j = 0; j < indexEnemy; j++) {
                if(gc.canAttack(canShoot[i],enemy[j])){
                    gc.attack(canShoot[i],enemy[j]);
                    break;
                }
            }
        }
    }
    public void shootOptimally(){

    }
}
