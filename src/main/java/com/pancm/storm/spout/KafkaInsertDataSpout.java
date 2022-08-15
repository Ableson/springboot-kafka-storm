package com.pancm.storm.spout;

import com.alibaba.fastjson.JSON;
import com.pancm.config.ApplicationConfiguration;
import com.pancm.constant.Constants;
import com.pancm.pojo.User;
import com.pancm.util.GetSpringBean;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 
* @Title: KafkaInsertDataSpout
* @Description: 
* 从kafka获取新增数据
* @Version:1.0.0  
 */
public class KafkaInsertDataSpout extends BaseRichSpout{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2548451744178936478L;
	
	private static final Logger logger = LoggerFactory.getLogger(KafkaInsertDataSpout.class);
	
	private SpoutOutputCollector collector;
	
	private KafkaConsumer<String, String> consumer;
	
	private ConsumerRecords<String, String> msgList;


	private ApplicationConfiguration app;
	
	
	
	@SuppressWarnings("rawtypes")
	@Override
	/**
	 * open()方法中是在ISpout接口中定义，在Spout组件初始化时被调用。 有三个参数: 1.Storm配置的Map;
	 * 2.topology中组件的信息; 3.发射tuple的方法;
	 */
	public void open(Map map, TopologyContext arg1, SpoutOutputCollector collector) {
		app=GetSpringBean.getBean(ApplicationConfiguration.class);
		kafkaInit();
		this.collector = collector;
	}
	
	
	@Override
	/**
	 * nextTuple()方法是Spout实现的核心。 也就是主要执行方法，用于输出信息,通过collector.emit方法发射。
	 */
	public void nextTuple() {
		for (;;) {
			try {
				//例如poll(5000)： 如果拉到数据的话 会立即放回；如果拉不到数据的话，这个是最长的等待时间；如果一直没有数据的话，每5s拉一次返回一次，有数据就立即返回再拉
				msgList = consumer.poll(100);
				if (null != msgList && !msgList.isEmpty()) {
					String msg = "";
					List<User> list=new ArrayList<User>();
					for (ConsumerRecord<String, String> record : msgList) {
						// 原始数据
						msg = record.value();
						if (null == msg || "".equals(msg.trim())) continue;
						try{
							list.add(JSON.parseObject(msg, User.class));
						}catch(Exception e){
							logger.error("数据格式不符!数据:{}",msg);
							continue;
						}
				     } 
					logger.info("Spout发射的数据:"+list);
					//发送到bolt中  解释：values中的参数数量对应declareOutputFields中的fields数量，供下游bolt使用
					collector.emit(new Values(JSON.toJSONString(list)));
					consumer.commitAsync();
				}else{
					TimeUnit.SECONDS.sleep(3);
					logger.info("未拉取到数据...");
				}
			} catch (Exception e) {
				logger.error("消息队列处理异常!", e);
				try {TimeUnit.SECONDS.sleep(10);} catch (InterruptedException e1) {logger.error("暂停失败!",e1);}
			}
		}
	}
	
	
	@Override
	/**
	 * declareOutputFields是在IComponent接口中定义，用于声明数据格式。 即输出的一个Tuple中，包含几个字段。
	 */
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(Constants.FIELD));
	}
	
	/**
	 * 初始化kafka配置
	 */
	private void kafkaInit(){
		Properties props = new Properties();
        props.put("bootstrap.servers", app.getServers());  
        props.put("max.poll.records", app.getMaxPollRecords());
        props.put("enable.auto.commit", app.getAutoCommit());
        props.put("group.id", app.getGroupId());
        props.put("auto.offset.reset", app.getCommitRule());
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);
        String topic=app.getTopicName();
    	this.consumer.subscribe(Arrays.asList(topic));
    	logger.info("消息队列[" + topic + "] 开始初始化...");
	}
}
