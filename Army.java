import bc.*;

public class Army {
    GameController gc;
    Path p;
    Fighter carlsRangers;
    Defenders baseProtection;
    int size = 0;
    private int rocketId = -1;

    public Army(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
        carlsRangers = new RangersTargetNextUnit(gc,p);
        baseProtection = new Defenders(gc,p);
    }
    public void conductTurn() throws Exception{
        carlsRangers.conductTurn();
        baseProtection.conductTurn();
        factoryProduce();
        rocketShouldLaunchIfItCan();
        resetSize();
        rocketId = -1;
    }
    public void addUnit(int id) throws Exception{
        if(id%2 == 0){
            carlsRangers.add(id);
        } else{
            baseProtection.add(id);
        }
        size++;
    }
    public void addEnemyUnit(int id){
        carlsRangers.addEnemy(id);
        //baseProtection.addEnemy(id);
    }
    public void addFact(Unit fact){
        if(p.builtFactIndex < p.MAX_NUM_FACTS){
            p.builtFactary[p.builtFactIndex++] = fact.id();
        }
    }
    public void addRocket(Unit unit){
        rocketId = unit.id();
        baseProtection.rocket = rocketId;
        System.out.println("rocket garrison: " + unit.structureGarrison().size());
    }
    public void rocketShouldLaunchIfItCan(){
        if(rocketId == -1) return;
        if(gc.unit(rocketId).structureGarrison().size() == 12 && gc.canLaunchRocket(rocketId, p.placeToLandOnMars)){
            gc.launchRocket(rocketId, p.placeToLandOnMars);
        }
    }
    public void factoryProduce(){
        UnitType production = UnitType.Ranger;
        Direction random = p.getRandDirection();
        for (int i = 0; i < p.builtFactIndex; i++) {
            System.out.println("Num in garrison: " + gc.unit(p.builtFactary[i]).structureGarrison().size());
            if(gc.canProduceRobot(p.builtFactary[i],production)){
                gc.produceRobot(p.builtFactary[i],production);
            }
            if(gc.canUnload(p.builtFactary[i],random) ){ // && !gc.hasUnitAtLocation(gc.unit(builtFactary[i]).location().mapLocation().add(random))
                gc.unload(p.builtFactary[i],random);
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
