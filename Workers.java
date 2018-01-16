import bc.*;

public class Workers extends Group{
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


    public MapLocation setBlueprint(){
//        Direction rand = p.getRandDirection();
//        System.out.println("Trying to place blueprint");
//        for (int i = 0; i < index; i++) {
//            if(gc.canBlueprint(ids[i], UnitType.Factory,rand)){
//                gc.blueprint(ids[i], UnitType.Factory,rand);
//                System.out.println("blueprint placed");
//            }
//        }
        Direction rand = p.getRandDirection();
        for(int i = 0; i <= index; i++){
            System.out.println("Trying to find blueprint loc, worker attempting: " + ids[i]);
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

    /*
    public void changeToTargetDestinationState(MapLocation loc){
        for(int i = 0; i < index; i++){
            if(gc.canBuild(ids[i], factBlueId)){
                gc.build(ids[i], factBlueId);
            }else{
                super.changeToTargetDestinationState(loc);
            }
        }
    }*/
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
    public void conductTurn() throws Exception{
        System.out.println("carb: " + gc.karbonite());
        System.out.println("Worker turn conducting");
        for(int i = 0; i < index; i++){
            /*
            if(gc.canBuild(ids[i], factBlueId)){
                gc.build(ids[i], factBlueId);
            }*/
            if(p.builtFactIndex == p.NUM_FACTORIES_WANTED){
                System.out.println("Factory complete");
                setState(WorkerStates.GatherKarbonite);
            }else{
                System.out.println("About to continue to build factory");
                contBuildingFactory();
            }

        }
        moveToTarget(hill);
        movableIndex = 0;
        index = 0;
    }
    public boolean doneReplicating(){
        return(index > 3);
    }
    void setState(WorkerStates state){
        this.state = state;
    }
    void resetWorkerIndexCount(){
        index = 0;
    }
    void contBuildingFactory(){
        if(p.unbuiltFactIndex != 0){
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
    void contBuildingRocket(){
        //// TODO
    }
    boolean doneBuildingRocket(){
        return false;
        //todo
    }
}