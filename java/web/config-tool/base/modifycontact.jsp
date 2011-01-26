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
<h3>Modify Contact details:</h3>

	<table name="contact_table">
		<h:form>
		<tr><td>Contact Name:</td><td><h:inputText id="contact_name" size="20" required="true" value="#{contactHandler.modifyContact.contactName}"/></td><td><h:message for="contact_name"/></td></tr>
		<tr><td>Alias:</td><td><h:inputText id="alias" size="20" value="#{contactHandler.modifyContact.alias}" required="true"/></td><td><h:message for="alias"/></td></tr>
		<tr><td>Host Notification Period:</td><td><h:selectOneMenu id="host_notification_period" value="#{contactHandler.modifyContact.hostNotificationPeriod}" required="true">
								<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
							  </h:selectOneMenu>
		</td><td><h:message for="host_notification_period"/></td></tr>
		<tr><td>Service Notification Period:</td><td><h:selectOneMenu id="service_notification_period" value="#{contactHandler.modifyContact.serviceNotificationPeriod}" required="true">
								<f:selectItems value="#{timePeriodHandler.timePeriodNames}"/>
							     </h:selectOneMenu>
		</td><td><h:message for="service_notification_period"/></td></tr>
		<tr><td>Host Notification Options:</td><td>
			<h:selectManyCheckbox id="host_notification_options" value="#{contactHandler.modifyContact.hostNotificationOptions}" required="true">
					<f:selectItem itemValue="d" itemLabel="Down"/>
					<f:selectItem itemValue="u" itemLabel="Unreachable"/>
					<f:selectItem itemValue="r" itemLabel="Recoveries"/>
					<f:selectItem itemValue="f" itemLabel="Flapping"/>
					<f:selectItem itemValue="n" itemLabel="None"/>
				</h:selectManyCheckbox>
		
		</td><td><h:message for="host_notification_options"/></td></tr>
		<tr><td>Service Notification Options:</td><td>
			<h:selectManyCheckbox id="service_notification_options" value="#{contactHandler.modifyContact.serviceNotificationOptions}" required="true">
					<f:selectItem itemValue="w" itemLabel="Warning"/>
					<f:selectItem itemValue="u" itemLabel="Unknown"/>
					<f:selectItem itemValue="c" itemLabel="Critical"/>
					<f:selectItem itemValue="r" itemLabel="Recovery"/>
					<f:selectItem itemValue="n" itemLabel="None"/>
			</h:selectManyCheckbox>					
		</td><td><h:message for="service_notification_options"/></td></tr>
		<tr><td>Host Notification Commands:</td><td><h:selectManyListbox id="host_notification_commands" value="#{contactHandler.modifyContact.hostNotificationCommands}" size="3" required="true">
								<f:selectItems value="#{commandHandler.commandNames}"/>
							    </h:selectManyListbox>
			
		</td><td><h:message for="host_notification_commands"/></td></tr>
		<tr><td>Service Notification Commands:</td><td><h:selectManyListbox id="service_notification_commands" value="#{contactHandler.modifyContact.serviceNotificationCommands}" size="3" required="true">
								 <f:selectItems value="#{commandHandler.commandNames}"/>
								</h:selectManyListbox>
		</td><td><h:message for="service_notification_commands"/></td></tr>
		<tr><td>Email:</td><td><h:inputText id="email" size="20" value="#{contactHandler.modifyContact.email}"/></td><td><h:message for="email"/></td></tr>
		<tr><td>Pager:</td><td><h:inputText id="pager" size="20" value="#{contactHandler.modifyContact.pager}"/></td><td><h:message for="pager"/></td></tr>
		<tr><td colspan="3"><h:commandButton id="modify_contact" value="Modify Contact" action="#{contactHandler.modifyContact}"/></td></tr>		
		</h:form>
	</table>	

</f:view>
<p>Use the form above to modify details of the Contact.</p>

</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
