package FastADC.plishard;

import java.util.List;

public class PliShard {

    public final List<Pli> plis;
    public final int beg, end;  // tuple id range [beg, end)

    public PliShard(List<Pli> plis, int beg, int end) {
        this.plis = plis;
        this.beg = beg;
        this.end = end;

        for (Pli pli : plis)
            pli.pliShard = this;
    }
}
