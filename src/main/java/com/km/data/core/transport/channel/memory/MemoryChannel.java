package com.km.data.core.transport.channel.memory;


import com.km.data.common.element.Record;
import com.km.data.core.transport.channel.Channel;

import java.util.ArrayList;


/**
 * 内存Channel的具体实现，底层其实是一个ArrayBlockingQueue
 *
 */
public class MemoryChannel extends Channel {

    public MemoryChannel(){
        this.list = new ArrayList<>();
    }

    @Override
    public void add(Record record){
        list.add(record);
    }

    @Override
    public Record remove() {
        if(list.size()!=0)
            return list.remove(0);
        return null;
    }

    public Record remove(int index){
        return list.remove(index);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Record get(int index) {
        if(list.size()>index)
            return list.get(index);
        return null;
    }

    @Override
    public void set(int index, Record record) {
        list.set(index,record);
    }
}
