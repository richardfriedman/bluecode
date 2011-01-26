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
<h3>Add a new Host Dependency <br/><a href="javascript:popUp('hostdhelp.html')">Need Help? Click Here!</a></h3>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
<h:inputHidden value="#{hostHandler.hostCount}"/>
<h:inputHidden value="#{dependencyHandler.hostDependencyCount}"/>

<c:choose>
	<c:when test="${hostHandler.hostCount == 0}">
		<p>There are currently no Hosts defined. Please <a href="hosts.faces" title="Define a Host">define a Host</a> before continuing.</p>
	</c:when>
	<c:otherwise>
		<p>* Denotes a required field.</p>
		<table name="hostdependency_table">
			<h:form>
			<tr><td>Dependent Host Name: (*)</td><td><h:selectOneMenu id="dependent_host_name" value="#{dependencyHandler.hostDependency.dependentHostname}" required="true">
								<f:selectItems value="#{hostHandler.hostNames}"/>
							     </h:selectOneMenu>
			</td><td><h:message for="dependent_host_name"/></td></tr>
			<tr><td>Host Name: (*)</td><td><h:selectOneMenu id="host_name" value="#{dependencyHandler.hostDependency.hostname}" required="true">
							<f:selectItems value="#{hostHandler.hostNames}"/>
						   </h:selectOneMenu>
			</td><td><h:message for="host_name"/></td></tr>
			<tr><td>Inherits Parents:</td><td><h:selectBooleanCheckbox id="inherits_parents" value="#{dependencyHandler.hostDependency.inheritsParents}"/></td><td><h:message for="inherits_parents"/></td></tr>
			<tr><td>Execution Failure Criteria:</td><td><h:selectManyCheckbox id="execution_failure_criteria" value="#{dependencyHandler.hostDependency.executionFailureCriteria}">
									<f:selectItem itemValue="o" itemLabel="Up"/>
									<f:selectItem itemValue="d" itemLabel="Down"/>
									<f:selectItem itemValue="u" itemLabel="Unreachable"/>
									<f:selectItem itemValue="p" itemLabel="Pending"/>
									<f:selectItem itemValue="n" itemLabel="None"/>
								    </h:selectManyCheckbox>
			</td><td><h:message for="execution_failure_criteria"/></td></tr>
			<tr><td>Notification Failure Criteria:</td><td><h:selectManyCheckbox id="notification_failure_criteria" value="#{dependencyHandler.hostDependency.notificationFailureCriteria}">
									 <f:selectItem itemValue="o" itemLabel="Up"/>
									 <f:selectItem itemValue="d" itemLabel="Down"/>
									 <f:selectItem itemValue="u" itemLabel="Unreachable"/>
									 <f:selectItem itemValue="p" itemLabel="Pending"/>
									 <f:selectItem itemValue="n" itemLabel="None"/>
									</h:selectManyCheckbox>
			</td><td><h:message for="notification_failure_criteria"/></td></tr>
			<tr><td colspan="2"><h:commandButton value="Add Dependency"  styleClass="button" action="#{dependencyHandler.addResult}" actionListener="#{dependencyHandler.addDependency}" rendered="#{dependencyHandler.hostDependency.isModifiable == false}">
						<f:attribute name="dependencyType" value="0"/>
					    </h:commandButton> 
					    <h:commandButton value="Modify Dependency"  styleClass="button" action="#{dependencyHandler.modResult}" actionListener="#{dependencyHandler.modifyDependency}" rendered="#{dependencyHandler.hostDependency.isModifiable}">
					    	<f:attribute name="dependencyType" value="0"/>
					    </h:commandButton>
			 <input type="reset"  class="button" value="Clear"/></td></tr>
			</h:form>
		</table>
	</c:otherwise>
</c:choose>	
<br/>
<h3>List of current Host Dependencies:</h3>

	<c:choose>
		<c:when test="${dependencyHandler.hostDependencyCount == 0}">
		<p>There are currently no Host Dependencies defined.</p>
		</c:when>
		<c:otherwise>
		<h:form>
			<h:dataTable value="#{dependencyHandler.sortedHostDependencyData}" rowClasses="even, odd" var="e" rows="#{dependencyHandler.rowCount}" first="#{dependencyHandler.firstRowIndex}">
				<h:column>
					<f:facet name="header">
						<h:commandLink actionListener="#{dependencyHandler.sortByDependentHostname}" immediate="true" title="Sort By Dependent Host Name">
							<f:attribute name="dependencyType" value="0"/>
							<h:outputText value="Dependent Host Name"/>
						</h:commandLink>
					</f:facet>
					<h:commandLink actionListener="#{dependencyHandler.select}" action="#{dependencyHandler.selectResult}" immediate="true" title="View details for this Dependency">
						<h:outputText value="#{e.dependentHostname}"/>
						<f:attribute name="dependencyType" value="0"/>
					</h:commandLink>						
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:commandLink actionListener="#{dependencyHandler.sortByName}" immediate="true" title="Sort By Host Name">
							<f:attribute name="dependencyType" value="0"/>
							<h:outputText value="Host Name"/>
						</h:commandLink>
					</f:facet>
					<h:commandLink actionListener="#{dependencyHandler.select}" action="#{dependencyHandler.selectResult}" immediate="true" title="View details for thid Dependency">
						<h:outputText value="#{e.hostname}"/>
						<f:attribute name="dependencyType" value="0"/>
					</h:commandLink>	
				</h:column>
				<h:column>
					<f:facet name="header">
						<h:outputText value="Delete"/>
					</f:facet>
					<h:commandLink actionListener="#{dependencyHandler.deleteDependency}" action="#{dependencyHandler.delResult}" immediate="true" title="Delete this Dependency">
						<f:param name="objectId" value="#{e.id}"/>
						<f:param name="dependencyType" value="0"/>
						<h:outputText value="Delete"/>
					</h:commandLink>
				</h:column>
			</h:dataTable>
			<p>
			First: <h:commandButton id="first" value="<<" disabled="#{dependencyHandler.scrollFirstDisabled}" actionListener="#{dependencyHandler.scrollFirst}" title="Scroll to First Page">
					<f:attribute name="depType" value="0"/>
				</h:commandButton>
			 	<h:commandButton id="previous" value="<" disabled="#{dependencyHandler.scrollFirstDisabled}" actionListener="#{dependencyHandler.scrollPrevious}" title="Scroll to the Previous Page">
			 		<f:attribute name="depType" value="0"/>
			 	</h:commandButton>
			  	<h:commandButton id="next" value=">" disabled="#{dependencyHandler.scrollLastHostDisabled}" actionListener="#{dependencyHandler.scrollNext}" title="Scroll to the Next Page">
			  		<f:attribute name="depType" value="0"/>
			  	</h:commandButton>
			  	<h:commandButton value=">>" action="#{dependencyHandler.scrollLast}" disabled="#{dependencyHandler.scrollLastHostDisabled}" title="Scroll to the Last Page" id="last">
			  		<f:attribute name="depType" value="0"/>
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
