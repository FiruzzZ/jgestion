package gui.generics;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author FiruzzZ
 */
public class SearcherJDialog extends JDialog {

    private static final long serialVersionUID = 2L;
    protected JPanel filtrosPanel;
    private JProgressBar searchingBar;
    private JPanel topPanel;
    private JButton btnNuevo;
    private JButton btnBorrar;
    private JButton btnEditar;
    private JScrollPane jScrollPane;
    private JTable table;
    private static final boolean customTextPosition = false;
    private static final int HORIZONTAL_TEXT_POSITION = SwingUtilities.CENTER;
    private static final int VERTICAL_TEXT_POSITION = SwingUtilities.BOTTOM;
    private static final String RESOURCE_FOLDER_PATH = "/iconos/";
    private Color topButtonForegroundColor = null;//Color.BLACK;
    private Color topButtonBackgroundColor = null;//Color.BLACK;

    public SearcherJDialog(Frame owner, String title, boolean modal, JPanel panel) {
        super(owner, title, modal);
        if (panel == null) {
            throw new IllegalArgumentException("No te olvides del PANEL capooooooo!!");
        }
        this.filtrosPanel = panel;
        initComponents();
    }

    public SearcherJDialog(JDialog owner, String title, boolean modal, JPanel panel) {
        super(owner, title, modal);
        if (panel == null) {
            throw new IllegalArgumentException("No te olvides del PANEL capooooooo!!");
        }
        this.filtrosPanel = panel;
        initComponents();
    }

    private void initComponents() {
        table = new JTable(new Object[0][0], new Object[]{"asdfasdf", "asdfsassssssss1", "safd    afawe "});
        jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(table);
        Insets insets = new Insets(2, 1, 2, 1);
        btnNuevo = new JButton();
        btnBorrar = new JButton();
        btnEditar = new JButton();

        btnNuevo.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "editar.png")));
        btnNuevo.setForeground(topButtonForegroundColor);
        btnNuevo.setBackground(topButtonBackgroundColor);
        btnNuevo.setMnemonic('n');
        btnNuevo.setText("Nuevo");
        btnNuevo.setFocusable(false);
        btnNuevo.setMargin(insets);

        btnBorrar.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "editar.png")));
        btnBorrar.setForeground(topButtonForegroundColor);
        btnBorrar.setBackground(topButtonBackgroundColor);
        btnBorrar.setMnemonic('e');
        btnBorrar.setText("Eliminar");
        btnBorrar.setFocusable(false);
        btnBorrar.setMargin(insets);

        btnEditar.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "editar.png")));
        btnEditar.setForeground(topButtonForegroundColor);
        btnEditar.setBackground(topButtonBackgroundColor);
        btnEditar.setMnemonic('m');
        btnEditar.setText("Modificar");
        btnEditar.setFocusable(false);
        btnEditar.setMargin(insets);


        JPanel rightButtonsPanel = new JPanel();
//        rightButtonsPanel.setBackground(Color.BLACK);
        rightButtonsPanel.setLayout(new GridLayout(3, 1));
        rightButtonsPanel.add(btnNuevo);
        rightButtonsPanel.add(btnBorrar);
        rightButtonsPanel.add(btnEditar);

        topPanel = new JPanel();
//        topPanel.setBackground(Color.BLACK);
        GroupLayout layout = new GroupLayout(topPanel);
        //We specify automatic gap insertion:
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        topPanel.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup().
                addComponent(filtrosPanel).
                addComponent(rightButtonsPanel));
        layout.setVerticalGroup(
                layout.createSequentialGroup().
                addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
                addComponent(filtrosPanel).
                addComponent(rightButtonsPanel)));
        topPanel.add(filtrosPanel);
        topPanel.add(rightButtonsPanel);

        searchingBar = new JProgressBar();
        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.LINE_AXIS));
//        bottomPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 10));
        bottomPane.add(Box.createHorizontalGlue());
        bottomPane.add(searchingBar);
//        bottomButtonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        getContentPane().add(topPanel, BorderLayout.PAGE_START);
        getContentPane().add(jScrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPane, BorderLayout.PAGE_END);
        pack();
    }

    public void showMessage(String msj, String title, int messageType) {
        JOptionPane.showMessageDialog(this, getHtmlText(msj), title, messageType);
    }

    private String getHtmlText(String messageText1) {
        String insentiveCaseRegEx = "(?ui)";
        if (messageText1 != null) {
            String htmlFormatted;
            htmlFormatted = messageText1.replaceAll(insentiveCaseRegEx + "<html>", "").replaceAll(insentiveCaseRegEx + "</html>", "");
            htmlFormatted = htmlFormatted.replaceAll(insentiveCaseRegEx + "\\n", "<br>");
            return "<HTML>" + htmlFormatted + "</HTML>";
        }
        return null;
    }

    public static void main(String[] args) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //</editor-fold>
        SearcherJDialog jd = new SearcherJDialog((JDialog) null, null, true, null);
        jd.setVisible(true);
        System.exit(0);
    }
}
