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
</head>

<body><div id="content"><b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
<div id="nav">
	<%@ include file="menu.html" %>
</div>
<div id="main">


<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
<h:inputHidden value="#{serviceHandler.serviceCount}"/>
<h:inputHidden value="#{groupHandler.contactGroupCount}"/>
<h:inputHidden value="#{escalationHandler.serviceEscalationCount}"/>

<c:if test="${accountHandler.inWizard}">
<div id="wizard">
<h3>Wizard Info:</h3>
<p>A Service Escalation allows you to escalate any issues with a Service should they occur for a set period of time.</p>
<p>The use of Service Escalations is synonymous with that of Host Escalations, and for the sake of brevity the definition of Host Escalations are not included
in this wizard. For many Service Escalations are outside their monitoring requirements and for that reason this Wizard does not require you to add a Service Escalation. If you
wish to add a Service Escalation please do so; otherwise press the Skip button.</p>
<h:form>
		<h:commandButton styleClass="button" value="Skip" action="proceed"/>
</h:form>
</div>
</c:if>


<h3>Add a new Service Escalation<br/><a href="javascript:popUp('serviceehelp.html')">Need Help? Click Here!</a></h3>


<c:choose>
	<c:when test="${serviceHandler.serviceCount == 0 && groupHandler.contactGroupCount == 0}">
		<p>There are currently no Services defined. Please <a href="services.faces" title="Define a Service">define a Service</a> before continuing.</p>
	</c:when>
	
	<c:when test="${serviceHandler.serviceCount == 0 && groupHandler.contactGroupCount > 0}">
		<p>There are currently no Services defined. Please <a href="services.faces" title="Define a Service">define a Service</a> before continuing.</p>
	</c:when>
	
	<c:when test="${serviceHandler.serviceCount > 0 && groupHandler.contactGroupCount == 0}">
		<p>There are currently no Contact Groups defined. Please <a href="contactgroups.faces" title="Define a Contact Group">define a Contact Group</a> before continuing.</p>
	</c:when>	
	<c:otherwise>
		<p>* Denotes a required field.</p>
		<table name="serviceescalation_table">
			<h:form>
			<tr><td>Service Description: (*)</td><td><h:selectOneMenu id="service_description" value="#{escalationHandler.serviceEscalation.serviceHost}" required="true">
								<f:selectItems value="#{serviceHandler.serviceHostNames}"/>
							     </h:selectOneMenu>
			</td><td><h:message for="service_description"/></td></tr>
			<tr><td>Contact Groups: (*)</td><td><h:selectManyListbox id="contact_groups" size="3" value="#{escalationHandler.serviceEscalation.contactGroups}" required="true">
								<f:selectItems value="#{groupHandler.contactGroupNames}"/>
							</h:selectManyListbox>
			</td><td><h:message for="contact_groups"/></td></tr>
			<tr><td>First Notification: (*)</td><td><h:inputText id="first_notification" size="5" maxlength="3" value="#{escalationHandler.serviceEscalation.firstNotification}" required="true"/></td><td><h:message for="first_notification"/></td></tr>
			<tr><td>Last Notification: (*)</td><td><h:inputText id="last_notification" size="5" maxlength="3" value="#{escalationHandler.serviceEscalation.lastNotification}" required="true"/></td><td></td></tr>
			<tr><td>Notification Interval: (*)</td><td><h:inputText id="notification_interval" size="5" maxlength="3" value="#{escalationHandler.serviceEscalation.notificationInterval}" required="true"/></td><td><h:message for="notification_interval"/></td></tr>
			<tr><td>Escalation Period:</td><td><h:selectOneMenu id="escalation_period" value="#{escalationHandler.serviceEscalation.escalationPeriod}">
								<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
							   </h:selectOneMenu>
			</td><td><h:message for="escalation_period"/></td></tr>
			<tr><td>Escalation Options:</td><td><h:selectManyCheckbox id="escalation_options" value="#{escalationHandler.serviceEscalation.escalationOptions}">
								<f:selectItem itemValue="r" itemLabel="Ok"/>
								<f:selectItem itemValue="w" itemLabel="Warning"/>
								<f:selectItem itemValue="u" itemLabel="Unknown"/>
								<f:selectItem itemValue="c" itemLabel="Critical"/>
							     </h:selectManyCheckbox>
			</td><td><h:message for="escalation_options"/></td></tr>
			<tr><td colspan="3"><h:commandButton value="Save Escalation" styleClass="button" action="#{escalationHandler.addResult}" actionListener="#{escalationHandler.addEscalation}" rendered="#{escalationHandler.serviceEscalation.isModifiable == false}"> 
				<f:attribute name="escType" value="1"/>
				</h:commandButton>
				 <h:commandButton styleClass="button" value="Modify Escalation" action="#{escalationHandler.modResult}" actionListener="#{escalationHandler.modifyEscalation}" rendered="#{escalationHandler.serviceEscalation.isModifiable}">
				 	<f:attribute name="escType" value="1"/>
				 </h:commandButton>
				  <input type="reset" value="Clear" class="button"/> </td></tr>
			</h:form>
		</table>
	</c:otherwise>	
