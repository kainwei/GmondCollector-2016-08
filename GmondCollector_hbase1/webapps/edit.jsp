<%@ page
        import="java.util.List"
        contentType="text/html; charset=utf-8"
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title></title>
    <link rel="stylesheet" href="http://cdn.bootcss.com/bootstrap/3.3.0/css/bootstrap.min.css">
    <script src="http://cdn.bootcss.com/jquery/1.11.1/jquery.min.js"></script>
    <script src="http://cdn.bootcss.com/bootstrap/3.3.0/js/bootstrap.min.js"></script>
</head>
<body>




<form action="./update.jsp"  class="form-horizontal" role="form">

    <legend>Metric Setting</legend>
    <div class="form-group">
        <label for="register" class="col-sm-2 control-label">Register Name</label>
        <div class="col-sm-7">
            <input name="register" class="form-control" id="register"
                   placeholder="regeister">
        </div>
    </div>
    <div class="form-group">
        <label for="metric" class="col-sm-2 control-label">Metric Name</label>
        <div class="col-sm-7">
            <input name="metric" class="form-control" id="metric"
                   value="metric" >

        </div>
    </div>

    <div class="form-group">
        <label for="host" class="col-sm-2 control-label">Host List</label>
        <div class="col-sm-7">
            <input name="host" class="form-control" id="host"
                   value=host >

        </div>
    </div>

    <div class="form-group">
        <label for="threshold" class="col-sm-2 control-label">Threshold</label>
        <div class="col-sm-7">
            <input name="threshold" class="form-control" id="threshold"
                   value="threshold" >

        </div>
    </div>

    <div class="form-group">
        <label for="metric_judge_type" class="col-sm-2 control-label">Judge Type</label>
        <div class="col-sm-7">
            <select id="metric_judge_type"  class="form-control"  name="metric_judge_type" class="chzn-select required" value=type >
                <option>type</option>
                <option>le</option>
                <option>lt</option>
                <option>ge</option>
                <option>eq</option>
                <option>ne</option>
            </select>
        </div>
    </div>

    <div class="form-group">
        <label for="ex" class="col-sm-2 control-label">Extends</label>
        <div class="col-sm-7">
            <textarea name="ex" class="form-control" rows="3" id="ex" >ext</textarea>


        </div>
    </div>
    <legend>Email</legend>
    <div class="control-group">

    </div>


    <div class="form-group">
        <label  class="col-sm-2 control-label"></label>
        <div class="col-sm-7">
            <input name="email_enable" class="uniform_on" type="checkbox" id="email_enable" checked="1" value="1" Enable="false" onclick="enableEmail(this)">Enable
        </div>
    </div>




    <div class="form-group">
        <label  class="col-sm-2 control-label">Sent People</label>
        <div class="col-sm-7">
            <input name="email_sent_people" class="form-control" id="email_sent_people"
                   value="send people" >

        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">Recieve List</label>
        <div class="col-sm-7">
            <input name="email_receive_list" class="form-control" id="email_receive_list"
                   value="list" >

        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">Theme</label>
        <div class="col-sm-7">
            <input name="email_theme" class="form-control" id="email_theme"
                   value="theme" >

        </div>
    </div>

    <div class="form-group">
        <!--label class="control-label" for="textarea2">Content</label-->
        <label  class="col-sm-2 control-label">Content</label>

        <div class="col-sm-7">
            <textarea name="email_content"  class="form-control"   id="email_content" rows="3" >content</textarea>


        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">Interval time</label>
        <div class="col-sm-7">
            <input name="email_interval_time" class="form-control" id="email_interval_time"
                   value="in_time" >

        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">Last Time</label>
        <div class="col-sm-7">
            <input name="email_last_time" class="form-control" id="email_last_time"
                   value="last time">

        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">extends</label>
        <div class="col-sm-7">
            <input name="email" class="form-control" id="email"
                   value="exten">

        </div>
        <div class="col-sm-7">

        </div>
    </div>




    <legend>SMS</legend>
    <div class="form-group">
        <label  class="col-sm-2 control-label"></label>
        <div class="col-sm-7">
            <input name="sms_enable" class="uniform_on" type="checkbox" id="sms_enable" checked="1" value="1" onclick="enable">Enable


        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">Recieve List</label>
        <div class="col-sm-7">
            <input name="sms_receive_list" class="form-control" id="sms_receive_list"
                   value="sms_recv" >

        </div>
    </div>

    <div class="form-group">
        <label  class="col-sm-2 control-label">Content</label>
        <div class="col-sm-7">
            <textarea name="sms_content" class="form-control" rows="3" id="sms_content" >sms_content</textarea>

        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">Interval time</label>
        <div class="col-sm-7">
            <input name="sms_interval_time" class="form-control" id="sms_interval_time"
                   value=in_time >

        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">Last Time</label>
        <div class="col-sm-7">
            <input name="sms_last_time"  class="form-control" id="sms_last_time"
                   value=sms_last_time >

        </div>
    </div>
    <div class="form-group">
        <label  class="col-sm-2 control-label">Extends</label>
        <div class="col-sm-7">
            <input name="sms_extends" class="form-control" id="sms_extends"
                   value="sms_ext" >

        </div>
        <div class="col-sm-7">


        </div>
    </div>

    <div class="form-group">
        <label  class="col-sm-2 control-label"></label>
        <div class="col-sm-7">

            <button   class="btn btn-primary btn-large" type="submit">保存</button>
            <button   class="btn btn-large btn-danger" type="button" onClick="quit()">返回</button>
        </div>
        <div class="col-sm-7">


        </div>
    </div>
