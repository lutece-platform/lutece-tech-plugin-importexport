<%@ page errorPage="../../ErrorPage.jsp" %>

<jsp:useBean id="exportData" scope="session" class="fr.paris.lutece.plugins.importexport.web.ExportDataJspBean" />

<% exportData.init( request, exportData.RIGHT_IMPORT_DATA ); %>
<%
	String strResult = exportData.getExportProcessing( request, response );
	if ( strResult != null )
	{
%>
		<jsp:include page="../../AdminHeader.jsp" />
		<%= strResult %>
		<%@ include file="../../AdminFooter.jsp" %>
<%
	}
%>
