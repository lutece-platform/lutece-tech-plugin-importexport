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

import fr.paris.lutece.plugins.importexport.business.importdata.ImportResult;
import fr.paris.lutece.plugins.importexport.service.ImportExportPlugin;
import fr.paris.lutece.plugins.importexport.service.importdata.IImportSource;
import fr.paris.lutece.plugins.importexport.service.importdata.ImportManager;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.admin.AdminFeaturesPageJspBean;
import fr.paris.lutece.portal.web.upload.MultipartHttpServletRequest;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
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
    private static final String PROPERTY_ASYNCHRONOUS_IMPORT_FILE_SIZE = "importexport.importdata.asynchronousImportFileSize";

    // Messages
    private static final String MESSAGE_FILE_NOT_VALIDE = "importexport.import_data.labelErrorFileFormatNotSupported";
    private static final String MESSAGE_MANDATORY_FIELDS = "portal.util.message.mandatoryFields";

    // Marks
    private static final String MARK_DATABASE_TABLES = "databaseTables";
    private static final String MARK_LIST_PLUGIN = "listPlugin";
    private static final String MARK_SESSION_IMPORT_RESULT = "importexport.session_import_result";
    private static final String MARK_RESULT = "result";

    // Parameters
    private static final String PARAMETER_FILE = "file";
    private static final String PARAMETER_TABLE_NAME = "databaseTable";
    private static final String PARAMETER_PLUGIN_NAME = "plugin";
    private static final String PARAMETER_UPDATE_EXISTING_ROWS = "update";
    private static final String PARAMETER_STOP_ON_ERRORS = "stopOnErrors";

    // Templates
    private static final String TEMPLATE_IMPORT_DATA = "admin/plugins/importexport/import_data.html";
    private static final String TEMPLATE_IMPORT_WAITING = "admin/plugins/importexport/import_waiting.html";
    private static final String TEMPLATE_IMPORT_RESULT = "admin/plugins/importexport/import_result.html";

    private static final String JSP_URL_MANAGE_IMPORT = "jsp/admin/plugins/importexport/ManageImportData.jsp";
    private static final String JSP_URL_IMPORT_PROCESSING = "jsp/admin/plugins/importexport/GetImportProcessing.jsp";
    private static final String JSP_URL_IMPORT_RESULT = "jsp/admin/plugins/importexport/GetImportResult.jsp";

    private static final String CONSTANT_SEMICOLON = ";";

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
        // We remove any previous import result generated synchronously or asynchronously
        request.getSession( ).removeAttribute( MARK_SESSION_IMPORT_RESULT );
        ImportManager.getAsynchronousImportResult( AdminUserService.getAdminUser( request ).getUserId( ) );

        String strDatabaseTables = AppPropertiesService.getProperty( PROPERTY_DATABASE_TABLES );
        ReferenceList refList = new ReferenceList( );
        if ( StringUtils.isNotBlank( strDatabaseTables ) )
        {
            for ( String strDatabaseTable : strDatabaseTables.split( CONSTANT_SEMICOLON ) )
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
        ReferenceItem refItemPlugin = new ReferenceItem( );
        Plugin core = PluginService.getCore( );
        refItemPlugin.setName( core.getName( ) );
        refItemPlugin.setCode( core.getName( ) );
        refListPlugins.add( refItemPlugin );
        for ( Plugin plugin : listPlugins )
        {
            if ( plugin.isDbPoolRequired( ) )
            {
                refItemPlugin = new ReferenceItem( );
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
     * Do import data into the database
     * @param request The request
     * @return The next URL to redirect to
     * @throws AccessDeniedException If the table to import data in has not been
     *             declared as an importable table
     */
    public String doImportData( HttpServletRequest request ) throws AccessDeniedException
    {
        AdminUser admin = AdminUserService.getAdminUser( request );
        if ( ImportManager.hasImportInProcess( admin.getUserId( ) ) )
        {
            return AppPathService.getBaseUrl( request ) + JSP_URL_IMPORT_PROCESSING;
        }

        if ( request instanceof MultipartHttpServletRequest )
        {
            FileItem fileItem = ( (MultipartHttpServletRequest) request ).getFile( PARAMETER_FILE );
            String strTableName = request.getParameter( PARAMETER_TABLE_NAME );
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            Plugin plugin = PluginService.getPlugin( strPluginName );
            boolean bUpdateExistingRows = Boolean.parseBoolean( request.getParameter( PARAMETER_UPDATE_EXISTING_ROWS ) );
            boolean bStopOnErrors = Boolean.parseBoolean( request.getParameter( PARAMETER_STOP_ON_ERRORS ) );

            if ( fileItem == null || StringUtils.isEmpty( strTableName ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_MANDATORY_FIELDS, AdminMessage.TYPE_ERROR );
            }
            boolean bAuthorizedTable = false;
            String strDatabaseTables = AppPropertiesService.getProperty( PROPERTY_DATABASE_TABLES );
            for ( String strDatabaseTable : strDatabaseTables.split( CONSTANT_SEMICOLON ) )
            {
                if ( StringUtils.equals( strDatabaseTable, strTableName ) )
                {
                    bAuthorizedTable = true;
                    break;
                }
            }

            if ( !bAuthorizedTable )
            {
                throw new AccessDeniedException( "The database table '" + strTableName
                        + "' has NOT been decalred as an importable table" );
            }

            IImportSource importSource = ImportManager.getImportSource( fileItem );
            if ( importSource != null )
            {
                long lThresholdSize = AppPropertiesService.getPropertyLong( PROPERTY_ASYNCHRONOUS_IMPORT_FILE_SIZE,
                        1048576l );
                Locale locale = AdminUserService.getLocale( request );
                if ( fileItem.getSize( ) < lThresholdSize )
                {
                    ImportResult result = ImportManager.doProcessImport( importSource, strTableName,
                            bUpdateExistingRows, bStopOnErrors, plugin, locale );
                    request.getSession( ).setAttribute( MARK_SESSION_IMPORT_RESULT, result );
                    return AppPathService.getBaseUrl( request ) + JSP_URL_IMPORT_RESULT;
                }
                ImportManager.doProcessAsynchronousImport( importSource, strTableName, plugin, locale,
                        bUpdateExistingRows, bStopOnErrors, admin );
                return AppPathService.getBaseUrl( request ) + JSP_URL_IMPORT_PROCESSING;

            }
            return AdminMessageService.getMessageUrl( request, MESSAGE_FILE_NOT_VALIDE, AdminMessage.TYPE_ERROR );
        }
        return AppPathService.getBaseUrl( request ) + JSP_URL_MANAGE_IMPORT;
    }

    /**
     * Get the waiting page if an import has been started by the user and has
     * not terminated yet. If there is no running import, then the import result
     * page is displayed
     * @param request The request
     * @return The HTML content to display
     */
    public String getImportProcessing( HttpServletRequest request )
    {
        AdminUser admin = AdminUserService.getAdminUser( request );
        if ( ImportManager.hasImportInProcess( admin.getUserId( ) ) )
        {
            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_IMPORT_WAITING,
                    AdminUserService.getLocale( request ) );
            return getAdminPage( template.getHtml( ) );
        }
        return getImportResult( request );
    }

    /**
     * Get the import result page. If the user has already displayed results of
     * imports, or if he has not started any import then page to create import
     * is displayed instead
     * @param request The request
     * @return The HTML content to display
     */
    public String getImportResult( HttpServletRequest request )
    {
        AdminUser admin = AdminUserService.getAdminUser( request );
        ImportResult result = (ImportResult) request.getSession( ).getAttribute( MARK_SESSION_IMPORT_RESULT );
        if ( result == null )
        {
            result = ImportManager.getAsynchronousImportResult( admin.getUserId( ) );
            if ( result == null )
            {
                return getImportData( request );
            }
        }
        else
        {
            request.getSession( ).removeAttribute( MARK_SESSION_IMPORT_RESULT );
        }

        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( MARK_RESULT, result );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_IMPORT_RESULT,
                AdminUserService.getLocale( request ), model );

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
