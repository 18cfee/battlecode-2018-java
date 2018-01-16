import bc.GameController;
import bc.MapLocation;

public class Defenders extends Fighter {
    Defenders(GameController gc, Path p){
        super(gc,p);
    }
    private short[][] baseHill = null;
    private MapLocation base = null;
    @Override
    public void conductTurn() throws Exception{
        if(base == null){
            //not ready to go
            if(p.baseLoc == null){
                return;
            } else {
                base = p.baseLoc;
                baseHill = p.generateHill(base);
            }
        }
        moveToTarget(baseHill);
        shootAtSomething();
        indexShooters = 0;
        indexEnemy = 0;
        movableIndex = 0;
        index = 0;
    }
    public void loadRocetIfPossible(int rocketId){
        for (int i = 0; i < movableIndex; i++) {
        }
    }
}
