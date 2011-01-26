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
<p>Page three of the main Blue configuration options. Keep going you're nearly there!</p>
</div>
</c:if>


<h3>Blue Configuration Part 3:</h3>
<h:form>
	<table>
		<tr><td>Use Aggressive Host Checking:</td><td><h:selectBooleanCheckbox id="uahc" value="#{blueConfigHandler.blueConfig.useAggressiveHostChecking}"/></td></tr>
		<tr><td>Enable Flap Detection:</td><td><h:selectBooleanCheckbox id="efd" value="#{blueConfigHandler.blueConfig.enableFlapDetection}"/></td></tr>
		<tr><td>Low Service Flap Threshold: (%)</td><td><h:inputText id="lsft" value="#{blueConfigHandler.blueConfig.lowServiceFlapThreshold}" size="5" maxlength="4"/></td></tr>
		<tr><td>High Service Flap Threshold: (%)</td><td><h:inputText id="hsft" value="#{blueConfigHandler.blueConfig.highServiceFlapThreshold}" size="5" maxlength="4"/></td></tr>
		<tr><td>Low Host Flap Threshold: (%)</td><td><h:inputText id="lhft" value="#{blueConfigHandler.blueConfig.lowHostFlapThreshold}" size="5" maxlength="4"/></td></tr>
		<tr><td>High Host Flap Threshold: (%)</td><td><h:inputText id="hhft" value="#{blueConfigHandler.blueConfig.highHostFlapThreshold}" size="5" maxlength="4"/></td></tr>
		<tr><td>Soft State Dependencies:</td><td><h:selectBooleanCheckbox id="ssd" value="#{blueConfigHandler.blueConfig.softStateDependencies}"/></td></tr>
		<tr><td>Service Check Timeout: (secs)</td><td><h:inputText id="sct" value="#{blueConfigHandler.blueConfig.serviceCheckTimeout}" size="5" maxlength="3"/></td></tr>
		<tr><td>Host Check Timeout: (secs)</td><td><h:inputText id="hct" value="#{blueConfigHandler.blueConfig.hostCheckTimeout}" size="5" maxlength="3"/></td></tr>
		<tr><td>Event Handler Timeout: (secs)</td><td><h:inputText id="eht" value="#{blueConfigHandler.blueConfig.eventHandlerTimeout}" size="5" maxlength="3"/></td></tr>
		<tr><td>Notification Timeout: (secs)</td><td><h:inputText id="nt" value="#{blueConfigHandler.blueConfig.notificationTimeout}" size="5" maxlength="3"/></td></tr>
		<tr><td>Obsessive Compulsive Service Processor Timeout: (secs)</td><td><h:inputText id="octo" value="#{blueConfigHandler.blueConfig.ocspTimeout}" size="5" maxlength="3"/></td></tr>
		<tr><td>Obsessive Compulsive Host Processor Timeout: (secs)</td><td><h:inputText id="ochpt" value="#{blueConfigHandler.blueConfig.ochpTimeout}" size="5" maxlength="3"/></td></tr>
		<tr><td>Perf Data Processor Command Timeout: (secs)</td><td><h:inputText id="pdpct" value="#{blueConfigHandler.blueConfig.perfdataTimeout}" size="5" maxlength="3"/></td></tr>
		<tr><td>Obsess Over Services:</td><td><h:selectBooleanCheckbox id="oos" value="#{blueConfigHandler.blueConfig.obsessOverServices}"/></td></tr>
		<tr><td>Obsessive Compulsive Service Processor Command:</td><td><h:selectOneMenu id="ocspc" value="#{blueConfigHandler.blueConfig.ocspCommand}">
										  <f:selectItems value="#{commandHandler.commandNames}"/>
										 </h:selectOneMenu></td></tr>
		<tr><td>Obsess Over Hosts:</td><td><h:selectBooleanCheckbox id="ooh" value="#{blueConfigHandler.blueConfig.obsessOverHosts}"/></td></tr>
		<tr><td>Obsessive Compulsive Host Processor Command:</td><td><h:selectOneMenu id="ochpc" value="#{blueConfigHandler.blueConfig.ochpCommand}">
										<f:selectItems value="#{commandHandler.commandNames}"/>
									     </h:selectOneMenu></td></tr>
		<tr><td>Process Performance Data:</td><td><h:selectBooleanCheckbox id="ppd" value="#{blueConfigHandler.blueConfig.processPerformanceData}"/></td></tr>
		<tr><td>Host PerfData Processing Command:</td><td><h:selectOneMenu id="hppc" value="#{blueConfigHandler.blueConfig.hostPerfdataCommand}">
									<f:selectItems value="#{commandHandler.commandNames}"/>
								  </h:selectOneMenu></td></tr>
		<tr><td>Service PerfData Processing Command:</td><td><h:selectOneMenu id="sppc" value="#{blueConfigHandler.blueConfig.servicePerfdataCommand}">
									<f:selectItems value="#{commandHandler.commandNames}"/>
								  </h:selectOneMenu></td></tr>
		<tr><td>Host PerfData File:</td><td><h:inputText id="hpdf" value="#{blueConfigHandler.blueConfig.hostPerfdataFile}" size="20"/></td></tr>
		<tr><td>Service PerfData File:</td><td><h:inputText id="spdf" value="#{blueConfigHandler.blueConfig.servicePerfdataFile}" size="20"/></td></tr>
		<tr><td>Host PerfData File Template:</td><td><h:inputText id="hpdft" value="#{blueConfigHandler.blueConfig.hostPerfdataFileTemplate}" size="45"/></td></tr>
		<tr><td>Service PerfData File Template:</td><td><h:inputText id="spdft" value="#{blueConfigHandler.blueConfig.servicePerfdataFileTemplate}" size="45"/></td></tr>
		<tr><td colspan="2"><h:commandButton value="Save Config"  styleClass="button" action="#{blueConfigHandler.saveResult}" actionListener="#{blueConfigHandler.saveOptions}">
		
					<f:attribute name="stage" value="3"/>
				    </h:commandButton>
		 <h:commandButton value="Next Page >>"  styleClass="button" action="next-page-3"/></td></tr>
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
