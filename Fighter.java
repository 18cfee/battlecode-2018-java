import bc.*;

import java.util.ArrayList;

public class Fighter extends Group {
    int[] canShoot = new int[DEF_ARMY_SIZE];
    int numShooters = 0;
    private int enRoundNum = 0;
    public ArrayList<Enemy> enemies;
    private int shooterTurn = 0;
    Fighter(GameController gc, Path p){
        super(gc,p);
        enemies = new ArrayList<>(100);
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
    public void add(int id) throws Exception{
        if(shooterTurn != p.round){
            numShooters = 0;
            shooterTurn = p.round;
        }
        super.add(id);
        if(gc.isAttackReady(id)){
            canShoot[numShooters++] = id;
        }
    }
    @Override
    public void conductTurn() throws Exception{
        if(noUnits()) return;
        roamRandom();
        shootAtSomething();
    }
    public void shootAtSomething ()throws Exception{
        if(noEnemies() || noShooters()) return; // not just for efficiency
        for (int i = 0; i < numShooters; i++) {
            for (Enemy enemy: enemies) {
                if(gc.canAttack(canShoot[i],enemy.id)){
                    gc.attack(canShoot[i],enemy.id);
                    break;
                }
            }
        }
    }
    private boolean noShooters(){
        if(numShooters == 0) return true;
        if(shooterTurn != p.round){
            numShooters = 0;
            shooterTurn = p.round;
            return true;
        }
        return false;
    }
    public void shootOptimally(){
        if(noEnemies() || noShooters()) return;
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
