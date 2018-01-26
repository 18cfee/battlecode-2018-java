import bc.MapLocation;
import bc.Planet;

public class MapLoc {
    public int x;
    public int y;
    Planet planet = null;
    long distanceToBase = 0;

    MapLoc(int x, int y){
        this.x = x;
        this.y = y;
    }

    public MapLoc(MapLocation loc){
        this.x = loc.getX();
        this.y = loc.getY();
    }

    public MapLoc(Planet planet, MapLocation loc, long distanceToBase){
        this.x = loc.getX();
        this.y = loc.getY();
        this.distanceToBase = distanceToBase;
        this.planet = planet;
    }
    public MapLoc add(int x, int y){
        return new MapLoc(this.x + x,this.y + y);
    }
    public MapLoc add(int[] dir){
        return new MapLoc(this.x + dir[0],this.y + dir[1]);
    }

    public MapLocation toMapLocation(){
        if(planet == null){
            return null;
        }else {
            return new MapLocation(planet, x, y);
        }
    }
}
