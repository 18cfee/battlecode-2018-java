import bc.*;

import java.util.*;

public class Path {
    private PlanetMap map;
    GameController gc;
    int planetHeight;
    int planetWidth;
    public Direction[] directions;
    public Random random;
    public MapLocation closestStartLocation;
    Planet planet;
    public final static short greatestPathNum = 3000;
    Hill baseHill;
    public int[][] numsDirections = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};
    BitSet[] passable;
    //MapLocation startLoc;
    int unbuiltFactIndex = 0;
    public int round = 0;
    //public final static int MAX_NUM_FACTS = 20;
    //public int builtFactIndex = 0;
    //public int [] builtFactary = new int [MAX_NUM_FACTS];
    public final static int NUM_FACTORIES_WANTED = 2;
    int rocketIndex = 0;
    public MapLocation baseLoc = null;
    public Hill firstRocketLocHill = null;
    public MapLocation placeToLandOnMars;
    public long totalKarbOnEarth;
    public ArrayList<MapLoc> karbLocs;
    public MPQ closestKarbLocs;
    private int numKarbLocs = 0;
    HashSet<Integer> currentBuiltFactories;
    public Path(GameController gc,Planet planet){
        currentBuiltFactories = new HashSet<>(10);
        this.planet = planet;
        this.gc = gc;
        random = new Random();
        random.setSeed(724);
        map = gc.startingMap(planet);
        planetHeight = (int) map.getHeight();
        planetWidth = (int) map.getWidth();
        directions = Direction.values();
        Direction[] temp = Direction.values();
        directions = new Direction[8];
        generatePassable();
        for (int i = 0; i < directions.length; i++) {
            directions[i] = temp[i];
        }
        if(planet == Planet.Earth){
            //closestStartLocation = findClosestEnemyStartLoc();
            //startLoc = gc.myUnits().get(0).location().mapLocation();
        }
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
            baseLoc = chooseBaseLocation();
            baseHill = new Hill(this);
            baseHill.generateCompleteReachableHill(baseLoc);
        }
        totalKarbOnEarth = calculateTotalKarbOnEarth();
    }
    private MapLocation chooseBaseLocation(){
        int maxGreenPercent = 0;
        VecUnit units = gc.myUnits();
        MapLocation bestLoc = null;
        for (int i = 0; i < units.size(); i++) {
            int greens = 0;
            int totlocs = 0;
            MapLocation loc = units.get(i).location().mapLocation();
            for (int j = loc.getX() - 5; j <= loc.getX() + 5; j++) {
                for (int k = loc.getY() - 5; k <= loc.getY() + 5; k++) {
                    MapLoc countedLoc = new MapLoc(j,k);
                    if (onMap(countedLoc)){
                        totlocs++;
                        if(passable(countedLoc)){
                            greens++;
                        }
                    }
                }
            }
            int greenCov = 100*greens/totlocs;
            if(greenCov > maxGreenPercent){
                maxGreenPercent = greenCov;
                bestLoc = loc;
            }
        }
        if(bestLoc == null){
            bestLoc = units.get(0).location().mapLocation();
        }
        return bestLoc;
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
    public boolean passable(MapLoc location){
        return passable[location.x].get(location.y);
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
//    private long functionCalled = 0;
//    private long totalTimeInFunc = 0;
//    public short[][] generateHill(MapLocation destination){
//        functionCalled++;
//        long start = System.currentTimeMillis();
//        short hill[][] = new short[planetWidth][planetHeight];
//        hill[destination.getX()][destination.getY()] = 1;
//        ArrayDeque<MapLocation> toCheck = new ArrayDeque<MapLocation>();
//        toCheck.addLast(destination);
//        while(!toCheck.isEmpty()){
//            MapLocation cur = toCheck.removeFirst();
//            short dis = hill[cur.getX()][cur.getY()];
//            for(Direction d : directions){
//                MapLocation newLoc = cur.add(d);
//                if(previouslyUncheckedMapLoc(newLoc,hill)){
//                    if(!passable(newLoc)){
//                        //mark as unreachable
//                        hill[newLoc.getX()][newLoc.getY()] = greatestPathNum;
//                    } else {
//                        toCheck.addLast(newLoc);
//                        hill[newLoc.getX()][newLoc.getY()] = (short)(dis + 1);
//                    }
//                }
//            }
//        }
//        long end = System.currentTimeMillis();
//        totalTimeInFunc+=(end - start);
//        System.out.println("asdfasdfsdfasdf milis attention " + totalTimeInFunc + " Num times called " + functionCalled);
//        // todo smaller versions need to know if a path was found
//        return hill;
//    }
//    private boolean previouslyUncheckedMapLoc(MapLocation a, short[][] hill){
//        return(map.onMap(a) && hill[a.getX()][a.getY()] == (short)0);
//    }
    public Direction getRandDirection(){
        int a = random.nextInt(8);
        return directions[a];
    }
    public long calculateTotalKarbOnEarth() {
        karbLocs = new ArrayList<>();
        long totalCarbs = 0;
        for (int x = 0; x < planetWidth; x++) {
            for (int y = 0; y < planetHeight; y++) {
                MapLocation loc = new MapLocation(planet,x,y);
                long karbATLoc = map.initialKarboniteAt(loc);
                if(karbATLoc > 0){
                    totalCarbs += karbATLoc;
                    MapLoc karbLoc;
                    if(baseLoc != null) {
                        karbLoc = new MapLoc(planet, loc, baseHill.getGradient(loc));
                    }else{
                        karbLoc = new MapLoc(loc);
                    }
                    karbLocs.add(karbLoc);
                    numKarbLocs++;
                }
            }
        }
        System.out.println("Starting the pq");
        System.out.println("There are " + numKarbLocs + " karbonite locations on this planet!");
        closestKarbLocs = new MPQ(numKarbLocs+1);
        for(MapLoc loc : karbLocs){
            closestKarbLocs.insert(loc);
            System.out.println("New centerLoc added to pq!");
        }
        return totalCarbs;
    }
    public boolean onMap(int x, int y){
        return (0 <= x && x < planetWidth && 0 <= y && y < planetHeight);
    }
    public boolean onMap(MapLoc loc){
        return (0 <= loc.x && loc.x < planetWidth && 0 <= loc.y && loc.y < planetHeight);
    }
}