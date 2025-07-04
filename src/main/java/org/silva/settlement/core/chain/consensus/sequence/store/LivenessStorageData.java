package org.silva.settlement.core.chain.consensus.sequence.store;

import org.silva.settlement.infrastructure.bytes.ByteArrayWrapper;
import org.silva.settlement.core.chain.consensus.sequence.model.*;
import org.silva.settlement.core.chain.consensus.sequence.model.*;
import org.silva.settlement.core.chain.consensus.sequence.model.settlement.SettlementChainOffsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.util.*;

/**
 * description:
 * @author carrot
 */
public class LivenessStorageData {

    public static class LedgerRecoveryData extends  LivenessStorageData {

        private static final Logger logger = LoggerFactory.getLogger("consensus");

        LedgerInfo storageLedger;

        SettlementChainOffsets latestOffsets;

        private LedgerRecoveryData(LedgerInfo storageLedger, SettlementChainOffsets latestOffsets) {
            this.storageLedger = storageLedger;
            this.latestOffsets = latestOffsets;
        }

        public static  LedgerRecoveryData build(LedgerInfo storageLedger, SettlementChainOffsets consensusSettlementChainOffsets) {
            return new LedgerRecoveryData(storageLedger, consensusSettlementChainOffsets);
        }

        public long getCommitRound() {
            return storageLedger.getRound();
        }

        public RecoveryData.RootInfo findRoot(ConsensusChainStore consensusChainStore, SettlementChainOffsets latestOffsets, List<Event> events, List<QuorumCert> quorumCerts) {
            byte[] rootId;
            if (storageLedger.getNewCurrentEpochState().isPresent()) {

                Event genesis = Event.buildGenesisEventFromLedgerInfo(storageLedger, latestOffsets);
                QuorumCert genesisQC = QuorumCert.certificateForGenesisFromLedgerInfo(storageLedger, genesis.getId());

                rootId = genesis.getId();

                boolean isCreate = false;
                for (Event event: events) {
                    if (Arrays.equals(event.getId(), rootId)) {
                        isCreate = true;
                        break;
                    }
                }

                if (!isCreate) {
                    events.add(genesis);
                    quorumCerts.add(genesisQC);
                    consensusChainStore.saveEventsAndQuorumCertificates(List.of(genesis), List.of(genesisQC));
                }
                logger.info("findRoot genesis qc epoch[{}] round[{}] number[{}] rootId:[{}]", genesis.getEpoch(), genesis.getRound(), genesis.getEventNumber(), Hex.toHexString(rootId));
            } else {
                rootId = storageLedger.getConsensusEventId();
                logger.info("findRoot common epoch[{}] round[{}] number[{}] rootId:[{}]", storageLedger.getEpoch(), storageLedger.getRound(), storageLedger.getNumber(), Hex.toHexString(rootId));

            }

            // sort by (epoch, round) to guarantee the topological order of parent <- child
            events.sort((o1, o2) -> {
                long epochCompare = o1.getEpoch() - o2.getEpoch();
                if (epochCompare != 0) {
                    return (int)epochCompare;
                } else {
                    return (int)(o1.getRound() - o2.getRound());
                }
            });

            Integer rootIdx = null;
            for (int i = 0; i < events.size(); i++) {
                if (Arrays.equals(events.get(i).getId(), rootId)) {
                    rootIdx = i;
                    break;
                }
            }

            if (rootIdx == null) {
                logger.warn("unable to find root: {}, start sync!", Hex.toHexString(rootId));
                throw new RuntimeException("unable to find root: {}, start sync" + Hex.toHexString(rootId));

            }

            Event rootEvent = events.get(rootIdx.intValue());

            QuorumCert rootQuorumCert = null;
            for (QuorumCert qc: quorumCerts) {
                if (Arrays.equals(qc.getCertifiedEvent().getId(), rootEvent.getId())) {
                    rootQuorumCert = qc;
                    break;
                }
            }
            if (rootQuorumCert == null) {
                logger.error("No QC found for root: {}", Hex.toHexString(rootId));
                // we should restart from ledgerRecoveryData,
                throw new RuntimeException("No QC found for root: " +  Hex.toHexString(rootId));
            }

            QuorumCert rootLedgerInfo  = null;
            for (QuorumCert qc: quorumCerts) {
                if (Arrays.equals(qc.getCommitEvent().getId(), rootEvent.getId())) {
                    rootLedgerInfo = qc;
                    break;
                }
            }
            if (rootLedgerInfo == null) {
                logger.error("No LI found for root: {}", Hex.toHexString(rootId));
                throw new RuntimeException("No LI found for root: {}" + Hex.toHexString(rootId));
            }

            logger.info("Consensus root event is {}", rootEvent);
            return new RecoveryData.RootInfo(rootEvent, rootQuorumCert, rootLedgerInfo);
        }
    }

    public static class RecoveryData extends LivenessStorageData {

        private static final Logger logger = LoggerFactory.getLogger("consensus");

        public static class RootInfo {
            Event rootEvent;
            QuorumCert rootQc;
            QuorumCert rootLi;

            public RootInfo(Event rootEvent, QuorumCert rootQc, QuorumCert rootLi) {
                this.rootEvent = rootEvent;
                this.rootQc = rootQc;
                this.rootLi = rootLi;
            }

            public Event getRootEvent() {
                return rootEvent;
            }

            public QuorumCert getRootQc() {
                return rootQc;
            }

            public QuorumCert getRootLi() {
                return rootLi;
            }
        }

        Optional<Vote> lastVote;

