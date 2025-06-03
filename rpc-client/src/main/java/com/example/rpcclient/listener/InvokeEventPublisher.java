package com.example.rpcclient.listener;

import java.util.List;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 17:31
 * @Description
 */
public class InvokeEventPublisher {

    public static void publishEvent(InvokeEvent event){
        List<InvokeEventListener> listenerList = InvokeEventListener.getListenerListByEvent(event);
       if(listenerList != null){
           for (InvokeEventListener listener : listenerList) {
               listener.listen(event);
           }
       }
    }
}
