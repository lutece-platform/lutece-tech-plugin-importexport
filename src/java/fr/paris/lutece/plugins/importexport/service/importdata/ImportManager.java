package fr.paris.lutece.plugins.importexport.service.importdata;

import fr.paris.lutece.plugins.importexport.business.ImportExportElement;
import fr.paris.lutece.plugins.importexport.business.importdata.ImportDataDAO;
import fr.paris.lutece.plugins.importexport.business.importdata.ImportMessage;
import fr.paris.lutece.plugins.importexport.business.importdata.ImportResult;
import fr.paris.lutece.plugins.importexport.service.ImportExportPlugin;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.daemon.ThreadLauncherDaemon;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;


/**
 * Manager of imports
 */
public class ImportManager
{
    private static List<IImportSourceFactory> _listImportSourceFactories = new ArrayList<IImportSourceFactory>( );

    private static final String CONSTANT_POINT = ".";

    private static Map<Integer, RunnableImportService> _mapWorkingRunnableImportServices = new HashMap<Integer, RunnableImportService>( );

    /**
     * Private constructor
     */
    private ImportManager( )
    {
    }

    /**
     * Register an import source factory
     * @param importSourceFactory The import source factory to register
     */
    public static void registerImportSourceFactory( IImportSourceFactory importSourceFactory )
    {
        _listImportSourceFactories.add( importSourceFactory );
    }

    /**
     * Get an import source for a file item. The import source is instantiated
     * from registered factories that are compatible with the given file
     * extension.
     * @param fileItem The file item to read data from
     * @return The import source, or null if no factories are associated to the
     *         given file extension or if an error occurs during the
     *         instantiation of the import source.
     */
    public static IImportSource getImportSource( FileItem fileItem )
    {
        String strFileName = fileItem.getName( );
        String strFileExtention = null;
        if ( StringUtils.isNotEmpty( strFileName ) )
        {
            strFileExtention = strFileName.substring( strFileName.lastIndexOf( CONSTANT_POINT ) + 1 );
        }

        if ( StringUtils.isEmpty( strFileExtention ) )
        {
            return null;
        }

        for ( IImportSourceFactory importSourceFactory : _listImportSourceFactories )
        {
            if ( importSourceFactory.isValidImportSource( strFileExtention ) )
            {
                return importSourceFactory.getImportSource( fileItem );
            }
        }
        return null;
    }

    /**
     * Get an import source for a file. The import source is instantiated
     * from registered factories that are compatible with the given file
     * extension.
     * @param file The file to read data from
     * @return The import source, or null if no factories are associated to the
     *         given file extension or if an error occurs during the
     *         instantiation of the import source.
     */
    public static IImportSource getImportSource( File file )
    {
        String strFileName = file.getName( );
        String strFileExtention = null;
        if ( StringUtils.isNotEmpty( strFileName ) )
        {
            strFileExtention = strFileName.substring( strFileName.lastIndexOf( CONSTANT_POINT ) + 1 );
        }

        if ( StringUtils.isEmpty( strFileExtention ) )
        {
            return null;
        }

        for ( IImportSourceFactory importSourceFactory : _listImportSourceFactories )
        {
            if ( importSourceFactory.isValidImportSource( strFileExtention ) )
            {
                return importSourceFactory.getImportSource( file );
            }
        }
        return null;
    }

