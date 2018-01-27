import bc.*;

public class MarsSector {
    GameController gc;
    Path p;
    public AggresiveRangers mars;
    Team myTeam;
    MarsSector(GameController gc,Path p, Team myTeam){
        this.gc = gc;
        this.p = p;
        this.myTeam = myTeam;
        mars = new AggresiveRangers(gc,p);
    }
    public void addUnit(Unit unit) throws Exception {
        Location loc = unit.location();
        int id = unit.id();
        if (unit.unitType() == UnitType.Worker) {
            //workforce.addWorker(id);
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
    }
}
