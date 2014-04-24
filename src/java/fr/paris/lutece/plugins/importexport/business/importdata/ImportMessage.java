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
package fr.paris.lutece.plugins.importexport.business.importdata;

/**
 * Describe a message of imported elements
 */
public class ImportMessage
{
    /**
     * Status that indicates that the item was skipped
     */
    public static final int STATUS_SKIPPED = 0;
    /**
     * Status that indicates that an error occurred during the importation of
     * the item
     */
    public static final int STATUS_ERROR = 1;
    /**
     * Status that indicates that the item was successfully imported
     */
    public static final int STATUS_OK = 10;

    private String _strMessage;
    private int _nStatus;
    private int _nItemNumber;

    /**
     * Creates a new import message
     * @param strMessage The message
     * @param nStatus The {@link #setStatus(int) status} of the message
     * @param nItemNumber The number of the imported item
     */
    public ImportMessage( String strMessage, int nStatus, int nItemNumber )
    {
        this._strMessage = strMessage;
        this._nStatus = nStatus;
        this._nItemNumber = nItemNumber;
    }

    /**
     * Get the message
     * @return the message
     */
    public String getMessage( )
    {
        return _strMessage;
    }

    /**
     * Set the message
     * @param strMessage the message
     */
    public void setMessage( String strMessage )
    {
        this._strMessage = strMessage;
    }

    /**
     * Get the status of the importation of the item described by this
     * {@link ImportMessage}. Status are :
     * <ul>
     * <li>{@link #STATUS_SKIPPED} if the item was skipped</li>
     * <li>{@link #STATUS_ERROR} if an error occurred during the importation of
     * the item</li>
     * <li>{@link #STATUS_OK} if the item was successfully imported</li>
     * </ul>
     * @return The status
     */
    public int getStatus( )
    {
        return _nStatus;
    }

    /**
     * Set the status of importation of the item described by this
     * {@link ImportMessage}.
     * @param nStatus <ul>
     *            <li>{@link #STATUS_SKIPPED} if the item was skipped</li>
     *            <li>{@link #STATUS_ERROR} if an error occurred during the
     *            importation of the item</li>
     *            <li>{@link #STATUS_OK} if the item was successfully imported</li>
     *            </ul>
     */
    public void setStatus( int nStatus )
    {
        this._nStatus = nStatus;
    }

    /**
     * Get the number of the imported item in the data source
     * @return the number of the imported item in the data source
     */
    public int getItemNumber( )
    {
        return _nItemNumber;
    }

    /**
     * Set the number of the imported item in the data source. This number
     * should be unique for every row of a data source
     * @param nItemNumber The imported item number
     */
    public void setItemNumber( int nItemNumber )
    {
        this._nItemNumber = nItemNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString( )
    {
        StringBuilder sbMessage = new StringBuilder( );
        if ( _nStatus == STATUS_ERROR )
        {
            sbMessage.append( "Error : " );
        }
        else if ( _nStatus == STATUS_SKIPPED )
        {
            sbMessage.append( "Skipped : " );
        }
        else if ( _nStatus == STATUS_OK )
        {
            sbMessage.append( "Ok : " );
        }
        sbMessage.append( _nItemNumber );
        sbMessage.append( ", " );
        sbMessage.append( _strMessage );

        return sbMessage.toString( );
    }
}
