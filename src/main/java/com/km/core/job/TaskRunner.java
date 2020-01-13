package com.km.core.job;

import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;
import com.km.core.transport.channel.memory.MemoryChannel;
import com.km.core.util.container.CoreConstant;
import com.km.reader.MongoDBReader;
import com.km.reader.MysqlReader;
import com.km.reader.Reader;
import com.km.writer.MongoDBWriter;
import com.km.writer.MysqlWriter;
import com.km.writer.Writer;

import java.sql.SQLException;

public class TaskRunner implements Runnable {
    Configuration configuration;
    Reader.Task reader;
    Writer.Task writer;

    Channel channel;



    public TaskRunner(Configuration configuration){
        this.configuration = configuration;
        this.reader = createReader(configuration);
        this.writer = createWriter(configuration);
        this.channel = createChannel();
    }

    private Channel createChannel() {
        return new MemoryChannel();
    }

    private Writer.Task createWriter(Configuration configuration) {
        String writerName = this.configuration.getString(CoreConstant.JOB_WRITER_NAME);
        return new MongoDBWriter.Task(configuration);
    }

    private Reader.Task createReader(Configuration configuration) {
        String readerName = this.configuration.getString(CoreConstant.JOB_READER_NAME);

        return new MongoDBReader.Task(configuration);
    }

    @Override
    public void run() {
        try {
            this.reader.startRead(this.channel);

            this.writer.startWrite(this.channel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
