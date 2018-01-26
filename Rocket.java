import bc.*;

import java.util.*;

public class Rocket {
    private HashSet<Integer> unbuiltIds;
    private HashSet<Integer> builtRockets;
    private int roundNumber = 0;
    private Path p;
    private GameController gc;
    private Stack<MapLocation> destinationStack;
    private Stack<Integer> unClaimedIds;
    private HashMap<Integer,Integer> idToRoundLastModified;
    private BitSet[] launchPad;
    private HashMap<Integer,Integer> launchTurn;
    private final static int TURNS_BEFORE_LAUNCH = 10;
    private int marsWidth;
    private int marsHeight;
    public Rocket(Path p, GameController gc) {
        unbuiltIds = new HashSet<>();
        builtRockets = new HashSet<>();
        this.p = p;
        this.gc = gc;
        unClaimedIds = new Stack<>();
        idToRoundLastModified = new HashMap<>();
        destinationStack = new Stack<>();
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
                int garisonMax = (int) unit.structureMaxCapacity();
                int curLoad = (int) unit.structureGarrison().size();
                if (((curLoad == garisonMax || tooManyRoundsSinceLastInsert(id) || roundNumber > 740 - TURNS_BEFORE_LAUNCH) && !destinationStack.empty())) {
                    MapLocation dest = destinationStack.pop();
                    if (!launchTurn.containsKey(id)) {
                        launchTurn.put(id,roundNumber + TURNS_BEFORE_LAUNCH);
                        willLaunchSoon = 0;
                    }
                    if (gc.canLaunchRocket(id, dest) && (roundNumber > 745 - TURNS_BEFORE_LAUNCH || (launchTurn.get(id) <= roundNumber))) {
                        gc.launchRocket(id, dest);
                        launchTurn.remove(id);
                    }
                }
            }
        }
    }
    public void tryAddToRocket(int rocketId, int unitId){
        if(gc.canLoad(rocketId,unitId)){
            gc.load(rocketId,unitId);
            idToRoundLastModified.put(rocketId,p.round);
        }
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
    private void generateLaunchQ(){
        PlanetMap mars;
        if(p.planet == Planet.Earth){
            mars = gc.startingMap(Planet.Mars);
        } else {
            mars = p.map;
        }
        marsWidth = (int)mars.getWidth();
        marsHeight = (int)mars.getHeight();
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
            for (int j = marsHeight - 1; j >= 0; j--) {
                if(passable[i].get(j) && neighborsPassable(i,j,passable)){
                    destinationStack.push(new MapLocation(Planet.Mars,i,j));
                    // debug
                    //debug[i].set(j);
                    // mark neighbors unpassable
                    for(int[] numDir: p.numsDirections){
                        int x = i + numDir[0];
                        int y = j + numDir[1];
                        if(p.onMap(x,y)){
                            passable[x].clear(y);
                        }
                    }
                }
            }
        }
        //Debug.passable(passable);
        //Debug.passable(debug);
    }
    public boolean neighborsPassable(int x, int y, BitSet[] passable){
        MapLoc loc = new MapLoc(x,y);
        for (int i = 0; i < 8; i++) {
            int[] d = p.numsDirections[i];
            MapLoc newLoc = loc.add(d);
            if((!p.onMap(newLoc) || !passable[newLoc.x].get(newLoc.y))) return false;
        }
        System.out.println("returning true");
        return true;
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
