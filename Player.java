import bc.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

public class Player {
    static final boolean CarlTests = true;
    public static void main(String args[]){
        GameController gc = new GameController();
        // Direction is a normal java enum.
        Direction[] directions = Direction.values();
        System.out.println("num of directions: "+ directions.length);
        Path p = new Path(gc,gc.planet());
        Workers workers = new Workers(gc,p);
        workers.setState(WorkerStates.Replicate);
        Army sprint = new Army(gc,p);
        Workforce workforce = new Workforce(gc, p);
        Fighter mars = new Fighter(gc,p);
        int count = 0;
        while (true) {
            try {
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
                    System.out.println();
                    System.out.println("Current round: " + gc.round() + " bugs: "+ count);
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
                            if(unit.structureIsBuilt() != 0){
                                sprint.addRocket(unit);
                            } else {
                                System.out.println("un built rocket");
                            }
                        }else{
                            sprint.addUnit(id);
                        }
                    }
                    sprint.conductTurn();
                    workforce.conductTurn();
                    workers.resetWorkerIndexCount();
                    //this is here because multiple classes rely on it
                    p.builtFactIndex = 0;
                    // here is a section to start doing research
                    //todo
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