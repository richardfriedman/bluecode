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
<h3>Modify Service Escalation Details:</h3>
	<table name="serviceescalation_table">
		<h:form>
		<tr><td>Service Description:</td><td><h:selectOneMenu id="service_description" value="#{escalationHandler.modifyServiceEscalation.serviceHost}" required="true">
							<f:selectItems value="#{serviceHandler.serviceHostNames}"/>
						     </h:selectOneMenu>
		</td><td><h:message for="service_description"/></td></tr>
		<tr><td>Contact Groups:</td><td><h:selectManyListbox id="contact_groups" size="3" value="#{escalationHandler.modifyServiceEscalation.contactGroups}" required="true">
							<f:selectItems value="#{groupHandler.contactGroupNames}"/>
						</h:selectManyListbox>
		</td><td><h:message for="contact_groups"/></td></tr>
		<tr><td>First Notification:</td><td><h:inputText id="first_notification" size="5" maxlength="3" value="#{escalationHandler.modifyServiceEscalation.firstNotification}" required="true"/></td><td><h:message for="first_notification"/></td></tr>
		<tr><td>Last Notification:</td><td><h:inputText id="last_notification" size="5" maxlength="3" value="#{escalationHandler.modifyServiceEscalation.lastNotification}" required="true"/></td><td></td></tr>
		<tr><td>Notification Interval:</td><td><h:inputText id="notification_interval" size="5" maxlength="3" value="#{escalationHandler.modifyServiceEscalation.notificationInterval}" required="true"/></td><td><h:message for="notification_interval"/></td></tr>
		<tr><td>Escalation Period:</td><td><h:selectOneMenu id="escalation_period" value="#{escalationHandler.modifyServiceEscalation.escalationPeriod}">
							<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
						   </h:selectOneMenu>
		</td><td><h:message for="escalation_period"/></td></tr>
		<tr><td>Escalation Options:</td><td><h:selectManyCheckbox id="escalation_options" value="#{escalationHandler.modifyServiceEscalation.escalationOptions}">
							<f:selectItem itemValue="r" itemLabel="Ok"/>
							<f:selectItem itemValue="w" itemLabel="Warning"/>
							<f:selectItem itemValue="u" itemLabel="Unknown"/>
							<f:selectItem itemValue="c" itemLabel="Critical"/>
						     </h:selectManyCheckbox>
		</td><td><h:message for="escalation_options"/></td></tr>
		<tr><td colspan="2"><h:commandButton value="Modify Escalation" action="#{escalationHandler.modResult}" actionListener="#{escalationHandler.modifyEscalation}">
			 	<f:attribute name="escType" value="1"/>
			 </h:commandButton></td></tr>
		</h:form>
	</table>
</f:view>
<p>Use the form above to modify the details of your Service Escalation.</p>
</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
