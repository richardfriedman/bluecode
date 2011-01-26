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


<c:if test="${accountHandler.inWizard}">
<div id="wizard">
<h3>Wizard Info:</h3>
<p>Services can be thought of as 'something' that you wish to monitor on a Host. A Service can range from the amount of diskspace available on a host, to the temperature
reported in your server room. A Service is generally supported by a specific plug-in that will check the required information such as check_local_disk.</p>

<p>One of the most important parts of a Service definition are the command arguments. These arguments tell the plug-in the thresholds around which it should make decisions
about the current state of your service. The arguments are seperated by the ! character and are often used to fill in macro names that appear within command
definitions. If you are in doubt as to which parameters to use, you should consult the documentation of individual plug-ins</p>
<p>For the sake of this Wizard add the check_ping command to a Service called PING. Set this service to run on the host that you defined earlier, 
and set the Contact group for this Service to the one you have previously defined. You will also need to set the Time Period in which notifications are sent for
this service. For more information on the meaning of all other settings, please consult the Blue documentation.</p>
</div>

</c:if>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
<h:inputHidden value="#{hostHandler.hostCount}"/>

<h3>Add a new Service:<br/>
<a href="#" onclick="new Effect.toggle('search','slide',{duration:0.5})">(Search for existing Service/</a>
<c:choose>
	<c:when test="${serviceHandler.templateCount == 0}">
		<span class="disabled">No Templates Available)</span></a>
	</c:when>
	<c:otherwise>
<a href="#"  onclick="new Effect.toggle('template','slide',{duration:0.5})">Use Template)</a>
	</c:otherwise>
</c:choose>

</h3>
	<div id="search" style="display: none" class="dropbox">
		<h:form>
			<label for="service_search">Service Description:</label>	<h:inputText size="20" id="service_search" maxlength="30" value="#{searchHandler.searchString}" required="true"/><h:message for="service_search"/>
						<h:commandButton value="Search" action="#{searchHandler.searchObjects}" actionListener="#{searchHandler.setSearchType}">
							<f:attribute name="searchType" value="2"/>
	     				</h:commandButton>
						<input type="reset" value="Clear"/>
		</h:form>
	</div>
	<div id="template" style="display: none" class="dropbox">
		<h:form id="template_form">
		<label for="template_selector">Service Name:</label>	<h:selectOneListbox id="template_selector" value="#{serviceHandler.templateToLoad}" size="1">
			<f:selectItems value="#{serviceHandler.templateNames}"/>
    	  </h:selectOneListbox>
	    		 <h:commandButton action="#{serviceHandler.useTemplate}" value="Load"/>
    	  </h:form>
		<h:message id="template_message" for="template_selector"/></td></div>
		
	<!-- The add Service form...what a beaut! //-->
	
