import bc.*;

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
    public boolean add(int id) throws Exception{

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
    public void conductTurn() throws Exception{
        roamRandom();
        shootAtSomething();
        indexShooters = 0;
        indexEnemy = 0;
        movableIndex = 0;
        index = 0;
    }
    @Override
    protected boolean shouldContinueRoamingRandomly(){
        return (indexEnemy == 0);
    }
    public void shootAtSomething ()throws Exception{
        for (int i = 0; i < indexShooters; i++) {
            for (int j = 0; j < indexEnemy; j++) {
                if(gc.isAttackReady(canShoot[indexShooters]) && gc.canAttack(canShoot[i],enemy[j])){
                    gc.attack(canShoot[i],enemy[j]);
                    break;
                }
            }
        }
    }
    public void shootOptimally(){

    }
}
