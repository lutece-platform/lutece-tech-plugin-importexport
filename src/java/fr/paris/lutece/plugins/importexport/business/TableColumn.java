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
