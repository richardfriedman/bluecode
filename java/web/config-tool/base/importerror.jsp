<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<script language="javascript">
<!-- hide from older browsers
function redirectMe()
{
	setTimeout("location='import.faces'",2000);
}
//-->
</script>

<title>BLUE:: Open Source System and Network Monitoring</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<link href="styles/blue.css" rel="stylesheet" type="text/css" />
</head>

<body onLoad="redirectMe()">
<div id="content"><img src="images/bluebanner.jpg" />


<div id="nav"><b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
<!-- Include the main menu //-->

<%@ include file="menu.html"%>

<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> <b class="r1"></b></b></div>

<div id="main">

<div class="contentbox"><b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
<p>There has been a problem importing your configuration file. Please verify that it is in the correct Nagios format.</p>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> <b class="r1"></b></b></div>


</div>
</div>
</body>
</html>
