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
        //closestKarbDepot = p.baseLoc;
    }
    public void conductTurn() throws Exception{
        System.out.println("\n\n\n\nWorker turn starting");
        System.out.println("There are " + idleIndex + " workers");
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
            System.out.println("Work group " + i + " is in state: " + workerGroups[i].getState());
            if(workerGroups[i].noUnits()) {
                workerGroups[i].groupIsAlive = false;
            }else if (gc.round() == 1 || numWorkers < 10) {
                workerGroups[i].replicate();
            } else if (workerGroups[i].getState() == WorkerStates.Build) {
                System.out.println("Worker group " + i + " is going to just build this turn");

            }else if (!workerGroups[i].printInProgress && p.getNumFactories() < p.NUM_FACTORIES_WANTED){
                System.out.println("p.getNumFactories: " + p.getNumFactories());
                MapLocation blueLoc = workerGroups[i].setBlueprint(UnitType.Factory);

            } else if (canBuildRocket && p.rockets.getNumUnBuiltRockets() == 0 && p.rockets.getNumberOfBuiltRockets() < p.NUM_ROCKETS_WANTED){
                System.out.println("rocketIndex: " + p.rocketIndex);
                MapLocation blueLoc = workerGroups[i].setBlueprint(UnitType.Rocket);

            }else if (!p.closestKarbLocs.isEmpty()  && workerGroups[i].getState() != WorkerStates.SetBlueprint) {
                gatherKarbonite(workerGroups[i]);
            }else if(p.closestKarbLocs.isEmpty()){
                System.out.println(workerGroups[i].getState() + " but switching to Standby");
                workerGroups[i].setState(WorkerStates.Standby);
            }
        }

        gatherKarbonite(workerGroups[groupIndex-1]);
        for(int i = 0; i < groupIndex; i++){
            System.out.println("Worker group " + i + " conducting turn");
            System.out.println("Workers in group");
            for(int id : workerGroups[i].ids){
                System.out.println("Worker ID: " + id);
            }
                if(workerGroups[i].groupIsAlive == true) {
                workerGroups[i].conductTurn();
            }
        }

        idleIndex = 0;
    }

    private void gatherKarbonite(Workers group) throws Exception{
        group.setState(WorkerStates.GatherKarbonite);
        System.out.println("The workers want to gather");
        System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
        if (closestKarbDepot == null || gc.karboniteAt(closestKarbDepot) == 0) {
            boolean viable = false;
            while (!viable && !p.closestKarbLocs.isEmpty()) {
                MapLocation newLoc = p.closestKarbLocs.pop().toMapLocation();
                System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
                if (gc.canSenseLocation(newLoc)) {
                    if (newLoc != null && gc.karboniteAt(newLoc) != 0) {
                        viable = true;

                        System.out.println("A new spot was found: " + newLoc.toString());
                        System.out.println("Amount of karbs at newLoc " + gc.karboniteAt(newLoc));

                        short[][] hill = p.generateHill(newLoc);
                        group.changeToTargetMap(hill);
                        group.setHarvestPoint(newLoc);
                    } else {
                        System.out.println("No spot here!");
                    }
                } else{
                    viable = true;
                    System.out.println("Too far away, moving closer");
                    short[][] hill = p.generateHill(newLoc);
                    group.changeToTargetMap(hill);
                    group.setHarvestPoint(newLoc);
                }
            }
            if (!viable) {
                System.out.println("All out of karbonite on this planet");
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
        System.out.println("Worker added to idleIndex: " + id);
    }
}
