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
package fr.paris.lutece.plugins.importexport.web;

import fr.paris.lutece.plugins.importexport.business.export.AutomaticExportConfig;
import fr.paris.lutece.plugins.importexport.business.export.ExportDAO;
import fr.paris.lutece.plugins.importexport.business.export.IAutomaticExportConfigDAO;
import fr.paris.lutece.plugins.importexport.service.ImportExportPlugin;
import fr.paris.lutece.plugins.importexport.service.export.ExportManager;
import fr.paris.lutece.plugins.importexport.service.export.daemon.ExportDaemon;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.business.xsl.XslExportHome;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.admin.AdminFeaturesPageJspBean;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.datatable.DataTableManager;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.url.UrlItem;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * This class provides the user interface to export data from the database
 */
public class ExportDataJspBean extends AdminFeaturesPageJspBean
{
    public static final String RIGHT_IMPORT_DATA = "IMPORTEXPORT_MANAGEMENT";
    public static final String PROPERTY_SESSION_AUTOMATIC_EXPORT_TABLE_MANAGER = "importexport.automaticExportTableManager";

    private static final long serialVersionUID = -8290123143145394692L;

    private static final String PROPERTY_DATABASE_TABLES = "importexport.database.exportableTableNames";
    private static final String PROPERTY_MESSAGE_EXPORT_DATA_PAGE_TITLE = "importexport.export_data.pageTitle";
    private static final String PROPERTY_MESSAGE_EXPORT_DATA_COLUMNS_SELECTION_PAGE_TITLE = "importexport.export_data.columns_selection.pageTitle";
    private static final String PROPERTY_MESSAGE_AUTOMATIC_EXPORT_CONFIGURATION_PAGE_TITLE = "importexport.export_data.automaticExportConfig.pageTitle";
    private static final String PROPERTY_MESSAGE_ADD_EXPORT_CONFIG = "importexport.export_data.addExportConfig.pageTitle";
    private static final String PROPERTY_MESSAGE_MODIFY_EXPORT_CONFIG = "importexport.export_data.modifyExportConfig.pageTitle";

    private static final String PROPERTY_AUTOMATIC_EXPORT_ID = "importexport.export_data.automaticExportConfig.labelId";
    private static final String PROPERTY_AUTOMATIC_EXPORT_TABLE_NAME = "importexport.export_data.automaticExportConfig.labelTableName";
    private static final String PROPERTY_AUTOMATIC_EXPORT_OUTPUT_FILE_NAME = "importexport.export_data.automaticExportConfig.labelOutputFileName";
    private static final String PROPERTY_AUTOMATIC_EXPORT_ACTION = "importexport.export_data.automaticExportConfig.lableActions";

    private static final String MARK_DATABASE_TABLES = "databaseTables";
    private static final String MARK_LIST_PLUGIN = "listPlugin";
    private static final String MARK_COLUMNS = "cols";
    private static final String MARK_LIST_XSL_EXPORT = "listXslExport";
    private static final String MARK_TABLE_MANAGER = "tableManager";
    private static final String MARK_DAEMON_NEXT_SCHEDULE = "daemon_next_schedule";
    private static final String MARK_DAEMON_INTERVAL = "daemon_interval";
    private static final String MARK_ID_CONFIG = "idConfig";
    private static final String MARK_CONF = "conf";

    private static final String PARAMETER_TABLE_NAME = "databaseTable";
    private static final String PARAMETER_PLUGIN_NAME = "plugin";
    private static final String PARAMETER_COLUMNS = "columns";
    private static final String PARAMETER_XSL_EXPORT_ID = "xslExport";
    private static final String PARAMETER_OUTPUT_FILE_NAME = "output_file_name";
    private static final String PARAMETER_PAGE_RELOAD = "pageReload";
    private static final String PARAMETER_SAVE_EXPORT_CONFIG = "saveExportConfig";
    private static final String PARAMETER_CANCEL = "cancel";

    private static final String TEMPLATE_EXPORT_DATA = "admin/plugins/importexport/export_data.html";
    private static final String TEMPLATE_EXPORT_DATA_SELECT_COLUMNS = "admin/plugins/importexport/export_data_select_columns.html";
    private static final String TEMPLATE_EXPORT_WAITING = "admin/plugins/importexport/export_waiting.html";
    private static final String TEMPLATE_AUTOMATIC_EXPORT_CONFIGURATION = "admin/plugins/importexport/automatic_export_config.html";
    private static final String TEMPLATE_CREATE_MODIFY_EXPORT_CONFIG = "admin/plugins/importexport/create_modify_export_config.html";

