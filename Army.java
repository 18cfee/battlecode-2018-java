import bc.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Army {
    GameController gc;
    Path p;
    Fighter carlsRangers;
    Defenders baseProtection;
    private int numDefenders = 0;
    RocketBoarders marsTroops;
    ArrayList<RocketBoarders> rangers;
    ArrayList<HashSet<Integer>> troops;
    HashSet<Integer> tempOldFactories;
    AggresiveRangers killEm;
    int size = 0;
    private final static int MAXUnits = 200;
    private int armyRound = 0;
    private int fighterRound = 0;
    private int numGroupsCreated = 0;
    private final static int NEED_TO_SAVE_FOR_ROCKETS_ROUND = 300;
    private final static int REALLY_NEED_TO_SAVE_FOR_ROCKETS_ROUND = 500;
    private ArrayList<Enemy> enemies;
    public Army(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
        carlsRangers = new RangersTargetNextUnit(gc,p);
        baseProtection = new Defenders(gc,p);
        //marsTroops = new RocketBoarders(gc,p);
        tempOldFactories = new HashSet<>();
        rangers = new ArrayList<>();
        troops = new ArrayList<>();
        killEm = new AggresiveRangers(gc,p);
        enemies = new ArrayList<>();
    }
    public void conductTurn() throws Exception{
        long time = System.currentTimeMillis();
        distributeEnemies();
        carlsRangers.conductTurn();
        baseProtection.conductTurn();
        killEm.conductTurn();
//        marsTroops.conductTurn();
        ArrayList<Integer> beastsToRemove = new ArrayList<>(4);
        for (int i = 0; i < rangers.size(); i++) {
            RocketBoarders beasts = rangers.get(i);
            beasts.conductTurn();
            if(beasts.ids.size() == 0){
                beastsToRemove.add(i);
            }
        }
        // removes the empty groups
//        for(Integer i: beastsToRemove){
//            rangers.remove(i);
//        }
        factoryProduce();
//        rocketShouldLaunchIfItCan();
        resetSize();
        long end = System.currentTimeMillis();
    }
    private int shouldCreateRocketGroup = -1;
    private int oldNumRangerGroups = 0;
    RocketBoarders group = null;
    private int attackSize = 50;
    private HashSet<Integer> oldAttack;
    boolean haveIncreasedAttacketsThisRound = false;
    public void addUnit(int id) throws Exception{
        //if(p.round%50 == 0){
        size++;
        // get the group info from last round then clear
        if(fighterRound != p.round){
            group = null;
            fighterRound = p.round;
            troops.clear();
            oldNumRangerGroups = rangers.size();
            for (int i = 0; i < oldNumRangerGroups; i++) {
                troops.add((HashSet<Integer>)rangers.get(i).ids.clone());
            }
            numDefenders = baseProtection.ids.size();
            oldAttack = (HashSet<Integer>)killEm.ids.clone();
            haveIncreasedAttacketsThisRound = false;
            //System.out.println("thinks there are this many on d: " + numDefenders);
        }
        // assign all the rangers back to there groups
        for (int i = 0; i < oldNumRangerGroups; i++) {
            if(troops.get(i).contains(id)){
                //System.out.println("id assigned to old rangers: " + id);
                rangers.get(i).add(id);
                return;
            }
        }
        // assign attackers back to attackers
        if(oldAttack.contains(id)){
            killEm.add(id);
            return;
        }
//        if(gc.round() > 500 && gc.round()%2 == 0){
//            marsTroops.add(id);
//        } else {
        if((numDefenders > attackSize && p.round < NEED_TO_SAVE_FOR_ROCKETS_ROUND) || haveIncreasedAttacketsThisRound) {
            if(!haveIncreasedAttacketsThisRound){
                attackSize += 10;
                haveIncreasedAttacketsThisRound = true;
            }
            killEm.add(id);
            // assign units to an attack group
        }else if(numDefenders > 20 && shouldBuildRocket() && !shouldBeDefending()){
            group = new RocketBoarders(gc,p);
            group.attainRocketId();
            rangers.add(group);
            group.add(id);
            shouldCreateRocketGroup = p.round;
            numDefenders = 0;
            baseProtection.ids.clear();
            numGroupsCreated++;
        } else if(shouldCreateRocketGroup == p.round && group.size() < 14){
            group.add(id);
        } else {
            baseProtection.add(id);
        }
    }
    private boolean shouldBuildRocket(){
        return false;
    }
    private boolean shouldBeDefending(){
        return false;
    }
    private int enemyRound = -1;
    public void addEnemyUnit(Unit unit){
        if(enemyRound != p.round){
            enemyRound = p.round;
            enemies.clear();
        }
        Enemy enemy = new Enemy(unit);
        enemies.add(enemy);
    }
    private void distributeEnemies(){
        if(enemyRound != p.round){
            enemies.clear();
            return;
        }
        Collections.sort(enemies,new EnemySorter());
        for(Enemy enemy: enemies){
            carlsRangers.addEnemy(enemy);
            baseProtection.addEnemy(enemy);
            killEm.addEnemy(enemy);
            for(RocketBoarders group: rangers){
                group.addEnemy(enemy);
            }
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
        //System.out.println("there just was " + tempOldFactories.size() + " factories");
        if(tempOldFactories.contains(id)){
            p.currentBuiltFactories.add(id);
        } else {
            //System.out.println("there is a new factory");
            p.currentBuiltFactories.add(id);
        }
    }
//    public void addRocket(Unit unit){
//        rocketId = unit.id();
//        marsTroops.rocket = rocketId;
//        System.out.println("rocket garrison: " + unit.structureGarrison().size());
//    }
//    public void rocketShouldLaunchIfItCan(){
//        if(rocketId == -1) return;
//        if(((gc.unit(rocketId).structureGarrison().size() == 12) || (gc.round() > 700)) && gc.canLaunchRocket(rocketId, p.placeToLandOnMars)){
//            gc.launchRocket(rocketId, p.placeToLandOnMars);
//        }
//    }
    public void factoryProduce() throws Exception{
        //if(size > MAXUnits) return;
        // this means no units were added so the add method was never called
        if(armyRound != p.round){
            armyRound = p.round;
            tempOldFactories.clear();
            p.currentBuiltFactories.clear();
        }
        UnitType production = UnitType.Ranger;
        for (Integer factId: p.currentBuiltFactories) {
            //System.out.println("Num in garrison: " + gc.unit(factId).structureGarrison().size());
            if(p.sensableUnitNotInGarisonOrSpace(factId)){
                if(gc.canProduceRobot(factId,production) && weDoNotNeedRockets()){
                    gc.produceRobot(factId,production);
                }
                if(gc.unit(factId).structureGarrison().size() > 0){
                    // this should not produce in a launch pad
                    tryToUnloadInAlDirections(factId);
                }
            }
        }
    }

    private boolean weDoNotNeedRockets(){
        if(p.rockets.getTotalRockets() < p.NUM_ROCKETS_WANTED && p.round > REALLY_NEED_TO_SAVE_FOR_ROCKETS_ROUND){
            return false;
        }
        return(p.round < NEED_TO_SAVE_FOR_ROCKETS_ROUND || gc.karbonite() >= 190 || p.rockets.getTotalRockets() >= p.NUM_ROCKETS_WANTED - 1);
    }

    private void tryToUnloadInAlDirections(int id) throws Exception{
        int num = p.random.nextInt(8);
        for (int i = 0; i < 8; i++) {
            Direction dir = p.directions[(i+num)%8];
            if(gc.canUnload(id,dir) && notInLaunchArea(id,dir)){
                gc.unload(id,dir);
            }
        }
    }
    private boolean notInLaunchArea(int id, Direction dir) throws Exception{
        if(!p.rockets.isLaunchTurn()) return true;
        MapLocation target = gc.unit(id).location().mapLocation().add(dir);
        return !p.rockets.inLaunchPad(target);
    }
    public int getArmySize(){
        return size;
    }
    public void resetSize(){
        size = 0;
    }
}
