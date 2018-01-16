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
    public void conductTurn() throws Exception{
        if(groupIndex == 0){
            createGroup();
        }
        while(idleIndex > 0){
            workerGroups[0].add(idle[idleIndex-1]);
            idleIndex--;
        }
        System.out.println("built: " + p.builtFactIndex);
        System.out.println("unbuilt: " + p.getNumFactories());
        if(p.getNumFactories() == p.builtFactIndex && p.getNumFactories() < p.NUM_FACTORIES_WANTED){
            System.out.println("There aren't any factories yet");
            MapLocation blueLoc = workerGroups[0].setBlueprint();
            if(blueLoc != null ){
                for (int i = 0; i < 200; i++) {
                    System.out.println("attention");
                }
                short[][] hill = p.generateHill(blueLoc);
                workerGroups[0].changeToTargetMap(hill);
                p.unbuiltFactIndex++;
                System.out.println("num fact: " + p.getNumFactories());
            }
        }

        for(int i = 0; i < groupIndex; i++){
            workerGroups[i].conductTurn();
        }

        idleIndex = 0;
    }

    public void createGroup() throws Exception{
        workerGroups[groupIndex] = new Workers(gc, p);
        groupIndex++;
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
