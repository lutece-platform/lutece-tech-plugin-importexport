/*
 * Copyright (c) 2002-2017, Mairie de Paris
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
package fr.paris.lutece.plugins.importexport.business.export;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.List;


/**
 * Class that represents a configuration of an export that is processed by the
 * export daemon
 */
public class AutomaticExportConfig
{
    private int _nId;
    private String _strTableName;
    private String _strOutputFileName;
    private List<String> _listColumns;
    private int _nXslStylesheetId;
    private Plugin _plugin;

    /**
     * Get the id of the export configuration
     * @return the id of the export configuration
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * set the id of the export configuration
     * @param nId the id of the export configuration
     */
    public void setId( int nId )
    {
        this._nId = nId;
    }

    /**
     * Get the name of the table of the database to export data from
     * @return the name of the table of the database
     */
    public String getTableName( )
    {
        return _strTableName;
    }

    /**
     * Set the name of the table of the database to export data from
     * @param strTableName the name of the table of the database
     */
    public void setTableName( String strTableName )
    {
        this._strTableName = strTableName;
    }

    /**
     * Get the name of the file that will contain the result of the export
     * @return The name of the file that will contain the result of the export
     */
    public String getOutputFileName( )
    {
        return _strOutputFileName;
    }

    /**
     * Set the name of the file that will contain the result of the export
     * @param strOutputFileName The name of the file that will contain the
     *            result of the export
     */
    public void setOutputFileName( String strOutputFileName )
    {
        this._strOutputFileName = strOutputFileName;
    }

    /**
     * Get the list of columns to include in the export
     * @return The list of columns to include in the export
     */
    public List<String> getListColumns( )
    {
        return _listColumns;
    }

    /**
     * Set the list of columns to include in the export
     * @param listColumns The list of columns to include in the export
     */
    public void setListColumns( List<String> listColumns )
    {
        this._listColumns = listColumns;
    }

    /**
     * Get the id of the style sheet to apply to the data retrieved from the
     * database
     * @return the id of the style sheet
     */
    public int getXslStylesheetId( )
    {
        return _nXslStylesheetId;
    }

    /**
     * Set the id of the style sheet to apply to the data retrieved from the
     * database
     * @param nXslStylesheetId the id of the style sheet
     */
    public void setXslStylesheetId( int nXslStylesheetId )
    {
        this._nXslStylesheetId = nXslStylesheetId;
    }

    /**
     * Get the plugin to get the pool from
     * @return the plugin to get the pool from
     */
    public Plugin getPlugin( )
    {
        return _plugin;
    }

    /**
     * Set the plugin to get the pool from
     * @param plugin the plugin to get the pool from
     */
    public void setPlugin( Plugin plugin )
    {
        this._plugin = plugin;
    }

}
