package com.example.rpccommon.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.concurrent.atomic.AtomicInteger;


/**
 * @Author Cbc
 * @DateTime 2024/12/7 19:45
 * @Description
 */
@Data
@AllArgsConstructor
public class Request extends RpcMsg{

    public Request(){

    }

    public Request(Builder builder){
        this.interfaceName = builder.interfaceName;
        this.args = builder.args;
        this.methodName = builder.methodName;
        perfectRequest(this);
    }
    private final static AtomicInteger generateId = new AtomicInteger(0);

    private Integer serializationId;

    private String interfaceName;

    private String methodName;

    private Object[] args;

   private String[] argsClassNames;//json序列化必需

    //生成唯一id, 辨别唯一请求
    private static Integer generateId(){
        return generateId.incrementAndGet();
    }

    //完善Request的参数
    private static void perfectRequest(Request request){
        request.serializationId = generateId();
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
