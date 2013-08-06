package fr.paris.lutece.plugins.importexport.business.export;

import fr.paris.lutece.plugins.importexport.service.ImportExportPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * Implementation of the IAutomaticExportConfigDAO interface
 */
public class AutomaticExportConfigDAO implements IAutomaticExportConfigDAO
{
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = " SELECT MAX(id) FROM importexport_export_config ";
    private static final String SQL_QUERY_FIND_CONFIG_BY_PRIMARY_KEY = " SELECT id, table_name, output_file_name, xsl_stylesheet_id, plugin FROM importexport_export_config WHERE id = ? ";
    private static final String SQL_QUERY_FIND_ALL_CONFIG = " SELECT id, table_name, output_file_name, xsl_stylesheet_id, plugin FROM importexport_export_config ";
    private static final String SQL_QUERY_INSERT_CONFIG = " INSERT INTO importexport_export_config( id, table_name, output_file_name, xsl_stylesheet_id, plugin ) VALUES (?,?,?,?,?) ";
    private static final String SQL_QUERY_UPDATE_CONFIG = " UPDATE importexport_export_config SET table_name = ?, output_file_name = ?, xsl_stylesheet_id = ?, plugin = ? WHERE id = ? ";
    private static final String SQL_QUERY_DELETE_CONFIG = " DELETE FROM importexport_export_config WHERE id = ? ";
    private static final String SQL_QUERY_FIND_COLUMNS = " SELECT column_name FROM importexport_export_config_columns WHERE id_config = ? ";
    private static final String SQL_QUERY_INSERT_COLUMNS = " INSERT INTO importexport_export_config_columns(id_config, column_name) VALUES (?,?) ";
    private static final String SQL_QUERY_DELETE_COLUMNS = " DELETE FROM importexport_export_config_columns WHERE id_config = ? ";

    private Plugin _plugin;

    /**
     * {@inheritDoc}
     */
    @Override
    public AutomaticExportConfig findById( int nId )
    {
        AutomaticExportConfig result = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_CONFIG_BY_PRIMARY_KEY, getPlugin( ) );
        daoUtil.setInt( 1, nId );
        daoUtil.executeQuery( );
        if ( daoUtil.next( ) )
        {
            int nIndex = 1;
            result = new AutomaticExportConfig( );
            result.setId( daoUtil.getInt( nIndex++ ) );
            result.setTableName( daoUtil.getString( nIndex++ ) );
            result.setOutputFileName( daoUtil.getString( nIndex++ ) );
            result.setXslStylesheetId( daoUtil.getInt( nIndex++ ) );
            result.setPlugin( PluginService.getPlugin( daoUtil.getString( nIndex ) ) );
        }
        daoUtil.free( );
        getListColumns( result );
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AutomaticExportConfig> findAll( boolean bLoadColumns )
    {
        List<AutomaticExportConfig> listResult = new ArrayList<AutomaticExportConfig>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_ALL_CONFIG, getPlugin( ) );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            int nIndex = 1;
            AutomaticExportConfig config = new AutomaticExportConfig( );
            config.setId( daoUtil.getInt( nIndex++ ) );
            config.setTableName( daoUtil.getString( nIndex++ ) );
            config.setOutputFileName( daoUtil.getString( nIndex++ ) );
            config.setXslStylesheetId( daoUtil.getInt( nIndex++ ) );
            config.setPlugin( PluginService.getPlugin( daoUtil.getString( nIndex ) ) );
            listResult.add( config );
        }
        daoUtil.free( );
        if ( bLoadColumns )
        {
            for ( AutomaticExportConfig config : listResult )
            {
                getListColumns( config );
            }
        }
        return listResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void insert( AutomaticExportConfig config )
    {
        config.setId( getNewPrimaryKey( ) );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_CONFIG, getPlugin( ) );
        int nIndex = 1;
        daoUtil.setInt( nIndex++, config.getId( ) );
        daoUtil.setString( nIndex++, config.getTableName( ) );
        daoUtil.setString( nIndex++, config.getOutputFileName( ) );
        daoUtil.setInt( nIndex++, config.getXslStylesheetId( ) );
        if ( config.getPlugin( ) == null )
        {
            daoUtil.setString( nIndex, StringUtils.EMPTY );
        }
        else
        {
            daoUtil.setString( nIndex, config.getPlugin( ).getName( ) );
        }
        daoUtil.executeUpdate( );
        daoUtil.free( );
        insertColumns( config.getId( ), config.getListColumns( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update( AutomaticExportConfig config )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_CONFIG, getPlugin( ) );
        int nIndex = 1;
        daoUtil.setString( nIndex++, config.getTableName( ) );
        daoUtil.setString( nIndex++, config.getOutputFileName( ) );
        daoUtil.setInt( nIndex++, config.getXslStylesheetId( ) );
        if ( config.getPlugin( ) == null )
        {
            daoUtil.setString( nIndex++, StringUtils.EMPTY );
        }
        else
        {
            daoUtil.setString( nIndex++, config.getPlugin( ).getName( ) );
        }

        daoUtil.setInt( nIndex, config.getId( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
        removeColumns( config.getId( ) );
        insertColumns( config.getId( ), config.getListColumns( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( int nId )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_CONFIG, getPlugin( ) );
        daoUtil.setInt( 1, nId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
        removeColumns( nId );
    }

    /**
     * Get the list of columns associated with a given configuration. The list
     * can be retrieved through the method
     * {@link AutomaticExportConfig#getListColumns()} of the configuration
     * @param config The configuration to set the list of columns
     */
    private void getListColumns( AutomaticExportConfig config )
    {
        if ( config == null )
        {
            return;
        }
        List<String> listColumns = new ArrayList<String>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_FIND_COLUMNS, getPlugin( ) );
        daoUtil.setInt( 1, config.getId( ) );
        daoUtil.executeQuery( );
        while ( daoUtil.next( ) )
        {
            listColumns.add( daoUtil.getString( 1 ) );
        }
        daoUtil.free( );
        config.setListColumns( listColumns );
    }

    /**
     * Insert a list of columns associated to a given configuration
     * @param nIdConfig The id of the configuration
     * @param listColumns The list of columns to insert
     */
    private void insertColumns( int nIdConfig, List<String> listColumns )
    {
        if ( listColumns == null )
        {
            return;
        }
        for ( String strColumn : listColumns )
        {
            DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_COLUMNS, getPlugin( ) );
            daoUtil.setInt( 1, nIdConfig );
            daoUtil.setString( 2, strColumn );
            daoUtil.executeUpdate( );
            daoUtil.free( );
        }
    }

    /**
     * Remove every columns associated with a given configuration
     * @param nIdConfig The id of the configuration to remove columns of
     */
    private void removeColumns( int nIdConfig )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_COLUMNS, getPlugin( ) );
        daoUtil.setInt( 1, nIdConfig );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * Get a new primary key from the database
     * @return The new primary key
     */
    private int getNewPrimaryKey( )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PRIMARY_KEY, getPlugin( ) );
        int nResult = 1;
        daoUtil.executeQuery( );
        if ( daoUtil.next( ) )
        {
            nResult = daoUtil.getInt( 1 ) + 1;
        }
        daoUtil.free( );
        return nResult;
    }

    /**
     * Get the import export plugin
     * @return The import export plugin
     */
    private Plugin getPlugin( )
    {
        if ( _plugin == null )
        {
            _plugin = ImportExportPlugin.getPlugin( );
        }
        return _plugin;
    }

}
