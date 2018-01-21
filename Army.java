import bc.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Army {
    GameController gc;
    Path p;
    Fighter carlsRangers;
    Defenders baseProtection;
    private int numDefenders = 0;
    RocketBoarders marsTroops;
    ArrayList<AggresiveRangers> rangers;
    ArrayList<HashSet<Integer>> troops;
    HashSet<Integer> tempOldFactories;
    int size = 0;
    private int rocketId = -1;
    private final static int MAXUnits = 95;
    private int armyRound = 0;
    private int fighterRound = 0;
    public Army(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
        carlsRangers = new RangersTargetNextUnit(gc,p);
        baseProtection = new Defenders(gc,p);
        marsTroops = new RocketBoarders(gc,p);
        tempOldFactories = new HashSet<>();
        rangers = new ArrayList<>();
        troops = new ArrayList<>();
    }
    public void conductTurn() throws Exception{
        carlsRangers.conductTurn();
        baseProtection.conductTurn();
        marsTroops.conductTurn();
        ArrayList<Integer> beastsToRemove = new ArrayList<>(4);
        for (int i = 0; i < rangers.size(); i++) {
            AggresiveRangers beasts = rangers.get(i);
            beasts.conductTurn();
            if(beasts.ids.size() == 0){
                beastsToRemove.add(i);
            }
        }
        // removes the empty groups
        for(Integer i: beastsToRemove){
            rangers.remove(i);
        }
        factoryProduce();
        rocketShouldLaunchIfItCan();
        resetSize();
        rocketId = -1;
    }
    private int shouldEmptyBaseRound = -1;
    public void addUnit(int id) throws Exception{
        // get the group info from last round then clear
        if(fighterRound != p.round){
            troops.clear();
            for (int i = 0; i < rangers.size(); i++) {
                troops.add(rangers.get(i).ids);
            }
            numDefenders = baseProtection.ids.size();
        }
        // assign all the rangers back to there groups
        for (int i = 0; i < rangers.size(); i++) {
            if(troops.get(i).contains(id)){
                rangers.get(i).add(id);
                size++;
                return;
            }
        }
        if(gc.round() > 500 && gc.round()%2 == 0){
            marsTroops.add(id);
        } else {
            if(numDefenders > 10){
                AggresiveRangers group = new AggresiveRangers(gc,p);
                rangers.add(group);
                group.add(id);
                shouldEmptyBaseRound = p.round;
                numDefenders = 0;
            } else if(shouldEmptyBaseRound == p.round){
                rangers.get(rangers.size()-1).add(id);
            } else {
                baseProtection.add(id);
            }
        }
        size++;
    }
    public void addEnemyUnit(Unit unit){
        Enemy enemy = new Enemy(unit);
        carlsRangers.addEnemy(enemy);
        baseProtection.addEnemy(enemy);
        for(AggresiveRangers group: rangers){
            group.addEnemy(enemy);
        }
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
        System.out.println("there just was " + tempOldFactories.size() + " factories");
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
        // this means no units were added so the add method was never called
        if(armyRound != p.round){
            armyRound = p.round;
            tempOldFactories.clear();
            p.currentBuiltFactories.clear();
        }
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
