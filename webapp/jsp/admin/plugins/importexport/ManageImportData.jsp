<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<jsp:useBean id="importData" scope="session" class="fr.paris.lutece.plugins.importdata.web.ImportDataJspBean" />

<% importData.init( request, importData.RIGHT_MANAGE_IMPORT_DATA ); %>
<%= importData.getImportData( request ) %>

<%@ include file="../../AdminFooter.jsp" %>
