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

<script language="javascript">
function popUp(URL)
 {
	day = new Date();
	id = day.getTime();
	eval("page" + id + " = window.open(URL, '" + id + "', 'toolbar=0,scrollbars=1,location=0,statusbar=0,menubar=0,resizable=0,width=450,height=600');");
}
</script>

<body><div id="content"><b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
<div id="nav">
	<%@ include file="menu.html" %>
</div>
<div id="main">


<c:if test="${accountHandler.inWizard}">
<div id="wizard">
<h3>Wizard Info:</h3>
<p>A host can be any physical device on your network ranging from switches through to
printers and even temperature control units in your server room. Once a host has been defined it can then run zero or more services which are queried through
the definition of service checks. On this page you can specify the options you would like to enforce for the monitoring of each host.</p>
<p>For the sake of the wizard, please enter a valid host address on your network. It is probably easiest to add localhost as the first example. For more
information on each attribute, click on the help link.</p> 
</div>
</c:if>



<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
<h:inputHidden value="#{timePeriodHandler.timePeriodCount}"/>
<h:inputHidden value="#{groupHandler.contactGroupCount}"/>
<h:inputHidden value="#{commandHandler.commandCount}"/>

<h3>Add a new Host:<br/>
<a href="#" onclick="new Effect.toggle('search','slide',{duration:0.5})">(Search for existing host/</a>
<c:choose>
	<c:when test="${hostHandler.templateCount == 0}">
		<span class="disabled">No Templates Available)</span></a>
	</c:when>
	<c:otherwise>
<a href="#"  onclick="new Effect.toggle('template','slide',{duration:0.5})">Use Template/</a>
	</c:otherwise>
</c:choose>
<a href="javascript:popUp('hosthelp.html')">Need Help? Click Here!</a>

</h3>
	<div id="search" style="display: none" class="dropbox">
		<h:form>
			<label for="host_search">Host Name:</label>	<h:inputText size="20" id="host_search" maxlength="30" value="#{searchHandler.searchString}" required="true"/><h:message for="host_search"/>
						<h:commandButton styleClass="button" value="Search" action="#{searchHandler.searchObjects}" actionListener="#{searchHandler.setSearchType}">
							<f:attribute name="searchType" value="0"/>
	     				</h:commandButton>
						<input type="reset"  class="button" value="Clear"/>
		</h:form>
	</div>
	<div id="template" style="display: none" class="dropbox">
		<h:form id="template_form">
		<label for="template_selector">Template Name:</label>	<h:selectOneListbox id="template_selector" value="#{hostHandler.templateToLoad}" size="1">
			<f:selectItems value="#{hostHandler.templateNames}"/>
    	  </h:selectOneListbox>
	    		 <h:commandButton  styleClass="button" action="#{hostHandler.useTemplate}" value="Load"/>
    	  </h:form>
		<h:message id="template_message" for="template_selector"/></td></div>
	<!-- The glorious add host form //-->

