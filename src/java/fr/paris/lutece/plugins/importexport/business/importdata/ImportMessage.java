package fr.paris.lutece.plugins.importexport.business.importdata;

/**
 * Describe a message of imported elements
 */
public class ImportMessage
{
    /**
     * Status that indicates that the item was skipped
     */
    public static final int STATUS_SKIPPED = 0;
    /**
     * Status that indicates that an error occurred during the importation of
     * the item
     */
    public static final int STATUS_ERROR = 1;
    /**
     * Status that indicates that the item was successfully imported
     */
    public static final int STATUS_OK = 10;

    private String _strMessage;
    private int _nStatus;

    /**
     * Creates a new import message
     * @param strMessage The message
     * @param nStatus The {@link #setStatus(int) status} of the message
     */
    public ImportMessage( String strMessage, int nStatus )
    {
        this._strMessage = strMessage;
        this._nStatus = nStatus;
    }

    /**
     * Get the message
     * @return the message
     */
    public String getMessage( )
    {
        return _strMessage;
    }

    /**
     * Set the message
     * @param strMessage the message
     */
    public void setMessage( String strMessage )
    {
        this._strMessage = strMessage;
    }

    /**
     * Get the status of the importation of the item described by this
     * {@link ImportMessage}. Status are :
     * <ul>
     * <li>{@link #STATUS_SKIPPED} if the item was skipped</li>
     * <li>{@link #STATUS_ERROR} if an error occurred during the importation of
     * the item</li>
     * <li>{@link #STATUS_OK} if the item was successfully imported</li>
     * </ul>
     * @return The status
     */
    public int getStatus( )
    {
        return _nStatus;
    }

    /**
     * Set the status of importation of the item described by this
     * {@link ImportMessage}.
     * @param nStatus <ul>
     *            <li>{@link #STATUS_SKIPPED} if the item was skipped</li>
     *            <li>{@link #STATUS_ERROR} if an error occurred during the
     *            importation of the item</li>
     *            <li>{@link #STATUS_OK} if the item was successfully imported</li>
     *            </ul>
     */
    public void setStatus( int nStatus )
    {
        this._nStatus = nStatus;
    }
}
