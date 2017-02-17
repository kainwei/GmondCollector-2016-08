package com.sina.data.bigmonitor.web;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Created by kain on 2016/12/25.
 */
public class MsgHtmlLogger {
    public static String HtmlExtractor(String start_time, String end_time, String type) {
        long start = Long.parseLong(start_time);
        long end = Long.parseLong(end_time);
        if (start > end) {
            return "";
        }

        String item = type;

        int threshold = 85;
        String itemMsg = "Abormal URate Cpu Info In Period Time: ";
        if (type.equals("sys")) {
            threshold = 25;
            itemMsg = "Abormal URate cpu_systime Info In Period Time: ";
        } else if (type.equals("jvm")) {
            threshold = 50;
            itemMsg = "Abormal URate jvm_Urate Info In Period Time: ";
        } else if (type.equals("cpu_low")) {
            threshold = 50;
            itemMsg = "Abormal URate low_cpu_Urate Info In Period Time: ";
        }
        //String filePath="/Users/weizhonghui/Downloads/work/2016-12-25/";
        String filePath = "/usr/home/boyan5/monitor/HadoopMaster2.4_V3/bin/" + item + "_log/hour_log/";
        long startFileName = start - (start % 3600) + 3600;
        long endFileName = end - (end % 3600) + 3600;
        StringBuffer sb = new StringBuffer();
        Map<String, Integer> ipTimeMap = new HashMap<String, Integer>();
        int hours = 0;
        for (long fn = startFileName; fn <= endFileName; fn += 3600) {
            String fileName = String.valueOf(fn);
            //System.out.println(fileName);
            File file = new File(filePath + fileName);
            if (!file.exists()) {
                //System.out.println(file.getName() + " not exist");
                break;
            }
            hours += 60;
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String tmpString = null;
                while ((tmpString = reader.readLine()) != null) {
                    String ip = tmpString.split(":")[0];
                    if (ipTimeMap.containsKey(ip)) {
                        int newTime = ipTimeMap.get(ip) + 1;
                        ipTimeMap.put(ip, newTime);
                    } else {
                        ipTimeMap.put(ip, 1);
                    }

                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }

        }
        final int allTime = hours;
        if (ipTimeMap.size() != 0) {
            PriorityQueue<Map.Entry<String, Integer>> pq =
                    new PriorityQueue(ipTimeMap.size(), new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            if ((100 * o1.getValue() / allTime) <= (100 * o2.getValue() / allTime)) {
                                return 1;
                            } else {
                                return -1;
                            }


                        }
                    });
            sb.append(itemMsg + "<br>" + "\n");
            sb.append("<table border=1>" + "\n");
            sb.append("<tr><td>ip</td><td>last_time(Min)</td><td>Percent(last_time/period)</td></tr>" + "\n");
            for (Map.Entry<String, Integer> entry : ipTimeMap.entrySet()) {
                pq.offer(entry);
                /*String ip = entry.getKey();
                int last_time = entry.getValue();
                int percent = 100*last_time/hours;
                if(percent > threshold) {
                    sb.append("<tr><td>" + ip + "</td><td>" + last_time + "</td><td>" + percent  + " %</td></tr>" + "\n" );
                }*/
            }
            for (int num = 0; num < 20; num++) {
                Map.Entry<String, Integer> biggest = pq.poll();
                String ip = biggest.getKey();
                int last_time = biggest.getValue();
                int percent = 100 * last_time / hours;
                sb.append("<tr><td>" + ip + "</td><td>" + last_time + "</td><td>" + percent + " %</td></tr>" + "\n");
            }

            sb.append("</table>");
        }
        return sb.toString();

    }

    public static void main(String[] args) {
        String start_time = args[0];
        String end_time = args[1];
        String type = args[2];
        String res = HtmlExtractor(start_time, end_time, type);
        System.out.println(res);
    }
}