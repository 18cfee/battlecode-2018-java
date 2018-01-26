import bc.*;

public class Workers extends Group{
    private int blueID = -1;
    private MapLocation harvestPoint = null;
    GameController gc;
    private Path p;
    public WorkerStates state = WorkerStates.Standby;
    boolean printInProgress = false;
    short[][] currentHill = null;
    boolean groupIsAlive = false;
    boolean karbLocInSight = true;

    public Workers(GameController gc, Path p){
        super(gc, p);
        this.gc = gc;
        this.p = p;
    }

    public MapLocation setBlueprint(UnitType type)throws Exception{
        if(state != WorkerStates.SetBlueprint){
            state = WorkerStates.SetBlueprint;
            currentHill = p.hillToBase;
        }

        changeToTargetMap(currentHill);
        for(Integer id: ids){
            if(!p.sensableUnitNotInGarisonOrSpace(id)){
              // DO NOTHING WITH THIS UNIT
            } else if(gc.unit(id).location().mapLocation().distanceSquaredTo(p.baseLoc) < 12) {
                for (Direction d: p.directions) {
                    if (gc.canBlueprint(id, type, d)) {
                        state = WorkerStates.Build;
                        printInProgress = true;
                        gc.blueprint(id, type, d);
                        VecUnit units = gc.senseNearbyUnitsByType(gc.unit(id).location().mapLocation(), 2, type);
                        for (int j = 0; j < units.size(); j++) {
                            if (units.get(j).structureIsBuilt() == 0 && (type == UnitType.Factory || type == UnitType.Rocket) && units.get(j).team() == gc.team()) {
                                blueID = units.get(j).id();
                                return units.get(j).location().mapLocation();
                            }
                        }
                    }else{
                    }
                }
            }else{
                moveToTarget(currentHill);
            }
        }
        return null;
    }

    public void replicate(int id){
        for(Direction d: p.directions){
            if(gc.canReplicate(id,d)){
                gc.replicate(id,d);
                break;
            }
        }
    }

    @Override
    public void conductTurn() throws Exception{
        // this basically makes sure things have been reset we need it at the beggining of all conduct turns for
        // groups
        if(noUnits()) return;

        if(state == WorkerStates.Build) {
            //System.out.println("I'm in the build state, and I'm building:");
            for (Integer id: ids){
                contBuilding();
            }
        }else if(state == WorkerStates.GatherKarbonite){
            if(harvestPoint != null) {
                gatherKarbonite();
            }
        }else if(state == WorkerStates.Standby){
            standby();
        }

        moveToTarget(currentHill);
    }

    void setState(WorkerStates state){
        this.state = state;
    }

    void contBuilding() throws Exception{
        if(gc.canSenseUnit(blueID)) {
            if (gc.unit(blueID).structureIsBuilt() != 1) {
                for (Integer id : ids) {
                    if (gc.canBuild(id, blueID)) {
                        gc.build(id, blueID);
                    } else {
                        moveToTarget(currentHill);
                    }
                }
            } else {
                printInProgress = false;
                setState(WorkerStates.Standby);
            }
        }else{
            state = WorkerStates.Standby;
        }
    }

    void standby() throws Exception{
        //System.out.println("Trying to go to standby mode");
        moveToTarget(p.hillToBase);
    }
    void gatherKarbonite() throws Exception{
//        System.out.println("Harvest point: " + harvestPoint.toString());
        for(Integer id: ids){
            //System.out.println("Worker ID of gatherer: " + id);
            if(gc.canSenseUnit(id)) {
//                System.out.println("Worker loc: " + gc.unit(id).location().mapLocation().toString());
//                System.out.println("Adjacent?\t" + gc.unit(id).location().mapLocation().isAdjacentTo(harvestPoint));
                if (gc.canHarvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint))) {
//                    System.out.println("Harvested");
                    gc.harvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint));
                 } else {
//                    System.out.println("Did not harvest");
                    moveToTarget(currentHill);
                }
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