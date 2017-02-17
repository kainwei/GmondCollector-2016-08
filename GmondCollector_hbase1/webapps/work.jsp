<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<style>
    .hot {
        background-image: url(http://changyan.itc.cn/v2/asset/scs/imgs/hot.gif);
        height: 12px;
        width: 21px;
        position: absolute;
    }
</style>

<header class="jumbotron subhead" id="overview">
    <div class="container">
        <!--<a class="btn btn-lg btn-primary btn-shadow bs3-link" href="./filter_only_code.html" target="_blank"-->
        <!--role="button">代码回放</a>-->
    </div>
</header>

<div class="container">
    <div style="margin:10px 0">
        <script async src="//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"></script>
        <!-- test -->
        <ins class="adsbygoogle"
             style="display:block"
             data-ad-client="ca-pub-2101546703939638"
             data-ad-slot="9922577908"
             data-ad-format="auto"></ins>
        <script>
            (adsbygoogle = window.adsbygoogle || []).push({});
        </script>
    </div>
    <div class="row-fluid" style="margin-top:20px">
        <form action="./get.jsp" method="GET" id="myform">
        <!-- 表格开�?-->
            <button  type="submit">监控查看</button>
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
                request.setAttribute("location","shangdi");
            %>
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

<script>
    </form>
    (function(){
        var bp = document.createElement('script');
        bp.src = '//push.zhanzhang.baidu.com/push.js';
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(bp, s);
    })();
</script>

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
              alert( "The following data would have been submitted to the server: \n\n"+ res );
              window.location.href="get.jsp?" + res;

              return false;
          } );


    });
</script>
</html>


