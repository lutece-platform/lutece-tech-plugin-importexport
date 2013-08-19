package fr.paris.lutece.plugins.importexport.service.importdata.daemon;

import fr.paris.lutece.plugins.importexport.business.importdata.ImportMessage;
import fr.paris.lutece.plugins.importexport.business.importdata.ImportResult;
import fr.paris.lutece.plugins.importexport.service.importdata.IImportSource;
import fr.paris.lutece.plugins.importexport.service.importdata.ImportManager;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.File;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;


/**
 * Daemon to import data
 */
public class ImportDaemon extends Daemon
{
    private static final String PROPERTY_DATABASE_TABLES = "importexport.database.importableTableNames";
    private static final String PROPERTY_DAEMON_SOURCE_FOLDER_PATH = "importexport.daemon.importDaemon.sourceFolderPath";
    private static final String PROPERTY_DAEMON_UPDATE_EXISTING_ROWS = "importexport.daemon.importDaemon.updateExistingRows";
    private static final String PROPERTY_DAEMON_STOP_ON_ERRORS = "importexport.daemon.importDaemon.stopOnErrors";
    private static final String PROPERTY_DAEMON_EMPTY_TABLE_BEFORE_IMPORT = "importexport.daemon.importDaemon.emptyTableBeforeImporting";

    private static final String CONSTANT_SLASH = "/";
    private static final String CONSTANT_SEMICOLON = ";";
    private static final String CONSTANT_POINT = ".";

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        String strSourcePath = AppPropertiesService.getProperty( PROPERTY_DAEMON_SOURCE_FOLDER_PATH );
        File directory = new File( strSourcePath );
        if ( directory.isDirectory( ) && directory.canRead( ) )
        {
            if ( !strSourcePath.endsWith( CONSTANT_SLASH ) )
            {
                strSourcePath = strSourcePath + CONSTANT_SLASH;
            }
            String[] strFiles = directory.list( );
            if ( strFiles != null && strFiles.length > 0 )
            {
                String strDatabaseTables = AppPropertiesService.getProperty( PROPERTY_DATABASE_TABLES );
                boolean bUpdateExistingRows = Boolean.parseBoolean( AppPropertiesService
                        .getProperty( PROPERTY_DAEMON_UPDATE_EXISTING_ROWS ) );
                boolean bStopOnErrors = Boolean.parseBoolean( AppPropertiesService
                        .getProperty( PROPERTY_DAEMON_STOP_ON_ERRORS ) );
                boolean bEmptyTable = Boolean.parseBoolean( AppPropertiesService
                        .getProperty( PROPERTY_DAEMON_EMPTY_TABLE_BEFORE_IMPORT ) );
                for ( String strFileName : strFiles )
                {
                    File file = new File( strSourcePath + strFileName );
                    // The file name is the name of the database table plus an extension
                    String strTableName;
                    if ( StringUtils.contains( strFileName, CONSTANT_POINT ) )
                    {
                        strTableName = strFileName.substring( 0, strFileName.lastIndexOf( CONSTANT_POINT ) );
                    }
                    else
                    {
                        strTableName = strFileName;
                    }
                    if ( file.exists( ) && !file.isDirectory( ) )
                    {
                        boolean bAuthorizedTable = false;
                        for ( String strDatabaseTable : strDatabaseTables.split( CONSTANT_SEMICOLON ) )
                        {
                            if ( StringUtils.equals( strDatabaseTable, strTableName ) )
                            {
                                bAuthorizedTable = true;
                                break;
                            }
                        }
                        if ( bAuthorizedTable )
                        {
                            IImportSource importSource = ImportManager.getImportSource( file );
                            if ( importSource != null )
                            {
                                ImportResult result = ImportManager.doProcessImport( importSource, strTableName,
                                        bUpdateExistingRows, bStopOnErrors, bEmptyTable, null, Locale.getDefault( ) );
                                importSource.close( );
                                // If we extracted some data from the file, we remove it
                                String strLog = result.getCreatedElements( ) + " element(s) created, "
                                        + result.getUpdatedElements( ) + " element(s) updated and "
                                        + result.getIgnoredElements( ) + " element(s) ignored.";
                                AppLogService.info( "ImportDaemon : " + strLog );
                                setLastRunLogs( strLog );
                                if ( AppLogService.isDebugEnabled( ) && result.getListImportMessage( ) != null )
                                {
                                    for ( ImportMessage message : result.getListImportMessage( ) )
                                    {
                                        AppLogService.debug( message.toString( ) );
                                    }
                                }
                                if ( result.getCreatedElements( ) > 0 || result.getUpdatedElements( ) > 0
                                        || result.getIgnoredElements( ) > 0 )
                                {
                                    file.delete( );
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            setLastRunLogs( "The specified source folder '" + strSourcePath + "' does not exist or can not be read." );
        }
    }

}
