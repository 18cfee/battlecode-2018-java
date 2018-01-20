import bc.GameController;
import bc.MapLocation;

public class RangersTargetNextUnit extends Fighter{
    RangersTargetNextUnit(GameController gc, Path p){
        super(gc,p);
    }
    private Hill baseHill = null;
    private MapLocation base = null;
    private Hill baseHillDef = null;
    private MapLocation baseDef = null;
    private boolean seesEnemy = false;
    private short groupTargetCooldown = 0;
    @Override
    public void conductTurn() throws Exception{
        if(noUnits())return;
        System.out.println("it is making it into rangers next unit");
        if(baseDef == null){
            //not ready to go
            if(p.baseLoc == null){
                return;
            } else {
                baseDef = p.baseLoc;
                baseHillDef = new Hill(p);
                baseHillDef.generateCompleteReachableHill(baseDef);
            }
        }
        // disallows a longer trail of soldiers on bigger maps
        if(size() < (p.planetHeight + p.planetWidth)/2){
            base = baseDef;
            baseHill = baseHillDef;
        } else if(seesEnemy == false && enemies.size() != 0 && groupTargetCooldown == 0){
            seesEnemy = true;
            MapLocation a = gc.unit(enemies.get(0).id).location().mapLocation();
            MapLocation target = p.getLocBetween(base,a);
            if(p.passable(target)){
                base = target;
                baseHill.generateCompleteReachableHill(base);
                groupTargetCooldown+= 50;
            }
        } else if (seesEnemy = true && enemies.size() == 0){
            seesEnemy = false;
        }
        moveToTarget(baseHill);
        shootAtSomething();
        if(groupTargetCooldown > 0) groupTargetCooldown--;
    }
}
