package org.silva.settlement.core.chain.ledger.model.event.cross;

import org.silva.settlement.infrastructure.rlp.RLP;
import org.silva.settlement.core.chain.ledger.model.event.GlobalEventCommand;
import org.silva.settlement.core.chain.ledger.model.event.ca.CandidateStateSnapshot;

import java.util.Objects;

/**
 * description:
 * @author carrot
 */
public class CrossMainChainStateSnapshot extends CandidateStateSnapshot {

    int placeHolder;

    public CrossMainChainStateSnapshot() {
        super(null);
        this.placeHolder = 1;
        this.rlpEncoded = rlpEncoded();
    }

    @Override
    public GlobalEventCommand getCurrentCommand() {
        return GlobalEventCommand.CROSS_MAIN_CHAIN_EVENT;
    }

    @Override
    protected byte[] rlpEncoded() {
        return RLP.encodeInt(1);
    }

    @Override
    protected void rlpDecoded() {
        this.placeHolder = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrossMainChainStateSnapshot that = (CrossMainChainStateSnapshot) o;
        return placeHolder == that.placeHolder;
    }

    @Override
    public int hashCode() {

        return Objects.hash(placeHolder);
    }
}
