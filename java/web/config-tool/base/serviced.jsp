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
<h:inputHidden value="#{dependencyHandler.serviceDependencyCount}"/>

<c:if test="${accountHandler.inWizard}">
	<div id="wizard">
	<h3>Wizard Info:</h3>
	
	<p>A Service Dependency allows you to suppress the output from certain monitored services based around the status of other services.
	<p>Service Dependencies are again synonymous with the concept of Host Dependencies and the definition of Host Dependencies will not be discussed within this
	wizard.</p>
	<p>For many Service Dependencies are outside the range of monitoring requirements and therefore should you wish to continue the wizard without adding a
	Service Dependency, please click on the Skip button. If you want to know more about the individual attributes, click on the Help link.</p>
	
	<h:form>
		<h:commandButton styleClass="button" value="Skip" action="proceed-2"/>
	</h:form>
	
	</div>
</c:if>

<h3>Add a new Service Dependency<br/><a href="javascript:popUp('servicedhelp.html')">Need Help? Click Here!</a></h3>

<c:choose>
	<c:when test="${serviceHandler.serviceCount == 0}">
		<p>There are currently no Services defined. Please <a href="services.faces" title="Define a Service">define a Service</a> before continuing.</p>
	</c:when>
	<c:otherwise>
	
		<p>* Denotes a required field.</p>
		<table name="hostdependency_table">
			<h:form>
			<tr><td>Dependent Service Description: (*)</td><td><h:selectOneMenu value="#{dependencyHandler.serviceDependency.dependentServiceHost}" id="dependent_service_name" required="true">
								 <f:selectItems value="#{serviceHandler.serviceHostNames}"/>
								</h:selectOneMenu>
			</td><td><h:message for="dependent_service_name"/></td></tr>
			<tr><td>Service Description (*)</td><td><h:selectOneMenu id="service_description" value="#{dependencyHandler.serviceDependency.serviceHost}" required="true">
								<f:selectItems value="#{serviceHandler.serviceHostNames}"/>
							    </h:selectOneMenu></td><td><h:message for="service_description"/></td></tr>						
			<tr><td>Inherits Parents:</td><td><h:selectBooleanCheckbox id="inherits_parents" value="#{dependencyHandler.serviceDependency.inheritsParents}"/></td><td><h:message for="inherits_parents"/></td></tr>
			<tr><td>Execution Failure Criteria:</td><td><h:selectManyCheckbox id="execution_failure_criteria" value="#{dependencyHandler.serviceDependency.executionFailureCriteria}">
									<f:selectItem itemValue="o" itemLabel="Up"/>
									<f:selectItem itemValue="w" itemLabel="Warning"/>
									<f:selectItem itemValue="c" itemLabel="Critical"/>
									<f:selectItem itemValue="u" itemLabel="Unreachable"/>
									<f:selectItem itemValue="p" itemLabel="Pending"/>
									<f:selectItem itemValue="n" itemLabel="None"/>
								    </h:selectManyCheckbox>
			</td><td><h:message for="execution_failure_criteria"/></td></tr>
			<tr><td>Notification Failure Criteria:</td><td><h:selectManyCheckbox id="notification_failure_criteria" value="#{dependencyHandler.serviceDependency.notificationFailureCriteria}">
									 <f:selectItem itemValue="o" itemLabel="Up"/>
									 <f:selectItem itemValue="w" itemLabel="Warning"/>
									 <f:selectItem itemValue="c" itemLabel="Critical"/>
									 <f:selectItem itemValue="u" itemLabel="Unreachable"/>
									 <f:selectItem itemValue="p" itemLabel="Pending"/>
									 <f:selectItem itemValue="n" itemLabel="None"/>
									</h:selectManyCheckbox>
			</td><td><h:message for="notification_failure_criteria"/></td></tr>
			<tr><td colspan="2"><h:commandButton value="Add Dependency"styleClass="button" action="#{dependencyHandler.addResult}" actionListener="#{dependencyHandler.addDependency}" rendered="#{dependencyHandler.serviceDependency.isModifiable == false}">
						<f:attribute name="dependencyType" value="1"/>
					    </h:commandButton> 
					    <h:commandButton value="Modify Dependency" styleClass="button" action="#{dependencyHandler.modResult}" actionListener="#{dependencyHandler.modifyDependency}" rendered="#{dependencyHandler.serviceDependency.isModifiable}">
					    	<f:attribute name="dependencyType" value="1"/>
					    </h:commandButton>
			 <input type="reset" class="button" value="Clear"/></td></tr>
			</h:form>
		</table>	
	</c:otherwise>
