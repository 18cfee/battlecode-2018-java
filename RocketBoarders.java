import bc.GameController;
import bc.MapLocation;

public class RocketBoarders extends Fighter {
    private int rocketId;
    private short[][] hillToRocket;
    RocketBoarders(GameController gc, Path p){
        super(gc,p);
        rocketId = p.rockets.takeRocket();
        MapLocation loc = p.rockets.getMapLocation(rocketId);
        if(p.firstRocketLoc != null && loc.equals(p.firstRocketLoc)){
            hillToRocket = p.firstRocketLocHill;
        } else if (p.secondRocketLoc != null && loc.equals(p.secondRocketLoc)) {
            hillToRocket = p.secondRocketLocHill;
        }
    }

    @Override
    public void conductTurn() throws Exception{
        if(noUnits()) return;
        System.out.println("it is making it into rocket boarders: " + rocketId);
        System.out.println("size of boarders " + size());
        // rocket is gone
        shootOptimally();
        if(!p.rockets.builtRocketsContains(rocketId)) {
            ids.clear();
            return;
        }
        for (int i = 0; i < movableIndex; i++) {
            int id = moveAbles[i];
            if(!p.rockets.successfullyAdded(rocketId,id)){
                moveDownHill(id,hillToRocket);
            }
        }
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

