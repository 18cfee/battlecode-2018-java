import bc.*;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Stack;

public class Player {
    public static void main(String args[]){
        GameController gc = new GameController();
        // Direction is a normal java enum.
        int q = Math.abs(-4);
        System.out.println(q);
        Direction[] directions = Direction.values();
        System.out.println("num of directions: "+ directions.length);
        Path p = new Path(gc);
        VecUnit units = gc.myUnits();
        Troop[] army = new Troop[(int)units.size()];
        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i);
            army[i] = new Troop(gc,unit);
            army[i].setRoute(p.genShortestRouteBFS(army[i].curLoc(),new MapLocation(Planet.Earth,10,10)));
        }
        //try{
            while (true) {
                System.out.println();
                System.out.println("Current round Carl test aaa: "+gc.round());
                for (int i = 0; i < army.length; i++) {
                    Unit unit = units.get(i);
                    Troop tr = army[i];
                    // Most methods on gc take unit IDs, instead of the unit objects themselves.
                    int a = (int)(Math.random()*8);
                    tr.tryMoveNextRoute();
                    debug.printCords(tr.curLoc());
                }

                // Submit the actions we've done, and wait for our next turn.
                //long k = p.calculateTotalKripOnEarth();
                //System.out.println("krip on earth" + k);
                gc.nextTurn();
            }
        //} catch(Exception e){
          //  System.out.println("Exception thrown by 724");
        //}
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

class debug {
    static void printCords(MapLocation a){
        System.out.println("X: " + a.getX() + " Y: " + a.getY());
    }
}

class Group {
    public Unit[] bots;
    Group(){}
}