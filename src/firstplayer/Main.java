package firstplayer;

import bc.*;

import java.util.BitSet;
import java.util.PriorityQueue;
import java.util.Queue;

public class Main {
    public static void main(String args[]){
        GameController gc = new GameController();

        // Direction is a normal java enum.
        Direction[] directions = Direction.values();
        //Path p = new Path();

        while (true) {
            System.out.println("Current round: "+gc.round());
            // VecUnit is a class that you can think of as similar to ArrayList<Unit>, but immutable.
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++) {
                Unit unit = units.get(i);
                //todo add in players and path to this after this is running
                // Most methods on gc take unit IDs, instead of the unit objects themselves.
                if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), Direction.Southeast)) {
                    gc.moveRobot(unit.id(), Direction.Southeast);
                }
            }
            // Submit the actions we've done, and wait for our next turn.
            //p.calculateTotalKripOnEarth();
            gc.nextTurn();
        }
    }
}

class Path {
    private PlanetMap earth;
    int earthSize;
    // Direction is a normal java enum.
    //todo check that there is an array with only 8 directions
    Direction[] directions = Direction.values();
    public Path(){
        GameMap map = new GameMap();
        PlanetMap earth = map.getEarth_map();
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
    public Queue<MapLocation> genShortestRouteBFS(MapLocation start, MapLocation end){
        BitSet[] visited = new BitSet[earthSize];
        for (int i = 0; i <= earthSize; i++) {
            BitSet set = new BitSet(earthSize);
            visited[i] = set;
        }
        MapLocation[][] from = new MapLocation[earthSize][earthSize];
        //times three for circumfrence
        Queue<MapLocation> toCheck = new PriorityQueue<MapLocation>(earthSize*3);
        toCheck.add(start);
        recordFrom(start,start,from);
        boolean found = false;
        while(!toCheck.isEmpty()){
            MapLocation cur = toCheck.remove();
            for(Direction d : directions){
                MapLocation newLoc = cur.add(d);
                if(shouldBeCheckedLater(newLoc,visited)){
                    toCheck.add(newLoc);
                    recordFrom(cur,newLoc,from);
                }
                markVisited(visited,newLoc);
                if(newLoc.equals(end)){
                    found = true;
                    break;
                }
            }
        }
        if(!found) return null;
        else {
            return generateQ(from,end);
        }
    }
    boolean shouldBeCheckedLater(MapLocation a, BitSet[] checked){
        return(!checked[a.getY()].get(a.getX()) && earth.onMap(a) && earth.isPassableTerrainAt(a) == 1);
    }
    void markVisited(BitSet[] vis, MapLocation cur){
        BitSet set = vis[cur.getY()];
        set.set(cur.getX());
    }
    void recordFrom(MapLocation cur, MapLocation newLoc, MapLocation[][] from){
        from[newLoc.getX()][newLoc.getY()] = cur;
    }
    Queue<MapLocation> generateQ(MapLocation[][] from, MapLocation end){
        MapLocation cur = end;
        MapLocation cameFrom = from[cur.getX()][cur.getY()];
        Queue<MapLocation> route = new PriorityQueue<MapLocation>(earthSize*2);
        while(!cur.equals(cameFrom)){
            route.add(cur);
            cur = cameFrom;
            cameFrom = from[cur.getX()][cur.getY()];
        }
        return route;
    }
}

class Player extends Unit{
    private PriorityQueue<MapLocation> route;
    private GameController g;
    private int id;
    public Player(GameController g){
        this.g = g;
        id = id();
    }
    public void setRoute(PriorityQueue<MapLocation> route){
        this.route = route;
    }
    public boolean tryMoveNextRoute(){
        if(emptyRoute() || !g.isMoveReady(id)){
            return false;
        }
        MapLocation durGoal = route.peek();
        Direction curDirection = location().mapLocation().directionTo(durGoal);
        if(!g.canMove(id,curDirection)){
            return false;
        } else {
            g.moveRobot(id,curDirection);
            route.remove();
            return true;
        }
    }
    public boolean emptyRoute(){
        return (route == null || route.isEmpty());
    }

}

class Group {
    public Unit[] bots;
    Group(){}
}