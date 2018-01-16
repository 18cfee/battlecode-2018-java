import bc.Direction;
import bc.GameController;
import bc.Unit;
import bc.UnitType;

public class Army {
    GameController gc;
    Path p;
    Fighter carlsRangers;
    Fighter baseProtection;
    int size = 0;

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
        resetSize();
    }
    public void addUnit(int id) throws Exception{
        if(id%2 == 0) carlsRangers.add(id);
        else baseProtection.add(id);
        size++;
    }
    public void addEnemyUnit(int id){
        carlsRangers.addEnemy(id);
        baseProtection.addEnemy(id);
    }
    public void addFact(Unit fact){
        if(p.builtFactIndex < p.MAX_NUM_FACTS){
            System.out.println("carl think we have a fact");
            p.builtFactary[p.builtFactIndex++] = fact.id();
        }
    }
    public void factoryProduce(){
        UnitType production = UnitType.Ranger;
        //Direction random = p.getRandDirection();
        Direction random = p.getRandDirection();
        System.out.println("Attempting to make a unit");
        System.out.println("builtFactIndex = " + p.builtFactIndex);
        for (int i = 0; i < p.builtFactIndex; i++) {
            System.out.println("Num in garrison: " + gc.unit(p.builtFactary[i]).structureGarrison().size());
            if(gc.canProduceRobot(p.builtFactary[i],production)){
                System.out.println("factory made a unit");
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
