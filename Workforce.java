import bc.GameController;

public class Workforce {
    GameController gc;
    Path p;
    int[] idle = new int[100];
    int idleIndex = 0;

    public Workforce(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
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
