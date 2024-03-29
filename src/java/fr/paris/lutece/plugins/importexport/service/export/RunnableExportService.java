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
package fr.paris.lutece.plugins.importexport.service.export;

import fr.paris.lutece.portal.business.xsl.XslExport;
import fr.paris.lutece.portal.business.xsl.XslExportHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;

import java.util.List;

/**
 * RunnableExportService
 */
public class RunnableExportService implements Runnable
{

    public static final int STATUS_QUEUED = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_FINISHED = 2;

    private static final String PLUGIN_IMPORTEXPORT_FOLDER = "plugins/importexport/";
    private static final String CONSTANT_SLASH = "/";
    private static final String CONSTANT_POINT = ".";

    private int _nStatus = STATUS_QUEUED;
    private String _strTableName;
    private List<String> _listColumns;
    private int _nXSLStylesheetId;
    private Plugin _plugin;
    private String _strExportKey;
    private String _strFileExtention;

    /**
     * Creates a new export to run in a dedicated thread
     * 
     * @param strTableName
     *            The name of table to export
     * @param listColumns
     *            The list of columns to export
     * @param nXSLStylesheetId
     *            The id of the stylesheet to apply to the data retrieved from the database. If the id is 0, then the row XML is returned
     * @param plugin
     *            The plugin to get the pool of
     * @param strExportKey
     *            The key of the export. Only one export can be run at the same time for a given key
     */
    public RunnableExportService( String strTableName, List<String> listColumns, int nXSLStylesheetId, Plugin plugin, String strExportKey )
    {
        this._strTableName = strTableName;
        this._listColumns = listColumns;
        this._plugin = plugin;
        this._nXSLStylesheetId = nXSLStylesheetId;
        this._strExportKey = strExportKey;
        XslExport xslExport = XslExportHome.findByPrimaryKey( _nXSLStylesheetId );
        _strFileExtention = xslExport.getExtension( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        _nStatus = STATUS_WORKING;
        try
        {
            ExportManager.doProcessExportIntoFile( getExportedFileName( ), _strTableName, _listColumns, _nXSLStylesheetId, _plugin );
        }
        catch( Exception e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        finally
        {
            _nStatus = STATUS_FINISHED;
        }
    }

    /**
     * Get the status of the service.
     * 
     * @return {@link #STATUS_QUEUED} if the serviced has not been started, {@link #STATUS_WORKING} if it is executing, or {@link #STATUS_FINISHED} if its
     *         execution has ended.
     */
    public int getServiceStatus( )
    {
        return _nStatus;
    }

    /**
     * Get the name of the file generated by this export service
     * 
     * @return The name of the file generated by this export service
     */
    public String getExportedFileName( )
    {
        return AppPathService.getWebAppPath( ) + CONSTANT_SLASH + getExportedFileRelativeUrl( );
    }

    /**
     * Get the URL of the file generated by this export service. The URL is relative from the root folder of the webapp
     * 
     * @return The URL of the file generated by this export service
     */
    public String getExportedFileRelativeUrl( )
    {
        return PLUGIN_IMPORTEXPORT_FOLDER + _strExportKey + CONSTANT_SLASH + _strTableName + CONSTANT_POINT + _strFileExtention;
    }
}
