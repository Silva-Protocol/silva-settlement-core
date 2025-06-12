//package com.xchain.consensus.hotstuffbft;
//
//import SystemConfig;
//import org.silva.settlement.infrastructure.crypto.HashUtil;
//import org.silva.settlement.core.chain.consensus.hotstuffbft.model.*;
//import org.silva.settlement.core.chain.consensus.hotstuffbft.store.ConsensusSource;
//import crypto.model.ledger.org.silva.settlement.core.chain.ValidatorSigner;
//import com.thanos.model.common.EventDataBuilder;
//import com.thanos.model.common.EventInfoBuilder;
//import com.thanos.model.common.QuorumCertBuilder;
//import com.thanos.model.common.SignatureBuilder;
//import org.junit.Test;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
///**
// * ConsensusSourceTest.java description：
// *
// * @Author laiyiyu create on 2020-06-28 17:26:34
// */
//public class ConsensusSourceTest {
//
//
//    static byte[] author = HashUtil.sha3(new byte[]{111, 121, 122});
//
//
//    //@Test
//    public void insert() {
//        SystemConfig systemConfig = new SystemConfig();
//        ConsensusSource consensusSource = new ConsensusSource(true, systemConfig);
//        Event event1 = Event.buildProposalFromEventData(EventDataBuilder.buildCommon(), new ValidatorSigner(null));
//        Event event2 = Event.buildProposalFromEventData(EventDataBuilder.buildGensis(), new ValidatorSigner(null));
//        List<Event> events = Arrays.asList(event1, event2);
//
//        QuorumCert quorumCert1 = QuorumCertBuilder.buildGensis();
//        QuorumCert quorumCert2 = QuorumCertBuilder.buildHighest();
//        List<QuorumCert> quorumCerts = Arrays.asList(quorumCert1, quorumCert2);
//
//        consensusSource.saveEventsAndQuorumCertificates(events, quorumCerts);
//
//
//        EventInfo proposal = EventInfoBuilder.buildWithVerifier();
//        EventInfo parent = EventInfoBuilder.buildWithoutVerifier();
//
//        Vote vote = Vote.build(VoteData.build(proposal, parent), author, LedgerInfo.build(parent, parent.getHash()), new ValidatorSigner(null));
//        consensusSource.saveLastVoteMsg(vote);
//
//        TimeoutCertificate timeoutCertificate = TimeoutCertificate.build(Timeout.build(1, 1), SignatureBuilder.build());
//        consensusSource.saveHighestTimeoutCertificate(timeoutCertificate);
//
//    }
//
//    //@Test
//    public void query() {
//        SystemConfig systemConfig = new SystemConfig();
//        ConsensusSource consensusSource = new ConsensusSource(true, systemConfig);
//        List<Event> events = consensusSource.getAllEvents();
//        events.forEach(event ->System.out.println(event));
//        List<QuorumCert> quorumCerts = consensusSource.getAllQuorumCerts();
//        quorumCerts.forEach(quorumCert ->System.out.println(quorumCert));
//        Optional<Vote> vote = consensusSource.getLastVoteMsg();
//        System.out.println(vote.get());
//        Optional<TimeoutCertificate> timeoutCertificate = consensusSource.getHighestTimeoutCertificate();
//        System.out.println(timeoutCertificate.get());
//    }
//
//
//}
