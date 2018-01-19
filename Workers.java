import bc.*;

public class Workers extends Group{
    int factBlueId = -1;
    int rocketBlueId = -1;
    int unbuiltRocketIndex = 0;
    int builtRocketIndex = 0;
    int[] unbuiltRocket = new int[5];
    int[] builtRocket = new int[10];
    MapLocation harvestPoint = null;

    GameController gc;
    Path p;
    public WorkerStates state = WorkerStates.DefaultState;

    public Workers(GameController gc, Path p){
        super(gc, p);
        this.gc = gc;
        this.p = p;
    }

    public void addRocket(Unit rocket){
        if(rocket.structureIsBuilt() == 1){
            //System.out.println("a rocket is built");
            builtRocket[builtRocketIndex++] = rocket.id();
        }else{
            //System.out.println("There is an unfinished rocket");
            unbuiltRocket[unbuiltRocketIndex++] = rocket.id();
        }
    }


    public MapLocation setBlueprint(UnitType type){
        //System.out.println("This kind: " + type);
        Direction rand = p.getRandDirection();
        System.out.println(rand);
        for(int i = 0; i <= index; i++){
            //System.out.println("Trying to find blueprint loc, worker attempting: " + ids[i]);
            if(gc.canBlueprint(ids[i], type, rand)){
                state = WorkerStates.Build;
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

    public void replicate(){
        Direction random = p.getRandDirection();
        for (int i = 0; i < index; i++) {
            if(gc.canReplicate(ids[i],random)){
                gc.replicate(ids[i],random);
            }
        }
    }
    @Override
    public void conductTurn() throws Exception{
        //System.out.println("Total karb: " + gc.karbonite());
        System.out.println("Worker turn conducting");
        if(state == WorkerStates.Build) {
            for (int i = 0; i < index; i++) {
                if (p.builtFactIndex == p.NUM_FACTORIES_WANTED) {
                    //System.out.println("Factory complete");
                    if (unbuiltRocketIndex > 0) {
                        //System.out.println("About to continue building a rocket");
                        contBuilding(UnitType.Rocket);
                    } else {
                        //System.out.println("Nothing to build");
                        setState(WorkerStates.GatherKarbonite);
                    }
                } else {
                    //System.out.println("About to continue to build factory");
                    contBuilding(UnitType.Factory);
                }

            }
        }else if(state == WorkerStates.GatherKarbonite){
            System.out.println("About to gather karbonite");
            if(harvestPoint != null) {
                //System.out.println("There is a karbonite loc");
                gatherKarbonite();
            }
        }

        //moveToTarget(hill);
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
    void contBuilding(UnitType type) throws Exception{
        if(type == UnitType.Factory) {
            //System.out.println("UnbuiltIndex = " + p.unbuiltFactIndex);
            if (p.unbuiltFactIndex != 0) {
                for (int i = 0; i < index; i++) {
                    if (gc.canBuild(ids[i], factBlueId)) {
                        gc.build(ids[i], factBlueId);
                    }else{
                        moveToTarget(hill);
                    }
                }
            }
        }else{
            //System.out.println("Unbuilt rockets: " + p.rocketIndex);
            System.out.println();
            if(p.rocketIndex != 0){
                for(int i = 0; i < index; i++){
                    //System.out.println("Trying to work on the rocket: " + rocketBlueId);
                    if(gc.canBuild(ids[i], rocketBlueId)){
                        gc.build(ids[i], rocketBlueId);
                        //System.out.println("Worked on the rocket");
                    }else{
                        moveToTarget(hill);
                    }
                }
            }
        }
    }

    void gatherKarbonite() throws Exception{
        for(int i = 0; i < index; i++){
            if(gc.canHarvest(ids[i], gc.unit(ids[i]).location().mapLocation().directionTo(harvestPoint))){
                System.out.println("Karbonite harvested! I shouldn't have moved...");
                gc.harvest(ids[i], gc.unit(ids[i]).location().mapLocation().directionTo(harvestPoint));
            }else{
                System.out.println("Couldn't harvest, so I'm going to move");
                moveToTarget(hill);
            }
        }
    }

    public void setHarvestPoint(MapLocation harvestPoint) {
        this.harvestPoint = harvestPoint;
    }

    public WorkerStates getState(){
        return state;
    }

}