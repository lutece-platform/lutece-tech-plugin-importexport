<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:useBean id="importData" scope="session" class="fr.paris.lutece.plugins.importexport.web.ImportDataJspBean" />

<% importData.init( request, importData.RIGHT_IMPORT_DATA ); %>
<% response.sendRedirect( importData.doImportData( request ) ); %>
