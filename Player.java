import bc.*;

import java.util.Stack;

public class Player {
    public static void main(String args[]){
        GameController gc = new GameController();
        // Direction is a normal java enum.
        Direction[] directions = Direction.values();
        System.out.println("num of directions: "+ directions.length);
        Path p = new Path(gc);
        Workers workers = new Workers(gc);
        workers.setState(WorkerStates.Replicate);
        while (true) {
            System.out.println();
            System.out.println("Current round: "+gc.round());
            //Place Units into their groups
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                if(unit.unitType() == UnitType.Worker){
                    workers.addWorker(unit.id());
                }
            }
            if(workers.state == WorkerStates.Replicate){
                if (workers.doneReplicating()){
                    workers.setState(WorkerStates.BuildFactory);
                } else {
                    workers.contReplicating();
                }
            }
            if(workers.state == WorkerStates.BuildFactory){
                if (workers.doneBuildingFactory()){
                    workers.setState(WorkerStates.GatherKryptonite);
                } else {
                    workers.contBuildingFactory();
                }
            }
            workers.resetWorkerIndexCount();
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

class Group {
    public Unit[] bots;
    Group(){}
}