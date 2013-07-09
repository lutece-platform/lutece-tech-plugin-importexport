package fr.paris.lutece.plugins.importexport.business.importdata;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.sql.SQLException;
import java.util.List;


/**
 * Interface of DAO of import elements
 */
public interface IImportElementDAO
{
    void insertElement( List<ImportElement> listElements, String strTableName, Plugin plugin ) throws SQLException;

    void updateElement( List<ImportElement> listElements, String strTableName, Plugin plugin );
}
