package de.metanome.algorithms.dcfinder.helpers;

import java.util.*;
import java.util.stream.Stream;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

public class IndexProvider<T> {

    private int nextIndex = 0;
    private final List<T> objects = new ArrayList<>();
    private final Map<T, Integer> indexes = new HashMap<>();

    public int[] updateMap;     // old index -> new index, after sort


    public Integer getIndex(T object) {
        Integer index = indexes.putIfAbsent(object, nextIndex);
        if (index == null) {
            index = nextIndex;
            ++nextIndex;
            objects.add(object);
        }
        return index;
    }

    public Set<Map.Entry<T, Integer>> entrySet() {
        return indexes.entrySet();
    }

    public void addAll(Collection<T> objects) {
        for (T object : objects)
            getIndex(object);
    }

    public void clear() {
        nextIndex = 0;
        objects.clear();
        indexes.clear();
        updateMap = null;
    }

    public T getObject(int index) {
        return objects.get(index);
    }

    public List<T> getObjects() {
        return objects;
    }

    public int[] getUpdateMap() {
        return updateMap;
    }

    public static <A extends Comparable<A>> int[] sort(IndexProvider<A> r) {
        Collections.sort(r.objects);
        r.updateMap = new int[r.size()];

        for (int i = 0; i < r.objects.size(); i++) {
            A object = r.objects.get(i);
            r.updateMap[r.indexes.get(object)] = i;
            r.indexes.put(object, i);
        }

        return r.updateMap;
    }

    public int size() {
        return nextIndex;
    }

}
