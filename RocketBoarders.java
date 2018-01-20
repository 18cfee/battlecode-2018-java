import bc.GameController;
import bc.MapLocation;

public class RocketBoarders extends Fighter {
    RocketBoarders(GameController gc, Path p){
        super(gc,p);
    }
    public int rocket;
    private short[][] baseHill = null;
    private MapLocation base = null;
    private Wall wall;
    @Override
    public void conductTurn() throws Exception{
        System.out.println("it is making it into defenders");
        if(base == null){
            //not ready to go
            if(p.baseLoc == null){
                return;
            } else {
                base = p.baseLoc;
                baseHill = p.firstRocketLocHill;
            }
        }
        if(!loadRocketIfPossible(rocket)){
            moveToTarget(baseHill);
        }
        shootAtSomething();
        index = 0;
    }
    public boolean loadRocketIfPossible(int rocketId){
        if(rocketId == -1 ) return false;
        boolean loaded = false;
        System.out.println("gathering around rocket");
        baseHill = p.firstRocketLocHill;
        for (int i = 0; i < movableIndex; i++) {
            System.out.println("a unit tried");
            if (gc.canLoad(rocketId,moveAbles[i])){
                System.out.println("something was loaded");
                gc.load(rocketId,moveAbles[i]);
                loaded = true;
            }
        }
        return loaded;
    }
}

