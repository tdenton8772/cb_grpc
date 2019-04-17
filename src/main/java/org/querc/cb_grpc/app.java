package org.querc.cb_grpc;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;

import java.io.Serializable;
import java.util.Properties;
import org.querc.cb_grpc.msg.*;
import com.google.protobuf.InvalidProtocolBufferException;


public class app extends AbstractActor {
    final ActorSystem system = getContext().getSystem();
    private Properties m_props;

    static public Props props(Properties p){
        return Props.create(app.class, () -> new app(p));
    }

    public app(Properties p){
        System.out.println(getSelf());
        this.m_props = p;
        final ActorRef DBConnector = system.actorOf(dbConnector.props(m_props), "dbConnector");
        final ActorRef grpcServer = system.actorOf(org.querc.cb_grpc.grpc_server.props(m_props), "grpc_server");
    }
    
    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .matchAny(o -> System.out.println("Unknown Message in erpApp: " + o))
                .build();
    }
}

