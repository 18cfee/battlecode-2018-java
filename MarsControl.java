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
    private AsteroidPattern pattern;
    MarsControl(GameController gc, Path p, Team myTeam){
        this.gc = gc;
        this.p = p;
        this.myTeam = myTeam;
        armies = new ArrayList<>();
        enemies = new ArrayList<>();
        // just so the array index matches the groups Id
        armies.add(new MarsSector(gc,p,myTeam,0));
        for (int i = 1; i < p.rockets.numPerSection.size(); i++) {
            armies.add(new MarsSector(gc,p,myTeam,i));
        }
        pattern = gc.asteroidPattern();
        for (int i = 0; i < p.planetWidth; i++) {
            for (int j = 0; j < p.planetHeight; j++) {
                MapLocation location = new MapLocation(p.planet,i,j);
                if(p.map.initialKarboniteAt(location) > 0 && p.passable(location)){
                    int sector = p.rockets.disjointAreas[i][j];
                    armies.get(sector).addDeposit(location);
                }

            }
        }
    }
    private void distributeKarbToSectors(){
        if(pattern.hasAsteroid(p.round)){
            AsteroidStrike strike = pattern.asteroid(p.round);
            MapLocation loc = strike.getLocation();
            int sector = p.rockets.disjointAreas[loc.getX()][loc.getY()];
            //System.out.println(sector + "this sector is getting karb added to it");
            if(p.passable(loc)){
                armies.get(sector).addDeposit(loc);
            }
        }
    }
    public void conductTurn() throws Exception{
        distributeKarbToSectors();
        distributeEnemies();
        // starts at one cause 0 is the empty army
        for (int i = 1; i < armies.size(); i++) {
            armies.get(i).conductTurn();
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
            int groupId = p.rockets.disjointAreas[unitLoc.getX()][unitLoc.getY()];
            //System.out.println(groupId + " this group is having a unit added to it");
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
