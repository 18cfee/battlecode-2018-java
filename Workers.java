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
    public WorkerStates state;
    public Workers(GameController gc){
        this.gc = gc;
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
        for (int i = 0; i < builtFactIndex; i++) {
            if(gc.canProduceRobot(builtFactary[i],production)){
                System.out.println("factory made a unit");
                gc.produceRobot(builtFactary[i],production);
            }
        }
        for (int i = 0; i < unbuiltIndex; i++) {
            if(gc.canProduceRobot(unbuiltFactory[i],production)){
                System.out.println("unfactory made a unit");
                gc.produceRobot(unbuiltFactory[i],production);
            }
        }
    }
    public void addWorker(int id){
        ids[index] = id;
        index++;
    }
    public void contReplicating(){
        //todo, get this from path and also make it an available direction
        Direction[] d = Direction.values();
        int rand = (int)(Math.random()*9);
        Direction random = d[rand];
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
        return(index > 30);
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
        //todo, get this from path and also make it an available direction
        Direction[] d = Direction.values();
        int rand = (int)(Math.random()*9);
        Direction random = d[rand];
        for (int i = 0; i < index; i++) {
            if(gc.canMove(ids[i],random)){
                gc.canMove(ids[i],random);
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
                    gc.canBuild(ids[i], factBlueId);
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