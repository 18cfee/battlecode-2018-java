import bc.GameController;
import bc.MapLocation;

public class RangersTargetNextUnit extends Fighter{
    RangersTargetNextUnit(GameController gc, Path p){
        super(gc,p);
    }
    private short[][] baseHill = null;
    private MapLocation base = null;
    private short[][] baseHillDef = null;
    private MapLocation baseDef = null;
    private boolean seesEnemy = false;
    private short groupTargetCooldown = 0;
    @Override
    public void conductTurn() throws Exception{
        if(baseDef == null){
            //not ready to go
            if(p.baseLoc == null){
                return;
            } else {
                baseDef = p.baseLoc;
                baseHillDef = p.generateHill(baseDef);
            }
        }
        // disallows a longer trail of soldiers on bigger maps
        if(index < p.planetSize/2){
            base = baseDef;
            baseHill = baseHillDef;
        } else if(seesEnemy == false && indexEnemy != 0 && groupTargetCooldown == 0){
            seesEnemy = true;
            if(gc.canSenseUnit(enemy[0])){
                MapLocation a = gc.unit(enemy[0]).location().mapLocation();
                MapLocation target = p.getLocBetween(base,a);
                if(p.passable(target)){
                    base = target;
                    baseHill = p.generateHill(base);
                    groupTargetCooldown+= 50;
                }
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
