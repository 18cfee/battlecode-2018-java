import bc.MapLocation;
import bc.Unit;
import bc.UnitType;

public class Enemy {
    public int id;
    public int hp;
    public BasicEnemyTypes type;
    public int x;
    public int y;
    Enemy(BasicEnemyTypes type, int id, int hp, int x, int y){
        this.type = type;
        this.hp = hp;
        this.id = id;
        this.x = x;
        this.y = y;
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
        this.x = location.getX();
        this.y = location.getY();
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
        MapLocation location = enemy.location().mapLocation();
        this.x = location.getX();
        this.y = location.getY();
    }
}

enum BasicEnemyTypes {
    Factory, Troop, Worker, Rocket;
}