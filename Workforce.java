import bc.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

public class Workforce {
    private GameController gc;
    private Path p;
    private boolean canBuildRocket = false;
    private ArrayList<Workers> gatherers;
    private ArrayList<HashSet<Integer>> oldGatherers;
    private int workerRound = 0;
    private Workers builders;
    private int numWorkers = 0;
    private HashSet<Integer> oldBuilders;
    private Workers loners;
    private ArrayList<Integer> nonReplicateable;
    private int numWantedBuilders = 4;
    private int numWantedGatherers = 6;
    private int karboniteGathered = 0;
    private int replicationCosts = 0;
    private ArrayList<Integer> freeAgents;
    private MarsSector mySector;
    private short[][] hillToBase;
    private MapLocation baseLoc;
    private MPQ closestKarbLocs;
    private BitSet[] karbMap;

    public Workforce(GameController gc, Path p) {
        this.gc = gc;
        this.p = p;
        gatherers = new ArrayList<>();
        oldGatherers = new ArrayList<>();
        karbMap = p.karbMap;
        builders = new Workers(gc, p, p.closestKarbLocs, p.hillToBase, karbMap);
        loners = new Workers(gc, p, p.closestKarbLocs, p.hillToBase, karbMap);
        loners.setState(WorkerStates.CutOff);
        nonReplicateable = new ArrayList<>();
        freeAgents = new ArrayList<>();
        if(p.totalKarbOnEarth < 300){
            numWantedGatherers = 4;
        }else if(p.totalKarbOnEarth > 1000){
            numWantedGatherers = 10;
        }
        numWantedGatherers = calcGathers();
        baseLoc = p.baseLoc;
        hillToBase = p.hillToBase;
        closestKarbLocs = p.closestKarbLocs;
    }


    public Workforce(GameController gc, Path p, MarsSector mySector) {
        this.gc = gc;
        this.p = p;
        numWantedBuilders = 0;
        numWantedGatherers = 100;
        gatherers = new ArrayList<>();
        oldGatherers = new ArrayList<>();
        karbMap = mySector.karbMap;
        builders = new Workers(gc, p, mySector.priorityHarvesting, mySector.hillToBase, karbMap);
        loners = new Workers(gc, p, mySector.priorityHarvesting, mySector.hillToBase, karbMap);
        loners.setState(WorkerStates.CutOff);
        nonReplicateable = new ArrayList<>();
        freeAgents = new ArrayList<>();
        this.baseLoc = mySector.baseLoc;
        hillToBase = mySector.hillToBase;
        closestKarbLocs = mySector.priorityHarvesting;
    }
    private int calcGathers(){
        if(p.totalKarbOnEarth < 2000){
            return (int)Math.ceil(p.totalKarbOnEarth/200);
        } else if(p.totalKarbOnEarth >= 2000 && p.totalKarbOnEarth <= 4000){
            return (int)Math.max(10,p.totalKarbOnEarth/250);
        } else return 16;
    }
    public void conductTurn() throws Exception{
        //System.out.println(closestKarbLocs.getSize() + " size of closestKarbLocs");
        for(int id : freeAgents){
            addWorker(id);
        }
        if(p.planet == Planet.Mars){
            //System.out.println("conducting turn on mars");
        }
        //System.out.println("There are " + numWorkers + " workers");
        //System.out.println("There are " + builders.size() + " builders");
        /*
        int groupNum = 0;
        for(Workers group : gatherers){
            groupNum++;

            System.out.println("There are " + group.size() + " workers in gathering group " + groupNum);
            for(int id : group.ids){
                System.out.println("Worker " + id + " is in gathering group " + groupNum);
            }

        }*/

        builders.groupIsAlive = true;
        for(int i = 0; i < gatherers.size(); i++){
            if(gatherers.get(i).size() == 0){
                gatherers.remove(i);
            }else{
                gatherers.get(i).groupIsAlive = true;
            }
        }
            //System.out.println("Trying to replicate");
        //System.out.println(builders.ids.size() + " the size of the builders group");
        if(p.planet == Planet.Earth) {
            determineReplication(builders);
        }else{
            for (Workers group : gatherers){
                determineReplication(group);
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
        } else if (canBuildRocket && p.rockets.getNumUnBuiltRockets() == 0 && p.rockets.getNumberOfBuiltRockets() < Path.NUM_ROCKETS_WANTED) {
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
                //System.out.println("/**********************************************/");
                //System.out.println("Starting \"gatherKarbonite\"");
                gatherKarbonite(group);
                //System.out.println("/**********************************************/");
            }else{
                //System.out.println("group is dead");
            }
        }
        for (Workers group : gatherers) {
            if (group.groupIsAlive) {
                //System.out.println("/**********************************************/");
                //System.out.println("Starting \"conductTurn\"");
                group.conductTurn();
                karboniteGathered += group.karbsHarvested;
                //System.out.println("/**********************************************/");
            }else{
                //System.out.println("group is uber-dead");
            }
        }
    }

