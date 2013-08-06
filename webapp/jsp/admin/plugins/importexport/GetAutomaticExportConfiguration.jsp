<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="exportData" scope="session" class="fr.paris.lutece.plugins.importexport.web.ExportDataJspBean" />

<% exportData.init( request, exportData.RIGHT_IMPORT_DATA ); %>
<%= exportData.getAutomaticExportConfiguration( request ) %>

<%@ include file="../../AdminFooter.jsp" %>