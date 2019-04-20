package org.querc.cb_grpc.msg;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import org.querc.cb_grpc.msg.grpc.*;
import org.querc.cb_grpc.msg.internal_messages;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;
import java.util.List;

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
        QueryResponse result;
        try {
            final Future<Object> future = ask(system.actorSelection("/user/dbConnector"), in, 5000);
            result = (QueryResponse) Await.result(future, Duration.apply(5, "seconds"));
        } catch (Exception e){
            result = QueryResponse.newBuilder()
                    .setCode("Failed")
                    .setContent(e.toString())
                    .build();
        }
        return CompletableFuture.completedFuture(result);
    }
    
    @Override
    public CompletionStage<QueryResponse> kvGet(DocID in){
        List<QueryResponse> reply = null;
        String reply_string = new String();
        try {
            internal_messages.kvget message = internal_messages.kvget.newBuilder()
                    .addDoc(in)
                    .build();
            final Future<Object> future = ask(system.actorSelection("/user/dbConnector"), message, 5000);
            reply = (List<QueryResponse>) Await.result(future, Duration.apply(5, "seconds"));
            for (QueryResponse subMessage : reply){
                reply_string += subMessage.getContent();
            }
        } catch (Exception e){
            
        }
        QueryResponse result = QueryResponse.newBuilder()
                    .setCode("Success")
                    .setContent(reply_string)
                    .build();
        return CompletableFuture.completedFuture(result);
    }
    
    @Override
    public CompletionStage<QueryResponse> kvDelete(DocID in){
        List<QueryResponse> reply = null;
        String reply_string = new String();
        try {
            internal_messages.kvdelete message = internal_messages.kvdelete.newBuilder()
                    .addDoc(in)
                    .build();
            final Future<Object> future = ask(system.actorSelection("/user/dbConnector"), message, 5000);
            reply = (List<QueryResponse>) Await.result(future, Duration.apply(5, "seconds"));
            for (QueryResponse subMessage : reply){
                reply_string += subMessage.getContent();
            }
        } catch (Exception e){
            
        }
        QueryResponse result = QueryResponse.newBuilder()
                    .setCode("Success")
                    .setContent(reply_string)
                    .build();
        return CompletableFuture.completedFuture(result);
    }
    
    @Override
    public CompletionStage<QueryResponse> kvPut(JsonID in){
        List<QueryResponse> reply = null;
        String reply_string = new String();
        try {
                internal_messages.kvput message = internal_messages.kvput.newBuilder()
                    .addDoc(in)
                    .build();
            final Future<Object> future = ask(system.actorSelection("/user/dbConnector"), message, 5000);
            reply = (List<QueryResponse>) Await.result(future, Duration.apply(5, "seconds"));
            for (QueryResponse subMessage : reply){
                reply_string += subMessage.getContent();
            }
        } catch (Exception e){
            
        }
        QueryResponse result = QueryResponse.newBuilder()
                    .setCode("Success")
                    .setContent(reply_string)
                    .build();
        return CompletableFuture.completedFuture(result);
    }
    
    @Override
    public CompletionStage<QueryResponse> kvUpsert(JsonID in){
        List<QueryResponse> reply = null;
        String reply_string = new String();
        System.out.println(in.getDocument());
        try {
                internal_messages.kvupsert message = internal_messages.kvupsert.newBuilder()
                    .addDoc(in)
                    .build();
            final Future<Object> future = ask(system.actorSelection("/user/dbConnector"), message, 5000);
            reply = (List<QueryResponse>) Await.result(future, Duration.apply(5, "seconds"));
            for (QueryResponse subMessage : reply){
                reply_string += subMessage.getContent();
            }
        } catch (Exception e){
            
        }
        QueryResponse result = QueryResponse.newBuilder()
                    .setCode("Success")
                    .setContent(reply_string)
                    .build();
        return CompletableFuture.completedFuture(result);
    }
}