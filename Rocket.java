import bc.*;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Rocket {
    private HashSet<Integer> unbuiltIds;
    private HashMap<Integer, MapLocation> builtRockets;
    private int roundNumber = 0;
    private Path p;
    private GameController gc;
    private Stack<MapLocation> destinationStack;
    public Rocket(Path p, GameController gc) {
        unbuiltIds = new HashSet<>();
        builtRockets = new HashMap<>();
        this.p = p;
        this.gc = gc;
    }
    public void addRocket(Unit unit){
        if(p.round != roundNumber){
            roundNumber = p.round;
            unbuiltIds.clear();
            builtRockets.clear();
        }
        if(unit.structureIsBuilt() == 0){
            unbuiltIds.add(unit.id());
        } else {
            builtRockets.put(unit.id(),unit.location().mapLocation());
        }
    }
    public void rocketsShouldLauchIfPossible(){
        for(Integer id: builtRockets.keySet()){
            Unit unit = gc.unit(id);
            int garisonMax = (int)unit.structureMaxCapacity();
            int curLoad = (int)unit.structureGarrison().size();
            if(curLoad == garisonMax && !destinationStack.empty()){
                MapLocation dest = destinationStack.pop();
                if (gc.canLaunchRocket(id,dest)){
                    gc.launchRocket(id,dest);
                }
            }
        }
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
        for (int i = 0; i < marsWidth; i++) {
            for (int j = 0; j < marsHeight; j++) {
                if(passable[i].get(j)){
                    destinationStack.push(new MapLocation(Planet.Mars,i,j));
                    // mark neighbors unpassable
                    for(int[] numDir: p.numsDirections){
                        if(p.onMap(i,j)){
                            passable[i].set(j,false);
                        }
                    }
                }
            }
        }
    }
    public void clearRocketsIfNoUnits(){
        if(p.round!=roundNumber){
            unbuiltIds.clear();
            builtRockets.clear();
        }
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
}
