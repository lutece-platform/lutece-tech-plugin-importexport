package fr.paris.lutece.plugins.importexport.business.importdata;

import fr.paris.lutece.plugins.importexport.business.AbstractImportExportDAO;
import fr.paris.lutece.plugins.importexport.business.ColumnType;
import fr.paris.lutece.plugins.importexport.business.ImportExportElement;
import fr.paris.lutece.plugins.importexport.business.TableColumn;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.sql.DAOUtil;
import fr.paris.lutece.util.sql.Transaction;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;


/**
 * DAO to import elements from the database<br />
 * <b>Warning, this DAO is state full !</b>
 */
public class ImportDataDAO extends AbstractImportExportDAO
{
    private static final String SQL_QUERY_SELECT = " SELECT ";
    private static final String SQL_QUERY_FROM = " FROM ";
    private static final String SQL_QUERY_INSERT_INTO = " INSERT INTO ";
    private static final String SQL_QUERY_VALUES = " VALUES ";
    private static final String SQL_QUERY_UPDATE = " UPDATE ";
    private static final String SQL_QUERY_UPDATE_SET = " SET ";
    private static final String SQL_QUERY_EQUALS = " = ? ";
    private static final String SQL_QUERY_WHERE = " WHERE ";
    private static final String SQL_QUERY_AND = " AND ";

    private static final String CONSTANT_QUESTION_MARK = "?";
    private static final String CONSTANT_COMA = ",";
    private static final String CONSTANT_OPEN_PARENTHESIS = " ( ";
    private static final String CONSTANT_CLOSE_PARENTHESIS = " ) ";
    private static final String CONSTANT_SEMICOLON = ";";
    private static final String CONSTANT_STRING_NULL = "null";
    private static final String CONSTANT_HEXA_START = "0x";

    private static final String ERROR_EMPTY_TABLE_NAME = "importexport.import_data.errors.emptyTableName";
    private static final String ERROR_EMPTY_COLUMN_LIST = "importexport.import_data.errors.emptyColumnList";
    private static final String ERROR_MESSAGE_TRANSACTION_CLOSED = "importexport.import_data.errors.transactionClosed";
    private static final String ERROR_MESSAGE_WRONG_LIST_ELEMENTS_SIZE = "importexport.import_data.errors.emptyColumnList";

    private String _strSqlInsert;
    private String _strSqlUpdate;
    private String _sqlCheckElement;
    private List<TableColumn> _listTableColumns;
    private String _strTableName;
    private Transaction _transaction;
    private Plugin _plugin;
    private Locale _locale;

    /**
     * Creates a new DAO to import elements. When the DAO is created, a database
     * connection is opened. Therefore, either methods
     * {@link #commitTransaction} or {@link #rollbackTransaction} <b>must</b> be
     * called to close the connection
     * @param listTableColumns The list of columns of the table to import
     * @param strTableName The name of the table to import data in
     * @param plugin The plugin to get the pool from
     * @param locale The locale to display errors in
     * @throws AppException If an error occurs during the initialization of this
     *             DAO
     */
    public ImportDataDAO( List<String> listTableColumns, String strTableName, Plugin plugin, Locale locale )
            throws AppException
    {
        if ( StringUtils.isEmpty( strTableName ) )
        {
            throw new AppException( I18nService.getLocalizedString( ERROR_EMPTY_TABLE_NAME, locale ) );
        }
        if ( listTableColumns == null || listTableColumns.size( ) == 0 )
        {
            throw new AppException( I18nService.getLocalizedString( ERROR_EMPTY_COLUMN_LIST, locale ) );
        }

        // We create the list of columns of the required table
        this._strTableName = strTableName;
        this._plugin = plugin;
        this._locale = locale;
        this._listTableColumns = getTableColumns( listTableColumns, strTableName, plugin, locale );
        _transaction = new Transaction( );
        if ( _transaction.getStatus( ) != Transaction.OPENED )
        {
            throw new AppException( I18nService.getLocalizedString( ERROR_MESSAGE_TRANSACTION_CLOSED, _locale ) );
        }
    }

