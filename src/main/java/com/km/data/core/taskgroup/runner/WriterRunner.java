package com.km.data.core.taskgroup.runner;

import com.km.data.common.plugin.AbstractTaskPlugin;
import com.km.data.common.plugin.RecordReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jingxing on 14-9-1.
 * <p/>
 * 单个slice的writer执行调用
 */
public class WriterRunner extends AbstractRunner implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(WriterRunner.class);

    private RecordReceiver recordReceiver;

    public void setRecordReceiver(RecordReceiver receiver) {
        this.recordReceiver = receiver;
    }

    public WriterRunner(AbstractTaskPlugin abstractTaskPlugin) {
        super(abstractTaskPlugin);
    }

    @Override
    public void run() {
    }

    public void shutdown(){
        recordReceiver.shutdown();
    }
}
