package org.silva.settlement.core.chain.ledger.model.event.ca;

import org.silva.settlement.infrastructure.bytes.ByteUtil;
import org.silva.settlement.infrastructure.rlp.RLP;
import org.silva.settlement.infrastructure.rlp.RLPList;
import org.silva.settlement.core.chain.ledger.model.RLPModel;
import org.spongycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.Objects;

/**
 * description:
 * @author carrot
 */
public class FilterCandidate extends RLPModel {

    int processType;

    byte[] proposalId;

    CaContractCode caContractCode;

    public FilterCandidate(byte[] encode) {
        super(encode);
    }

    public FilterCandidate(int processType, byte[] proposalId, CaContractCode caContractCode) {
        super(null);
        this.processType = processType;
        this.proposalId = proposalId;
        this.caContractCode = caContractCode;
        this.rlpEncoded = rlpEncoded();
    }

    public static FilterCandidate convertFrom(VoteFilterCandidateEvent candidateEvent) {
        FilterCandidate candidate = new FilterCandidate(candidateEvent.getProcessType(), candidateEvent.getProposalId(), new CaContractCode(ByteUtil.copyFrom(candidateEvent.caContractCode.getEncoded())));
        return candidate;
    }

    public int getProcessType() {
        return processType;
    }

    public byte[] getProposalId() {
        return proposalId;
    }

    public CaContractCode getCaContractCode() {
        return caContractCode;
    }

    @Override
    protected byte[] rlpEncoded() {
        byte[] voteType = RLP.encodeInt(this.processType);
        byte[] proposalId = RLP.encodeElement(this.proposalId);
        byte[] caContractCode = this.caContractCode.getEncoded();
        return RLP.encodeList(voteType, proposalId, caContractCode);
    }

    @Override
    protected void rlpDecoded() {
        RLPList rlpInfo = (RLPList) RLP.decode2(rlpEncoded).get(0);
        this.processType = ByteUtil.byteArrayToInt(rlpInfo.get(0).getRLPData());
        this.proposalId = ByteUtil.copyFrom(rlpInfo.get(1).getRLPData());
        this.caContractCode = new CaContractCode(ByteUtil.copyFrom(rlpInfo.get(2).getRLPData()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterCandidate that = (FilterCandidate) o;
        return processType == that.processType &&
                Arrays.equals(proposalId, that.proposalId) &&
                Objects.equals(caContractCode, that.caContractCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processType, caContractCode);
    }

    @Override
    public String toString() {
        return "FilterCandidate{" +
                "processType=" + processType +
                "processId=" + Hex.toHexString(proposalId) +
                ", caContractCode=" + caContractCode +
                '}';
    }
}
