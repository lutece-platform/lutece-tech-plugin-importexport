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
package fr.paris.lutece.plugins.importexport.business.importdata;

import java.util.List;


/**
 * Describes the result of an import
 */
public class ImportResult
{
    private int _nCreatedElements;
    private int _nUpdatedElements;
    private int _nIgnoredElements;
    private List<ImportMessage> _listImportMessage;

    /**
     * Default constructor
     */
    public ImportResult( )
    {

    }

    /**
     * Constructor with parameters initialized
     * @param nCreatedElements The number of created elements
     * @param nUpdatedElements The number of updated elements
     * @param nIgnoredElements The number of ignored elements
     * @param listMessages The list of messages
     */
    public ImportResult( int nCreatedElements, int nUpdatedElements, int nIgnoredElements,
            List<ImportMessage> listMessages )
    {
        setCreatedElements( nCreatedElements );
        setUpdatedElements( nUpdatedElements );
        setIgnoredElements( nIgnoredElements );
        setListImportMessage( listMessages );
    }

    /**
     * Get the number of created elements
     * @return The number of created elements
     */
    public int getCreatedElements( )
    {
        return _nCreatedElements;
    }

    /**
     * Set the number of created elements
     * @param nCreatedElements The number of created elements
     */
    public void setCreatedElements( int nCreatedElements )
    {
        this._nCreatedElements = nCreatedElements;
    }

    /**
     * Get the number of updated elements
     * @return The number of updated elements
     */
    public int getUpdatedElements( )
    {
        return _nUpdatedElements;
    }

    /**
     * Set the number of updated elements
     * @param nUpdatedElements The number of updated elements
     */
    public void setUpdatedElements( int nUpdatedElements )
    {
        this._nUpdatedElements = nUpdatedElements;
    }

    /**
     * Get the number of ignored elements
     * @return The number of ignored elements
     */
    public int getIgnoredElements( )
    {
        return _nIgnoredElements;
    }

    /**
     * Set the number of ignored elements
     * @param nIgnoredElements The number of ignored elements
     */
    public void setIgnoredElements( int nIgnoredElements )
    {
        this._nIgnoredElements = nIgnoredElements;
    }

    /**
     * Get the list of import messages
     * @return The list of import messages
     */
    public List<ImportMessage> getListImportMessage( )
    {
        return _listImportMessage;
    }

    /**
     * Set the list of import messages
     * @param listImportMessage The list of import messages
     */
    public void setListImportMessage( List<ImportMessage> listImportMessage )
    {
        this._listImportMessage = listImportMessage;
    }

}
