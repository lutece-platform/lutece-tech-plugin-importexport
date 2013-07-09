package fr.paris.lutece.plugins.importexport.business.importdata;

/**
 * Describes the result of an import
 */
public class ImportResult
{
    int _nCreatedElements;
    int _nUpdatedElements;
    int _nIgnoredElements;

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
     */
    public ImportResult( int nCreatedElements, int nUpdatedElements, int nIgnoredElements )
    {
        setCreatedElements( nCreatedElements );
        setUpdatedElements( nUpdatedElements );
        setIgnoredElements( nIgnoredElements );
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

}