<c:choose>
	<c:when test="${hostHandler.hostCount <= 0}">
		<p>There are currently no Hosts defined. Please <a href="hosts.faces" title="Define a Host">define a Host</a> before constructing Services</p>
	</c:when>
	<c:otherwise>
		<p>* Denotes required field.</p>
		<table>
			<h:form>
			
				<tr><td>Service Description: (*)</td><td><h:inputText id="service_description" value="#{serviceHandler.service.serviceDescription}" required="true"/></td><td><h:message for="service_description"/></td></tr>
				<tr><td>Is Volatile:</td><td><h:selectBooleanCheckbox id="is_volatile" value="#{serviceHandler.service.isVolatile}"/></td><td><h:message for="is_volatile"/></td></tr>
				<tr><td>Max Check Attempts: (*)</td><td><h:inputText id="max_check_attempts" maxlength="3" value="#{serviceHandler.service.maxCheckAttempts}" size="5" required="true"/></td><td><h:message for="max_check_attempts"/></td></tr>
				<tr><td>Normal Check Interval: (*)</td><td><h:inputText id="normal_check_interval" maxlength="3" value="#{serviceHandler.service.normalCheckInterval}" size="5" required="true"/></td><td><h:message for="normal_check_interval"/></td></tr>
				<tr><td>Retry Check Interval: (*)</td><td><h:inputText id="retry_check_interval" maxlength="3" value="#{serviceHandler.service.retryCheckInterval}" size="5" required="true"/></td><td><h:message for="retry_check_interval"/></td></tr>
				<tr><td>Active Checks Enabled:</td><td><h:selectBooleanCheckbox id="active_checks_enabled" value="#{serviceHandler.service.activeChecksEnabled}"/></td><td><h:message for="active_checks_enabled"/></tr>
				<tr><td>Passive Checks Enabled:</td><td><h:selectBooleanCheckbox id="passive_checks_enabled" value="#{serviceHandler.service.passiveChecksEnabled}"/></td><td><h:message for="passive_checks_enabled"/></td></tr>
				<tr><td>Check Period: (*)</td><td><h:selectOneMenu id="check_period" value="#{serviceHandler.service.checkPeriod}" required="true">
								<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
							      </h:selectOneMenu>
				</td><td><h:message for="check_period"/></td></tr>
				<tr><td>Parallelize Check:</td><td><h:selectBooleanCheckbox id="parallelize_checks" value="#{serviceHandler.service.parallelizeChecks}"/></td><td><h:message for="parallelize_checks"/></td></tr>
				<tr><td>Obsess Over Service:</td><td><h:selectBooleanCheckbox id="obsess_over_service" value="#{serviceHandler.service.obsessOverService}"/></td><td><h:message for="obsess_over_service"/></td></tr>
				<tr><td>Check Freshness:</td><td><h:selectBooleanCheckbox id="check_freshness" value="#{serviceHandler.service.checkFreshness}"/></td><td><h:message for="check_freshness"/></td></tr>
				<tr><td>Freshness Threshold:</td><td><h:inputText id="freshness_threshold" value="#{serviceHandler.service.freshnessThreshold}" size="5" maxlength="2"/></td><td><h:message for="freshness_threshold"/></td></tr>
				<tr><td>Event Handler:</td><td><h:selectManyListbox id="event_handler" value="#{serviceHandler.service.eventHandler}" size="3">
								 <f:selectItems value="#{commandHandler.commandNames}"/>
								</h:selectManyListbox>
				</td><td><h:message for="event_handler"/></td></tr>
				<tr><td>Event Handler Enabled:</td><td><h:selectBooleanCheckbox id="event_handler_enabled" value="#{serviceHandler.service.eventHandlerEnabled}"/></td><td><h:message for="event_handler_enabled"/></td></tr>
				<tr><td>Low Flap Threshold:</td><td><h:inputText id="low_flap_threshold" size="5" value="#{serviceHandler.service.lowFlapThreshold}" maxlength="2"/></td><td><h:message for="low_flap_threshold"/></td></tr>
				<tr><td>High Flap Threshold:</td><td><h:inputText id="high_flap_threshold" size="5" value="#{serviceHandler.service.highFlapThreshold}" maxlength="2"/></td><td><h:message for="high_flap_threshold"/></td></tr>
				<tr><td>Flap Detection Enabled:</td><td><h:selectBooleanCheckbox id="flap_detection_enabled" value="#{serviceHandler.service.flapDetectionEnabled}"/></td><td><h:message for="flap_detection_enabled"/></td></tr>
				<tr><td>Process Perf Data:</td><td><h:selectBooleanCheckbox id="process_perf_data" value="#{serviceHandler.service.processPerfData}"/></td><td><h:message for="process_perf_data"/></td></tr>
				<tr><td>Retain Status Information:</td><td><h:selectBooleanCheckbox id="retain_status_info" value="#{serviceHandler.service.retainStatusInformation}"/></td><td><h:message for="retain_status_info"/></td></tr>
				<tr><td>Retain Non-Status Information:</td><td><h:selectBooleanCheckbox id="retain_nonstatus_info" value="#{serviceHandler.service.retainNonStatusInformation}"/></td><td><h:message for="retain_nonstatus_info"/></td></tr>
				<tr><td>Notifications Enabled:</td><td><h:selectBooleanCheckbox id="notifications_enabled" value="#{serviceHandler.service.notificationsEnabled}"/></td><td><h:message for="notifications_enabled"/></td></tr>
				<tr><td>Notificaton Period (*)</td><td><h:selectOneMenu id="notification_period" value="#{serviceHandler.service.notificationPeriod}" required="true">
									<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
								   </h:selectOneMenu>
				</td><td><h:message for="notification_period"/></td></tr>
				<tr><td>Notification Interval: (*)</td><td><h:inputText id="notification_interval" size="5" maxlength="2" value="#{serviceHandler.service.notificationInterval}" required="true"/></td><td><h:message for="notification_interval"/></td></tr>
				<tr><td>Notification Options: (*)</td><td>
						<h:selectManyCheckbox id="notification_options" value="#{serviceHandler.service.notificationOptions}" required="true">
							<f:selectItem itemValue="w" itemLabel="Warning"/>
							<f:selectItem itemValue="u" itemLabel="Unknown"/>
							<f:selectItem itemValue="c" itemLabel="Critical"/>
							<f:selectItem itemValue="r" itemLabel="Recoveries"/>
							<f:selectItem itemValue="f" itemLabel="Flapping"/>
							<f:selectItem itemValue="n" itemLabel="None"/>
						</h:selectManyCheckbox>					
	
				</td><td><h:message for="notification_options"/></td></tr>
							
				<tr><td>Stalking Options:</td><td>
						<h:selectManyCheckbox id="stalking_options" value="#{serviceHandler.service.stalkingOptions}">
							<f:selectItem itemValue="o" itemLabel="Ok"/>
							<f:selectItem itemValue="w" itemLabel="Warning"/>
							<f:selectItem itemValue="u" itemLabel="Unknown"/>
							<f:selectItem itemValue="c" itemLabel="Critical"/>
						</h:selectManyCheckbox>
				</td><td><h:message for="stalking_options"/></td></tr>
			
				<tr><td colspan="3"><p><u>Choose a command to use with this service:</u></p></td></tr>
				<tr><td colspan="2"><h:selectOneMenu id="check_command" value="#{serviceHandler.service.checkCommand}" required="true">
							<f:selectItems value="#{commandHandler.commandNames}"/>
						    </h:selectOneMenu></td><td><h:message for="check_command"/></td></tr>
				<tr><td>Command Args</td><td><h:inputText id="command_args" value="#{serviceHandler.service.commandArgs}" size="20" required="true"/></td><td><h:message for="command_args"/></td></tr>
				<tr><td colspan="3"><p><u>Select the host(s) on which you wish to monitor this service: (*)</u></p></td></tr>
				<tr><td colspan="2"><h:selectManyListbox id="select_hosts" value="#{serviceHandler.service.hostname}" size="3" required="true">
							<f:selectItems value="#{hostHandler.hostNames}"/>
						    </h:selectManyListbox></td><td><h:message for="select_hosts"/></td></tr>
				<tr><td colspan="3"><p><u>Select the contact group(s) responsible for this service: (*)</p></td></tr>
				<tr><td colspan="2"><h:selectManyListbox id="select_contact_groups" value="#{serviceHandler.service.contactGroups}" size="3" required="true">
							<f:selectItems value="#{groupHandler.contactGroupNames}"/>
						    </h:selectManyListbox></td><td><h:message for="select_contact_groups"/></td></tr>
				<tr><td colspan="3"><h:commandButton styleClass="button" value="Add Service" action="#{serviceHandler.addService}" rendered="#{serviceHandler.service.isModifiable==false}"/> <h:commandButton styleClass="button" value="Modify Service" action="#{serviceHandler.modifyService}" rendered="#{serviceHandler.service.isModifiable == true}"/> <h:commandButton styleClass="button" value="Save As Template" action="#{serviceHandler.addTemplate}" rendered="#{serviceHandler.service.isTemplate == false}"/> <input type="reset" class="button" value="Clear"></td></tr>		
			</h:form>
		</table>
	</c:otherwise>
