package org.querc.cb_grpc.msg;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import org.querc.cb_grpc.msg.grpc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class QueryServiceImpl implements QueryService{
    private final Materializer mat;
    private final ActorSystem system;

    public QueryServiceImpl(ActorSystem system, Materializer mat) {
        this.mat = mat;
        this.system = system;
    }

    @Override
    public CompletionStage<QueryResponse> n1qlQuery(Query in){
        System.out.println("n1ql query");
        String result = new String("");
        try {
            final Future<Object> future = ask(system.actorSelection("/user/dbConnector"), in, 5000);
            result = (String) Await.result(future, Duration.apply(5, "seconds"));
        } catch (Exception e){
            System.out.println("Ask error:" + e);
        }

        QueryResponse reply = QueryResponse.newBuilder()
                .setContent(result)
                .build();
        return CompletableFuture.completedFuture(reply);
    }
    
    @Override
    public CompletionStage<QueryResponse> kvQuery(DocID in){
        System.out.println("kv query");
        String result = new String("");
        try {
            final Future<Object> future = ask(system.actorSelection("/user/dbConnector"), in, 5000);
            result = (String) Await.result(future, Duration.apply(5, "seconds"));
        } catch (Exception e){
            System.out.println("Ask error:" + e);
        }

        QueryResponse reply = QueryResponse.newBuilder()
                .setContent(result)
                .build();
        return CompletableFuture.completedFuture(reply);
    }
    
}