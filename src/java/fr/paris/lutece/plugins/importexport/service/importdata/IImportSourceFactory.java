package fr.paris.lutece.plugins.importexport.service.importdata;

import java.io.File;

import org.apache.commons.fileupload.FileItem;


/**
 * Interface for import source factories.
 */
public interface IImportSourceFactory
{
    /**
     * Check if import sources created by this factory can be used for the given
     * file extension
     * @param strFileExtension The extension of the file to use
     * @return True if import sources are compatible with the given file
     *         extension, false otherwise
     */
    boolean isValidImportSource( String strFileExtension );

    /**
     * Get an import source from a file item
     * @param fileItem The file item to import data from
     * @return The import source associated with the given file item
     */
    IImportSource getImportSource( FileItem fileItem );

    /**
     * Get an import source from a file
     * @param file The file to import data from
     * @return The import source associated with the given file
     * 
     */
    IImportSource getImportSource( File file );
}
