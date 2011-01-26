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
	<table name="timeperiod_table">
		<h:form>
		<tr><td>Timeperiod Name:</td><td colspan="4" align="left"><h:inputText id="timeperiod_name" size="20" value="#{timePeriodHandler.modifyTimePeriod.name}" required="true"/></td><td><h:message for="timeperiod_name"/></td></tr>
		<tr><td>Alias:</td><td colspan="4" align="left"><h:inputText id="alias" size="20" value="#{timePeriodHandler.modifyTimePeriod.alias}" required="true"/></td><td><h:message for="alias"/></td></tr>
		<tr><td>Sunday:</td><td>From:</td><td><h:inputText id="sunday_start" value="#{timePeriodHandler.modifyTimePeriod.sundayStart}" size="10"/></td><td>To:</td><td><h:inputText id="sunday_end" value="#{timePeriodHandler.modifyTimePeriod.sundayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Monday:</td><td>From:</td><td><h:inputText id="monday_start" value="#{timePeriodHandler.modifyTimePeriod.mondayStart}" size="10"/></td><td>To:</td><td><h:inputText id="monday_end" value="#{timePeriodHandler.modifyTimePeriod.mondayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Tuesday:</td><td>From:</td><td><h:inputText id="tuesday_start" value="#{timePeriodHandler.modifyTimePeriod.tuesdayStart}" size="10"/></td><td>To:</td><td><h:inputText id="tuesday_end" value="#{timePeriodHandler.modifyTimePeriod.tuesdayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Wednesday:</td><td>From:</td><td><h:inputText id="wednesday_start" value="#{timePeriodHandler.modifyTimePeriod.wednesdayStart}" size="10"/></td><td>To:</td><td><h:inputText id="wednesday_end" value="#{timePeriodHandler.modifyTimePeriod.wednesdayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Thursday:</td><td>From:</td><td><h:inputText id="thursday_start" value="#{timePeriodHandler.modifyTimePeriod.thursdayStart}" size="10"/></td><td>To:</td><td><h:inputText id="thursday_end" value="#{timePeriodHandler.modifyTimePeriod.thursdayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Friday:</td><td>From:</td><td><h:inputText id="friday_start" value="#{timePeriodHandler.modifyTimePeriod.fridayStart}" size="10"/></td><td>To:</td><td><h:inputText id="friday_end" value="#{timePeriodHandler.modifyTimePeriod.fridayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td>Saturday:</td><td>From:</td><td><h:inputText id="saturday_start" value="#{timePeriodHandler.modifyTimePeriod.saturdayStart}" size="10"/></td><td>To:</td><td><h:inputText id="saturday_end" value="#{timePeriodHandler.modifyTimePeriod.saturdayEnd}" size="10"/></td><td>(HH:MM)</td></tr>
		<tr><td colspan="6"><h:commandButton value="Modify TimePeriod" action="#{timePeriodHandler.modifyTimePeriod}"/></td></tr>		
		</h:form>
	</table>	
</f:view>
<p>Use the form above to modify details of the Time Period</p>
</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
