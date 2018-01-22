import bc.MapLocation;
import bc.Planet;

import java.util.BitSet;

public class Debug {

    public static void printCoords(MapLocation a){
        System.out.println(a.getPlanet() +"X: " + a.getX() + " Y: " + a.getY());
    }
    public static void printHill(short[][]hill){
        System.out.println("hill");
        for (int i = hill.length - 1; i >= 0; i--) {
            for (int j = 0; j < hill.length; j++) {
                System.out.print(hill[j][i] + " ");
            }
            System.out.println();
        }
    }

    public static void attention(){
        for (int i = 0; i < 100; i++) {
            System.out.println("GEt my attention bit ttiiiiiiiiiiiiiiiiiiiiiiiiiiime");
        }
    }
    public static void passable(BitSet[] set){
        for (int i = 0; i < set.length; i++) {
            BitSet curSet = set[i];
            for (int j = 0; j < curSet.length(); j++) {
                if(curSet.get(j)){
                    System.out.print(1 + " ");
                } else {
                    System.out.print(0 + " ");
                }

            }
            System.out.println();
        }
    }
//    long totalCarbs = 0;
//        for (int i = 0; i < planetHeight; i++) {
//        for (int j = 0; j < planetHeight; j++) {
//            MapLocation centerLoc = new MapLocation(Planet.Earth,i,j);
//            totalCarbs += earth.initialKarboniteAt(centerLoc);
//        }
//    }
}
