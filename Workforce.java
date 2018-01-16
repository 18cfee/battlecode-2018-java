import bc.GameController;
import bc.MapLocation;
import bc.UnitType;

public class Workforce{
    GameController gc;
    Path p;
    int[] idle = new int[100];
    int idleIndex = 0;
    Workers[] workerGroups = new Workers[10];
    int groupIndex = 0;
    boolean canBuildRocket = false;

    public Workforce(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
    }
    private boolean hillChosen = false;
    public void conductTurn() throws Exception{
        if(groupIndex == 0){
            createGroup();
        }
        while(idleIndex > 0){
            workerGroups[0].add(idle[idleIndex--]);
        }
        if(p.getNumFactories() == 0){
            System.out.println("There aren't any factories yet");
            MapLocation blueLoc = workerGroups[0].setBlueprint(UnitType.Factory);
            if(blueLoc != null && !hillChosen){
                short[][] hill = p.generateHill(blueLoc);
                workerGroups[0].changeToTargetMap(hill);
                p.factIndex++;
                hillChosen = true;
            }
            System.out.println("Could not start a factory this turn.");
        }

        for(int i = 0; i < groupIndex; i++){
            System.out.println("Workforce turn conducting");
            workerGroups[i].conductTurn();
        }

        /*
        for(int i = 0; i < groupIndex; i++){
            workerGroups[i].resetWorkerIndexCount();
        }*/
        idleIndex = 0;
    }

    public void createGroup() throws Exception{
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
