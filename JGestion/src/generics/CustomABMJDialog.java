/*
 * CustomABMJDialog.java
 *
 * Created on 15/08/2009, 00:46:05
 */
package generics;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;

/**
 *
 * @author FiruzzZ
 */
public class CustomABMJDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JPanel panel;
    private JToolBar jToolBar;
    private JButton btnNuevo;
    private JButton btnBorrar;
    private JButton btnBuscar;
    private JButton btnEditar;
    private JButton btnAceptar;
    private JButton btnCancelar;
    private JPanel bottomButtonPane;
//    private JButton btnExtraBottom;
    private JPanel topPanel;
    private JLabel messageLabel;
    private static final boolean customTextPosition = false;
    public static final int HORIZONTAL_TEXT_POSITION = SwingUtilities.CENTER;
    public static final int VERTICAL_TEXT_POSITION = SwingUtilities.BOTTOM;
    private static final String RESOURCE_FOLDER_PATH = "/iconos/";
    private String messageToHTML;
    private Color topButtonForegroundColor = null;
    private Color topButtonBackgroundColor = null;

    public CustomABMJDialog(Window owner, JPanel panel, String title, boolean modal, String messageText) {
        super(owner, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        if (panel == null) {
            throw new IllegalArgumentException("y PANEL capooooo mafia????!!");
        }
        this.panel = panel;
        this.messageToHTML = messageText;
        initComponents();
        setLocationRelativeTo(owner);
    }

    public void setToolBarVisible(boolean visible) {
        jToolBar.setVisible(visible);
        this.pack();
    }

    public void setBottomButtonsVisible(boolean visible) {
        bottomButtonPane.setVisible(visible);
    }

    /**
     * Set a actionListener for the 4 buttons in the tool bar. Commonly used it
     * as: Add, Remove, Edit, Search.
     *
     * @param o
     */
    public void addToolBarButtonsListener(ActionListener o) {
        for (Component component : jToolBar.getComponents()) {
            if (component instanceof JButton) {
                JButton b = (JButton) component;
                b.addActionListener(o);
            }
        }
    }

    /**
     * Add an
     * <code>ActionListener</code> to all buttons in {@link #bottomButtonPane}
     *
     * @param o the ActionListener to be added
     */
    public void addBottomButtonsActionListener(ActionListener o) {
        for (Component component : bottomButtonPane.getComponents()) {
            if (component instanceof JButton) {
                JButton b = (JButton) component;
                b.addActionListener(o);
            }
        }
    }

    /**
     * Enables or disables all the buttons into {@link #bottomButtonPane}
     *
     * @param enable
     */
    public void setEnabledBottomButtons(boolean enable) {
        for (Component component : bottomButtonPane.getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(enable);
            }
        }
    }

    public void setEnabledToolBarButtons(boolean enable) {
        for (Component component : jToolBar.getComponents()) {
            if (component instanceof JButton) {
                component.setEnabled(enable);
            }
        }
    }

    /**
     * Set enable to FALSE to all componenets in the panel. For visibility
     * reasons, this methond doesn't affect to
     * {@link JScrollPane}, {@link JLabel} and {@link JSeparator}
     *
     * @param enable
     * @see #setComponentsEnabled(java.awt.Component[], boolean)
     * @see #setEnableDependingOfType(java.awt.Component, boolean)
     */
    public void setPanelComponentsEnabled(boolean enable) {
        setComponentsEnabled(panel.getComponents(), enable);
    }

    private void setComponentsEnabled(Component[] components, boolean enable) {
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel subPanel = (JPanel) component;
                setComponentsEnabled(subPanel.getComponents(), enable);
            } else {
                setEnableDependingOfType(component, enable);
            }
        }
    }

    protected void setEnableDependingOfType(Component component, boolean enable) {
        if (!(component instanceof JScrollPane)
                && !(component instanceof JLabel)
                && !(component instanceof JSeparator)) {
            component.setEnabled(enable);
        }
    }

    public void setPermisos(int uno_o_dos) {
        if (uno_o_dos == 1) {
            setEnabledToolBarButtons(false);
            setEnabledBottomButtons(false);
            btnBuscar.setEnabled(true);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="getters components">
    public JPanel getBottomButtonPane() {
        return bottomButtonPane;
    }

    public JButton getBtnAceptar() {
        return btnAceptar;
    }

    public JButton getBtnAgregar() {
        return btnNuevo;
    }

    public JButton getBtnBorrar() {
        return btnBorrar;
    }

    public JButton getBtnBuscar() {
        return btnBuscar;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }

    public JButton getBtnEditar() {
        return btnEditar;
    }

    public JToolBar getjToolBar() {
        return jToolBar;
    }

    public JPanel getPanel() {
        return panel;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="init componentes......que loco!">
    private void initComponents() {
        this.setResizable(false);
        Insets insets = new Insets(2, 1, 2, 1);
//        Insets insets = new Insets(0, 0, 0, 0);
        jToolBar = new JToolBar();
        btnNuevo = new JButton();
        btnBorrar = new JButton();
        btnEditar = new JButton();
        btnBuscar = new JButton();
        btnAceptar = new JButton();
        btnCancelar = new JButton();

        messageLabel = new JLabel();
        messageLabel.setFont(new Font("Tahoma", 0, 12));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
        messageLabel.setVerticalAlignment(SwingConstants.TOP);
        setMessageText(messageToHTML);
        JPanel messagePanel = new JPanel();
        messagePanel.setBackground(Color.BLACK);
        messagePanel.setLayout(new GridLayout(1, 1));
        messagePanel.add(messageLabel);

        JPanel toolBarPanel = new JPanel();
        toolBarPanel.setBackground(topButtonBackgroundColor);
        toolBarPanel.setLayout(new GridLayout(1, 1));
        toolBarPanel.add(jToolBar);
        try {
            btnNuevo.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "add.png")));
        } catch (NullPointerException e) {
            System.out.println("Icono nuevo no encontrado");
        }
        btnNuevo.setMnemonic('n');
        btnNuevo.setText("Nuevo");
        btnNuevo.setFocusable(false);
        btnNuevo.setMargin(insets);
        try {
            btnBorrar.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "delete.png")));
        } catch (NullPointerException e) {
            System.out.println("Icono borrar no encontrado");
        }
        btnBorrar.setMnemonic('e');
        btnBorrar.setText("Eliminar");
        btnBorrar.setFocusable(false);
        btnBorrar.setMargin(insets);

        try {
            btnEditar.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "book_edit.png")));
        } catch (NullPointerException e) {
            System.out.println("Icono editar no encontrado");
        }
        btnEditar.setMnemonic('m');
        btnEditar.setText("Modificar");
        btnEditar.setFocusable(false);
        btnEditar.setMargin(insets);
        try {
            btnBuscar.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "buscar.png")));
        } catch (NullPointerException e) {
            System.out.println("Icono buscar no encontrado");
        }
        btnBuscar.setMnemonic('b');
        btnBuscar.setText("Buscar");
        btnBuscar.setFocusable(false);

        btnAceptar.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "32px-Crystal_Clear_action_apply.png")));
        btnAceptar.setMnemonic('a');
        btnAceptar.setText("Aceptar");
        btnAceptar.setMargin(insets);

        btnCancelar.setIcon(new ImageIcon(getClass().getResource(RESOURCE_FOLDER_PATH + "cancelar.png")));
        btnCancelar.setMnemonic('c');
        btnCancelar.setText("Cancelar");
        btnCancelar.setMargin(insets);

        jToolBar.setFloatable(false);
        jToolBar.add(btnNuevo);
        jToolBar.add(btnBorrar);
        jToolBar.add(btnEditar);
        jToolBar.add(btnBuscar);
        for (Component component : jToolBar.getComponents()) {
            if (component instanceof JButton) {
                JButton b = (JButton) component;
                b.setForeground(topButtonForegroundColor);
                b.setBackground(topButtonBackgroundColor);
            }
        }

        bottomButtonPane = new JPanel();
        bottomButtonPane.setLayout(new BoxLayout(bottomButtonPane, BoxLayout.LINE_AXIS));
        bottomButtonPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 10));
        bottomButtonPane.add(Box.createHorizontalGlue());
        bottomButtonPane.add(btnAceptar);
        bottomButtonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        bottomButtonPane.add(btnCancelar);

        topPanel = new JPanel();
        topPanel.setBackground(topButtonBackgroundColor);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        topPanel.add(messagePanel);
        topPanel.add(toolBarPanel);

        getContentPane().add(topPanel, BorderLayout.PAGE_START);
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bottomButtonPane, BorderLayout.PAGE_END);

        pack();
    }
    //</editor-fold>

    public void showMessage(String msj, String title, int messageType) {
        JOptionPane.showMessageDialog(this, msj, title, messageType);
    }

    /**
     * Set a message at the top of the GUI. This will be formatted with HTML
     * TAG's like: Enclosed by &lt HTML> .. &lt/HTML>, "\n" replaced by &lt br>
     *
     * @param messageText
     */
    public void setMessageText(String messageText) {
        messageToHTML = messageText;
        String htmlMessage = getHtmlText(messageText);
        if (htmlMessage != null) {
            Border paddingBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            messageLabel.setBorder(paddingBorder);
        }
        messageLabel.setText(htmlMessage);
        messageLabel.setVisible(htmlMessage != null);
        this.pack();
        if (htmlMessage != null) {
            int i = htmlMessage.split("<BR>").length;
            System.out.println(this.getClass() + "Cantidada de <BR> = " + i);
            int extraHeight = 0;
            if (i - 2 > 0) {
                extraHeight = 30 * (i - 2);
            }
            //setea el ancho del mensaje igual al ancho del panel, así no se estiiiiiiiiiiiiiiira el mensaje 
            messageLabel.setPreferredSize(new Dimension(panel.getPreferredSize().width, messageLabel.getPreferredSize().height + extraHeight));
        }
    }

    public JLabel getMessageLabel() {
        return messageLabel;
    }

    private String getHtmlText(String messageText1) {
        String insentiveCaseRegEx = "(?ui)";
        if (messageText1 != null) {
            String htmlFormatted;
            htmlFormatted = messageText1.replaceAll(insentiveCaseRegEx + "<html>", "").replaceAll(insentiveCaseRegEx + "</html>", "");
            htmlFormatted = htmlFormatted.replaceAll(insentiveCaseRegEx + "\\n", "<BR>");
            return "<HTML><BR>" + htmlFormatted + "</HTML>";
        }
        return null;
    }
}
