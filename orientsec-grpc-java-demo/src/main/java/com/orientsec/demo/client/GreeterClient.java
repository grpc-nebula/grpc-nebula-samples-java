/*
 * Copyright 2019 Orient Securities Co., Ltd.
 * Copyright 2019 BoCloud Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orientsec.demo.client;

import com.orientsec.demo.GreeterGrpc;
import com.orientsec.demo.GreeterReply;
import com.orientsec.demo.GreeterRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 服务消费者
 *
 * @author sxp
 * @since 2019/4/16
 */
public class GreeterClient {
  private static final Logger logger = LoggerFactory.getLogger(GreeterClient.class);

  private final ManagedChannel channel;
  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  public static GreeterClient getInstance() {
    return SingletonHolder.INSTANCE;
  }

  // 懒汉式单例模式--直到使用时才创建对象
  private static class SingletonHolder {
    private static final GreeterClient INSTANCE = new GreeterClient();
  }

  private GreeterClient() {
    //channel = ManagedChannelBuilder.forAddress(host, port)
    //        .usePlaintext()
    //        .build();

    String target = "zookeeper:///" + GreeterGrpc.SERVICE_NAME;

    channel = ManagedChannelBuilder.forTarget(target)
            .usePlaintext()
            .build();

    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }


  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public void greet() {
    try {
      int no = 100;
      String name = "Alice";
      boolean sex = false;// true:male,false:female
      double salary = 6000.0;
      String desc = "我爱夏天";

      GreeterRequest request = GreeterRequest.newBuilder()
              .setNo(no)
              .setName(name)
              .setSex(sex)
              .setSalary(salary)
              .setDesc(desc)
              .build();

      GreeterReply reply = blockingStub.sayHello(request);

      logger.info(String.valueOf(reply.getSuccess()));
      logger.info(reply.getMessage());
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * main
   */
  public static void main(String[] args) throws Exception {
    GreeterClient client = GreeterClient.getInstance();

    long count = -1;
    long interval = 9000L;// 时间单位为毫秒
    long LOOP_NUM = 10;

    while (true) {
      count++;
      if (count >= LOOP_NUM) {
        break;
      }

      client.greet();
      TimeUnit.MILLISECONDS.sleep(interval);
    }

    client.shutdown();
  }
}