    private void determineReplication(Workers group){
        for(Integer id: group.ids){
            //System.out.println("worker id: " + id);
            if(shouldReplicate()){
                //System.out.println("trying to replicate: " + id);
                group.replicate(id);
            }
        }

    }
    private boolean shouldReplicate(){
        if(p.planet == Planet.Mars){
            if(p.round > 750 || p.round%10 == 0){
                return true;
            }
            return false;
        }
        if(numWorkers < 4)return true;
        //System.out.println("identifiable as a homosexual");
        if(p.spoolingForFactory) return false;
        //System.out.println("slighty better on the male dudes");
        if(p.rockets.getTotalNumFactories() < 2){
            //System.out.println("fact less than two");
            //System.out.println("num wanted builders/dfsdd " + (numWantedBuilders + (numWantedGatherers/2)));
            if(p.producedWorkers < (numWantedBuilders + (numWantedGatherers/2))){
                //System.out.println(" num workers " + numWorkers);
                return true;
            }
            else {
                //System.out.println("did not replicate");
                return false;
            }
        }
        if(replicationCosts >= karboniteGathered*.5) return false;
        if(numWorkers < numWantedGatherers + numWantedBuilders) return true;
        return false;
    }
    private boolean findASpot(Workers group, MPQ pq){
        group.harvestPoint = pq.pop().toMapLocation();
        if(karbMap[group.harvestPoint.getX()].get(group.harvestPoint.getY())){
            group.currentHill = p.generateHill(group.harvestPoint);
            return true;
        }
        return false;
    }
    private void gatherKarbonite(Workers group){
        group.setState(WorkerStates.GatherKarbonite);
        if (group.harvestPoint == null || !karbMap[group.harvestPoint.getX()].get(group.harvestPoint.getY())) {
            //System.out.println("Picking a new location");
            if(!closestKarbLocs.isEmpty()) {
                for(int id : group.ids){
                    if (!group.personalPQ.isEmpty() &&
                    gc.unit(id).location().mapLocation().distanceSquaredTo(group.personalPQ.peek().toMapLocation()) < gc.unit(id).location().mapLocation().distanceSquaredTo(closestKarbLocs.peek().toMapLocation())) {
                        if(findASpot(group, group.personalPQ)){
                            break;
                        }
                    }else if(!closestKarbLocs.isEmpty()){
                        if(findASpot(group, closestKarbLocs)){
                            break;
                        }
                    }
                }
            }
        }
        if(group.karbLocInSight && group.harvestPoint != null) {
            group.currentHill = p.generateHill(group.harvestPoint);
        }
    }

    public void setCanBuildRocket(boolean set) {
        canBuildRocket = set;
    }

    public boolean isCanBuildRocket() {
        return canBuildRocket;
    }


