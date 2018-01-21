import bc.*;

public class Workforce{
    GameController gc;
    Path p;
    int[] idle = new int[100];
    int idleIndex = 0;
    Workers[] workerGroups = new Workers[10];
    int groupIndex = 0;
    boolean canBuildRocket = false;
    MapLocation closestKarbDepot;
    int numWorkers = 0;

    public Workforce(GameController gc, Path p) {
        this.gc = gc;
        this.p = p;
        createGroup();
        closestKarbDepot = p.baseLoc;
    }
    public void conductTurn() throws Exception{

        System.out.println("There are " + idleIndex + " workers");
        while(groupIndex < 2){
            createGroup();
        }

        numWorkers = idleIndex;
        while(idleIndex > 0){
            if(idleIndex >= numWorkers/2) {
                //System.out.println("Worker " + idleIndex + " added to group 0");
                workerGroups[0].add(idle[--idleIndex]);
            }else{
                //System.out.println("Worker " + idleIndex + " added to group 1");
                workerGroups[1].add(idle[--idleIndex]);
            }
        }

        System.out.println("Unbuilt fact index " + p.unbuiltFactIndex);
        System.out.println("p.currentBuiltFactories: " + p.currentBuiltFactories.size());
        System.out.println("p.getNumFactories: " + p.getNumFactories());
        for(int i = 0; i < groupIndex; i++) {
            System.out.println("Work group " + i + " is in state: " +workerGroups[i].getState());
            if (gc.round() == 1 || numWorkers < 10) {
                System.out.println("Worker group " + i + " is trying to replicate");
                workerGroups[i].replicate();
            } else if (p.unbuiltFactIndex == p.currentBuiltFactories.size() && p.getNumFactories() < p.NUM_FACTORIES_WANTED){
                MapLocation blueLoc = workerGroups[i].setBlueprint(UnitType.Factory);
                if (blueLoc != null) {
                    short[][] hill = p.generateHill(blueLoc);
                    workerGroups[i].changeToTargetMap(hill);
                    p.unbuiltFactIndex++;
                }
            } else if (canBuildRocket && p.rocketIndex == 0) {
                //System.out.println("Let's build a rocket!");
                MapLocation blueLoc = workerGroups[i].setBlueprint(UnitType.Rocket);
                if (blueLoc != null) {
                    short[][] hill = p.generateHill(blueLoc);
                    p.firstRocketLocHill = hill;
                    workerGroups[i].changeToTargetMap(hill);
                    p.rocketIndex++;
                    //hillChosen = true;
                }
            }else if (!p.closestKarbLocs.isEmpty()) {
                workerGroups[i].setState(WorkerStates.GatherKarbonite);
                System.out.println("The workers want to gather");
                System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
                if (gc.karboniteAt(closestKarbDepot) == 0) {
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
                                workerGroups[i].changeToTargetMap(hill);
                                workerGroups[i].setHarvestPoint(newLoc);
                                System.out.println("There was!");
                            } else {
                                System.out.println("No spot here!");
                            }
                        } else{
                            viable = true;
                            System.out.println("Too far away, moving closer");
                            short[][] hill = p.generateHill(newLoc);
                            workerGroups[i].changeToTargetMap(hill);
                            workerGroups[i].setHarvestPoint(newLoc);
                        }
                    }
                    if (!viable) {
                        System.out.println("All out of karbonite on this planet");
                        workerGroups[i].setState(WorkerStates.Standby);
                    }
                }
            }
        }

        for(int i = 0; i < groupIndex; i++){
            workerGroups[i].conductTurn();
        }
        for(int i = 0; i < groupIndex; i++){
            workerGroups[i].resetWorkerIndexCount();
        }
        idleIndex = 0;
    }


//    public void addRocket(Unit unit){
//        workerGroups[0].addRocket(unit);
//    }

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
        /*use this code later
        boolean present = false;
        for(int i = 0; i <= idleIndex; i++){
            if(idle[i] == id){
                present = true;
                break;
            }
        }

        if(!present){
            System.out.println("Adding worker to the idle list");
            idle[idleIndex] = id;
            idleIndex++;
        }*/

        idle[idleIndex] = id;
        idleIndex++;


    }
}
