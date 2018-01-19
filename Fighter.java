import bc.*;

import java.util.ArrayList;
import java.util.HashSet;

public class Fighter extends Group {
    int[] canShoot = new int[DEF_ARMY_SIZE];
    int indexShooters = 0;
    private int enRoundNum = 0;
    public ArrayList<Enemy> enemies;
    Fighter(GameController gc, Path p){
        super(gc,p);
        enemies = new ArrayList<>();
    }
    public void addEnemy(Unit unit){
        if(enRoundNum != p.round){
            enRoundNum = p.round;
            enemies.clear();
        }
        Enemy enemy = new Enemy(unit);
        enemies.add(enemy);
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
    @Override
    public void conductTurn() throws Exception{
        roamRandom();
        shootAtSomething();
        indexShooters = 0;
        movableIndex = 0;
        index = 0;
    }
    public void shootAtSomething ()throws Exception{
        if(noEnemies()) return; // not just for efficiency
        for (int i = 0; i < indexShooters; i++) {
            for (Enemy enemy: enemies) {
                if(gc.canAttack(canShoot[i],enemy.id)){
                    gc.attack(canShoot[i],enemy.id);
                    break;
                }
            }
        }
    }
    public void shootOptimally(){
        if(noEnemies()) return;
    }
    private boolean noEnemies(){
        if(enemies.size() == 0) return true;
        if(enRoundNum != p.round){
            enRoundNum = p.round;
            enemies.clear();
            return true;
        }
        return false;
    }
}
