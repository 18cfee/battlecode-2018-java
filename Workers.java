import bc.*;

public class Workers extends Group{
    int factBlueId = -1;
    int rocketBlueId = -1;
    int unbuiltRocketIndex = 0;
    int builtRocketIndex = 0;
    int[] unbuiltRocket = new int[5];
    int[] builtRocket = new int[10];

    GameController gc;
    Path p;
    int[] individuals = new int[10];
    public WorkerStates state;

    public Workers(GameController gc, Path p){
        super(gc, p);
        this.gc = gc;
        this.p = p;
    }

    public void addRocket(Unit rocket){
        if(rocket.structureIsBuilt() == 1){
            System.out.println("a rocket is built");
            builtRocket[builtRocketIndex++] = rocket.id();
        }else{
            System.out.println("There is an unfinished rocket");
            unbuiltRocket[unbuiltRocketIndex++] = rocket.id();
        }
    }


    public MapLocation setBlueprint(UnitType type){
        System.out.println("This kind: " + type);
        Direction rand = p.getRandDirection();
        for(int i = 0; i <= index; i++){
            System.out.println("Trying to find blueprint loc, worker attempting: " + ids[i]);
            if(gc.canBlueprint(ids[i], type, rand)){
                System.out.println("I found a spot to place it");
                gc.blueprint(ids[i], type, rand);
                VecUnit units = gc.senseNearbyUnitsByType(gc.unit(ids[i]).location().mapLocation(), 50, type);
                for (int j = 0; j < units.size(); j++) {
                    if(units.get(j).structureIsBuilt() == 0 && type == UnitType.Factory){
                        factBlueId = units.get(j).id();
                        System.out.println("Factory blueprint set: ID " + factBlueId);
                        return units.get(j).location().mapLocation();
                    } else if(units.get(j).structureIsBuilt() == 0 && type == UnitType.Rocket){
                        rocketBlueId = units.get(j).id();
                        unbuiltRocketIndex++;
                        System.out.println("Rocker blueprint set: ID " + rocketBlueId);
                        return units.get(j).location().mapLocation();
                    }
                }

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
                if(unbuiltRocketIndex > 0){
                    System.out.println("About to continue building a rocket");
                    contBuilding(UnitType.Rocket);
                }else{
                    System.out.println("Nothing to build");
                    setState(WorkerStates.GatherKarbonite);
                }
            }else{
                System.out.println("About to continue to build factory");
                contBuilding(UnitType.Factory);
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
        builtRocketIndex = 0;
        unbuiltRocketIndex = 0;
    }
    void contBuilding(UnitType type){
        if(type == UnitType.Factory) {
            System.out.println("UnbuiltIndex = " + p.unbuiltFactIndex);
            if (p.unbuiltFactIndex != 0) {
                System.out.println("trying to build fact");
                for (int i = 0; i < index; i++) {
                    if (gc.canBuild(ids[i], factBlueId)) {
                        gc.build(ids[i], factBlueId);
                    }
                }
            }
        }else{
            System.out.println("Unbuilt rockets: " + unbuiltRocketIndex);
            if(unbuiltRocketIndex != 0){
                for(int i = 0; i < index; i++){
                    if(gc.canBuild(ids[i], rocketBlueId)){
                        gc.build(ids[i], rocketBlueId);
                    }
                }
            }
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