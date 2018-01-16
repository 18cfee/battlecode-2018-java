import bc.GameController;
import bc.MapLocation;
import bc.Unit;
import bc.UnitType;

public class Workforce{
    GameController gc;
    Path p;
    int[] idle = new int[100];
    int idleIndex = 0;
    Workers[] workerGroups = new Workers[10];
    int groupIndex = 0;
    boolean canBuildRocket = false;

    public Workforce(GameController gc, Path p) {
        this.gc = gc;
        this.p = p;
        createGroup();
    }
    public void conductTurn() throws Exception{

        if(groupIndex == 0){
            createGroup();
        }
        while(idleIndex > 0){
            workerGroups[0].add(idle[--idleIndex]);
        }
        if(p.unbuiltFactIndex == p.builtFactIndex && p.getNumFactories() < p.NUM_FACTORIES_WANTED){
            MapLocation blueLoc = workerGroups[0].setBlueprint(UnitType.Factory);
            if(p.baseLoc == null){
                p.baseLoc = blueLoc;
                p.hillToBase = p.generateHill(p.startLoc);
            }
            if(blueLoc != null ){
                short[][] hill = p.generateHill(blueLoc);
                workerGroups[0].changeToTargetMap(hill);
                p.unbuiltFactIndex++;
            }
        }

        if(canBuildRocket && p.rocketIndex == 0){
            MapLocation blueLoc = workerGroups[0].setBlueprint(UnitType.Rocket);
            if(blueLoc != null){
                short[][] hill = p.generateHill(blueLoc);
                p.firstRocketLocHill = hill;
                workerGroups[0].changeToTargetMap(hill);
                p.rocketIndex++;
                //hillChosen = true;
            }
        }
        for(int i = 0; i < groupIndex; i++){
            workerGroups[i].conductTurn();
        }
        for(int i = 0; i < groupIndex; i++){
            workerGroups[i].resetWorkerIndexCount();
        }
        idleIndex = 0;
    }


    public void addRocket(Unit unit){
        workerGroups[0].addRocket(unit);
    }

    public void createGroup(){
        workerGroups[groupIndex] = new Workers(gc, p);
        groupIndex++;
    }

    public void setCanBuildRocket(boolean set){
        canBuildRocket = set;
    }

    public boolean isCanBuildRocket(){
        return canBuildRocket;
    }

    public void addWorker(int id){
        /*use this code later
        boolean present = false;
        for(int i = 0; i <= idleIndex; i++){
            if(idle[i] == id){
                present = true;
                break;
            }
        }

        if(!present){
            System.out.println("Adding worker to the idle list");
            idle[idleIndex] = id;
            idleIndex++;
        }*/
        idle[idleIndex] = id;
        idleIndex++;
    }
}
