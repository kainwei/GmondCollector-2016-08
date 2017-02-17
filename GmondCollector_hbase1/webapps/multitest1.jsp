<%@ page
		import="com.sina.data.DataProducer.GetData"
        import="java.util.*"
%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String [] res = { GetData.get(), GetData.get() };
	String [] title = { "小研和灰灰的爱情故事1", "小研和灰灰的爱情故事2" };
	String yAtext = "爱情曲线";
	int len = res.length;
	String [] hstr = new String[len];
	String [] divstr = new String[len];
	for(int i=0; i<len ;i++ ){
	    hstr[i] = "$('#container"+i+"').highcharts({chart:{zoomType:'x'},title:{text:'" + title[i] +"'}," +
	"subtitle:{text:document.ontouchstart===undefined?'" +
	"Clickanddragintheplotareatozoomin':'Pinchthecharttozoomin'},xAxis:{type:'datetime'}," +
	"yAxis:{title:{text:'"+ yAtext + " '}},legend:{enabled:false},plotOptions:{area:{fillColor:" +
	"{linearGradient:{x1:0,y1:0,x2:0,y2:1},stops:[[0,Highcharts.getOptions().colors[0]],[1," +
	"Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]]}," +
	"marker:{radius:2},lineWidth:1,states:{hover:{lineWidth:1}},threshold:null}}," +
	"series:[{type:'area',name:'USDtoEUR',data:"+ res[i] +"}]});";
		divstr[i] = "<div id=\"container"+i+"\" style=\"min-width: 310px; height: 400px; margin: 0 auto\"></div>";
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

	<%for(int i=0;i<len;i++) {
		out.println(hstr[i]);
	}
	%>


});





		</script>
	</head>
	<body>
<script src="./Highcharts-4.2.6/js/highcharts.js"></script>
<script src="./Highcharts-4.2.6/modules/exporting.js"></script>

<%for(int i=0;i<len;i++) {
	out.println(divstr[i]);
}
%>

	</body>
</html>
