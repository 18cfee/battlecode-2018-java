import bc.GameController;
import bc.MapLocation;

public class MarsAggressive extends AggresiveRangers{
    private int groupNum;
    MarsAggressive(GameController gc, Path p, int groupNum){
        super(gc,p);
        this.groupNum = groupNum;
    }
    @Override
    protected boolean onContinuousArea(MapLocation a){
        System.out.println(p.rockets.disjointAreas[a.getX()][a.getY()] == groupNum);
        return (p.rockets.disjointAreas[a.getX()][a.getY()] == groupNum);
    }
}
