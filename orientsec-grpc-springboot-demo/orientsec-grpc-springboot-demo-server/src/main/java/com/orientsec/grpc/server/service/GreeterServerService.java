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
