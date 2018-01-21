import bc.*;

public class Workers extends Group{
    int factBlueId = -1;
    int rocketBlueId = -1;
    int blueID = -1;
    MapLocation harvestPoint = null;
    GameController gc;
    Path p;
    public WorkerStates state = WorkerStates.Standby;
    boolean printInProgress = false;

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
            //System.out.println("Trying to find blueprint loc, worker attempting: " + ids[i]);
            if(gc.canBlueprint(id, type, rand)){
                state = WorkerStates.Build;
                printInProgress = true;
                System.out.println("I found a spot to place it");
                gc.blueprint(id, type, rand);
                VecUnit units = gc.senseNearbyUnitsByType(gc.unit(id).location().mapLocation(), 50, type);
                for (int j = 0; j < units.size(); j++) {
                    if(units.get(j).structureIsBuilt() == 0 && type == UnitType.Factory){
                        //factBlueId = units.get(j).id();
                        blueID = units.get(j).id();
                        System.out.println("Factory blueprint set: ID " + factBlueId);
                        return units.get(j).location().mapLocation();
                    } else if(units.get(j).structureIsBuilt() == 0 && type == UnitType.Rocket){
                        //rocketBlueId = units.get(j).id();
                        blueID = units.get(j).id();
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
        System.out.println("Worker state: " + state);
        if(state == WorkerStates.Build) {
            System.out.println("I'm in the build state, and I'm building:");
            for (Integer id: ids){
                /*
                if (p.currentBuiltFactories.size() == p.NUM_FACTORIES_WANTED) {
                    if (p.rocketIndex > 0) {
                        //contBuilding(UnitType.Rocket);
                        contBuilding();
                        System.out.println("\tA rocket!");
                    } else {
                        setState(WorkerStates.GatherKarbonite);
                    }
                } else {
                    //contBuilding(UnitType.Factory);
                    contBuilding();
                    System.out.println("\tA factory");
                }*/
                contBuilding();

            }
        }else if(state == WorkerStates.GatherKarbonite){
            System.out.println("About to gather karbonite");
            if(harvestPoint != null) {
                //System.out.println("There is a karbonite loc");
                gatherKarbonite();
            }
        }else if(state == WorkerStates.Standby){
            standby();
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

    void contBuilding() throws Exception{
        if(gc.unit(blueID).structureIsBuilt() != 1) {
            for (Integer id : ids) {
                if (gc.canBuild(id, blueID)) {
                    gc.build(id, blueID);
                } else {
                    moveToTarget(hill);
                }
            }
        }else{
            printInProgress = false;
            setState(WorkerStates.Standby);
        }
    }
    /*
    void contBuilding(UnitType type) throws Exception{
        if(type == UnitType.Factory) {
            if(gc.unit(factBlueId).structureIsBuilt() != 1) {
                for (Integer id : ids) {
                    if (gc.canBuild(id, factBlueId)) {
                        gc.build(id, factBlueId);
                    } else {
                        moveToTarget(hill);
                    }
                }
            }else{
                setState(WorkerStates.Standby);
            }
        }else{
            //System.out.println("Unbuilt rockets: " + p.rocketIndex);
            if(gc.unit(rocketBlueId).structureIsBuilt() != 1) {
                for (Integer id : ids) {
                    //System.out.println("Trying to work on the rocket: " + rocketBlueId);
                    if (gc.canBuild(id, rocketBlueId)) {
                        gc.build(id, rocketBlueId);
                        //System.out.println("Worked on the rocket");
                    } else {
                        moveToTarget(hill);
                    }
                }
            }else{
                setState(WorkerStates.Standby);
            }
        }
    }*/

    void standby() throws Exception{
        short[][] hill = p.generateHill(p.baseLoc);
        System.out.println("Trying to go to standby mode");
        for(int i = 0; i < movableIndex; i++){
            if(gc.unit(moveAbles[i]).location().mapLocation().distanceSquaredTo(p.baseLoc) > 10){
                System.out.println("In standby position");
                moveToTarget(hill);
            }else{
                System.out.println("Not in position yet");
            }
        }
    }
    void gatherKarbonite() throws Exception{
        for(Integer id: ids){
            if(gc.canHarvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint))){
                //System.out.println("Karbonite harvested! I shouldn't have moved...");
                // this threw an error
                // I think this could be out of sight sometimes?System.out.println("Karbonite left at location: " + gc.karboniteAt(harvestPoint));
                gc.harvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint));
            }else{
                //System.out.println("Couldn't harvest, so I'm going to move");
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