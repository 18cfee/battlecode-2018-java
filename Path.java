import bc.*;

import java.util.*;

public class Path {
    private PlanetMap map;
    GameController gc;
    int planetHeight;
    int planetWidth;
    public Direction[] directions;
    private Random random;
    public MapLocation closestStartLocation;
    Planet planet;
    public final static short greatestPathNum = 3000;
    short[][] hillToBase;
    public int[][] numsDirections = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};
    BitSet[] passable;
    MapLocation startLoc;
    int unbuiltFactIndex = 0;
    public final static int MAX_NUM_FACTS = 20;
    public int builtFactIndex = 0;
    public int [] builtFactary = new int [MAX_NUM_FACTS];
    public final static int NUM_FACTORIES_WANTED = 2;
    int rocketIndex = 0;
    int[] rockets = new int[10];
    public MapLocation baseLoc = null;
    public MapLocation firstRocket;
    public short[][] firstRocketLocHill = null;
    public MapLocation placeToLandOnMars;
    public long totalKarbOnEarth;
    public ArrayList<MapLoc> karbLocs;
    public Path(GameController gc,Planet planet){
        this.planet = planet;
        this.gc = gc;
        if(planet == Planet.Earth){
            int height = (int)gc.startingMap(Planet.Mars).getHeight();
            int width = (int)gc.startingMap(Planet.Mars).getWidth();
            loop:
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if(gc.startingMap(Planet.Mars).isPassableTerrainAt
                            (new MapLocation(Planet.Mars,i,j)) != 0){
                        placeToLandOnMars = new MapLocation(Planet.Mars,i,j);
                        break loop;
                    }
                }
            }
        }
        random = new Random();
        random.setSeed(724);
        map = gc.startingMap(planet);
        planetHeight = (int) map.getHeight();
        planetWidth = (int) map.getWidth();
        directions = Direction.values();
        Direction[] temp = Direction.values();
        directions = new Direction[8];
        for (int i = 0; i < directions.length; i++) {
            directions[i] = temp[i];
        }
        if(planet == Planet.Earth){
            closestStartLocation = findClosestEnemyStartLoc();
            startLoc = gc.myUnits().get(0).location().mapLocation();
        }
        totalKarbOnEarth = calculateTotalKarbOnEarth();
        generatePassable();
    }
    private void generatePassable(){
        passable = new BitSet[planetWidth];
        for (int i = 0; i < planetWidth; i++) {
            BitSet cur = new BitSet(planetHeight);
            for (int j = 0; j < planetHeight; j++) {
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
        int newX = planetWidth - 1 - oldX;
        int newY = planetHeight - 1 - oldY;
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
        short hill[][] = new short[planetWidth][planetHeight];
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
    public long calculateTotalKarbOnEarth() {
        karbLocs = new ArrayList<>();
        long totalCarbs = 0;
        for (int i = 0; i < planetWidth; i++) {
            for (int j = 0; j < planetHeight; j++) {
                System.out.println("x " +i+ "y" + j);
                MapLocation loc = new MapLocation(planet,i,j);

                long karbATLoc = map.initialKarboniteAt(loc);
                if(karbATLoc > 0){
                    totalCarbs += karbATLoc;
                    MapLoc karbLoc = new MapLoc(loc);
                    karbLocs.add(karbLoc);
                }
            }
        }
        return totalCarbs;
    }
    public boolean onMap(int x, int y){
        return (0 <= x && x < planetWidth && 0 <= y && y < planetHeight);
    }
}