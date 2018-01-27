import bc.GameController;
import bc.MapLocation;

public class MarsGlobal extends Rocket {
    MarsGlobal(GameController gc, Path p) throws Exception{
        super(p,gc);
    }
    public MapLocation getBaseLocationForSection(int section){
        return destinationList.get(0).toMapLocation();
    }
}
