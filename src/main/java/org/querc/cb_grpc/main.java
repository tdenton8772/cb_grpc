package org.querc.cb_grpc;


import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import akka.util.Timeout;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class main {
    private static Properties m_props;
    private static File propFile;
    public static ActorSystem system;
    public static ActorRef mainApp;

    public static void main(String[] args){
       m_props = new Properties();
       propFile = new File("main.properties");

       try{
           LoadProperties(propFile);
       } catch (Exception e){
           System.out.println(e);
       }
       final ActorSystem system = makeActorSystem("main");
       final ActorRef MainApp = makeMainApp(system);
    }
    
    public static ActorSystem makeActorSystem(String name) {
        Config conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
                .withFallback(ConfigFactory.defaultApplication());
        final ActorSystem system = ActorSystem.create(name, conf);
        return system;
    }

    public static ActorRef makeMainApp(ActorSystem system) {
        final ActorRef MainApp = system.actorOf(app.props(m_props), "mainApp");

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return MainApp;
    }
    
    public static Timeout timeout() {
        return new Timeout(Duration.create(5, TimeUnit.SECONDS));
    }

    public static void LoadProperties(File f) throws IOException {
        FileInputStream propStream = null;
        propStream = new FileInputStream(f);
        m_props.load(propStream);
        propStream.close();
    }
    

}
