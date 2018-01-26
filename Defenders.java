import bc.GameController;
import bc.Location;
import bc.MapLocation;

public class Defenders extends Fighter {
    private final static int MAXATTACKFROMBOUNDARY = 10;//6
    private int boundarySize = 5;
    private int increaseThresh = 20;
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
    public void conductTurn() throws Exception{
        if(noUnits()) return;
        if(size() > increaseThresh){
            boundarySize++;
            increaseThresh += 15;
        }
        if(!noEnemies() && seesEnemy == false && groupTargetCooldown == 0){
            Enemy enemy = enemies.get(0);
            if(enemy.hp > 0 && p.movesToBase(enemy.loc) < MAXATTACKFROMBOUNDARY + boundarySize){
                MapLoc a = enemy.getMapLoc();
                target = a;
                if(!miniHill.generateMiniRing(target,ids)){
                    System.out.println("mini was not generated");
                    target = null;
                } else {
                    System.out.println("mini was generated");
                }
                groupTargetCooldown+= 25;
                seesEnemy = true;
            }
        } else if (seesEnemy == true && enemies.size() == 0){
            seesEnemy = false;
            target = null;
        }
        if(target!= null){
            System.out.println("move to mini hill");
            moveToMiniHill(miniHill);
        }
        else{
            System.out.println("move randomly");
            roamRandomlyInRangeOfBase();
        }
        shootOptimally();
        if(groupTargetCooldown > 0) groupTargetCooldown--;
    }
    protected void moveToMiniHill(MiniHill miniHill) throws Exception{
        for (int i = 0; i < movableIndex; i++) {
            miniHill.moveUnit(moveAbles[i]);
        }
    }
    private void roamRandomlyInRangeOfBase() throws Exception{
        for (int i = 0; i < movableIndex; i++) {
            int id = moveAbles[i];
            MapLocation loc = p.getMapLocationIfLegit(id);
            if(loc != null){
                if(p.movesToBase(loc) > boundarySize){
                    moveDownHill(id,p.hillToBase);
                } else {
                    p.moveIfPossible(id);
                }
            }
        }
    }
}
