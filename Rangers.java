import bc.Direction;
import bc.GameController;

public class Rangers {
    static final int MAX_ARMY_SIZE = 100;
    GameController gc;
    int [] ids = new int[MAX_ARMY_SIZE];
    int index = 0;
    Path p;
    Rangers(GameController gc, Path p){
        this.p = p;
        this.gc = gc;
    }
    public void add(int id){
        if(index != MAX_ARMY_SIZE){
            ids[index++] = id;
        }
    }
    void conductTurn(){
        for (int i = 0; i < index; i++) {
            int id = ids[i];
            Direction toMove = p.getRandDirection();
            if(gc.isMoveReady(id) && gc.canMove(id,toMove)){
                gc.moveRobot(id,toMove);
            }
        }
    }
}
