package com.km.data.writer;



import com.km.data.common.util.Configuration;
import com.km.data.core.transport.channel.Channel;

import java.sql.SQLException;
import java.util.List;

/**
 * 每个Writer插件需要实现Writer类，并在其内部实现Job、Task两个内部类。
 * 
 * 
 * */
public abstract class Writer {
	/**
	 * 每个Writer插件必须实现Job内部类
	 */
    public static abstract class Job{
        private Configuration configuration;

		public abstract List<Configuration> split(int mandatoryNumber);

        public Job(Configuration configuration) {
            this.configuration = configuration;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public abstract void init();

        public abstract void prepare();

        public abstract void post();

        public abstract void destroy();
    }

	/**
	 * 每个Writer插件必须实现Task内部类
	 */
	public abstract static class Task{

        private Configuration configuration;

        public Task(Configuration configuration){
            this.configuration = configuration;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public abstract void startWrite(Channel channel) throws SQLException;

        public abstract void init();

        public abstract void prepare();

        public abstract void post();

        public abstract void destroy();

	}
}
