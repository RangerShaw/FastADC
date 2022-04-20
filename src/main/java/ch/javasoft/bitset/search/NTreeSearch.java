package ch.javasoft.bitset.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.LongBitSet;

public class NTreeSearch {

    private HashMap<Integer, NTreeSearch> subtrees = new HashMap<>();
    private LongBitSet bitset;

    /*
     * (non-Javadoc)
     *
     * @see ch.javasoft.bitset.search.ITreeSearch#add(ch.javasoft.bitset.LongBitSet)
     */

    public NTreeSearch() {

    }

    public NTreeSearch(Collection<LongBitSet> bitSets) {
        for (LongBitSet bitSet : bitSets)
            add(bitSet);
    }

    public boolean add(LongBitSet bs) {
        add(bs, 0);
        return true;
    }

    private void add(LongBitSet bs, int next) {
        int nextBit = bs.nextSetBit(next);
        if (nextBit < 0) {
            bitset = bs;
            return;
        }

        NTreeSearch nextTree = subtrees.get(nextBit);
        if (nextTree == null) {
            nextTree = new NTreeSearch();
            subtrees.put(nextBit, nextTree);
        }
        nextTree.add(bs, nextBit + 1);

//        NTreeSearch nextTree = new NTreeSearch();
//        NTreeSearch subTree = subtrees.putIfAbsent(nextBit, nextTree);
//        if (subTree == null)
//            nextTree.add(bs, nextBit + 1);
//        else
//            subTree.add(bs, nextBit + 1);
    }


    public Set<LongBitSet> getAndRemoveGeneralizations(LongBitSet invalidFD) {
        HashSet<LongBitSet> removed = new HashSet<>();
        getAndRemoveGeneralizations(invalidFD, 0, removed);
        return removed;
    }

    private boolean getAndRemoveGeneralizations(LongBitSet invalidFD, int next, Set<LongBitSet> removed) {
        if (bitset != null) {
            removed.add(bitset);
            bitset = null;
        }

        int nextBit = invalidFD.nextSetBit(next);
        while (nextBit >= 0) {
            NTreeSearch subTree = subtrees.get(nextBit);
            if (subTree != null)
                if (subTree.getAndRemoveGeneralizations(invalidFD, nextBit + 1, removed))
                    subtrees.remove(nextBit);
            nextBit = invalidFD.nextSetBit(nextBit + 1);
        }
        return subtrees.isEmpty();
    }

    public boolean isEmpty() {
        return bitset == null && subtrees.isEmpty();
    }

    public boolean containsSubset(LongBitSet add) {
        return getSubset(add, 0) != null;
    }

    public LongBitSet getSubset(LongBitSet add) {
        return getSubset(add, 0);
    }

    private LongBitSet getSubset(LongBitSet add, int next) {
        if (bitset != null)
            return bitset;

        int nextBit = add.nextSetBit(next);
        while (nextBit >= 0) {
            NTreeSearch subTree = subtrees.get(nextBit);
            if (subTree != null) {
                LongBitSet res = subTree.getSubset(add, nextBit + 1);
                if (res != null)
                    return res;
            }
            nextBit = add.nextSetBit(nextBit + 1);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * ch.javasoft.bitset.search.ITreeSearch#forEachSuperSet(ch.javasoft.bitset.
     * LongBitSet, java.util.function.Consumer)
     */

    public void forEachSuperSet(LongBitSet bitset, Consumer<LongBitSet> consumer) {
        forEachSuperSet(bitset, consumer, 0);
    }

    private void forEachSuperSet(LongBitSet bitset, Consumer<LongBitSet> consumer, int next) {
        int nextBit = bitset.nextSetBit(next);
        if (nextBit < 0)
            forEach(consumer);

        // for(int i = next; i <= nextBit; ++i) {
        for (Entry<Integer, NTreeSearch> entry : subtrees.entrySet()) {
            if (entry.getKey().intValue() > nextBit)
                continue;
            NTreeSearch subTree = entry.getValue();
            if (subTree != null)
                subTree.forEachSuperSet(bitset, consumer, entry.getKey().intValue() + 1);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see ch.javasoft.bitset.search.ITreeSearch#forEach(java.util.function.
     * Consumer)
     */

    public void forEach(Consumer<LongBitSet> consumer) {
        if (bitset != null)
            consumer.accept(bitset);
        for (NTreeSearch subtree : subtrees.values()) {
            subtree.forEach(consumer);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see ch.javasoft.bitset.search.ITreeSearch#remove(ch.javasoft.bitset.LongBitSet)
     */

    public void remove(LongBitSet remove) {
        remove(remove, 0);
    }

    private boolean remove(LongBitSet remove, int next) {
        int nextBit = remove.nextSetBit(next);
        if (nextBit < 0) {
            if (bitset.equals(remove))
                bitset = null;
        } else {
            NTreeSearch subTree = subtrees.get(Integer.valueOf(nextBit));
            if (subTree != null) {
                if (subTree.remove(remove, nextBit + 1))
                    subtrees.remove(Integer.valueOf(nextBit));
            }
        }
        return bitset == null && subtrees.size() == 0;
    }

}
