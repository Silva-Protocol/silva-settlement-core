package org.silva.settlement.core.chain.ledger.model.event.ca;

import org.silva.settlement.infrastructure.bytes.ByteArrayWrapper;
import org.silva.settlement.infrastructure.bytes.ByteUtil;
import org.silva.settlement.infrastructure.rlp.RLP;
import org.silva.settlement.infrastructure.rlp.RLPList;
import org.silva.settlement.core.chain.ledger.model.event.GlobalEventCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * description:
 * @author carrot
 */
public class NodeCandidateStateSnapshot extends CandidateStateSnapshot {

    List<ByteArrayWrapper> currentAgreeVotes;

    List<ByteArrayWrapper> currentRejectVotes;

    Optional<NodeCandidate> currentCandidate;

    public NodeCandidateStateSnapshot(byte[] encode) {
        super(encode);
    }

    public NodeCandidateStateSnapshot(List<ByteArrayWrapper> currentAgreeVotes, List<ByteArrayWrapper> currentRejectVotes, Optional<NodeCandidate> currentCandidate) {
        super(null);
        this.currentAgreeVotes = currentAgreeVotes;
        this.currentRejectVotes = currentRejectVotes;
        this.currentCandidate = currentCandidate;
        this.rlpEncoded = rlpEncoded();
    }

    @Override
    public GlobalEventCommand getCurrentCommand() {
        return GlobalEventCommand.VOTE_NODE_CANDIDATE;
    }

    public List<ByteArrayWrapper> getCurrentAgreeVotes() {
        return currentAgreeVotes;
    }

    public List<ByteArrayWrapper> getCurrentRejectVotes() {
        return currentRejectVotes;
    }

    public Optional<NodeCandidate> getCurrentCandidate() {
        return currentCandidate;
    }

    @Override
    protected byte[] rlpEncoded() {

        int currentAgreeVotesSize = currentAgreeVotes.size();
        int currentRejectVotesSize = currentRejectVotes.size();
        byte[][] encode = new byte[1 + currentAgreeVotesSize + 1 + currentRejectVotesSize + 1][];

        encode[0] = RLP.encodeInt(currentAgreeVotesSize);
        int agreeVotesStart = 1;
        for (ByteArrayWrapper addr : currentAgreeVotes) {
            encode[agreeVotesStart] = RLP.encodeElement(addr.getData());
            agreeVotesStart++;
        }

        int rejectVotesSizePos = 1 + currentAgreeVotesSize;
        encode[rejectVotesSizePos] = RLP.encodeInt(currentRejectVotesSize);
        int rejectVotesStart = rejectVotesSizePos + 1;
        for (ByteArrayWrapper addr : currentRejectVotes) {
            encode[rejectVotesStart] = RLP.encodeElement(addr.getData());
            rejectVotesStart++;
        }

        int candidatePos = 1 + currentAgreeVotesSize + 1 + currentRejectVotesSize;
        if (currentCandidate.isPresent()) {
            encode[candidatePos] = RLP.encodeElement(currentCandidate.get().getEncoded());
        } else {
            encode[candidatePos] = ByteUtil.EMPTY_BYTE_ARRAY;
        }

        return RLP.encodeList(encode);
    }

    @Override
    protected void rlpDecoded() {
        RLPList rlpDecode = (RLPList) RLP.decode2(rlpEncoded).get(0);

        int currentAgreeVotesSize = ByteUtil.byteArrayToInt(rlpDecode.get(0).getRLPData());
        List<ByteArrayWrapper> currentAgreeVotes = new ArrayList<>(currentAgreeVotesSize);
        int agreeVotesStartPos = 1;
        int agreeVotesEndPos = 1 + currentAgreeVotesSize;
        for (int i = agreeVotesStartPos; i < agreeVotesEndPos; i++) {
            currentAgreeVotes.add(new ByteArrayWrapper(rlpDecode.get(i).getRLPData()));
        }
        this.currentAgreeVotes = currentAgreeVotes;

        int currentRejectVotesSize = ByteUtil.byteArrayToInt(rlpDecode.get(agreeVotesEndPos).getRLPData());
        List<ByteArrayWrapper> currentRejectVotes = new ArrayList<>(currentRejectVotesSize);
        int rejectVotesStartPos = agreeVotesEndPos + 1;
        int rejectVotesEndPos = rejectVotesStartPos + currentRejectVotesSize;
        for (int i = rejectVotesStartPos; i < rejectVotesEndPos; i++) {
            currentRejectVotes.add(new ByteArrayWrapper(rlpDecode.get(i).getRLPData()));
        }
        this.currentRejectVotes = currentRejectVotes;

        int candidatePos = 1 + currentAgreeVotesSize + 1 + currentRejectVotesSize;
        if (rlpDecode.size() > candidatePos) {
            this.currentCandidate = Optional.of(new NodeCandidate(rlpDecode.get(candidatePos).getRLPData()));
        } else {
            this.currentCandidate = Optional.empty();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeCandidateStateSnapshot that = (NodeCandidateStateSnapshot) o;
        return Objects.equals(currentAgreeVotes, that.currentAgreeVotes) &&
                Objects.equals(currentRejectVotes, that.currentRejectVotes) &&
                Objects.equals(currentCandidate, that.currentCandidate);
    }

    @Override
    public int hashCode() {

        return Objects.hash(currentAgreeVotes, currentRejectVotes, currentCandidate);
    }

    @Override
    public String toString() {
        return "NodeCandidateStateSnapshot{" +
                "currentAgreeVotes=" + currentAgreeVotes +
                ", currentRejectVotes=" + currentRejectVotes +
                ", currentCandidate=" + currentCandidate +
                '}';
    }
}
