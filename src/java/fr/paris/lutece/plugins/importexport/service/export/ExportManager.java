package fr.paris.lutece.plugins.importexport.service.export;

import fr.paris.lutece.plugins.importexport.business.export.ExportDAO;
import fr.paris.lutece.plugins.importexport.business.export.RowExportData;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.daemon.ThreadLauncherDaemon;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.xml.XmlUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExportManager
{
    private static ExportDAO _exportDAO = new ExportDAO( );
    private static Map<Integer, RunnableExportService> _mapRunningImports = new HashMap<Integer, RunnableExportService>( );

    /**
     * Do process the export of a table of the database
     * @param strTableName The name of the database table to export
     * @param listColumns The list of columns to export
     * @param nXSLStylesheetId The id of the XSL export stylesheet to use to
     *            format data retreived from the database. If the id of the XSL
     *            stylesheet is 0, then the row XML is returned
     * @param plugin The plugin to get the pool of
     * @return The string containing the formatted values of the table of the
     *         database
     */
    public static String doProcessExport( String strTableName, List<String> listColumns, int nXSLStylesheetId,
            Plugin plugin )
    {
        List<RowExportData> listRowData = _exportDAO.getDataFromTable( strTableName, listColumns, plugin );
        StringBuilder sbXml = new StringBuilder( XmlUtil.getXmlHeader( ) );
        for ( RowExportData rowExportData : listRowData )
        {
            // TODO : generate XML content
        }
        return null;
    }

    /**
     * Register an import to be generated asynchronously
     * @param strTableName The name of the database table to export
     * @param listColumns The list of columns to export
     * @param nXSLStylesheetId The id of the XSL export stylesheet to use to
     *            format data retrieved from the database. If the id of the XSL
     *            stylesheet is 0, then the row XML is returned
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

    public static String getExportResult( AdminUser admin )
    {
        RunnableExportService exportService = _mapRunningImports.get( admin.getUserId( ) );
        if ( exportService != null )
        {
            if ( exportService.getServiceStatus( ) == RunnableExportService.STATUS_FINISHED )
            {
                _mapRunningImports.remove( admin.getUserId( ) );
                return exportService.getExportedFileName( );
            }
        }
        return null;
    }
}
