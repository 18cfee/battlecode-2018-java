import bc.GameController;
import bc.MapLocation;

public class Defenders extends Fighter {
    Defenders(GameController gc, Path p){
        super(gc,p);
    }
    public int rocket;
    private short[][] baseHill = null;
    private MapLocation base = null;
    @Override
    public void conductTurn() throws Exception{
        System.out.println("it is making it into defenders");
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
        //else loadRocketIfPossible(rocket);
        shootAtSomething();
        indexShooters = 0;
        indexEnemy = 0;
        movableIndex = 0;
        index = 0;
    }
    public void loadRocketIfPossible(int rocketId){
        if(rocketId == -1 ) return;
        System.out.println("gathering around rocket");
        baseHill = p.firstRocketLocHill;
        for (int i = 0; i < movableIndex; i++) {
            if (gc.canLoad(rocketId,moveAbles[i])){
                gc.load(rocketId,moveAbles[i]);
            }
        }
        index = 0;
    }
}
