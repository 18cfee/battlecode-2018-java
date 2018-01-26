import bc.*;

public class MarsControl {
    GameController gc;
    Path p;
    AggresiveRangers mars;
    Team myTeam;
    MarsControl(GameController gc, Path p, Team myTeam){
        this.gc = gc;
        this.p = p;
        this.myTeam = myTeam;
        mars = new AggresiveRangers(gc,p);
    }
    public void conductTurn() throws Exception{
        mars.conductTurn();

    }
    public void addUnit(Unit unit) throws Exception{
        Location loc = unit.location();
        int id = unit.id();
        if(loc.isInGarrison() || loc.isInSpace()){ // do nothing with unit
        } else if(unit.team() != myTeam){
            mars.addEnemy(new Enemy(unit));
        } else if (unit.unitType() == UnitType.Worker) {
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
}
