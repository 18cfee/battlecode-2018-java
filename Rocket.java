import bc.*;

import java.util.*;

public class Rocket {
    private HashSet<Integer> unbuiltIds;
    private HashSet<Integer> builtRockets;
    protected int roundNumber = 0;
    protected Path p;
    protected GameController gc;
    protected ArrayList<MapLoc> destinationList;
    private Stack<Integer> unClaimedIds;
    private HashMap<Integer,Integer> idToRoundLastModified;
    private BitSet[] launchPad;
    private HashMap<Integer,Integer> launchTurn;
    private final static int TURNS_BEFORE_LAUNCH = 10;
    private int marsWidth;
    private int marsHeight;
    private int destinationIndex = 0;
    protected short[][] disjointAreas;
    public ArrayList<Integer> numPerSection;
    public Rocket(Path p, GameController gc) throws Exception{
        unbuiltIds = new HashSet<>();
        builtRockets = new HashSet<>();
        this.p = p;
        this.gc = gc;
        unClaimedIds = new Stack<>();
        idToRoundLastModified = new HashMap<>();
        destinationList = new ArrayList<>();
        generateLaunchQ();
        launchPad = new BitSet[p.planetWidth];
        for (int i = 0; i < p.planetWidth; i++) {
            BitSet set = new BitSet(p.planetHeight);
            launchPad[i] = set;
        }
        launchTurn = new HashMap<>();
        factories = new ArrayList<>();
    }

