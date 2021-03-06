import bc.*;

import java.util.ArrayList;
import java.util.BitSet;

public class MarsSector {
    GameController gc;
    Path p;
    public MarsAggressive mars;
    public Workforce workforce;
    Team myTeam;
    private int sector;
    public MapLocation baseLoc;
    public short[][] hillToBase;
    public MPQ priorityHarvesting;
    BitSet[] karbMap;
    MarsSector(GameController gc,Path p, Team myTeam, int sector){
        this.gc = gc;
        this.p = p;
        this.myTeam = myTeam;
        mars = new MarsAggressive(gc,p,sector);
        this.sector = sector;
        for(MapLoc loc: p.rockets.destinationList){
            if(p.rockets.disjointAreas[loc.x][loc.y] == sector){
                baseLoc = new MapLocation(p.planet,loc.x,loc.y);
                hillToBase = p.generateHill(baseLoc);
            }
        }
        karbMap = new BitSet[p.planetWidth];
        for (int i = 0; i < karbMap.length; i++) {
            karbMap[i] = new BitSet(p.planetHeight);
        }
        priorityHarvesting = new MPQ(p.planetWidth*p.planetHeight+1000,p);
        workforce = new Workforce(gc,p,this);
    }
    public void addUnit(Unit unit) throws Exception {
        Location loc = unit.location();
        int id = unit.id();
        if (unit.unitType() == UnitType.Worker) {
            workforce.addWorker(id);
        } else if (unit.unitType() == UnitType.Factory) {
//                            if(unit.structureIsBuilt() == 1){
//                                sprint.addFact(unit);
//                                p.addFactory(id);
//                            }
        } else if (unit.unitType() == UnitType.Rocket) {
            for (int t = 0; t < 8; t++) {
                Direction dir = p.directions[t];
                if(gc.canUnload(id,dir)){
                    gc.unload(id,dir);
                }
            }
        }else{
            mars.add(id);
        }
    }
    public void addDeposit(MapLocation loc){
        //System.out.println(sector + " was added to");
        short disToBase = hillToBase[loc.getX()][loc.getY()];
        //System.out.println(disToBase + " distance to base");
        MapLoc mapLoc = new MapLoc(Planet.Mars,loc,disToBase);
        karbMap[mapLoc.x].set(mapLoc.y);
        priorityHarvesting.insert(mapLoc);
    }
    public void conductTurn() throws Exception{
        mars.conductTurn();
        workforce.conductTurn();
    }
}
