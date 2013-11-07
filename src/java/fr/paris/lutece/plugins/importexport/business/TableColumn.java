/*
 * Copyright (c) 2002-2013, Mairie de Paris
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
package fr.paris.lutece.plugins.importexport.business;



/**
 * Describes an SQL column
 */
public class TableColumn
{
    private String _strColumnName;
    private boolean _bIsPrimaryKey;
    private ColumnType _columnType;

    /**
     * Creates a new table column
     * @param strColumnName The name of the column
     * @param bIsPrimaryKey True if the column has a primary key constraint,
     *            false otherwise
     * @param columnType The type of the column
     */
    public TableColumn( String strColumnName, boolean bIsPrimaryKey, ColumnType columnType )
    {
        _strColumnName = strColumnName;
        _bIsPrimaryKey = bIsPrimaryKey;
        _columnType = columnType;
    }

    /**
     * Get the name of the column
     * @return The name of the column
     */
    public String getColumnName( )
    {
        return _strColumnName;
    }

    /**
     * Set the name of the column
     * @param strColumnName The name of the column
     */
    public void setColumnName( String strColumnName )
    {
        this._strColumnName = strColumnName;
    }

    /**
     * Check if this column has a primary key constraint
     * @return True if this column has a primary key constraint, false otherwise
     */
    public boolean getIsPrimaryKey( )
    {
        return _bIsPrimaryKey;
    }

    /**
     * Set whether this column has a primary key constraint
     * @param bIsPrimaryKey True if this column has a primary key constraint,
     *            false otherwise
     */
    public void setIsPrimaryKey( boolean bIsPrimaryKey )
    {
        this._bIsPrimaryKey = bIsPrimaryKey;
    }

    /**
     * Get the type of this column
     * @return The type of this column
     */
    public ColumnType getColumnType( )
    {
        return _columnType;
    }

    /**
     * Set the type of this column
     * @param columnType The type of this column
     */
    public void setColumnType( ColumnType columnType )
    {
        this._columnType = columnType;
    }
}
