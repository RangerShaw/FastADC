package FastADC.evidence;

import FastADC.evidence.clue.CrossClueSetBuilder;
import FastADC.evidence.clue.ClueSetBuilder;
import FastADC.evidence.clue.SingleClueSetBuilder;
import FastADC.evidence.evidenceSet.EvidenceSet;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import FastADC.plishard.PliShard;
import FastADC.predicate.PredicateBuilder;
import com.koloboke.function.LongLongConsumer;

import java.util.concurrent.CountedCompleter;


public class EvidenceSetBuilder {

    private final EvidenceSet fullEvidenceSet;

    public EvidenceSetBuilder(PredicateBuilder predicateBuilder) {
        ClueSetBuilder.configure(predicateBuilder);
        fullEvidenceSet = new EvidenceSet(predicateBuilder, ClueSetBuilder.getCorrectionMap());
    }

    public EvidenceSet buildEvidenceSet(PliShard[] pliShards, boolean linear) {
        if (pliShards.length != 0) {
            HashLongLongMap clueSet = linear ? linearBuildClueSet(pliShards) : buildClueSet(pliShards);
            fullEvidenceSet.build(clueSet);
        }
        return fullEvidenceSet;
    }

    private HashLongLongMap linearBuildClueSet(PliShard[] pliShards) {
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);

        HashLongLongMap clueSet = HashLongLongMaps.newMutableMap();
        LongLongConsumer add = (k, v) -> clueSet.addValue(k, v, 0L);

        for (int i = 0; i < pliShards.length; i++) {
            for (int j = i; j < pliShards.length; j++) {
                ClueSetBuilder builder = i == j ? new SingleClueSetBuilder(pliShards[i]) : new CrossClueSetBuilder(pliShards[i], pliShards[j]);
                HashLongLongMap partialClueSet = builder.buildClueSet();
                partialClueSet.forEach(add);
            }
        }
        return clueSet;
    }

    private HashLongLongMap buildClueSet(PliShard[] pliShards) {
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);

        ClueSetTask rootTask = new ClueSetTask(null, pliShards, 0, taskCount);
        return rootTask.invoke();
    }

    public EvidenceSet getEvidenceSet() {
        return fullEvidenceSet;
    }

}


class ClueSetTask extends CountedCompleter<HashLongLongMap> {

    private static int[] searchIndexes;

    private static void buildSearchIndex(int count) {
        if (searchIndexes == null || searchIndexes[searchIndexes.length - 1] < count) {
            int n = (int) Math.sqrt(2 * count + 2) + 3;
            searchIndexes = new int[n];
            for (int i = 1; i < n; i++)
                searchIndexes[i] = searchIndexes[i - 1] + i + 1;
        }
    }

    final int taskBeg, taskEnd;
    PliShard[] pliShards;

    ClueSetTask sibling;
    HashLongLongMap partialClueSet;

    public ClueSetTask(ClueSetTask parent, PliShard[] _pliShards, int _beg, int _end) {
        super(parent);
        pliShards = _pliShards;
        taskBeg = _beg;
        taskEnd = _end;
        buildSearchIndex(taskEnd);
    }

    @Override
    public void compute() {
        if (taskEnd - taskBeg >= 2) {
            int mid = (taskBeg + taskEnd) >>> 1;
            ClueSetTask left = new ClueSetTask(this, pliShards, taskBeg, mid);
            ClueSetTask right = new ClueSetTask(this, pliShards, mid, taskEnd);
            left.sibling = right;
            right.sibling = left;

            setPendingCount(1);
            right.fork();

            left.compute();
        } else {
            if (taskEnd > taskBeg) {
                ClueSetBuilder builder = getClueSetBuilder(taskBeg);
                partialClueSet = builder.buildClueSet();
            }

            tryComplete();
        }
    }

    private ClueSetBuilder getClueSetBuilder(int taskID) {
        // taskID = i*(i+1)/2 + j
        int i = lowerBound(searchIndexes, taskID);
        int j = i - (searchIndexes[i] - taskID);

        return i == j ? new SingleClueSetBuilder(pliShards[i]) : new CrossClueSetBuilder(pliShards[i], pliShards[j]);
    }

    // return the index of the first num that's >= target, or nums.length if no such num
    private int lowerBound(int[] nums, int target) {
        int l = 0, r = nums.length;
        while (l < r) {
            int m = l + ((r - l) >>> 1);
            if (nums[m] >= target) r = m;
            else l = m + 1;
        }
        return l;
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (caller != this) {
            ClueSetTask child = (ClueSetTask) caller;
            ClueSetTask childSibling = child.sibling;

            partialClueSet = child.partialClueSet;
            if (childSibling != null && childSibling.partialClueSet != null) {
                for (var e : childSibling.partialClueSet.entrySet())
                    partialClueSet.addValue(e.getKey(), e.getValue(), 0L);
            }
        }
    }

    @Override
    public HashLongLongMap getRawResult() {
        return partialClueSet == null ? HashLongLongMaps.newMutableMap() : partialClueSet;
    }
}


