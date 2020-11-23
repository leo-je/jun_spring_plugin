/**
 * Copyright (c) 2015-2017, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.core.rpc.zbus;

import io.zbus.mq.Broker;
import io.zbus.rpc.RpcConfig;
import io.zbus.rpc.RpcInvoker;
import io.zbus.rpc.bootstrap.mq.ClientBootstrap;
import io.zbus.rpc.transport.mq.RpcMessageInvoker;
import io.zbus.transport.ServerAddress;

/**
 * @author Wujun
 * @version V1.0
 * @Package io.jboot.core.rpc.zbus
 */
public class JbootClientBootstrap extends ClientBootstrap {

    public <T> T serviceObtain(Class<T> serviceClass, String group, String version) {

        if (broker == null) {
            String token = producerConfig.getToken();
            if (token != null) {
                for (ServerAddress address : brokerConfig.getTrackerList()) {
                    if (address.getToken() == null) {
                        address.setToken(token);
                    }
                }
            }
            broker = new Broker(brokerConfig);
        }
        producerConfig.setBroker(broker);
        RpcMessageInvoker messageInvoker = new RpcMessageInvoker(producerConfig, this.topic);

        RpcConfig rpcConfig = new RpcConfig();
        rpcConfig.setModule(ZbusKits.buildModule(serviceClass, group, version));
        rpcConfig.setMessageInvoker(messageInvoker);

        RpcInvoker rpcInvoker = new RpcInvoker(rpcConfig);
        rpcInvoker.getCodec().setRequestTypeInfo(requestTypeInfo);

        return rpcInvoker.createProxy(serviceClass, rpcConfig);
    }


//    public RpcInvoker invoker(Class serviceClass, String group, String version) {
//        if (broker == null) {
//            String token = producerConfig.getToken();
//            if (token != null) {
//                for (ServerAddress address : brokerConfig.getTrackerList()) {
//                    if (address.getToken() == null) {
//                        address.setToken(token);
//                    }
//                }
//            }
//            broker = new Broker(brokerConfig);
//        }
//        producerConfig.setBroker(broker);
//        RpcMessageInvoker messageInvoker = new RpcMessageInvoker(producerConfig, this.topic);
//
//        RpcConfig rpcConfig = new RpcConfig();
//        rpcConfig.setModule(ZbusKits.buildModule(serviceClass, group, version));
//        rpcConfig.setMessageInvoker(messageInvoker);
//
//        RpcInvoker rpcInvoker = new RpcInvoker(rpcConfig);
//        rpcInvoker.getCodec().setRequestTypeInfo(requestTypeInfo);
//
//        return rpcInvoker;
//    }


}
