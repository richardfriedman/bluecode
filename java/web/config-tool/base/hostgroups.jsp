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

<h3>Wizard Info:</h3>
<p>As with Contact groups, Host groups allow you to logically group together several hosts. While this is mainly used for display
purposes in the Blue console, it also helps to organise your monitoring solution. A typical example of a Host group would be one named Database 
Servers that contained all database hosts as members.</p> 
<p>For the sake of the wizard create a Host group of your choice and add the Host you defined to it.</p>

</c:if>

<h3>Add a new Group<br/>

<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
	<a href="#" onclick="new Effect.toggle('search','slide',{duration:0.5})">(Search for a Host Group)</a> <a href="javascript:popUp('hostgrouphelp.html')">Need Help? Click Here!</a>
	</h3>	
	<div id="search" style="display: none" class="dropbox">
		<h:form>
			<label for="group_name">Group Name:</label>	<h:inputText id="group_name" value="#{searchHandler.searchString}" maxlength="40"/>
			<h:commandButton value="Search" action="#{searchHandler.searchObjects}" actionListener="#{searchHandler.setSearchType}">
				<f:attribute name="searchType" value="1"/>
			</h:commandButton>	
		</h:form>
	</div>
	
	<br/>
<c:choose>
	<c:when test="${hostHandler.hostCount == 0}">
		<p>There are currenly no Hosts defined. Please <a href="hosts.faces">add a host</a> before continuing.</p>
	</c:when>
	<c:otherwise>
		<p>* Denotes required field.</p>
		<table name="group_table">
			<h:form>
				<tr><td>Group Name: (*)</td><td><h:inputText size="20" id="group_name" value="#{groupHandler.hostGroup.name}" required="true"/></td><td><h:message for="group_name"/></td></tr>
				<tr><td>Alias: (*)</td><td><h:inputText size="20" id="alias" value="#{groupHandler.hostGroup.alias}" required="true"/></td><td><h:message for="alias"/></td></tr>
				<tr><td>Members: (*)</td><td><h:selectManyListbox id="group_members" value="#{groupHandler.hostGroup.members}" size="5" required="true">
								<f:selectItems value="#{hostHandler.hostNames}"/>
							 </h:selectManyListbox>
				</td><td><h:message for="group_members"/></td></tr>		
				<tr><td colspan="3"><h:commandButton id="submit_button"  styleClass="button" action="#{groupHandler.addResult}" actionListener="#{groupHandler.addGroup}" value="Save Group" rendered="#{groupHandler.hostGroup.isModifiable == false}">
							<f:attribute name="groupType" value="0"/>
						    </h:commandButton>
						
				<h:commandButton  styleClass="button" actionListener="#{groupHandler.modifyGroup}" action="#{groupHandler.modResult}" value="Modify Group" rendered="#{groupHandler.hostGroup.isModifiable == true}">
					<f:attribute name="groupType" value="0"/>
				</h:commandButton>
				 <input type="reset" value="Clear" class="button"></td></tr>		
			</h:form>
			
		</table>	
	</c:otherwise>
</c:choose>
	
<br/>

<h3>List of current Host Groups:</h3>

	
	<c:choose>
	<c:when test="${groupHandler.hostGroupCount == 0}">
		<p>There are currently no host groups defined.</p>
	</c:when>
	<c:otherwise>
	<h:form>

		<h:dataTable value="#{groupHandler.sortedHostGroups}" var="e" rowClasses="even, odd" rows="#{groupHandler.rowCount}" first="#{groupHandler.firstRowIndex}">
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{groupHandler.sortByGroupName}" immediate="true">
						<h:outputText value="Group Name"/>
					</h:commandLink>
				</f:facet>
				<h:commandLink actionListener="#{groupHandler.select}" action="#{groupHandler.selectResult}" immediate="true">
					<h:outputText value="#{e.name}"/>
					<f:attribute name="groupType" value="0"/>			
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:commandLink action="#{groupHandler.sortByGroupAlias}" immediate="true">
						<h:outputText value="Alias"/>
					</h:commandLink>
				</f:facet>
				<h:outputText value="#{e.alias}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="Delete"/>
				</f:facet>
				<h:commandLink actionListener="#{groupHandler.deleteGroup}" action="#{groupHandler.delResult}" immediate="true" title="Delete this Group!">
					<f:param name="groupType" value="0"/>
					<f:param name="groupId" value="#{e.id}"/>
					<h:outputText value="Delete"/>
				</h:commandLink>
			</h:column>
		</h:dataTable>
		<p>
			First: <h:commandButton id="first" value="<<" disabled="#{groupHandler.scrollFirstDisabled}" actionListener="#{groupHandler.scrollFirst}" title="Scroll to First Page">
					<f:attribute name="groupType" value="0"/>
				</h:commandButton>
			 	<h:commandButton id="previous" value="<" disabled="#{groupHandler.scrollFirstDisabled}" actionListener="#{groupHandler.scrollPrevious}" title="Scroll to the Previous Page">
			 		<f:attribute name="groupType" value="0"/>
			 	</h:commandButton>
			  	<h:commandButton id="next" value=">" disabled="#{groupHandler.scrollLastHostDisabled}" actionListener="#{groupHandler.scrollNext}" title="Scroll to the Next Page">
			  		<f:attribute name="groupType" value="0"/>
			  	</h:commandButton>
			  	<h:commandButton value=">>" action="#{groupHandler.scrollLast}" disabled="#{groupHandler.scrollLastHostDisabled}" title="Scroll to the Last Page" id="last">
			  		<f:attribute name="groupType" value="0"/>
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
