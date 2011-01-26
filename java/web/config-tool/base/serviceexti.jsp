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
<f:view>
<h:inputHidden value="#{accountHandler.outputLocation}"/>
<h:inputHidden value="#{serviceHandler.serviceCount}"/>
<h:inputHidden value="#{extInfoHandler.serviceExtInfoCount}"/>

<div id="main">

<c:if test="${accountHandler.inWizard}">
<div id="wizard">
<h3>Wizard Info:</h3>
<p>This page allows you to define what is known as Service Extended Info. You may find that you have very little use for this page, but it's overall
purpose is to make the Service presentation within Blue look pretty. It has no other purpose than to make things look good and therefore has no real
effect on your monitoring setup. You can add a piece of Service Extended Info if you wish, or click the 'Finish' button to end the wizard.</p>
<h:form>
	<h:commandButton styleClass="button" value="Finish" action="#{accountHandler.finishWizard}"/>
</h:form>
</div>
</c:if>

<h3>Service Extended Information. <br/><a href="javascript:popUp('serviceexthelp.html')">Need Help? Click Here!</a></h3>

<c:choose>
	<c:when test="${serviceHandler.serviceCount == 0}">
		<p>There are currently no Services defined. Please <a href="services.faces" title="Define a Service">define a Service</a> before adding Service Extended Info.</p>
	</c:when>
	<c:otherwise>
		<p>* Denotes a required field.</p>
		<table name="serviceexi_table">
		
			<h:form>
			<tr><td>Host & Service Description: (*)</td><td><h:selectOneMenu id="service_name" value="#{extInfoHandler.serviceExtInfo.serviceHost}">
								<f:selectItems value="#{serviceHandler.serviceHostNames}"/>
							     </h:selectOneMenu></td><td colspan="2" h:message for="service_name"/></td></tr>
			<tr><td>Notes:</td><td colspan="3"><h:inputText id="notes" value="#{extInfoHandler.serviceExtInfo.notes}" size="40"/></td></tr>
			<tr><td>Notes URL:</td><td colspan="3"><h:inputText id="notes_url" value="#{extInfoHandler.serviceExtInfo.notesURL}" size="20"/></td></tr>
			<tr><td>Action URL:</td><td colspan="3"><h:inputText id="action_url" value="#{extInfoHandler.serviceExtInfo.actionURL}" size="20"/></td></tr>
			<tr><td>Icon Image:</td><td colspan="3"><h:inputText id="ii" value="#{extInfoHandler.serviceExtInfo.iconImage}" size="20"/></td></tr>
			<tr><td>Icon Image Alt:</td><td colspan="3"><h:inputText id="iia" value="#{extInfoHandler.serviceExtInfo.iconImageAlt}" size="20"/></td></tr>
			<tr><td colspan="4"><h:commandButton styleClass="button" value="Save Extended Info" actionListener="#{extInfoHandler.addExtInfo}" action="#{extInfoHandler.addResult}" rendered="#{extInfoHandler.serviceExtInfo.isModifiable == false}">
						<f:attribute name="extType" value="1"/>
					    </h:commandButton>
			 <input type="reset" class="button" value="Clear"></td></tr>		
			</h:form>
		</table>	
	</c:otherwise>
</c:choose>

<br/>
<h3>List of current Service Extended Info:</h3>

<c:choose>
<c:when test="${extInfoHandler.serviceExtInfoCount==0}">
	<p>There is currently no Service Extended Info defined.</p>
</c:when>
<c:otherwise>
	<h:form>
		<h:dataTable rowClasses="even, odd" value="#{extInfoHandler.serviceExtInfoDetails}" var="e" first="#{extInfoHandler.firstRowIndex}" rows="#{extInfoHandler.rowCount}">
			<h:column>
				<f:facet name="header">
						<h:outputText value="Host Name"/>
				</f:facet>
				<h:commandLink actionListener="#{extInfoHandler.select}" action="#{extInfoHandler.selectResult}" title="View details for this Extended Info." immediate="true">
					<f:attribute name="ext_info_type" value="1"/>
					<h:outputText value="#{e.hostname}"/>			
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="Service Description"/>
				</f:facet>
				<h:commandLink actionListener="#{extInfoHandler.select}" action="#{extInfoHandler.selectResult}" title="View details for this Extended Info." immediate="true">
					<f:attribute name="ext_info_type" value="1"/>
					<h:outputText value="#{e.serviceDescription}"/>
				</h:commandLink>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="Delete"/>
				</f:facet>
				<h:commandLink  action="#{extInfoHandler.delResult}" actionListener="#{extInfoHandler.deleteExtInfo}" immediate="true" title="Delete this Extended Information">
					<f:param name="extInfoId" value="#{e.id}"/>
					<f:param name="extInfoType" value="1"/>
					<h:outputText value="Delete"/>
				</h:commandLink>
			</h:column>	
		</h:dataTable>
		<p>
			First: <h:commandButton id="first" value="<<" disabled="#{extInfoHandler.scrollFirstDisabled}" actionListener="#{extInfoHandler.scrollFirst}" title="Scroll to First Page">
					<f:attribute name="extType" value="1"/>
				</h:commandButton>
			 	<h:commandButton id="previous" value="<" disabled="#{extInfoHandler.scrollFirstDisabled}" actionListener="#{extInfoHandler.scrollPrevious}" title="Scroll to the Previous Page">
			 		<f:attribute name="extType" value="1"/>
			 	</h:commandButton>
			  	<h:commandButton id="next" value=">" disabled="#{extInfoHandler.scrollLastServiceDisabled}" actionListener="#{extInfoHandler.scrollNext}" title="Scroll to the Next Page">
			  		<f:attribute name="extType" value="1"/>
			  	</h:commandButton>
			  	<h:commandButton value=">>" action="#{extInfoHandler.scrollLast}" disabled="#{extInfoHandler.scrollLastServiceDisabled}" title="Scroll to the Last Page" id="last">
			  		<f:attribute name="extType" value="1"/>
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
