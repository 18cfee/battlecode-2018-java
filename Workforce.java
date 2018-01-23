import bc.*;

public class Workforce{
    private GameController gc;
    private Path p;
    private int[] idle = new int[100];
    private int idleIndex = 0;
    private Workers[] workerGroups = new Workers[10];
    private int groupIndex = 0;
    private boolean canBuildRocket = false;
    private MapLocation closestKarbDepot = null;

    public Workforce(GameController gc, Path p) {
        this.gc = gc;
        this.p = p;
        createGroup();
    }
    public void conductTurn() throws Exception{
        while(groupIndex < 2){
            createGroup();
        }

        int numWorkers = idleIndex;
        while(idleIndex > 0){
            if(idleIndex >= numWorkers/2) {
                //System.out.println("Worker " + idleIndex + " added to group 0");
                workerGroups[0].add(idle[--idleIndex]);
            }else{
                //System.out.println("Worker " + idleIndex + " added to group 1");
                workerGroups[1].add(idle[--idleIndex]);
            }
        }

        for(int i = 0; i < groupIndex; i++){
            workerGroups[i].groupIsAlive = true;
        }

        for(int i = 0; i < groupIndex - 1; i++) {
            if(workerGroups[i].noUnits()) {
                workerGroups[i].groupIsAlive = false;
            }else if (gc.round() == 1 || (numWorkers < 10 && gc.round() < 20)) {
                workerGroups[i].replicate();
            } else if (workerGroups[i].getState() == WorkerStates.Build) {

            }else if (!workerGroups[i].printInProgress && p.getNumFactories() < p.NUM_FACTORIES_WANTED){
                MapLocation blueLoc = workerGroups[i].setBlueprint(UnitType.Factory);
                if(blueLoc != null) {
                    workerGroups[i].currentHill = p.generateHill(blueLoc);
                }

            } else if (canBuildRocket && p.rockets.getNumUnBuiltRockets() == 0 && p.rockets.getNumberOfBuiltRockets() < p.NUM_ROCKETS_WANTED) {
                MapLocation blueLoc = workerGroups[i].setBlueprint(UnitType.Rocket);
                if (blueLoc != null){
                    workerGroups[i].currentHill = p.generateHill(blueLoc);
                }

            }else if (!p.closestKarbLocs.isEmpty()  && workerGroups[i].getState() != WorkerStates.SetBlueprint  || (!p.closestKarbLocs.isEmpty() && workerGroups[i].getState() == WorkerStates.GatherKarbonite)) {
                gatherKarbonite(workerGroups[i]);
            }else if(p.closestKarbLocs.isEmpty()){
                workerGroups[i].setState(WorkerStates.Standby);
            }
        }

        gatherKarbonite(workerGroups[groupIndex-1]);
        for(int i = 0; i < groupIndex; i++){
            if(workerGroups[i].groupIsAlive) {
                workerGroups[i].conductTurn();
            }
        }

        idleIndex = 0;
    }

    private void gatherKarbonite(Workers group) throws Exception{
        group.setState(WorkerStates.GatherKarbonite);
        if (closestKarbDepot == null || gc.karboniteAt(closestKarbDepot) == 0) {
            boolean viable = false;
            while (!viable && !p.closestKarbLocs.isEmpty()) {
                MapLocation newLoc = p.closestKarbLocs.pop().toMapLocation();
                //System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
                if (gc.canSenseLocation(newLoc)) {
                    if (newLoc != null && gc.karboniteAt(newLoc) != 0) {
                        viable = true;


                        short[][] hill = p.generateHill(newLoc);
                        group.currentHill = hill;
                        group.setHarvestPoint(newLoc);
                    } else {
                    }
                } else{
                    viable = true;
                    if(group.karbLocInSight){
                        short[][] hill = p.generateHill(newLoc);
                        group.setHarvestPoint(newLoc);
                        group.karbLocInSight = false;
                        group.currentHill = hill;
                    }
                }
            }
            if (!viable) {
                group.setState(WorkerStates.Standby);
            }
        }else{
            if(p.closestKarbLocs.isEmpty()){
                group.setState(WorkerStates.Standby);
            }
        }
    }
    public void createGroup(){
        workerGroups[groupIndex] = new Workers(gc, p);
        groupIndex++;
    }

    public void setCanBuildRocket(boolean set){
        canBuildRocket = set;
    }

    public boolean isCanBuildRocket(){
        return canBuildRocket;
    }

    public void addWorker(int id){
        idle[idleIndex] = id;
        idleIndex++;
        //System.out.println("Worker added to idleIndex: " + id);
    }
}
