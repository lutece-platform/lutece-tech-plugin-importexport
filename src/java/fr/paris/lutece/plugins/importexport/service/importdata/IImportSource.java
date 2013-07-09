package fr.paris.lutece.plugins.importexport.service.importdata;

import fr.paris.lutece.plugins.importexport.business.importdata.ImportElement;

import java.util.List;


/**
 * Interface for data sources of imports
 */
public interface IImportSource
{
    /**
     * Get the next set of values, or null if this data source has no more
     * values.
     * @return The list of values, or null if there is no more or if an error
     *         occurs while reading values in the data source
     */
    List<ImportElement> getNextValues( );

    /**
     * Get the name of columns of this data source
     * @return The name of columns of this data source, or null if an error
     *         occurs while reading values in the data source
     */
    List<String> getColumnsName( );

    /**
     * Close the data source
     */
    void close( );
}
