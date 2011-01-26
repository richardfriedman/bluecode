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

<h3>Modify Host Details:</h3>
	<!-- The glorious add host form //-->	
		<table name="host_table">
			<h:form>
			<tr><td>Hostname:</td><td><h:inputText id="hostname" size="20" value="#{hostHandler.modifyHost.hostname}" required="true"/></td><td><h:message for="hostname"/></td></tr>
			<tr><td>Alias:</td><td><h:inputText id="alias" size="20" value="#{hostHandler.modifyHost.alias}" required="true"/></td><td><h:message for="alias"/></td></tr>
			<tr><td>IP Address:</td><td><h:inputText id="ip_address" size="20" value="#{hostHandler.modifyHost.IPAddress}" required="true"/></td><td><h:message for="ip_address"/></td></tr>
			<tr><td>Parents:</td><td><h:selectManyListbox value="#{hostHandler.modifyHost.parents}" id="parents">
							<f:selectItems value="#{hostHandler.hostNames}"/>
						</h:selectManyListbox>
			</td><td><h:message for="parents"/></td></tr>
			<tr><td colspan="3"><p>Specify a command that is used to check this host.</p></td></tr>
			<tr><td colspan="2"><h:selectOneMenu value="#{hostHandler.modifyHost.checkCommand}" id="check_command">
						<f:selectItems value="#{commandHandler.commandNames}"/>
					    </h:selectOneMenu></td><td><h:message for="check_command"/></td></tr>
			<tr><td>Max Check Attempts:</td><td><h:inputText id="max_check_attempts" maxlength="5" size="5" value="#{hostHandler.modifyHost.maxCheckAttempts}" required="true"/></td><td><h:message for="max_check_attempts"/></td></tr>
			<tr><td>Checks Enabled:</td><td><h:selectBooleanCheckbox id="checks_Enabled" value="#{hostHandler.modifyHost.checksEnabled}" required="true"/></td><td><h:message for="check_interval"/></td></tr>
			<tr><td>Check Interval:</td><td><h:inputText id="check_interval" size="5" maxlength="5" value="#{hostHandler.modifyHost.checkInterval}" required="true"/></td><td><h:message for="check_interval"/></td></tr>
			<tr><td>Active Checks Enabled:</td><td><h:selectBooleanCheckbox id="active_checks_enabled" value="#{hostHandler.modifyHost.activeChecksEnabled}"/></td><td><h:message for="active_checks_enabled"/></td></tr>
			<tr><td>Passive Checks Enabled:</td><td><h:selectBooleanCheckbox id="passive_checks_enabled" value="#{hostHandler.modifyHost.passiveChecksEnabled}"/></td><td><h:message for="passive_checks_enabled"/></td></tr>
			<tr><td>Check Period</td><td><h:selectOneMenu id="check_period" value="#{hostHandler.modifyHost.checkPeriod}">
							<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
						     </h:selectOneMenu>
			</td><td><h:message for="check_period"/></td></tr>
			<tr><td>Obsess Over Host:</td><td><h:selectBooleanCheckbox id="obsess_over_host" value="#{hostHandler.modifyHost.obsessOverHost}"/></td><td><h:message for="obsess_over_host"/></td></tr> 											
			<tr><td>Check Freshness:</td><td><h:selectBooleanCheckbox id="check_freshness" value="#{hostHandler.modifyHost.checkFreshness}"/></td><td><h:message for="check_freshness"/></td></tr>
			<tr><td>Freshness Threshold:</td><td><h:inputText id="freshness_threshold" size="5" maxlength="5" value="#{hostHandler.modifyHost.freshnessThreshold}"/></td><td><h:message for="freshness_threshold"/></td></tr>								
			<tr><td>Event Handler:</td><td><h:selectOneMenu id="event_handler" value="#{hostHandler.modifyHost.eventHandler}">
								<f:selectItems value="#{commandHandler.commandNames}"/>
							</h:selectOneMenu>
			</td><td><h:message for="event_handler"/></td></tr>
			<tr><td>Event Handler Enabled:</td><td><h:selectBooleanCheckbox id="event_handler_enabled" value="#{hostHandler.modifyHost.eventHandlerEnabled}"/></td><td><h:message for="event_handler_enabled"/></td></tr>
			<tr><td>Low Flap Threshold:</td><td><h:inputText id="low_flap_threshold" value="#{hostHandler.modifyHost.lowFlapThreshold}" size="5" maxlength="5"/></td><td><h:message for="low_flap_threshold"/></td></tr>
			<tr><td>High Flap Threshold:</td><td><h:inputText id="high_flap_threshold" value="#{hostHandler.modifyHost.highFlapThreshold}" size="5" maxlength="5"/></td><td><h:message for="high_flap_threshold"/></td></tr>
			<tr><td>Flap Detection Enabled:</td><td><h:selectBooleanCheckbox id="flap_detection_enabled" value="#{hostHandler.modifyHost.flapDetectionEnabled}"/></td><td><h:message for="flap_detection_enabled"/></td></tr>
			<tr><td>Failure Prediction Enabled:</td><td><h:selectBooleanCheckbox id="fpe" value="#{hostHandler.host.failurePredictionEnabled}"/></td><td><h:message for="fpe"/></td></tr>
			<tr><td>Process Perf Data:</td><td><h:selectBooleanCheckbox id="process_perf_data" value="#{hostHandler.modifyHost.processPerfData}"/></td><td><h:message for="process_perf_data"/></td></tr>
			<tr><td>Retain Status Info:</td><td><h:selectBooleanCheckbox id="retain_status_info" value="#{hostHandler.modifyHost.retainStatusInformation}"/></td><td><h:message for="retain_status_info"/></td></tr>
			<tr><td>Retain Non-status Info:</td><td><h:selectBooleanCheckbox id="retain_nonstatus_info" value="#{hostHandler.modifyHost.retainNonStatusInformation}"/></td><td><h:message for="retain_nonstatus_info"/></td></tr>
			<tr><td>Notifications Enabled:</td><td><h:selectBooleanCheckbox id="notification_enabled" value="#{hostHandler.modifyHost.notificationsEnabled}"/></td><td><h:message for="notification_enabled"/></td></tr>
			<tr><td>Notification Interval:</td><td><h:inputText id="notification_interval" size="5" maxlength="5" value="#{hostHandler.modifyHost.notificationInterval}" required="true"/></td><td><h:message for="notification_interval"/></td></tr>
			<tr><td>Notification Period:</td><td><h:selectOneMenu id="notification_period" value="#{hostHandler.modifyHost.notificationPeriod}" required="true">
								<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
							     </h:selectOneMenu>
			</td><td><h:message for="notification_period"/></td></tr>
			<tr><td>Notification Options:</td><td>
				<h:selectManyCheckbox id="notification_options" value="#{hostHandler.modifyHost.notificationOptions}" required="true">
					<f:selectItem itemValue="d" itemLabel="Down"/>
					<f:selectItem itemValue="u" itemLabel="Unreachable"/>
					<f:selectItem itemValue="r" itemLabel="Recoveries"/>
					<f:selectItem itemValue="f" itemLabel="Flapping"/>
				</h:selectManyCheckbox>			
			</td><td><h:message for="notification_options"/></td></tr>
			<tr><td>Stalking Options:</td><td>
				<h:selectManyCheckbox id="stalking_options" value="#{hostHandler.modifyHost.stalkingOptions}">
					<f:selectItem itemValue="o" itemLabel="Up"/>
					<f:selectItem itemValue="d" itemLabel="Down"/>
					<f:selectItem itemValue="u" itemLabel="Unreachable"/>
				</h:selectManyCheckbox></td><td></td></tr>			
			<tr><td colspan="3"><p>Specify contact group(s) that should be notified by this host:</p></td></tr>
			<tr><td colspan="2"><h:selectManyListbox value="#{hostHandler.modifyHost.contactGroups}" id="contact_group_selector" size="3" required="true">
						<f:selectItems value="#{groupHandler.contactGroupNames}"/>
					    </h:selectManyListbox></td><td><h:message for="contact_group_selector"/><!--<p>No Contact Groups? <a href="contactgroups.faces" title="Add a Contact Group">Add One</a>.//--></p></td></tr>
			<tr><td colspan="3"><h:commandButton value="Modify Host" action="#{hostHandler.updateHost}"/></td></tr>		
			</h:form>
		</table>
	
	<!-- End of that form //-->
	
</f:view>

</div>


<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
