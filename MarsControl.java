import bc.*;

import java.util.ArrayList;
import java.util.Collections;

public class MarsControl {
    GameController gc;
    Path p;
    Team myTeam;
    ArrayList<MarsSector> armies;
    ArrayList<Enemy> enemies;
    private int controlRound = 0;
    MarsControl(GameController gc, Path p, Team myTeam){
        this.gc = gc;
        this.p = p;
        this.myTeam = myTeam;
        armies = new ArrayList<>();
        enemies = new ArrayList<>();
        for (int i = 0; i < p.rockets.numPerSection.size(); i++) {
            armies.add(new MarsSector(gc,p,myTeam));
        }
    }
    public void conductTurn() throws Exception{
        distributeEnemies();
        for (MarsSector sector: armies){
            sector.conductTurn();
        }
    }
    public void addUnit(Unit unit) throws Exception{
        Location loc = unit.location();
        if(loc.isInGarrison() || loc.isInSpace()){
            // do nothing with unit
            return;
        } else if(unit.team() != myTeam){
            addToEnemies(new Enemy(unit));
        } else {
            MapLocation unitLoc = loc.mapLocation();
            int groupId = p.rockets.disjointAreas[unitLoc.getX()][unitLoc.getY()] - 1;
            armies.get(groupId).addUnit(unit);
        }
    }
    private void addToEnemies(Enemy enemy){
        if(controlRound != p.round){
            enemies.clear();
            controlRound = p.round;
        }
        enemies.add(enemy);
    }
    private void distributeEnemies(){
       if(controlRound != p.round){
           controlRound = p.round;
           return;
       }
       Collections.sort(enemies,new EnemySorter());
       for(Enemy enemy: enemies){
           for(MarsSector sector: armies){
               sector.mars.addEnemy(enemy);
           }
       }
    }
}
