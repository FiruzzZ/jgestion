package generics;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.Serializable;
import javax.swing.*;
import org.apache.log4j.LogManager;

/**
 *
 * @author FiruzzZ
 */
public class WaitingDialog extends JDialog implements Serializable {

    private static final long serialVersionUID = 1L;
    private JLabel labelMessage;
    private Icon iconMessage;
    private String messageToKeep;

    public WaitingDialog(Window owner, String title, boolean modal, String message) {
        this(owner, title, modal, message, null);
    }

    public WaitingDialog(Window owner, String title, boolean modal, String message, Icon iconMessage) {
        super(owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        this.messageToKeep = message;
        this.iconMessage = iconMessage;
        initPrintingReportDialog();
        if (message != null) {
            appendMessage(message, true);
        }
    }

    private void initPrintingReportDialog() {
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//        this.setUndecorated(true);
//        this.setSize(400, 100);
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        labelMessage = new JLabel();
//        labelMessage.setForeground(Color.WHITE);
//        labelMessage.setFont(new Font("Tahoma", 0, 12)); // NOI18N
//        labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
        labelMessage.setIcon(iconMessage);
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelMessage)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelMessage)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setLocationRelativeTo(null);
    }

    public JLabel getLabelMessage() {
        return labelMessage;
    }

    /**
     * Agrega el mensaje al panel de waiting..
     *
     * @param message the string will be enclosed between HTML tags
     * @param keepIt if this message will be concatenated to the previous one, so the next message
     * will not override this.
     */
    public final void appendMessage(String message, boolean keepIt) {
        appendMessage(message, keepIt, false);
    }

    /**
     * Agrega el mensaje al panel de waiting..
     *
     * @param message the string will be enclosed between HTML tags
     * @param keepIt if this message will be concatenated to the previous one, so the next message
     * will not override this.
     * @param newLine <code>true</code> will concat at the beginning of <code>message</code> a new
     * line tag <b>&lt br></b>
     */
    public void appendMessage(String message, boolean keepIt, boolean newLine) {
        labelMessage.setText(getHtmlText(messageToKeep + (newLine ? "<BR>" : "") + message));
        if (keepIt) {
            keepCurrentMessage();
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                pack();
            }
        });
    }

    public String keepCurrentMessage() {
        messageToKeep = labelMessage.getText();
        return messageToKeep;
    }

    private String getHtmlText(String messageText1) {
        String insentiveCaseRegEx = "(?ui)";
        if (messageText1 != null) {
            String htmlFormatted;
            htmlFormatted = messageText1.replaceAll(insentiveCaseRegEx + "<html>", "").replaceAll(insentiveCaseRegEx + "</html>", "");
            htmlFormatted = htmlFormatted.replaceAll(insentiveCaseRegEx + "\\n", "<BR>");
            return "<HTML>" + htmlFormatted + "</HTML>";
        }
        return null;
    }

    public String getMessageToKeep() {
        return messageToKeep;
    }

    /**
     * Este metodo no permite ir appendando información al diálogo del progreso de la acción
     *
     * @param owner
     * @param title
     * @param message
     * @param run
     */
    public static final void initWaitingDialog(Window owner, String title, String message, Runnable run) {
        final WaitingDialog waitingDialog = new WaitingDialog(owner, title, true, message);
        waitingDialog.pack();
        waitingDialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                try {
                    Thread thread = new Thread(run);
                    thread.start();
                    thread.join();
                } catch (InterruptedException ex) {
                    LogManager.getLogger(WaitingDialog.class.getName()).error(ex.getMessage(), ex);
                } finally {
                    waitingDialog.dispose();
                }
            }
        });
        waitingDialog.setVisible(true);
    }

}
