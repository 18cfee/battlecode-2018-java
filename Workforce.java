import bc.*;

import java.util.ArrayList;
import java.util.HashSet;

public class Workforce {
    private GameController gc;
    private Path p;
    private int[] idle = new int[100];
    private Workers[] workerGroups = new Workers[10];
    private int groupIndex = 0;
    private boolean canBuildRocket = false;
    private MapLocation closestKarbDepot = null;
    private ArrayList<Workers> gatherers;
    private ArrayList<HashSet<Integer>> oldGatherers;
    private int workerRound = 0;
    private Workers builders;
    private int numWorkers = 0;
    private HashSet<Integer> oldBuilders;
    private Workers loners;

    public Workforce(GameController gc, Path p) {
        this.gc = gc;
        this.p = p;
        createGroup();
        gatherers = new ArrayList<>();
        oldGatherers = new ArrayList<>();
        builders = new Workers(gc, p);
        loners = new Workers(gc, p);
        loners.setState(WorkerStates.CutOff);
    }

    public void conductTurn() throws Exception{

        System.out.println("There are " + numWorkers + " workers");
        System.out.println("There are " + builders.size() + " builders");
        int groupNum = 0;
        for(Workers group : gatherers){
            groupNum++;
            System.out.println("There are " + group.size() + " workers in gathering group " + groupNum);
            for(int id : group.ids){
                System.out.println("Worker " + id + " is in gathering group " + groupNum);
            }

        }
        builders.groupIsAlive = true;
        for (Workers group : gatherers) {
            group.groupIsAlive = true;
        }

        if (!builders.noUnits() && numWorkers < 10) {
            //System.out.println("Trying to replicate");
            for (int id : builders.ids) {
                builders.replicate(id);
            }
        }

        if (builders.noUnits()) {
            builders.groupIsAlive = false;
        } else if (builders.getState() == WorkerStates.Build) {

        } else if (!builders.printInProgress && p.getNumFactories() < p.NUM_FACTORIES_WANTED) {
            MapLocation blueLoc = builders.setBlueprint(UnitType.Factory);
            if (blueLoc != null) {
                builders.currentHill = p.generateHill(blueLoc);
            }
        } else if (canBuildRocket && p.rockets.getNumUnBuiltRockets() == 0 && p.rockets.getNumberOfBuiltRockets() < p.NUM_ROCKETS_WANTED) {
            MapLocation blueLoc = builders.setBlueprint(UnitType.Rocket);
            if (blueLoc != null) {
                builders.currentHill = p.generateHill(blueLoc);
            }
        } else {
            builders.setState(WorkerStates.Standby);
        }


        builders.conductTurn();
        for(Workers group : gatherers){
            if(group.groupIsAlive) {
                gatherKarbonite(group);
            }
        }
        for (Workers group : gatherers) {
            if (group.groupIsAlive) {
                group.conductTurn();
            }
        }
    }

    private void gatherKarbonite(Workers group) throws Exception {
        group.setState(WorkerStates.GatherKarbonite);
        if(closestKarbDepot != null){
            if(gc.canSenseLocation(closestKarbDepot)) {
                System.out.println("Karbonite at last location: " + gc.karboniteAt(closestKarbDepot));
            }else{
                System.out.println("Location is out of sight");
            }
        }
        if (closestKarbDepot == null || (gc.canSenseLocation(closestKarbDepot) && gc.karboniteAt(closestKarbDepot) == 0)) {
            if(closestKarbDepot != null) {
                //System.out.println("Need a new location");
            }
            boolean viable = false;

            while (!viable && !p.closestKarbLocs.isEmpty()) {
                if (p.closestKarbLocs.peek() != null && gc.canSenseLocation(p.closestKarbLocs.peek().toMapLocation())) {
                    closestKarbDepot = p.closestKarbLocs.pop().toMapLocation();
                    System.out.println("New harvest loc picked out: " + closestKarbDepot.toString());
                    System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
                    if (gc.karboniteAt(closestKarbDepot) != 0) {
                        viable = true;
                        group.karbLocInSight = true;
                    }
                } else {
                    viable = true;
                    if (group.karbLocInSight) {
                        short[][] hill = p.generateHill(p.closestKarbLocs.peek().toMapLocation());
                        group.setHarvestPoint(p.closestKarbLocs.peek().toMapLocation());
                        group.karbLocInSight = false;
                        group.currentHill = hill;
                    }
                    group.karbLocInSight = false;
                }
            }
            if (!viable) {
                group.setState(WorkerStates.Standby);
                return;
            }
        } else {
            group.karbLocInSight = false;
            if (p.closestKarbLocs.isEmpty()) {
                group.setState(WorkerStates.Standby);
                return;
            }
        }
        if(group.karbLocInSight) {
            short[][] hill = p.generateHill(closestKarbDepot);
            group.setHarvestPoint(closestKarbDepot);
            group.currentHill = hill;
        }
    }

    private void createGroup() {
        workerGroups[groupIndex] = new Workers(gc, p);
        groupIndex++;
    }

    public void setCanBuildRocket(boolean set) {
        canBuildRocket = set;
    }

    public boolean isCanBuildRocket() {
        return canBuildRocket;
    }


    private int numWorkerGroups = 0;
    public void addWorker(int id) throws Exception{
        if(workerRound != p.round) {
            workerRound = p.round;
            oldGatherers.clear();
            numWorkerGroups = gatherers.size();
            oldBuilders = (HashSet<Integer>) builders.ids.clone();

            for (int i = 0; i < numWorkerGroups; i++) {
                oldGatherers.add((HashSet<Integer>)gatherers.get(i).ids.clone());
            }
            gatherers.clear();
            for(int i = 0; i < numWorkerGroups; i++){
                gatherers.add(new Workers(gc, p));
            }
            numWorkers = 0;
        }
        if(p.movesToBase(gc.unit(id).location().mapLocation()) == 0 && gc.unit(id).location().mapLocation() != p.baseLoc){
            loners.add(id);
            System.out.println("Worker " + id + " added to loners");
            return;
        }else {
            numWorkers++;
        }
        if(oldBuilders.contains(id) && builders.size() < 4){
            System.out.println("Worker " + id + " added to builders");
            builders.add(id);
            return;
        }

        if(builders.size() < 4){
            System.out.println("Worker " + id + " added to builders");
            builders.add(id);
            return;
        }

        if(oldGatherers.size() > 0) {
            for (int i = 0; i < oldGatherers.size(); i++) {
                if (oldGatherers.get(i).contains(id)) {
                    gatherers.get(i).add(id);
                    System.out.println("Worker " + id + " added to gatherering group " + i + " from OldGatherers");
                    return;
                }
            }
        }

        if(gatherers.size() == 0){
            gatherers.add(new Workers(gc, p));
        }

        for (int i = 0; i < numWorkerGroups; i++) {
            if (gatherers.get(i).size() < 3) {
                gatherers.get(i).add(id);
                System.out.println("Worker " + id + " added to gathering group " + i);
                return;
            } else if (i == gatherers.size() - 1) {
                gatherers.add(new Workers(gc, p));
            }
        }
    }
}