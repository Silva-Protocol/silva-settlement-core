package org.silva.settlement.core.chain.consensus.slowpath;

import org.silva.settlement.core.chain.consensus.sequence.liveness.EpochState;
import org.silva.settlement.core.chain.ledger.model.SettlementBatch;

/**
 * description:
 * @author carrot
 */
public interface SettlementBatchFinality {

    boolean onChain(EpochState signEpoch, SettlementBatch settlementBatch);
}
