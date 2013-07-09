package fr.paris.lutece.plugins.importexport.business.importdata;

/**
 * Describes a value of a row to import
 */
public class ImportElement
{
    private String _strColumnName;
    private String _strValue;

    /**
     * Get the name of the column of the database to insert the value of this
     * element in
     * @return the name of the column of the database
     */
    public String getColumnName( )
    {
        return _strColumnName;
    }

    /**
     * Set the name of the column of the database to insert the value of this
     * element in
     * @param strColumnName the name of the column of the database
     */
    public void setColumnName( String strColumnName )
    {
        this._strColumnName = strColumnName;
    }

    /**
     * Get the value of this element
     * @return the value of this element
     */
    public String getValue( )
    {
        return _strValue;
    }

    /**
     * Set the value of this element
     * @param strValue the value of this element
     */
    public void setValue( String strValue )
    {
        this._strValue = strValue;
    }

}
