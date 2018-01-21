import bc.Direction;
import bc.MapLocation;

import java.util.ArrayDeque;

public class Hill {
    private MapLocation centerLoc;
    private MapLoc destination;
    private Path p;
    private short[][] hill = null;
    public Hill(Path p){
        this.p = p;
    }
    public MapLocation getMapLocation(){
        return centerLoc;
    }
    public MapLoc getMapLoc(){
        return destination;
    }
    public void generateCompleteReachableHill(MapLocation centerLoc){
        this.centerLoc = centerLoc;
        destination = new MapLoc(centerLoc.getX(),centerLoc.getY());
        long start = System.currentTimeMillis();
        hill = new short[p.planetWidth][p.planetHeight];
        hill[destination.x][destination.y] = 1;
        ArrayDeque<MapLoc> toCheck = new ArrayDeque<>();
        toCheck.addLast(destination);
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            short dis = hill[cur.x][cur.y];
            for (int i = 0; i < 8; i++) {
                MapLoc newLoc = cur.add(p.numsDirections[i]);
                if(previouslyUncheckedMapLoc(newLoc,hill)){
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
        // todo smaller versions need to know if a path was found
    }
    public boolean generateCompactHill(MapLocation destination){
        return false;
    }
    private boolean previouslyUncheckedMapLoc(MapLoc a, short[][] hill){
        return(p.onMap(a) && hill[a.x][a.y] == (short)0);
    }
}
