import bc.MapLocation;
import bc.Planet;

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
//    long totalCarbs = 0;
//        for (int i = 0; i < planetSize; i++) {
//        for (int j = 0; j < planetSize; j++) {
//            MapLocation loc = new MapLocation(Planet.Earth,i,j);
//            totalCarbs += earth.initialKarboniteAt(loc);
//        }
//    }
}
