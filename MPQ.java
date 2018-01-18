import java.util.Map;
import java.util.NoSuchElementException;

public class MPQ {

    MapLoc[] pq;
    int n = 0;

    public MPQ(int size){
        pq = new MapLoc[size];
    }

    public void insert(MapLoc x){
        pq[++n] = x;
        swim(n);
    }

    public MapLoc pop(){
        if(isEmpty()) throw new NoSuchElementException("Out of locations to harvest");
        MapLoc min = pq[1];
        exchange(1, n--);
        sink(1);
        pq[n+1] = null;
        return min;
    }

    private void swim(int i){
        while(i > 1 && greater(i/2, i)){
            exchange(i, i/2);
            i = i/2;
        }
    }

    public MapLoc peek(){
        return pq[1];
    }

    private void sink(int i){
        while(2*i <= n){
            int j = 2*i;
            if(j < n && greater(j, j+1)){
                j++;
            }
            if(!greater(i, j)){
                break;
            }
            exchange(i, j);
            i = j;
        }
    }

    private boolean greater(int i, int j){
        return pq[i].distanceToBase > pq[j].distanceToBase;
    }

    private void exchange(int i, int j){
        MapLoc temp = pq[i];
        pq[i] = pq[j];
        pq[j] = temp;
    }

    public boolean isEmpty(){
        return n == 0;
    }

    public int getSize(){
        return n;
    }
}
