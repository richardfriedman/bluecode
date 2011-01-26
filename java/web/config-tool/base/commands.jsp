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

<c:if test="${accountHandler.inWizard == true}">
<div id="wizard">
<h3>Wizard Info:</h3>
<p>A command object simply provides you with a mechanism to tell Blue what commands to run. Commands are used to perform actions
such as running a specific service check. An example of a typical command would be one containing the name check_ping and the command line which invocates the check_ping
plug-in with the required parameters i.e</p>
<p><b>command_name:</b> check_ping<br/>
<b>command_line:</b> $USER1$/check_ping -H $HOSTADDRESS$ -w $ARG1$ -c $ARG2$ -p 5</p>
Other Examples:<br/>
<p><b>command_name:</b> check-host-alive<br/>
<b>command_line:</b> $USER1$/check_ping -H $HOSTADDRESS$<br/><br/>
<b>command_name:</b> email_oncall<br/>
<b>command_line:</b> echo "help oncall!" | mailx -S "Get Oncall out of bed!" oncall@mycompany.com<br/><br/>
<b>command_name:</b> global_event_handler<br/>
<b>command_line:</b> echo "A Global Event Has Occurred!" | mailx -S "News of a Global Event!" blue@mycompany.com</p>

<p>Once a command has been defined, you can use it with any service/host/contact to perform required functions should certain events occur within your
monitoring setup.</p>

<p>To get started go ahead and add the four commands as shown using the $USER1$ macro for the path to your plug-ins. </p>
</div>
</c:if>

<h3>Add a new Command<br/>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
	<a href="#" onclick="new Effect.toggle('search','slide',{duration:0.5})">(Search for a Command)</a> <a href="javascript:popUp('commandhelp.html')">Need Help? Click Here!</a>
	</h3>	
	<div id="search" style="display: none" class="dropbox">
		<h:form>
			<label for="command_name">Command Name:</label>	<h:inputText id="command_name" value="#{searchHandler.searchString}" maxlength="40"/>
			<h:commandButton value="Search" action="#{searchHandler.searchObjects}" actionListener="#{searchHandler.setSearchType}">
				<f:attribute name="searchType" value="7"/>
			</h:commandButton>	
		</h:form>
	</div>
	<br/>
	<p>* Denotes a required field.</p>
	<table name="command_table">
		<h:form>
		<tr><td>Command Name: (*)</td><td><h:inputText id="command_name" value="#{commandHandler.command.name}" size="20" required="true"/></td><td><h:message for="command_name"/></td></tr>
		<tr><td>Command Line: (*)</td><td><h:inputText id="command_line" value="#{commandHandler.command.commandLine}" size="60" required="true"/></td><td><h:message for="command_line"/></td></tr>
		<tr><td colspan="3"><h:commandButton value="Save Command"  styleClass="button" action="#{commandHandler.addCommand}" rendered="#{commandHandler.command.isModifiable == false}"/> <h:commandButton value="Modify Command"  styleClass="button" action="#{commandHandler.modifyCommand}" rendered="#{commandHandler.command.isModifiable}"/> <input type="reset" class="button" value="Clear"/></td></tr>
		</h:form>
	</table>	

<br/>

<h3>List of current Commands:</h3>

	<c:choose>
	<c:when test="${commandHandler.commandCount == 0}">
		<p>There are currently no commands defined.</p>
	</c:when>
	<c:otherwise>
	<h:form>
		<h:dataTable value="#{commandHandler.sortedCommands}" rowClasses="even, odd" var="e" rows="#{commandHandler.rowCount}" first="#{commandHandler.firstRowIndex}">
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{commandHandler.sortByName}" immediate="true" title="Sort By Command Name">
						<h:outputText value="Command Name"/>
					</h:commandLink>
				</f:facet>
				<h:commandLink action="#{commandHandler.select}" immediate="true" title="View Details for this Command">
					<h:outputText value="#{e.name}"/>			
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="Delete"/>
				</f:facet>
				<h:commandLink action="#{commandHandler.delResult}" actionListener="#{commandHandler.deleteCommand}" immediate="true" title="Delete this command">
					<f:param name="objectId" value="#{e.id}"/>
					<h:outputText value="Delete"/>
				</h:commandLink>
			</h:column>
		</h:dataTable>
		<p>
		First: <h:commandButton value="<<" disabled="#{commandHandler.scrollFirstDisabled}" action="#{commandHandler.scrollFirst}" title="Scroll to First Page"/> <h:commandButton value="<" disabled="#{commandHandler.scrollFirstDisabled}" action="#{commandHandler.scrollPrevious}"/> <h:commandButton value=">" disabled="#{commandHandler.scrollLastDisabled}" action="#{commandHandler.scrollNext}"/> <h:commandButton value=">>" action="#{commandHandler.scrollLast}" disabled="#{commandHandler.scrollLastDisabled}"/> : Last
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
