/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
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
import org.apache.commons.lang3.StringUtils;

/**
 * Manager of imports
 */
public final class ImportManager
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
     * 
     * @param importSourceFactory
     *            The import source factory to register
     */
    public static void registerImportSourceFactory( IImportSourceFactory importSourceFactory )
    {
        _listImportSourceFactories.add( importSourceFactory );
    }

    /**
     * Get an import source for a file item. The import source is instantiated from registered factories that are compatible with the given file extension.
     * 
     * @param fileItem
     *            The file item to read data from
     * @return The import source, or null if no factories are associated to the given file extension or if an error occurs during the instantiation of the
     *         import source.
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
     * Get an import source for a file. The import source is instantiated from registered factories that are compatible with the given file extension.
     * 
     * @param file
     *            The file to read data from
     * @return The import source, or null if no factories are associated to the given file extension or if an error occurs during the instantiation of the
     *         import source.
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
     * Do process the import of data from an import source to a given table in the database.
     * 
     * @param importSource
     *            The import source to get data from
     * @param strTableName
     *            The name of the table in the database to import data to
     * @param bUpdateExistingRows
     *            True to update existing rows, false to ignore them
     * @param bStopOnErrors
     *            True to stop when an error occurred, false to skip the item and continue
     * @param bEmptyTable
     *            True to empty the table before importing data, false otherwise
     * @param plugin
     *            The plugin to get the pool from
     * @param locale
     *            The locale
     * @return The result of the import
     */
    public static ImportResult doProcessImport( IImportSource importSource, String strTableName, boolean bUpdateExistingRows, boolean bStopOnErrors,
            boolean bEmptyTable, Plugin plugin, Locale locale )
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
        catch( AppException e )
        {
            AppLogService.info( e.getMessage( ) );
            return createErrorImportResult( e );
        }

        List<ImportMessage> listErrors = new ArrayList<ImportMessage>( );
        try
        {
            if ( bEmptyTable )
            {
                try
                {
                    importElementDAO.emptyTable( );
                }
                catch( AppException e )
                {
                    AppLogService.error( e.getMessage( ), e );
                }
                catch( SQLException e )
                {
                    AppLogService.error( e.getMessage( ), e );
                }
            }
            // While there is values in the import source
            while ( ( listElements = importSource.getNextValues( ) ) != null )
            {
                nItemNumber++;
                try
                {
                    // If we didn't emptied the table, and the row already exists
                    if ( !bEmptyTable && importElementDAO.checkElementExists( listElements ) )
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
                catch( AppException e )
                {
                    ImportMessage importMessage = new ImportMessage( e.getMessage( ), ImportMessage.STATUS_ERROR, nItemNumber );
                    listErrors.add( importMessage );
                    nIgnoredElements++;
                    if ( bStopOnErrors )
                    {
                        importElementDAO.rollbackTransaction( );
                        return new ImportResult( nCreatedElements, nUpdatedElements, nIgnoredElements, listErrors );
                    }
                }
                catch( SQLException e )
                {
                    ImportMessage importMessage = new ImportMessage( e.getMessage( ), ImportMessage.STATUS_ERROR, nItemNumber );
                    listErrors.add( importMessage );
                    nIgnoredElements++;
                    if ( bStopOnErrors )
                    {
                        importElementDAO.rollbackTransaction( );
                        return new ImportResult( nCreatedElements, nUpdatedElements, nIgnoredElements, listErrors );
                    }
                }
            }
            importElementDAO.commitTransaction( );
        }
        catch( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
            importElementDAO.rollbackTransaction( );
            ImportMessage importMessage = new ImportMessage( e.getMessage( ), ImportMessage.STATUS_ERROR, nItemNumber );
            listErrors.add( importMessage );
        }
        return new ImportResult( nCreatedElements, nUpdatedElements, nIgnoredElements, listErrors );
    }

    /**
     * Do process an asynchronous import of data from an import source to a given table in the database.
     * 
     * @param importSource
     *            The import source to get data from
     * @param strTableName
     *            The name of the table in the database to import data to
     * @param plugin
     *            The plugin to get the pool from
     * @param locale
     *            The locale
     * @param bUpdateExistingRows
     *            True to update existing rows, false to ignore them
     * @param bStopOnErrors
     *            True to stop when an error occurred, false to skip the item and continue
     * @param bEmptyTable
     *            True to empty the table before importing data, false otherwise
     * @param admin
     *            The admin user that started the import, or null if the import was started by a daemon
     */
    public static void doProcessAsynchronousImport( IImportSource importSource, String strTableName, Plugin plugin, Locale locale, boolean bUpdateExistingRows,
            boolean bStopOnErrors, boolean bEmptyTable, AdminUser admin )
    {
        RunnableImportService runnableImportService = new RunnableImportService( importSource, strTableName, plugin, locale, bUpdateExistingRows, bStopOnErrors,
                bEmptyTable );
        if ( admin != null )
        {
            _mapWorkingRunnableImportServices.put( admin.getUserId( ), runnableImportService );
        }
        ThreadLauncherDaemon.addItemToQueue( runnableImportService, strTableName, ImportExportPlugin.getPlugin( ) );
    }

    /**
     * Check if an admin user has an import processing
     * 
     * @param nAdminId
     *            The id of the admin user
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
     * Get the result of an asynchronous import. The import service is then removed from the list of current imports
     * 
     * @param nAdminId
     *            The id of the user that started the import
     * @return The result of the import, or null if no result were found
     */
    public static ImportResult getAsynchronousImportResult( int nAdminId )
    {
        RunnableImportService runnableImportService = _mapWorkingRunnableImportServices.get( nAdminId );
        if ( runnableImportService != null && runnableImportService.getServiceStatus( ) == RunnableImportService.STATUS_FINISHED )
        {
            ImportResult result = runnableImportService.getImportResult( );
            _mapWorkingRunnableImportServices.remove( nAdminId );
            return result;
        }
        return null;
    }

    /**
     * Creates a new import result from a throwable. The import result has one error message, which contain the message of the throwable.
     * 
     * @param throwable
     *            The throwable to get the message from
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
