package com.orientsec.grpc.server.service;

import com.orientsec.demo.GreeterGrpc;
import com.orientsec.demo.GreeterReply;
import com.orientsec.demo.GreeterRequest;
import com.orientsec.grpc.server.config.GRpcService;
import io.grpc.stub.StreamObserver;

@GRpcService
public class GreeterServerService extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(GreeterRequest request, StreamObserver<GreeterReply> responseObserver) {
        int no = request.getNo();
        String name = request.getName();
        boolean sex = request.getSex();// true:male,false:female
        double salary = request.getSalary();
        String desc = request.getDesc();

        String appellation;
        if (sex) {
            appellation = "Mr " + name;
        } else {
            appellation = "Miss " + name;
        }

        GreeterReply reply = GreeterReply.newBuilder()
            .setSuccess(true)
            .setMessage(appellation + ", well done.(" + desc + ")")
            .setNo(no + 100)
            .setSalary(salary * 1.2)
            .setTotal(System.currentTimeMillis())
            .build();

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
