import bc.GameController;
import bc.Location;
import bc.MapLocation;

public class AggresiveKnights extends Fighter {
    private final static int MAXATTACKFROMBOUNDARY = 10;//6
    private int boundarySize = 5;
    private int increaseThresh = 20;
    private MiniHill miniHill = null;
    private MapLoc target = null;
    private boolean seesEnemy;
    private int groupTargetCooldown;
    private int targetId;
    AggresiveKnights(GameController gc, Path p){
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
            if(enemy.hp > 0){
                MapLoc a = enemy.getMapLoc();
                target = a;
                if(!miniHill.generateMini(target,ids,p.baseLoc)){
                    groupTargetCooldown += 2;
                    target = null;
                } else {
                    groupTargetCooldown+= 7;
                    seesEnemy = true;
                    targetId = enemy.id;
                }
            }
        } else if (seesEnemy == true && (enemies.size() == 0 || !p.sensableUnitNotInGarisonOrSpace(targetId))){
            seesEnemy = false;
            target = null;
        }
        if(target!= null){
            moveToMiniHill(miniHill);
        }
        else{
            for(Integer id: ids){
                p.moveIfPossible(id);
            }
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
                if(p.movesToBase(loc) > boundarySize || (p.round > 650 && p.round%2 == 0)){
                    moveDownHill(id,p.hillToBase);
                } else {
                    p.moveIfPossible(id);
                }
            }
        }
    }
}