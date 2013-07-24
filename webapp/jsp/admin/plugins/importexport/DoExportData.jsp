<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:useBean id="exportData" scope="session" class="fr.paris.lutece.plugins.importexport.web.ExportDataJspBean" />

<% exportData.init( request, exportData.RIGHT_IMPORT_DATA ); %>
<% exportData.doExportData( request, response ); %>
