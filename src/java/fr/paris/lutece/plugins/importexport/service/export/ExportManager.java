package fr.paris.lutece.plugins.importexport.service.export;

import fr.paris.lutece.plugins.importexport.business.ImportExportElement;
import fr.paris.lutece.plugins.importexport.business.export.ExportDAO;
import fr.paris.lutece.plugins.importexport.business.export.RowExportData;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.daemon.ThreadLauncherDaemon;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.xsl.XslExportService;
import fr.paris.lutece.util.xml.XmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Export manager
 */
public class ExportManager
{
    private static final String XML_TAG_EXPORT = "export";
    private static final String XML_TAG_ROW = "row";
    private static final String XML_PARAMETER_COLUMNS = "columns";

    private static final String PROPERTY_EXPORT_COLUMN_NAME_SEPARATOR = "importexport.export_data.xml.columnNameSeparator";

    private static final String CONSTANT_SEMICOLON = ";";

    private static ExportDAO _exportDAO = new ExportDAO( );
    private static Map<Integer, RunnableExportService> _mapRunningImports = new HashMap<Integer, RunnableExportService>( );

    /**
     * Private constructor
     */
    private ExportManager( )
    {
    }

    /**
     * Do process the export of a table of the database
     * @param strTableName The name of the database table to export
     * @param listColumns The list of columns to export
     * @param nXSLStylesheetId The id of the XSL export style sheet to use to
     *            format data retrieved from the database
     * @param plugin The plugin to get the pool of
     * @return The string containing the formatted values of the table of the
     *         database
     */
    public static String doProcessExport( String strTableName, List<String> listColumns, int nXSLStylesheetId,
            Plugin plugin )
    {
        String strSeparator = AppPropertiesService.getProperty( PROPERTY_EXPORT_COLUMN_NAME_SEPARATOR,
                CONSTANT_SEMICOLON );
        StringBuffer sbXml = new StringBuffer( XmlUtil.getXmlHeader( ) );
        Map<String, String> mapAttributes = new HashMap<String, String>( );
        StringBuilder sbColumns = new StringBuilder( );
        boolean bIsFirst = true;
        for ( String strColumnName : listColumns )
        {
            if ( !bIsFirst )
            {
                sbColumns.append( strSeparator );
            }
            else
            {
                bIsFirst = false;
            }
            sbColumns.append( strColumnName );
        }

        mapAttributes.put( XML_PARAMETER_COLUMNS, sbColumns.toString( ) );
        XmlUtil.beginElement( sbXml, XML_TAG_EXPORT, mapAttributes );
        mapAttributes = null;
        sbColumns = null;

        List<RowExportData> listRowData = _exportDAO.getDataFromTable( strTableName, listColumns, plugin );

        for ( RowExportData rowExportData : listRowData )
        {
            XmlUtil.beginElement( sbXml, XML_TAG_ROW );
            for ( ImportExportElement element : rowExportData.getListExportElements( ) )
            {
                XmlUtil.addElementHtml( sbXml, element.getColumnName( ), element.getValue( ) );
            }
            XmlUtil.endElement( sbXml, XML_TAG_ROW );
        }
        XmlUtil.endElement( sbXml, XML_TAG_EXPORT );
        return XslExportService.exportXMLWithXSL( nXSLStylesheetId, sbXml.toString( ) );
    }

    /**
     * Register an import to be generated asynchronously
     * @param strTableName The name of the database table to export
     * @param listColumns The list of columns to export
     * @param nXSLStylesheetId The id of the XSL export style sheet to use to
     *            format data retrieved from the database
     * @param plugin The plugin to get the pool of
     * @param admin The admin user that started the export, or null if it has
     *            been started by a daemon
     */
    public static void registerAsynchronousExport( String strTableName, List<String> listColumns, int nXSLStylesheetId,
            Plugin plugin, AdminUser admin )
    {
        RunnableExportService exportService = new RunnableExportService( strTableName, listColumns, nXSLStylesheetId,
                plugin, Integer.toString( admin.getUserId( ) ) );
        _mapRunningImports.put( admin.getUserId( ), exportService );
        ThreadLauncherDaemon.addItemToQueue( exportService, strTableName, plugin );
    }

    /**
     * Check if an admin user has an export processing
     * @param nAdminId The id of the admin user
     * @return True if the admin user has an export processing, false otherwise
     */
    public static boolean hasExportInProcess( int nAdminId )
    {
        if ( nAdminId > 0 )
        {
            RunnableExportService runnableImportService = _mapRunningImports.get( nAdminId );
            if ( runnableImportService != null )
            {
                return runnableImportService.getServiceStatus( ) != RunnableExportService.STATUS_FINISHED;
            }
        }
        return false;
    }

    /**
     * Get the result of an export
     * @param admin The admin user that started the export
     * @return The URL of the generated file relative from the root of the
     *         webapp, or null if no export was found
     */
    public static String getExportResult( AdminUser admin )
    {
        RunnableExportService exportService = _mapRunningImports.get( admin.getUserId( ) );
        if ( exportService != null )
        {
            if ( exportService.getServiceStatus( ) == RunnableExportService.STATUS_FINISHED )
            {
                _mapRunningImports.remove( admin.getUserId( ) );
                return exportService.getExportedFileRelativeUrl( );
            }
        }
        return null;
    }
}
