<%@ page
        contentType="text/html; charset=utf-8"
        import="java.text.SimpleDateFormat"
        import="java.util.*"
        import="com.sina.data.bigmonitor.web.*"
        import="java.io.IOException"

%>
<%
    SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd" + " " + "HH:mm:ss");
    Date reqTime = new Date();
    String endStr = request.getParameter("endTime");
    String metric = request.getParameter("Metrics");
    String begStr = request.getParameter("startTime");
    session.setAttribute("Metrics", metric);
    if (endStr == null || endStr.equals("")) {
        endStr = dateFormat.format(reqTime);
    }
    if (begStr == null || begStr.equals("")) {
        begStr = dateFormat.format(
                new Date(reqTime.getTime() - 3600 * 1000));
    }
%>


<!DOCTYPE html>
<html>
<head>

    <!-- Le styles -->
    <link href="http://www.zhuhaihengxin.com/libs/bootstrap/2.3.2/css/bootstrap.min.css" rel="stylesheet">
    <link href="http://www.zhuhaihengxin.com/libs/bootstrap/2.3.2/css/bootstrap-responsive.min.css" rel="stylesheet">
    <!--代码高亮-->
    <link href="http://www.zhuhaihengxin.com/libs/syntaxhighlighter/3.0.83/styles/shCore.css" rel="stylesheet" type="text/css"/>
    <link href="http://www.zhuhaihengxin.com/libs/syntaxhighlighter/3.0.83/styles/shThemeDefault.css" rel="stylesheet" type="text/css"/>

    <link href="http://www.zhuhaihengxin.com/libs/datatables/1.10.0/css/jquery.dataTables.css" rel="stylesheet">
    <link href="/css/docs.css" rel="stylesheet">
    <style>


        /* example/api/multi_filter.html use*/
        tfoot input {
            width: 100%;
            padding: 3px;
            box-sizing: border-box;
        }
        tfoot select{
            width: 100%;
            padding: 3px;
            box-sizing: border-box;
        }
        td.highlight {
            background-color: whitesmoke !important;
        }
        td.details-control {
            background: url('../resources/details_open.png') no-repeat center center;
            cursor: pointer;
        }
        tr.shown td.details-control {
            background: url('../resources/details_close.png') no-repeat center center;
        }
    </style>
    <link rel="shortcut icon" href="/images/favicon.png">
</head>
<body>


<header class="jumbotron subhead" id="overview">
    <div class="container">
        <!--<a class="btn btn-lg btn-primary btn-shadow bs3-link" href="./filter_only_code.html" target="_blank"-->
        <!--role="button">代码回放</a>-->
    </div>
</header>

<div class="container">

    <div class="row-fluid" style="margin-top:20px">
        <form action="./2.jsp" method="GET" id="myform">

            <button  type="submit">监控查看</button>

            <label id="deltaTimeLabel" for="deltaTime"><B>Last: </B></label>
            <select name="deltaTime" id="deltaTime" onchange="time_refresh()" >
                <option value="10" >10 min</option>
                <option value="30" >30 min</option>
                <option value="60" SELECTED>hour</option>
                <option value="120" >2 hour</option>
                <option value="180" >3 hour</option>
                <option value="360" >6 hour</option>
                <option value="1440" >day</option>
                <option value="2880" >2 day</option>
                <option value="4320" >3 day</option>
                <option value="10080" >week</option>
            </select>

            <label id="startTimeLabel" for="startTime"><B>From: </B></label>

                <input name="startTime" id="startTime" type="text" class="form_css"
                       value="<%=begStr%>"
                       onclick="SelectDate(this,'yyyy-MM-dd hh:mm:ss',0,0)"
                       onchange="time_check()" />


            <label id="endTimeLabel" for="endTime"><B>To: </B></label>

                <input name="endTime" id="endTime" type="text" class="form_css"
                       value="<%=endStr%>"
                       onclick="SelectDate(this,'yyyy-MM-dd hh:mm:ss',0,0)"
                       onchange="time_check()" />


            <!-- 表格开始 -->
            <table id="example" class="display" cellspacing="0" width="100%">



                <thead>
                <tr>
                    <th>host</th>
                </tr>
                </thead>
                <tbody>


                <tr>
                    <td>
                        <input type="hidden" name="time" value="20160921">
                    </td>
                </tr>
                <%

                    for(int i=0;i<100;i++)
                    {
                %><tr>
                    <td>
                        <input type="checkbox" name="hosts"  value="<%=i%>"><%=i%>
                    </td>
                </tr> <%
                    }
                %>
                </tbody>
            </table>

            <!-- 表格结束 -->

        </form>


            <script type="text/javascript">
                function noteTitle(obj){
                    var note = $(obj).attr("title");
                    alert(note);
                }
            </script>

            <!-- jQuery文件。务必在bootstrap.min.js 之前引入 -->

            <script src="http://www.zhuhaihengxin.com/libs/jquery/1.10.2/jquery.min.js"></script>

            <!-- 最新的 Bootstrap 核心 JavaScript 文件 -->

            <script src="http://www.zhuhaihengxin.com/libs/bootstrap/3.0.3/js/bootstrap.min.js"></script>




            <script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/1.10.12/js/jquery.dataTables.min.js"></script>

            <script type="text/javascript">
                //判断是否显示更新提示
                $.get("/assets/updatelog.txt", function (data) {
                    var json = JSON.parse(data);
                    if (json.isNote) {
                        var cache = localStorage['dt.thxopen.com.note'];
                        if (typeof cache == 'undefined') {
                            $("#updateFlag").addClass("hot");
                        } else {
                            var current = new Date().getTime();
                            var bl = current - cache;
                            var s = 24 * 60 * 60 * 1000;
                            if (bl > s) {
                                $("#updateFlag").addClass("hot");
                            }
                        }
                    }
                });
            </script>

            <script type="text/javascript">
                var table;

                $(document).ready(function () {
                    table = $('#example').dataTable();



                    $('#myform').submit( function() {
                        var res = table.$('input').serialize();

                        var st = document.getElementById('startTime').value;
                        var et = document.getElementById('endTime').value;

                        res = res + "&startTime=" + st + "&endTime=" + et;
                        alert( "The following data would have been submitted to the server: \n\n"+ res);
                        //window.location.href="2.jsp?" + res;

                        return false;
                    } );


                });
            </script>
        </div>
</div>
</html>


