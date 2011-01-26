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

<h3>Modify Host Extended Information.</h3>

	<h:messages/>
	<table name="hostexi_table">
	
		<h:form>
		<tr><td>Host Name:</td><td><h:selectOneMenu id="host_name" value="#{extInfoHandler.modifyHostExtInfo.hostname}" required="true">
						<f:selectItems value="#{hostHandler.hostNames}"/>
				   	    </h:selectOneMenu></td><td colspan="2"><h:message for="host_name"/></td></tr>
		<tr><td>Notes:</td><td colspan="3"><h:inputText id="notes" value="#{extInfoHandler.modifyHostExtInfo.notes}" size="40"/></td></tr>
		<tr><td>Notes URL:</td><td colspan="3"><h:inputText id="notes_url" value="#{extInfoHandler.modifyHostExtInfo.notesURL}" size="20"/></td></tr>
		<tr><td>Action URL:</td><td colspan="3"><h:inputText id="action_url" value="#{extInfoHandler.modifyHostExtInfo.actionURL}" size="20"/></td></tr>
		<tr><td>Icon Image:</td><td colspan="3"><h:inputText id="ii" value="#{extInfoHandler.modifyHostExtInfo.iconImage}" size="20"/></td></tr>
		<tr><td>Icon Image Alt:</td><td colspan="3"><h:inputText id="iia" value="#{extInfoHandler.modifyHostExtInfo.iconImageAlt}" size="20"/></td></tr>
		<tr><td>VRML Image:</td><td colspan="3"><h:inputText id="vrml" value="#{extInfoHandler.modifyHostExtInfo.vrmlImage}" size="20"/></td></tr>
		<tr><td>Status Map Image:</td><td colspan="3"><h:inputText id="smi" value="#{extInfoHandler.modifyHostExtInfo.statusMapImage}" size="20"/></td></tr>
		<tr><td>2D Coords:</td><td>X: <h:inputText id="x" size="5" maxlength="7" value="#{extInfoHandler.modifyHostExtInfo.twodX}"/></td><td>Y: <h:inputText id="y" size="5" maxlength="7" value="#{extInfoHandler.modifyHostExtInfo.twodY}"/></td></tr>
		<tr><td>3D Coords:</td><td>X: <h:inputText id="threedx" size="5" maxlength="7" value="#{extInfoHandler.modifyHostExtInfo.threedX}"/></td><td>Y: <h:inputText id="threedy" size="5" maxlength="7" value="#{extInfoHandler.modifyHostExtInfo.threedY}"/></td><td>Z: <h:inputText id="threedz" size="5" maxlength="7" value="#{extInfoHandler.modifyHostExtInfo.threedZ}"/></td></tr>
		<tr><td colspan="4"><h:commandButton id="modify_hei" value="Modify Extended Info" actionListener="#{extInfoHandler.modifyExtInfo}" action="#{extInfoHandler.modResult}">
		 	<f:attribute name="extType" value="0"/>
		 </h:commandButton></td></tr>		
		</h:form>
	</table>	
</f:view>
<p>Use the form above to update the Extended Host Information.</p>
</div>


<div style="clear:both;"></div>
<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> </b>

<b class="rbottom"><b class="r1"></b></b>
</div>
</body>
</html>
