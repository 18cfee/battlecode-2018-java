import bc.*;

public class Workforce{
    GameController gc;
    Path p;
    int[] idle = new int[100];
    int idleIndex = 0;
    Workers[] workerGroups = new Workers[10];
    int groupIndex = 0;
    boolean canBuildRocket = false;
    MapLocation closestKarbDepot = null;
    int numWorkers = 0;

    public Workforce(GameController gc, Path p) {
        this.gc = gc;
        this.p = p;
        createGroup();
    }
    public void conductTurn() throws Exception{

        System.out.println("There are " + idleIndex + " workers");
        if(groupIndex == 0){
            createGroup();
        }
        numWorkers = idleIndex;
        while(idleIndex > 0){
            workerGroups[0].add(idle[--idleIndex]);
        }
        if(gc.round() == 1 || numWorkers < 10){
            workerGroups[0].replicate();
        }else {
            System.out.println("built: " + p.currentBuiltFactories.size());
            System.out.println("unbuilt: " + p.getNumFactories());
            if (p.unbuiltFactIndex == p.currentBuiltFactories.size() && p.getNumFactories() < p.NUM_FACTORIES_WANTED) {
                //System.out.println("There aren't any factories yet");
                MapLocation blueLoc = workerGroups[0].setBlueprint(UnitType.Factory);
                if (p.baseLoc == null) {
                    //p.baseLoc = blueLoc;
                    //p.hillToBase = p.generateHill(p.startLoc);
                }
                if (blueLoc != null) {
                    Hill hill = new Hill(p);
                    hill.generateCompleteReachableHill(blueLoc);
                    workerGroups[0].changeToTargetMap(hill);
                    p.unbuiltFactIndex++;
                    System.out.println("num fact: " + p.getNumFactories());
                }
            }
        }

        //System.out.println("p.RocketIndex = " + p.getNumRockets());
        if(canBuildRocket && p.rocketIndex == 0){
            //System.out.println("Let's build a rocket!");
            MapLocation blueLoc = workerGroups[0].setBlueprint(UnitType.Rocket);
            if(blueLoc != null){
                Hill hill = new Hill(p);
                hill.generateCompleteReachableHill(blueLoc);
                p.firstRocketLocHill = hill;
                workerGroups[0].changeToTargetMap(hill);
                p.rocketIndex++;
                //hillChosen = true;
            }
        }

        System.out.println("Do the workers want to gather?");
        if(workerGroups[0].getState() == WorkerStates.GatherKarbonite){
            System.out.println("They do! But is there a karbLocs?");
            System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
            if(closestKarbDepot == null || gc.karboniteAt(closestKarbDepot) == 0) {
                boolean viable = false;
                while (!viable && !p.closestKarbLocs.isEmpty()) {
                    MapLocation newLoc = p.closestKarbLocs.pop().toMapLocation();
                    System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
                    if(gc.canSenseLocation(newLoc)) {
                        if (newLoc != null && gc.karboniteAt(newLoc) != 0) {
                            viable = true;
                            //todo I got a null pointer on the following line for your info jase I think it was cause of the peek code use when the Q was empty
                            //System.out.println("A new spot was found: " + p.closestKarbLocs.peek().toMapLocation().toString());
//
//                            if (gc.canSenseLocation(p.closestKarbLocs.peek().toMapLocation())) {
//                                System.out.println("Amount of karbs at location = " + gc.karboniteAt(p.closestKarbLocs.peek().toMapLocation()));
//                            } else {
//                                System.out.println("But we can't see it from here");
//                            }

                            Hill hill = new Hill(p);
                            hill.generateCompleteReachableHill(newLoc);
                            workerGroups[0].changeToTargetMap(hill);
                            workerGroups[0].setHarvestPoint(newLoc);
                            System.out.println("There was!");
                        } else {
                            System.out.println("No spot here!");
                        }
                    }else{
                        System.out.println("Too far away, moving closer");
                        Hill hill = new Hill(p);
                        hill.generateCompleteReachableHill(newLoc);
                        workerGroups[0].changeToTargetMap(hill);
                        workerGroups[0].setHarvestPoint(newLoc);
                    }
                }
                if(!viable){
                    System.out.println("All out of karbonite on this planet");
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
