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

<c:if test="${accountHandler.inWizard}">
	<div id="wizard">
	<h3>Wizard Info:</h3>
	<p>As the name would suggest a contact is a person who is contacted should certain events occur.</p>
	<p>On this page you can provide details such as the name of the person to contact, their email address and also other contact methods such as their pager. You can
	also specify a command that should be run when needing to contact this person, and the Time Period in which it is valid to contact this person. 
	<p>For the sake of the wizard we will add a single contact who will be notified within the 24x7 time Period using the command email_contact. Should you need
	further help, click on the Help link.</p>
	</div>
</c:if>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
<h:inputHidden value="#{timePeriodHandler.timePeriodCount}"/>
<h:inputHidden value="#{commandHandler.commandCount}"/>

<h3>Add a new Contact<br/>
<a href="#" onclick="new Effect.toggle('search','slide',{duration:0.5})">(Search for Contacts/</a>
<c:choose>
	<c:when test="${contactHandler.templateCount == 0}">
		<span class="disabled">No Templates Available)</span></a>
	</c:when>
	<c:otherwise>
<a href="#"  onclick="new Effect.toggle('template','slide',{duration:0.5})">Use Template)</a>
	</c:otherwise>
</c:choose>
 <a href="javascript:popUp('contacthelp.html')">Need Help? Click Here!</a>
</h3>
	<div id="search" style="display: none" class="dropbox">
		<h:form>
			<label for="contact_search">Contact Name:</label>	<h:inputText size="20" id="contact_search" maxlength="30" value="#{searchHandler.searchString}" required="true"/><h:message for="contact_search"/>
						<h:commandButton value="Search" action="#{searchHandler.searchObjects}" actionListener="#{searchHandler.setSearchType}">
							<f:attribute name="searchType" value="4"/>
	     				</h:commandButton>
						
		</h:form>
	</div>
	<div id="template" style="display: none" class="dropbox">
		<h:form id="template_form">
		<label for="template_selector">Template Name:</label>	<h:selectOneListbox id="template_selector" value="#{contactHandler.templateToLoad}" size="1">
			<f:selectItems value="#{contactHandler.templateNames}"/>
    	  </h:selectOneListbox>
	    		 <h:commandButton action="#{contactHandler.useTemplate}" value="Load"/>
    	  </h:form>
		<h:message id="template_message" for="template_selector"/></td></div>

