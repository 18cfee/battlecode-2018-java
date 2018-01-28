import bc.*;

import java.util.BitSet;

public class Workers extends Group{
    private int blueID = -1;
    MapLocation harvestPoint = null;
    GameController gc;
    private Path p;
    public WorkerStates state = WorkerStates.Standby;
    boolean printInProgress = false;
    short[][] currentHill = null;
    boolean groupIsAlive = false;
    boolean karbLocInSight = false;
    boolean onWayToOutofSight = true;
    int karbsHarvested = 0;
    MPQ personalPQ;
    short[][] hillToBase;


    public Workers(GameController gc, Path p, MPQ closestKarbLocs, short[][] baseHill){
        super(gc, p);
        this.gc = gc;
        this.p = p;
        hillToBase = baseHill;
        personalPQ  = new MPQ(closestKarbLocs.getSize(), p);
    }

    public MapLocation setBlueprint(UnitType type)throws Exception{
        if(state != WorkerStates.SetBlueprint) {
            state = WorkerStates.SetBlueprint;
            currentHill = p.hillToBase;
        }

        changeToTargetMap(currentHill);
        BitSet[] areasContainingStructures = areasContainingStructures = p.rockets.getStructArea();
//        if(type == UnitType.Rocket){
//            areasContainingStructures = p.rockets.getStructArea();
//        }
        for(Integer id: ids){
            MapLocation unitLoc = p.getMapLocationIfLegit(id);
            if(unitLoc == null){
              // DO NOTHING WITH THIS UNIT
            } else if(unitLoc.distanceSquaredTo(p.baseLoc) < p.maxDistanceFromBase) {
                for (Direction d: p.directions) {
                    if (gc.canBlueprint(id, type, d) && p.rockets.notPlacingRocketbyOtherStruct(areasContainingStructures, id, d)) {
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
                    }
                }
                // if they did not place but are in the base area
                p.moveIfPossible(id);
            }else{
                moveDownHill(id,currentHill);
            }
        }
        return null;
    }

    public boolean replicate(int id){
        for(Direction d: p.directions){
            if(gc.canReplicate(id,d)){
                //System.out.println("did replicate");
                gc.replicate(id,d);
                return true;
            }
        }
        return false;
    }

    @Override
    public void conductTurn() throws Exception{
        // this basically makes sure things have been reset we need it at the beggining of all conduct turns for
        // groups
        karbsHarvested = 0;
        if(noUnits()) return;

        if(state == WorkerStates.CutOff){
            for(int id : ids){
                handleCutOff(id);
            }
        }
        if(p.round >= 700){
            state = WorkerStates.Standby;
        }
        if(state == WorkerStates.Build) {
            //System.out.println("I'm in the build state, and I'm building:");
            contBuilding();
        }else if(state == WorkerStates.GatherKarbonite){
            //System.out.println("Checking to see if can harvest");
            if(harvestPoint != null) {
                //System.out.println("Starting the harvest for the gods");
                karbsHarvested = gatherKarbonite();
            }
        }else if(state == WorkerStates.Standby){
            standby();
        }

        moveToTarget(currentHill);
    }

    void setState(WorkerStates state){
        this.state = state;
    }

    private void contBuilding() throws Exception{
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
        for(Direction d: p.directions){
            for(int id : ids){
                if(gc.canHarvest(id, d)){
                    gc.harvest(id, d);
                }
            }
        }
        moveToTarget(hillToBase);
    }

    int gatherKarbonite() throws Exception{
        int karbsAtLoc;
        if(gc.canSenseLocation(harvestPoint)){
            karbsAtLoc = (int)gc.karboniteAt(harvestPoint);
        }else{
            karbsAtLoc = 0;
        }
        int totalKarbsHarvested = 0;
        for(Integer id: ids){
            //System.out.println("Worker ID of gatherer: " + id);
            if(gc.canSenseUnit(id)) {
                //System.out.println("Worker loc: " + gc.unit(id).location().mapLocation().toString());
                //System.out.println("Adjacent? " + gc.unit(id).location().mapLocation().isAdjacentTo(harvestPoint));
                if (gc.canHarvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint))) {
                    gc.harvest(id, gc.unit(id).location().mapLocation().directionTo(harvestPoint));
                    //System.out.println("Harvest successful");
                 } else {
                    moveToTarget(currentHill);
                    for(Direction d : p.directions){
                        if(gc.canHarvest(id, d)){
                            gc.harvest(id, d);
                        }
                    }
                    //System.out.println("Could not harvest");
                }
                if (gc.canSenseLocation(harvestPoint)) {
                    totalKarbsHarvested = Math.abs(karbsAtLoc - (int)gc.karboniteAt(harvestPoint));
                } else {
                    totalKarbsHarvested = 0;
                }
                if(p.round % 3 == 0){
                    VecMapLocation locs = gc.allLocationsWithin(gc.unit(id).location().mapLocation(), (long)Math.sqrt(gc.unit(id).visionRange()));
                    for(int i = 0; i < locs.size(); i++){
                        if(gc.karboniteAt(locs.get(i)) > 0){
                            personalPQ.insert(new MapLoc(p.planet, locs.get(i), locs.get(i).distanceSquaredTo(gc.unit(id).location().mapLocation())));
                        }
                    }
                }
                moveToTarget(currentHill);
            }
        }

        return totalKarbsHarvested;
    }


    public WorkerStates getState(){
        return state;
    }

    private void handleCutOff(int id){
        for(Direction d : p.directions){
            if(gc.canHarvest(id, d)){
                gc.harvest(id, d);
            }
        }
        for(Direction d : p.directions){
            if(gc.canMove(id, d)){
                gc.moveRobot(id, d);
                return;
            }
        }
    }

}