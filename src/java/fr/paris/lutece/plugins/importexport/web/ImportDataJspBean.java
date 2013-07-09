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
package fr.paris.lutece.plugins.importexport.web;

import fr.paris.lutece.plugins.importexport.service.ImportExportPlugin;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.admin.AdminFeaturesPageJspBean;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;


/**
 * This class provides the user interface to import data into the database
 */
public class ImportDataJspBean extends AdminFeaturesPageJspBean
{
    public static final String RIGHT_IMPORT_DATA = "IMPORTDATA_MANAGEMENT";

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 8260626003003487223L;

    // Properties
    private static final String PROPERTY_DATABASE_TABLES = "importexport.database.importableTableNames";
    private static final String PROPERTY_MESSAGE_IMPORT_DATA_PAGE_TITLE = "importexport.adminFeature.importdata_management.name";

    // Marks
    private static final String MARK_DATABASE_TABLES = "databaseTables";
    private static final String MARK_LIST_PLUGIN = "listPlugin";

    // Templates
    private static final String TEMPLATE_IMPORT_DATA = "admin/plugins/importexport/import_data.html";

    /**
     * Creates a new ImportDataJspBean object.
     */
    public ImportDataJspBean( )
    {
    }

    /**
     * Get the import data page
     * @param request The request
     * @return The HTML content to display
     */
    public String getImportData( HttpServletRequest request )
    {
        String strDatabaseTables = AppPropertiesService.getProperty( PROPERTY_DATABASE_TABLES );
        ReferenceList refList = new ReferenceList( );
        if ( StringUtils.isNotBlank( strDatabaseTables ) )
        {
            for ( String strDatabaseTable : strDatabaseTables.split( ";" ) )
            {
                ReferenceItem refItem = new ReferenceItem( );
                refItem.setCode( strDatabaseTable );
                refItem.setName( strDatabaseTable );
                refList.add( refItem );
            }
        }
        Map<String, Object> model = new HashMap<String, Object>( );

        model.put( MARK_DATABASE_TABLES, refList );
        Collection<Plugin> listPlugins = PluginService.getPluginList( );
        ReferenceList refListPlugins = new ReferenceList( );
        for ( Plugin plugin : listPlugins )
        {
            if ( plugin.isDbPoolRequired( ) )
            {
                ReferenceItem refItemPlugin = new ReferenceItem( );
                refItemPlugin.setName( plugin.getName( ) );
                refItemPlugin.setCode( plugin.getName( ) );
                refListPlugins.add( refItemPlugin );
            }
        }
        model.put( MARK_LIST_PLUGIN, refListPlugins );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_IMPORT_DATA,
                AdminUserService.getLocale( request ), model );

        setPageTitleProperty( PROPERTY_MESSAGE_IMPORT_DATA_PAGE_TITLE );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Return the plugin
     * @return Plugin
     */
    public Plugin getPlugin( )
    {
        return PluginService.getPlugin( ImportExportPlugin.PLUGIN_NAME );
    }
}
