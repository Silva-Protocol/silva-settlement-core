package org.silva.settlement.core.chain.consensus.sequence.executor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.silva.settlement.infrastructure.async.SilvaSettlementWorker;
import org.silva.settlement.core.chain.consensus.fastpath.SettlementFastPathCommitter;
import org.silva.settlement.core.chain.ledger.StateLedger;
import org.silva.settlement.core.chain.ledger.model.Block;
import org.silva.settlement.core.chain.sync.SettlementChainsSyncer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


/**
 * description:
 * @author carrot
 */
public class AsyncExecutor {

    static final Logger logger = LoggerFactory.getLogger("executor");

    StateLedger stateLedger;

    SettlementChainsSyncer settlementChainsSyncer;

    Block latestBlock;

    volatile long currentLatestToBeExeNum;

    SilvaSettlementWorker mainExecutor;

    public AsyncExecutor(StateLedger stateLedger, SettlementChainsSyncer settlementChainsSyncer) {
        this.stateLedger = stateLedger;
        this.settlementChainsSyncer = settlementChainsSyncer;
    }

    public void start() {
        this.currentLatestToBeExeNum = stateLedger.getLatestBeExecutedNum() + 1;
        this.latestBlock = stateLedger.getBlockByNumber(stateLedger.getLatestBeExecutedNum());
        this.mainExecutor = new SilvaSettlementWorker("event_pipeline_main_async_executor") {
            @Override
            protected void beforeLoop() {
                logger.info("event_pipeline_main_async_executor start, latestConsensusNum:[{}], currentLatestToBeExeNum;[{}]!", stateLedger.getLatestLedger().getLatestNumber(), stateLedger.getLatestBeExecutedNum());
            }

            @Override
            protected void doWork() throws Exception {
                var latestConsensusNum = stateLedger.getLatestLedger().getLatestNumber();

                if (latestConsensusNum < AsyncExecutor.this.currentLatestToBeExeNum) {
                    Thread.sleep(10);
                    return;
                }
                exeCurrent(AsyncExecutor.this.currentLatestToBeExeNum);
                AsyncExecutor.this.currentLatestToBeExeNum++;
            }

            @Override
            protected void doException(Throwable e) {
                logger.warn("event_pipeline_main_async_executor doWork error! {}", ExceptionUtils.getStackTrace(e));
            }
        };
        this.mainExecutor.start();
    }

    private void exeCurrent(long currentExeNum) {
        try {
            var finishCondition = new CountDownLatch(1);
            var eventData = this.stateLedger.getEventData(currentExeNum);
            var currentsOffsets = eventData.getPayload().getCrossChainOffsets();
            var settlementsInfos = this.settlementChainsSyncer.generateSettlementBlockInfos(this.latestBlock.getCrossChainOffsets(), currentsOffsets);
            var height = settlementsInfos.getSettlementBlockInfos().isEmpty()? this.latestBlock.getHeight() : this.latestBlock.getHeight() + 1;
            var block = new Block(this.latestBlock.getHash(), eventData.getEpoch(), eventData.getNumber(), height, eventData.getTimestamp(), currentsOffsets, settlementsInfos);
            //logger.info("block[{}] will add to state check!", block.getNumber());
            SettlementFastPathCommitter.addToStateCheck(block, finishCondition);
            finishCondition.await();
            this.latestBlock = block;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
