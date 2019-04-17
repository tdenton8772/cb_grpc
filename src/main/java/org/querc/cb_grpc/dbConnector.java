package org.querc.cb_grpc;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.RawJsonDocument;

import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;

import java.util.*;
import java.util.Properties;

import org.querc.cb_grpc.msg.grpc.*;

public class dbConnector extends AbstractActor {
    final ActorSystem system = getContext().getSystem();
    private Properties m_props;
    static private Cluster cluster;
    static private Bucket bucketMain;
    static private Bucket bucketHxn;
    static private Bucket bucketTxn;
    static private JsonFormat.Printer printer;

    static public Props props(Properties p){
        return Props.create(dbConnector.class, () -> new dbConnector(p));
    }
    
    public dbConnector(Properties p){
        this.printer = JsonFormat.printer().includingDefaultValueFields();
        System.out.println(getSelf());
        this.m_props = p;
        String dbClusterList = m_props.getProperty("DBCluster");
        List<String> nodes = Arrays.asList(dbClusterList.split("\\s*,\\s*"));
        String cbVersion = m_props.getProperty("DBVersion");

        if(Boolean.parseBoolean(m_props.getProperty("UseDatabase")) == true) {
            if (cbVersion.equals("4") || cbVersion.equals("4.5") || cbVersion.equals("4.5.1")) {
                CouchbaseEnvironment env = DefaultCouchbaseEnvironment
                        .builder()
                        .queryTimeout(Integer.parseInt(m_props.getProperty("QueryTimeout")))
                        .kvTimeout(Integer.parseInt(m_props.getProperty("KVTimeout")))
                        .build();
                this.cluster = CouchbaseCluster.create(env, nodes);
                this.bucketMain = cluster.openBucket(m_props.getProperty("DBMain"), m_props.getProperty("DBErpPassword"));
                this.bucketTxn = cluster.openBucket(m_props.getProperty("DBTxn"), m_props.getProperty("DBTxnPassword"));
                this.bucketHxn = cluster.openBucket(m_props.getProperty("DBHxn"), m_props.getProperty("DBHxnPassword"));
            } else {
                CouchbaseEnvironment env = DefaultCouchbaseEnvironment
                        .builder()
                        .queryTimeout(Integer.parseInt(m_props.getProperty("QueryTimeout")))
                        .kvTimeout(Integer.parseInt(m_props.getProperty("KVTimeout")))
                        .build();
                this.cluster = CouchbaseCluster.create(env, nodes);
                this.cluster.authenticate(m_props.getProperty("ClusterUser"), m_props.getProperty("ClusterPW"));
                this.bucketMain = cluster.openBucket(m_props.getProperty("DBErp"));
                this.bucketTxn = cluster.openBucket(m_props.getProperty("DBTxn"));
                this.bucketHxn = cluster.openBucket(m_props.getProperty("DBHxn"));
            }
            N1qlQueryResult holdingVar = this.bucketMain.query(N1qlQuery.simple("Select * from `" + m_props.getProperty("DBMain") + "` limit 1;"));
        }
    }
    
    protected String EstablishConnection(InitiateConnection msg) throws InvalidProtocolBufferException {
        try{
            N1qlQueryResult holdingVar = this.bucketMain.query(N1qlQuery.simple("Select * from " + m_props.getProperty("DBMain") + " limit 1;"));
            return "Connection already established";
        } catch (Exception e) {
            System.out.println("Expected Error while setting up connection: " + e);
            try{
                if (msg.getVersion().equals("4") || msg.getVersion().equals("4.5") || msg.getVersion().equals("4.5.1")) {
                    CouchbaseEnvironment env = DefaultCouchbaseEnvironment
                            .builder()
                            .queryTimeout(msg.getQueryTimeout())
                            .kvTimeout(msg.getKvTimeout())
                            .build();
                    this.cluster = CouchbaseCluster.create(env, msg.getClusterAddressList());
                    this.bucketMain = cluster.openBucket(msg.getDBMain(), msg.getDBMainPassword());
                    this.bucketTxn = cluster.openBucket(msg.getDBTxn(), msg.getDBTxnPassword());
                    this.bucketHxn = cluster.openBucket(msg.getDBHxn(), msg.getDBHxnPassword());
                    N1qlQueryResult holdingVar = this.bucketMain.query(N1qlQuery.simple("Select * from `" + msg.getDBMain() + "` limit 1;"));
                    return "Successful";
                } else {
                    CouchbaseEnvironment env = DefaultCouchbaseEnvironment
                            .builder()
                            .queryTimeout(msg.getQueryTimeout())
                            .kvTimeout(msg.getKvTimeout())
                            .build();
                    this.cluster = CouchbaseCluster.create(env, msg.getClusterAddressList());
                    this.cluster.authenticate(msg.getClusterUser(), msg.getClusterPW());
                    this.bucketMain = cluster.openBucket(msg.getDBMain());
                    this.bucketTxn = cluster.openBucket(msg.getDBTxn());
                    this.bucketHxn = cluster.openBucket(msg.getDBHxn());
                    N1qlQueryResult holdingVar = this.bucketMain.query(N1qlQuery.simple("Select * from `" + msg.getDBMain() + "` limit 1;"));
                    return "Successful";
                }
            } catch (Exception r) {
                System.out.println(r);
                return "Unsuccessful. Error during connection";
            }
        }
    }

    protected QueryResponse cbQuery(Query msg) throws InvalidProtocolBufferException {
        try { 
            N1qlQueryResult holdingVar = this.bucketMain.query(N1qlQuery.simple(msg.getN1QlQuery()));
            QueryResponse reply = QueryResponse.newBuilder()
                    .setCode("Success")
                    .setContent(holdingVar.allRows().toString())
                    .build();
            return reply;
        } catch (Exception e) {
            QueryResponse reply = QueryResponse.newBuilder()
                    .setCode("Failed")
                    .setContent(e.toString())
                    .build();
            return reply;
        }
    }
    
    protected QueryResponse kvQuery(DocID msg) throws InvalidProtocolBufferException {
        Bucket queryBucket;
        System.out.println(msg.getBucket().toString());
        if (msg.getBucket().toString().equals("main")){
            queryBucket = this.bucketMain;
        } else if (msg.getBucket().toString().equals("txn")){
            queryBucket = this.bucketTxn;
        } else if (msg.getBucket().toString().equals("hxn")){
            queryBucket = this.bucketHxn;
        } else {
            QueryResponse reply = QueryResponse.newBuilder()
                    .setCode("Failed")
                    .build();
            return reply;
        } 
        
        try{
            RawJsonDocument doc = queryBucket.get(msg.getDocID(), RawJsonDocument.class);
            QueryResponse reply = QueryResponse.newBuilder()
                    .setCode("Success")
                    .setContent(doc.content())
                    .build();
            return reply;
        } catch (Exception e){
            QueryResponse reply = QueryResponse.newBuilder()
                    .setCode("Failed")
                    .setContent(e.toString())
                    .build();
            return reply;
        }
    }
    
    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(InitiateConnection.class, (msg) ->{
                    String message = EstablishConnection(msg);
                    System.out.println("Message: " + message);
                    getSender().tell(message, getSelf());
                })
                .match(Query.class, (msg) ->{
                    QueryResponse message = cbQuery(msg);
                    getSender().tell(message, getSelf());
                })
                .match(DocID.class, (msg)->{
                    QueryResponse message = kvQuery(msg);
                    getSender().tell(message, getSelf());
                })
            .matchAny(o -> System.out.println("Unknown Message in dbConnector: " + o))
            .build();
    }
}
