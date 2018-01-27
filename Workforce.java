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
    private ArrayList<Integer> nonReplicatable;

    public Workforce(GameController gc, Path p) {
        this.gc = gc;
        this.p = p;
        createGroup();
        gatherers = new ArrayList<>();
        oldGatherers = new ArrayList<>();
        builders = new Workers(gc, p);
        loners = new Workers(gc, p);
        loners.setState(WorkerStates.CutOff);
        nonReplicatable = new ArrayList<>();
    }

    public void conductTurn() throws Exception{

        System.out.println("There are " + numWorkers + " workers");
        System.out.println("There are " + builders.size() + " builders");
        int groupNum = 0;
        for(Workers group : gatherers){
            groupNum++;
            System.out.println("There are " + group.size() + " workers in gathering group " + groupNum);
            for(int id : group.ids){
                //System.out.println("Worker " + id + " is in gathering group " + groupNum);
            }

        }
        builders.groupIsAlive = true;
        for(int i = 0; i < gatherers.size(); i++){
            if(gatherers.get(i).size() == 0){
                gatherers.remove(i);
            }else{
                gatherers.get(i).groupIsAlive = true;
            }
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
                System.out.println("/**********************************************/");
                System.out.println("Starting \"gatherKarbonite\"");
                gatherKarbonite(group);
                System.out.println("/**********************************************/");
            }else{
                System.out.println("group is dead");
            }
        }
        for (Workers group : gatherers) {
            if (group.groupIsAlive) {
                System.out.println("/**********************************************/");
                System.out.println("Starting \"conductTurn\"");
                group.conductTurn();
                System.out.println("/**********************************************/");
            }else{
                System.out.println("group is uber-dead");
            }
        }
    }

    private void gatherKarbonite(Workers group){
        group.setState(WorkerStates.GatherKarbonite);
        if(group.harvestPoint != null){
            if(gc.canSenseLocation(group.harvestPoint)) {
                System.out.println("Karbonite at last location: " + gc.karboniteAt(group.harvestPoint));
                System.out.println("Location is " + group.harvestPoint.toString());
            }else{
                System.out.println("Location is out of sight");
            }
        }else{
            System.out.println("The last location is null");
        }
        if (group.harvestPoint == null || (gc.canSenseLocation(group.harvestPoint) && gc.karboniteAt(group.harvestPoint) == 0)) {
            boolean viable = false;

            while (!viable && !p.closestKarbLocs.isEmpty()) {
                if (p.closestKarbLocs.peek() != null && gc.canSenseLocation(p.closestKarbLocs.peek().toMapLocation())) {
                    group.harvestPoint = p.closestKarbLocs.pop().toMapLocation();
                    System.out.println("New harvest loc picked out: " + group.harvestPoint.toString());
                    System.out.println("PQ says there are " + p.closestKarbLocs.getSize() + " deposits left");
                    if (gc.karboniteAt(group.harvestPoint) != 0) {
                        viable = true;
                        if(group.karbLocInSight){
                            short[][] hill = p.generateHill(group.harvestPoint);
                            group.currentHill = hill;
                        }
                        group.karbLocInSight = true;
                    }
                } else {
                    viable = true;
                    if (group.karbLocInSight) {
                        short[][] hill = p.generateHill(p.closestKarbLocs.peek().toMapLocation());
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
                System.out.println("The pq is empty");
                group.setState(WorkerStates.Standby);
                return;
            }
        }
        if(group.karbLocInSight) {
            short[][] hill = p.generateHill(group.harvestPoint);
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
            System.out.println("\n\n\nRefreshing");
            workerRound = p.round;
            oldGatherers.clear();
            nonReplicatable.clear();
            numWorkerGroups = gatherers.size();
            oldBuilders = (HashSet<Integer>) builders.ids.clone();
            builders.ids.clear();

            System.out.println(oldBuilders.size() + " workers in oldBuilders");
            for(int check : oldBuilders) {
                System.out.println("Worker " + check + " is in oldBuilders");
                if(gc.unit(check).abilityHeat() > 0){
                    nonReplicatable.add(check);
                }
            }

            for (int i = 0; i < numWorkerGroups; i++) {
                oldGatherers.add((HashSet<Integer>)gatherers.get(i).ids.clone());
                gatherers.get(i).ids.clear();
                for(int ids : oldGatherers.get(i)){
                    System.out.println("Worker " + ids + " is in oldGatherers group " + i);
                }
            }
            /*
            gatherers.clear();
            for(int i = 0; i < numWorkerGroups; i++){
                gatherers.add(new Workers(gc, p));
            }*/
            numWorkers = 0;
        }
        if(p.movesToBase(gc.unit(id).location().mapLocation()) == 0 && gc.unit(id).location().mapLocation() != p.baseLoc){
            loners.add(id);
            System.out.println("Worker " + id + " added to loners");
            return;
        }else {
            numWorkers++;
        }
        if(gc.unit(id).abilityHeat() == 0 && nonReplicatable.size() != 0){
            int chopped = nonReplicatable.get(0);
            oldBuilders.remove(chopped);
            if(builders.ids.contains(chopped)){
                builders.ids.remove(chopped);
                numWorkers--;
                addWorker(chopped);
            }
        }

        //System.out.println("Checking for old builders");
        if(oldBuilders.contains(id) && builders.size() < 4){
            System.out.println("\tWorker " + id + " added to builders from OldBuilders");
            builders.add(id);
            return;
        }


        //System.out.println("Checking for old gatherers");
        if(oldGatherers.size() > 0  && oldBuilders.size() >= 4) {
            for (int i = 0; i < oldGatherers.size(); i++) {
                if (oldGatherers.get(i).contains(id)) {
                    gatherers.get(i).add(id);
                    System.out.println("\tWorker " + id + " added to gathering group " + i + " from OldGatherers");
                    return;
                }
            }
        }

        //System.out.println("Checking to see if we need builders");
        if(builders.size() < 4){
            System.out.println("\tWorker " + id + " added to builders");
            builders.add(id);
            return;
        }


        if(gatherers.size() == 0){
            gatherers.add(new Workers(gc, p));
        }

        //System.out.println("Sorting remaining into gathering groups");
        for (int i = 0; i < gatherers.size(); i++) {
            if (gatherers.get(i).size() < 3) {
                gatherers.get(i).add(id);
                System.out.println("\tWorker " + id + " added to gathering group " + i);
                return;
            } else if (i == gatherers.size() - 1) {
                gatherers.add(new Workers(gc, p));
            }
        }
    }
}