import bc.*;

import java.util.*;

public class Rocket {
    private HashSet<Integer> unbuiltIds;
    private HashMap<Integer, MapLocation> builtRockets;
    private int roundNumber = 0;
    private Path p;
    private GameController gc;
    private Stack<MapLocation> destinationStack;
    private ArrayList<Integer> availableIds;
    private HashMap<Integer,Integer> idToRoundLastModified;
    private HashSet<Integer> oldIds;
    public Rocket(Path p, GameController gc) {
        unbuiltIds = new HashSet<>();
        builtRockets = new HashMap<>();
        this.p = p;
        this.gc = gc;
        availableIds = new ArrayList<>();
        idToRoundLastModified = new HashMap<>();
        destinationStack = new Stack<>();
        oldIds = new HashSet<>(20);
        generateLaunchQ();
    }
    public void addRocket(Unit unit){
        if(p.round != roundNumber){
            roundNumber = p.round;
            unbuiltIds.clear();
            builtRockets.clear();
            availableIds.clear();
        }
        int id = unit.id();
        if(unit.structureIsBuilt() == 0){
            unbuiltIds.add(id);
        } else {
            builtRockets.put(unit.id(),unit.location().mapLocation());
            if(!oldIds.contains(id)){
                availableIds.add(id);
            }
        }
    }
    private boolean noRockets(){
        if(builtRockets.size() == 0) return true;
        if(p.round != roundNumber){
            builtRockets.clear();
            roundNumber = p.round;
            availableIds.clear();
            return true;
        }
        return false;
    }
    public void rocketsShouldLaunchIfPossible() throws Exception{
        if(noRockets()) return;
        for(Integer id: builtRockets.keySet()){
            Unit unit = gc.unit(id);
            int garisonMax = (int)unit.structureMaxCapacity();
            int curLoad = (int)unit.structureGarrison().size();
            if(((true ||curLoad == garisonMax || tooManyRoundsSinceLastInsert(id) || roundNumber == 749) && !destinationStack.empty())){
                MapLocation dest = destinationStack.pop();
                if (gc.canLaunchRocket(id,dest)){
                    gc.launchRocket(id,dest);
                }
            }
        }
    }
    public boolean successfullyAdded(int rocketId, int unitId){
        if(gc.canLoad(rocketId,unitId)){
            gc.load(rocketId,unitId);
            idToRoundLastModified.put(rocketId,p.round);
            return true;
        }
        return false;
    }
    private boolean tooManyRoundsSinceLastInsert(int rocketId){
        return (idToRoundLastModified.containsKey(rocketId) && p.round > idToRoundLastModified.get(rocketId) + 15);
    }
    public int takeRocket(){
        System.out.println("number of rockets on stack " + availableIds.size());
            int id = availableIds.get(0);
            idToRoundLastModified.put(id,p.round);
            oldIds.add(id);
            return id;
    }
    public boolean availableRocket(){
        return availableIds.size() > 0;
    }
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
        for (int i = 0; i < marsWidth; i++) {
            for (int j = 0; j < marsHeight; j++) {
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
    public void clearRockets(){
        unbuiltIds.clear();
        builtRockets.clear();
        availableIds.clear();
    }
    public int getNumUnBuiltRockets(){
        return unbuiltIds.size();
    }
    public boolean unbuiltRocketsContains(int id){
        return unbuiltIds.contains(id);
    }
    public boolean builtRocketsContains(int id){
        return builtRockets.containsKey(id);
    }
    public int getNumberOfBuiltRockets(){
        return builtRockets.size();
    }
    public MapLocation getMapLocation(int id){
        if(builtRockets.containsKey(id))return builtRockets.get(id);
        else return null;
    }

}
