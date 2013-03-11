package generics;

import java.awt.*;
import java.io.Serializable;
import javax.swing.*;

/**
 *
 * @author FiruzzZ
 */
public class WaitingDialog extends JDialog implements Serializable, Runnable {

    private static final long serialVersionUID = 1L;
    private String message;
    private JLabel labelMessage;
    private Icon iconMessage;

    public WaitingDialog(Window owner, String title, boolean modal, String message, Icon iconMessage) {
        super(owner, title, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        this.message = message;
        this.iconMessage = iconMessage;
        initPrintingReportDialog();
    }

    public WaitingDialog(Window owner, String title, boolean modal, String message) {
        this(owner, title, modal, message, null);
    }

    private void initPrintingReportDialog() {
        initMessageLabel();
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setComponentOrientation(this.getComponentOrientation());
        Container contentPane = this.getContentPane();
//        BorderLayout b = new BorderLayout();
//        contentPane.setLayout(b);
//        contentPane.add(getMessageLabel(), BorderLayout.CENTER);
        GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(labelMessage)));
//        layout.setHorizontalGroup(
//                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap()
//                .addComponent(labelMessage).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                .addComponent(labelMessage) //                .addContainerGap(100, Short.MAX_VALUE)
                ));
//        contentPane.add(labelMessage);
        this.pack();
        this.setResizable(false);
        this.setAlwaysOnTop(true);
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        Dimension frameSize = this.getSize();
//        this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        this.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint());
    }

    private Component initMessageLabel() {
        labelMessage = new JLabel();
        labelMessage.setFont(new Font("Tahoma", 0, 14)); // NOI18N
        labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
        labelMessage.setIcon(iconMessage); // NOI18N
        labelMessage.setText(message);
        return labelMessage;
    }

    @Override
    public void run() {
        this.setVisible(true);
    }

    public JLabel getLabelMessage() {
        return labelMessage;
    }

    public static void main(String[] args) throws Exception {
//        String[] x = {"sadfsadfdasffdsf", "3242142134231233422323321334324223", "ssssssssssssssss.....", "fsadfasdfasdfasdfasdfwefea!!!!!!"};
//        WaitingDialog jd = new WaitingDialog(null, "Tiiiitle....!!!", true, "<html>Cuando yo era chiquitito.. vi una mina andando en bicicletass.. <br>como no tenía manos manejaba con las teee..rminó<html>");
//        new Thread(jd).start();
//        for (int i = 0; i < 4; i++) {
//            String string = x[i];
//            jd.getLabelMessage().setText(string);
//            Thread.sleep(3000);
//        }
//        System.exit(0);
    }
}
