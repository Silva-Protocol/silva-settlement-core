package org.silva.settlement.core.chain.ledger.model.event.ca;

import org.silva.settlement.infrastructure.datasource.model.Persistable;

/**
 * description:
 * @author carrot
 */
public class CaContractStateValue extends Persistable {

    public CaContractStateValue(byte[] encode) {
        super(encode);
    }

    @Override
    protected byte[] rlpEncoded() {
        return this.rlpEncoded;
    }

    @Override
    protected void rlpDecoded() {
        // do noting
    }
}
