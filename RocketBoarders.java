import bc.GameController;
import bc.MapLocation;

public class RocketBoarders extends Fighter {
    RocketBoarders(GameController gc, Path p){
        super(gc,p);
    }
    private int rocketId;
    @Override
    public void conductTurn() throws Exception{
        if(noUnits()) return;
        System.out.println("it is making it into rocket boarders: " + rocketId);
        System.out.println("size of boarders " + size());
        roamRandom();
        shootAtSomething();
    }
    public void attainRocketId(){
        //todo
    }
//    public boolean loadRocketIfPossible(int rocketId){
//        if(rocketId == -1 ) return false;
//        boolean loaded = false;
//        System.out.println("gathering around rocket");
//        baseHill = p.firstRocketLocHill;
//        for (int i = 0; i < movableIndex; i++) {
//            System.out.println("a unit tried");
//            if (gc.canLoad(rocketId,moveAbles[i])){
//                System.out.println("something was loaded");
//                gc.load(rocketId,moveAbles[i]);
//                loaded = true;
//            }
//        }
//        return loaded;
//    }
}

