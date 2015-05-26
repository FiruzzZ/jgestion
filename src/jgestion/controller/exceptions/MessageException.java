package jgestion.controller.exceptions;

import java.awt.EventQueue;
import java.awt.Window;
import javax.swing.JOptionPane;

/**
 * User-level (End user) error message
 *
 * @author FiruzzZ
 */
public class MessageException extends Exception {

    private final String title;

    public MessageException(String title, String message) {
        super(message);
        this.title = title;
    }

    public MessageException(String message) {
        super(message);
        title = "Error";
    }

    /**
     * Levanta un {@link JOptionPane#showMessageDialog( owner, getMessage(), null, WARNING_MESSAGE)}
     * con el mensaje de la exception.
     *
     * @param owner
     */
    public void displayMessage(Window owner) {
        if (!EventQueue.isDispatchThread()) {
            /**
             * Para que sea compatible con el dialogo de espera WaitingDialog.. Finaliza Waiting y
             * aparece este
             */
            EventQueue.invokeLater(()
                    -> JOptionPane.showMessageDialog(owner, getMessage(), title, JOptionPane.WARNING_MESSAGE));
        } else {
            JOptionPane.showMessageDialog(owner, getMessage(), title, JOptionPane.WARNING_MESSAGE);

        }
    }
}
