import bc.GameController;
import bc.MapLocation;

public class Defenders extends Fighter {
    private MiniHill miniHill = null;
    private MapLoc target = null;
    private boolean seesEnemy;
    private int groupTargetCooldown;
    Defenders(GameController gc, Path p){
        super(gc,p);
        seesEnemy = false;
        groupTargetCooldown = 0;
        miniHill = new MiniHill(gc,p);
    }
    @Override
    public void conductTurn(){
        if(noUnits()) return;
        if(!noEnemies() && seesEnemy == false && groupTargetCooldown == 0){
            Enemy enemy = enemies.get(0);
            if(enemy.hp > 0){
                MapLoc a = enemy.getMapLoc();
                target = a;
                if(!miniHill.generateMiniRing(target,ids)){
                    target = null;
                }
                groupTargetCooldown+= 25;
                seesEnemy = true;
            }
        } else if (seesEnemy == true && enemies.size() == 0){
            seesEnemy = false;
        }
        if(target!= null){
            System.out.println("roaming to hill");
            moveToMiniHill(miniHill);
        }
        else{
            System.out.println("roaming random");
            roamRandom();
        }
        shootOptimally();
        if(groupTargetCooldown > 0) groupTargetCooldown--;
    }
    protected void moveToMiniHill(MiniHill miniHill){
        for (int i = 0; i < movableIndex; i++) {
            miniHill.moveUnit(moveAbles[i]);
        }
    }
}
