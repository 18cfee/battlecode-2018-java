import bc.GameController;
import bc.MapLocation;

public class AggresiveRangers extends Fighter{
    AggresiveRangers(GameController gc, Path p){
        super(gc,p);
    }
    private short[][] baseHill = null;
    private MapLocation base = null;
    private short[][] baseHillDef = null;
    private MapLocation baseDef = null;
    private boolean seesEnemy = false;
    private short groupTargetCooldown = 0;
    @Override
    public void conductTurn() throws Exception{
        if(noUnits())return;
        System.out.println("aggressive rangers " + size());
        if(!noEnemies() && seesEnemy == false && groupTargetCooldown == 0){
            seesEnemy = true;
            Enemy enemy = enemies.get(0);
            if(enemy.hp > 0){
                MapLocation a = gc.unit(enemy.id).location().mapLocation();
                base = a;
                baseHill = p.generateHill(base);
                groupTargetCooldown+= 25;
            }
        } else if (seesEnemy = true && enemies.size() == 0){
            seesEnemy = false;
        }
        if(base!= null) moveToTarget(baseHill);
        else roamRandom();
        shootOptimally();
        if(groupTargetCooldown > 0) groupTargetCooldown--;
    }
}
