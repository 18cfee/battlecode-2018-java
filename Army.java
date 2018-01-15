import bc.GameController;

public class Army {
    GameController gc;
    Path p;
    Fighter carlsRangers;
    public Army(GameController gc, Path p){
        this.gc = gc;
        this.p = p;
        carlsRangers = new Fighter(gc,p);
    }
    public void conductTurn() throws Exception{
        carlsRangers.conductTurn();
    }
    public void addUnit(int id){
        carlsRangers.add(id);
    }
    public void addEnemyUnit(int id){
        carlsRangers.addEnemy(id);
    }
}
