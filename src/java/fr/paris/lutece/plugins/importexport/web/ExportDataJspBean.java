package fr.paris.lutece.plugins.importexport.web;

import fr.paris.lutece.plugins.importexport.business.export.ExportDAO;
import fr.paris.lutece.plugins.importexport.service.ImportExportPlugin;
import fr.paris.lutece.plugins.importexport.service.export.ExportManager;
import fr.paris.lutece.portal.business.xsl.XslExportHome;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.admin.AdminFeaturesPageJspBean;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * This class provides the user interface to export data from the database
 */
public class ExportDataJspBean extends AdminFeaturesPageJspBean
{
    public static final String RIGHT_IMPORT_DATA = "IMPORTEXPORT_MANAGEMENT";

    private static final long serialVersionUID = -8290123143145394692L;

    private static final String PROPERTY_DATABASE_TABLES = "importexport.database.exportableTableNames";
    private static final String PROPERTY_MESSAGE_EXPORT_DATA_PAGE_TITLE = "importexport.adminFeature.exportdata.name";

    private static final String MARK_DATABASE_TABLES = "databaseTables";
    private static final String MARK_LIST_PLUGIN = "listPlugin";
    private static final String MARK_COLUMNS = "columns";
    private static final String MARK_LIST_XSL_EXPORT = "";

    private static final String PARAMETER_TABLE_NAME = "databaseTable";
    private static final String PARAMETER_PLUGIN_NAME = "plugin";
    private static final String PARAMETER_COLUMNS = "columns";
    private static final String PARAMETER_XSL_EXPORT_ID = "xslExport";

    private static final String TEMPLATE_EXPORT_DATA = "admin/plugins/importexport/export_data.html";
    private static final String TEMPLATE_EXPORT_DATA_SELECT_COLUMNS = "admin/plugins/importexport/export_data_select_columns.html";

    private static final String MESSAGE_ERROR_NO_COLUMN_SELECTED = "importexport.export_data.error.noColumnSelected";

    private static final String CONSTANT_SEMICOLON = ";";

    /**
     * Creates a new ExportDataJspBean object.
     */
    public ExportDataJspBean( )
    {
    }

    /**
     * Get the export data page
     * @param request The request
     * @return The HTML content to display
     */
    public String getExportData( HttpServletRequest request )
    {
        String strDatabaseTables = AppPropertiesService.getProperty( PROPERTY_DATABASE_TABLES );
        ReferenceList refList = new ReferenceList( );
        if ( StringUtils.isNotBlank( strDatabaseTables ) )
        {
            for ( String strDatabaseTable : strDatabaseTables.split( CONSTANT_SEMICOLON ) )
            {
                ReferenceItem refItem = new ReferenceItem( );
                refItem.setCode( strDatabaseTable );
                refItem.setName( strDatabaseTable );
                refList.add( refItem );
            }
        }
        Map<String, Object> model = new HashMap<String, Object>( );

        model.put( MARK_DATABASE_TABLES, refList );
        Collection<Plugin> listPlugins = PluginService.getPluginList( );
        ReferenceList refListPlugins = new ReferenceList( );
        ReferenceItem refItemPlugin = new ReferenceItem( );
        Plugin core = PluginService.getCore( );
        refItemPlugin.setName( core.getName( ) );
        refItemPlugin.setCode( core.getName( ) );
        refListPlugins.add( refItemPlugin );
        for ( Plugin plugin : listPlugins )
        {
            if ( plugin.isDbPoolRequired( ) )
            {
                refItemPlugin = new ReferenceItem( );
                refItemPlugin.setName( plugin.getName( ) );
                refItemPlugin.setCode( plugin.getName( ) );
                refListPlugins.add( refItemPlugin );
            }
        }
        model.put( MARK_LIST_PLUGIN, refListPlugins );
        ReferenceList refListXslExport = XslExportHome.getRefListByPlugin( ImportExportPlugin.getPlugin( ) );
        model.put( MARK_LIST_XSL_EXPORT, refListXslExport );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EXPORT_DATA,
                AdminUserService.getLocale( request ), model );

