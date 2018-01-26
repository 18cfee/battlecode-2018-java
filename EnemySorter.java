import java.util.Comparator;

public class EnemySorter implements Comparator<Enemy>{
    @Override
    public int compare(Enemy a, Enemy b){
        if(a.type == b.type){
            return a.hp - b.hp;
        } else if(a.type == BasicEnemyTypes.Rocket){
            return -1;
        } else if(b.type == BasicEnemyTypes.Rocket){
            return 1;
        } else if(a.type == BasicEnemyTypes.Troop){
            return -1;
        } else if(b.type == BasicEnemyTypes.Troop){
            return 1;
        } else if(a.type == BasicEnemyTypes.Factory){
            return -1;
        } else if(b.type == BasicEnemyTypes.Factory){
            return 1;
        } else{
            return 0;
        }
    }
}
