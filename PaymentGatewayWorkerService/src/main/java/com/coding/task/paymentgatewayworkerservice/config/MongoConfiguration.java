package com.coding.task.paymentgatewayworkerservice.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.WriteConcern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;

@Configuration
public class MongoConfiguration {

    @Value("${mongo.user}")
    private String mongoUser;

    @Value("${mongo.password}")
    private String mongoPassword;

    @Value("${mongo.host}")
    private String mongoHost;

    @Value("${mongo.port}")
    private int mongoPort;

    @Value("${mongo.authDb}")
    private String authDb;

    @Bean
    public MongoClientFactoryBean mongo() {
        MongoCredential mongoCredential = MongoCredential.createCredential(
          this.mongoUser,
                this.authDb,
                this.mongoPassword.toCharArray()
        );
        MongoCredential[] mongoCredentials = new MongoCredential[] {mongoCredential};

        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .writeConcern(WriteConcern.ACKNOWLEDGED)
                .retryWrites(false)
                .build();

        MongoClientFactoryBean mongo = new MongoClientFactoryBean();
        mongo.setCredential(mongoCredentials);
        mongo.setHost(this.mongoHost);
        mongo.setPort(this.mongoPort);
        mongo.setMongoClientSettings(mongoClientSettings);

        return mongo;
    }

    //transaction requires replica set...
    //original plan was to use multi documents transaction to better protect data integrity
    //but cannot enable transaction as my local mongodb was not configured as replica set
    /*@Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }*/

}
