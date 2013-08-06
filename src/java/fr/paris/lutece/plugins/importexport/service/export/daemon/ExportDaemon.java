package fr.paris.lutece.plugins.importexport.service.export.daemon;

import fr.paris.lutece.plugins.importexport.business.export.AutomaticExportConfig;
import fr.paris.lutece.plugins.importexport.business.export.IAutomaticExportConfigDAO;
import fr.paris.lutece.plugins.importexport.service.export.ExportManager;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * Daemon to automatically export data from the database into files
 */
public class ExportDaemon extends Daemon
{
    private static final String PARAMETER_KEY_DAEMON_NEXT_SCHEDULE = "importexport.exportDaemon.daemonNextSchedule";
    private static final String PARAMETER_KEY_DAEMON_INTERVAL = "importexport.exportDaemon.daemonInterval";

    private static final String PROPERTY_EXPORT_FOLDER = "importexport.exportDaemon.exportFolder";

    private static final String CONSTANT_DEFAULT_DAEMON_INTERVAL = "86400000";
    private static final String CONSTANT_SLASH = "/";

    private IAutomaticExportConfigDAO _configDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        String strNextSchedule = DatastoreService.getInstanceDataValue( PARAMETER_KEY_DAEMON_NEXT_SCHEDULE,
                StringUtils.EMPTY );
        if ( StringUtils.isNotEmpty( strNextSchedule ) && StringUtils.isNumeric( strNextSchedule ) )
        {
            long lNextSchedule = Long.parseLong( strNextSchedule );
            // If the time of the next schedule has passed, we run the exports
            if ( lNextSchedule < new Date( ).getTime( ) )
            {
                doExportData( );
            }
        }
        else
        {
            String strDaemonInterval = getDaemonInterval( );
            // We compute the next schedule from the current date plus the interval, and save it into the datastore
            long lNextSchedule = new Date( ).getTime( ) + Long.parseLong( strDaemonInterval );
            DatastoreService.setInstanceDataValue( PARAMETER_KEY_DAEMON_NEXT_SCHEDULE, Long.toString( lNextSchedule ) );
        }
    }

    /**
     * Do export data from the database into files in the file system
     */
    private void doExportData( )
    {
        List<AutomaticExportConfig> listConfig = getAutomaticExportConfigDAO( ).findAll( true );
        String strExportFolder = AppPropertiesService.getProperty( PROPERTY_EXPORT_FOLDER );
        if ( strExportFolder != null && !strExportFolder.endsWith( CONSTANT_SLASH ) )
        {
            strExportFolder = strExportFolder + CONSTANT_SLASH;
        }
        int nExportSuccess = 0;
        for ( AutomaticExportConfig config : listConfig )
        {
            try
            {
                boolean bRes = ExportManager.doProcessExportIntoFile( strExportFolder + config.getOutputFileName( ),
                        config.getTableName( ), config.getListColumns( ), config.getXslStylesheetId( ),
                        config.getPlugin( ) );
                if ( bRes )
                {
                    nExportSuccess++;
                }
                else
                {
                    AppLogService.error( "The file '" + config.getOutputFileName( )
                            + "' was NOT filled with data of the table '" + config.getTableName( ) + "'" );
                }
            }
            catch ( Exception e )
            {
                AppLogService.error( e.getMessage( ), e );
            }
        }
        setLastRunLogs( nExportSuccess + " out of " + listConfig.size( ) + " exports have been performed" );
    }

    private IAutomaticExportConfigDAO getAutomaticExportConfigDAO( )
    {
        if ( _configDAO == null )
        {
            _configDAO = SpringContextService.getBean( ExportManager.BEAN_NAME_AUTOMATIC_EXPORT_CONFIG_DAO );
        }
        return _configDAO;
    }

    /**
     * Get the next scheduled export
     * @return A string describing the next scheduled export, in milliseconds
     *         format. The string may be null or empty if there is no next
     *         schedule
     */
    public static String getDaemonNextSchedule( )
    {
        return DatastoreService.getInstanceDataValue( PARAMETER_KEY_DAEMON_NEXT_SCHEDULE, StringUtils.EMPTY );
    }

    /**
     * Set the next schedule of the daemon
     * @param dateNextSchedule The next schedule of the daemon
     */
    public static void setDaemonNextSchedule( Date dateNextSchedule )
    {
        if ( dateNextSchedule != null )
        {
            setDaemonNextSchedule( dateNextSchedule.getTime( ) );
        }
    }

    /**
     * Set the next schedule of the daemon
     * @param lNextSchedule The next schedule of the daemon in milliseconds
     */
    public static void setDaemonNextSchedule( long lNextSchedule )
    {
        DatastoreService.setInstanceDataValue( PARAMETER_KEY_DAEMON_NEXT_SCHEDULE, Long.toString( lNextSchedule ) );
    }

    /**
     * Get the interval of time between exports
     * @return The interval of time between exports. The returned value is
     *         assured o be a parsable long
     */
    public static String getDaemonInterval( )
    {
        String strDaemonInterval = DatastoreService.getInstanceDataValue( PARAMETER_KEY_DAEMON_INTERVAL,
                CONSTANT_DEFAULT_DAEMON_INTERVAL );
        if ( strDaemonInterval == null || !StringUtils.isNumeric( strDaemonInterval ) )
        {
            // If the value retrieved is null or not numeric, we restore the default value 
            strDaemonInterval = CONSTANT_DEFAULT_DAEMON_INTERVAL;
            DatastoreService.setInstanceDataValue( PARAMETER_KEY_DAEMON_INTERVAL, strDaemonInterval );
        }
        return strDaemonInterval;
    }

    /**
     * Set the interval of time between exports
     * @param lInterval The interval of time between exports
     */
    public static void setDaemonInterval( long lInterval )
    {
        DatastoreService.setInstanceDataValue( PARAMETER_KEY_DAEMON_INTERVAL, Long.toString( lInterval ) );
    }
}
