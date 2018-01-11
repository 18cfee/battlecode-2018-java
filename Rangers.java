import bc.GameController;

public class Rangers {
    static final int MAX_ARMY_SIZE = 100;
    GameController gc;
    int [] ids = new int[MAX_ARMY_SIZE];
    int index = 0;
    Rangers(GameController gc){
        this.gc = gc;
    }
    public void add(int id){
        if(index != MAX_ARMY_SIZE){
            ids[index++] = id;
        }
    }
    void conductTurn(){

    }
}
