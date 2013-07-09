package fr.paris.lutece.plugins.importexport.service.importdata.csvimportsource;

import fr.paris.lutece.plugins.importexport.service.importdata.IImportSource;
import fr.paris.lutece.plugins.importexport.service.importdata.IImportSourceFactory;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;


/**
 * Factory for CSV import sources
 */
public class CSVImportSourceFactory implements IImportSourceFactory
{
    private static final String CONSTANT_CSV_EXTENTION = "csv";

    private static final String PROPERTY_CSV_SEPARATOR = "importexport.importdata.csv.separator";
    private static final String PROPERTY_CSV_QUOTE = "importexport.importdata.csv.quote";

    private String _strSeparator = AppPropertiesService.getProperty( PROPERTY_CSV_SEPARATOR );
    private String _strQuote = AppPropertiesService.getProperty( PROPERTY_CSV_QUOTE );

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidImportSource( String strFileExtension )
    {
        return StringUtils.equalsIgnoreCase( CONSTANT_CSV_EXTENTION, strFileExtension );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IImportSource getImportSource( FileItem fileItem )
    {
        return new CSVImportSource( fileItem, _strSeparator, _strQuote );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IImportSource getImportSource( File file )
    {
        return new CSVImportSource( file, _strSeparator, _strQuote );
    }
}
