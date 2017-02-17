<%@ page
		import="com.sina.data.DataProducer.*"
                import="com.sina.data.bigmonitor.web.HbaseClient.*"
%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@page contentType="text/html; charset=GB2312"%>

<%
       // String [] test = {"ns_hadoopadmin:BigMonitorMetricDataClusterHour", "hadoop-2.4.0-master", "100058", "1472115300", "1472122020"};
    /** Example of GetRowId's usage:
      *String metric_name = "yarn.ClusterMetrics.NumActiveNMs";
      *String talbe_name = "ns_hadoopadmin:BigMonitorMetricDataClusterHour";
      *String rowid = new HbaseClient().GetRowId(table_name, metric_name);
      
    **/

    String[] test=new String[5];
    String clusterName=session.getAttribute("cluster").toString();
    String begStr = request.getParameter("startTime");
    String endStr = request.getParameter("endTime");
    SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date1=simpleDateFormat.parse(begStr);
    Date date2=simpleDateFormat.parse(endStr);
    String begStemp = String.valueOf(date1.getTime()).substring(0,10);
    String endStemp = String.valueOf(date2.getTime()).substring(0,10);
      //test[0]="ns_hadoopadmin:BigMonitorMetricDataClusterHour";
      test[0]="ns_hadoopadmin:BigMonitorMetricDataClusterOrig";
      //test[1]= clusterName;
      test[1]= "hadoop-2.4.0-master";
      test[2]="100058";
      //test[3]="1472115300";
      //test[4]="1472122020";
      //test[3]="1472389409";
      //test[3]="1472089409";
      //test[4]="1472393059";
      test[3]=begStemp;
      test[4]=endStemp;
	//String res = GetData.get();
        String res = getDataTest.get(test);

%>
<!DOCTYPE HTML>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<title>Highcharts Example</title>

		<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
		<style type="text/css">
${demo.css}
		</style>
		<script type="text/javascript">
$( function () {
        Highcharts.setOptions({
            global: {
                useUTC: false 
            }
        });
        var data=<%=res%>
        $('#container').highcharts({
            chart: {
                zoomType: 'x'
            },
            title: {
                text: 'metric'
            },
            subtitle: {
                text: document.ontouchstart === undefined ?
                        'Click and drag in the plot area to zoom in' : 'Pinch the chart to zoom in'
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                title: {
                    text: 'Exchange rate'
                }
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                area: {
                    fillColor: {
                        linearGradient: {
                            x1: 0,
                            y1: 0,
                            x2: 0,
                            y2: 1
                        },
                        stops: [
                            [0, Highcharts.getOptions().colors[0]],
                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                        ]
                    },
                    marker: {
                        radius: 2
                    },
                    lineWidth: 1,
                    states: {
                        hover: {
                            lineWidth: 1
                        }
                    },
                    threshold: null
                }
            },

            series: [{
                type: 'area',
                name: 'USD to EUR',
                data: data
            }]
        });

        $('#container2').highcharts({
            chart: {
                zoomType: 'x'
            },
            title: {
                text: 'metric'
            },
            subtitle: {
                text: document.ontouchstart === undefined ?
                        'Click and drag in the plot area to zoom in' : 'Pinch the chart to zoom in'
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                title: {
                    text: 'Exchange rate'
                }
            },
            legend: {
                enabled: false
            },
            plotOptions: {
                area: {
                    fillColor: {
                        linearGradient: {
                            x1: 0,
                            y1: 0,
                            x2: 0,
                            y2: 1
                        },
                        stops: [
                            [0, Highcharts.getOptions().colors[0]],
                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                        ]
                    },
                    marker: {
                        radius: 2
                    },
                    lineWidth: 1,
                    states: {
                        hover: {
                            lineWidth: 1
                        }
                    },
                    threshold: null
                }
            },

            series: [{
                type: 'area',
                name: 'USD to EUR',
                data: data
            }]
        });

});
		</script>
	</head>
	<body>
<script src="./Highcharts-4.2.6/js/highcharts.js"></script>
<script src="./Highcharts-4.2.6/modules/exporting.js"></script>

<div id="container" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
<div id="container2" style="min-width: 310px; height: 400px; margin: 0 auto"></div>

	</body>
</html>

