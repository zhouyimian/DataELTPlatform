package com.km.data.core.transport.channel;


import com.km.data.common.element.Record;
import com.km.data.common.util.Configuration;
import com.km.data.core.statistics.communication.Communication;
import com.km.data.core.util.container.CoreConstant;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;


public abstract class Channel {
    public Long totalBytes = 0L;
    protected int byteCapacity;
    protected volatile boolean isClosed = false;
    protected Configuration configuration;



    public Channel(final Configuration configuration) {
        //channel的queue里默认record为1万条。原来为512条
        int capacity = configuration.getInt(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_CAPACITY, 2048);

        if (capacity <= 0) {
            throw new IllegalArgumentException(String.format(
                    "通道容量[%d]必须大于0.", capacity));
        }
        //channel的queue默认大小为8M，原来为64M
        this.byteCapacity = configuration.getInt(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_CAPACITY_BYTE, 8 * 1024 * 1024);
        this.configuration = configuration;
    }

    public abstract void add(Record record);

    public abstract int size();

    public void close() {
        this.isClosed = true;
    }

    public void open() {
        this.isClosed = false;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public int getByteCapacity() {
        return byteCapacity;
    }

    public void setByteCapacity(int byteCapacity) {
        this.byteCapacity = byteCapacity;
    }
    public abstract Record get(int index);

    public abstract Record remove(int index);

    public abstract Record remove();

    public Long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(Long totalBytes) {
        this.totalBytes = totalBytes;
    }
}