<c:choose>
	<c:when test="${groupHandler.contactGroupCount == 0 && timePeriodHandler.timePeriodCount == 0}">
		<p>There are currently no Contact Groups defined. Please <a href="contactgroups.faces" title="Define a Contact Group">define a Contact Group</a> before continuing.</p>
	</c:when>
	<c:when test="${groupHandler.contactGroupCount == 0 && timePeriodHandler.timePeriodCount > 0}">
		<p>There are currently no Contact Groups defined. Please <a href="contactgroups.faces" title="Define a Contact Group">define a Contact Group</a> before continuing.</p>
	</c:when>
	<c:when test="${groupHandler.contactGroupCount > 0 && timePeriodHandler.timePeriodCount == 0}">
		<p>There are currently no Time Periods defined. Please <a href="timeperiods.faces" title="Define a Time Period">define a Time Period</a> before continuing.</p>
	</c:when>
	<c:when test="${commandHandler.commandCount == 0 }">
		<p>There are currently no Commands defined. Please <a href="commands.faces" title="Define a Command">define a Command</a> before continuing.</p>
	</c:when>
	<c:otherwise>
		<p>* Denotes a Required Field</p>
		<table name="host_table">
			<h:form>
			<tr><td>Hostname: (*)</td><td><h:inputText id="hostname" size="20" value="#{hostHandler.host.hostname}" required="true"/></td><td><h:message for="hostname"/></td></tr>
			<tr><td>Alias:  (*)</td><td><h:inputText id="alias" size="20" value="#{hostHandler.host.alias}" required="true"/></td><td><h:message for="alias"/></td></tr>
			<tr><td>IP Address:  (*)</td><td><h:inputText id="ip_address" size="20" value="#{hostHandler.host.IPAddress}" required="true"/></td><td><h:message for="ip_address"/></td></tr>
			<tr><td>Parents:</td><td><h:selectManyListbox value="#{hostHandler.host.parents}" id="parents" size="3">
							<f:selectItems value="#{hostHandler.hostNames}"/>
						</h:selectManyListbox>
			</td><td><h:message for="parents"/></td></tr>
			<tr><td colspan="3"><p>Specify a command that is used to check this host.</p></td></tr>
			<tr><td colspan="2"><h:selectOneMenu value="#{hostHandler.host.checkCommand}" id="check_command">
						<f:selectItems value="#{commandHandler.commandNames}"/>
					    </h:selectOneMenu></td><td><h:message for="check_command"/><!--<p>No Commands? <a href="commands.faces" title="Add a Command">Add One</a>.</p>//--></td></tr>
			<tr><td>Max Check Attempts: (*)</td><td><h:inputText id="max_check_attempts" maxlength="5" size="5" value="#{hostHandler.host.maxCheckAttempts}" required="true"/></td><td><h:message for="max_check_attempts"/></td></tr>
			<tr><td>Checks Enabled:</td><td><h:selectBooleanCheckbox id="checks_enabled" value="#{hostHandler.host.checksEnabled}"/></td><td><h:message for="checks_enabled"/></td></tr>
			<tr><td>Check Interval:</td><td><h:inputText id="check_interval" size="5" maxlength="5" value="#{hostHandler.host.checkInterval}" required="true"/></td><td><h:message for="check_interval"/></td></tr>
			<tr><td>Active Checks Enabled:</td><td><h:selectBooleanCheckbox id="active_checks_enabled" value="#{hostHandler.host.activeChecksEnabled}"/></td><td><h:message for="active_checks_enabled"/></td></tr>
			<tr><td>Passive Checks Enabled:</td><td><h:selectBooleanCheckbox id="passive_checks_enabled" value="#{hostHandler.host.passiveChecksEnabled}"/></td><td><h:message for="passive_checks_enabled"/></td></tr>
			<tr><td>Check Period:  (*)</td><td><h:selectOneMenu id="check_period" value="#{hostHandler.host.checkPeriod}">
							<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
						     </h:selectOneMenu>
			</td><td><h:message for="check_period"/></td></tr>
			<tr><td>Obsess Over Host:</td><td><h:selectBooleanCheckbox id="obsess_over_host" value="#{hostHandler.host.obsessOverHost}"/></td><td><h:message for="obsess_over_host"/></td></tr> 											
			<tr><td>Check Freshness:</td><td><h:selectBooleanCheckbox id="check_freshness" value="#{hostHandler.host.checkFreshness}"/></td><td><h:message for="check_freshness"/></td></tr>
			<tr><td>Freshness Threshold:</td><td><h:inputText id="freshness_threshold" size="5" maxlength="5" value="#{hostHandler.host.freshnessThreshold}"/></td><td><h:message for="freshness_threshold"/></td></tr>								
			<tr><td>Event Handler:</td><td><h:selectOneMenu id="event_handler" value="#{hostHandler.host.eventHandler}">
								<f:selectItems value="#{commandHandler.commandNames}"/>
							</h:selectOneMenu>
			</td><td><h:message for="event_handler"/></td></tr>
			<tr><td>Event Handler Enabled:</td><td><h:selectBooleanCheckbox id="event_handler_enabled" value="#{hostHandler.host.eventHandlerEnabled}"/></td><td><h:message for="event_handler_enabled"/></td></tr>
			<tr><td>Low Flap Threshold:</td><td><h:inputText id="low_flap_threshold" value="#{hostHandler.host.lowFlapThreshold}" size="5" maxlength="5"/></td><td><h:message for="low_flap_threshold"/></td></tr>
			<tr><td>High Flap Threshold:</td><td><h:inputText id="high_flap_threshold" value="#{hostHandler.host.highFlapThreshold}" size="5" maxlength="5"/></td><td><h:message for="high_flap_threshold"/></td></tr>
			<tr><td>Flap Detection Enabled:</td><td><h:selectBooleanCheckbox id="flap_detection_enabled" value="#{hostHandler.host.flapDetectionEnabled}"/></td><td><h:message for="flap_detection_enabled"/></td></tr>
			<tr><td>Failure Prediction Enabled:</td><td><h:selectBooleanCheckbox id="fpe" value="#{hostHandler.host.failurePredictionEnabled}"/></td><td><h:message for="fpe"/></td></tr>
			<tr><td>Process Perf Data:</td><td><h:selectBooleanCheckbox id="process_perf_data" value="#{hostHandler.host.processPerfData}"/></td><td><h:message for="process_perf_data"/></td></tr>
			<tr><td>Retain Status Info:</td><td><h:selectBooleanCheckbox id="retain_status_info" value="#{hostHandler.host.retainStatusInformation}"/></td><td><h:message for="retain_status_info"/></td></tr>
			<tr><td>Retain Non-status Info:</td><td><h:selectBooleanCheckbox id="retain_nonstatus_info" value="#{hostHandler.host.retainNonStatusInformation}"/></td><td><h:message for="retain_nonstatus_info"/></td></tr>
			<tr><td>Notifications Enabled:</td><td><h:selectBooleanCheckbox id="notification_enabled" value="#{hostHandler.host.notificationsEnabled}"/></td><td><h:message for="notification_enabled"/></td></tr>
			<tr><td>Notification Interval:  (*)</td><td><h:inputText id="notification_interval" size="5" maxlength="5" value="#{hostHandler.host.notificationInterval}" required="true"/></td><td><h:message for="notification_interval"/></td></tr>
			<tr><td>Notification Period:  (*)</td><td><h:selectOneMenu id="notification_period" value="#{hostHandler.host.notificationPeriod}" required="true">
								<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
							     </h:selectOneMenu>
			</td><td><h:message for="notification_period"/></td></tr>
			<tr><td>Notification Options:  (*)</td><td>
				<h:selectManyCheckbox id="notification_options" value="#{hostHandler.host.notificationOptions}" required="true">
					<f:selectItem itemValue="d" itemLabel="Down"/>
					<f:selectItem itemValue="u" itemLabel="Unreachable"/>
					<f:selectItem itemValue="r" itemLabel="Recoveries"/>
					<f:selectItem itemValue="f" itemLabel="Flapping"/>
				</h:selectManyCheckbox>			
			</td><td><h:message for="notification_options"/></td></tr>
			<tr><td>Stalking Options:</td><td>
				<h:selectManyCheckbox id="stalking_options" value="#{hostHandler.host.stalkingOptions}">
					<f:selectItem itemValue="o" itemLabel="Up"/>
					<f:selectItem itemValue="d" itemLabel="Down"/>
					<f:selectItem itemValue="u" itemLabel="Unreachable"/>
				</h:selectManyCheckbox></td><td></td></tr>			
			<tr><td colspan="3"><p>Specify contact group(s) that should be notified by this host:  (*)</p></td></tr>
			<tr><td colspan="2"><h:selectManyListbox value="#{hostHandler.host.contactGroups}" id="contact_group_selector" size="3" required="true">
						<f:selectItems value="#{groupHandler.contactGroupNames}"/>
					    </h:selectManyListbox></td><td><h:message for="contact_group_selector"/><!--<p>No Contact Groups? <a href="contactgroups.faces" title="Add a Contact Group">Add One</a>.//--></p></td></tr>
			<tr><td colspan="3"><h:commandButton value="Save Host"  styleClass="button" action="#{hostHandler.addHost}" rendered="#{hostHandler.host.isModifiable == false}"/> <h:commandButton value="Modify Host"  styleClass="button" action="#{hostHandler.updateHost}" rendered="#{hostHandler.host.isModifiable == true}"/> <h:commandButton  styleClass="button" value="Save As Template" action="#{hostHandler.addTemplate}" rendered="#{hostHandler.host.isTemplate == false}"/> <input type="reset"  class="button" value="Clear"></td></tr>		
			</h:form>
		</table>
	</c:otherwise>
