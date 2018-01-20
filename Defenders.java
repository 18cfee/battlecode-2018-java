import bc.GameController;
import bc.MapLocation;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.HashSet;

public class Defenders extends Fighter {
    Defenders(GameController gc, Path p){
        super(gc,p);
    }
    public int rocket;
    private short[][] baseHill = null;
    private MapLocation base = null;
    private Wall wall;
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
                wall = new Wall(gc,p,baseHill,base);
            }
        }
        moveToTarget(wall.hillToTargetWall);
        if(wall.percentCoverageOfWall() > 90){
            wall.growTargetWall();
        }
        //else loadRocketIfPossible(rocket);

        shootAtSomething();
        numShooters = 0;
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

class Wall{
    private static final int maxWallSize = 5;
    private short[][] baseHill;
    public short targetNumFromBase;
    private ArrayDeque<MapLoc> curWall;
    public short[][] hillToTargetWall;
    private MapLocation hillCenterLoc;
    GameController gc;
    Path p;
    Wall(GameController gc, Path p, short[][] hill, MapLocation startingPoint){
        this.gc = gc;
        this.p = p;
        this.baseHill = hill;
        this.hillCenterLoc = startingPoint;
        curWall = new ArrayDeque<>();
        curWall.push(new MapLoc(startingPoint));
        targetNumFromBase = 1;
        hillToTargetWall = new short[p.planetWidth][p.planetHeight];
        System.out.println("about to grow");
        growTargetWall();
    }
    public void growTargetWall(){
        ArrayDeque<MapLoc> newWall = new ArrayDeque<MapLoc>();
        HashSet<MapLoc> alreadyChecked = new HashSet<>();
        targetNumFromBase++;
        while(!curWall.isEmpty()){
            MapLoc current = curWall.pop();
            int curX = current.x;
            int curY = current.y;
            for (int i = 0; i < 8; i++) {
                int x = curX + p.numsDirections[i][0];
                int y = curY + p.numsDirections[i][1];
                if(p.onMap(x,y) && baseHill[x][y] == targetNumFromBase){
                    MapLoc loc = new MapLoc(x,y);
                    if(!alreadyChecked.contains(loc)){
                        alreadyChecked.add(loc);
                        newWall.addLast(loc);
                    }
                }
            }
        }
        System.out.println("about to generate gradient");
        curWall = newWall;
        generateWallGradient(curWall);
    }
    public void generateWallGradient(ArrayDeque<MapLoc> input){
        ArrayDeque<MapLoc> toCheck = input.clone();
        BitSet[] checked = new BitSet[p.planetWidth];
        for (int i = 0; i < checked.length; i++) {
            BitSet cur = new BitSet(p.planetHeight);
            checked[i] = cur;
        }
        while(!toCheck.isEmpty()){
            MapLoc cur = toCheck.removeFirst();
            short dis;
            if(baseHill[cur.x][cur.y] == targetNumFromBase){
                dis = 0;
            } else {
                dis = (short)(hillToTargetWall[cur.x][cur.y] + 1);
            }
            for (int i = 0; i < 8; i++) {
                MapLoc newLoc = cur.add(p.numsDirections[i][0],p.numsDirections[i][1]);
                if(p.onMap(newLoc) && !checked[newLoc.x].get(newLoc.y)){
                    if(!p.passable(newLoc)){
                        //mark as unreachable
                        hillToTargetWall[newLoc.x][newLoc.y] = p.greatestPathNum;
                    } else {
                        toCheck.addLast(newLoc);
                        hillToTargetWall[newLoc.x][newLoc.y] = dis;
                    }
                    checked[newLoc.x].set(newLoc.y);
                }
            }
        }
        // todo smaller versions need to know if a path was found
    }
    public int percentCoverageOfWall(){
        if(targetNumFromBase > maxWallSize) return 0;
        ArrayDeque<MapLoc> cloneWall = curWall.clone();
        int size = 0;
        int covered = 0;
        while(!cloneWall.isEmpty()){
            MapLoc cur = cloneWall.removeFirst();
            size++;
            MapLocation loc = new MapLocation(p.planet,cur.x,cur.y);
            if(gc.hasUnitAtLocation(loc)){
                covered++;
            }
        }
        return covered*100/size;
    }
}
