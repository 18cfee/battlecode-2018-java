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
                System.out.println("This group is dead");
            }else if (gc.round() == 1 || numWorkers < 10) {
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
        System.out.println("Starting to harvest");
        group.setState(WorkerStates.GatherKarbonite);
        if(closestKarbDepot == null){
            System.out.println("There is no location yet");
        }else{
            System.out.println(gc.karboniteAt(closestKarbDepot) + " karbonite at last location");
        }
        if (closestKarbDepot == null || gc.karboniteAt(closestKarbDepot) < 3) {
            System.out.println("Either no location yet or the last one is empty");
            boolean viable = false;
            while (!viable && !p.closestKarbLocs.isEmpty()) {
                System.out.println("Trying a new location");
                if(p.closestKarbLocs.peek() != null && gc.canSenseLocation(p.closestKarbLocs.peek().toMapLocation())) {
                    if(closestKarbDepot != null) {
                        System.out.println("There is " + gc.karboniteAt(closestKarbDepot) + " karbonite at last location, moving on!");
                    }
                    closestKarbDepot = p.closestKarbLocs.pop().toMapLocation();
                    System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
                    if (closestKarbDepot != null && gc.karboniteAt(closestKarbDepot) != 0) {
                        System.out.println("This is not a viable location");
                        viable = true;
                        if(!group.karbLocInSight) {
                            short[][] hill = p.generateHill(closestKarbDepot);
                            group.currentHill = hill;
                            group.setHarvestPoint(closestKarbDepot);
                        }
                        group.karbLocInSight = true;
                    }
                }else{
                    viable = true;
                    if (group.karbLocInSight) {
                        short[][] hill = p.generateHill(p.closestKarbLocs.peek().toMapLocation());
                        group.setHarvestPoint(p.closestKarbLocs.peek().toMapLocation());
                        group.karbLocInSight = false;
                        group.currentHill = hill;
                    }
                    System.out.println("Can't see location, moving toward it");
                    group.karbLocInSight = false;
                }
            }
            if (!viable) {
                group.setState(WorkerStates.Standby);
            }
        }else{
            if(p.closestKarbLocs.isEmpty()){
                System.out.println("The world is picked clean, going to standby");
                group.setState(WorkerStates.Standby);
            }
            System.out.println("Still karbs at loc, staying the course");
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
