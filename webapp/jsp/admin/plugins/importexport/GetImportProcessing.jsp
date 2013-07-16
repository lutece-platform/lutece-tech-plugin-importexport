<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="importData" scope="session" class="fr.paris.lutece.plugins.importexport.web.ImportDataJspBean" />

<% importData.init( request, importData.RIGHT_IMPORT_DATA ); %>
<%= importData.getImportProcessing( request ) ) %>

<%@ include file="../../AdminFooter.jsp" %>
