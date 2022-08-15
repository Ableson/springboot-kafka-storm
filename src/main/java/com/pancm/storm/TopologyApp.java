package com.pancm.storm;

import com.pancm.constant.Constants;
import com.pancm.storm.bolt.InsertBolt;
import com.pancm.storm.spout.KafkaInsertDataSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @Title: TopologyApp
 * @Description: TopologyApp
 * @Version:1.0.0
 * @author pancm
 * @date 2018年5月8日
 */

public class TopologyApp {
	private  final Logger logger = LoggerFactory.getLogger(TopologyApp.class);

	public  void runStorm(String[] args) {
		// 定义一个拓扑
		TopologyBuilder builder = new TopologyBuilder();
		// 设置1个Executeor(线程)，默认一个
		//设置一个id为KAFKA_SPOUT，并行度为1的kafkaInsertDataSpout对象
		builder.setSpout(Constants.KAFKA_SPOUT, new KafkaInsertDataSpout(), 1);
		// shuffleGrouping:表示是随机分组 对id为KAFKA_SPOUT的组件随机分组
		// 设置1个Executeor(线程)，和两个task
		//setNumTasks的数目, 可以不配置, 默认和executor1:1, 也可以通过setNumTasks()配置
		builder.setBolt(Constants.INSERT_BOLT, new InsertBolt(), 1).setNumTasks(2).shuffleGrouping(Constants.KAFKA_SPOUT);
		//对id为KAFKA_SPOUT的组件按Constants.FIELD进行分组
//		builder.setBolt(Constants.INSERT_BOLT, new InsertBolt(), 1).setNumTasks(1).fieldsGrouping(Constants.KAFKA_SPOUT,new Fields(Constants.FIELD));
		Config conf = new Config();
		//设置一个应答者 默认情况下有几个worker就有几个acker
		conf.setNumAckers(1);
		//设置一个work 设置让集群启动2个worker来执行这个作业
		conf.setNumWorkers(3);
		try {
			// 有参数时，表示向集群提交作业，并把第一个参数当做topology名称
			// 没有参数时，本地提交
			if (args != null && args.length > 0) { 
				logger.info("运行远程模式");
				StormSubmitter.submitTopology(args[0], conf, builder.createTopology());
			} else {
				// 启动本地模式
				logger.info("运行本地模式");
				LocalCluster cluster = new LocalCluster();
				cluster.submitTopology("TopologyApp", conf, builder.createTopology());
			}
		} catch (Exception e) {
			logger.error("storm启动失败!程序退出!",e);
			System.exit(1);
		}

		logger.info("storm启动成功...");
	}
}
