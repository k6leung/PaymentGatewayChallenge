package com.coding.task.paymentgatewaytaskdispatcher.repository;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@AllArgsConstructor
public class DispatchClaimBulkUpdateRepositoryImpl implements DispatchClaimBulkUpdateRepository {

    private MongoTemplate mongoTemplate;

    private void executebulkUpdate(Query query, Update update) {
        BulkOperations bulkOperations = this.mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                PaymentTransaction.class);
        bulkOperations.updateMulti(query, update);

        bulkOperations.execute();
    }

    @Override
    public void claimTransactionForDispatch(ExecutionState newState,
                                            String claimTimeKey,
                                            LocalDateTime claimTime,
                                            ObjectId[] ids) {
        Query query = query(where("_id").in(ids));
        Update update = update("executionState", newState)
                .set(claimTimeKey, claimTime);

        this.executebulkUpdate(query, update);
    }

    @Override
    public void unclaimTransaction(ExecutionState newState, ObjectId[] ids) {
        Query query = query(where("_id").in(ids));
        Update update = update("executionState", newState);

        this.executebulkUpdate(query, update);
    }
}
