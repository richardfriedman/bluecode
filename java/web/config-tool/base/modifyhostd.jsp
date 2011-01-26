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
<h3>Modify Host Dependency:</h3>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
	<table name="hostdependency_table">
		<h:form>
		<tr><td>Dependent Host Name:</td><td><h:selectOneMenu id="dependent_host_name" value="#{dependencyHandler.modifyHostDependency.dependentHostname}" required="true">
							<f:selectItems value="#{hostHandler.hostNames}"/>
						     </h:selectOneMenu>
		</td><td><h:message for="dependent_host_name"/></td></tr>
		<tr><td>Host Name:</td><td><h:selectOneMenu id="host_name" value="#{dependencyHandler.modifyHostDependency.hostname}" required="true">
						<f:selectItems value="#{hostHandler.hostNames}"/>
					   </h:selectOneMenu>
		</td><td><h:message for="host_name"/></td></tr>
		<tr><td>Inherits Parents:</td><td><h:selectBooleanCheckbox id="inherits_parents" value="#{dependencyHandler.modifyHostDependency.inheritsParents}"/></td><td><h:message for="inherits_parents"/></td></tr>
		<tr><td>Execution Failure Criteria:</td><td><h:selectManyCheckbox id="execution_failure_criteria" value="#{dependencyHandler.modifyHostDependency.executionFailureCriteria}">
								<f:selectItem itemValue="o" itemLabel="Up"/>
								<f:selectItem itemValue="d" itemLabel="Down"/>
								<f:selectItem itemValue="u" itemLabel="Unreachable"/>
								<f:selectItem itemValue="p" itemLabel="Pending"/>
								<f:selectItem itemValue="n" itemLabel="None"/>
							    </h:selectManyCheckbox>
		</td><td><h:message for="execution_failure_criteria"/></td></tr>
		<tr><td>Notification Failure Criteria:</td><td><h:selectManyCheckbox id="notification_failure_criteria" value="#{dependencyHandler.modifyHostDependency.notificationFailureCriteria}">
								 <f:selectItem itemValue="o" itemLabel="Up"/>
								 <f:selectItem itemValue="d" itemLabel="Down"/>
								 <f:selectItem itemValue="u" itemLabel="Unreachable"/>
								 <f:selectItem itemValue="p" itemLabel="Pending"/>
								 <f:selectItem itemValue="n" itemLabel="None"/>
								</h:selectManyCheckbox>
		</td><td><h:message for="notification_failure_criteria"/></td></tr>
		<tr><td colspan="2"><h:commandButton value="Modify Dependency" action="#{dependencyHandler.modResult}" actionListener="#{dependencyHandler.modifyDependency}">
				    	<f:attribute name="dependencyType" value="0"/>
				    </h:commandButton></td></tr>
		
		</h:form>
	</table>	
</f:view>
<p>Use the form above to update details of the Host Dependency.</p>
</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
