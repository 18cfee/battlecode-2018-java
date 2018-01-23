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
    public void addEnemy(Enemy enemy){
        if(enRoundNum != p.round){
            enRoundNum = p.round;
            enemies.clear();
        }
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
            if(numShooters == canShoot.length){
                int[] temp = canShoot.clone();
                canShoot = new int[temp.length*2];
                for (int i = 0; i < temp.length; i++) {
                    canShoot[i] = temp[i];
                }
            }
        }
    }
    @Override
    public void conductTurn() throws Exception{
        if(noUnits()) return;
        roamRandom();
        shootOptimally();
    }
//    public void shootAtSomething ()throws Exception{
//        if(noEnemies() || noShooters()) return; // not just for efficiency
//        for (int i = 0; i < numShooters; i++) {
//            for (Enemy enemy: enemies) {
//                if(enemy.hp > 0 && gc.canAttack(canShoot[i],enemy.id)){
//                    gc.attack(canShoot[i],enemy.id);
//                    enemy.hp -= gc.unit(canShoot[i]).damage();
//                    break;
//                }
//            }
//        }
//    }
    protected boolean noShooters(){
        if(numShooters == 0) return true;
        if(shooterTurn != p.round){
            numShooters = 0;
            shooterTurn = p.round;
            return true;
        }
        return false;
    }
    public void shootOptimally(){
        if(noEnemies() || noShooters()) return; // not just for efficiency
        for (int i = 0; i < numShooters; i++) {
            for (Enemy enemy: enemies) {
                if(enemy.hp > 0 && gc.canAttack(canShoot[i],enemy.id)){
                    //System.out.println("shot at " + enemy.id);
                    gc.attack(canShoot[i],enemy.id);
                    enemy.hp -= gc.unit(canShoot[i]).damage();
                    break;
                }
            }
        }
    }
    protected boolean noEnemies(){
        if(enemies.size() == 0) return true;
        if(enRoundNum != p.round){
            enRoundNum = p.round;
            enemies.clear();
            return true;
        }
        return false;
    }
}
