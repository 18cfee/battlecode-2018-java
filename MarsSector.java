import bc.*;

import java.util.ArrayList;

public class MarsSector {
    GameController gc;
    Path p;
    public MarsAggressive mars;
    public Workforce workforce;
    Team myTeam;
    private int sector;
    public MapLocation baseLoc;
    public short[][] hillToBase;
    MPQ priorityHarvesting;
    MarsSector(GameController gc,Path p, Team myTeam, int sector){
        this.gc = gc;
        this.p = p;
        this.myTeam = myTeam;
        mars = new MarsAggressive(gc,p,sector);
        workforce = new Workforce(gc,p);
        this.sector = sector;
        for(MapLoc loc: p.rockets.destinationList){
            if(p.rockets.disjointAreas[loc.x][loc.y] == sector){
                baseLoc = new MapLocation(p.planet,loc.x,loc.y);
                hillToBase = p.generateHill(baseLoc);
            }
        }
        priorityHarvesting = new MPQ(34,p);
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
    public void conductTurn() throws Exception{
        mars.conductTurn();
        workforce.conductTurn();
    }
}
