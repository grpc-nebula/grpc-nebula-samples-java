package com.orientsec.demo.server;

import com.orientsec.demo.common.Constants;
import com.orientsec.demo.service.GreeterImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 服务提供者
 *
 * @author sxp
 * @since 2019/4/16
 */
public class GreeterServer {
  private static final Logger logger = LoggerFactory.getLogger(GreeterServer.class);

  private Server server;
  private int port = Constants.Port.GREETER_SERVICE_SERVER;

  private void start() throws IOException {
    server = ServerBuilder.forPort(port)
            .addService(new GreeterImpl())
            .build()
            .start();

    logger.info("GreeterServer start...");

    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        GreeterServer.this.stop();
        System.err.println("*** GreeterServer shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      logger.info("stop GreeterServer...");
      server.shutdown();
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }


  public static void main(String[] args) throws Exception {
    GreeterServer server = new GreeterServer();
    server.start();

    server.blockUntilShutdown();
  }
}
