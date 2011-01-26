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
<c:if test="${accountHandler.inWizard}">
<div id="wizard">
<h3>Wizard Info:</h3>
<p>This is the second page of the main Blue configuration. Please select the options you require. Again if you need to understand what a particular option does
, then please consult the documentation.</p>
</div>
</c:if>


<h3>Blue Configuration Part 2:</h3>
<h:form>
	<table>
		<tr><td>Use Retained Program State</td><td><h:selectBooleanCheckbox id="urps" value="#{blueConfigHandler.blueConfig.useRetainedProgramState}"/></td></tr>
		<tr><td>Use Retained Scheduling Info</td><td><h:selectBooleanCheckbox id="ursi" value="#{blueConfigHandler.blueConfig.useRetainedSchedulingInfo}"/></td></tr>
		<tr><td>Use Syslog:</td><td><h:selectBooleanCheckbox id="use_syslog" value="#{blueConfigHandler.blueConfig.useSyslog}"/></td></tr>
		<tr><td>Log Notifications:</td><td><h:selectBooleanCheckbox id="log_not" value="#{blueConfigHandler.blueConfig.logNotifications}"/></td></tr>
		<tr><td>Log Service Retries:</td><td><h:selectBooleanCheckbox id="log_sr" value="#{blueConfigHandler.blueConfig.logServiceRetries}"/></td></tr>
		<tr><td>Log Host Retries:</td><td><h:selectBooleanCheckbox id="log_hr" value="#{blueConfigHandler.blueConfig.logHostRetries}"/></td></tr>
		<tr><td>Log Event Handlers:</td><td><h:selectBooleanCheckbox id="log_eh" value="#{blueConfigHandler.blueConfig.logEventHandlers}"/></td></tr>
		<tr><td>Log Initial States:</td><td><h:selectBooleanCheckbox id="log_is" value="#{blueConfigHandler.blueConfig.logInitialStates}"/></td></tr>
		<tr><td>Log External Commands:</td><td><h:selectBooleanCheckbox id="log_ec" value="#{blueConfigHandler.blueConfig.logExternalCommands}"/></td></tr>
		<tr><td>Log Passive Checks:</td><td><h:selectBooleanCheckbox id="log_pc" value="#{blueConfigHandler.blueConfig.logPassiveChecks}"/></td></tr>
		<tr><td>Global Host Event Handler:</td><td><h:selectOneMenu id="gheh" value="#{blueConfigHandler.blueConfig.globalHostEventHandler}">
									<f:selectItems value="#{commandHandler.commandNames}"/>
							   </h:selectOneMenu>
		</td></tr>
		<tr><td>Global Service Event Handler:</td><td><h:selectOneMenu id="gseh" value="#{blueConfigHandler.blueConfig.globalServiceEventHandler}">
									<f:selectItems value="#{commandHandler.commandNames}"/>
							      </h:selectOneMenu></td></tr>
		<tr><td>Sleep Time:</td><td><h:inputText id="sleep" value="#{blueConfigHandler.blueConfig.sleepTime}" size="5" maxlength="3"/></td></tr>
		<tr><td>Service Inter-Check Delay Method:</td><td><h:inputText id="sicdm" value="#{blueConfigHandler.blueConfig.serviceInterCheckDelayMethod}" size="3" maxlength="1"/></td></tr>
		<tr><td>Maximum Service Check Spread: (mins)</td><td><h:inputText id="mscs" value="#{blueConfigHandler.blueConfig.maxServiceCheckSpread}" size="5" maxlength="3"/></td></tr>
		<tr><td>Service Interleave Factor:</td><td><h:inputText id="sif" value="#{blueConfigHandler.blueConfig.serviceInterleaveFactor}" size="3" maxlength="1"/></td></tr>
		<tr><td>Max Concurrent Checks:</td><td><h:inputText id="mcc" value="#{blueConfigHandler.blueConfig.maxConcurrentChecks}" size="5" maxlength="3"/></td></tr>
		<tr><td>Service Reaper Frequency: (secs)</td><td><h:inputText id="srf" value="#{blueConfigHandler.blueConfig.serviceReaperFrequency}" size="5" maxlength="3"/></td></tr>
		<tr><td>Host Inter-Check Delay Method:</td><td><h:inputText id="hicdm" value="#{blueConfigHandler.blueConfig.hostInterCheckDelayMethod}" size="3" maxlength="1"/></td></tr>
		<tr><td>Max Host Check Spread:</td><td><h:inputText id="mhcs" value="#{blueConfigHandler.blueConfig.maxHostCheckSpread}" size="5" maxlength="3"/></td></tr>
		<tr><td>Interval Length: (secs)</td><td><h:inputText id="il" value="#{blueConfigHandler.blueConfig.intervalLength}" size="5" maxlength="3"/></td></tr>
		<tr><td>Auto Re-schedule Checks:</td><td><h:selectBooleanCheckbox id="arc" value="#{blueConfigHandler.blueConfig.autoRescheduleChecks}"/></td></tr>
		<tr><td>Auto Re-scheduling Interval: (secs)</td><td><h:inputText id="ari" value="#{blueConfigHandler.blueConfig.autoReschedulingInterval}" size="5" maxlength="3"/></td></tr>
		<tr><td>Auto Re-scheduling Window: (secs)</td><td><h:inputText id="arw" value="#{blueConfigHandler.blueConfig.autoReschedulingWindow}" size="5" maxlength="3"/></td></tr>
		<tr><td colspan="2"><h:commandButton value="Save Config"  styleClass="button" action="#{blueConfigHandler.saveResult}" actionListener="#{blueConfigHandler.saveOptions}">
		
					<f:attribute name="stage" value="2"/>
				    </h:commandButton>
		 <h:commandButton value="Next Page >>"  styleClass="button" action="next-page-2"/></td></tr>
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
