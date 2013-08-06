package fr.paris.lutece.plugins.importexport.business.export;

import java.util.List;


/**
 * Interface for the DAO of automatic export configuration
 */
public interface IAutomaticExportConfigDAO
{
    /**
     * Get a configuration from its id
     * @param nId The id of the configuration
     * @return The configuration, or null if no configuration has the given id
     */
    AutomaticExportConfig findById( int nId );

    /**
     * Get the list of every configuration
     * @param bLoadColumns True to load columns of configurations, false to
     *            ignore them
     * @return The list of every configuration. If there is no configuration,
     *         then an empty list is returned
     */
    List<AutomaticExportConfig> findAll( boolean bLoadColumns );

    /**
     * Insert a new configuration into the database.
     * @param config The configuration to update
     */
    void insert( AutomaticExportConfig config );

    /**
     * Update a configuration. Both the configuration and the list of columns
     * are update
     * @param config The configuration to update
     */
    void update( AutomaticExportConfig config );

    /**
     * Remove a configuration from the database by its id
     * @param nId The id of the configuration to remove
     */
    void delete( int nId );

}