</form>


<script>

    function quit()
    {
        window.location.href="./login.jsp";
    }
    function enableEmail(obj){
        var ched = obj.getAttribute("checked");
        if(ched==1){
            obj.setAttribute("checked",0);
            obj.setAttribute("value",0);
        }
        else{
            obj.setAttribute("checked",1);
            obj.setAttribute("value",1);
        }
        excute_enable("email_enable");
    }
    function enableSMS(obj){
        var ched = obj.getAttribute("checked");
        if(ched==1){
            obj.setAttribute("checked",0);
            obj.setAttribute("value",0);
        }
        else{
            obj.setAttribute("checked",1);
            obj.setAttribute("value",1);
        }
        excute_enable("sms_enable");
    }
    function excute_enable(method){
        var plate = document.getElementById(method);
        var ched = plate.getAttribute("checked");
        if(method=="email_enable"){
            var z = document.getElementById("email_sent_people");
            var y = document.getElementById("email_receive_list");
            var x = document.getElementById("email_theme");
            var w = document.getElementById("email_content");
            var v = document.getElementById("email_interval_time");
            var u = document.getElementById("email_last_time");
            var k = document.getElementById("email");
            var t = document.getElementById("email_extends");

            if(ched==1){
                z.removeAttribute("readonly","");
                y.removeAttribute("readonly","");
                x.removeAttribute("readonly","");
                v.removeAttribute("readonly","");
                u.removeAttribute("readonly","");
                w.removeAttribute("readonly","");
                k.removeAttribute("readonly","");
                t.removeAttribute("readonly","");


                z.setAttribute("class","required email");
                y.setAttribute("class","required emailReceiveList");
                x.setAttribute("class","required");
                v.setAttribute("class","required");
                u.setAttribute("class","required number");
                w.setAttribute("class","required");
                k.setAttribute("class","required");
                t.setAttribute("class","required email_extends");


            }
            else{

                z.setAttribute("readonly","");
                y.setAttribute("readonly","");
                x.setAttribute("readonly","");
                v.setAttribute("readonly","");
                u.setAttribute("readonly","");
                w.setAttribute("readonly","");
                k.setAttribute("readonly","");
                t.setAttribute("readonly","");
                z.removeAttribute("class","");
                y.removeAttribute("class","");
                x.removeAttribute("class","");
                v.removeAttribute("class","");
                u.removeAttribute("class","");
                w.removeAttribute("class","");
                k.removeAttribute("class","");
                t.removeAttribute("class","");
            }
        }
        else if(method=="sms_enable"){
            var z = document.getElementById("sms_receive_list");
            var y = document.getElementById("sms_content");
            var x = document.getElementById("sms_interval_time");
            var w = document.getElementById("sms_last_time");
            var v = document.getElementById("sms_extends");
            var mm = document.getElementById("sms");
            if(ched==1){
                z.removeAttribute("readonly","");
                y.removeAttribute("readonly","");
                x.removeAttribute("readonly","");
                w.removeAttribute("readonly","");
                v.removeAttribute("readonly","");
                mm.removeAttribute("readonly","");
                z.setAttribute("class","required smsReceiveList");
                y.setAttribute("class","required");
                x.setAttribute("class","required number");
                w.setAttribute("class","required number");
                v.setAttribute("class","required number");
                mm.setAttribute("class","required");
            }
            else{
                z.setAttribute("readonly","");
                y.setAttribute("readonly","");
                x.setAttribute("readonly","");
                w.setAttribute("readonly","");
                v.setAttribute("readonly","");
                mm.setAttribute("readonly","");
                z.removeAttribute("class","");
                y.removeAttribute("class","");
                x.removeAttribute("class","");
                w.removeAttribute("class","");
                v.removeAttribute("class","");
                mm.removeAttribute("class","");
            }
        }


    }

    excute_enable("email_enable");
    excute_enable("sms_enable");


</script>

</body>
</html>
