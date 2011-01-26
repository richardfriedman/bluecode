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
<h:inputHidden value="#{hostHandler.hostCount}"/>
<h:inputHidden value="#{extInfoHandler.hostExtInfoCount}"/>

<h3>Host Extended Information. <br/><a href="javascript:popUp('hostextihelp.html')">Need Help? Click Here!</a></h3>

<c:choose>
	<c:when test="${hostHandler.hostCount == 0}">
		<p>There are currently no Hosts defined. Please <a href="hosts.faces" title="Add a Host"> add a Host</a> before adding Host Extended Info.</p>
	</c:when>
<c:otherwise>

	<p>* Denotes a required field.</p>
	<table name="hostexi_table">
	
		<h:form>
		<tr><td>Host Name: (*)</td><td><h:selectOneMenu id="host_name" value="#{extInfoHandler.hostExtInfo.hostname}" required="true">
						<f:selectItems value="#{hostHandler.hostNames}"/>
				   	    </h:selectOneMenu></td><td colspan="2"><h:message for="host_name"/></td></tr>
		<tr><td>Notes:</td><td colspan="3"><h:inputText id="notes" value="#{extInfoHandler.hostExtInfo.notes}" size="40"/></td></tr>
		<tr><td>Notes URL:</td><td colspan="3"><h:inputText id="notes_url" value="#{extInfoHandler.hostExtInfo.notesURL}" size="20"/></td></tr>
		<tr><td>Action URL:</td><td colspan="3"><h:inputText id="action_url" value="#{extInfoHandler.hostExtInfo.actionURL}" size="20"/></td></tr>
		<tr><td>Icon Image:</td><td colspan="3"><h:inputText id="ii" value="#{extInfoHandler.hostExtInfo.iconImage}" size="20"/></td></tr>
		<tr><td>Icon Image Alt:</td><td colspan="3"><h:inputText id="iia" value="#{extInfoHandler.hostExtInfo.iconImageAlt}" size="20"/></td></tr>
		<tr><td>VRML Image:</td><td colspan="3"><h:inputText id="vrml" value="#{extInfoHandler.hostExtInfo.vrmlImage}" size="20"/></td></tr>
		<tr><td>Status Map Image:</td><td colspan="3"><h:inputText id="smi" value="#{extInfoHandler.hostExtInfo.statusMapImage}" size="20"/></td></tr>
		<tr><td>2D Coords:</td><td>X: <h:inputText id="x" size="5" maxlength="7" value="#{extInfoHandler.hostExtInfo.twodX}"/></td><td colspan="2">Y: <h:inputText id="y" size="5" maxlength="7" value="#{extInfoHandler.hostExtInfo.twodY}"/></td></tr>
		<tr><td>3D Coords:</td><td>X: <h:inputText id="threedx" size="5" maxlength="7" value="#{extInfoHandler.hostExtInfo.threedX}"/></td><td>Y: <h:inputText id="threedy" size="5" maxlength="7" value="#{extInfoHandler.hostExtInfo.threedY}"/></td><td>Z: <h:inputText id="threedz" size="5" maxlength="7" value="#{extInfoHandler.hostExtInfo.threedZ}"/></td></tr>
		<tr><td colspan="4"><h:commandButton  styleClass="button" value="Save Extended Info" actionListener="#{extInfoHandler.addExtInfo}" action="#{extInfoHandler.addResult}" rendered="#{extInfoHandler.hostExtInfo.isModifiable == false}">
					<f:attribute name="extType" value="0"/>
				    </h:commandButton>
		 <h:commandButton id="modify_hei" styleClass="button" value="Modify Extended Info" rendered="#{extInfoHandler.hostExtInfo.isModifiable}" actionListener="#{extInfoHandler.modifyExtInfo}" action="#{extInfoHandler.modResult}">
		 	<f:attribute name="extType" value="0"/>
		 </h:commandButton> 
		 <input type="reset"  class="button" value="Clear"></td></tr>		
		</h:form>
	</table>
</c:otherwise>
</c:choose>			
<br/>
<h3>List of current Host Extended Info:</h3>


<c:choose>
<c:when test="${extInfoHandler.hostExtInfoCount==0}">
	<p>There is currently no Host Extended Info defined.</p>
</c:when>
<c:otherwise>
<h:form>
		<h:dataTable value="#{extInfoHandler.hostExtInfoDetails}" var="e" rows="#{extInfoHandler.rowCount}" first="#{extInfoHandler.firstRowIndex}" rowClasses="even, odd">
			<h:column>
				<f:facet name="header">
						<h:outputText value="Host Name"/>
				</f:facet>
				<h:commandLink actionListener="#{extInfoHandler.select}" action="#{extInfoHandler.selectResult}" title="View details for this Extended Info." immediate="true">
					<f:attribute name="ext_info_type" value="0"/>
					<h:outputText value="#{e.hostname}"/>			
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="Delete"/>
				</f:facet>
				<h:commandLink  action="#{extInfoHandler.delResult}" actionListener="#{extInfoHandler.deleteExtInfo}" immediate="true" title="Delete this Extended Information">
					<f:param name="extInfoId" value="#{e.id}"/>
					<f:param name="extInfoType" value="0"/>
					<h:outputText value="Delete"/>
				</h:commandLink>
			</h:column>	
		</h:dataTable>
		<p>
			First: <h:commandButton id="first" value="<<" disabled="#{extInfoHandler.scrollFirstDisabled}" actionListener="#{extInfoHandler.scrollFirst}" title="Scroll to First Page">
					<f:attribute name="extType" value="0"/>
				</h:commandButton>
			 	<h:commandButton id="previous" value="<" disabled="#{extInfoHandler.scrollFirstDisabled}" actionListener="#{extInfoHandler.scrollPrevious}" title="Scroll to the Previous Page">
			 		<f:attribute name="extType" value="0"/>
			 	</h:commandButton>
			  	<h:commandButton id="next" value=">" disabled="#{extInfoHandler.scrollLastHostDisabled}" actionListener="#{extInfoHandler.scrollNext}" title="Scroll to the Next Page">
			  		<f:attribute name="extType" value="0"/>
			  	</h:commandButton>
			  	<h:commandButton value=">>" action="#{extInfoHandler.scrollLast}" disabled="#{extInfoHandler.scrollLastHostDisabled}" title="Scroll to the Last Page" id="last">
			  		<f:attribute name="extType" value="0"/>
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
