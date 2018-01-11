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
        Troop[] army = new Troop[(int)units.size()];
        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i);
            army[i] = new Troop(gc,unit);
            army[i].setRoute(p.genShortestRouteBFS(army[i].curLoc(),new MapLocation(Planet.Earth,10,10)));
        }
        //try{
        int blueId = 20;
        int[] blueprints = new int[500];
        int numAThings = 0;
            while (true) {
                System.out.println();
                System.out.println("Current round Carl test aaa: "+gc.round());
                units = gc.units();
                int idBlueprint = 0;
                for (int i = 0; i < units.size(); i++) {
                    Unit unit = units.get(i);
                    if(unit.unitType() == UnitType.Factory){
                        idBlueprint = unit.id();
                        if (gc.canProduceRobot(idBlueprint,UnitType.Ranger));
                    }
                }
                for (int i = 0; i < units.size(); i++) {
                    Unit unit = units.get(i);
                    int id = unit.id();
                    // Most methods on gc take unit IDs, instead of the unit objects themselves.
                    int a = (int) (Math.random() * 8);
                    Direction rand = directions[a];
                    if(gc.isMoveReady(id) && gc.canMove(id,rand)) gc.moveRobot(id,rand);
                    //Debug.printCoords(tr.curLoc());
                    if(gc.canReplicate(id,rand) && numAThings < 10){
                        System.out.println("replicate worker");
                        gc.replicate(id,rand);
                        numAThings++;
                    }
                    else if(gc.canBlueprint(id,UnitType.Factory,rand) && numAThings == 10){
                        System.out.println("blue factory");
                        gc.blueprint(id,UnitType.Factory,rand);
                        numAThings++;
                    } else if(gc.canBuild(id,idBlueprint)){
                        gc.build(id,idBlueprint);
                    }
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

class Group {
    public Unit[] bots;
    Group(){}
}