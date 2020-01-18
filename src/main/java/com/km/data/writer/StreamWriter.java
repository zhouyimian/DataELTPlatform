
package com.km.data.writer;


import com.km.data.common.element.Record;
import com.km.data.common.util.Configuration;
import com.km.data.core.transport.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class StreamWriter extends Writer {
    

    public static class Job extends Writer.Job {
        private static final Logger LOG = LoggerFactory
                .getLogger(Job.class);

        private Configuration originalConfig;

        public Job(Configuration configuration) {
            super(configuration);
            this.originalConfig = super.getConfiguration();
        }

        @Override
        public void init() {

        }

        @Override
        public void prepare() {

        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }


        @Override
        public List<Configuration> split(int mandatoryNumber) {
            List<Configuration> writerSplitConfigs = new ArrayList<Configuration>();
            for (int i = 0; i < mandatoryNumber; i++) {
                writerSplitConfigs.add(this.originalConfig);
            }

            return writerSplitConfigs;
        }
    }

    public static class Task extends Writer.Task {
        private static final Logger LOG = LoggerFactory
                .getLogger(Task.class);

        private Configuration writerSliceConfig;

        public Task(Configuration configuration) {
            super(configuration);
            this.writerSliceConfig = this.getConfiguration();
        }

        public void startWrite(Channel channel) {
            if(channel!=null){
                Record record = null;
                while ((record = channel.remove())!=null){
                    System.out.println(record);
                }
            }
        }

        @Override
        public void init() {

        }

        @Override
        public void prepare() {

        }

        @Override
        public void post() {

        }

        @Override
        public void destroy() {

        }

    }
}
