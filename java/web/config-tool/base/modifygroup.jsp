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

<h3>Please Modify Group Details:</h3>

<f:view>
	<table name="group_table">
		<h:form id="mod_group_details">
			<tr><td>Group Name:</td><td><h:inputText size="20" id="group_name" value="#{groupHandler.modifyGroup.name}" required="true"/></td><td><h:message for="group_name"/></td></tr>
			<tr><td>Alias:</td><td><h:inputText size="20" id="alias" value="#{groupHandler.modifyGroup.alias}" required="true"/></td><td><h:message for="alias"/></td></tr>
			<tr><td>Members:</td><td>
						<h:selectManyListbox id="host_group_members" value="#{groupHandler.modifyGroup.members}" size="5" required="true" rendered="#{groupHandler.modifyGroupType == 0}">
							<f:selectItems value="#{hostHandler.hostNames}"/>
						 </h:selectManyListbox>
						 
						 <h:selectManyListbox id="service_group_members" value="#{groupHandler.modifyGroup.members}" size="5" required="true" rendered="#{groupHandler.modifyGroupType == 1}">
							<f:selectItems value="#{serviceHandler.serviceHostNames}"/>
						 </h:selectManyListbox>
						 
						<h:selectManyListbox id="contact_group_members" value="#{groupHandler.modifyGroup.members}" size="5" required="true" rendered="#{groupHandler.modifyGroupType == 2}">
							<f:selectItems value="#{contactHandler.contactNames}"/>
						 </h:selectManyListbox>
			</td><td><h:message for="group_members"/></td></tr>
			<tr><td colspan="3"><h:commandButton action="#{groupHandler.modResult}" actionListener="#{groupHandler.modifyGroup}" value="Modify Group" rendered="#{groupHandler.modifyGroupType == 0}">
				<f:attribute name="groupType" value="0"/>
			</h:commandButton>
			<h:commandButton action="#{groupHandler.modResult}" actionListener="#{groupHandler.modifyGroup}" value="Modify Group" rendered="#{groupHandler.modifyGroupType == 1}">
				<f:attribute name="groupType" value="1"/>
			</h:commandButton>
			<h:commandButton action="#{groupHandler.modResult}" actionListener="#{groupHandler.modifyGroup}" value="Modify Group" rendered="#{groupHandler.modifyGroupType == 2}">
				<f:attribute name="groupType" value="2"/>
			</h:commandButton></td></tr>
		</h:form>
		
		
	</table>
<br/>
<p>Use the above form to modify the details of the group.</p>	
</f:view>	

</div>

<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
