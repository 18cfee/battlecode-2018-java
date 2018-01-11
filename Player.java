import bc.*;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Stack;

public class Player {

    GameController gc = new GameController();

    public static void main(String args[]){

    }

    public Player() {
        run();
    }

    void run(){
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
                Debug.printCoords(tr.curLoc());
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

class Path {
    private PlanetMap earth;
    GameController gc;
    int earthSize;
    // Direction is a normal java enum.
    //todo check that there is an array with only 8 directions not 9
    Direction[] directions = Direction.values();
    public Path(GameController gc){
        this.gc = gc;
        System.out.println("made it here");
        earth = gc.startingMap(Planet.Earth);
        earthSize = (int)earth.getHeight();
    }
    public long calculateTotalKripOnEarth(){
        long totalCarbs = 0;
        for (int i = 0; i < earthSize; i++) {
            for (int j = 0; j < earthSize; j++) {
                MapLocation loc = new MapLocation(Planet.Earth,i,j);
                totalCarbs += earth.initialKarboniteAt(loc);
            }
        }
        return totalCarbs;
    }
    public Stack<MapLocation> genShortestRouteBFS(MapLocation start, MapLocation end){
        BitSet[] visited = new BitSet[earthSize];
        for (int i = 0; i < earthSize; i++) {
            BitSet set = new BitSet(earthSize);
            visited[i] = set;
        }
        MapLocation[][] from = new MapLocation[earthSize][earthSize];
        ArrayDeque<MapLocation> toCheck = new ArrayDeque<MapLocation>();
        toCheck.addLast(start);
        recordFrom(start,start,from);
        boolean found = false;
        outerLoop:
        while(!toCheck.isEmpty()){
            MapLocation cur = toCheck.removeFirst();
            System.out.println("vitit:");
            Debug.printCoords(cur);
            for(Direction d : directions){
                MapLocation newLoc = cur.add(d);
                if(shouldBeCheckedLater(newLoc,visited)){
                    toCheck.addLast(newLoc);
                    recordFrom(cur,newLoc,from);
                    markVisited(visited,newLoc);
                }
                if(newLoc.equals(end)){
                    found = true;
                    break outerLoop;
                }
            }
        }
        if(!found) return null;
        else {
            return generateStack(from,end);
        }
    }
    boolean shouldBeCheckedLater(MapLocation a, BitSet[] checked){
        return(earth.onMap(a) && !checked[a.getY()].get(a.getX()) && earth.isPassableTerrainAt(a) == 1);
    }
    void markVisited(BitSet[] vis, MapLocation cur){
        BitSet set = vis[cur.getY()];
        set.set(cur.getX());
    }
    void recordFrom(MapLocation cur, MapLocation newLoc, MapLocation[][] from){
        from[newLoc.getX()][newLoc.getY()] = cur;
    }
    Stack<MapLocation> generateStack(MapLocation[][] from, MapLocation end){
        System.out.println("Stack");
        MapLocation cur = end;
        Debug.printCoords(cur);
        MapLocation cameFrom = from[cur.getX()][cur.getY()];
        Stack<MapLocation> route = new Stack<MapLocation>();
        while(!cur.equals(cameFrom)){
            route.add(cur);
            cur = cameFrom;
            Debug.printCoords(cur);
            cameFrom = from[cur.getX()][cur.getY()];
        }
        return route;
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