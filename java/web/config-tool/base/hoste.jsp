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
<h3>Add a new Host Escalation <br/><a href="javascript:popUp('hostehelp.html')">Need Help? Click Here!</a></h3>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
<h:inputHidden value="#{groupHandler.contactGroupCount}"/>
<h:inputHidden value="#{hostHandler.hostCount}"/>
<h:inputHidden value="#{escalationHandler.hostEscalationCount}"/>

<c:choose>
	<c:when test="${groupHandler.contactGroupCount == 0 && hostHandler.hostCount == 0}">
		<p>There are currently no Contact Groups defined. Please <a href="contactgroups.faces" title="Define a Contact Group">define a Contact Group</a> before continuing.</p>
	</c:when>
	<c:when test="${groupHandler.contactGroupCount == 0 && hostHandler.hostCount > 0}">
		<p>There are currently no Contact Groups defined. Please <a href="contactgroups.faces" title="Define a Contact Group">define a Contact Group</a> before continuing.</p>
	</c:when>
	<c:when test="${hostHandler.hostCount == 0 && groupHandler.contactGroupCount > 0}">
		<p>There are currently no Hosts defined. Please <a href="hosts.faces" title="Define a Host">define a Host</a> before continuing.</p>
	</c:when>
	<c:otherwise>
		<p>* Denotes a required field.</p>
		<table name="hostescalation_table">
			<h:form>
			<tr><td>Host Name: (*)</td><td><h:selectOneMenu id="host_name" value="#{escalationHandler.hostEscalation.hostname}" required="true">
							<f:selectItems value="#{hostHandler.hostNames}"/>
						   </h:selectOneMenu>
			</td><td><h:message for="host_name"/></td></tr>
			<tr><td>Host Group Name:</td><td><h:selectManyListbox id="host_group" value="#{escalationHandler.hostEscalation.hostGroups}" size="3">
							  	<f:selectItems value="#{groupHandler.hostGroupNames}"/>
							  </h:selectManyListbox>
			</td><td><h:message for="host_group"/></td></tr>
			<tr><td>Contact Groups: (*)</td><td><h:selectManyListbox id="contact_groups" value="#{escalationHandler.hostEscalation.contactGroups}" size="3" required="true">
								<f:selectItems value="#{groupHandler.contactGroupNames}"/>
							</h:selectManyListbox>
			</td><td><h:message for="contact_groups"/></td></tr>
			<tr><td>First Notification: (*)</td><td><h:inputText id="first_notification" size="5" maxlength="3" value="#{escalationHandler.hostEscalation.firstNotification}" required="true"/></td><td><h:message for="first_notification"/></td></tr>
			<tr><td>Last Notification: (*)</td><td><h:inputText id="last_notification" size="5" maxlength="3" value="#{escalationHandler.hostEscalation.lastNotification}" required="true"/></td><td></td></tr>
			<tr><td>Notification Interval: (*)</td><td><h:inputText id="notification_interval" size="5" maxlength="3" value="#{escalationHandler.hostEscalation.notificationInterval}" required="true"/></td><td><h:message for="notification_interval"/></td></tr>
			<tr><td>Escalation Period:</td><td><h:selectOneMenu id="escalation_period" value="#{escalationHandler.hostEscalation.escalationPeriod}">
								<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
							    </h:selectOneMenu>
			</td><td><h:message for="escalation_period"/></td></tr>
			<tr><td>Escalation Options:</td><td><h:selectManyCheckbox id="escalation_options" value="#{escalationHandler.hostEscalation.escalationOptions}">
								<f:selectItem itemValue="r" itemLabel="Up"/>
								<f:selectItem itemValue="d" itemLabel="Down"/>
								<f:selectItem itemValue="u" itemLabel="UnReachable"/>
							     </h:selectManyCheckbox>
			</td><td><h:message for="escalation_options"/></td></tr>
			<tr><td colspan="2"><h:commandButton  styleClass="button" value="Save Escalation" action="#{escalationHandler.addResult}" actionListener="#{escalationHandler.addEscalation}" rendered="#{escalationHandler.hostEscalation.isModifiable == false}">
				<f:attribute name="escType" value="0"/>
				</h:commandButton>
				<h:commandButton value="Modify Escalation"  styleClass="button" action="#{escalationHandler.modResult}" actionListener="#{escalationHandler.modifyEscalation}" rendered="#{escalationHandler.hostEscalation.isModifiable}">
					<f:attribute name="escType" value="0"/>
				</h:commandButton>
				 <input type="reset"  class="button" value="Clear"/></td></tr>
			</h:form>
		</table>
	</c:otherwise>
</c:choose>	

<br/>
<h3>List of current Host Escalations:</h3>

	<c:choose>
		<c:when test="${escalationHandler.hostEscalationCount == 0}">
		<p>There are currently no Host Escalations defined.</p>
		</c:when>
		<c:otherwise>
		<h:form>
			<h:dataTable value="#{escalationHandler.sortedHostEscalationData}" var="e" rowClasses="even, odd" rows="#{escalationHandler.rowCount}" first="#{escalationHandler.firstRowIndex}">
				<h:column>
					<f:facet name="header">
						<h:commandLink actionListener="#{escalationHandler.sortByName}" immediate="true" title="Sort By Name">
							<f:attribute name="escType" value="0"/>
							<h:outputText value="Host Name"/>
						</h:commandLink>
					</f:facet>
					<h:commandLink actionListener="#{escalationHandler.select}" action="#{escalationHandler.selectResult}" immediate="true" title="View details for this Escalation">
						<h:outputText value="#{e.hostname}"/>
						<f:attribute name="escType" value="0"/>
					</h:commandLink>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="Delete"/>
					</f:facet>
					<h:commandLink actionListener="#{escalationHandler.deleteEscalation}" action="#{escalationHandler.delResult}" immediate="true" title="Delete this Escalation">
						<f:param name="objectId" value="#{e.id}"/>
						<f:param name="escType" value="0"/>
						<h:outputText value="Delete"/>
					</h:commandLink>
				</h:column>
			</h:dataTable>
			<p>
			First: <h:commandButton id="first" value="<<" disabled="#{escalationHandler.scrollFirstDisabled}" actionListener="#{escalationHandler.scrollFirst}" title="Scroll to First Page">
					<f:attribute name="escType" value="0"/>
				</h:commandButton>
			 	<h:commandButton id="previous" value="<" disabled="#{escalationHandler.scrollFirstDisabled}" actionListener="#{escalationHandler.scrollPrevious}" title="Scroll to the Previous Page">
			 		<f:attribute name="escType" value="0"/>
			 	</h:commandButton>
			  	<h:commandButton id="next" value=">" disabled="#{escalationHandler.scrollLastHostDisabled}" actionListener="#{escalationHandler.scrollNext}" title="Scroll to the Next Page">
			  		<f:attribute name="escType" value="0"/>
			  	</h:commandButton>
			  	<h:commandButton value=">>" action="#{escalationHandler.scrollLast}" disabled="#{escalationHandler.scrollLastHostDisabled}" title="Scroll to the Last Page" id="last">
			  		<f:attribute name="escType" value="0"/>
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
