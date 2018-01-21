import bc.*;

public class Workers extends Group{
    private int blueID = -1;
    private MapLocation harvestPoint = null;
    GameController gc;
    private Path p;
    public WorkerStates state = WorkerStates.Standby;
    boolean printInProgress = false;
    private short[][] currentHill = null;

    public Workers(GameController gc, Path p){
        super(gc, p);
        this.gc = gc;
        this.p = p;
    }

    public MapLocation setBlueprint(UnitType type)throws Exception{
        Direction rand = p.getRandDirection();
        System.out.println(rand);
        for(Integer id: ids){
            currentHill = p.generateHill(p.baseLoc);
            state = WorkerStates.SetBlueprint;
            if(gc.unit(id).location().mapLocation().distanceSquaredTo(p.baseLoc) < 9) {

                if (gc.canBlueprint(id, type, rand)) {

                    state = WorkerStates.Build;
                    printInProgress = true;
                    gc.blueprint(id, type, rand);
                    VecUnit units = gc.senseNearbyUnitsByType(gc.unit(id).location().mapLocation(), 2, type);
                    for (int j = 0; j < units.size(); j++) {
                        if (units.get(j).structureIsBuilt() == 0 && type == UnitType.Factory) {
                            blueID = units.get(j).id();
                            return units.get(j).location().mapLocation();
                        } else if (units.get(j).structureIsBuilt() == 0 && type == UnitType.Rocket) {
                            blueID = units.get(j).id();
                            return units.get(j).location().mapLocation();
                        }
                    }
                }
            }else{
                moveToTarget(currentHill);
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
        System.out.println("Total karb: " + gc.karbonite());
        System.out.println("Worker state: " + state);
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

        //moveToTarget(hill);
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

    void standby() throws Exception{
        currentHill = p.generateHill(p.baseLoc);
        System.out.println("Trying to go to standby mode");
        for(int i = 0; i < movableIndex; i++){
            if(gc.unit(moveAbles[i]).location().mapLocation().distanceSquaredTo(p.baseLoc) > 10){
                System.out.println("In standby position");
                moveToTarget(currentHill);
            }else{
                System.out.println("Not in position yet");
            }
        }
    }
    void gatherKarbonite() throws Exception{
        for(Integer id: ids){
            if(gc.canHarvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint))){
                gc.harvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint));
            }else{
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