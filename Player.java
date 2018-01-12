import bc.*;

import java.util.ArrayList;
import java.util.List;
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
        Group carlsRangers = new CarlRangers(gc,p);
        ArrayList<Group> army = new ArrayList<>();
        army.add(carlsRangers);
        while (true) {
            if(gc.planet() != Planet.Earth) {
            } else {
                System.out.println();
                System.out.println("Current round: "+gc.round());
                System.out.println(workers.state);
                //Place Units into their groups
                VecUnit units = gc.myUnits();
                for (int i = 0; i < units.size(); i++) {
                    // todo assign units to groups
                    Unit unit = units.get(i);
                    if(unit.unitType() == UnitType.Worker){
                        workers.addWorker(unit.id());
                    }
                    if(unit.unitType() == UnitType.Factory){
                        workers.addFactory(unit);
                    }
                    if(CarlTests && unit.unitType() == UnitType.Ranger && !unit.location().isInGarrison()){
                        carlsRangers.add(unit.id());
                    }
                    //todo keegan - set the CArltest to false if you want to snag the rangers for your testing
                }
                if(workers.state == WorkerStates.Replicate){
                    if (workers.doneReplicating()){
                        workers.setState(WorkerStates.BuildFactory);
                    } else {
                        workers.contReplicating();
                    }
                }
                // todo right now not complete
                if(workers.state == WorkerStates.BuildFactory){
                    if (workers.doneBuildingFactory()){
                        workers.setState(WorkerStates.GatherKryptonite);
                    } else {
                        workers.contBuildingFactory();
                    }
                }
                // todo have the workers get krypt

                //todo this should churn out units
                workers.factoryProduce();
                workers.resetWorkerIndexCount();

                // here is a section to start doing research
                //todo

                //here is a section to start rocket stuff
                //todo

                // othere groups after this
                //todo make stuff shoot
                for(Group group: army){
                    group.conductTurn();
                }
            }

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