        setPageTitleProperty( PROPERTY_MESSAGE_EXPORT_DATA_PAGE_TITLE );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Get the page to choose which columns of the table to export
     * @param request The request
     * @return The HTML content to display
     * @throws AccessDeniedException If the database table has not been declared
     *             as an exportable table
     */
    public String getExportColumns( HttpServletRequest request ) throws AccessDeniedException
    {
        String strTableName = request.getParameter( PARAMETER_TABLE_NAME );
        String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
        String strXslExportId = request.getParameter( PARAMETER_XSL_EXPORT_ID );
        Plugin plugin = PluginService.getPlugin( strPluginName );
        boolean bAuthorizedTable = false;
        String strDatabaseTables = AppPropertiesService.getProperty( PROPERTY_DATABASE_TABLES );
        for ( String strDatabaseTable : strDatabaseTables.split( CONSTANT_SEMICOLON ) )
        {
            if ( StringUtils.equals( strDatabaseTable, strTableName ) )
            {
                bAuthorizedTable = true;
                break;
            }
        }
        if ( !bAuthorizedTable )
        {
            throw new AccessDeniedException( "The database table '" + strTableName
                    + "' has NOT been decalred as an exportable table" );
        }
        Map<String, Object> model = new HashMap<String, Object>( );

        model.put( PARAMETER_TABLE_NAME, strTableName );
        model.put( PARAMETER_PLUGIN_NAME, strPluginName );
        model.put( PARAMETER_XSL_EXPORT_ID, strXslExportId );
        List<String> listColumns = ExportDAO.getTableColumnsNames( strTableName, plugin );
        model.put( MARK_COLUMNS, listColumns );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EXPORT_DATA_SELECT_COLUMNS,
                AdminUserService.getLocale( request ), model );

        setPageTitleProperty( PROPERTY_MESSAGE_EXPORT_DATA_PAGE_TITLE );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Do export data from a database table and start a download of the export
     * @param request The request
     * @param response The response
     * @throws AccessDeniedException If the database table has not been declared
     *             as an exportable table
     * @throws IOException If an IOException occurs
     */
    public void doExportData( HttpServletRequest request, HttpServletResponse response ) throws AccessDeniedException,
            IOException
    {
        String strTableName = request.getParameter( PARAMETER_TABLE_NAME );
        String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
        String strXslExportId = request.getParameter( PARAMETER_XSL_EXPORT_ID );
        Plugin plugin = PluginService.getPlugin( strPluginName );
        boolean bAuthorizedTable = false;
        String strDatabaseTables = AppPropertiesService.getProperty( PROPERTY_DATABASE_TABLES );
        for ( String strDatabaseTable : strDatabaseTables.split( CONSTANT_SEMICOLON ) )
        {
            if ( StringUtils.equals( strDatabaseTable, strTableName ) )
            {
                bAuthorizedTable = true;
                break;
            }
        }
        if ( !bAuthorizedTable )
        {
            throw new AccessDeniedException( "The database table '" + strTableName
                    + "' has NOT been decalred as an exportable table" );
        }

        String[] strColumns = request.getParameterValues( PARAMETER_COLUMNS );
        if ( strColumns == null || strColumns.length == 0 )
        {
            response.sendRedirect( AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_NO_COLUMN_SELECTED,
                    AdminMessage.TYPE_STOP ) );
            return;
        }
        List<String> listColumns = new ArrayList<String>( strColumns.length );
        for ( String strColumn : strColumns )
        {
            listColumns.add( strColumn );
        }

        int nXslExportId = 0;
        if ( StringUtils.isNumeric( strXslExportId ) )
        {
            nXslExportId = Integer.parseInt( strXslExportId );
        }
        ExportManager.registerAsynchronousExport( strTableName, listColumns, nXslExportId, plugin,
                AdminUserService.getAdminUser( request ) );
        // TODO : redirect to the waiting page
        //        response.sendRedirect(  );
    }
}
