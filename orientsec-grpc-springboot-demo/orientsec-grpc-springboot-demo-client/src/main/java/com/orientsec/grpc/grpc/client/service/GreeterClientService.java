package com.orientsec.grpc.grpc.client.service;

import com.orientsec.demo.GreeterGrpc;
import com.orientsec.demo.GreeterReply;
import com.orientsec.demo.GreeterRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/***
 * Description:下线
 * @author wangcheng
 * @date 2018/8/10.
 */
@Service
public class GreeterClientService {
    private static final Logger logger = LoggerFactory.getLogger(GreeterClientService.class);

    private ManagedChannel channel;
    private  GreeterGrpc.GreeterBlockingStub blockingStub;

    @PostConstruct
    public void init() {
        String target = "zookeeper:///" + GreeterGrpc.SERVICE_NAME;

        this.channel = ManagedChannelBuilder.forTarget(target)
            .usePlaintext(true)
            .build();

        this.blockingStub = GreeterGrpc.newBlockingStub(channel);


        //test
        while (true) {
            try {
                greet();
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

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

            System.out.println(reply);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
