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
<p>Welcome to the configuration page for Blue itself. Within these pages you can change the way in which you wish Blue to run. Not all options are required and Blue will take
some system defaults for the options you leave blank.</p>
<p>Blue can be highly configured to suit your needs so there are a lot of options. Because of this the main Blue configuration is split over 4 pages. You can navigate between the pages
using the next pages button below, or alternatively as you click on the Save Config button you will automatically be taken to the next page in the configuration line.</p>
<p>For the sake of the Wizard, please spend a few moments going through the 4 configuration pages to familiarise yourself with some of the configuration options available to you. For a full
definition of how the configuration elements effect your Blue installation, please refer to the Blue documentation.</p>
</div>
</c:if>


<h3>Blue Configuration:</h3>
<h:form>
	<table>
		<tr><td>Log File:</td><td><h:inputText id="log_file" value="#{blueConfigHandler.blueConfig.logFile}" size="20"/></td></tr>
		<tr><td>Object Cache File:</td><td><h:inputText id="object_cache_file" value="#{blueConfigHandler.blueConfig.objectCacheFile}" size="20"/></td></tr>
		<tr><td>Temp File:</td><td><h:inputText id="temp_file" value="#{blueConfigHandler.blueConfig.tempFile}" size="20"/></td></tr>
		<tr><td>Status File:</td><td><h:inputText id="status_file" value="#{blueConfigHandler.blueConfig.statusFile}" size="20"/></td></tr>
		<tr><td>Aggregate Status Updates:</td><td><h:selectBooleanCheckbox id="a_status_option" value="#{blueConfigHandler.blueConfig.aggregateStatusUpdates}"/></td></tr>
		<tr><td>Aggregated Status Update Interval:</td><td><h:inputText id="status_update_interval" value="#{blueConfigHandler.blueConfig.statusUpdateInterval}"/></td></tr>
		<tr><td>Blue User:</td><td><h:inputText id="blue_user" value="#{blueConfigHandler.blueConfig.blueUser}" size="20"/></td></tr>
		<tr><td>Blue Group:</td><td><h:inputText id="blue_group" value="#{blueConfigHandler.blueConfig.blueGroup}" size="20"/></td></tr>
		<tr><td>Enable Notifications:</td><td><h:selectBooleanCheckbox id="enable_notifications" value="#{blueConfigHandler.blueConfig.enableNotifications}"/></td></tr>
		<tr><td>Execute Service Checks:</td><td><h:selectBooleanCheckbox id="execute_service_checks" value="#{blueConfigHandler.blueConfig.executeServiceChecks}"/></td></tr>
		<tr><td>Accept Passive Service Checks:</td><td><h:selectBooleanCheckbox id="accept_passive_service_checks" value="#{blueConfigHandler.blueConfig.acceptPassiveServiceChecks}"/></td></tr>
		<tr><td>Execute Host Checks:</td><td><h:selectBooleanCheckbox id="execute_host_checks" value="#{blueConfigHandler.blueConfig.executeHostChecks}"/></td></tr>
		<tr><td>Accept Passive Host Checks:</td><td><h:selectBooleanCheckbox id="accept_passive_host_checks" value="#{blueConfigHandler.blueConfig.acceptPassiveHostChecks}"/></td></tr>
		<tr><td>Enable Event Handlers:</td><td><h:selectBooleanCheckbox id="enable_event_handlers" value="#{blueConfigHandler.blueConfig.enableEventHandlers}"/></td></tr>
		<tr><td>Log Rotation Method:</td><td><h:inputText id="log_rotation_method" value="#{blueConfigHandler.blueConfig.logRotationMethod}" size="3" maxlength="1"/></td></tr>
		<tr><td>Log Archive Path:</td><td><h:inputText id="log_archive_path" value="#{blueConfigHandler.blueConfig.logArchivePath}" size="20"/></td></tr>
		<tr><td>Check External Commands:</td><td><h:selectBooleanCheckbox id="check_external_commands" value="#{blueConfigHandler.blueConfig.checkExternalCommands}"/></td></tr>
		<tr><td>Command Check Interval:</td><td><h:inputText id="command_check_interval" value="#{blueConfigHandler.blueConfig.commandCheckInterval}" size="5" maxlength="3"/></td></tr>
		<tr><td>External Command File:</td><td><h:inputText id="command_file" value="#{blueConfigHandler.blueConfig.commandFile}" size="20"/></td></tr>
		<tr><td>Downtime File:</td><td><h:inputText id="downtime_file" value="#{blueConfigHandler.blueConfig.downtimeFile}" size="20"/></td></tr>
		<tr><td>Comment File:</td><td><h:inputText id="comment_file" value="#{blueConfigHandler.blueConfig.commentFile}" size="20"/></td></tr>
		<tr><td>Lock File:</td><td><h:inputText id="lock_file" value="#{blueConfigHandler.blueConfig.lockFile}" size="20"/></td></tr>
		<tr><td>Retain State Information:</td><td><h:selectBooleanCheckbox value="#{blueConfigHandler.blueConfig.retainStateInformation}" id="rsi"/></td></tr>
		<tr><td>State Retention File:</td><td><h:inputText value="#{blueConfigHandler.blueConfig.stateRetentionFile}" id="srf" size="20"/></td></tr>
		<tr><td>Retention Update Interval (mins):</td><td><h:inputText value="#{blueConfigHandler.blueConfig.retentionUpdateInterval}" id="rui" size="20"/></td></tr>
		<tr><td colspan="2"><h:commandButton value="Save Config"  styleClass="button" action="#{blueConfigHandler.saveResult}" actionListener="#{blueConfigHandler.saveOptions}">
					<f:attribute name="stage" value="1"/>
				    </h:commandButton>
		 <h:commandButton value="Next Page >>" styleClass="button" action="next-page-1"/></td></tr>
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