</c:choose>
<br/>
<h3>List of current Service Escalations:</h3>

	<c:choose>
		<c:when test="${escalationHandler.serviceEscalationCount == 0}">
		<p>There are currently no Service Escalations defined.</p>
		</c:when>
		<c:otherwise>
		<h:form>
			<h:dataTable value="#{escalationHandler.sortedServiceEscalationData}" var="e" rows="#{escalationHandler.rowCount}" first="#{escalationHandler.firstRowIndex}">
				<h:column>
					<f:facet name="header">
						<h:commandLink actionListener="#{escalationHandler.sortByName}" immediate="true" title="Sort By Name">
							<f:attribute name="escType" value="1"/>
							<h:outputText value="Host Name"/>
						</h:commandLink>
					</f:facet>
					<h:commandLink actionListener="#{escalationHandler.select}" action="#{escalationHandler.selectResult}" title="View Details for this Escalation" immediate="true">
						<h:outputText value="#{e.hostname}"/>
						<f:attribute name="escType" value="1"/>
					</h:commandLink>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="Service Description"/>
					</f:facet>
					<h:outputText value="#{e.serviceDescription}"/>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="Delete"/>
					</f:facet>
					<h:commandLink actionListener="#{escalationHandler.deleteEscalation}" action="#{escalationHandler.delResult}" immediate="true" title="Delete this Escalation">
						<f:param name="objectId" value="#{e.id}"/>
						<f:param name="escType" value="1"/>
						<h:outputText value="Delete"/>
					</h:commandLink>
				</h:column>
			</h:dataTable>
			<p>
			First: <h:commandButton id="first" value="<<" disabled="#{escalationHandler.scrollFirstDisabled}" actionListener="#{escalationHandler.scrollFirst}" title="Scroll to First Page">
					<f:attribute name="escType" value="1"/>
				</h:commandButton>
			 	<h:commandButton id="previous" value="<" disabled="#{escalationHandler.scrollFirstDisabled}" actionListener="#{escalationHandler.scrollPrevious}" title="Scroll to the Previous Page">
			 		<f:attribute name="escType" value="1"/>
			 	</h:commandButton>
			  	<h:commandButton id="next" value=">" disabled="#{escalationHandler.scrollLastHostDisabled}" actionListener="#{escalationHandler.scrollNext}" title="Scroll to the Next Page">
			  		<f:attribute name="escType" value="1"/>
			  	</h:commandButton>
			  	<h:commandButton value=">>" action="#{escalationHandler.scrollLast}" disabled="#{escalationHandler.scrollLastHostDisabled}" title="Scroll to the Last Page" id="last">
			  		<f:attribute name="escType" value="1"/>
			  	</h:commandButton> : Last
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