    /**
     * Insert an element to the database. The transaction is NOT committed by
     * this method.
     * @param listElements The list of elements to add in the statement. The
     *            number of elements and the name of columns must be the same as
     *            the columns associated with this DAO. <br />
     *            Their order must also be the same.
     * @throws AppException If an error occurred during the insertion
     * @throws SQLException If an error occurred with the database
     */
    public void insertElement( List<ImportExportElement> listElements ) throws AppException, SQLException
    {
        if ( _transaction == null || _transaction.getStatus( ) != Transaction.OPENED )
        {
            throw new AppException( I18nService.getLocalizedString( ERROR_MESSAGE_TRANSACTION_CLOSED, _locale ) );
        }
        if ( listElements == null || listElements.size( ) != _listTableColumns.size( ) )
        {
            AppException appException = new AppException( I18nService.getLocalizedString(
                    ERROR_MESSAGE_WRONG_LIST_ELEMENTS_SIZE, _locale ) );
            int nElemNumber = 0;
            if ( listElements != null )
            {
                nElemNumber = listElements.size( );
            }
            AppLogService.info( appException.getMessage( ) + " expected " + _listTableColumns.size( )
                    + " elements, found " + nElemNumber );
            throw appException;
        }
        _transaction.prepareStatement( getSqlInsert( ) );
        int nIndex = 1;
        Iterator<TableColumn> columnIterator = _listTableColumns.iterator( );
        for ( ImportExportElement element : listElements )
        {
            TableColumn tableColumn = columnIterator.next( );
            // If the two lists are not synchronized, we throw an exception to skip this item
            if ( !StringUtils.equalsIgnoreCase( tableColumn.getColumnName( ), element.getColumnName( ) ) )
            {
                throw new AppException(
                        I18nService.getLocalizedString( ERROR_MESSAGE_WRONG_LIST_ELEMENTS_SIZE, _locale ) );
            }
            addSqlParameter( nIndex++, element.getValue( ), tableColumn.getColumnType( ) );
        }
        _transaction.executeStatement( );
    }

    /**
     * Update an element of the database. The transaction is NOT committed by
     * this method.
     * @param listElements The list of elements to add in the statement. The
     *            number of elements and the name of columns must be the same as
     *            the columns associated with this DAO. <br />
     *            Their order must also be the same.
     * @throws AppException If an error occurred during the update
     * @throws SQLException If an error occurred with the database
     */
    public void updateElement( List<ImportExportElement> listElements ) throws AppException, SQLException
    {
        if ( _transaction == null || _transaction.getStatus( ) != Transaction.OPENED )
        {
            throw new AppException( I18nService.getLocalizedString( ERROR_MESSAGE_TRANSACTION_CLOSED, _locale ) );
        }
        if ( listElements == null || listElements.size( ) != _listTableColumns.size( ) )
        {
            AppException appException = new AppException( I18nService.getLocalizedString(
                    ERROR_MESSAGE_WRONG_LIST_ELEMENTS_SIZE, _locale ) );
            int nElemNumber = 0;
            if ( listElements != null )
            {
                nElemNumber = listElements.size( );
            }
            AppLogService.info( appException.getMessage( ) + " expected " + _listTableColumns.size( )
                    + " elements, found " + nElemNumber );
            throw appException;
        }
        _transaction.prepareStatement( getSqlUpdate( ) );
        int nIndex = 1;
        Iterator<TableColumn> columnIterator = _listTableColumns.iterator( );
        for ( ImportExportElement element : listElements )
        {
            TableColumn tableColumn = columnIterator.next( );
            // We skip primary keys that must be added at the end
            if ( !tableColumn.getIsPrimaryKey( ) )
            {
                // If the two lists are not synchronized, we throw an exception to skip this item
                if ( !StringUtils.equalsIgnoreCase( tableColumn.getColumnName( ), element.getColumnName( ) ) )
                {
                    throw new AppException( I18nService.getLocalizedString( ERROR_MESSAGE_WRONG_LIST_ELEMENTS_SIZE,
                            _locale ) );
                }
                addSqlParameter( nIndex++, element.getValue( ), tableColumn.getColumnType( ) );
            }
        }
        // We now add primary keys
        columnIterator = _listTableColumns.iterator( );
        for ( ImportExportElement element : listElements )
        {
            TableColumn tableColumn = columnIterator.next( );
            if ( tableColumn.getIsPrimaryKey( ) )
            {
                if ( !StringUtils.equalsIgnoreCase( tableColumn.getColumnName( ), element.getColumnName( ) ) )
                {
                    throw new AppException( I18nService.getLocalizedString( ERROR_MESSAGE_WRONG_LIST_ELEMENTS_SIZE,
                            _locale ) );
                }
                addSqlParameter( nIndex++, element.getValue( ), tableColumn.getColumnType( ) );
            }
        }
        _transaction.executeStatement( );
    }

