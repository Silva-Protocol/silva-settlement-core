package org.silva.settlement.core.chain.consensus.sequence.model;

import org.silva.settlement.infrastructure.anyhow.ProcessResult;
import org.silva.settlement.infrastructure.bytes.ByteArrayWrapper;
import org.silva.settlement.infrastructure.rlp.RLP;
import org.silva.settlement.infrastructure.rlp.RLPList;
import org.silva.settlement.ethereum.model.settlement.Signature;
import org.silva.settlement.infrastructure.datasource.model.Persistable;

import java.util.Map;
import java.util.TreeMap;

/**
 * description:
 * @author carrot
 */
public class LedgerInfoWithSignatures extends Persistable {

    private LedgerInfo ledgerInfo;

    //account address to sig
    private TreeMap<ByteArrayWrapper, Signature> signatures;

    private LedgerInfoWithSignatures() {
        super(null);
    }

    public LedgerInfoWithSignatures(byte[] encode) {
        super(encode);
    }

    public static  LedgerInfoWithSignatures build(LedgerInfo ledgerInfo, TreeMap<ByteArrayWrapper, Signature> signatures) {
        LedgerInfoWithSignatures ledgerInfoWithSignatures = new LedgerInfoWithSignatures();
        ledgerInfoWithSignatures.ledgerInfo = ledgerInfo;
        ledgerInfoWithSignatures.signatures = signatures;
        ledgerInfoWithSignatures.rlpEncoded = ledgerInfoWithSignatures.rlpEncoded();
        return ledgerInfoWithSignatures;
    }

    public LedgerInfo getLedgerInfo() {
        return ledgerInfo;
    }

    public TreeMap<ByteArrayWrapper, Signature> getSignatures() {
        return signatures;
    }

    public void addSignature(byte[] author, Signature signature) {
        ByteArrayWrapper authorKey = new ByteArrayWrapper(author);
        if (!this.signatures.containsKey(authorKey)) {
            this.signatures.put(authorKey, signature);
        }
    }

    public void removeSignature(byte[] author) {
        this.signatures.remove(new ByteArrayWrapper(author));
    }

    public ProcessResult<Void> verifySignatures(ValidatorVerifier validator) {
        byte[] ledgerHash = this.ledgerInfo.getHash();
        return validator.batchVerifyAggregatedSignature(ledgerHash, this.signatures);
    }

    public void reEncode() {
        this.rlpEncoded = rlpEncoded();
    }

    @Override
    protected byte[] rlpEncoded() {
        byte[][] encode = new byte[signatures.size() + 1][];
        encode[0] = this.ledgerInfo.getEncoded();
        int i = 1;
        for (Map.Entry<ByteArrayWrapper, Signature> entry: signatures.entrySet()) {
            encode[i] =
                    RLP.encodeList(
                            RLP.encodeElement(entry.getKey().getData()),
                            RLP.encodeElement(entry.getValue().getSig())
                    );
            i++;
        }
        return RLP.encodeList(encode);
    }

    @Override
    protected void rlpDecoded() {
        RLPList params = RLP.decode2(rlpEncoded);
        RLPList liSign = (RLPList) params.get(0);
        this.ledgerInfo = new LedgerInfo(liSign.get(0).getRLPData());

        TreeMap<ByteArrayWrapper, Signature> signatures = new TreeMap<>();
        for (int i = 1; i < liSign.size(); i++) {
            RLPList kvBytes = (RLPList) RLP.decode2(liSign.get(i).getRLPData()).get(0);
            signatures.put(new ByteArrayWrapper(kvBytes.get(0).getRLPData()), new Signature(kvBytes.get(1).getRLPData()));
        }
        this.signatures = signatures;
    }

    @Override
    public String toString() {
        return
                "{" + ledgerInfo +
                ", signatures size =" + signatures.size() +
                '}';
    }

//    public void clear() {
//        this.ledgerInfo.clear();
//        this.ledgerInfo = null;
//        this.signatures.clear();
//    }
}
