import bc.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

public class Player {
    static final boolean CarlTests = true;
    public static void main(String args[]) {
        GameController gc = new GameController();
        // Direction is a normal java enum.
        Path p = null;
        try {
            p = new Path(gc, gc.planet());
        } catch (Exception e){
            System.out.println("init fail");
            e.printStackTrace();
        }
        Workers workers = new Workers(gc,p);
        workers.setState(WorkerStates.Standby);
        Army sprint = new Army(gc,p);
        Workforce workforce = new Workforce(gc, p);
        //AggresiveRangers mars = new AggresiveRangers(gc,p);
        Team myTeam = gc.team();
        int count = 0;
        MarsControl marsGame = new MarsControl(gc,p,myTeam);
        while (true) {
            try {
                p.round = (int)gc.round();
                if(!gc.researchInfo().hasNextInQueue()){
                    gc.queueResearch(UnitType.Worker);
                    gc.queueResearch(UnitType.Worker);
                    gc.queueResearch(UnitType.Ranger);
                    gc.queueResearch(UnitType.Worker);
                    gc.queueResearch(UnitType.Rocket);
                    gc.queueResearch(UnitType.Worker);
                    gc.queueResearch(UnitType.Rocket);
                    gc.queueResearch(UnitType.Rocket);
                    gc.queueResearch(UnitType.Ranger);
                    gc.queueResearch(UnitType.Healer);
                    gc.queueResearch(UnitType.Healer);
                    gc.queueResearch(UnitType.Healer);
                }
                if (gc.planet() != Planet.Earth) {
                    VecUnit units = gc.units();
                    for (int i = 0; i < units.size(); i++) {
                        marsGame.addUnit(units.get(i));
                    }
                    marsGame.conductTurn();
                } else {
                    //System.out.println("Current karb count: " + gc.karbonite());
                    //System.out.println(workers.state);
                    //Place Units into their groups
                    VecUnit units = gc.units();
                    if(!workforce.isCanBuildRocket() && gc.researchInfo().getLevel(UnitType.Rocket) >= 1){
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
                            sprint.addEnemyUnit(unit);
                        } else if (unit.unitType() == UnitType.Worker) {
                            workforce.addWorker(id);
                        } else if (unit.unitType() == UnitType.Factory) {
                            p.rockets.addFactory(unit.id());
                            if(unit.structureIsBuilt() == 1){
                                sprint.addFact(unit);
                            }
                        } else if (unit.unitType() == UnitType.Rocket) {
                            p.rockets.addRocket(unit);
                        }else{
                            sprint.addUnit(unit);
                        }
                    }
                    p.rockets.clearRocketsIfNoUnits();
                    p.rockets.rocketsShouldLauchIfPossible();
                    sprint.conductTurn();
                    workforce.conductTurn();
                    // after other things to give them a chance to conduct turn
                }
            } catch (Exception e){
                // todo set indexes to 0 in here
                System.out.println("Current round: " + p.round + " bugs: "+ count);
                e.printStackTrace();
                //System.exit(0);
                count++;
            }

            //not working
            //System.out.println(gc.getTimeLeftMs());
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