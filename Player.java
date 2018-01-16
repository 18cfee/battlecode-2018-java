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
        while (true) {
            try {
                if(!gc.researchInfo().hasNextInQueue()){
                    gc.queueResearch(UnitType.Rocket);
                    gc.queueResearch(UnitType.Worker);
                    gc.queueResearch(UnitType.Ranger);
                    gc.queueResearch(UnitType.Knight);
                }
                if (gc.planet() != Planet.Earth) {
                } else {
                    System.out.println();
                    System.out.println("Current round: " + gc.round());
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
                            workforce.addFactory(unit);
                        } else if (unit.unitType() == UnitType.Rocket) {
                            workforce.addRocket(unit);
                        }else{
                            sprint.addUnit(id);
                        }
                    }
                    workforce.factoryProduce();
                    workforce.conductTurn();
                    sprint.conductTurn();
                    // here is a section to start doing research
                    //todo
                }
            } catch (Exception e){
                e.printStackTrace();
                System.exit(0);
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
    public void setRoute(Stack<MapLocation> route){
        this.route = route;
    }
    public boolean tryMoveNextRoute(){
        if(emptyRoute() || !g.isMoveReady(id)){
            return false;
        }
        MapLocation durGoal = route.peek();
        Direction curDirection = parent.location().mapLocation().directionTo(durGoal);
        if(!g.canMove(id,curDirection)){
            return false;
        } else {
            g.moveRobot(id,curDirection);
            route.pop();
            return true;
        }
    }
    public MapLocation curLoc(){
        return parent.location().mapLocation();
    }
    public boolean emptyRoute(){
        return (route == null || route.isEmpty());
    }

}