    private static final String MESSAGE_ERROR_NO_COLUMN_SELECTED = "importexport.export_data.error.noColumnSelected";
    private static final String MESSAGE_ERROR_UPDATE_COLUMNS = "importexport.export_data.automaticExportConfig.error.updateColumns";
    private static final String MESSAGE_ERROR_MANDATORY_FIELDS = "portal.util.message.mandatoryFields";
    private static final String MESSAGE_ERROR_DATE_FORMAT = "importexport.export_data.automaticExportConfig.error.dateFormat";
    private static final String MESSAGE_CONFIRM_DELETE_EXPORT_CONFIG = "importexport.export_data.automaticExportConfig.confirmDeleteExportConfig";

    private static final String JSP_URL_EXPORT_WAITING_PAGE = "jsp/admin/plugins/importexport/GetExportProcessing.jsp";
    private static final String JSP_URL_AUTOMATIC_EXPORT_CONFIGURATION = "jsp/admin/plugins/importexport/GetAutomaticExportConfiguration.jsp";
    private static final String JSP_URL_DO_DELETE_EXPORT_CONFIG = "jsp/admin/plugins/importexport/DoDeleteExportConfig.jsp";
    private static final String JSP_URL_EXPORT_DATA = "jsp/admin/plugins/importexport/GetExportData.jsp";

    private static final String CONSTANT_SEMICOLON = ";";

    private IAutomaticExportConfigDAO _automaticExportConfigDAO = SpringContextService.getBean( ExportManager.BEAN_NAME_AUTOMATIC_EXPORT_CONFIG_DAO );

    /**
     * Creates a new ExportDataJspBean object.
     */
    public ExportDataJspBean( )
    {
    }

    /**
     * Get the export data page
     * 
     * @param request
     *            The request
     * @return The HTML content to display
     */
    public String getExportData( HttpServletRequest request )
    {
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
        ReferenceList refListXslExport = XslExportHome.getRefListByPlugin( ImportExportPlugin.getPlugin( ) );
        model.put( MARK_LIST_XSL_EXPORT, refListXslExport );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EXPORT_DATA, AdminUserService.getLocale( request ), model );

        setPageTitleProperty( PROPERTY_MESSAGE_EXPORT_DATA_PAGE_TITLE );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Get the page to choose which columns of the table to export
     * 
     * @param request
     *            The request
     * @return The HTML content to display
     * @throws AccessDeniedException
     *             If the database table has not been declared as an exportable table
     */
    public String getExportColumns( HttpServletRequest request ) throws AccessDeniedException
    {
        String strTableName = request.getParameter( PARAMETER_TABLE_NAME );
        String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
        String strXslExportId = request.getParameter( PARAMETER_XSL_EXPORT_ID );
        Plugin plugin = PluginService.getPlugin( strPluginName );
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
            throw new AccessDeniedException( "The database table '" + strTableName + "' has NOT been decalred as an exportable table" );
        }
        Map<String, Object> model = new HashMap<String, Object>( );

