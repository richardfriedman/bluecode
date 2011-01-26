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
<p>The final page of Blue configuration options.
</div>

</c:if>


<h3>Blue Configuration:</h3>
<h:form>
	<table>
		<tr><td>Host PerfData File Mode:</td><td><h:inputText id="hpfm" value="#{blueConfigHandler.blueConfig.hostPerfdataFileMode}" size="3" maxlength="1"/></td></tr>
		<tr><td>Service PerfData File Mode:</td><td><h:inputText id="spfm" value="#{blueConfigHandler.blueConfig.servicePerfdataFileMode}" size="3" maxlength="1"/></td></tr>
		<tr><td>Host Perfdata Processing Interval: (secs)</td><td><h:inputText id="hppi" value="#{blueConfigHandler.blueConfig.hostPerfdataFileProcessingInterval}" size="5" maxlength="3"/></td></tr>
		<tr><td>Service Perfdata Processing Interval: (secs)</td><td><h:inputText id="sppi" value="#{blueConfigHandler.blueConfig.servicePerfdataFileProcessingInterval}" size="5" maxlength="3"/></td></tr>
		<tr><td>Host PerfData File Processing Command:</td><td><h:selectOneMenu id="hpfpc" value="#{blueConfigHandler.blueConfig.hostPerfdataFileProcessingCommand}">
										<f:selectItems value="#{commandHandler.commandNames}"/>
									</h:selectOneMenu></td></tr>
		<tr><td>Service PerfData File Processing Command:</td><td><h:selectOneMenu id="spfpc" value="#{blueConfigHandler.blueConfig.servicePerfdataFileProcessingCommand}">
										<f:selectItems value="#{commandHandler.commandNames}"/>
									</h:selectOneMenu></td></tr>
		<tr><td>Check For Orphaned Services:</td><td><h:selectBooleanCheckbox id="cfos" value="#{blueConfigHandler.blueConfig.checkForOrphanedServices}"/></td></tr>
		<tr><td>Check Service Freshness:</td><td><h:selectBooleanCheckbox id="csf" value="#{blueConfigHandler.blueConfig.checkServiceFreshness}"/></td></tr>
		<tr><td>Service Freshness Check Interval: (secs)</td><td><h:inputText id="sfci" value="#{blueConfigHandler.blueConfig.serviceFreshnessCheckInterval}" size="5" maxlength="3"/></td></tr>
		<tr><td>Check Host Freshness:</td><td><h:selectBooleanCheckbox id="chf" value="#{blueConfigHandler.blueConfig.checkHostFreshness}"/></td></tr>
		<tr><td>Host Freshness Check Interval: (secs)</td><td><h:inputText id="hfci" value="#{blueConfigHandler.blueConfig.hostFreshnessCheckInterval}" size="5" maxlength="3"/></td></tr>
		<tr><td>Date Format:</td><td><h:selectOneMenu id="date_format" value="#{blueConfigHandler.blueConfig.dateFormat}">
							<f:selectItem itemValue="us" itemLabel="MM/DD/YYYY HH:MM:SS"/>
							<f:selectItem itemValue="euro" itemLabel="DD/MM/YYYY HH:MM:SS"/>
							<f:selectItem itemValue="iso8601" itemLabel="YYYY-MM-DD HH:MM:SS"/>
							<f:selectItem itemValue="strict-iso8601" itemLabel="YYYY-MM-DDTHH:MM:SS"/>
					     </h:selectOneMenu></td></tr>
		<tr><td>Illegal Object Name Characters:</td><td><h:inputText id="ionc" value="#{blueConfigHandler.blueConfig.illegalObjectNameChars}" size="20"/></td></tr>
		<tr><td>Illegal Macro Output Characters:</td><td><h:inputText id="imoc" value="#{blueConfigHandler.blueConfig.illegalMacroOutputChars}" size="20"/></td></tr>
		<tr><td>Regular Expression Matching:</td><td><h:selectBooleanCheckbox id="rem" value="#{blueConfigHandler.blueConfig.useRegexpMatching}"/></td></tr>
		<tr><td>True Regular Expression Matching:</td><td><h:selectBooleanCheckbox id="trem" value="#{blueConfigHandler.blueConfig.useTrueRegexpMatching}"/></td></tr>
		<tr><td>Admin Email:</td><td><h:inputText value="#{blueConfigHandler.blueConfig.adminEmail}" size="20" id="ae"/></td></tr>
		<tr><td>Admin Pager:</td><td><h:inputText value="#{blueConfigHandler.blueConfig.adminPager}" size="20" id="ap"/></td></tr>
		<tr><td colspan="2"><h:commandButton value="Save Config"  styleClass="button" action="#{blueConfigHandler.saveResult}" actionListener="#{blueConfigHandler.saveOptions}">
					<f:attribute name="stage" value="4"/>
				    </h:commandButton>
		 </td></tr>
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
