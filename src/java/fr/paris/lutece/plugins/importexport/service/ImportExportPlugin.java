package fr.paris.lutece.plugins.importexport.service;

import fr.paris.lutece.plugins.importexport.service.importdata.ImportManager;
import fr.paris.lutece.plugins.importexport.service.importdata.csvimportsource.CSVImportSourceFactory;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginDefaultImplementation;
import fr.paris.lutece.portal.service.plugin.PluginService;


/**
 * ImportExportPlugin
 */
public class ImportExportPlugin extends PluginDefaultImplementation
{
    public static final String PLUGIN_NAME = "importexport";

    /**
     * Get the ImportExport plugin
     * @return The ImportExport Plugin
     */
    public static Plugin getPlugin( )
    {
        return PluginService.getPlugin( PLUGIN_NAME );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init( )
    {
        // We register the CSV import source factory to the import manager
        CSVImportSourceFactory csvImportSourceFactory = new CSVImportSourceFactory( );
        ImportManager.registerImportSourceFactory( csvImportSourceFactory );
    }
}