    /**
     * Do process the import of data from an import source to a given table in
     * the database.
     * @param importSource The import source to get data from
     * @param strTableName The name of the table in the database to import data
     *            to
     * @param bUpdateExistingRows True to update existing rows, false to ignore
     *            them
     * @param bStopOnErrors True to stop when an error occurred, false to skip
     *            the item and continue
     * @param plugin The plugin to get the pool from
     * @param locale The locale
     * @return The result of the import
     */
    public static ImportResult doProcessImport( IImportSource importSource, String strTableName,
            boolean bUpdateExistingRows, boolean bStopOnErrors, Plugin plugin, Locale locale )
    {
        List<ImportExportElement> listElements;
        int nCreatedElements = 0;
        int nUpdatedElements = 0;
        int nIgnoredElements = 0;
        int nItemNumber = 0;
        ImportDataDAO importElementDAO = null;
        try
        {
            importElementDAO = new ImportDataDAO( importSource.getColumnsName( ), strTableName, plugin, locale );
        }
        catch ( AppException e )
        {
            AppLogService.info( e.getMessage( ) );
            return createErrorImportResult( e );
        }

        List<ImportMessage> listErrors = new ArrayList<ImportMessage>( );
        try
        {
            // While there is values in the import source
            while ( ( listElements = importSource.getNextValues( ) ) != null )
            {
                nItemNumber++;
                try
                {
                    // If the row already exists
                    if ( importElementDAO.checkElementExists( listElements ) )
                    {
                        // If we must update existing rows
                        if ( bUpdateExistingRows )
                        {
                            importElementDAO.updateElement( listElements );
                            nUpdatedElements++;
                        }
                        else
                        {
                            nIgnoredElements++;
                        }
                    }
                    else
                    {
                        // If it doesn't exist, we insert a new one 
                        importElementDAO.insertElement( listElements );
                        nCreatedElements++;
                    }

                }
                catch ( AppException e )
                {
                    ImportMessage importMessage = new ImportMessage( e.getMessage( ), ImportMessage.STATUS_ERROR,
                            nItemNumber );
                    listErrors.add( importMessage );
                    nIgnoredElements++;
                    if ( bStopOnErrors )
                    {
                        importElementDAO.rollbackTransaction( );
                        nCreatedElements = 0;
                        nUpdatedElements = 0;
                        return new ImportResult( nCreatedElements, nUpdatedElements, nIgnoredElements, listErrors );
                    }
                }
                catch ( SQLException e )
                {
                    ImportMessage importMessage = new ImportMessage( e.getMessage( ), ImportMessage.STATUS_ERROR,
                            nItemNumber );
                    listErrors.add( importMessage );
                    nIgnoredElements++;
                    if ( bStopOnErrors )
                    {
                        importElementDAO.rollbackTransaction( );
                        nCreatedElements = 0;
                        nUpdatedElements = 0;
                        return new ImportResult( nCreatedElements, nUpdatedElements, nIgnoredElements, listErrors );
                    }
                }
            }
            importElementDAO.commitTransaction( );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
            importElementDAO.rollbackTransaction( );
            nCreatedElements = 0;
            nUpdatedElements = 0;
            ImportMessage importMessage = new ImportMessage( e.getMessage( ), ImportMessage.STATUS_ERROR, nItemNumber );
            listErrors.add( importMessage );
        }
        return new ImportResult( nCreatedElements, nUpdatedElements, nIgnoredElements, listErrors );
    }

    /**
     * Do process an asynchronous import of data from an import source to a
     * given table in the database.
     * @param importSource The import source to get data from
     * @param strTableName The name of the table in the database to import data
     *            to
     * @param plugin The plugin to get the pool from
     * @param locale The locale
     * @param bUpdateExistingRows True to update existing rows, false to ignore
     *            them
     * @param bStopOnErrors True to stop when an error occurred, false to skip
     *            the item and continue
     * @param admin The admin user that started the import, or null if the
     *            import was started by a daemon
     */
    public static void doProcessAsynchronousImport( IImportSource importSource, String strTableName, Plugin plugin,
            Locale locale, boolean bUpdateExistingRows, boolean bStopOnErrors, AdminUser admin )
    {
        RunnableImportService runnableImportService = new RunnableImportService( importSource, strTableName, plugin,
                locale, bUpdateExistingRows, bStopOnErrors );
        if ( admin != null )
        {
            _mapWorkingRunnableImportServices.put( admin.getUserId( ), runnableImportService );
        }
        ThreadLauncherDaemon.addItemToQueue( runnableImportService, strTableName, ImportExportPlugin.getPlugin( ) );
    }

    /**
     * Check if an admin user has an import processing
     * @param nAdminId The id of the admin user
     * @return True if the admin user has an import processing, false otherwise
     */
    public static boolean hasImportInProcess( int nAdminId )
    {
        if ( nAdminId > 0 )
        {
            RunnableImportService runnableImportService = _mapWorkingRunnableImportServices.get( nAdminId );
            if ( runnableImportService != null )
            {
                return runnableImportService.getServiceStatus( ) != RunnableImportService.STATUS_FINISHED;
            }
        }
        return false;
    }

    /**
     * Get the result of an asynchronous import. The import service is then
     * removed from the list of current imports
     * @param nAdminId The id of the user that started the import
     * @return The result of the import, or null if no result were found
     */
    public static ImportResult getAsynchronousImportResult( int nAdminId )
    {
        RunnableImportService runnableImportService = _mapWorkingRunnableImportServices.get( nAdminId );
        if ( runnableImportService != null
                && runnableImportService.getServiceStatus( ) == RunnableImportService.STATUS_FINISHED )
        {
            ImportResult result = runnableImportService.getImportResult( );
            _mapWorkingRunnableImportServices.remove( nAdminId );
            return result;
        }
        return null;
    }

    /**
     * Creates a new import result from a throwable. The import result has one
     * error message, which contain the message of the throwable.
     * @param throwable The throwable to get the message from
     * @return An import result
     */
    private static ImportResult createErrorImportResult( Throwable throwable )
    {
        ImportResult result = new ImportResult( );
        ImportMessage message = new ImportMessage( throwable.getMessage( ), ImportMessage.STATUS_ERROR, 0 );
        List<ImportMessage> listMessages = new ArrayList<ImportMessage>( );
        listMessages.add( message );
        result.setListImportMessage( listMessages );
        return result;
    }
}