</c:choose>

	
	<!-- End of add Service Form //-->	
<br/>
<h3>List of current Services:</h3>

	<c:choose>
		<c:when test="${serviceHandler.serviceCount == 0}">
		<p>There are currently no services defined</p>
		</c:when>
		<c:otherwise>
		<h:form>
			<h:dataTable rowClasses="even, odd" value="#{serviceHandler.sortedServiceData}" var="e" rows="#{serviceHandler.rowCount}" first="#{serviceHandler.firstRowIndex}">
				<h:column>
					<f:facet name="header">
						<h:commandLink action="#{serviceHandler.sortByServiceDescription}" immediate="true" title="Sort By Service Description">
							<h:outputText value="Service Description"/>
						</h:commandLink>
					</f:facet>
					<h:commandLink action="#{serviceHandler.select}" immediate="true" title="Show details for this Service">
						<h:outputText value="#{e.serviceDescription}"/>
					</h:commandLink>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="Delete"/>
					</f:facet>
					<h:commandLink action="#{serviceHandler.delResult}" actionListener="#{serviceHandler.deleteService}" immediate="true" title="Delete this Service">
						<f:param name="objectId" value="#{e.id}"/>
						<h:outputText value="Delete"/>
					</h:commandLink>
				</h:column>		
			</h:dataTable>
			<p>
			First: <h:commandButton id="first" value="<<" disabled="#{serviceHandler.scrollFirstDisabled}" action="#{serviceHandler.scrollFirst}" title="Scroll to First Page"/> <h:commandButton id="previous" value="<" disabled="#{serviceHandler.scrollFirstDisabled}" action="#{serviceHandler.scrollPrevious}" title="Scroll to the Previous Page"/> <h:commandButton id="next" value=">" disabled="#{serviceHandler.scrollLastDisabled}" action="#{serviceHandler.scrollNext}" title="Scroll to the Next Page"/> <h:commandButton value=">>" action="#{serviceHandler.scrollLast}" disabled="#{serviceHandler.scrollLastDisabled}" title="Scroll to the Last Page" id="last"/> : Last
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
