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
<h3>Welcome to Blue::Open Source System and Network Monitoring</h3>


<p>Welcome to the Blue Configuration Interface beta 0.1. The aim of this interface is to enabled you to easily configure Blue and manage your configuration as your Blue installation grows.</p>


<h3>Getting started:</h3>
<f:view>
	<h:form>
		<h:inputHidden value="#{accountHandler.outputLocation}"/>
		
		<c:choose>
			<c:when test="${accountHandler.hasRun == false && accountHandler.inWizard == false && accountHandler.userSetDirectory == false}">
				<p>It appears that this is the first time that you have used the Blue configuration Interface.</p>
				<p>Before we begin please specify a location to store your configuration files: (We recommend the using the config directory of your Blue installation)
				<table>
					<tr><td><p>Output Directory:</p></td><td><h:inputText id="output_directory" value="#{accountHandler.outputLocation}" size="50" required="true"/></td><td><h:message for="output_directory"/></td></tr>
					<tr><td colspan="2"/><h:commandButton  styleClass="button" value="Set Directory" action="#{accountHandler.setInitialOutputLocation}"/></td></tr>
				</table>
				
			</c:when>
			<c:when test="${accountHandler.userSetDirectory && accountHandler.hasRun == false && accountHandler.inWizard == false}">
				<p>Either <a href="import.faces" title="Import your configuration">Import your old configuration</a>, or use our <h:commandLink id="start_wizard" action="#{accountHandler.selectWizard}" value="wizard" title="Use the wizard!"/> to get started!</p>
			</c:when>
			<c:when test="${accountHandler.inWizard && accountHandler.userSetDirectory}">
				<p>Welcome to the Blue wizard! The aim of this wizard is to give you an introduction into how to configure Blue to meet your needs.</p>
				<p>This wizard will guide you through the process of adding the objects you require to begin monitoring a single host. Once you have
				   completed the wizard, you will be free to modify your configuration to suit your needs!</p>
				
				<p><h:commandButton  styleClass="button" value="Begin Wizard!" action="#{accountHandler.beginWizard}" title="Begin the Wizard!"/></p>
		  	</c:when>
		  	<c:when test="${accountHandler.hasRun == true && accountHandler.userSetDirectory == true && accountHandler.inWizard == false}">
		  		
		  		<h5>Your configuration files are output to: <h:outputText value="#{accountHandler.outputLocation}"/></h5>
		  		<p>Please select from the links on the left hand side or use the icons below to manage your configuration.</p>
		  		<p>Note: you will need to restart your Blue server for changes made here to take effect in your monitoring setup.</p>
				<h5><img src="images/icon_hosts.png" align="absbottom" /> <a href="hosts.faces" title="Manage your Hosts">Hosts</a></h5>
<h5><img src="images/icon_services.png" align="absbottom" /> <a href="services.faces" title="Manage your Services">Services</a></h5>
<h5><img src="images/icon_contacts.png" align="absbottom" /> <a href="contacts.faces" title="Manage your Contacts">Contacts</a></h5>
<h5><img src="images/icon_time.png" align="absbottom" /> <a href="timeperiods.faces" title="Manage your Time Periods">Time Periods</a></h5>
<h5><img src="images/icon_commands.png" align="absbottom" /> <a href="commands.faces" title="Manage your Commands">Commands</a></h5>
		  	</c:when>
		  	
		  </c:choose>
	</h:form>
</f:view>
<p>
For More Information about this Tool, <a href="javascript:popUp('mainhelp.html')" title="Help with this Tool.">click here</a>
</p>
</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
