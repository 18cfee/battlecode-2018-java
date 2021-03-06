import bc.Direction;
import bc.GameController;
import bc.Location;
import bc.MapLocation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

public class MiniHill {
    private MapLoc destination;
    private Path p;
    private short[][] hill = null;
    private GameController gc;
    MaxGCoordinates bounds;
    public MiniHill(GameController gc, Path p){
        this.p = p;
        this.gc = gc;
    }
    public MapLocation getMapLocation(){
        return destination.toMapLocation();
    }
    public MapLoc getMapLoc(){
        return destination;
    }
    public boolean generateMini(MapLoc centerLoc, HashSet<Integer> ids) throws Exception{
        destination = centerLoc;
        bounds = new MaxGCoordinates(destination,ids,gc,p, null);
        long start = System.currentTimeMillis();
        hill = new short[bounds.width][bounds.height];
        setHillValue(destination,(short)1);
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            short dis = getHillValue(cur);
            for (int i = 0; i < 8; i++) {
                MapLoc newLoc = cur.add(p.numsDirections[i]);
                if(previouslyUncheckedMapLoc(newLoc)){
                    if(!p.passable(newLoc)){
                        //mark as unreachable
                        setHillValue(newLoc,p.greatestPathNum);
                    } else {
                        toCheck.addLast(newLoc);
                        setHillValue(newLoc,(short)(dis+1));
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        //Debug.printHill(hill);
        for (int i = 0; i < bounds.locSize; i++) {
            short val = getHillValue(bounds.locs[i]);
            // at least one of the units can not reach the target via the minimap
            if(val == 0) return false;
        }
        return true;
    }
    public boolean generateMiniRing(MapLoc centerLoc, HashSet<Integer> ids, MapLocation extra) throws Exception{
        destination = centerLoc;
        bounds = new MaxGCoordinates(destination,ids,gc,p,extra);
        long start = System.currentTimeMillis();
        hill = new short[bounds.width][bounds.height];
        setHillValue(destination,(short)1);
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            short dis = getHillValue(cur);
            for (int i = 0; i < 8; i++) {
                MapLoc newLoc = cur.add(p.numsDirections[i]);
                if(previouslyUncheckedMapLoc(newLoc)){
                    if (tooClose(destination,newLoc)) {
                        toCheck.addLast(newLoc);
                        setHillValue(newLoc,(short)2);
                    } else if (closeEnough(destination,newLoc)) {
                        toCheck.addLast(newLoc);
                        setHillValue(newLoc,(short)1);
                    } else if(!p.passable(newLoc)){
                        //mark as unreachable
                        setHillValue(newLoc,p.greatestPathNum);
                    } else {
                        toCheck.addLast(newLoc);
                        setHillValue(newLoc,(short)(dis+1));
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        //Debug.printHill(hill);
        for (int i = 0; i < bounds.locSize; i++) {
            short val = getHillValue(bounds.locs[i]);
            // at least one of the units can not reach the target via the minimap
            if(val == 0) return false;
        }
        return true;
    }
    public boolean generateMini(MapLoc centerLoc, HashSet<Integer> ids, MapLocation extra) throws Exception{
        destination = centerLoc;
        bounds = new MaxGCoordinates(destination,ids,gc,p,extra);
        long start = System.currentTimeMillis();
        hill = new short[bounds.width][bounds.height];
        setHillValue(destination,(short)1);
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            short dis = getHillValue(cur);
            for (int i = 0; i < 8; i++) {
                MapLoc newLoc = cur.add(p.numsDirections[i]);
                if(previouslyUncheckedMapLoc(newLoc)){
                    if(!p.passable(newLoc)){
                        //mark as unreachable
                        setHillValue(newLoc,p.greatestPathNum);
                    } else {
                        toCheck.addLast(newLoc);
                        setHillValue(newLoc,(short)(dis+1));
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        //Debug.printHill(hill);
        for (int i = 0; i < bounds.locSize; i++) {
            short val = getHillValue(bounds.locs[i]);
            // at least one of the units can not reach the target via the minimap
            if(val == 0) return false;
        }
        return true;
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
    public void moveUnit(int id) throws Exception{
        MapLocation loc = p.getMapLocationIfLegit(id);
        if(loc == null) return;
        MapLoc cur = new MapLoc(loc);
        short curVal = getHillValue(cur);
        short min = p.greatestPathNum;
        Direction topChoice = null;
        for (int direction = 0; direction < 8; direction++) {
            Direction dir = p.directions[direction];
            int[] dirR = p.numsDirections[direction];
            MapLoc newLoc = cur.add(dirR);
            if(inHill(newLoc) && p.canMove(id,dir)){
                short grad = getHillValue(newLoc);
                if (grad < min) {
                    min = grad;
                    topChoice = dir;
                }
            }
        }
        if(topChoice != null){ //min <= dirVal
            gc.moveRobot(id,topChoice);
        }
    }
    public Enemy findNextEnemy(ArrayList<Enemy> enemies, MapLocation startPos){
        //System.out.println(startPos.getX());
        //System.out.println(startPos.getY());
        MapLoc destination = new MapLoc(startPos);
        Enemy[][] enemyPos = new Enemy[p.planetWidth][p.planetHeight];
        for (Enemy enemy: enemies){
            enemyPos[enemy.loc.x][enemy.loc.y] = enemy;
        }
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        BitSet[] checked = new BitSet[p.planetWidth];
        for (int i = 0; i < p.planetWidth; i++) {
            BitSet set = new BitSet(p.planetHeight);
            checked[i] = set;
        }
        checked[destination.x].set(destination.y);
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            if(enemyPos[cur.x][cur.y] != null){
                return enemyPos[cur.x][cur.y];
            }
            for (int t = 0; t < p.numsDirections.length; t++) {
                int[] d = p.numsDirections[t];
                MapLoc newLoc = cur.add(d);
                if(p.onMap(newLoc) && !checked[newLoc.x].get(newLoc.y)){
                    if(!p.passable(newLoc)){
                        //mark as checked
                        checked[newLoc.x].set(newLoc.y);
                    } else {
                        checked[newLoc.x].set(newLoc.y);
                        toCheck.addLast(newLoc);
                    }
                }
            }
        }
        return null;
    }
    /*public boolean generateMiniHill(MapLocation centerLoc, HashSet<Integer> ids){
        bounds = new MaxGCoordinates(ids,gc,p);
        this.centerLoc = centerLoc;
        destination = new MapLoc(centerLoc.getX(),centerLoc.getY());
        long start = System.currentTimeMillis();
        hill = new short[bounds.width][bounds.height];
        hill[destination.x][destination.y] = 1;
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            short dis = getHillValue(cur);
            for (int i = 0; i < 8; i++) {
                MapLoc newLoc = cur.add(p.numsDirections[i]);
                if(previouslyUncheckedMapLoc(newLoc)){
                    if(!p.passable(newLoc)){
                        //mark as unreachable
                        hill[newLoc.x][newLoc.y] = p.greatestPathNum;
                    } else {
                        toCheck.addLast(newLoc);
                        hill[newLoc.x][newLoc.y] = (short)(dis + 1);
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("asdfasdfsdfasdf mini hill attention goooooooooooooo" + (end - start));
        for (int i = 0; i < bounds.locs.length; i++) {
            short val = getHillValue(bounds.locs[i]);
            // at least one of the units can not reach the target via the minimap
            if(val == 0) return false;
        }
        return true;
    }*/
    private void setHillValue(MapLoc loc, short dis){
        hill[loc.x - bounds.x1][loc.y - bounds.y1] = dis;
    }
    public short getHillValue(MapLoc loc){
        //System.out.println("what is going on");
        //System.out.println(loc.x + " " + loc.y);
        //Debug.printHill(hill);
        return hill[loc.x - bounds.x1][loc.y - bounds.y1];
    }
    public boolean generateCompactHill(MapLocation destination){
        return false;
    }
    private boolean previouslyUncheckedMapLoc(MapLoc a){
        return(inHill(a) && getHillValue(a) == (short)0);
    }
    private boolean inHill(MapLoc a){
        int x = a.x;
        int y = a.y;
        return(bounds.x1 <= x && x <= bounds.x2 && bounds.y1 <= y && y <= bounds.y2);
    }
}

class MaxGCoordinates{
    public int x1, x2, y1, y2, width, height;
    public MapLoc[] locs;
    public int locSize;
    private final static int buffer = 5;
    MaxGCoordinates(MapLoc destination, HashSet<Integer> ids, GameController gc,Path p, MapLocation extra) throws Exception{
        locs = new MapLoc[ids.size() + 1]; // 1 for extra
        int index = 0;
        x1 = x2 = destination.x;
        y1 = y2 = destination.y;
        for (Integer id: ids) {
            MapLocation location = p.getMapLocationIfLegit(id);
            if(location != null){
                locs[index++] = new MapLoc(location);
                int x = location.getX();
                int y = location.getY();
                x1 = Math.min(x1,x);
                x2 = Math.max(x2,x);
                y1 = Math.min(y1,y);
                y2 = Math.max(y2,y);
            }
        }
        if(extra != null){
            locs[index++] = new MapLoc(extra);
            int x = extra.getX();
            int y = extra.getY();
            x1 = Math.min(x1,x);
            x2 = Math.max(x2,x);
            y1 = Math.min(y1,y);
            y2 = Math.max(y2,y);
        }
        x1 = Math.max(0,x1 - buffer);
        x2 = Math.min(x2 + buffer,p.planetWidth - 1);
        y1 = Math.max(y1 - buffer,0);
        y2 = Math.min(y2+ buffer,p.planetHeight - 1);
        width = x2 - x1 + 1;
        height = y2 - y1 + 1;
    }

}