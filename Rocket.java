import bc.MapLocation;
import bc.Unit;

import java.util.HashMap;
import java.util.HashSet;

public class Rocket {
    private HashSet<Integer> unbuiltIds;
    private HashMap<Integer, MapLocation> builtRockets;
    private int roundNumber = 0;
    private Path p;
    public Rocket(Path p) {
        unbuiltIds = new HashSet<>();
        builtRockets = new HashMap<>();
        this.p = p;
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
