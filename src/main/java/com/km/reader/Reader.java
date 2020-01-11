package com.km.reader;


import com.km.common.util.Configuration;
import com.km.core.transport.channel.Channel;

import java.sql.SQLException;
import java.util.List;

/**
 * 每个Reader插件在其内部内部实现Job、Task两个内部类。
 * 
 * 
 * */
public abstract class Reader {


	public static abstract class Job{
        private Configuration configuration;

        public abstract List<Configuration> split(int adviceNumber);

		public Job(Configuration configuration){
		    this.configuration = configuration;
        }

        public Configuration getConfiguration() {
            return configuration;
        }
    }

	public static abstract class Task{
        private Configuration configuration;

        public Task(Configuration configuration){
            this.configuration = configuration;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public abstract void startRead(Channel channel) throws SQLException;
	}
}
