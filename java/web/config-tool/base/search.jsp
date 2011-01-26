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
	<h:form>
	You Searched For: <h:outputText value="#{searchHandler.searchString}"/>
	
	</h:form>



<!-- Display search results here //-->
<h3>Your Search Results:</h3>
<h:form>

<h:dataTable value="#{searchHandler.objectSearchResults}" var="e" rowClasses="even, odd" id="result_table_hosts" rendered="#{searchHandler.objectSearchFor == 0}">
	<h:column>
		<f:facet name="header">
			<h:outputText value="Hostname"/>
		</f:facet>
		<h:commandLink actionListener="#{hostHandler.loadHostById}" action="#{hostHandler.selectResult}" title="View Details for this Host!">
			<f:param name="host_id" value="#{e.id}"/>
			<h:outputText value="#{e.hostname}"/>
		</h:commandLink>
	</h:column>
</h:dataTable>

<h:dataTable value="#{searchHandler.objectSearchResults}"  rowClasses="even, odd" var="e" id="result_table_hostgroups" rendered="#{searchHandler.objectSearchFor == 1}">
	<h:column>
		<f:facet name="header">
			<h:outputText value="Host Group Name"/>
		</f:facet>
		<h:commandLink actionListener="#{groupHandler.loadGroupById}" action="#{groupHandler.selectResult}" title="View Details for this Host Group!">
			<f:param name="group_id" value="#{e.id}"/>
			<f:param name="group_type" value="0"/>
			<h:outputText value="#{e.name}"/>
		</h:commandLink>
	</h:column>
</h:dataTable>

<h:dataTable value="#{searchHandler.objectSearchResults}"  rowClasses="even, odd" var="e" id="result_table_services" rendered="#{searchHandler.objectSearchFor == 2}">
	<h:column>
		<f:facet name="header">
			<h:outputText value="Service Description"/>
		</f:facet>
		<h:commandLink actionListener="#{serviceHandler.loadServiceById}" action="#{serviceHandler.selectResult}" title="View Details for this Service!">
			<f:param name="service_id" value="#{e.id}"/>
			<h:outputText value="#{e.serviceDescription}"/>
		</h:commandLink>
	</h:column>
</h:dataTable>

<h:dataTable value="#{searchHandler.objectSearchResults}"  rowClasses="even, odd" var="e" id="result_table_servicegroups" rendered="#{searchHandler.objectSearchFor == 3}">
	<h:column>
		<f:facet name="header">
			<h:outputText value="Service Group Name"/>
		</f:facet>
		<h:commandLink actionListener="#{groupHandler.loadGroupById}" action="#{groupHandler.selectResult}" title="View Details for this Service Group!">
			<f:param name="group_id" value="#{e.id}"/>
			<f:param name="group_type" value="1"/>
			<h:outputText value="#{e.name}"/>
		</h:commandLink>
	</h:column>
</h:dataTable>

<h:dataTable value="#{searchHandler.objectSearchResults}" var="e"  rowClasses="even, odd" id="result_table_contacts" rendered="#{searchHandler.objectSearchFor == 4}">
	<h:column>
		<f:facet name="header">
			<h:outputText value="Contact Name"/>
		</f:facet>
		<h:commandLink actionListener="#{contactHandler.loadContactById}" action="#{contactHandler.selectResult}" title="View Details for this Contact!">
			<f:param name="contact_id" value="#{e.id}"/>
			<h:outputText value="#{e.contactName}"/>
		</h:commandLink>
	</h:column>
</h:dataTable>

<h:dataTable value="#{searchHandler.objectSearchResults}" var="e"  rowClasses="even, odd" id="result_table_contactgroups" rendered="#{searchHandler.objectSearchFor == 5}">
	<h:column>
		<f:facet name="header">
			<h:outputText value="Contact Group Name"/>
		</f:facet>
		<h:commandLink actionListener="#{groupHandler.loadGroupById}" action="#{groupHandler.selectResult}" title="View Details for this Contact Group!">
			<f:param name="group_id" value="#{e.id}"/>
			<f:param name="group_type" value="2"/>
			<h:outputText value="#{e.name}"/>
		</h:commandLink>
	</h:column>
</h:dataTable>

<h:dataTable value="#{searchHandler.objectSearchResults}" var="e"  rowClasses="even, odd" id="result_table_timePeriods" rendered="#{searchHandler.objectSearchFor == 6}">
	<h:column>
		<f:facet name="header">
			<h:outputText value="Time Period Name"/>
		</f:facet>
		<h:commandLink actionListener="#{timePeriodHandler.loadTimePeriodById}" action="#{timePeriodHandler.selectResult}" title="View Details for this Time Period!">
			<f:param name="timeperiod_id" value="#{e.id}"/>
			<h:outputText value="#{e.name}"/>
		</h:commandLink>
	</h:column>
</h:dataTable>
<h:dataTable value="#{searchHandler.objectSearchResults}" var="e" rowClasses="even, odd"  id="result_table_commands" rendered="#{searchHandler.objectSearchFor == 7}">
	<h:column id="name_column">
		<f:facet name="header">
			<h:outputText value="Command Name" id="header_name"/>
		</f:facet>
		<h:commandLink action="#{commandHandler.selectResult}" actionListener="#{commandHandler.loadCommandById}">
			<h:outputText value="#{e.name}" id="name_output"/>
			<f:param name="command_id" value="#{e.id}"/>
		</h:commandLink>
	</h:column>
</h:dataTable>


</h:form>

</f:view>
</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
