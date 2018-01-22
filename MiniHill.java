import bc.GameController;
import bc.MapLocation;

import java.util.ArrayDeque;
import java.util.HashSet;

public class MiniHill {
    private MapLocation centerLoc;
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
        return centerLoc;
    }
    public MapLoc getMapLoc(){
        return destination;
    }
    public boolean generateMiniHill(MapLocation centerLoc, HashSet<Integer> ids){
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
                    if(p.passable(newLoc)){
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
        System.out.println("asdfasdfsdfasdf milis attention goooooooooooooo" + (end - start));
        for (int i = 0; i < bounds.locs.length; i++) {
            short val = getHillValue(bounds.locs[i]);
            // at least one of the units can not reach the target via the minimap
            if(val == 0) return false;
        }
        return true;
    }
    private void setHillValue(MapLoc loc, short dis){
        hill[loc.x - bounds.x1][loc.y - bounds.y1] = dis;
    }
    public short getHillValue(MapLoc loc){
        return hill[loc.x + bounds.x1][loc.y + bounds.y1];
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
    public int x1 = 49, x2 = 0, y1 = 49, y2 = 0, width, height;
    public MapLoc[] locs;
    private final static int buffer = 5;
    MaxGCoordinates(HashSet<Integer> ids, GameController gc,Path p){
        locs = new MapLoc[ids.size()];
        int i = 0;
        for (Integer id: ids) {
            MapLocation location = gc.unit(id).location().mapLocation();
            locs[i++] = new MapLoc(location);
            int x = location.getX();
            int y = location.getY();
            x1 = Math.min(x1,x);
            x2 = Math.max(x2,x);
            y1 = Math.min(y1,y);
            y2 = Math.min(y2,y);
        }
        x1 = Math.min(0,x1 - buffer);
        x2 = Math.max(x2 + buffer,p.planetWidth - 1);
        y1 = Math.min(y1 - buffer,0);
        y2 = Math.max(y2+ buffer,p.planetHeight - 1);
        width = x2 - x1;
        height = y2 - y1;
    }
}