</c:choose>

<br/>
<h3>List of current Service Dependencies:</h3>

	<c:choose>
		<c:when test="${dependencyHandler.serviceDependencyCount == 0}">
		<p>There are currently no Service Dependencies defined.</p>
		</c:when>
		<c:otherwise>
		<h:form>
			<h:dataTable rowClasses="even, odd" value="#{dependencyHandler.sortedServiceDependencyData}" var="e" rows="#{dependencyHandler.rowCount}" first="#{dependencyHandler.firstRowIndex}">
				<h:column>
					<f:facet name="header">
						<h:commandLink actionListener="#{dependencyHandler.sortByDependentHostname}" immediate="true" title="Sort By Dependent Host Name">
							<f:attribute name="dependencyType" value="1"/>
							<h:outputText value="Dependent Host Name"/>
						</h:commandLink>
					</f:facet>
					<h:commandLink actionListener="#{dependencyHandler.select}" action="#{dependencyHandler.selectResult}" title="View details for this dependency">
						<h:outputText value="#{e.dependentHostname}"/>
						<f:attribute name="dependencyType" value="1"/>
					</h:commandLink>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:commandLink actionListener="#{dependencyHandler.sortByName}" immediate="true" title="Sort By Host Name">
							<f:attribute name="dependencyType" value="1"/>
							<h:outputText value="Host Name"/>
						</h:commandLink>
					</f:facet>
					<h:commandLink actionListener="#{dependencyHandler.select}" action="#{dependencyHandler.selectResult}" immediate="true" title="View details for this dependency">
						<h:outputText value="#{e.hostname}"/>
						<f:attribute name="dependencyType" value="1"/>
					</h:commandLink>
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="Delete"/>
					</f:facet>
					<h:commandLink actionListener="#{dependencyHandler.deleteDependency}" action="#{dependencyHandler.delResult}" immediate="true" title="Delete this Dependency">
						<f:param name="objectId" value="#{e.id}"/>
						<f:param name="dependencyType" value="1"/>
						<h:outputText value="Delete"/>
					</h:commandLink>
				</h:column>
			</h:dataTable>
			<p>
			First: <h:commandButton id="first" value="<<" disabled="#{dependencyHandler.scrollFirstDisabled}" actionListener="#{dependencyHandler.scrollFirst}" title="Scroll to First Page">
					<f:attribute name="depType" value="1"/>
				</h:commandButton>
			 	<h:commandButton id="previous" value="<" disabled="#{dependencyHandler.scrollFirstDisabled}" actionListener="#{dependencyHandler.scrollPrevious}" title="Scroll to the Previous Page">
			 		<f:attribute name="depType" value="1"/>
			 	</h:commandButton>
			  	<h:commandButton id="next" value=">" disabled="#{dependencyHandler.scrollLastHostDisabled}" actionListener="#{dependencyHandler.scrollNext}" title="Scroll to the Next Page">
			  		<f:attribute name="depType" value="1"/>
			  	</h:commandButton>
			  	<h:commandButton value=">>" action="#{dependencyHandler.scrollLast}" disabled="#{dependencyHandler.scrollLastHostDisabled}" title="Scroll to the Last Page" id="last">
			  		<f:attribute name="depType" value="1"/>
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
