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
    Planet planet;
    public final static short greatestPathNum = 3000;
    short[][] hillToBase;
    public int[][] numsDirections = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};
    BitSet[] passable;
    MapLocation startLoc;
    int[] factories = new int[10];
    int unbuiltFactIndex = 0;
    public final static int MAX_NUM_FACTS = 20;
    public int builtFactIndex = 0;
    public int [] builtFactary = new int [MAX_NUM_FACTS];
    public final static int NUM_FACTORIES_WANTED = 2;
    int factIndex = 0;
    int rocketIndex = 0;
    int[] rockets = new int[10];
    public MapLocation baseLoc = null;
    public Path(GameController gc,Planet planet){
        this.planet = planet;
        this.gc = gc;
        random = new Random();
        random.setSeed(724);
        System.out.println("made it to Path");
        map = gc.startingMap(planet);
        planetSize = (int) map.getHeight();
        directions = Direction.values();
        //todo the following code was making an infinite loop on bfs possibly
        // remove the direction none
        Direction[] temp = Direction.values();
        directions = new Direction[8];
        for (int i = 0; i < directions.length; i++) {
            directions[i] = temp[i];
        }
        if(planet == Planet.Earth){
            closestStartLocation = findClosestEnemyStartLoc();
            startLoc = gc.myUnits().get(0).location().mapLocation();
            hillToBase = generateHill(startLoc);
            Debug.printHill(hillToBase);
        }
        generatePassable();
    }
    private void generatePassable(){
        passable = new BitSet[planetSize];
        for (int i = 0; i < planetSize; i++) {
            BitSet cur = new BitSet(planetSize);
            for (int j = 0; j < planetSize; j++) {
                if (map.isPassableTerrainAt(new MapLocation(planet,i,j)) == 1){
                    cur.set(j);
                }
            }
            passable[i] = cur;
        }
    }

    public int getNumFactories(){
        return unbuiltFactIndex;
    }
    public int getNumRockets(){
        return rocketIndex;
    }
    public MapLocation getLocBetween(MapLocation a, MapLocation b){
        int x = (a.getX()*4 + b.getX())/5;
        int y = (a.getY()*4 + b.getY())/5;
        return new MapLocation(planet,x,y);
    }
    public boolean passable(MapLocation location){
        return passable[location.getX()].get(location.getY());
    }

    //only meant for earth
    private MapLocation findClosestEnemyStartLoc(){
        //todo maybe find as crow flies for shooting or actual movement necesary
        //just a mirror of one for now
        if(planet == Planet.Earth){
            return flippLocDiag(gc.myUnits().get(0).location().mapLocation());
        } else return new MapLocation(Planet.Mars,0,0);

    }

    public MapLocation getClosestStartLocation() {
        return closestStartLocation;
    }

    private MapLocation flippLocDiag(MapLocation loc){
        int oldX = loc.getX();
        int oldY = loc.getY();
        int newX = planetSize - 1 - oldX;
        int newY = planetSize - 1 - oldY;
        return new MapLocation(planet,newX,newY);
    }
    public boolean moveInRandomAvailableDirection(int id){
        int startD = random.nextInt(8);
        for (int i = startD; i < startD + 8; i++) {
            Direction d = directions[i%8];
            if(gc.isMoveReady(id) && gc.canMove(id,d)){
                gc.moveRobot(id,d);
                return true;
            }
        }
        return false;
    }
    public short[][] generateHill(MapLocation destination){
        short hill[][] = new short[planetSize][planetSize];
        hill[destination.getX()][destination.getY()] = 1;
        ArrayDeque<MapLocation> toCheck = new ArrayDeque<MapLocation>();
        toCheck.addLast(destination);
        while(!toCheck.isEmpty()){
            MapLocation cur = toCheck.removeFirst();
            short dis = hill[cur.getX()][cur.getY()];
            for(Direction d : directions){
                MapLocation newLoc = cur.add(d);
                if(previouslyUncheckedMapLoc(newLoc,hill)){
                    if(map.isPassableTerrainAt(newLoc) != 1){
                        //mark as unreachable
                        hill[newLoc.getX()][newLoc.getY()] = greatestPathNum;
                    } else {
                        toCheck.addLast(newLoc);
                        hill[newLoc.getX()][newLoc.getY()] = (short)(dis + 1);
                    }
                }
            }
        }
        // todo smaller versions need to know if a path was found
        return hill;
    }
    private boolean previouslyUncheckedMapLoc(MapLocation a, short[][] hill){
        return(map.onMap(a) && hill[a.getX()][a.getY()] == (short)0);
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
    public Stack<MapLocation> genShortestRouteBFS(MapLocation start, MapLocation end){
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
            //System.out.println("visit:");
            //Debug.printCoords(cur);
            for(Direction d : directions){
                MapLocation newLoc = cur.add(d);
                if(shouldBeCheckedLater(newLoc,visited)){
                    Debug.printCoords(newLoc);
                    toCheck.addLast(newLoc);
                    recordFrom(cur,newLoc,from);
                    markVisited(visited,newLoc);
                    if(newLoc.getY() == end.getY() && newLoc.getX() == end.getX()){
                        found = true;
                        break outerLoop;
                    }
                }
            }
        }
        if(!found) return null;
        else {
            return generateStack(from,end);
        }
    }
    private boolean shouldBeCheckedLater(MapLocation a, BitSet[] checked){
        return(a != null && map.onMap(a) && !checked[a.getY()].get(a.getX()) && map.isPassableTerrainAt(a) == 1);
    }
    private void markVisited(BitSet[] vis, MapLocation cur){
        BitSet set = vis[cur.getY()];
        set.set(cur.getX());
    }
    private void recordFrom(MapLocation cur, MapLocation newLoc, MapLocation[][] from){
        from[newLoc.getX()][newLoc.getY()] = cur;
    }
    private Stack<MapLocation> generateStack(MapLocation[][] from, MapLocation end){
        System.out.println("Stack");
        MapLocation cur = end;
        //Debug.printCoords(cur);
        MapLocation cameFrom = from[cur.getX()][cur.getY()];
        Stack<MapLocation> route = new Stack<MapLocation>();
        while(!cur.equals(cameFrom)){
            route.add(cur);
            cur = cameFrom;
            //Debug.printCoords(cur);
            cameFrom = from[cur.getX()][cur.getY()];
        }
        return route;
    }
}