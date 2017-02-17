//package com.sina.hbase_start;
package com.sina.data.bigmonitor.web;
import com.sina.data.bigmonitor.conf.BigMonitorConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

public class TestGet extends Configured implements Tool {

	public int run(String[] args) throws IOException {

		String table_name="ns_hadoopadmin:BigMonitorHostConfTable";
		Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum","10.39.6.85");
		config.set("hbase.zookeeper.property.clientPort", "2181");
		setConf(HBaseConfiguration.create(config));
		Connection connection = null;
		Table table = null;
		TableName TABLE_NAME=TableName.valueOf(table_name);
		connection = ConnectionFactory.createConnection(getConf());
		table = connection.getTable(TABLE_NAME);
		Get get = new Get(Bytes.toBytes("10.39.0.115"));
		Result r = table.get(get);

		NavigableMap<byte[], byte[]> m = r.getFamilyMap(Bytes
				.toBytes(BigMonitorConstants.HostConfTableCF1));
		if (m.size() > 0)
		{
			List<String> metricNames = new ArrayList<String>();
			for (byte[] k : m.keySet()) {
				Long id = Long.parseLong(Bytes.toString(k));
				System.out.println("testhbase"+id);
				System.out.println("miaomiaomiao"+m.get(id));

			}

		}

         table.close();
		return 0;
	}


	public static void main(String[] argv) throws Exception {
		int ret = ToolRunner.run(new TestGet(), argv);
		System.exit(ret);
	}
}
