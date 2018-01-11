import bc.Direction;
import bc.GameController;

public class Rangers {
    static final int MAX_ARMY_SIZE = 100;
    GameController gc;
    int [] ids = new int[MAX_ARMY_SIZE];
    int index = 0;
    Path p;
    CStates state;
    Rangers(GameController gc, Path p){
        this.p = p;
        this.gc = gc;
        state = CStates.RandomMove;
    }
    public void add(int id){
        if(index != MAX_ARMY_SIZE){
            ids[index++] = id;
        }
    }
    void conductTurn(){
        if(state == CStates.RandomMove){
            roamRandom();
        }
        index = 0;
    }
    private void roamRandom(){
        for (int i = 0; i < index; i++) {
            int id = ids[i];
            Direction toMove = p.getRandDirection();
            if(gc.isMoveReady(id) && gc.canMove(id,toMove)){
                gc.moveRobot(id,toMove);
            }
        }
    }
    boolean shouldContinueRoamingRandomly(){
        return gc.round() < 100;
    }
}

enum CStates {
    RandomMove, Not;
}