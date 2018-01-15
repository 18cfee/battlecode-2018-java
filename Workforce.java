import bc.GameController;
import bc.MapLocation;

public class Workforce{
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
        while(idleIndex > 0){
            workerGroups[0].add(idle[idleIndex-1]);
            idleIndex--;
        }
        if(p.getNumFactories() == 0){
            System.out.println("There aren't any factories yet");
            MapLocation blueLoc = workerGroups[0].setBlueprint();
            short[][] hill = null;
            if(blueLoc != null && hill == null){
                hill = p.generateHill(blueLoc);
                workerGroups[0].changeToTargetMap(hill);
            }
            System.out.println("Could not start a factory this turn.");
        }

        for(int i = 0; i < groupIndex; i++){
            System.out.println("Workforce turn conducting");
            workerGroups[i].conductTurn();
        }

    }

    public void createGroup(){
        workerGroups[groupIndex] = new Workers(gc, p);
        groupIndex++;
    }

    public void addWorker(int id){
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
        }
    }
}
