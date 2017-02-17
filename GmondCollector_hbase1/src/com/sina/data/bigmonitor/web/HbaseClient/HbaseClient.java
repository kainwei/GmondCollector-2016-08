package com.sina.data.bigmonitor.web.HbaseClient;

//import com.sina.data.bigmonitor.web.OperRequset.HbaseFilter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HbaseClient extends Configured implements Tool {
    private int record_num_capacity = 10000;
    private Connection conn;
    private volatile static HbaseClient hbaseclient;


    private HbaseClient(){
        Configuration config = HBaseConfiguration.create();
        //config.set("hbase.zookeeper.quorum", "10.39.3.78");
        config.set("hbase.zookeeper.quorum", "10.39.6.85,10.39.6.86,10.39.6.87");
        config.set("hbase.zookeeper.property.clientPort", "2181");


        setConf(HBaseConfiguration.create(config));
        try {
            conn= ConnectionFactory.createConnection(getConf());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static HbaseClient getHc(){
        if(hbaseclient == null) {
            synchronized (HbaseClient.class) {
                if(hbaseclient == null ) {
                    hbaseclient = new HbaseClient();
                }
            }
        }
        return hbaseclient;
    }


    public Table tableConn(String table_name) throws IOException {
        Table table = null;
        Connection connection = conn;
        //String table_name="ns_hadoopadmin:BigMonitorMetricDataClusterDay";
        TableName TABLE_NAME = TableName.valueOf(table_name);
        table = connection.getTable(TABLE_NAME);



        return table;

    }

    public String getRes(String table_name, FilterList filterlist) {
        Table table = null;
        try {
            table = tableConn(table_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scan scan = new Scan();
        scan.setFilter(filterlist);
        ResultScanner rs = null ;
        try {
            rs = table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuffer res = new StringBuffer();

        int count = 0;
        for (Result r : rs) {
            for (KeyValue kv : r.raw()) {
                res.append(String.format("row:%s, family:%s, qualifier:%s, qualifiervalue:%s, timestamp:%s.",
                        Bytes.toString(kv.getRow()),
                        Bytes.toString(kv.getFamily()),
                        Bytes.toString(kv.getQualifier()),
                        Bytes.toString(kv.getValue()),
                        kv.getTimestamp())+"\n");
                count++;
                //		if(count>=record_num_capacity){
                //			break;
                //		}

            }
            //	if(count>=record_num_capacity){
            //		break;
            //	}
        }
        try {
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res.toString();






    }

    public String GetRangeRes(String table_name, String row, String rid, String start, String end) {
        String startStr = row + "#" + rid + "#" + start;
        String endStr = row + "#" + rid + "#" + end;
        Table table = null;
        try {
            table = tableConn(table_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startStr));
        scan.setStopRow(Bytes.toBytes(endStr));
        System.out.println(startStr);
        System.out.println(endStr);
        //scan.setStartRow(Bytes.toBytes("hadoop-2.4.0_master#100005#1472038860"));
        //scan.setStopRow(Bytes.toBytes("hadoop-2.4.0_master#100005#1472114700"));

        //scan.setStartRow(Bytes.toBytes("hadoop-2.4.0_master#100005#1472038860"));
        //scan.setStopRow(Bytes.toBytes("hadoop-2.4.0_master#100005#1472114700"));
        ResultScanner rs = null ;
        try {
            rs = table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuffer res = new StringBuffer();
        //System.out.println("amina's log rs size is " + rs.size() );

        int count = 0;
        for (Result r : rs) {
            for (KeyValue kv : r.raw()) {
                res.append(String.format("row:%s, family:%s, qualifier:%s, qualifiervalue:%s, timestamp:%s.",
                        Bytes.toString(kv.getRow()),
                        Bytes.toString(kv.getFamily()),
                        Bytes.toString(kv.getQualifier()),
                        Bytes.toString(kv.getValue()),
                        kv.getTimestamp())+"\n");
                count++;
                if(count>=record_num_capacity){
                    break;
                }

            }
            if(count>=record_num_capacity){
                break;
            }
        }
        try {
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("amina's log "+ count + " capacity is " + record_num_capacity );

        return res.toString();
    }


    public String CeilScan(String table_name, String host, String metricid, String start) {
        String startRow = host + "#" + metricid + "#" + start;
        String end = String.valueOf(Long.valueOf(start) - 1800);
        String endRow = host + "#" + metricid + "#" + end;
        return LimitScan(table_name, startRow, endRow, 1, true);
    }

    public String LimitScan(String table_name, String startRow, String endRow, int limit, boolean isReversed) {

        //String endStr = row + "#" + rid + "#" + end;
        Table table = null;
        try {
            table = tableConn(table_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scan scan = new Scan();
        scan.setReversed(isReversed);
        scan.setStartRow(Bytes.toBytes(startRow));
        scan.setStopRow(Bytes.toBytes(endRow));
        //scan.setMaxResultSize(limit);
        //scan.setMaxResultsPerColumnFamily(limit);
        scan.setFilter(new PageFilter(limit));
        //System.out.println(startStr);
        //System.out.println(endStr);
        //scan.setStartRow(Bytes.toBytes("hadoop-2.4.0_master#100005#1472038860"));
        //scan.setStopRow(Bytes.toBytes("hadoop-2.4.0_master#100005#1472114700"));

        //scan.setStartRow(Bytes.toBytes("hadoop-2.4.0_master#100005#1472038860"));
        //scan.setStopRow(Bytes.toBytes("hadoop-2.4.0_master#100005#1472114700"));
        ResultScanner rs = null ;
        try {
            rs = table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //StringBuffer res = new StringBuffer();
        //System.out.println("amina's log rs size is " + rs.size() );
        String res = "0";

        int count = 0;
        for (Result r : rs) {
            for (KeyValue kv : r.raw()) {
                /*
                res.append(String.format("row:%s, family:%s, qualifier:%s, qualifiervalue:%s, timestamp:%s.",
                        Bytes.toString(kv.getRow()),
                        Bytes.toString(kv.getFamily()),
                        Bytes.toString(kv.getQualifier()),
                        Bytes.toString(kv.getValue()),
                        kv.getTimestamp())+"\n");
                */
                res = Bytes.toString(kv.getValue());
                break;

            }
        }
        try {
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("amina's log "+ count + " capacity is " + record_num_capacity );

        return res;
    }

    public String GetRowId(String table_name, String row) {
        Table table = null;
        try {
            table = tableConn(table_name);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Get get = new Get(Bytes.toBytes(row));
        Result rs = null ;
        try {
            rs = table.get(get);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = null;
        for (KeyValue kv : rs.raw()) {
            if(Bytes.toString(kv.getQualifier()).equals("id")){
                                /*
                		res = String.format("row:%s, family:%s, qualifier:%s, qualifiervalue:%s, timestamp:%s.",
                        	Bytes.toString(kv.getRow()),
                        	Bytes.toString(kv.getFamily()),
                        	Bytes.toString(kv.getQualifier()),
                        	Bytes.toString(kv.getValue()),
                        	kv.getTimestamp())+"\n";
                                */
                res = Bytes.toString(kv.getValue());
                break;
            }

        }
        try {
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }



    public List<List<Object>> GetRangeList(String table_name, String row, String rid, String start, String end) {
        String startStr = row + "#" + rid + "#" + start;
        String endStr = row + "#" + rid + "#" + end;
        Table table = null;
        try {
            table = tableConn(table_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scan scan = new Scan();
        // scan.setFilter(filterlist);
        scan.setStartRow(Bytes.toBytes(startStr));
        scan.setStopRow(Bytes.toBytes(endStr));
        ResultScanner rs = null ;
        try {
            rs = table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List res = new ArrayList<List<Object>>();

        int count = 0;
        for (Result r : rs) {
            for (KeyValue kv : r.raw()) {
                List tmplist = new ArrayList<Object>();
                //tmplist.add(TimeStampToUtc.TsToUtc(kv.getTimestamp()));
                //tmplist.add(Bytes.toString(kv.getValue()));
                tmplist.add(kv.getTimestamp());
                tmplist.add(Double.valueOf(Bytes.toString(kv.getValue())));
                res.add(tmplist);
                                /*
                                res.append(String.format("row:%s, family:%s, qualifier:%s, qualifiervalue:%s, timestamp:%s.",
                                                Bytes.toString(kv.getRow()),
                                                Bytes.toString(kv.getFamily()),
                                                Bytes.toString(kv.getQualifier()),
                                                Bytes.toString(kv.getValue()),
                                                kv.getTimestamp())+"\n");
                                */
                count++;
                if(count>=record_num_capacity){
                    break;
                }

            }
            if(count>=record_num_capacity){
                break;
            }
        }
        try {
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }



    public String getTimeRangeRes(String table_name, FilterList filterlist, String start, String end) {
        Table table = null;
        try {
            table = tableConn(table_name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scan scan = new Scan();
        scan.setFilter(filterlist);
        try {
            scan.setTimeRange(Long.valueOf(start), Long.valueOf(end));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResultScanner rs = null ;
        try {
            rs = table.getScanner(scan);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuffer res = new StringBuffer();
        int count = 0;
        for (Result r : rs) {
            for (KeyValue kv : r.raw()) {
                res.append(String.format("row:%s, family:%s, qualifier:%s, qualifiervalue:%s, timestamp:%s.",
                        Bytes.toString(kv.getRow()),
                        Bytes.toString(kv.getFamily()),
                        Bytes.toString(kv.getQualifier()),
                        Bytes.toString(kv.getValue()),
                        kv.getTimestamp())+"\n");
                count++;
                if(count >= record_num_capacity){
                    break;
                }
            }
            if(count >= record_num_capacity){
                break;
            }
        }
        try {
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res.toString();






    }


    public static void main(String[] argv) throws Exception {
        //FilterList fl = new HbaseFilter().EqualRowFilterList(argv[1]);
        //String res = new HbaseClient().getRes(argv[0], fl);
        //FilterList fl = new HbaseFilter().RangeRowFilterList(argv[1], argv[2], argv[3], argv[4]);
        //String prefix = argv[1] + "#" + argv[2];
        //FilterList fl = new HbaseFilter().PrefixRowFilterList(prefix);
        //String res = new HbaseClient().getTimeRangeRes(argv[0],fl, argv[3], argv[4]);

        //String res = new HbaseClient().GetRangeRes(argv[0], argv[1], argv[2], argv[3], argv[4]);
        //String res = new HbaseClient().GetRowId(argv[0], argv[1]);
        //String res = new HbaseClient().CeilScan(argv[0],argv[1], argv[2], argv[3]);
        //String res = new HbaseClient().CeilScan(argv[0],argv[1], argv[2], argv[3]);
        String res = getHc().CeilScan(argv[0],argv[1], argv[2], argv[3]);
        System.out.println(res);

    }

    @Override
    public int run(String[] args) throws Exception {
        return 0;
    }
}


