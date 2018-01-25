import bc.GameController;
import bc.MapLocation;

import java.util.ArrayDeque;

public class AggresiveRangers extends Fighter{
    private short[][] attackRingHill = null;
    private MapLocation target = null;
    private boolean seesEnemy;
    private short groupTargetCooldown = 0;
    AggresiveRangers(GameController gc, Path p){
        super(gc,p);
        seesEnemy = false;
    }
    @Override
    public void conductTurn() throws Exception{
        if(noUnits()) {
            groupTargetCooldown = 0;
            seesEnemy = false;
            attackRingHill = null;
            return;
        }
        if(!noEnemies() && seesEnemy == false && groupTargetCooldown <= 0){
            Enemy enemy = enemies.get(0);
            if(enemy.hp > 0){
                MapLocation a = gc.unit(enemy.id).location().mapLocation();
                target = a;
                attackRingHill = generateAttackRing(target);
                groupTargetCooldown= 25;
                seesEnemy = true;
            }
        } else if ((seesEnemy == true && enemies.size() == 0) || groupTargetCooldown < -35){
            seesEnemy = false;
        }
        if(attackRingHill!= null){
            moveToTarget(attackRingHill);
        } else if (p.centerMapHill != null){
            moveToTarget(p.centerMapHill);
        } else{
            roamRandom();
        }
        shootOptimally();
        groupTargetCooldown--;
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
