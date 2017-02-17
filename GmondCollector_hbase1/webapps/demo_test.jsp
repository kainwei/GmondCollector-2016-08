<%@ page
		import="com.sina.data.DataProducer.*"
%>
<%
        //String [] test = {"ns_hadoopadmin:BigMonitorMetricDataClusterHour", "hadoop-2.4.0_master", "100005", "1469762820", "1469763180"};
    //    String [] test = {"ns_hadoopadmin:BigMonitorMetricDataClusterHour", "hadoop-2.4.0-master", "100058", "1472115300", "1472122020"};
	String res = GetData.get();
    String[] a=new String[10];
    for(int i=0;i<10;i++)
    {
        a[i]="#container"+i;
    }
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
      var str='#container';
     for (var i=0;i<2;i++) {
      var c=str+i;
      //   window.alert(c);
         $(c).highcharts({
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

     }

});
		</script>
	</head>
	<body>
<script src="./Highcharts-4.2.6/js/highcharts.js"></script>
<script src="./Highcharts-4.2.6/modules/exporting.js"></script>

<%
for(int i=0;i<2;i++) {
     String  aaa="container"+String.valueOf(i);
    // out.print(aaa);
%><div id="<%=aaa%>" style="min-width: 310px; height: 400px; margin: 0 auto"></div><%
    }
%>
	</body>
</html>
