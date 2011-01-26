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
<h:inputHidden value="#{accountHandler.outputLocation}"/>
<h3>Modify Service Details:</h3>

	
	<!-- The add Service form...what a beaut! //-->
	<table>
		<h:form>
		
			<tr><td>Service Description:</td><td><h:inputText id="service_description" value="#{serviceHandler.modifyService.serviceDescription}" required="true"/></td><td><h:message for="service_description"/></td></tr>
			<tr><td>Is Volatile:</td><td><h:selectBooleanCheckbox id="is_volatile" value="#{serviceHandler.modifyService.isVolatile}"/></td><td><h:message for="is_volatile"/></td></tr>
			<tr><td>Max Check Attempts:</td><td><h:inputText id="max_check_attempts" maxlength="3" value="#{serviceHandler.modifyService.maxCheckAttempts}" size="5" required="true"/></td><td><h:message for="max_check_attempts"/></td></tr>
			<tr><td>Normal Check Interval:</td><td><h:inputText id="normal_check_interval" maxlength="3" value="#{serviceHandler.modifyService.normalCheckInterval}" size="5" required="true"/></td><td><h:message for="normal_check_interval"/></td></tr>
			<tr><td>Retry Check Interval:</td><td><h:inputText id="retry_check_interval" maxlength="3" value="#{serviceHandler.modifyService.retryCheckInterval}" size="5" required="true"/></td><td><h:message for="retry_check_interval"/></td></tr>
			<tr><td>Active Checks Enabled:</td><td><h:selectBooleanCheckbox id="active_checks_enabled" value="#{serviceHandler.modifyService.activeChecksEnabled}"/></td><td><h:message for="active_checks_enabled"/></tr>
			<tr><td>Passive Checks Enabled:</td><td><h:selectBooleanCheckbox id="passive_checks_enabled" value="#{serviceHandler.modifyService.passiveChecksEnabled}"/></td><td><h:message for="passive_checks_enabled"/></td></tr>
			<tr><td>Check Period:</td><td><h:selectOneMenu id="check_period" value="#{serviceHandler.modifyService.checkPeriod}" required="true">
							<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
						      </h:selectOneMenu>
			</td><td><h:message for="check_period"/></td></tr>
			<tr><td>Parallelize Check:</td><td><h:selectBooleanCheckbox id="parallelize_checks" value="#{serviceHandler.modifyService.parallelizeChecks}"/></td><td><h:message for="parallelize_checks"/></td></tr>
			<tr><td>Obsess Over Service:</td><td><h:selectBooleanCheckbox id="obsess_over_service" value="#{serviceHandler.modifyService.obsessOverService}"/></td><td><h:message for="obsess_over_service"/></td></tr>
			<tr><td>Check Freshness:</td><td><h:selectBooleanCheckbox id="check_freshness" value="#{serviceHandler.modifyService.checkFreshness}"/></td><td><h:message for="check_freshness"/></td></tr>
			<tr><td>Freshness Threshold:</td><td><h:inputText id="freshness_threshold" value="#{serviceHandler.modifyService.freshnessThreshold}" size="5" maxlength="2"/></td><td><h:message for="freshness_threshold"/></td></tr>
			<tr><td>Event Handler:</td><td><h:selectManyListbox id="event_handler" value="#{serviceHandler.modifyService.eventHandler}" size="3">
							 <f:selectItems value="#{commandHandler.commandNames}"/>
							</h:selectManyListbox>
			</td><td><h:message for="event_handler"/></td></tr>
			<tr><td>Event Handler Enabled:</td><td><h:selectBooleanCheckbox id="event_handler_enabled" value="#{serviceHandler.modifyService.eventHandlerEnabled}"/></td><td><h:message for="event_handler_enabled"/></td></tr>
			<tr><td>Low Flap Threshold:</td><td><h:inputText id="low_flap_threshold" size="5" value="#{serviceHandler.modifyService.lowFlapThreshold}" maxlength="2"/></td><td><h:message for="low_flap_threshold"/></td></tr>
			<tr><td>High Flap Threshold:</td><td><h:inputText id="high_flap_threshold" size="5" value="#{serviceHandler.modifyService.highFlapThreshold}" maxlength="2"/></td><td><h:message for="high_flap_threshold"/></td></tr>
			<tr><td>Flap Detection Enabled:</td><td><h:selectBooleanCheckbox id="flap_detection_enabled" value="#{serviceHandler.modifyService.flapDetectionEnabled}"/></td><td><h:message for="flap_detection_enabled"/></td></tr>
			<tr><td>Process Perf Data:</td><td><h:selectBooleanCheckbox id="process_perf_data" value="#{serviceHandler.modifyService.processPerfData}"/></td><td><h:message for="process_perf_data"/></td></tr>
			<tr><td>Retain Status Information:</td><td><h:selectBooleanCheckbox id="retain_status_info" value="#{serviceHandler.modifyService.retainStatusInformation}"/></td><td><h:message for="retain_status_info"/></td></tr>
			<tr><td>Retain Non-Status Information:</td><td><h:selectBooleanCheckbox id="retain_nonstatus_info" value="#{serviceHandler.modifyService.retainNonStatusInformation}"/></td><td><h:message for="retain_nonstatus_info"/></td></tr>
			<tr><td>Notifications Enabled:</td><td><h:selectBooleanCheckbox id="ne" value="#{serviceHandler.modifyService.notificationsEnabled}"/></td><td><h:message for="ne"/></td></tr>
			<tr><td>Notificaton Period</td><td><h:selectOneMenu id="notification_period" value="#{serviceHandler.modifyService.notificationPeriod}" required="true">
								<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
							   </h:selectOneMenu>
			</td><td><h:message for="notification_period"/></td></tr>
			<tr><td>Notification Interval:</td><td><h:inputText id="notification_interval" size="5" maxlength="2" value="#{serviceHandler.modifyService.notificationInterval}" required="true"/></td><td><h:message for="notification_interval"/></td></tr>
			<tr><td>Notification Options:</td><td>
					<h:selectManyCheckbox id="notification_options" value="#{serviceHandler.modifyService.notificationOptions}" required="true">
						<f:selectItem itemValue="w" itemLabel="Warning"/>
						<f:selectItem itemValue="u" itemLabel="Unknown"/>
						<f:selectItem itemValue="c" itemLabel="Critical"/>
						<f:selectItem itemValue="r" itemLabel="Recoveries"/>
						<f:selectItem itemValue="f" itemLabel="Flapping"/>
						<f:selectItem itemValue="n" itemLabel="None"/>
					</h:selectManyCheckbox>					

			</td><td><h:message for="notification_options"/></td></tr>
						
			<tr><td>Stalking Options:</td><td>
					<h:selectManyCheckbox id="stalking_options" value="#{serviceHandler.modifyService.stalkingOptions}">
						<f:selectItem itemValue="o" itemLabel="Ok"/>
						<f:selectItem itemValue="w" itemLabel="Warning"/>
						<f:selectItem itemValue="u" itemLabel="Unknown"/>
						<f:selectItem itemValue="c" itemLabel="Critical"/>
					</h:selectManyCheckbox>
			</td><td><h:message for="stalking_options"/></td></tr>
		
			<tr><td colspan="3"><p><u>Choose a command to use with this service:</u></p></td></tr>
			<tr><td colspan="2"><h:selectOneMenu id="check_command" value="#{serviceHandler.modifyService.checkCommand}" required="true">
						<f:selectItems value="#{commandHandler.commandNames}"/>
					    </h:selectOneMenu></td><td><h:message for="check_command"/></td></tr>
			<tr><td>Command Args</td><td><h:inputText id="command_args" value="#{serviceHandler.modifyService.commandArgs}" size="20" required="true"/></td><td><h:message for="command_args"/></td></tr>
			<tr><td colspan="3"><p><u>Select the host(s) on which you wish to monitor this service:</u></p></td></tr>
			<tr><td colspan="2"><h:selectManyListbox id="select_hosts" value="#{serviceHandler.modifyService.hostname}" size="3" required="true">
						<f:selectItems value="#{hostHandler.hostNames}"/>
					    </h:selectManyListbox></td><td><h:message for="select_hosts"/></td></tr>
			<tr><td colspan="3"><p><u>Select the contact group(s) responsible for this service:</p></td></tr>
			<tr><td colspan="2"><h:selectManyListbox id="select_contact_groups" value="#{serviceHandler.modifyService.contactGroups}" size="3" required="true">
						<f:selectItems value="#{groupHandler.contactGroupNames}"/>
					    </h:selectManyListbox></td><td><h:message for="select_contact_groups"/></td></tr>
			<tr><td colspan="3"><h:commandButton value="Modify Service" action="#{serviceHandler.modifyService}"/></td></tr>		
		</h:form>
	</table>
	
	<!-- End of add Service Form //-->	
</f:view>	

<p>Use the form above to modify details of the Service.</p>
</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
