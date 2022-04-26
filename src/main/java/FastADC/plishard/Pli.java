package FastADC.plishard;

import java.util.*;

public class Pli {

    public PliShard pliShard;   // the PliShard that this PLI belongs to

    public int[] keys;
    List<Cluster> clusters;
    Map<Integer, Integer> keyToClusterIdMap;

    public Pli(List<Cluster> rawClusters, int[] keys, Map<Integer, Integer> translator) {
        this.clusters = rawClusters;
        this.keys = keys;
        this.keyToClusterIdMap = translator;
    }


    public int size() {
        return keys.length;
    }

    public int[] getKeys() {
        return keys;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public Cluster getClusterByKey(Integer key) {
        Integer clusterId = keyToClusterIdMap.get(key);
        return clusterId != null ? clusters.get(clusterId) : null;
    }

    public Integer getClusterIdByKey(Integer key) {
        return keyToClusterIdMap.get(key);
    }

    public Cluster get(int i) {
        return clusters.get(i);
    }

    // less than or equal to target
    public int getFirstIndexWhereKeyIsLTE(int target) {
        return getFirstIndexWhereKeyIsLTE(target, 0);
    }

    public int getFirstIndexWhereKeyIsLTE(int target, int l) {
        Integer i = keyToClusterIdMap.get(target);
        if (i != null) return i;

        int r = keys.length;
        while (l < r) {
            int m = l + ((r - l) >>> 1);
            if (keys[m] <= target) r = m;
            else l = m + 1;
        }

        return l;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clusters.size(); i++)
            sb.append(keys[i] + ": " + clusters.get(i) + "\n");

        sb.append(Arrays.toString(keys) + "\n");
        sb.append(keyToClusterIdMap + "\n");

        return sb.toString();
    }

}
