package com.example.rpccommon.message;

import com.example.rpccommon.constants.RpcMsgTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:45
 * @Description 建造者模式
 */
@Data
@AllArgsConstructor
public class Request implements Serializable {

    private static AtomicLong createId = new AtomicLong(0);

    public Request(){

    }

    public Request(Builder builder){
        this.interfaceName = builder.interfaceName;
        this.args = builder.args;
        this.methodName = builder.methodName;
        this.rqId = createId.getAndIncrement();
        this.attachment = new HashMap<>();
        perfectRequest(this);
    }

    private transient Integer retryNum;//重试次数

    private transient Byte serializeCode;

    private transient Long rqId;

    private boolean isAsync;

    private String interfaceName;

    private String methodName;

    private Object[] args;

   private String[] argsClassNames;//json序列化必需

    private Map<String, Object> attachment;




    //完善Request的参数
    private static void perfectRequest(Request request){

        request.retryNum = 0;
        Object[] a = request.args;
        if(a != null){
            request.argsClassNames = new String[a.length];
            for (int i = 0; i < a.length; i++) {
                request.argsClassNames[i] = a[i].getClass().getName();
            }
        }
    }

    public static Builder builder(){
        return new Builder();
    }



    public static class Builder{
        private String interfaceName;

        private String methodName;

        private Object[] args;

        public Builder interfaceName(String interfaceName){
            this.interfaceName = interfaceName;
            return this;
        }

        public Builder methodName(String methodName){
            this.methodName = methodName;
            return this;
        }

        public Builder args(Object[] args){
            this.args = args;
            return this;
        }

        public Request build(){
            return new Request(this);
        }
    }
}