    public void addWorker(int id) throws Exception{
        if(workerRound != p.round) {
            //System.out.println("\n\n\nRefreshing");
            workerRound = p.round;
            oldGatherers.clear();
            nonReplicateable.clear();
            int numWorkerGroups = gatherers.size();
            oldBuilders = (HashSet<Integer>) builders.ids.clone();
            builders.ids.clear();
            freeAgents.clear();
            if(p.planet == Planet.Mars){
                numWantedBuilders = 0;
            }else {
                if (numWorkers <= 4) {
                    numWantedBuilders = 2;
                } else {
                    numWantedBuilders = 4;
                }
            }

            //System.out.println(oldBuilders.size() + " workers in oldBuilders");
            for(int check : oldBuilders) {
                //System.out.println("Worker " + check + " is in oldBuilders");
                if(p.sensableUnitNotInGarisonOrSpace(check) && gc.unit(check).abilityHeat() > 0){
                    nonReplicateable.add(check);
                }
            }

            for (int i = 0; i < numWorkerGroups; i++) {
                oldGatherers.add((HashSet<Integer>)gatherers.get(i).ids.clone());
                gatherers.get(i).ids.clear();
                /*
                for(int ids : oldGatherers.get(i)){
                    System.out.println("Worker " + ids + " is in oldGatherers group " + i);
                }*/
            }
            /*
            gatherers.clear();
            for(int i = 0; i < numWorkerGroups; i++){
                gatherers.add(new Workers(gc, p));
            }*/
            numWorkers = 0;
        }
        if(p.planet == Planet.Earth) {
            if (p.movesToBase(gc.unit(id).location().mapLocation()) == 0 && gc.unit(id).location().mapLocation() != baseLoc && p.planet != Planet.Mars) {
                loners.add(id);
                //System.out.println("Worker " + id + " added to loners");
                return;

            } else {
                numWorkers++;
            }
        }else{
            numWorkers++;
        }

        if(gc.unit(id).abilityHeat() == 0 && nonReplicateable.size() != 0){
            int chopped = nonReplicateable.get(0);
            nonReplicateable.remove(0);
            oldBuilders.remove(chopped);
            if(builders.ids.contains(chopped)){
                builders.ids.remove(chopped);
                numWorkers--;
                freeAgents.add(chopped);
            }
        }

        /*
        if(gc.unit(id).abilityHeat() == 0){
            replicateable.add(id);
        }
        */

        //System.out.println("Checking for old builders");
        if(oldBuilders.contains(id) && builders.size() < numWantedBuilders){
            //System.out.println("\tWorker " + id + " added to builders from OldBuilders");
            builders.add(id);
            return;
        }


        //System.out.println("Checking for old gatherers");
        if(oldGatherers.size() > 0  && (oldBuilders.size() >= numWantedBuilders || builders.size() >= numWantedBuilders)) {
            //System.out.println("Round " + p.round + " and worker added to gatherer");
            for (int i = 0; i < oldGatherers.size(); i++) {
                if (oldGatherers.get(i).contains(id)) {
                    gatherers.get(i).add(id);
                    //System.out.println("\tWorker " + id + " added to gathering group " + i + " from OldGatherers");
                    return;
                }
            }
        }

        //System.out.println("Checking to see if we need builders");
        if(builders.size() < numWantedBuilders){
            //System.out.println("\tWorker " + id + " added to builders");
            builders.add(id);
            return;
        }


        if(gatherers.size() == 0){
            gatherers.add(new Workers(gc, p, closestKarbLocs, hillToBase, karbMap));
        }

        //System.out.println("Sorting remaining into gathering groups");
        for (int i = 0; i < gatherers.size(); i++) {
            //System.out.println("Round " + p.round + " and worker added to gatherers");
            //System.out.println("gathering sort loop");
            if (gatherers.get(i).size() < 1) {
                gatherers.get(i).add(id);
                //System.out.println("\tWorker " + id + " added to gathering group " + i);
                return;
            } else if (i == gatherers.size() - 1) {
                gatherers.add(new Workers(gc, p, closestKarbLocs, hillToBase, karbMap));
            }
        }
    }
}