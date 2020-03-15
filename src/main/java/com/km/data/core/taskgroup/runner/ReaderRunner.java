package com.km.data.core.taskgroup.runner;

import com.km.data.common.plugin.AbstractTaskPlugin;
import com.km.data.common.plugin.RecordSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jingxing on 14-9-1.
 * <p/>
 * 单个slice的reader执行调用
 */
public class ReaderRunner extends AbstractRunner implements Runnable {

    private static final Logger LOG = LoggerFactory
            .getLogger(ReaderRunner.class);

    private RecordSender recordSender;

    public void setRecordSender(RecordSender recordSender) {
        this.recordSender = recordSender;
    }

    public ReaderRunner(AbstractTaskPlugin abstractTaskPlugin) {
        super(abstractTaskPlugin);
    }

    @Override
    public void run() {
    }

    public void shutdown(){
        recordSender.shutdown();
    }
}
