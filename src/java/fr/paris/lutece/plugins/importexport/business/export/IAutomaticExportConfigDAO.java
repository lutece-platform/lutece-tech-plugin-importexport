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
package fr.paris.lutece.plugins.importexport.business.export;

import java.util.List;


/**
 * Interface for the DAO of automatic export configuration
 */
public interface IAutomaticExportConfigDAO
{
    /**
     * Get a configuration from its id
     * @param nId The id of the configuration
     * @return The configuration, or null if no configuration has the given id
     */
    AutomaticExportConfig findById( int nId );

    /**
     * Get the list of every configuration
     * @param bLoadColumns True to load columns of configurations, false to
     *            ignore them
     * @return The list of every configuration. If there is no configuration,
     *         then an empty list is returned
     */
    List<AutomaticExportConfig> findAll( boolean bLoadColumns );

    /**
     * Insert a new configuration into the database.
     * @param config The configuration to update
     */
    void insert( AutomaticExportConfig config );

    /**
     * Update a configuration. Both the configuration and the list of columns
     * are update
     * @param config The configuration to update
     */
    void update( AutomaticExportConfig config );

    /**
     * Remove a configuration from the database by its id
     * @param nId The id of the configuration to remove
     */
    void delete( int nId );

}
