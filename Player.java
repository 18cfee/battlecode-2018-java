import bc.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Stack;

public class Player {
    public static void main(String args[]){
        GameController gc = new GameController();
        // Direction is a normal java enum.
        Direction[] directions = Direction.values();
        System.out.println("num of directions: "+ directions.length);
        Path p = new Path(gc);
        VecUnit units = gc.myUnits();
        Workers workers = new Workers(gc);
        while (true) {
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                army[i] = new Troop(gc,unit);
                army[i].setRoute(p.genShortestRouteBFS(army[i].curLoc(),new MapLocation(Planet.Earth,10,10)));
            }

            for (int i = 0; i < ; i++) {

            }
            System.out.println();
            System.out.println("Current round: "+gc.round());

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