import bc.GameController;

public class Army {
    GameController gc;
    Path p;
    Fighter carlsRangers;
    int size = 0;

    public Army(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
        carlsRangers = new RangersTargetNextUnit(gc,p);
    }
    public void conductTurn() throws Exception{
        carlsRangers.conductTurn();
        resetSize();
    }
    public void addUnit(int id) throws Exception{
        carlsRangers.add(id);
        size++;
    }
    public void addEnemyUnit(int id){
        carlsRangers.addEnemy(id);
    }

    public int getArmySize(){
        return size;
    }
    public void resetSize(){
        size = 0;
    }
}
