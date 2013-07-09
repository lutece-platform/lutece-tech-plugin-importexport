package fr.paris.lutece.plugins.importexport.service.importdata;

import fr.paris.lutece.plugins.importexport.business.importdata.IImportElementDAO;
import fr.paris.lutece.plugins.importexport.business.importdata.ImportElement;
import fr.paris.lutece.plugins.importexport.business.importdata.ImportResult;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.plugin.Plugin;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Manager of imports
 */
public class ImportManager
{
    private static List<IImportSourceFactory> _listImportSourceFactories = new ArrayList<IImportSourceFactory>( );

    private static final String CONSTANT_POINT = ".";

    private static Map<Integer, RunnableImportService> _mapWorkingRunnableImportServices = new HashMap<Integer, RunnableImportService>( );

    @Autowired
    private static IImportElementDAO _importElementDAO;

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
            strFileExtention = strFileName.substring( strFileName.lastIndexOf( CONSTANT_POINT ) );
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
            strFileExtention = strFileName.substring( strFileName.lastIndexOf( CONSTANT_POINT ) );
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

    public static ImportResult doProcessImport( IImportSource importSource, String strTableName,
            boolean bUpdateExistingRows, Plugin plugin )
    {
        List<ImportElement> listElements;
        int nCreatedElements = 0;
        int nUpdatedElements = 0;
        int nIgnoredElements = 0;
        while ( ( listElements = importSource.getNextValues( ) ) != null )
        {
            try
            {
                _importElementDAO.insertElement( listElements, strTableName, plugin );
                nCreatedElements++;
            }
            catch ( SQLException e )
            {
                if ( bUpdateExistingRows )
                {
                    _importElementDAO.updateElement( listElements, strTableName, plugin );
                    nUpdatedElements++;
                }
                else
                {
                    nIgnoredElements++;
                }
            }
        }
        return new ImportResult( nCreatedElements, nUpdatedElements, nIgnoredElements );
    }

    public static void doProcessAsynchronousImport( IImportSource importSource, String strTableName, Plugin plugin,
            boolean bUpdateExistingRows, AdminUser admin )
    {
        RunnableImportService runnableImportService = new RunnableImportService( importSource, strTableName, plugin,
                bUpdateExistingRows );
        _mapWorkingRunnableImportServices.put( admin.getUserId( ), runnableImportService );
    }

    public static void notifyEndOfProcess( int nAdminId )
    {

    }
}
