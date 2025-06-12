package org.silva.settlement.core.chain.consensus.sequence.safety;

import org.silva.settlement.core.chain.consensus.sequence.model.LedgerInfo;
import org.silva.settlement.core.chain.consensus.sequence.model.LedgerInfoWithSignatures;
import org.silva.settlement.infrastructure.anyhow.ProcessResult;

/**
 * description:
 * @author carrot
 */
public interface Verifier {

    ProcessResult<Void> verify(LedgerInfoWithSignatures ledgerInfo);

    boolean epochChangeVerificationRequired(long epoch);

    boolean isLedgerInfoStale(LedgerInfo ledgerInfo);

}
