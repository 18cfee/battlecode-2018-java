import bc.GameController;
import bc.MapLocation;

public class RangersTargetNextUnit extends Fighter{
    RangersTargetNextUnit(GameController gc, Path p){
        super(gc,p);
    }
    private short[][] baseHill = p.hillToBase;
    private MapLocation base = p.startLoc;
    private boolean seesEnemy = false;
    private short groupTargetCooldown = 0;
    @Override
    public void conductTurn() throws Exception{
        if(seesEnemy == false && indexEnemy != 0 && groupTargetCooldown == 0){
            seesEnemy = true;
            MapLocation a = gc.unit(enemy[0]).location().mapLocation();
            MapLocation target = p.getLocBetween(a,base);
            if(p.passable(target)){
                baseHill = p.generateHill(target);
                groupTargetCooldown+= 10;
            }
        } else if (seesEnemy = true && indexEnemy == 0){
            seesEnemy = false;
        }
        moveToTarget(baseHill);
        shootAtSomething();
        indexShooters = 0;
        indexEnemy = 0;
        movableIndex = 0;
        index = 0;
        if(groupTargetCooldown > 0) groupTargetCooldown--;
    }
}
