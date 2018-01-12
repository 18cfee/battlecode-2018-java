import bc.*;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Random;
import java.util.Stack;

public class Path {
    private PlanetMap map;
    GameController gc;
    int planetSize;
    public Direction[] directions;
    private Random random;
    public MapLocation closestStartLocation;
    public Path(GameController gc,Planet planet){
        this.gc = gc;
        random = new Random();
        random.setSeed(724);
        System.out.println("made it to Path");
        map = gc.startingMap(Planet.Earth);
        planetSize = (int) map.getHeight();
        directions = Direction.values();
        //todo the following code was making an infinite loop on bfs possibly
        // remove the direction none
        Direction[] temp = Direction.values();
        directions = new Direction[8];
        for (int i = 1; i <= directions.length; i++) {
            directions[i - 1] = temp[i];
        }
        closestStartLocation = findClosestStartLoc();
    }
    private MapLocation findClosestStartLoc(){
        return new MapLocation(Planet.Earth,)
    }
    private MapLocation flippLocDiag(MapLocation loc){
        int oldX = loc.getX();
        int oldY = loc.getY();

        if(loc.getPlanet() == Planet.Earth){

        }
        int newX = planetSize;
    }
    public Direction getRandDirection(){
        int a = random.nextInt(8);
        return directions[a];
    }
    public long calculateTotalKripOnEarth(){
        long totalCarbs = 0;
        for (int i = 0; i < planetSize; i++) {
            for (int j = 0; j < planetSize; j++) {
                MapLocation loc = new MapLocation(Planet.Earth,i,j);
                totalCarbs += map.initialKarboniteAt(loc);
            }
        }
        return totalCarbs;
    }
    public Stack<MapLocation> genShortestRouteBFSEarth(MapLocation start, MapLocation end){
        BitSet[] visited = new BitSet[planetSize];
        for (int i = 0; i < planetSize; i++) {
            BitSet set = new BitSet(planetSize);
            visited[i] = set;
        }
        MapLocation[][] from = new MapLocation[planetSize][planetSize];
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
        return(map.onMap(a) && !checked[a.getY()].get(a.getX()) && map.isPassableTerrainAt(a) == 1);
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