/*
 * JDABM.java
 *
 * Created on 20/11/2009, 20:51:14
 */

package gui;

import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Administrador
 */
public class JDABM extends javax.swing.JDialog {
    private final JPanel panel;

    /** Creates new form JDABM */
    public JDABM(java.awt.Frame parent, boolean modal, javax.swing.JPanel panelNEW) {
        super(parent, modal);
        this.panel = panelNEW;
        getContentPane().add(this.panel);
        pack();
        initComponents();
        this.setLocationRelativeTo(parent);
        ajustarABMSizeToPanel();
    }

   public JDABM(javax.swing.JDialog owner, boolean modal, javax.swing.JPanel panelNEW) {
        super(owner, modal);
        this.panel = panelNEW;
        getContentPane().add(this.panel);
        pack();
        initComponents();
        this.setLocationRelativeTo(owner);
        ajustarABMSizeToPanel();
   }

   private void ajustarABMSizeToPanel() {
      this.setSize(panel.getWidth() + 10, panel.getHeight() + 90);
   }
    public void setListener(Object o) {
        bAceptar.addActionListener((ActionListener) o);
        bCancelar.addActionListener((ActionListener) o);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      bCancelar = new javax.swing.JButton();
      bAceptar = new javax.swing.JButton();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setResizable(false);

      bCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_button_cancel.png"))); // NOI18N
      bCancelar.setText("Cancelar");
      bCancelar.setName("cancelar"); // NOI18N

      bAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_apply.png"))); // NOI18N
      bAceptar.setText("Aceptar");
      bAceptar.setName("aceptar"); // NOI18N

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap(83, Short.MAX_VALUE)
            .addComponent(bAceptar)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(bCancelar)
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(47, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(bCancelar)
               .addComponent(bAceptar))
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton bAceptar;
   private javax.swing.JButton bCancelar;
   // End of variables declaration//GEN-END:variables

    public void cerrar() { dispose(); }

    public void hideBotones() {
        bAceptar.setVisible(false);
        bCancelar.setVisible(false);
    }

    /**
     * Setea un mensaje de información.
     * Si @param messageType < -1 || >3 no hace NADA.
     * @param msg,.. mensaje.
     * @param titulo (puede tener título o no).
     * @param messageType =3 (sin título), sino -1=PLAIN,0=ERROR, 1=INFO,2=WARRNING.
     */
    public void showMessage(String msg, String titulo, int messageType) {
        if(messageType <-1 || messageType >3) return;
        if(messageType==3)
            javax.swing.JOptionPane.showMessageDialog(this, msg);
        else
            javax.swing.JOptionPane.showMessageDialog(this, msg, titulo, messageType);

    }

    public boolean mensajeDeAceptacion(String mensaje, String title) {
        if(0 == JOptionPane.showConfirmDialog(this, mensaje, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
            return true;
        return false;
    }

}
