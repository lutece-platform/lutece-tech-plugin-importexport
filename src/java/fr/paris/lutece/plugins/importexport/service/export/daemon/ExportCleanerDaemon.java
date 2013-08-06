package fr.paris.lutece.plugins.importexport.service.export.daemon;

import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import org.apache.commons.io.filefilter.FileFilterUtils;


/**
 * Daemon to remove old exports of data from the file system
 */
public class ExportCleanerDaemon extends Daemon
{
    private static final String PLUGIN_IMPORTEXPORT_FOLDER = "plugins/importexport/";

    private static final String PROPERTY_EXPORT_FILES_LIFE_TIME = "importexport.exportdata.exportFileLifeTime";

    private static final long DEFAULT_EXPORT_FILES_LIFE_TIME = 7200000l;

    private static final String CONSTANT_SLASH = "/";

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        File mainExportFolder = new File( AppPathService.getWebAppPath( ) + CONSTANT_SLASH + PLUGIN_IMPORTEXPORT_FOLDER );
        long lExportFileLifeTime = AppPropertiesService.getPropertyLong( PROPERTY_EXPORT_FILES_LIFE_TIME,
                DEFAULT_EXPORT_FILES_LIFE_TIME );
        long lDateThreshold = new Date( ).getTime( ) - lExportFileLifeTime;
        int nFileRemoved = 0;
        int nFileIgnored = 0;
        if ( mainExportFolder.exists( ) )
        {
            for ( File userFolder : mainExportFolder.listFiles( (FileFilter) FileFilterUtils.directoryFileFilter( ) ) )
            {
                for ( File file : userFolder.listFiles( ) )
                {
                    if ( file.lastModified( ) < lDateThreshold )
                    {
                        boolean bRes = file.delete( );
                        if ( bRes )
                        {
                            nFileRemoved++;
                        }
                        else
                        {
                            nFileIgnored++;
                        }
                    }
                }
            }
        }
        setLastRunLogs( nFileRemoved + " old export file(s) have been removed and " + nFileIgnored
                + " have been ignored" );
    }

}
