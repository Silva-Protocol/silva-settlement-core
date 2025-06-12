package org.silva.settlement.core.chain.consensus.slowpath.impl;

import org.silva.settlement.core.chain.consensus.sequence.liveness.EpochState;
import org.silva.settlement.core.chain.consensus.slowpath.SettlementBatchFinality;
import org.silva.settlement.core.chain.ledger.model.SettlementBatch;

/**
 * description:
 * @author carrot
 */
public class SettlementBatchFinalityMock implements SettlementBatchFinality {

    @Override
    public boolean onChain(EpochState signEpoch, SettlementBatch settlementBatch) {
        return true;
    }
}
