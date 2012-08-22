package controller.exceptions;

import java.awt.Window;
import javax.swing.JOptionPane;

/**
 * User-level error message
 *
 * @author FiruzzZ
 */
public class MessageException extends Exception {

    public MessageException(String message) {
        super(message);
    }

    /**
     * Levanta un
     * {@link JOptionPane#showMessageDialog( owner, getMessage(), null, WARNING_MESSAGE)}
     * con el mensaje de la exception.
     *
     * @param owner
     */
    public void displayMessage(Window owner) {
        JOptionPane.showMessageDialog(owner, getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
    }
}
