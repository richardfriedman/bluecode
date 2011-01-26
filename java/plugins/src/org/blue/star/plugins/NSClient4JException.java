package org.blue.star.plugins;

/**
 * <p>Title: NSClient4JException</p>
 * <p>Description: Custom Exception for NSClient4j</p>
 * <p>@author Nicholas Whitehead (nwhitehe@yahoo.com)</p>
 * @version $Revision: 1.1 $
 * Date $Date: 2007/04/17 19:14:23 $
 * @todo This class needs fleshing out a bit. More custom information about the failure can be made available.
 */
public class NSClient4JException extends Exception {
    public NSClient4JException() {
        super();
    }

    public NSClient4JException(String message) {
        super(message);
    }

    public NSClient4JException(String message, Throwable cause) {
        super(message, cause);
    }

    public NSClient4JException(Throwable cause) {
        super(cause);
    }
}
