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
<h3>Modify Command Details:</h3>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
	
	<table name="command_table">
		<h:form>
		<tr><td>Command Name:</td><td><h:inputText id="command_name" value="#{commandHandler.modifyCommand.name}" size="20" required="true"/></td><td><h:message for="command_name"/></td></tr>
		<tr><td>Command Line:</td><td><h:inputText id="command_line" value="#{commandHandler.modifyCommand.commandLine}" size="60" required="true"/></td><td><h:message for="command_line"/></td></tr>
		<tr><td colspan="3"><h:commandButton value="Modify Command" action="#{commandHandler.modifyCommand}"/></td></tr>
		</h:form>
	</table>	
<p>Use the form above to modify details of the Command.</p>
</f:view>	
</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