</c:choose>	
	<!-- End of that form //-->
	
<h3>List of current Hosts:</h3>

	<c:choose>
	<c:when test="${hostHandler.hostCount == 0}">
		<p>There are currently no Hosts defined.</p>
	</c:when>
	<c:otherwise>	
	<h:form>
		<h:dataTable value="#{hostHandler.sortedHostDetails}" rowClasses="even, odd" var="e" rows="#{hostHandler.rowCount}" first="#{hostHandler.firstRowIndex}">
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{hostHandler.sortByHostname}" immediate="true">
						<h:outputText value="Hostname"/>
					</h:commandLink>
				</f:facet>
				<h:commandLink action="#{hostHandler.select}" immediate="true" title="View Details for this Host">
					<h:outputText value="#{e.hostname}"/>			
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{hostHandler.sortByAlias}" immediate="true" title="Sort Hosts by Alias">
						<h:outputText value="Alias"/>
					</h:commandLink>
				</f:facet>
				<h:outputText value="#{e.alias}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{hostHandler.sortByIPAddress}" immediate="true" title="Sort Hosts by IP Address">
						<h:outputText value="IP Address"/>
					</h:commandLink>
				</f:facet>
				<h:outputText value="#{e.IPAddress}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="Delete"/>
				</f:facet>
				<h:commandLink action="#{hostHandler.delResult}" actionListener="#{hostHandler.deleteHost}" immediate="true" title="Delete this host">
					<f:param name="objectId" value="#{e.id}"/>
					<h:outputText value="Delete"/>
				</h:commandLink>
			</h:column>	
		</h:dataTable>
		<p>
		First: <h:commandButton id="first" value="<<" disabled="#{hostHandler.scrollFirstDisabled}" action="#{hostHandler.scrollFirst}" title="Scroll to First Page"/> <h:commandButton id="previous" value="<" disabled="#{hostHandler.scrollFirstDisabled}" action="#{hostHandler.scrollPrevious}" title="Scroll to the Previous Page"/> <h:commandButton id="next" value=">" disabled="#{hostHandler.scrollLastDisabled}" action="#{hostHandler.scrollNext}" title="Scroll to the Next Page"/> <h:commandButton value=">>" action="#{hostHandler.scrollLast}" disabled="#{hostHandler.scrollLastDisabled}" title="Scroll to the Last Page" id="last"/> : Last
		</p>
	</h:form>
	</c:otherwise>
	</c:choose>
</f:view>

</div>


<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
