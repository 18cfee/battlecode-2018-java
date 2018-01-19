import bc.*;

import java.util.HashMap;
import java.util.HashSet;

public class Army {
    GameController gc;
    Path p;
    Fighter carlsRangers;
    Defenders baseProtection;
    RocketBoarders marsTroops;
    HashSet<Integer> tempOldFactories;
    int size = 0;
    private int rocketId = -1;
    private final static int MAXUnits = 95;
    private int armyRound = 0;
    public Army(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
        carlsRangers = new RangersTargetNextUnit(gc,p);
        baseProtection = new Defenders(gc,p);
        marsTroops = new RocketBoarders(gc,p);
        tempOldFactories = new HashSet<>();
    }
    public void conductTurn() throws Exception{
        carlsRangers.conductTurn();
        baseProtection.conductTurn();
        marsTroops.conductTurn();
        factoryProduce();
        rocketShouldLaunchIfItCan();
        resetSize();
        rocketId = -1;
    }
    public void addUnit(int id) throws Exception{
        if(gc.round() > 500 && gc.round()%2 == 0){
            marsTroops.add(id);
        } else {
            baseProtection.add(id);
        }
        size++;
    }
    public void addEnemyUnit(int id){
        carlsRangers.addEnemy(id);
        baseProtection.addEnemy(id);
    }
//    public void distributeFactories(){
//        p.currentBuiltFactories.retainAll(tempFactories);
//        tempFactories.remove(p.currentBuiltFactories);
//        // for now just put all the new facotories in the builtFactories
//
//    }
    public void addFact(Unit fact){
        // resets the temp factories every turn
        if(armyRound != p.round){
            armyRound = p.round;
            tempOldFactories = p.currentBuiltFactories;
            p.currentBuiltFactories = new HashSet<>();
        }
        int id = fact.id();
        if(tempOldFactories.contains(id)){
            p.currentBuiltFactories.add(id);
        } else {
            System.out.println("there is a new factory");
            p.currentBuiltFactories.add(id);
        }
    }
    public void addRocket(Unit unit){
        rocketId = unit.id();
        marsTroops.rocket = rocketId;
        System.out.println("rocket garrison: " + unit.structureGarrison().size());
    }
    public void rocketShouldLaunchIfItCan(){
        if(rocketId == -1) return;
        if(((gc.unit(rocketId).structureGarrison().size() == 12) || (gc.round() > 700)) && gc.canLaunchRocket(rocketId, p.placeToLandOnMars)){
            gc.launchRocket(rocketId, p.placeToLandOnMars);
        }
    }
    public void factoryProduce(){
        if(size > MAXUnits) return;

        UnitType production = UnitType.Ranger;
        Direction random = p.getRandDirection();
        for (Integer factId: p.currentBuiltFactories) {
            System.out.println("Num in garrison: " + gc.unit(factId).structureGarrison().size());
            if(gc.canProduceRobot(factId,production)){
                gc.produceRobot(factId,production);
            }
            if(gc.unit(factId).structureGarrison().size() > 0){
                tryToUnloadInAlDirections(factId);
            }
        }
    }

    private void tryToUnloadInAlDirections(int id){
        int num = p.random.nextInt(8);
        for (int i = 0; i < 8; i++) {
            Direction dir = p.directions[(i+num)%8];
            if(gc.canUnload(id,dir)){
                gc.unload(id,dir);
            }
        }
    }

    public int getArmySize(){
        return size;
    }
    public void resetSize(){
        size = 0;
    }
}
