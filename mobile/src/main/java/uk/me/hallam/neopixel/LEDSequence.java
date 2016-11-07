package uk.me.hallam.neopixel;

/**
 * Created by phallam on 07/11/16.
 */

public class LEDSequence {
    String sequence = null;
    boolean needColour = false;
    boolean needWait = false;
    boolean needIterations = false;

    public LEDSequence(String seq, boolean ncol, boolean nwt, boolean nit) {
        this.sequence = seq;
        this.needColour = ncol;
        this.needWait = nwt;
        this.needIterations = nit;
    }

    String getSequence() {
        return sequence;
    }

    boolean isNeedColour() {
        return needColour;
    }

    boolean isNeedWait() {
        return needWait;
    }

    boolean isNeedIterations() {
        return needIterations;
    }
}
