package org.querc.cb_grpc;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.grpc.javadsl.ServiceHandler;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.UseHttp2;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.ConnectHttp;
import akka.japi.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

import org.querc.cb_grpc.msg.*;
import org.querc.cb_grpc.msg.grpc.*;

public class grpc_server extends AbstractActor {
    final ActorSystem system = getContext().getSystem();
    private Properties m_props;

    static public Props props(Properties p){
        return Props.create(grpc_server.class, () -> new grpc_server(p));
    }

    @Override
    public void preStart() throws Exception {
        System.out.println(getSelf());

        Materializer mat = ActorMaterializer.create(system);

        run(system).thenAccept(bindings -> {
            bindings.forEach(binding -> {
                System.out.println("CouchBase gRPC server bound to: " + binding.localAddress());
            });
        });
    }
    
    public static CompletionStage<List<ServerBinding>> run(ActorSystem system) throws Exception {
        Materializer mat = ActorMaterializer.create(system);
        
        Function<HttpRequest, CompletionStage<HttpResponse>> greeterService =
                org.querc.cb_grpc.msg.grpc.QueryServiceHandlerFactory.create(new org.querc.cb_grpc.msg.QueryServiceImpl(system, mat), mat, system);
        
        Function<HttpRequest, CompletionStage<HttpResponse>> serviceHandlers =
                ServiceHandler.concatOrNotFound(greeterService);

        CompletionStage<ServerBinding> binding1 = Http.get(system).bindAndHandleAsync(
                serviceHandlers,
                ConnectHttp.toHost("127.0.0.1", 8080, UseHttp2.always()),
                mat);
        
        CompletionStage<ServerBinding> binding2 = Http.get(system).bindAndHandleAsync(
                DatabaseServiceHandlerFactory.create(new org.querc.cb_grpc.msg.DatabaseServiceImpl(system, mat), mat, system),
                ConnectHttp.toHost("127.0.0.1", 8081, UseHttp2.always()),
                mat);
        
        return binding1.thenCombine(binding2, (b1, b2) -> Arrays.asList(b1, b2));
    }
    
    public grpc_server(Properties p) throws Exception{
        this.m_props = p;
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(o -> System.out.println("Unknown Message in grpc_server: " + o))
                .build();
    }
}



        