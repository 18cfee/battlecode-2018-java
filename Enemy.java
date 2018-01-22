import bc.MapLocation;
import bc.Unit;
import bc.UnitType;

public class Enemy {
    public int id;
    public int hp;
    public BasicEnemyTypes type;
    public MapLoc loc;
    Enemy(BasicEnemyTypes type, int id, int hp, int x, int y){
        this.type = type;
        this.hp = hp;
        this.id = id;
        this.loc = new MapLoc(x,y);
    }
    Enemy(UnitType type, int id, int hp, MapLocation location){
        if(UnitType.Factory == type){
            this.type = BasicEnemyTypes.Factory;
        } else if(UnitType.Worker == type){
            this.type = BasicEnemyTypes.Worker;
        } else if (UnitType.Rocket == type) {
            this.type = BasicEnemyTypes.Rocket;
        } else {
            this.type = BasicEnemyTypes.Troop;
        }
        this.hp = hp;
        this.id = id;
        this.loc = new MapLoc(location);
    }
    Enemy(Unit enemy){
        UnitType type = enemy.unitType();
        if(UnitType.Factory == type){
            this.type = BasicEnemyTypes.Factory;
        } else if(UnitType.Worker == type){
            this.type = BasicEnemyTypes.Worker;
        } else if (UnitType.Rocket == type) {
            this.type = BasicEnemyTypes.Rocket;
        } else {
            this.type = BasicEnemyTypes.Troop;
        }
        this.hp = (int)enemy.health();
        this.id = enemy.id();
        loc = new MapLoc(enemy.location().mapLocation());
    }
    public MapLoc getMapLoc(){
        return loc;
    }
}

enum BasicEnemyTypes {
    Factory, Troop, Worker, Rocket;
}