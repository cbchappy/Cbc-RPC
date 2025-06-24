package com.example.rpcclient.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Cbc
 * @DateTime 2025/5/18 17:33
 * @Description //todo 完善事件监听
 */
public abstract class InvokeEventListener {
    private static final Map<Class<?>, List<InvokeEventListener>> listMap = new ConcurrentHashMap<>();
    public abstract void listen(InvokeEvent event);

    public static  <T extends InvokeEvent> void addListener(InvokeEventListener listener, Class<T> eventClass){
        List<InvokeEventListener> listenerList = listMap.computeIfAbsent(eventClass, k -> new ArrayList<>());
        listenerList.add(listener);
    }

    public static void removeListener(InvokeEventListener listener){
        Class<? extends InvokeEventListener> key = listener.getClass();
        List<InvokeEventListener> listenerList = listMap.get(key);
        if(listenerList != null){
            listenerList.remove(listener);
        }
    }

    public static List<InvokeEventListener> getListenerListByEvent(InvokeEvent event){
        Class<?> aClass = event.getClass();
        return listMap.get(aClass);
    }

}