    /**
     * Check if a row already exists in the database
     * @param listElements The list of elements to check the existence of
     * @return True if the element exists, false otherwise
     * @throws SQLException If an error occur while checking the existence of
     *             the element
     */
    public boolean checkElementExists( List<ImportExportElement> listElements ) throws SQLException
    {
        if ( listElements == null || listElements.size( ) != _listTableColumns.size( ) )
        {
            AppException appException = new AppException( I18nService.getLocalizedString(
                    ERROR_MESSAGE_WRONG_LIST_ELEMENTS_SIZE, _locale ) );
            int nElemNumber = 0;
            if ( listElements != null )
            {
                nElemNumber = listElements.size( );
            }
            AppLogService.info( appException.getMessage( ) + " expected " + _listTableColumns.size( )
                    + " elements, found " + nElemNumber );
            throw appException;
        }
        DAOUtil daoUtil = new DAOUtil( getSqlCheckElementExists( ), _plugin );
        boolean bResult = false;
        try
        {
            String strPrimaryKey = listElements.get( 0 ).getValue( );
            addSqlParameter( 1, strPrimaryKey, _listTableColumns.get( 0 ).getColumnType( ), daoUtil );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                bResult = true;
            }
        }
        catch ( SQLException e )
        {
            AppLogService.error( e.getMessage( ), e );
        }
        finally
        {
            daoUtil.free( );
        }