        model.put( PARAMETER_TABLE_NAME, strTableName );
        model.put( PARAMETER_PLUGIN_NAME, strPluginName );
        model.put( PARAMETER_XSL_EXPORT_ID, strXslExportId );
        List<String> listColumns = ExportDAO.getTableColumnsNames( strTableName, plugin );
        model.put( MARK_COLUMNS, listColumns );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EXPORT_DATA_SELECT_COLUMNS, AdminUserService.getLocale( request ), model );

        setPageTitleProperty( PROPERTY_MESSAGE_EXPORT_DATA_COLUMNS_SELECTION_PAGE_TITLE );

        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Do export data from a database table and start a download of the export
     * 
     * @param request
     *            The request
     * @return The next URL to redirect to
     * @throws AccessDeniedException
     *             If the database table has not been declared as an exportable table
     * @throws IOException
     *             If an IOException occurs
     */
    public String doExportData( HttpServletRequest request ) throws AccessDeniedException, IOException
    {
        String strTableName = request.getParameter( PARAMETER_TABLE_NAME );
        String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
        String strXslExportId = request.getParameter( PARAMETER_XSL_EXPORT_ID );
        Plugin plugin = PluginService.getPlugin( strPluginName );
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
            throw new AccessDeniedException( "The database table '" + strTableName + "' has NOT been decalred as an exportable table" );
        }

        String [ ] strColumns = request.getParameterValues( PARAMETER_COLUMNS );
        if ( strColumns == null || strColumns.length == 0 )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_NO_COLUMN_SELECTED, AdminMessage.TYPE_STOP );
        }
        List<String> listColumns = new ArrayList<String>( strColumns.length );
        for ( String strColumn : strColumns )
        {
            listColumns.add( strColumn );
        }

        int nXslExportId = 0;
        if ( StringUtils.isNumeric( strXslExportId ) )
        {
            nXslExportId = Integer.parseInt( strXslExportId );
        }
        ExportManager.registerAsynchronousExport( strTableName, listColumns, nXslExportId, plugin, AdminUserService.getAdminUser( request ) );
        return AppPathService.getBaseUrl( request ) + JSP_URL_EXPORT_WAITING_PAGE;
    }

    /**
     * Get the waiting page that indicates that an export is processing, or the result page if the export has ended.
     * 
     * @param request
     *            The request
     * @param response
     *            The response
     * @return The HTML content to display, or null if a download has be initialized
     */
    public String getExportProcessing( HttpServletRequest request, HttpServletResponse response )
    {
        AdminUser admin = AdminUserService.getAdminUser( request );
        if ( ExportManager.hasExportInProcess( admin.getUserId( ) ) )
        {
            HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EXPORT_WAITING, AdminUserService.getLocale( request ) );
            return getAdminPage( template.getHtml( ) );
        }

        return getExportResult( request, response );
    }

    /**
     * Get the page that display the result of an import, or the export data main page if no export result is available for the given user
     * 
     * @param request
     *            The request
     * @param response
     *            The response
     * @return The HTML content to display, or null if a download has be initialized
     */
    public String getExportResult( HttpServletRequest request, HttpServletResponse response )
    {
        String strFileURl = ExportManager.getExportResult( AdminUserService.getAdminUser( request ) );
        if ( StringUtils.isNotBlank( strFileURl ) )
        {
            try
            {
                response.sendRedirect( AppPathService.getBaseUrl( request ) + strFileURl );
                return null;
            }
            catch( IOException e )
            {
                AppLogService.error( e.getMessage( ), e );
            }
        }
        return getExportData( request );
    }

    /**
     * Get the page to manage automatic exports
     * 
     * @param request
     *            The request
     * @return The HTML content to display
     */
    public String getAutomaticExportConfiguration( HttpServletRequest request )
    {
        List<AutomaticExportConfig> listConfig = _automaticExportConfigDAO.findAll( false );
        String strDaemonNextSchedule = ExportDaemon.getDaemonNextSchedule( );
        String strInterval = ExportDaemon.getDaemonInterval( );
        long lInterval = Long.parseLong( strInterval );

        Date dateNextSchedule;
        if ( !StringUtils.isEmpty( strDaemonNextSchedule ) && StringUtils.isNumeric( strDaemonNextSchedule ) )
        {
            dateNextSchedule = new Date( Long.parseLong( strDaemonNextSchedule ) );
        }
        else
        {
            dateNextSchedule = new Date( );
        }

        DataTableManager<AutomaticExportConfig> tableManager = (DataTableManager<AutomaticExportConfig>) request.getSession( )
                .getAttribute( PROPERTY_SESSION_AUTOMATIC_EXPORT_TABLE_MANAGER );
        if ( tableManager == null )
        {
            tableManager = new DataTableManager<AutomaticExportConfig>( JSP_URL_AUTOMATIC_EXPORT_CONFIGURATION, JSP_URL_AUTOMATIC_EXPORT_CONFIGURATION, 50,
                    true );
            tableManager.addColumn( PROPERTY_AUTOMATIC_EXPORT_ID, "id", true );
            tableManager.addColumn( PROPERTY_AUTOMATIC_EXPORT_TABLE_NAME, "tableName", true );
            tableManager.addColumn( PROPERTY_AUTOMATIC_EXPORT_OUTPUT_FILE_NAME, "outputFileName", true );
            tableManager.addActionColumn( PROPERTY_AUTOMATIC_EXPORT_ACTION );
            request.getSession( ).setAttribute( PROPERTY_SESSION_AUTOMATIC_EXPORT_TABLE_MANAGER, tableManager );
        }
        tableManager.filterSortAndPaginate( request, listConfig );

        Map<String, Object> model = new HashMap<String, Object>( );
        model.put( MARK_TABLE_MANAGER, tableManager );
        DateFormat df = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
        model.put( MARK_DAEMON_NEXT_SCHEDULE, df.format( dateNextSchedule ) );
        model.put( MARK_DAEMON_INTERVAL, lInterval / 1000 );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_AUTOMATIC_EXPORT_CONFIGURATION, AdminUserService.getLocale( request ), model );

        setPageTitleProperty( PROPERTY_MESSAGE_AUTOMATIC_EXPORT_CONFIGURATION_PAGE_TITLE );
        String strContent = template.getHtml( );
        tableManager.clearItems( );
        return getAdminPage( strContent );
    }

    /**
     * Do modify the configuration of automatic export
     * 
     * @param request
     *            The request
     * @return The next URL to redirect to
     */
    public String doModifyAutomaticExport( HttpServletRequest request )
    {
        if ( StringUtils.isEmpty( request.getParameter( PARAMETER_CANCEL ) ) )
        {
            String strDaemonNextSchedule = request.getParameter( MARK_DAEMON_NEXT_SCHEDULE );
            String strInterval = request.getParameter( MARK_DAEMON_INTERVAL );
            if ( StringUtils.isEmpty( strDaemonNextSchedule ) || StringUtils.isEmpty( strInterval ) || !StringUtils.isNumeric( strInterval ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_MANDATORY_FIELDS, AdminMessage.TYPE_ERROR );
            }
            long lInterval = Long.parseLong( strInterval );
            try
            {
                DateFormat df = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
                Date dateNextSchedule = df.parse( strDaemonNextSchedule );
                ExportDaemon.setDaemonNextSchedule( dateNextSchedule );
                ExportDaemon.setDaemonInterval( lInterval * 1000 );

            }
            catch( ParseException e )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_DATE_FORMAT, AdminMessage.TYPE_ERROR );
            }
        }
        return AppPathService.getBaseUrl( request ) + JSP_URL_EXPORT_DATA;
    }

    /**
     * Get the page to create or modify an export config
     * 
     * @param request
     *            The request
     * @param response
     *            The response
     * @return The HTML content to display, or null if a redirection has already been send
     */
    public String getCreateModifyExportConfig( HttpServletRequest request, HttpServletResponse response )
    {

        if ( StringUtils.isNotEmpty( request.getParameter( PARAMETER_CANCEL ) ) )
        {
            try
            {
                response.sendRedirect( AppPathService.getBaseUrl( request ) + JSP_URL_AUTOMATIC_EXPORT_CONFIGURATION );
            }
            catch( IOException e )
            {
                AppLogService.error( e.getMessage( ), e );
            }
            return null;
        }
        if ( StringUtils.isNotEmpty( request.getParameter( PARAMETER_SAVE_EXPORT_CONFIG ) ) )
        {
            try
            {
                response.sendRedirect( doCreateModifyExportConfig( request ) );
            }
            catch( IOException e )
            {
                AppLogService.error( e.getMessage( ), e );
            }
            return null;
        }

        String strIdConfig = request.getParameter( MARK_ID_CONFIG );
        boolean bPageReload = StringUtils.isNotEmpty( request.getParameter( PARAMETER_PAGE_RELOAD ) );
        Map<String, Object> model = new HashMap<String, Object>( );
        if ( StringUtils.isNotEmpty( strIdConfig ) && StringUtils.isNumeric( strIdConfig ) )
        {
            int nIdConfig = Integer.parseInt( strIdConfig );
            AutomaticExportConfig config;
            if ( bPageReload )
            {
                config = getConfigFromRequest( request );
            }
            else
            {
                config = _automaticExportConfigDAO.findById( nIdConfig );
            }
            if ( StringUtils.isNotEmpty( config.getTableName( ) ) )
            {
                List<String> listColumns = ExportDAO.getTableColumnsNames( config.getTableName( ), config.getPlugin( ) );
                model.put( MARK_COLUMNS, listColumns );
            }
            model.put( MARK_CONF, config );
            model.put( MARK_ID_CONFIG, strIdConfig );
            setPageTitleProperty( PROPERTY_MESSAGE_MODIFY_EXPORT_CONFIG );
        }
        else
        {
            setPageTitleProperty( PROPERTY_MESSAGE_ADD_EXPORT_CONFIG );
            if ( bPageReload )
            {
                AutomaticExportConfig config = getConfigFromRequest( request );
                model.put( MARK_CONF, config );
                if ( StringUtils.isNotEmpty( config.getTableName( ) ) )
                {
                    List<String> listColumns = ExportDAO.getTableColumnsNames( config.getTableName( ), config.getPlugin( ) );
                    model.put( MARK_COLUMNS, listColumns );
                }
            }
        }

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
        ReferenceList refListXslExport = XslExportHome.getRefListByPlugin( ImportExportPlugin.getPlugin( ) );
        model.put( MARK_LIST_XSL_EXPORT, refListXslExport );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_MODIFY_EXPORT_CONFIG, AdminUserService.getLocale( request ), model );
        return getAdminPage( template.getHtml( ) );
    }

    /**
     * Do create or modify an export config
     * 
     * @param request
     *            The request
     * @return The next URL to redirect to
     */
    public String doCreateModifyExportConfig( HttpServletRequest request )
    {
        if ( StringUtils.isEmpty( request.getParameter( PARAMETER_CANCEL ) ) )
        {
            AutomaticExportConfig config = getConfigFromRequest( request );

            if ( StringUtils.isEmpty( config.getTableName( ) ) || StringUtils.isEmpty( config.getOutputFileName( ) ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_MANDATORY_FIELDS, AdminMessage.TYPE_ERROR );
            }
            if ( config.getListColumns( ) == null || config.getListColumns( ).size( ) == 0 )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_NO_COLUMN_SELECTED, AdminMessage.TYPE_ERROR );
            }
            List<String> listColumns = ExportDAO.getTableColumnsNames( config.getTableName( ), config.getPlugin( ) );
            if ( !listColumns.containsAll( config.getListColumns( ) ) )
            {
                return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_UPDATE_COLUMNS, AdminMessage.TYPE_ERROR );
            }

            if ( config.getId( ) > 0 )
            {
                _automaticExportConfigDAO.update( config );
            }
            else
            {
                _automaticExportConfigDAO.insert( config );
            }
        }
        return AppPathService.getBaseUrl( request ) + JSP_URL_AUTOMATIC_EXPORT_CONFIGURATION;
    }

    /**
     * Get an export config from an HTTP request
     * 
     * @param request
     *            The request
     * @return The export config containing data in the request
     */
    private AutomaticExportConfig getConfigFromRequest( HttpServletRequest request )
    {
        String strIdConfig = request.getParameter( MARK_ID_CONFIG );
        String strTableName = request.getParameter( PARAMETER_TABLE_NAME );
        String strOutputFileName = request.getParameter( PARAMETER_OUTPUT_FILE_NAME );
        String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
        String strXslExport = request.getParameter( PARAMETER_XSL_EXPORT_ID );
        String [ ] strListColumns = request.getParameterValues( PARAMETER_COLUMNS );
        AutomaticExportConfig config = new AutomaticExportConfig( );
        if ( StringUtils.isNotEmpty( strIdConfig ) && StringUtils.isNumeric( strIdConfig ) )
        {
            int nId = Integer.parseInt( strIdConfig );
            config.setId( nId );
        }
        config.setTableName( strTableName );
        config.setOutputFileName( strOutputFileName );
        config.setPlugin( PluginService.getPlugin( strPluginName ) );
        if ( StringUtils.isNotEmpty( strXslExport ) && StringUtils.isNumeric( strXslExport ) )
        {
            config.setXslStylesheetId( Integer.parseInt( strXslExport ) );
        }
        if ( strListColumns != null && strListColumns.length > 0 )
        {
            List<String> listColumns = new ArrayList<String>( strListColumns.length );
            for ( String strColumn : strListColumns )
            {
                listColumns.add( strColumn );
            }
            config.setListColumns( listColumns );
        }

        return config;
    }

    /**
     * Confirm the removal of an automatic export configuration
     * 
     * @param request
     *            The request
     * @return The next URL to redirect to
     */
    public String confirmDeleteExportConfig( HttpServletRequest request )
    {
        UrlItem urlItem = new UrlItem( JSP_URL_DO_DELETE_EXPORT_CONFIG );
        urlItem.addParameter( MARK_ID_CONFIG, request.getParameter( MARK_ID_CONFIG ) );
        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_DELETE_EXPORT_CONFIG, urlItem.getUrl( ), AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Do remove an automatic export configuration
     * 
     * @param request
     *            The request
     * @return The next URL to redirect to
     */
    public String doDeleteExportConfig( HttpServletRequest request )
    {
        String strId = request.getParameter( MARK_ID_CONFIG );
        if ( StringUtils.isNotEmpty( strId ) && StringUtils.isNumeric( strId ) )
        {
            int nId = Integer.parseInt( strId );
            _automaticExportConfigDAO.delete( nId );
        }
        return AppPathService.getBaseUrl( request ) + JSP_URL_AUTOMATIC_EXPORT_CONFIGURATION;
    }
}
