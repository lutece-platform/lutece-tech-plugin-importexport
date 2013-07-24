package fr.paris.lutece.plugins.importexport.business.export;

import fr.paris.lutece.plugins.importexport.business.ImportExportElement;

import java.util.List;


/**
 * This class represents data to export
 */
public class RowExportData
{
    private List<ImportExportElement> _listExportElements;

    /**
     * Creates a new RowExportData
     * @param listExportElements The list of elements of this row
     */
    public RowExportData( List<ImportExportElement> listExportElements )
    {
        this._listExportElements = listExportElements;
    }

    /**
     * Get the list of import elements
     * @return The list of import elements
     */
    public List<ImportExportElement> getListExportElements( )
    {
        return _listExportElements;
    }

    /**
     * Set the list of import elements
     * @param listExportElements The list of import elements
     */
    public void setListExportElements( List<ImportExportElement> listExportElements )
    {
        this._listExportElements = listExportElements;
    }
}
