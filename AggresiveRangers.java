import bc.GameController;
import bc.MapLocation;

import java.util.ArrayDeque;

public class AggresiveRangers extends Fighter{
    AggresiveRangers(GameController gc, Path p){
        super(gc,p);
    }
    private short[][] attackRingHill = null;
    private MapLocation target = null;
    private boolean seesEnemy = false;
    private short groupTargetCooldown = 0;
    @Override
    public void conductTurn() throws Exception{
        if(noUnits())return;
        System.out.println("            kill em with units: " + size());
        //System.out.println("aggressive rangers " + size());
        if(!noEnemies() && seesEnemy == false && groupTargetCooldown == 0){
            seesEnemy = true;
            Enemy enemy = enemies.get(0);
            if(enemy.hp > 0){
                MapLocation a = gc.unit(enemy.id).location().mapLocation();
                target = a;
                attackRingHill = generateAttackRing(target);
                groupTargetCooldown+= 25;
            }
        } else if (seesEnemy = true && enemies.size() == 0){
            seesEnemy = false;
        }
        if(attackRingHill!= null) moveToTarget(attackRingHill);
        else roamRandom();
        shootOptimally();
        if(groupTargetCooldown > 0) groupTargetCooldown--;
    }
    private short[][] generateAttackRing(MapLocation loc){
        MapLoc destination = new MapLoc(loc);
        short hill[][] = new short[p.planetWidth][p.planetHeight];
        hill[destination.x][destination.y] = 2;
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            short dis = hill[cur.x][cur.y];
            for(int[] d : p.numsDirections){
                MapLoc newLoc = cur.add(d);
                if(previouslyUncheckedMapLoc(newLoc,hill)){
                    if (tooClose(destination,newLoc)) {
                        toCheck.addLast(newLoc);
                        hill[newLoc.x][newLoc.y] = 2;
                    } else if (closeEnough(destination,newLoc)) {
                        toCheck.addLast(newLoc);
                        hill[newLoc.x][newLoc.y] = 1;
                    } else if(!p.passable(newLoc)){
                        //mark as unreachable
                        hill[newLoc.x][newLoc.y] = p.greatestPathNum;
                    } else {
                        toCheck.addLast(newLoc);
                        hill[newLoc.x][newLoc.y] = (short)(dis + 1);
                    }
                }
            }
        }
        return hill;
    }
    private boolean previouslyUncheckedMapLoc(MapLoc a, short[][] hill){
        return(p.onMap(a) && hill[a.x][a.y] == (short)0);
    }
    private boolean tooClose(MapLoc a, MapLoc b){
        int dif1 = Math.abs(a.x - b.x);
        int dif2 = Math.abs(a.y - b.y);
        return (Math.max(dif1,dif2) <= p.RANGERDANGER);
    }
    private boolean closeEnough(MapLoc a, MapLoc b){
        int dif1 = Math.abs(a.x - b.x);
        int dif2 = Math.abs(a.y - b.y);
        return (Math.max(dif1,dif2) <= p.RANGERRANGE);
    }
}
