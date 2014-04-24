/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import fr.paris.lutece.plugins.importexport.business.importdata.ImportResult;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.util.Locale;


/**
 * Service that allow to import data. This service implements the Runnable
 * interface.
 */
public class RunnableImportService implements Runnable
{
    public static final int STATUS_QUEUED = 0;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_FINISHED = 2;

    private IImportSource _importSource;
    private String _strTableName;
    private Plugin _plugin;
    private Locale _locale;
    private boolean _bUpdateExistingRows;
    private boolean _bStopOnErrors;
    private boolean _bEmptyTable;
    private int _nStatus = STATUS_QUEUED;
    private volatile ImportResult _importResult;

    /**
     * Service to asynchronously import data
     * @param importSource The data source to get data from
     * @param strTableName The name of the table
     * @param plugin The plugin associated with the pool the table of the
     *            database is in.
     * @param locale The locale to display messages in
     * @param bUpdateExistingRows Indicates whether existing rows should be
     *            updated (true) or ignored (false)
     * @param bStopOnErrors True to stop when an error occurred, false to skip
     *            the item and continue
     * @param bEmptyTable True to empty the table before importing data, false
     *            otherwise
     */
    public RunnableImportService( IImportSource importSource, String strTableName, Plugin plugin, Locale locale,
            boolean bUpdateExistingRows, boolean bStopOnErrors, boolean bEmptyTable )
    {
        this._importSource = importSource;
        this._strTableName = strTableName;
        this._plugin = plugin;
        this._bUpdateExistingRows = bUpdateExistingRows;
        this._bStopOnErrors = bStopOnErrors;
        this._bEmptyTable = bEmptyTable;
        this._locale = locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        try
        {
            _nStatus = STATUS_WORKING;
            _importResult = ImportManager.doProcessImport( _importSource, _strTableName, _bUpdateExistingRows,
                    _bStopOnErrors, _bEmptyTable, _plugin, _locale );
        }
        catch ( Exception e )
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
     * @return {@link #STATUS_QUEUED} if the serviced has not been started,
     *         {@link #STATUS_WORKING} if it is executing, or
     *         {@link #STATUS_FINISHED} if its execution has ended.
     */
    public int getServiceStatus( )
    {
        return _nStatus;
    }

    /**
     * Get the result of the importation of this service
     * @return The result of the importation of this service, or null if the
     *         import is not complete or if an error occurred
     */
    public ImportResult getImportResult( )
    {
        return _importResult;
    }
}
