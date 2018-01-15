import bc.GameController;
import bc.MapLocation;

public class Workforce {
    GameController gc;
    Path p;
    int[] idle = new int[100];
    int idleIndex = 0;
    Workers[] workerGroups = new Workers[10];
    int groupIndex = 0;

    public Workforce(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
    }

    public void conductTurn(){
        if(groupIndex == 0){
            createGroup();
        }
        while(idleIndex < 0){
            workerGroups[0].add(idleIndex-1);
        }
        if(p.getNumFactories() == 0){
            MapLocation blueLoc = workerGroups[0].setBlueprint();
            if(blueLoc != null){
                workerGroups[0].changeToTargetDestinationState(blueLoc);
            }
        }

    }

    public void createGroup(){
        workerGroups[groupIndex] = new Workers(gc, p);
        groupIndex++;
    }
    public void addWorker(int id){
        boolean present = false;
        for(int i = 0; i < idleIndex; i++){
            if(idle[i] == id){
                present = true;
                break;
            }
        }
        if(!present){
            idle[idleIndex] = id;
            idleIndex++;
        }
    }
}
