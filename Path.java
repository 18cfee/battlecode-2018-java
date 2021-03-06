import bc.*;

import java.util.*;

public class Path {
    public final static int RANGERRANGE = 5; // everything within a square that big
    public final static int RANGERDANGER = 3; // any smaller and might no be able to shoot
    public final static int FACTORYSPOOL = 80;
    public boolean spoolingForFactory = false;
    public boolean spoolingForRocket = false;
    public int producedWorkers = 2;
    public PlanetMap map;
    GameController gc;
    int planetHeight;
    int planetWidth;
    public Direction[] directions;
    public Random random;
    public MapLocation closestStartLocation;
    Planet planet;
    public final static short greatestPathNum = 3000;
    short[][] hillToBase;
    public int[][] numsDirections = {{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};
    BitSet[] passable;
    public int round = 0;
    public int NUM_FACTORIES_WANTED = 0;
    int rocketIndex = 0;
    public MapLocation baseLoc = null;
    public long totalKarbOnEarth;
    public ArrayList<MapLoc> karbLocs;
    public MPQ closestKarbLocs;
    private int numKarbLocs = 0;
    HashSet<Integer> currentBuiltFactories;
    public Rocket rockets;
    public final static int NUM_ROCKETS_WANTED = 3;
    int maxDistanceFromBase = 16;
    short[][] centerMapHill = null;
    public boolean shouldNotTryToMakeMoreFactories = false;
    public BitSet[] karbMap;
    public Path(GameController gc,Planet planet) throws Exception{
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
        karbMap = new BitSet[planetWidth];
        for (int i = 0; i < karbMap.length; i++) {
            karbMap[i] = new BitSet(planetHeight);

        }
        for (int i = 0; i < directions.length; i++) {
            directions[i] = temp[i];
        }
        if(planet == Planet.Earth){
            //closestStartLocation = findClosestEnemyStartLoc();
            //startLoc = gc.myUnits().get(0).location().mapLocation();
        }
        if(planet == Planet.Earth){
            baseLoc = chooseBaseLocation();
            hillToBase = generateHill(baseLoc);
            setMiddleThird();
            MapLocation temp2 = findCenterLoc();
            if(temp2 != null){
                centerMapHill = generateHill(findCenterLoc());
            }
            rockets = new Rocket(this,gc);
        } else {
            rockets = new MarsGlobal(gc,this);
        }
        totalKarbOnEarth = calculateTotalKarbOnEarth();

    }

    public boolean sensableUnitNotInGarisonOrSpace(int id){
        if(!gc.canSenseUnit(id)){
            return false;
        } else {
            Location loc = gc.unit(id).location();
            return (!loc.isInGarrison() || !loc.isInSpace());
        }
    }
    public  MapLocation getMapLocationIfLegit(int id) throws Exception{
        if(!gc.canSenseUnit(id)){
            return null;
        } else {
            Location loc = gc.unit(id).location();
            if (loc.isInGarrison() || loc.isInSpace()){
                return null;
            }
            return loc.mapLocation();
        }
    }
    private MapLocation findCenterLoc(){
        for (int i = 0; i < planetWidth; i++) {
            for (int j = 0; j < planetHeight; j++) {
                if(passable[i].get(j) && hillToBase[i][j] > 7 && hillToBase[i][j] < 24 && middleThirdMap(i,j)){
                    return new MapLocation(planet,i,j);
                }
            }
        }
        return null;
    }
    private int min3x, min3y, max3x, max3y;
    private void setMiddleThird(){
        min3x = planetWidth/3;
        max3x = min3x*2;
        min3y = planetHeight/3;
        max3y = min3y*2;
    }
    public boolean canMove(int id, Direction dir) throws Exception{
        return gc.canMove(id,dir) && notMovingToLaunchArea(id,dir);
    }
    private boolean notMovingToLaunchArea(int id, Direction dir) throws Exception{
        if(!rockets.isLaunchTurn()) return true;
        if(rockets.inLaunchPad(gc.unit(id).location().mapLocation())) return true;
        MapLocation target = gc.unit(id).location().mapLocation().add(dir);
        return !rockets.inLaunchPad(target);
    }
    private boolean middleThirdMap(int x, int y){
        return (min3x <= x && x <= max3x && min3y <= y && y <= max3y);
    }
    private MapLocation chooseBaseLocation(){
        int mostGreenCov = 0;
        VecUnit units = gc.myUnits();
        MapLoc bestLoc = null;
        for (int i = 0; i < units.size(); i++) {
            MapLocation loc = units.get(i).location().mapLocation();
            MapLoc destination = new MapLoc(loc);
            destination.distanceToBase = 0;
            ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
            toCheck.addLast(destination);
            BitSet[] checked = new BitSet[planetWidth];
            for (int r = 0; r < planetWidth; r++) {
                BitSet set = new BitSet(planetHeight);
                checked[r] = set;
            }
            checked[destination.x].set(destination.y);
            while(!toCheck.isEmpty()){
                MapLoc cur = toCheck.removeFirst();
                int thisSpot = calcNumReachableWithin5(new MapLoc(cur.x,cur.y,0));
                if(thisSpot > mostGreenCov){
                    mostGreenCov = thisSpot;
                    bestLoc = cur;
                }
                long dis = cur.distanceToBase + 1;
                if(dis < 5){
                    for (int k = 0; k < numsDirections.length; k++) {
                        int[] d = numsDirections[k];
                        MapLoc newLoc = cur.add(d,dis);
                        if(onMap(newLoc) && !checked[newLoc.x].get(newLoc.y)){
                            if(!passable(newLoc)){
                                //mark as checked
                                checked[newLoc.x].set(newLoc.y);
                            } else {
                                checked[newLoc.x].set(newLoc.y);
                                toCheck.addLast(newLoc);
                            }
                        }
                    }
                }
            }
        }
        if(bestLoc == null){
            return units.get(0).location().mapLocation();
        }
        //System.out.println("chose " + bestLoc.x + " " + bestLoc.y);
        return new MapLocation(Planet.Earth,bestLoc.x,bestLoc.y);
    }
    private int calcNumReachableWithin5(MapLoc destination){
        int total = 0;
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        BitSet[] checked = new BitSet[planetWidth];
        for (int i = 0; i < planetWidth; i++) {
            BitSet set = new BitSet(planetHeight);
            checked[i] = set;
        }
        checked[destination.x].set(destination.y);
        while(!toCheck.isEmpty()){
            total++;
            MapLoc cur = toCheck.removeFirst();
            long dis = cur.distanceToBase + 1;
            if(dis < 5){
                for (int t = 0; t < numsDirections.length; t++) {
                    int[] d = numsDirections[t];
                    MapLoc newLoc = cur.add(d,dis);
                    if(onMap(newLoc) && !checked[newLoc.x].get(newLoc.y)){
                        if(!passable(newLoc)){
                            //mark as checked
                            checked[newLoc.x].set(newLoc.y);
                        } else {
                            checked[newLoc.x].set(newLoc.y);
                            toCheck.addLast(newLoc);
                        }
                    }
                }
            }
        }
        return total;
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

    public void addFactory(int id){
            currentBuiltFactories.add(id);
    }

    public int getNumFactories(){
        return currentBuiltFactories.size();
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
    public boolean moveInRandomAvailableDirection(int id) throws Exception{
        int startD = random.nextInt(8);
        for (int i = startD; i < startD + 8; i++) {
            Direction d = directions[i%8];
            if(gc.isMoveReady(id) && canMove(id,d)){
                gc.moveRobot(id,d);
                return true;
            }
        }
        return false;
    }
    long totalTimeInFunc = 0;
    int functionCalled = 0;
    public short[][] generateHill(MapLocation target){ ;
        MapLoc destination = new MapLoc(target);
        functionCalled++;
        long start = System.currentTimeMillis();
        //System.out.println("Height: " + planetHeight);
        //System.out.println("Width: " + planetWidth);
        short hill[][] = new short[planetWidth][planetHeight];
        hill[destination.x][destination.y] = 1;
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            short dis = hill[cur.x][cur.y];
            for (int i = 0; i < numsDirections.length; i++) {
                int[] d = numsDirections[i];
                MapLoc newLoc = cur.add(d);
                if(previouslyUncheckedMapLoc(newLoc,hill)){
                    if(!passable(newLoc)){
                        //mark as unreachable
                        hill[newLoc.x][newLoc.y] = greatestPathNum;
                    } else {
                        toCheck.addLast(newLoc);
                        hill[newLoc.x][newLoc.y] = (short)(dis + 1);
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        totalTimeInFunc=(end - start);
        //System.out.println(totalTimeInFunc);
        // todo smaller versions need to know if a path was found
        return hill;
    }
    private boolean previouslyUncheckedMapLoc(MapLoc a, short[][] hill){
        return(onMap(a) && hill[a.x][a.y] == (short)0);
    }
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
                    if(baseLoc != null && hillToBase[x][y] != 0) {
                        karbLoc = new MapLoc(planet, loc, hillToBase[x][y]);
                        karbLocs.add(karbLoc);
                        karbMap[karbLoc.x].set(karbLoc.y);
                        numKarbLocs++;
                    }
                }
            }
        }
        closestKarbLocs = new MPQ(numKarbLocs+1, this);
        for(MapLoc loc : karbLocs){
            closestKarbLocs.insert(loc);
            //System.out.println("New loc added to pq!");
        }
        return totalCarbs;
    }
    public boolean onMap(int x, int y){
        return (0 <= x && x < planetWidth && 0 <= y && y < planetHeight);
    }
    public boolean onMap(MapLoc loc){
        return (0 <= loc.x && loc.x < planetWidth && 0 <= loc.y && loc.y < planetHeight);
    }
    public int movesToBase(MapLoc loc){
        return hillToBase[loc.x][loc.y];
    }
    public int movesToBase(MapLocation loc){
        return hillToBase[loc.getX()][loc.getY()];
    }
    public void moveIfPossible(int id) throws Exception{
        int randomN = random.nextInt(8);
        for (int i = 0; i < 8; i++) {
            int d = (randomN + i)%8;
            Direction dir = directions[d];
            if (canMove(id,dir) && gc.isMoveReady(id)){
                gc.moveRobot(id,dir);
            }
        }
    }
}