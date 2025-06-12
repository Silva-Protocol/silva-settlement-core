package org.silva.settlement.core.chain.ledger.model.event.ca;

import org.silva.settlement.infrastructure.rlp.RLP;
import org.silva.settlement.core.chain.ledger.model.event.GlobalEventCommand;

/**
 * description:
 * @author carrot
 */
public class OperationsStaffStateSnapshot extends CandidateStateSnapshot {

    int placeHolder;

    public OperationsStaffStateSnapshot() {
        super(null);
        this.placeHolder = 1;
        this.rlpEncoded = rlpEncoded();
    }

    @Override
    public GlobalEventCommand getCurrentCommand() {
        return GlobalEventCommand.PROCESS_OPERATIONS_STAFF;
    }

    @Override
    protected byte[] rlpEncoded() {
        return RLP.encodeInt(1);
    }

    @Override
    protected void rlpDecoded() {
        this.placeHolder = 1;
    }
}
