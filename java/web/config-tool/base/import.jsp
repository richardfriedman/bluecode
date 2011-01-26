<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>BLUE:: Open Source System and Network Monitoring</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<link href="styles/blue_layout.css" rel="stylesheet" type="text/css" />
<script src="scripts/prototype.js" type="text/javascript"></script>
<script src="scripts/scriptaculous.js" type="text/javascript"></script>

</head>

<body><div id="content"><b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
<div id="nav">
	<%@ include file="menu.html" %>
</div>
<div id="main">
<f:view>

<h3>Import your existing configuration</h3>
<p>Please use this page to import your existing Nagios configuration. To import your existing configuration you will need to specify
two pieces of information:</p>
<ul>
	<li>The full path to your current Nagios Configuration, e.g /usr/local/nagios/etc/nagios.cfg</li>
	<li>The full path to where you would like output from this tool to appear, eg /usr/local/blue/output</li>
</ul>
<p>Your Nagios configuration must be in a valid Nagios format and have the correct permissions on all configuration files & directories. You must also
make sure that the directory in which you wish the configuration files to appear has full read/write permissions. You should also make sure that all paths
within your Nagios configuration files are absolute and not relative.</p>

<p>Please be aware that the import only currently works with congifuration files in the style of Nagios 2.x</p>
<h:form>
	<table>
		<tr><td>Config Location:</td><td><h:inputText id="config_location" value="#{accountHandler.configLocation}" size="30" required="true"/></td></tr>
		<tr><td>Output Location:</td><td><h:inputText id="output_location" value="#{accountHandler.outputLocation}" size="30" required="true"/></td></tr>		
		<tr><td colspan="2"><h:commandButton  styleClass="button" value="Import Config" action="#{accountHandler.importConfig}"/></td></tr>
	</table>
</h:form>

</f:view>
</div>


<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
