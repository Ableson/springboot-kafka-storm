/**
 * 
 */
package com.pancm.storm.bolt;

import com.alibaba.fastjson.JSON;
import com.pancm.constant.Constants;
import com.pancm.pojo.User;
import com.pancm.service.UserService;
import com.pancm.util.GetSpringBean;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Title: InsertBolt
 * @Description: 
 * 写入数据的bolt
 * storm提供了两种bolt，BasicBolt和RichBolt，
 * RichBolt在执行execute后要手动提交ack或者fail，
 * BasicBolt在execute执行后会自动提交ack，但是只对FailedException异常捕获并自动执行fail方法，其他异常需自己处理。
 */
public class InsertBolt extends BaseRichBolt{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6542256546124282695L;

		
		private static final Logger logger = LoggerFactory.getLogger(InsertBolt.class);


	    private UserService userService;
		

		@SuppressWarnings("rawtypes")
		@Override
		/**
		 * 在Bolt启动前执行，提供Bolt启动环境配置的入口 一般对于不可序列化的对象进行实例化。
		 */
		public void prepare(Map map, TopologyContext arg1, OutputCollector collector) {
			userService=GetSpringBean.getBean(UserService.class);
		}
	  
		   
		@Override
		public void execute(Tuple tuple) {
			//tuple.getString(0) 相同
			String msg=tuple.getStringByField(Constants.FIELD);
			try{
				List<User> listUser =JSON.parseArray(msg,User.class);
				//移除age小于10的数据
				if(listUser!=null&&listUser.size()>0){
					Iterator<User> iterator = listUser.iterator();
					 while (iterator.hasNext()) {
						 User user = iterator.next();
						 if (user.getAge()<10) {
							 logger.warn("Bolt移除的数据:{}",user);
							 iterator.remove();
						 }
					 }
					if(listUser!=null&&listUser.size()>0){
						userService.insertBatch(listUser);
					}
				}
			}catch(Exception e){
				logger.error("Bolt的数据处理失败!数据:{}",msg,e);
			}
		}

		
		/**
	     * cleanup是IBolt接口中定义,用于释放bolt占用的资源。
	     * Storm在终止一个bolt之前会调用这个方法。
		 */
		@Override
		public void cleanup() {
			logger.info("bolt中调用清理cleanup");
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer arg0) {
			logger.info("bolt中调用declareOutputFields:"+JSON.toJSONString(arg0));
		}
		
	
}
