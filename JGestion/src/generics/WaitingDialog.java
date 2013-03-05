package generics;

import java.awt.*;
import java.io.Serializable;
import javax.swing.*;

/**
 *
 * @author FiruzzZ
 */
public class WaitingDialog extends JDialog implements Serializable {

    private static final long serialVersionUID = 1L;
    private String message;
    private JLabel labelMessage;

    public WaitingDialog(Window owner, String title, boolean modal, String message) {
        super(owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        this.message = message;
        initPrintingReportDialog();
    }

    private void initPrintingReportDialog() {
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//        this.setUndecorated(true);
//        this.setSize(400, 75);
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        labelMessage = new JLabel();
//        labelMessage.setForeground(Color.WHITE);
        labelMessage.setFont(new Font("Tahoma", 0, 14)); // NOI18N
        labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
        labelMessage.setIcon(new ImageIcon(getClass().getResource("/iconos/impresora.png"))); // NOI18N
        labelMessage.setText(message);
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap()
                .addComponent(labelMessage).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()//.addGap(19, 19, 19)
                .addComponent(labelMessage).addContainerGap(100, Short.MAX_VALUE)));
        
        this.pack();
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        Dimension frameSize = this.getSize();
//        this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        this.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint());
    }

    public JLabel getLabelMessage() {
        return labelMessage;
    }
}
