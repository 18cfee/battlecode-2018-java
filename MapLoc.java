import bc.MapLocation;

public class MapLoc {
    public int x;
    public int y;
    long distanceToBase = 0;

    MapLoc(int x, int y){
        this.x = x;
        this.y = y;
    }

    MapLoc(MapLocation loc){
        this.x = loc.getX();
        this.y = loc.getY();
    }
    MapLoc(MapLocation loc, MapLocation base){
        this.x = loc.getX();
        this.y = loc.getY();
        distanceToBase = loc.distanceSquaredTo(base);
    }

}