        return bResult;
    }

    /**
     * Commit the transaction to the database, and close the connection.
     */
    public void commitTransaction( )
    {
        if ( _transaction != null )
        {
            _transaction.commit( );
            _transaction = null;
        }
        else
        {
            SQLException sqlException = new SQLException( I18nService.getLocalizedString(
                    ERROR_MESSAGE_TRANSACTION_CLOSED, _locale ) );
            AppLogService.error( sqlException.getMessage( ), sqlException );
        }
    }

    /**
     * Roll back the transaction to the database, and close the connection.
     */
    public void rollbackTransaction( )
    {
        if ( _transaction != null )
        {
            _transaction.rollback( );
            _transaction = null;
        }
        else
        {
            SQLException sqlException = new SQLException( I18nService.getLocalizedString(
                    ERROR_MESSAGE_TRANSACTION_CLOSED, _locale ) );
            AppLogService.error( sqlException.getMessage( ), sqlException );
        }
    }

    /**
     * Add a parameter to the current statement of the transaction according to
     * its type.
     * @param nIndex The index of the parameter to add
     * @param strElementValue The value of the parameter
     * @param columnType The type of the column
     * @throws AppException if the value of the parameter is not valid
     * @throws SQLException If an error occurred with the database
     */
    private void addSqlParameter( int nIndex, String strElementValue, ColumnType columnType ) throws SQLException,
            AppException
    {
        try
        {
            switch ( columnType )
            {
            case TYPE_INT:
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    _transaction.getStatement( ).setNull( nIndex, Types.INTEGER );
                }
                else
                {
                    _transaction.getStatement( ).setInt( nIndex, Integer.parseInt( strElementValue ) );
                }
                break;
            case TYPE_LONG:
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    _transaction.getStatement( ).setNull( nIndex, Types.BIGINT );
                }
                else
                {
                    _transaction.getStatement( ).setLong( nIndex, Long.parseLong( strElementValue ) );
                }
                break;
            case TYPE_DOUBLE:
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    _transaction.getStatement( ).setNull( nIndex, Types.DOUBLE );
                }
                else
                {
                    _transaction.getStatement( ).setDouble( nIndex, Double.parseDouble( strElementValue ) );
                }
                break;
            case TYPE_STRING:
                _transaction.getStatement( ).setString( nIndex, strElementValue );
                break;
            case TYPE_TIMESTAMP:
                Timestamp timestamp;
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    timestamp = null;
                }
                else
                {
                    // If the timestamp value is numeric
                    if ( StringUtils.isNumeric( strElementValue ) )
                    {
                        timestamp = new Timestamp( Long.parseLong( strElementValue ) );
                    }
                    else
                    {
                        // If the timestamp value is literal
                        try
                        {
                            timestamp = Timestamp.valueOf( strElementValue );
                        }
                        catch ( IllegalArgumentException e )
                        {
                            throw new SQLException( e );
                        }
                    }
                }
                _transaction.getStatement( ).setTimestamp( nIndex, timestamp );
                break;
            case TYPE_DATE:
                Date date;
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    date = null;
                }
                else
                {
                    try
                    {
                        date = new Date( DateFormat.getDateInstance( ).parse( strElementValue ).getTime( ) );
                    }
                    catch ( ParseException e )
                    {
                        throw new SQLException( e );
                    }
                }
                _transaction.getStatement( ).setDate( nIndex, date );
                break;
            case TYPE_BYTE:
                byte[] blobItem;
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    blobItem = null;
                }
                else
                {
                    String strValue = strElementValue;
                    // If the blob contains the sequence "0x" that indicates that it is encoded in hex, we remove it since we do know it is hex
                    // Furthermore, the 'x' character is not a valid hex character, so we can safely remove it
                    if ( strValue.startsWith( CONSTANT_HEXA_START ) )
                    {
                        strValue = strValue.substring( CONSTANT_HEXA_START.length( ) );
                    }
                    try
                    {
                        blobItem = Hex.decodeHex( strValue.toCharArray( ) );
                    }
                    catch ( DecoderException e )
                    {
                        throw new SQLException( e );
                    }
                }
                _transaction.getStatement( ).setBytes( nIndex, blobItem );
                break;
            default:
                AppLogService.error( "Unknown column type : " + columnType );
            }
        }
        catch ( SQLException e )
        {
            AppLogService.error( e.getMessage( ), e );
            throw e;
        }
    }

    /**
     * Add a parameter to a DAOUtil according to its type.
     * @param nIndex The index of the parameter to add
     * @param strElementValue The value of the parameter
     * @param columnType The type of the column
     * @param daoUtil The DAOUtil to add the parameter to
     * @throws AppException if the value of the parameter is not valid
     * @throws SQLException If an error occurred with the database
     */
    private void addSqlParameter( int nIndex, String strElementValue, ColumnType columnType, DAOUtil daoUtil )
            throws AppException, SQLException
    {
        try
        {
            switch ( columnType )
            {
            case TYPE_INT:
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    daoUtil.setIntNull( nIndex );
                }
                else
                {
                    daoUtil.setInt( nIndex, Integer.parseInt( strElementValue ) );
                }
                break;
            case TYPE_LONG:
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    daoUtil.setLongNull( nIndex );
                }
                else
                {
                    daoUtil.setLong( nIndex, Long.parseLong( strElementValue ) );
                }
                break;
            case TYPE_DOUBLE:
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    daoUtil.setDoubleNull( nIndex );
                }
                else
                {
                    daoUtil.setDouble( nIndex, Double.parseDouble( strElementValue ) );
                }
                break;
            case TYPE_STRING:
                daoUtil.setString( nIndex, strElementValue );
                break;
            case TYPE_TIMESTAMP:
                Timestamp timestamp;
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    timestamp = null;
                }
                else
                {
                    // If the timestamp value is numeric
                    if ( StringUtils.isNumeric( strElementValue ) )
                    {
                        timestamp = new Timestamp( Long.parseLong( strElementValue ) );
                    }
                    else
                    {
                        // If the timestamp value is literal
                        try
                        {
                            timestamp = Timestamp.valueOf( strElementValue );
                        }
                        catch ( IllegalArgumentException e )
                        {
                            throw new AppException( );
                        }
                    }
                }
                daoUtil.setTimestamp( nIndex, timestamp );
                break;
            case TYPE_DATE:
                Date date;
                if ( isStringBlankOrNull( strElementValue ) )
                {
                    date = null;
                }
                else
                {
                    try
                    {
                        date = new Date( DateFormat.getDateInstance( ).parse( strElementValue ).getTime( ) );
                    }
                    catch ( ParseException e )
                    {
                        throw new SQLException( e );
                    }
                }
                daoUtil.setDate( nIndex, date );
                break;
            case TYPE_BYTE:
                byte[] blobItem;
                try
                {
                    blobItem = Hex.decodeHex( strElementValue.toCharArray( ) );
                }
                catch ( DecoderException e )
                {
                    throw new SQLException( e );
                }
                daoUtil.setBytes( nIndex, blobItem );
                break;
            default:
                AppLogService.error( "Unknown column type : " + columnType );
            }
        }
        catch ( SQLException e )
        {
            AppLogService.error( e.getMessage( ), e );
            throw e;
        }
    }

    /**
     * Get the SQL script to insert an element of this DAO
     * @return The SQL script to insert an element of this DAO
     */
    private String getSqlInsert( )
    {
        if ( StringUtils.isNotBlank( _strSqlInsert ) )
        {
            return _strSqlInsert;
        }
        int nListSize = _listTableColumns.size( );
        StringBuilder sbSql = new StringBuilder( SQL_QUERY_INSERT_INTO );
        sbSql.append( _strTableName );
        sbSql.append( CONSTANT_OPEN_PARENTHESIS );
        sbSql.append( _listTableColumns.get( 0 ).getColumnName( ) );
        for ( int i = 1; i < nListSize; i++ )
        {
            sbSql.append( CONSTANT_COMA );
            sbSql.append( _listTableColumns.get( i ).getColumnName( ) );
        }
        sbSql.append( CONSTANT_CLOSE_PARENTHESIS );
        sbSql.append( SQL_QUERY_VALUES );
        sbSql.append( CONSTANT_OPEN_PARENTHESIS );
        sbSql.append( CONSTANT_QUESTION_MARK );
        for ( int i = 1; i < nListSize; i++ )
        {
            sbSql.append( CONSTANT_COMA );
            sbSql.append( CONSTANT_QUESTION_MARK );
        }
        sbSql.append( CONSTANT_CLOSE_PARENTHESIS );
        sbSql.append( CONSTANT_SEMICOLON );

        _strSqlInsert = sbSql.toString( );
        return _strSqlInsert;
    }

    /**
     * Get the SQL script to update an element of this DAO
     * @return The SQL script to update an element of this DAO
     */
    private String getSqlUpdate( )
    {
        if ( StringUtils.isNotBlank( _strSqlUpdate ) )
        {
            return _strSqlUpdate;
        }
        StringBuilder sbSql = new StringBuilder( SQL_QUERY_UPDATE );
        sbSql.append( _strTableName );
        sbSql.append( SQL_QUERY_UPDATE_SET );

        boolean bIsFirstColumn = true;
        for ( TableColumn tableColumn : _listTableColumns )
        {
            if ( !tableColumn.getIsPrimaryKey( ) )
            {
                if ( bIsFirstColumn )
                {
                    bIsFirstColumn = false;
                }
                else
                {
                    sbSql.append( CONSTANT_COMA );
                }
                sbSql.append( tableColumn.getColumnName( ) );
                sbSql.append( SQL_QUERY_EQUALS );
            }
        }
        sbSql.append( SQL_QUERY_WHERE );
        bIsFirstColumn = true;
        for ( TableColumn tableColumn : _listTableColumns )
        {
            if ( tableColumn.getIsPrimaryKey( ) )
            {
                if ( bIsFirstColumn )
                {
                    bIsFirstColumn = false;
                }
                else
                {
                    sbSql.append( SQL_QUERY_AND );
                }
                sbSql.append( tableColumn.getColumnName( ) );
                sbSql.append( SQL_QUERY_EQUALS );
            }
        }

        _strSqlUpdate = sbSql.toString( );
        return _strSqlUpdate;
    }

    /**
     * Get the SQL query to check if an item already exist in the table of the
     * database
     * @return The SQL query to execute
     */
    private String getSqlCheckElementExists( )
    {
        if ( StringUtils.isNotBlank( _sqlCheckElement ) )
        {
            return _sqlCheckElement;
        }
        StringBuilder sbSql = new StringBuilder( SQL_QUERY_SELECT );
        sbSql.append( _listTableColumns.get( 0 ).getColumnName( ) );
        sbSql.append( SQL_QUERY_FROM );
        sbSql.append( _strTableName );
        sbSql.append( SQL_QUERY_WHERE );
        sbSql.append( _listTableColumns.get( 0 ).getColumnName( ) );
        sbSql.append( SQL_QUERY_EQUALS );
        _sqlCheckElement = sbSql.toString( );
        return _sqlCheckElement;
    }

    /**
     * Check if a string is null, empty, blank or equals to the 'null' string.
     * @param strString The string to check
     * @return True if the string null, empty, blank or equals to the 'null'
     *         string, false otherwise
     */
    private boolean isStringBlankOrNull( String strString )
    {
        return StringUtils.isBlank( strString ) || StringUtils.equalsIgnoreCase( strString, CONSTANT_STRING_NULL );
    }

    /**
     * Finalize the DAO. If the transaction has not been closed, then it is
     * rolled backed and closed
     * @throws Throwable If an exception is thrown
     */
    @Override
    protected void finalize( ) throws Throwable
    {
        if ( _transaction != null )
        {
            _transaction.rollback( );
        }
        super.finalize( );
    }
}
