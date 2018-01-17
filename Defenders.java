import bc.Direction;
import bc.GameController;
import bc.MapLocation;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Stack;

public class Defenders extends Fighter {
    Defenders(GameController gc, Path p){
        super(gc,p);
    }
    public int rocket;
    private short[][] baseHill = null;
    private MapLocation base = null;
    @Override
    public void conductTurn() throws Exception{
        System.out.println("it is making it into defenders");
        if(base == null){
            //not ready to go
            if(p.baseLoc == null){
                return;
            } else {
                base = p.baseLoc;
                baseHill = p.generateHill(base);
            }
        }
        moveToTarget(baseHill);
        //else loadRocketIfPossible(rocket);
        shootAtSomething();
        indexShooters = 0;
        indexEnemy = 0;
        movableIndex = 0;
        index = 0;
    }
    public void loadRocketIfPossible(int rocketId){
        if(rocketId == -1 ) return;
        System.out.println("gathering around rocket");
        baseHill = p.firstRocketLocHill;
        for (int i = 0; i < movableIndex; i++) {
            if (gc.canLoad(rocketId,moveAbles[i])){
                gc.load(rocketId,moveAbles[i]);
            }
        }
        index = 0;
    }
}

//class Wall{
//    private short[][] baseHill;
//    public int targetNumFromBase;
//    private Stack<MapLoc> curWall;
//    private short[][] hillToTargetWall;
//    private MapLocation hillCenterLoc;
//    GameController gc;
//    Path p;
//    Wall(GameController gc, Path p, short[][] hill, MapLocation startingPoint){
//        this.gc = gc;
//        this.p = p;
//        this.baseHill = hill;
//        this.hillCenterLoc = startingPoint;
//        curWall = new Stack<MapLoc>();
//        curWall.push(new MapLoc(startingPoint));
//        targetNumFromBase = 1;
//        hillToTargetWall = new short[p.planetWidth][p.planetHeight];
//        growTargetWall();
//    }
//    private void growTargetWall(){
//        ArrayDeque<MapLoc> toCheck = new ArrayDeque<MapLocation>();
//        HashSet<MapLoc> alreadyChecked = new HashSet<>();
//        targetNumFromBase++;
//        while(!curWall.empty()){
//            MapLoc current = curWall.pop();
//            int curX = current.x;
//            int curY = current.y;
//            for (int i = 0; i < 8; i++) {
//                int x = curX + p.numsDirections[i][0];
//                int y = curY + p.numsDirections[i][1];
//                if(p.onMap(x,y) && baseHill[x][y] == targetNumFromBase){
//                    MapLoc loc = new MapLoc(x,y);
//                    if(!alreadyChecked.contains(loc)){
//                        alreadyChecked.add(loc);
//                        newWall.push(loc);
//                    }
//                }
//            }
//        }
//        curWall = newWall;
//    }
//    public short[][] generateWallGradient(MapLocation destination){
//        hillToTargetWall[destination.getX()][destination.getY()] = 1;
//        ArrayDeque<MapLoc> toCheck = new ArrayDeque<MapLocation>();
//        toCheck.addLast(destination);
//        while(!toCheck.isEmpty()){
//            MapLocation cur = toCheck.removeFirst();
//            short dis = hillToTargetWall[cur.getX()][cur.getY()];
//            for(Direction d : directions){
//                MapLocation newLoc = cur.add(d);
//                if(previouslyUncheckedMapLoc(newLoc,hillToTargetWall)){
//                    if(map.isPassableTerrainAt(newLoc) != 1){
//                        //mark as unreachable
//                        hillToTargetWall[newLoc.getX()][newLoc.getY()] = greatestPathNum;
//                    } else {
//                        toCheck.addLast(newLoc);
//                        hillToTargetWall[newLoc.getX()][newLoc.getY()] = (short)(dis + 1);
//                    }
//                }
//            }
//        }
//        // todo smaller versions need to know if a path was found
//        return hillToTargetWall;
//    }
//}
