import bc.*;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

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
    }

    public void addRocket(Unit unit){
        if(p.round != roundNumber){
            resetRockets();
        }
        if(unit.structureIsBuilt() == 0){
            unbuiltIds.add(unit.id());
        } else {
            builtRockets.add(unit.id());
            unClaimedIds.push(unit.id());
            addToLaunchPad(unit.location().mapLocation());
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
        Debug.passable(launchPad);
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
                if (((curLoad == garisonMax || tooManyRoundsSinceLastInsert(id) || roundNumber == 749) && !destinationStack.empty())) {
                    MapLocation dest = destinationStack.pop();
                    if (gc.canLaunchRocket(id, dest)) {
                        //gc.launchRocket(id, dest);
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
        PlanetMap mars = gc.startingMap(Planet.Mars);
        int marsWidth = (int)mars.getWidth();
        int marsHeight = (int)mars.getHeight();
        BitSet[] passable = new BitSet[marsWidth];
        for (int i = 0; i < marsWidth; i++) {
            BitSet cur = new BitSet(marsHeight);
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
                if(passable[i].get(j)){
                    destinationStack.push(new MapLocation(Planet.Mars,i,j));
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
        ///Debug.passable(passable);
    }
    public boolean isLaunchTurn(){
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
}
