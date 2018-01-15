import bc.*;

public class Workers extends Group{
    int [] unbuiltFactory = new int [5];
    int unbuiltIndex = 0;
    int builtFactIndex = 0;
    int [] builtFactary = new int [50];
    int factBlueId = -1;
    int totalHp = 0;
    GameController gc;
    Path p;
    int[] individuals = new int[10];
    public WorkerStates state;

    public Workers(GameController gc, Path p){
        super(gc, p);
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

    public MapLocation setBlueprint(){
        /*working code
        System.out.println("Trying to place blueprint");
            for (int i = 0; i < index; i++) {
                if(gc.canBlueprint(ids[i], UnitType.Factory,random)){
                    gc.blueprint(ids[i], UnitType.Factory,random);
                    System.out.println("blueprint placed");
                }
            }
         */
        Direction rand = p.getRandDirection();
        for(int i = 0; i <= index; i++){
            System.out.println("Trying to find blueprint loc, worker attempting: " + gc.unit(ids[i]).toString());
            if(gc.canBlueprint(ids[i], UnitType.Factory, rand)){
                System.out.println("I found a spot to place it");
                gc.blueprint(ids[i], UnitType.Factory, rand);
                VecUnit unit = gc.senseNearbyUnitsByType(gc.unit(ids[i]).location().mapLocation(), 50, UnitType.Factory);
                System.out.println("This is the unit found nearby: " + unit.toString());
                factBlueId = unit.get(0).id();
                System.out.println("Factory blueprint set: ID " + factBlueId);
                return unit.get(0).location().mapLocation();
            }
        }
        return null;
    }

    public void changeToTargetDestinationState(MapLocation loc){
        for(int i = 0; i < index; i++){
            if(gc.canBuild(ids[i], factBlueId)){
                gc.build(ids[i], factBlueId);
            }else{
                super.changeToTargetDestinationState(loc);
            }
        }
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
    @Override
    public void conductTurn(){
        System.out.println("Worker turn conducting");
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
//        for (int i = 0; i < index; i++) {
//            if(gc.isMoveReady(ids[i]) && gc.canMove(ids[i],random)){
//                gc.moveRobot(ids[i],random);
//            }
//        }
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