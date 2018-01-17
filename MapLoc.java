import bc.MapLocation;

public class MapLoc {
    public int x;
    public int y;
    MapLoc(int x, int y){
        this.x = x;
        this.y = y;
    }
    MapLoc(MapLocation loc){
        this.x = loc.getX();
        this.y = loc.getY();
    }
}