<c:choose>
	<c:when test="${timePeriodHandler.timePeriodCount == 0 && commandHandler.commandCount ==0}">
		<p>There are currently no Time Periods defined. Please <a href="timeperiods.faces" title="Define a Time Period">define a Time Period</a> before continuing.</p>
	</c:when>
	<c:when test="${commandHandler.commandCount == 0 && timePeriodHandler.timePeriodCount > 0}">
		<p>There are currently no Commands defined. Please <a href="commands.faces" title="Define a Command">define a Command</a> before continuing.</p>
	</c:when>
	<c:when test="$timePeriodHandler.timePeriodCount == 0 && commandHandler.commandCount > 0}">
		<p>There are currently no Time Periods defined. Please <a href="timeperiods.faces" title="Define a Time Period">define a Time Period</a> before continuing.</p>
	</c:when>
	<c:otherwise>
		<p>* Denotes required field.</p>
		<table name="contact_table">
			<h:form>
			<tr><td>Contact Name: (*)</td><td><h:inputText id="contact_name" size="20" required="true" value="#{contactHandler.contact.contactName}"/></td><td><h:message for="contact_name"/></td></tr>
			<tr><td>Alias: (*)</td><td><h:inputText id="alias" size="20" value="#{contactHandler.contact.alias}" required="true"/></td><td><h:message for="alias"/></td></tr>
			<tr><td>Host Notification Period: (*)</td><td><h:selectOneMenu id="host_notification_period" value="#{contactHandler.contact.hostNotificationPeriod}" required="true">
									<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
								  </h:selectOneMenu>
			</td><td><h:message for="host_notification_period"/></td></tr>
			<tr><td>Service Notification Period: (*)</td><td><h:selectOneMenu id="service_notification_period" value="#{contactHandler.contact.serviceNotificationPeriod}" required="true">
									<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
								     </h:selectOneMenu>
			</td><td><h:message for="service_notification_period"/></td></tr>
			<tr><td>Host Notification Options: (*)</td><td>
				<h:selectManyCheckbox id="host_notification_options" value="#{contactHandler.contact.hostNotificationOptions}" required="true">
						<f:selectItem itemValue="d" itemLabel="Down"/>
						<f:selectItem itemValue="u" itemLabel="Unreachable"/>
						<f:selectItem itemValue="r" itemLabel="Recoveries"/>
						<f:selectItem itemValue="f" itemLabel="Flapping"/>
						<f:selectItem itemValue="n" itemLabel="None"/>
					</h:selectManyCheckbox>
			
			</td><td><h:message for="host_notification_options"/></td></tr>
			<tr><td>Service Notification Options: (*)</td><td>
				<h:selectManyCheckbox id="service_notification_options" value="#{contactHandler.contact.serviceNotificationOptions}" required="true">
						<f:selectItem itemValue="w" itemLabel="Warning"/>
						<f:selectItem itemValue="u" itemLabel="Unknown"/>
						<f:selectItem itemValue="c" itemLabel="Critical"/>
						<f:selectItem itemValue="r" itemLabel="Recovery"/>
						<f:selectItem itemValue="n" itemLabel="None"/>
				</h:selectManyCheckbox>					
			</td><td><h:message for="service_notification_options"/></td></tr>
			<tr><td>Host Notification Commands:</td><td><h:selectManyListbox id="host_notification_commands" value="#{contactHandler.contact.hostNotificationCommands}" size="3" required="true">
									<f:selectItems value="#{commandHandler.commandNames}"/>
								    </h:selectManyListbox>
				
			</td><td><h:message for="host_notification_commands"/></td></tr>
			<tr><td>Service Notification Commands:</td><td><h:selectManyListbox id="service_notification_commands" value="#{contactHandler.contact.serviceNotificationCommands}" size="3" required="true">
									 <f:selectItems value="#{commandHandler.commandNames}"/>
									</h:selectManyListbox>
			</td><td><h:message for="service_notification_commands"/></td></tr>
			<tr><td>Email:</td><td><h:inputText id="email" size="20" value="#{contactHandler.contact.email}"/></td><td><h:message for="email"/></td></tr>
			<tr><td>Pager:</td><td><h:inputText id="pager" size="20" value="#{contactHandler.contact.pager}"/></td><td><h:message for="pager"/></td></tr>
			<tr><td colspan="3"><h:commandButton  styleClass="button" value="Save Contact" action="#{contactHandler.addContact}" rendered="#{contactHandler.contact.isModifiable == false}"/> <h:commandButton  styleClass="button" id="modify_contact" value="Modify Contact" rendered="#{contactHandler.contact.isModifiable}" action="#{contactHandler.modifyContact}"/> <h:commandButton value="Save As Template"  styleClass="button" action="#{contactHandler.addTemplate}" rendered="#{contactHandler.contact.isTemplate == false}"/> <input type="reset" class="button" value="Clear"></td></tr>		
			</h:form>
		</table>
	</c:otherwise>
</c:choose>	
<br/>

<h3>List of current contacts:</h3>
<p>

<c:choose>
<c:when test="${contactHandler.contactCount==0}">
	<p>There are currently no Contacts defined</p>
</c:when>
<c:otherwise>
<h:form>
		<h:dataTable value="#{contactHandler.sortedContactDetails}" rowClasses="even, odd" var="e" rows="#{contactHandler.rowCount}" first="#{contactHandler.firstRowIndex}">
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{contactHandler.sortByContactName}" immediate="true" title="Sort By Contact Name">
						<h:outputText value="Contact Name"/>
					</h:commandLink>
				</f:facet>
				<h:commandLink action="#{contactHandler.select}" immediate="true" title="View Details for this Contact">
					<h:outputText value="#{e.contactName}"/>			
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{contactHandler.sortByAlias}" immediate="true" title="Sort By Alias">
						<h:outputText value="Alias"/>
					</h:commandLink>
				</f:facet>
				<h:outputText value="#{e.alias}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{contactHandler.sortByEmailAddress}" immediate="true" title="Sort By Email Address">
						<h:outputText value="Email Address"/>
					</h:commandLink>
				</f:facet>
				<h:outputText value="#{e.email}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="Delete"/>
				</f:facet>
				<h:commandLink  action="#{contactHandler.delResult}" actionListener="#{contactHandler.deleteContact}" immediate="true" title="Delete this Contact">
					<f:param name="objectId" value="#{e.id}"/>
					<h:outputText value="Delete"/>
				</h:commandLink>
			</h:column>	
		</h:dataTable>
		<p>
		First: <h:commandButton value="<<" disabled="#{contactHandler.scrollFirstDisabled}" action="#{contactHandler.scrollFirst}" title="Scroll to First Page"/> <h:commandButton value="<" disabled="#{contactHandler.scrollFirstDisabled}" action="#{contactHandler.scrollPrevious}" title="Scroll to the Previous Page"/> <h:commandButton value=">" disabled="#{contactHandler.scrollLastDisabled}" action="#{contactHandler.scrollNext}" title="Scroll to the Next Page"/> <h:commandButton value=">>" action="#{contactHandler.scrollLast}" disabled="#{contactHandler.scrollLastDisabled}" title="Scroll to the Last Page"/> : Last
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
