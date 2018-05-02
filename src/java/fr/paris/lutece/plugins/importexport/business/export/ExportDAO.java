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

import fr.paris.lutece.plugins.importexport.business.AbstractImportExportDAO;
import fr.paris.lutece.plugins.importexport.business.ColumnType;
import fr.paris.lutece.plugins.importexport.business.ImportExportElement;
import fr.paris.lutece.plugins.importexport.business.TableColumn;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;


/**
 * DAO to export data from a database table
 */
public class ExportDAO extends AbstractImportExportDAO
{
    private static final String SQL_QUERY_SELECT = " SELECT ";
    private static final String SQL_QUERY_FROM = " FROM ";

    private static final String CONSTANT_COMMA = ",";

    /**
     * Get the list of rows of a table of the database.
     * @param strTableName The name of the table to get data from
     * @param listColumns The list of columns to get
     * @param plugin The plugin to use the pool of
     * @return The list of rows containing the requested columns of the given
     *         table
     */
    public List<RowExportData> getDataFromTable( String strTableName, List<String> listColumns, Plugin plugin )
    {
        List<TableColumn> listTableColumns = getTableColumns( listColumns, strTableName, plugin, Locale.getDefault( ) );
        List<RowExportData> listRowExportData = new ArrayList<RowExportData>( );
        DAOUtil daoUtil = new DAOUtil( getSqlSelect( strTableName, listColumns ), plugin );
        try
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                int nIndex = 1;
                List<ImportExportElement> listElements = new ArrayList<ImportExportElement>( listTableColumns.size( ) );
                for ( TableColumn tableColumn : listTableColumns )
                {
                    String strValue = getElementValue( daoUtil, tableColumn.getColumnType( ), nIndex++ );
                    ImportExportElement element = new ImportExportElement( );
                    element.setColumnName( tableColumn.getColumnName( ) );
                    element.setValue( strValue );
                    listElements.add( element );
                }
                RowExportData rowData = new RowExportData( listElements );
                listRowExportData.add( rowData );
            }
        }
        catch ( AppException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        finally
        {
            daoUtil.free( );
        }
        return listRowExportData;
    }

    /**
     * Get the SQL string to select data from the given table
     * @param strTableName The name of the table
     * @param listColumns The list of columns to get
     * @return the SQL SELECT string
     */
    private String getSqlSelect( String strTableName, List<String> listColumns )
    {
        StringBuilder sbSqlSelect = new StringBuilder( SQL_QUERY_SELECT );
        boolean bFirst = true;
        for ( String strColumn : listColumns )
        {
            if ( bFirst )
            {
                bFirst = false;
            }
            else
            {
                sbSqlSelect.append( CONSTANT_COMMA );
            }
            sbSqlSelect.append( strColumn );
        }
        sbSqlSelect.append( SQL_QUERY_FROM );
        sbSqlSelect.append( strTableName );
        return sbSqlSelect.toString( );
    }

    /**
     * Get the value of an element from a daoUtil.
     * @param daoUtil The daoUtil to get the value from
     * @param columnType The column type of the element to get
     * @param nIndex The index of the element in the DAO.
     * @return The string representation of the element, or an empty string if
     *         the value could not be retrieved
     */
    private String getElementValue( DAOUtil daoUtil, ColumnType columnType, int nIndex )
    {
        String strResult = StringUtils.EMPTY;
        switch ( columnType )
        {
        case TYPE_INT:
            int nValue = daoUtil.getInt( nIndex );
            strResult = Integer.toString( nValue );
            break;
        case TYPE_STRING:
            strResult = daoUtil.getString( nIndex );
            break;
        case TYPE_LONG:
            long lValue = daoUtil.getLong( nIndex );
            strResult = Long.toString( lValue );
            break;
        case TYPE_TIMESTAMP:
            Timestamp timestamp = daoUtil.getTimestamp( nIndex );
            if ( timestamp != null )
            {
                strResult = Long.toString( timestamp.getTime( ) );
            }
            break;
        case TYPE_DATE:
            Date date = daoUtil.getDate( nIndex );
            if ( date != null )
            {
                strResult = DateFormat.getDateInstance( ).format( date );
            }
            break;
        case TYPE_BYTE:
            byte[] bytesValue = daoUtil.getBytes( nIndex );
            if ( bytesValue != null )
            {
                strResult = String.valueOf( Hex.encodeHex( bytesValue ) );
            }
            break;
        case TYPE_DOUBLE:
            double dValue = daoUtil.getDouble( nIndex );
            strResult = Double.toString( dValue );
            break;
        default:
            AppLogService.error( "Error : unknown column type !" );
        }
        return strResult;
    }

}
