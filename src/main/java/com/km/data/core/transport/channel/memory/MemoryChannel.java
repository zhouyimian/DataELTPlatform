package com.km.data.core.transport.channel.memory;


import com.km.data.common.element.Record;
import com.km.data.common.util.Configuration;
import com.km.data.core.transport.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 内存Channel的具体实现，底层其实是一个ArrayList
 */
public class MemoryChannel extends Channel {
    private int bufferSize = 0;


    private List<Record> list = null;

    public MemoryChannel(Configuration configuration) {
        super(configuration);
        list = new ArrayList<>();
    }

    @Override
    public void add(Record record) {
        this.list.add(record);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Record get(int index) {
        if(index<list.size()&&index>=0)
            return list.get(index);
        return null;
    }

    @Override
    public Record remove(int index) {
        Record record = null;
        if(index<list.size()&&index>=0)
            record = list.remove(index);
        return record;
    }

    @Override
    public Record remove() {
        if(list.size()!=0)
            return list.remove(0);
        return null;
    }
}
