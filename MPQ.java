import java.util.Map;

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
        MapLoc min = pq[1];
        exchange(1, n--);
        sink(1);
        pq[n+1] = null;
        return min;
    }

    private void swim(int val){
        while(val > 1 && greater(val/2, val)){
            exchange(val, val/2);
            val = val/2;
        }
    }

    private void sink(int val){
        while(2*val <= n){
            int i = 2*val;
            if(i < n && greater(i, i+1)){
                i++;
            }
            if(!greater(val, i)){
                break;
            }
            exchange(val, i);
            val = i;
        }
    }

    private boolean greater(int val1, int val2){
        return pq[val1].distanceToBase > pq[val2].distanceToBase;
    }

    private void exchange(int val1, int val2){
        MapLoc temp = pq[val1];
        pq[val1] = pq[val2];
        pq[val2] = temp;
    }
}