    public BitSet[] getStructArea() throws Exception{
        BitSet[] area = new BitSet[p.planetWidth];
        for (int i = 0; i < p.planetWidth; i++) {
            BitSet set = new BitSet(p.planetHeight);
            area[i] = set;
        }
        for(Integer id: builtRockets){
            addToArea(area,id);
        }
        for(Integer id: unbuiltIds){
            addToArea(area,id);
        }
        if(factRound == p.round){
            for(Integer id: factories){
                addToArea(area,id);
            }
        }
        return area;
    }
    private void addToArea(BitSet[] set, int id) throws Exception{
        MapLocation loc = p.getMapLocationIfLegit(id);
        if(loc == null) return;
        int i = loc.getX();
        int j = loc.getY();
        for(int[] numDir: p.numsDirections){
            int x = i + numDir[0];
            int y = j + numDir[1];
            if(p.onMap(x,y)){
                set[x].set(y);
            }
        }
    }
    public boolean notPlacingRocketbyOtherStruct(BitSet[] noGo, int id, Direction direction) {
        if(noGo == null) return true;
        // check that is not placing into that area
        MapLocation newLoc = gc.unit(id).location().mapLocation().add(direction);
        return !noGo[newLoc.getX()].get(newLoc.getY());
    }
    private int factRound = 0 -1;
    private ArrayList<Integer> factories;
    public void addFactory(int id){
        if(p.round != factRound){
            factories.clear();
            factRound = p.round;
        }
        factories.add(id);
    }
    public int getTotalNumFactories(){
        if(p.round != factRound){
            factories.clear();
            factRound = p.round;
        }
        return factories.size();
    }
    public void addRocket(Unit unit){
        if(p.round != roundNumber){
            resetRockets();
        }
        if(unit.structureIsBuilt() == 0){
            unbuiltIds.add(unit.id());
        } else {
            int id = unit.id();
            builtRockets.add(id);
            unClaimedIds.push(id);
            if(launchTurn.containsKey(id)){
                addToLaunchPad(unit.location().mapLocation());
            }
        }
    }
    private void addToLaunchPad(MapLocation loc){
        int i = loc.getX();
        int j = loc.getY();
        for(int[] numDir: p.numsDirections){
            int x = i + numDir[0];
            int y = j + numDir[1];
            if(p.onMap(x,y)){
                launchPad[x].set(y);
            }
        }
    }
    private void resetRockets(){
        builtRockets.clear();
        unbuiltIds.clear();
        roundNumber = p.round;
        for (int i = 0; i < p.planetWidth; i++) {
            launchPad[i].clear();
        }
    }
    private boolean noRockets(){
        if(builtRockets.size() == 0) return true;
        if(p.round != roundNumber){
            resetRockets();
            return true;
        }
        return false;
    }
    private int workersTaken = 0;
    private final static int WANTEDWORKERS = 0;
    public void rocketsShouldLauchIfPossible() throws Exception{
        willLaunchSoon++;
        if(noRockets()) return;
        for(Integer id: builtRockets){
            if(p.sensableUnitNotInGarisonOrSpace(id)) {
                Unit unit = gc.unit(id);
                VecUnit units = gc.senseNearbyUnitsByType(gc.unit(id).location().mapLocation(), 2, UnitType.Ranger);
                for (int i = 0; i < units.size(); i++) {
                    tryAddToRocket(id, units.get(i).id());
                }
                if(workersTaken < WANTEDWORKERS){
                    VecUnit workers = gc.senseNearbyUnitsByType(gc.unit(id).location().mapLocation(), 2, UnitType.Worker);
                    for (int i = 0; i < workers.size(); i++) {
                        if(tryAddToRocket(id, workers.get(i).id())){
                            workersTaken++;
                        }
                        if(workersTaken == WANTEDWORKERS){
                            break;
                        }
                    }
                }
                int garisonMax = (int) unit.structureMaxCapacity();
                int curLoad = (int) unit.structureGarrison().size();
                if (((curLoad == garisonMax || tooManyRoundsSinceLastInsert(id) || roundNumber > 740 - TURNS_BEFORE_LAUNCH) && (destinationList.size() > destinationIndex))) {
                    if (!launchTurn.containsKey(id)) {
                        launchTurn.put(id,roundNumber + TURNS_BEFORE_LAUNCH);
                        willLaunchSoon = 0;
                    }
                    if ((roundNumber > 745 - TURNS_BEFORE_LAUNCH || (launchTurn.get(id) <= roundNumber))) {
                        MapLocation dest = destinationList.get(destinationIndex++).toMapLocation();
                        if(gc.canLaunchRocket(id, dest)){
                            gc.launchRocket(id, dest);
                            launchTurn.remove(id);
                        }
                    }
                }
            }
        }
    }
    public boolean tryAddToRocket(int rocketId, int unitId){
        if(gc.canLoad(rocketId,unitId)){
            gc.load(rocketId,unitId);
            idToRoundLastModified.put(rocketId,p.round);
            return true;
        }
        return false;
    }
    private int willLaunchSoon = 0;
    public boolean isLaunchTurn(){
        return (willLaunchSoon <= TURNS_BEFORE_LAUNCH + 1);
    }
    private boolean tooManyRoundsSinceLastInsert(int rocketId){
        return false;
        //return (idToRoundLastModified.containsKey(rocketId) && p.round > idToRoundLastModified.get(rocketId) + 15);
    }
//    public int takeRocket(){
//        int id = unClaimedIds.pop();
//        idToRoundLastModified.put(id,p.round);
//        return id;
//    }
//    public boolean availableRocket(){
//        return unClaimedIds.size() > 0;
//    }
    private void generateLaunchQ() throws Exception{
        PlanetMap mars;
        if(p.planet == Planet.Earth){
            mars = gc.startingMap(Planet.Mars);
        } else {
            mars = p.map;
        }
        marsWidth = (int)mars.getWidth();
        marsHeight = (int)mars.getHeight();
        disjointAreas = new short[marsWidth][marsHeight];
        numPerSection = new ArrayList<>();
        // fill in the 0 sector
        numPerSection.add(0);
        BitSet[] passable = new BitSet[marsWidth];
        //BitSet[] debug = new BitSet[marsWidth];
        for (int i = 0; i < marsWidth; i++) {
            BitSet cur = new BitSet(marsHeight);
            //debug[i] = new BitSet(marsHeight);
            for (int j = 0; j < marsHeight; j++) {
                if (mars.isPassableTerrainAt(new MapLocation(Planet.Mars,i,j)) == 1){
                    cur.set(j);
                }
            }
            passable[i] = cur;
        }
        //Debug.passable(passable);
        //System.out.println("second");
        for (int i = marsWidth - 1; i >= 0; i--) {
            BitSet set = passable[i];
            for (int j = 0; j < set.length(); j++) {
                if(disjointAreas[i][j] == 0 && passable[i].get(j)){
                    generateContiniousArea(disjointAreas,new MapLoc(i,j),passable);
                }
            }
        }
        for (int i = marsWidth - 1; i >= 0; i--) {
            for (int j = marsHeight - 1; j >= 0; j--) {
                if(passable[i].get(j) && neighborsPassable(i,j,passable)){
                    destinationList.add(new MapLoc(Planet.Mars,i,j));
                    // debug
                    //debug[i].set(j);
                    // mark neighbors unpassable
                    for(int[] numDir: p.numsDirections){
                        int x = i + numDir[0];
                        int y = j + numDir[1];
                        if(onMars(x,y)){
                            passable[x].clear(y);
                        }
                    }
                }
            }
        }
        //Debug.passable(passable);
        //Debug.passable(debug);
    }
    private short curNumDisjoints = 1;
    private void generateContiniousArea(short[][] hill, MapLoc destination,BitSet[] passable){
        hill[destination.x][destination.y] = curNumDisjoints;
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        int total = 1;
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            for (int i = 0; i < p.numsDirections.length; i++) {
                int[] d = p.numsDirections[i];
                MapLoc newLoc = cur.add(d);
                if(onMars(newLoc)){
                    if(hill[newLoc.x][newLoc.y] == 0){
                        if(!passable[newLoc.x].get(newLoc.y)){
                            //mark as unreachable
                            hill[newLoc.x][newLoc.y] = p.greatestPathNum;
                        } else {
                            toCheck.addLast(newLoc);
                            hill[newLoc.x][newLoc.y] = curNumDisjoints;
                            total++;
                        }
                    }
                }
            }
        }
        curNumDisjoints++;
        numPerSection.add(total);
    }
    public boolean neighborsPassable(int x, int y, BitSet[] passable) throws Exception{
        MapLoc loc = new MapLoc(x,y);
        for (int i = 0; i < 8; i++) {
            int[] d = p.numsDirections[i];
            MapLoc newLoc = loc.add(d);
            if((!onMars(newLoc) || !passable[newLoc.x].get(newLoc.y))) return false;
        }
        return true;
    }
    public boolean onMars(MapLoc loc){
        return (0 <= loc.x && loc.x < marsWidth && 0 <= loc.y && loc.y < marsHeight);
    }
    public boolean onMars(int x, int y){
        return (0 <= x && x < marsWidth && 0 <= y && y < marsHeight);
    }
    public boolean inLaunchPad(MapLocation loc){
        return launchPad[loc.getX()].get(loc.getY());
    }
    public void clearRocketsIfNoUnits() throws Exception{
        if(p.round!=roundNumber){
            resetRockets();
        }
    }
    public int getNumUnBuiltRockets(){
        return unbuiltIds.size();
    }
    public boolean unbuiltRocketsContains(int id){
        return unbuiltIds.contains(id);
    }
    public boolean builtRocketsContains(int id){
        return builtRockets.contains(id);
    }
    public int getNumberOfBuiltRockets(){
        return builtRockets.size();
    }
    public int getTotalRockets(){
        return builtRockets.size() + unbuiltIds.size();
    }
}
