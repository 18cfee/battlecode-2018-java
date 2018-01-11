import bc.Direction;
import bc.GameController;

public class Workers {
    int [] ids = new int[100];
    int factWorkOn = -1;
    int index;
    GameController gc;
    public WorkerStates state;
    public Workers(GameController gc){
        this.gc = gc;
    }
    public void addWorker(int id){
        ids[index] = id;
        index++;
    }
    public void contReplicating(){
        //todo, get this from path and also make it an available direction
        Direction[] d = Direction.values();
        int rand = (int)(Math.random()*9);
        Direction random = d[rand];
        for (int i = 0; i < index; i++) {
            if(gc.canMove(ids[i],random)){
                gc.canMove(ids[i],random);
            }
        }
        for (int i = 0; i < index; i++) {
            if(gc.canReplicate(ids[i],random)){
                gc.replicate(ids[i],random);
            }
        }
    }
    public boolean doneReplicating(){
        return(index > 10);
    }
    void setState(WorkerStates state){
        this.state = state;
    }
    void resetWorkerIndexCount(){
        index = 0;
    }
    void contBuildingFactory(){
    }
    boolean doneBuildingFactory(){
        return false;
    }
    void contBuildingRocket(){
        //// TODO
    }
    boolean doneBuildingRocket(){
        return false;
        //todo
    }
}