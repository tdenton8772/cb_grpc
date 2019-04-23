package org.querc.cb_grpc.msg;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import org.querc.cb_grpc.msg.grpc.*;
import org.querc.cb_grpc.msg.internal_messages;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;
//import akka.persistence.serialization.Message;
import com.google.protobuf.Any;
import java.util.Arrays;
import com.google.protobuf.Message;
import java.util.List;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import org.querc.cb_grpc.msg.database.*;
        
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
    
    @Override
    public CompletionStage<QueryResponse> anyService(AnyID in){
//        System.out.println(in.getDocList());
        for(Any x : in.getDetailsList()){
            try{
                System.out.println(x.getTypeUrl());
                String clazzName = x.getTypeUrl().split("/")[1];
                System.out.println(clazzName);
                String[] split_name = clazzName.split("\\.");
                String nameClass = String.join(".", Arrays.copyOfRange(split_name, 0, split_name.length - 1)) + "$" + split_name[split_name.length-1];
                Class<Message> clazz = (Class<Message>) Class.forName(nameClass);
                
                System.out.println(x.unpack(clazz));
                
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        QueryResponse result = QueryResponse.newBuilder()
                .build();
        return CompletableFuture.completedFuture(result);
    }
}