        RecoveryData.RootInfo root;

        List<Event> events;

        List<QuorumCert> quorumCerts;

        ExecutedEventOutput executedEventOutput;

        Optional<List<byte[]>> eventsToPrune;

        Optional<TimeoutCertificate>  highestTimeoutCertificate;

        Optional<TwoChainTimeoutCertificate>  highestTwoChainTimeoutCertificate;

        public static  RecoveryData build(ConsensusChainStore consensusChainStore, Optional<Vote> lastVote, LedgerRecoveryData ledgerRecoveryData, List<Event> events
                , List<QuorumCert> quorumCerts, ExecutedEventOutput executedEventOutput, Optional<TimeoutCertificate> highestTimeoutCertificate, Optional<TwoChainTimeoutCertificate> highestTwoChainTimeoutCertificate) {
            RecoveryData result = new RecoveryData();

            if (logger.isDebugEnabled()) {
                logger.debug("all build events {}", events);
                logger.debug("all build qcs {}", quorumCerts);
                logger.debug("all build storageLedger {}", ledgerRecoveryData.storageLedger);
            }

            RecoveryData.RootInfo root = ledgerRecoveryData.findRoot(consensusChainStore, ledgerRecoveryData.latestOffsets, events, quorumCerts);
            result.root = root;
            quorumCerts.sort((o1, o2) -> (int) (o1.getCertifiedEvent().getRound() - o2.getCertifiedEvent().getRound()));


            List<byte[]> eventsToPrune =  findEventsToPrune(root.rootEvent.getId(), events, quorumCerts);
            if (logger.isDebugEnabled()) {
                StringBuilder eventsToPruneSB = new StringBuilder();
                for (byte[] id: eventsToPrune) {
                    eventsToPruneSB.append(Hex.toHexString(id)).append(",");
                }
                logger.debug("all build eventsToPrune [{}]", eventsToPruneSB.toString());
            }


            long epoch = root.rootEvent.getEpoch();
            if (lastVote.isPresent() && lastVote.get().getEpoch() == epoch) {
                result.lastVote = lastVote;
            } else {
                result.lastVote = Optional.empty();
            }

            result.events = events;
            result.quorumCerts = quorumCerts;
            result.executedEventOutput = executedEventOutput;
            result.eventsToPrune = Optional.of(eventsToPrune);

            if (highestTimeoutCertificate.isPresent() && highestTimeoutCertificate.get().getEpoch() == epoch) {
                result.highestTimeoutCertificate = highestTimeoutCertificate;
            } else {
                result.highestTimeoutCertificate = Optional.empty();
            }

            if (highestTwoChainTimeoutCertificate.isPresent() && highestTwoChainTimeoutCertificate.get().getEpoch() == epoch) {
                result.highestTwoChainTimeoutCertificate = highestTwoChainTimeoutCertificate;
            } else {
                result.highestTwoChainTimeoutCertificate = Optional.empty();
            }

            return result;
        }

        public Optional<Vote> getLastVote() {
            return lastVote;
        }

        public List<byte[]> takeEventsToPrune() {
            List<byte[]> result = eventsToPrune.get();
            eventsToPrune = Optional.empty();
            return result;
        }

        public Optional<TimeoutCertificate> getHighestTimeoutCertificate() {
            return highestTimeoutCertificate;
        }

        public Optional<TwoChainTimeoutCertificate> getHighestTwoChainTimeoutCertificate() {
            return highestTwoChainTimeoutCertificate;
        }

        public RootInfo getRoot() {
            return root;
        }

        public List<Event> getEvents() {
            return events;
        }

        public List<QuorumCert> getQuorumCerts() {
            return quorumCerts;
        }

        public ExecutedEventOutput getExecutedEventOutput() { return executedEventOutput; }

        public static  List<byte[]> findEventsToPrune(byte[] rootId, List<Event> events, List<QuorumCert> quorumCerts) {

            HashSet<ByteArrayWrapper> tree = new HashSet<>();
            List<byte[]> toRemove = new ArrayList<>();

            ByteArrayWrapper rootWrapper = new ByteArrayWrapper(rootId);
            tree.add(rootWrapper);
            // assume blocks are sorted by round already
            Iterator<Event> eventIter = events.iterator();
            while (eventIter.hasNext()) {
                Event event = eventIter.next();
                if (tree.contains(new ByteArrayWrapper(event.getParentId()))) {
                    tree.add(new ByteArrayWrapper(event.getId()));
                } else {

                    // keep the root event, don't remove;
                    if (Arrays.equals(event.getId(), rootId)) continue;
                    toRemove.add(event.getId());
                    eventIter.remove();
                }
            }

            Iterator<QuorumCert> qcIter = quorumCerts.iterator();
            while (qcIter.hasNext()) {
                QuorumCert quorumCert = qcIter.next();
                if (!tree.contains(new ByteArrayWrapper(quorumCert.getCertifiedEvent().getId()))) {
                    qcIter.remove();
                }
            }
            return toRemove;
        }

        @Override
        public String toString() {
            return "RecoveryData{" +
                    "lastVote=" + lastVote + "\n" +
                    ", root=" + root + "\n" +
                    ", events=" + events + "\n" +
                    ", quorumCerts=" + quorumCerts + "\n" +
                    ", executedEventOutput=" + executedEventOutput + "\n" +
                    ", eventsToPrune=" + eventsToPrune + "\n" +
                    ", highestTimeoutCertificate=" + highestTimeoutCertificate + "\n" +
                    '}';
        }
    }
}
