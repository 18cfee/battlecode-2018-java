import bc.*;

public class Workers extends Group{
    int factBlueId = -1;
    int rocketBlueId = -1;
//    int unbuiltRocketIndex = 0;
//    int builtRocketIndex = 0;
//    int[] unbuiltRocket = new int[5];
    MapLocation harvestPoint = null;

    GameController gc;
    Path p;
    public WorkerStates state = WorkerStates.DefaultState;

    public Workers(GameController gc, Path p){
        super(gc, p);
        this.gc = gc;
        this.p = p;
    }

//    public void addRocket(Unit rocket){
//        if(rocket.structureIsBuilt() == 1){
//            //System.out.println("a rocket is built");
//            builtRocket[builtRocketIndex++] = rocket.id();
//        }else{
//            //System.out.println("There is an unfinished rocket");
//            unbuiltRocket[unbuiltRocketIndex++] = rocket.id();
//        }
//    }


    public MapLocation setBlueprint(UnitType type){
        //System.out.println("This kind: " + type);
        Direction rand = p.getRandDirection();
        System.out.println(rand);
        for(Integer id: ids){
            //System.out.println("Trying to find blueprint centerLoc, worker attempting: " + ids[i]);
            if(gc.canBlueprint(id, type, rand)){
                state = WorkerStates.Build;
                System.out.println("I found a spot to place it");
                gc.blueprint(id, type, rand);
                VecUnit units = gc.senseNearbyUnitsByType(gc.unit(id).location().mapLocation(), 50, type);
                for (int j = 0; j < units.size(); j++) {
                    if(units.get(j).structureIsBuilt() == 0 && type == UnitType.Factory){
                        factBlueId = units.get(j).id();
                        System.out.println("Factory blueprint set: ID " + factBlueId);
                        return units.get(j).location().mapLocation();
                    } else if(units.get(j).structureIsBuilt() == 0 && type == UnitType.Rocket){
                        rocketBlueId = units.get(j).id();
//                        unbuiltRocketIndex++;
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
        for(Integer id: ids){
            if(gc.canReplicate(id,random)){
                gc.replicate(id,random);
            }
        }
    }
    @Override
    public void conductTurn() throws Exception{
        // this basically makes sure things have been reset we need it at the beggining of all conduct turns for
        // groups
        if(noUnits()) return;
        //System.out.println("Total karb: " + gc.karbonite());
        System.out.println("Worker turn conducting");
        if(state == WorkerStates.Build) {
            for (Integer id: ids){
                if (p.currentBuiltFactories.size() == p.NUM_FACTORIES_WANTED) {
                    if (p.rocketIndex > 0) {
                        contBuilding(UnitType.Rocket);
                    } else {
                        setState(WorkerStates.GatherKarbonite);
                    }
                } else {
                    contBuilding(UnitType.Factory);
                }

            }
        }else if(state == WorkerStates.GatherKarbonite){
            System.out.println("About to gather karbonite");
            if(harvestPoint != null) {
                //System.out.println("There is a karbonite centerLoc");
                gatherKarbonite();
            }
        }

        //moveToTarget(hill);
    }
    public boolean doneReplicating(){
        return(size() > 3);
    }
    void setState(WorkerStates state){
        this.state = state;
    }

    void resetWorkerIndexCount(){
//        builtRocketIndex = 0;
//        unbuiltRocketIndex = 0;
    }
    void contBuilding(UnitType type) throws Exception{
        if(type == UnitType.Factory) {
            if (p.unbuiltFactIndex != 0) {
                for (Integer id: ids){
                    if (gc.canBuild(id, factBlueId)) {
                        gc.build(id, factBlueId);
                    }else{
                        moveToTarget(hill);
                    }
                }
            }
        }else{
            //System.out.println("Unbuilt rockets: " + p.rocketIndex);
            System.out.println();
            if(p.rocketIndex != 0){
                for(Integer id: ids){
                    //System.out.println("Trying to work on the rocket: " + rocketBlueId);
                    if(gc.canBuild(id, rocketBlueId)){
                        gc.build(id, rocketBlueId);
                        //System.out.println("Worked on the rocket");
                    }else{
                        moveToTarget(hill);
                    }
                }
            }
        }
    }

    void gatherKarbonite() throws Exception{
        for(Integer id: ids){
            if(gc.canHarvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint))){
                System.out.println("Karbonite harvested! I shouldn't have moved...");
                // this threw an error
                // I think this could be out of sight sometimes?System.out.println("Karbonite left at location: " + gc.karboniteAt(harvestPoint));
                gc.harvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint));
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