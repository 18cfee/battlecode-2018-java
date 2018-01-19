import bc.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

public class Player {
    static final boolean CarlTests = true;
    public static void main(String args[]){
        GameController gc = new GameController();
        // Direction is a normal java enum.
        Path p = new Path(gc,gc.planet());
        Workers workers = new Workers(gc,p);
        workers.setState(WorkerStates.Replicate);
        Army sprint = new Army(gc,p);
        Workforce workforce = new Workforce(gc, p);
        Fighter mars = new Fighter(gc,p);
        ArrayList<Group> newlist = new ArrayList<>();
        newlist.add(mars);
        int count = 0;
        while (true) {
            try {
                p.round = (int)gc.round();
                System.out.println("Current round: " + p.round + " bugs: "+ count);
                if(!gc.researchInfo().hasNextInQueue()){
                    gc.queueResearch(UnitType.Rocket);
                    gc.queueResearch(UnitType.Worker);
                    gc.queueResearch(UnitType.Ranger);
                    gc.queueResearch(UnitType.Knight);
                }
                if (gc.planet() != Planet.Earth) {
                    VecUnit units = gc.units();
                    Team myTeam = gc.team();
                    for (int i = 0; i < units.size(); i++) {
                        Unit unit = units.get(i);
                        Location loc = unit.location();
                        int id = unit.id();
                        if(loc.isInGarrison() || loc.isInSpace()){ // do nothing with unit
                        } else if(unit.team() != myTeam){
                            mars.addEnemy(id);
                        } else if (unit.unitType() == UnitType.Worker) {
                            workforce.addWorker(id);
                        } else if (unit.unitType() == UnitType.Factory) {
                            if(unit.structureIsBuilt() == 1){
                                sprint.addFact(unit);
                            }
                        } else if (unit.unitType() == UnitType.Rocket) {
                            Direction random = p.getRandDirection();
                            if(gc.canUnload(unit.id(),random)){
                                gc.unload(unit.id(),random);
                            }
                        }else{
                            mars.add(id);
                        }
                    }
                    mars.conductTurn();
                } else {
                    System.out.println("Current karb count: " + gc.karbonite());
                    System.out.println(workers.state);
                    //Place Units into their groups
                    VecUnit units = gc.units();
                    Team myTeam = gc.team();

                    if(!workforce.isCanBuildRocket() && gc.round() > 100){
                        workforce.setCanBuildRocket(true);
                    }

                    if(gc.round() % 50 == 0){
                        System.out.println(gc.researchInfo().nextInQueue());
                    }
                    for (int i = 0; i < units.size(); i++) {
                        Unit unit = units.get(i);
                        Location loc = unit.location();
                        int id = unit.id();
                        if(loc.isInGarrison() || loc.isInSpace()){ // do nothing with unit
                        } else if(unit.team() != myTeam){
                            sprint.addEnemyUnit(id);
                        } else if (unit.unitType() == UnitType.Worker) {
                            workforce.addWorker(id);
                        } else if (unit.unitType() == UnitType.Factory) {
                            if(unit.structureIsBuilt() == 1){
                                sprint.addFact(unit);
                            }
                        } else if (unit.unitType() == UnitType.Rocket) {
                            if(unit.structureIsBuilt() == 1){
                                System.out.println("The rocket is already built");
                                sprint.addRocket(unit);
                                workforce.addRocket(unit);
                            } else {
                                System.out.println("The rocket isn't built yet");
                                workforce.addRocket(unit);
                            }
                        }else{
                            sprint.addUnit(id);
                        }
                    }
                    sprint.conductTurn();
                    workforce.conductTurn();
                    workers.resetWorkerIndexCount();
                }
            } catch (Exception e){
                // todo set indexes to 0 in here
                e.printStackTrace();
                count++;
            }

            //not working
            System.out.println(gc.getTimeLeftMs());
            gc.nextTurn();
        }

    }
}




class Troop {
    private Stack<MapLocation> route;
    private GameController g;
    private int id;
    private Unit parent;
    public Troop(GameController g, Unit parent){
        this.g = g;
        id = parent.id();
        this.parent = parent;
    }

    public MapLocation curLoc(){
        return parent.location().mapLocation();
    }

}