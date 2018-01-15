import bc.GameController;
import bc.MapLocation;

public class RangersTargetNextUnit extends Fighter{
    RangersTargetNextUnit(GameController gc, Path p){
        super(gc,p);
    }
    private short[][] baseHill = p.hillToBase;
    private boolean seesEnemy = false;
    @Override
    public void conductTurn() throws Exception{
        if(seesEnemy == false && indexEnemy != 0){
            seesEnemy = true;
            MapLocation a = gc.unit(enemy[0]).location().mapLocation();
            MapLocation b = 
            baseHill = p.generateHill(;
        }
        moveToTarget(baseHill);
        shootAtSomething();
        indexShooters = 0;
        indexEnemy = 0;
        movableIndex = 0;
        index = 0;
    }
}
