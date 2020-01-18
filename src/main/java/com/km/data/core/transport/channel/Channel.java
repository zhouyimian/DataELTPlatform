package com.km.data.core.transport.channel;



import com.km.data.common.element.Record;

import java.util.List;


public abstract class Channel {
    public List<Record> list;
    public abstract void add(Record record);

    public abstract Record remove();

    public abstract Record remove(int index);

    public abstract int size();

    public abstract Record get(int index);

    public abstract void set(int index, Record record);

}
