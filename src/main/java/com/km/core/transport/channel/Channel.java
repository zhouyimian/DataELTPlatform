package com.km.core.transport.channel;



import com.km.common.element.Record;


public abstract class Channel {

    public abstract void add(Record record);

    public abstract Record remove();


}
