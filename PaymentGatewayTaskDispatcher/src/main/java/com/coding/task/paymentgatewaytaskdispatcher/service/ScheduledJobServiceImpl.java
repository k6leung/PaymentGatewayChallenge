package com.coding.task.paymentgatewaytaskdispatcher.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.paymentgatewaytaskdispatcher.repository.DispatcherPaymentTransactionRepositoryDispatchClaim;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobServiceImpl implements ScheduledJobService {

    private final DispatcherPaymentTransactionRepositoryDispatchClaim dispatcherPaymentTransactionRepository;

    private final PaymentService paymentService;

    private final NotificationService notificationService;

    @Value("${payment.retry.count}")
    private int paymentRetryCount;

    @Value("${payment.technical.backoff.second}")
    private int paymentTechnicalBackoffSecond;

    @Value("${payment.timeout.backoff.second}")
    private int paymentTimeoutBackoffSecond;

    @Value("${payment.claim.backoff.second}")
    private int paymentClaimBackoffSecond;

    @Value("${payment.batch.size}")
    private int paymentBatchSize;

    @Value("${notification.retry.count}")
    private int notificationRetryCount;

    @Value("${notification.technical.backoff.second}")
    private int notificationTechnicalBackoffSecond;

    @Value("${notification.timeout.backoff.second}")
    private int notificationTimeoutBackoffSecond;

    @Value("${notification.claim.backoff.second}")
    private int notificationClaimBackoffSecond;

    @Value("${notification.batch.size}")
    private int notificationBatchSize;

    private ObjectId[] extractObjectIdFromTransactionList(List<PaymentTransaction> transactionList) {
        return transactionList.stream()
                .map(transaction -> transaction.getId())
                .toArray(ObjectId[]::new);
    }

    @Transactional
    public List<PaymentTransaction> findAndClaimTransactionForPayment() {
        LocalDateTime now = LocalDateTime.now();
        int maxPaymentAttemptCount = 1 + this.paymentRetryCount;
        LocalDateTime paymentTimeoutBackoffBefore = now.minusSeconds(this.paymentTimeoutBackoffSecond);
        LocalDateTime paymentTechnicalErrorBackoffBefore = now.minusSeconds(this.paymentTechnicalBackoffSecond);
        PageRequest pageRequest = PageRequest.of(0, this.paymentBatchSize);

        List<PaymentTransaction> result = this.dispatcherPaymentTransactionRepository.findTransactionForPayment(
                maxPaymentAttemptCount,
                paymentTimeoutBackoffBefore,
                paymentTechnicalErrorBackoffBefore,
                pageRequest
        );
        ObjectId[] objectIds = this.extractObjectIdFromTransactionList(result);

        this.dispatcherPaymentTransactionRepository.claimTransactionForDispatch(
                ExecutionState.PAYMENT_EXECUTION_CLAIMED,
                "paymentDispatchClaimTime",
                now,
                objectIds
        );

        return result;
    }

    @Transactional
    public List<PaymentTransaction> findAndClaimTransactionForNotification() {
        LocalDateTime now = LocalDateTime.now();
        int maxNotificationAttemptCount = 1 + this.notificationRetryCount;
        LocalDateTime notificationTimeoutBackoffBefore = now.minusSeconds(this.notificationTimeoutBackoffSecond);
        LocalDateTime notificationTechnicalErrorBackoffBefore = now.minusSeconds(this.notificationTechnicalBackoffSecond);
        PageRequest pageRequest = PageRequest.of(0, this.notificationBatchSize);

        List<PaymentTransaction> result = this.dispatcherPaymentTransactionRepository.findTransactionForNotification(
                maxNotificationAttemptCount,
                notificationTimeoutBackoffBefore,
                notificationTechnicalErrorBackoffBefore,
                pageRequest
        );
        ObjectId[] objectIds = this.extractObjectIdFromTransactionList(result);

        this.dispatcherPaymentTransactionRepository.claimTransactionForDispatch(
                ExecutionState.RESPONSE_SENDING_EXECUTION_CLAIMED,
                "notificationDispatchClaimTime",
                now,
                objectIds
        );

        return result;
    }

    @Override
    @Scheduled(fixedRate = 1000)
    public void runPaymentJob() {
        log.info("Payment dispatching begin.");
        try {
            List<PaymentTransaction> claimedTransactions = this.findAndClaimTransactionForPayment();

            for (PaymentTransaction transaction : claimedTransactions) {
                this.paymentService.dispatchPayment(transaction.getId());
            }
        } catch (RuntimeException re) {
            log.error("Payment dispatching encountered error, skipping.", re);
        }
    }

    @Override
    @Scheduled(fixedRate = 1000)
    public void runNotificationJob() {
        log.info("Notification dispatching begin.");
        try {
            List<PaymentTransaction> claimedTransactions = this.findAndClaimTransactionForNotification();

            for(PaymentTransaction transaction : claimedTransactions) {
                this.notificationService.dispatchNotification(transaction.getId());
            }
        } catch (RuntimeException re) {
            log.error("Notification dispatching encountered error, skipping.", re);
        }

    }

    //this is for sudden shutdown while record state is stuck in "claimed"
    @Override
    @Scheduled(fixedRate = 1000)
    public void expireClaim() {
        //unclaim payment
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime paymentClaimBefore = now.minusSeconds(this.paymentClaimBackoffSecond);
        PageRequest pageRequest = PageRequest.of(0, this.paymentBatchSize);

        ObjectId[] toBeUnclaimed = this.extractObjectIdFromTransactionList(
                this.dispatcherPaymentTransactionRepository.findTransactionForPaymentUnclaim(
                        paymentClaimBefore,
                        pageRequest
                )
        );
        this.dispatcherPaymentTransactionRepository.unclaimTransaction(
                ExecutionState.PENDING_FOR_EXECUTION,
                toBeUnclaimed
        );

        //unclaim notification
        LocalDateTime notificationClaimBefore = now.minusSeconds(this.notificationClaimBackoffSecond);
        pageRequest = PageRequest.of(0, this.notificationBatchSize);

        toBeUnclaimed = this.extractObjectIdFromTransactionList(
                this.dispatcherPaymentTransactionRepository.findTransactionForNotificationUnclaim(
                        notificationClaimBefore,
                        pageRequest
                )
        );
        this.dispatcherPaymentTransactionRepository.unclaimTransaction(
                ExecutionState.RESULT_RECEIVED,
                toBeUnclaimed
        );
    }
}
