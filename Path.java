import bc.*;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Random;
import java.util.Stack;

public class Path {
    private PlanetMap earth;
    GameController gc;
    int earthSize;
    public Direction[] directions;
    private Random random;
    public Path(GameController gc){
        this.gc = gc;
        random = new Random();
        random.setSeed(724);
        System.out.println("made it to Path");
        earth = gc.startingMap(Planet.Earth);
        earthSize = (int)earth.getHeight();
        directions = Direction.values();
        //todo the following code was making an infinite loop on bfs possibly
        // remove the direction none
        Direction[] temp = Direction.values();
        directions = new Direction[8];
        for (int i = 1; i <= directions.length; i++) {
            directions[i - 1] = temp[i];
        }
    }
    public Direction getRandDirection(){
        int a = random.nextInt(8);
        return directions[a];
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
            System.out.println("visit:");
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