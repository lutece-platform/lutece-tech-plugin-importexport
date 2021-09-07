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
package fr.paris.lutece.plugins.importexport.service.export.daemon;

import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * Daemon to remove old exports of data from the file system
 */
public class ExportCleanerDaemon extends Daemon
{
    private static final String PLUGIN_IMPORTEXPORT_FOLDER = "plugins/importexport/";

    private static final String PROPERTY_EXPORT_FILES_LIFE_TIME = "importexport.exportdata.exportFileLifeTime";

    private static final long DEFAULT_EXPORT_FILES_LIFE_TIME = 7200000l;

    private static final String CONSTANT_SLASH = "/";

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( )
    {
        File mainExportFolder = new File( AppPathService.getWebAppPath( ) + CONSTANT_SLASH + PLUGIN_IMPORTEXPORT_FOLDER );
        long lExportFileLifeTime = AppPropertiesService.getPropertyLong( PROPERTY_EXPORT_FILES_LIFE_TIME, DEFAULT_EXPORT_FILES_LIFE_TIME );
        long lDateThreshold = new Date( ).getTime( ) - lExportFileLifeTime;
        int nFileRemoved = 0;
        int nFileIgnored = 0;
        if ( mainExportFolder.exists( ) )
        {
            for ( File userFolder : mainExportFolder.listFiles( (FileFilter) FileFilterUtils.directoryFileFilter( ) ) )
            {
                for ( File file : userFolder.listFiles( ) )
                {
                    if ( file.lastModified( ) < lDateThreshold )
                    {
                        boolean bRes = file.delete( );
                        if ( bRes )
                        {
                            nFileRemoved++;
                        }
                        else
                        {
                            nFileIgnored++;
                        }
                    }
                }
            }
        }
        setLastRunLogs( nFileRemoved + " old export file(s) have been removed and " + nFileIgnored + " have been ignored" );
    }

}
