package org.silva.settlement.core.chain.sync;

import org.silva.settlement.core.chain.ledger.model.SettlementBatch;

public class SettlementChainNetSender {

    public OnChainStatus send(int chain, SettlementBatch settlementBatch) {
        return OnChainStatus.SUCCESS;
    }

}
