import bc.Direction;
import bc.GameController;
import bc.Unit;
import bc.UnitType;

public class Workers {
    int [] ids = new int[100];
    int [] unbuiltFactory = new int [5];
    int unbuiltIndex = 0;
    int builtFactIndex = 0;
    int [] builtFactary = new int [50];
    int factBlueId = -1;
    int index = 0;
    int totalHp = 0;
    GameController gc;
    Path p;
    public WorkerStates state;
    public Workers(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
    }
    public void addFactory(Unit fact){
        if(fact.structureIsBuilt() == 1){
            System.out.println("a factory is built");
            builtFactary[builtFactIndex++] = fact.id();
        } else {
            unbuiltFactory[unbuiltIndex++] = fact.id();
            System.out.println("blue id: " + fact.id());
            //System.out.println("factory health: " + gc.ha);
            factBlueId = fact.id();
        }
    }
    public void factoryProduce(){
        UnitType production = UnitType.Ranger;
        //Direction random = p.getRandDirection();
        Direction random = Direction.Northwest;
        for (int i = 0; i < builtFactIndex; i++) {
            if(gc.canProduceRobot(builtFactary[i],production)){
                System.out.println("factory made a unit");
                gc.produceRobot(builtFactary[i],production);
            }
            if(gc.canUnload(builtFactary[i],random) ){ // && !gc.hasUnitAtLocation(gc.unit(builtFactary[i]).location().mapLocation().add(random))
                gc.unload(builtFactary[i],random);
            }
        }
    }
    public void addWorker(int id){
        ids[index] = id;
        index++;
    }
    public void contReplicating(){
        Direction random = p.getRandDirection();
        for (int i = 0; i < index; i++) {
            if(gc.canMove(ids[i],random)){
                gc.canMove(ids[i],random);
            }
        }
        for (int i = 0; i < index; i++) {
            if(gc.canReplicate(ids[i],random)){
                gc.replicate(ids[i],random);
            }
        }
    }
    public boolean doneReplicating(){
        return(index > 3);
    }
    void setState(WorkerStates state){
        this.state = state;
    }
    void resetWorkerIndexCount(){
        index = 0;
        builtFactIndex = 0;
        unbuiltIndex = 0;
    }
    void contBuildingFactory(){
        Direction random = p.getRandDirection();
        for (int i = 0; i < index; i++) {
            if(gc.isMoveReady(ids[i]) && gc.canMove(ids[i],random)){
                gc.moveRobot(ids[i],random);
            }
        }
        if(unbuiltIndex == 0){
            for (int i = 0; i < index; i++) {
                if(gc.canBlueprint(ids[i], UnitType.Factory,random)){
                    gc.blueprint(ids[i], UnitType.Factory,random);
                }
            }
        } else{
            System.out.println("trying to build fact");
            for (int i = 0; i < index; i++) {
                if(gc.canBuild(ids[i], factBlueId)){
                    totalHp += 5;
                    gc.build(ids[i], factBlueId);
                }
            }
            System.out.println("this many bots tried: " + index);
            System.out.println("this is how much health the factory should have: " + totalHp);
        }
    }
    boolean doneBuildingFactory(){
        return(builtFactIndex != 0);
    }
    void contBuildingRocket(){
        //// TODO
    }
    boolean doneBuildingRocket(){
        return false;
        //todo
    }
}