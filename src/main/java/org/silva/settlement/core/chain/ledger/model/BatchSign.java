package org.silva.settlement.core.chain.ledger.model;

import org.silva.settlement.infrastructure.bytes.ByteArrayWrapper;
import org.silva.settlement.infrastructure.bytes.ByteUtil;
import org.silva.settlement.infrastructure.rlp.RLP;
import org.silva.settlement.infrastructure.rlp.RLPList;
import org.silva.settlement.ethereum.model.settlement.Signature;
import org.silva.settlement.infrastructure.datasource.model.Persistable;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

/**
 * description:
 * @author carrot
 */
public class BatchSign extends Persistable {

    private long epoch;

    private long number;

    private byte[] hash;

    private Map<ByteArrayWrapper, Signature> signatures;

    public BatchSign(byte[] rawData) {
        super(rawData);
    }

    public static BatchSign buildWithoutDecode(byte[] rawData) {
        var blockSign = new BatchSign(null);
        blockSign.rlpEncoded = rawData;
        return blockSign;
    }

    public BatchSign(long epoch, long number, byte[] hash, Map<ByteArrayWrapper, Signature> signatures) {
        super(null);
        this.epoch = epoch;
        this.number = number;
        this.hash = hash;
        this.signatures = signatures == null? new TreeMap<>(): new TreeMap<>(signatures);
        this.rlpEncoded = rlpEncoded();
    }

    public long getEpoch() {
        return epoch;
    }

    public long getNumber() {
        return number;
    }

    public Map<ByteArrayWrapper, Signature> getSignatures() {
        return signatures;
    }

    public byte[] getHash() {
        return hash;
    }

    @Override
    protected byte[] rlpEncoded() {
        byte[][] encode = new byte[3 + signatures.size()][];

        encode[0] = RLP.encodeBigInteger(BigInteger.valueOf(this.epoch));
        encode[1] = RLP.encodeBigInteger(BigInteger.valueOf(this.number));
        encode[2] = RLP.encodeElement(this.hash);
        int i = 3;
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
        RLPList blockSignRLP = (RLPList) params.get(0);
        this.epoch = ByteUtil.byteArrayToLong(blockSignRLP.get(0).getRLPData());
        this.number = ByteUtil.byteArrayToLong(blockSignRLP.get(1).getRLPData());
        this.hash = blockSignRLP.get(2).getRLPData();

        TreeMap<ByteArrayWrapper, Signature> signatures = new TreeMap<>();
        for (int i = 3; i < blockSignRLP.size(); i++) {
            RLPList kvBytes = (RLPList) RLP.decode2(blockSignRLP.get(i).getRLPData()).get(0);
            signatures.put(new ByteArrayWrapper(kvBytes.get(0).getRLPData()), new Signature(kvBytes.get(1).getRLPData()));
        }
        this.signatures = signatures;
    }

    @Override
    public String toString() {
        return "BlockSign{" +
                "epoch=" + epoch +
                ", number=" + number +
                ", hash=" + Hex.toHexString(hash) +
                ", signatures=" + signatures +
                '}';
    }
}
