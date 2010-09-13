
/*
 * JDMiniABM.java
 *
 * Created on 15/02/2010, 11:13:23
 */
package gui;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Pantalla de ABM para Rubros y Marcas, UnidadMedidas
 * @author FiruzzZ
 */
public class JDMiniABM extends javax.swing.JDialog {

   /** Creates new form JDMiniABM */
   public JDMiniABM(java.awt.Frame parent, boolean modal) {
      super(parent, modal);
      initComponents();
      this.setLocationRelativeTo(parent);
   }

   public JDMiniABM(Dialog owner, boolean modal) {
      super(owner, modal);
      initComponents();
      this.setLocationRelativeTo(owner);
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jScrollPane2 = new javax.swing.JScrollPane();
      jTable1 = new javax.swing.JTable();
      bAceptar = new javax.swing.JButton();
      bCancelar = new javax.swing.JButton();
      bNuevo = new javax.swing.JButton();
      bEliminar = new javax.swing.JButton();
      jLabel1 = new javax.swing.JLabel();
      tfNombre = new javax.swing.JTextField();
      jLabelCodigo = new javax.swing.JLabel();
      tfCodigo = new javax.swing.JTextField();
      jLabelExtra = new javax.swing.JLabel();
      tfExtra = new javax.swing.JTextField();
      btnLock = new javax.swing.JButton();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("poner título!!!");
      setResizable(false);

      jTable1.setModel(new javax.swing.table.DefaultTableModel());
      jTable1.setFocusable(false);
      jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      jScrollPane2.setViewportView(jTable1);

      bAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_apply.png"))); // NOI18N
      bAceptar.setText("Guardar");
      bAceptar.setName("guardar"); // NOI18N

      bCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_button_cancel.png"))); // NOI18N
      bCancelar.setText("Cancelar");
      bCancelar.setName("cancelar"); // NOI18N

      bNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_edit_add.png"))); // NOI18N
      bNuevo.setText("Nuevo");
      bNuevo.setName("new"); // NOI18N
      bNuevo.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            bNuevoActionPerformed(evt);
         }
      });

      bEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/delete.png"))); // NOI18N
      bEliminar.setText("Eliminar");
      bEliminar.setName("del"); // NOI18N

      jLabel1.setText("Nombre");

      tfNombre.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfNombreKeyTyped(evt);
         }
      });

      jLabelCodigo.setText("Código");

      jLabelExtra.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      jLabelExtra.setText("Extra");

      btnLock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lock.png"))); // NOI18N
      btnLock.setText("Baja");
      btnLock.setName("lock"); // NOI18N

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(bNuevo)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(bEliminar)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(btnLock, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabelCodigo, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(tfCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelExtra, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tfExtra))
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(bAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(tfNombre, javax.swing.GroupLayout.Alignment.LEADING))
                  .addGap(10, 10, 10))
               .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addComponent(bNuevo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(bEliminar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(btnLock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel1))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jLabelCodigo)
               .addComponent(tfExtra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabelExtra)
               .addComponent(tfCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(bAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(bCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void bNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bNuevoActionPerformed
       tfNombre.requestFocus();
    }//GEN-LAST:event_bNuevoActionPerformed

    private void tfNombreKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfNombreKeyTyped
       toUpperCase(evt);
    }//GEN-LAST:event_tfNombreKeyTyped
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton bAceptar;
   private javax.swing.JButton bCancelar;
   private javax.swing.JButton bEliminar;
   private javax.swing.JButton bNuevo;
   private javax.swing.JButton btnLock;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabelCodigo;
   private javax.swing.JLabel jLabelExtra;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JTable jTable1;
   private javax.swing.JTextField tfCodigo;
   private javax.swing.JTextField tfExtra;
   private javax.swing.JTextField tfNombre;
   // End of variables declaration//GEN-END:variables

   public javax.swing.table.DefaultTableModel getDTM() {
      return (javax.swing.table.DefaultTableModel) jTable1.getModel();
   }

   public JTable getjTable1() {
      return jTable1;
   }

   public void setDTM(javax.swing.table.DefaultTableModel dtm) {
      jTable1.setModel(dtm);
   }

   public void setListeners(Object o) {
      bNuevo.addActionListener((ActionListener) o);
      bEliminar.addActionListener((ActionListener) o);
      bAceptar.addActionListener((ActionListener) o);
      bCancelar.addActionListener((ActionListener) o);
      jTable1.addMouseListener((MouseListener) o);
      btnLock.addActionListener((ActionListener) o);
   }

   public String getTfCodigo() {
      return tfCodigo.getText();
   }

   public String getTfNombre() {
      return tfNombre.getText();
   }

   public String getTfExtra() {
      return tfExtra.getText();
   }

   public void setTfCodigo(String tfCodigo) {
      this.tfCodigo.setText(tfCodigo);
   }

   public void setTfNombre(String tfNombre) {
      this.tfNombre.setText(tfNombre);
   }

   public void setTfExtra(String tfExtra) {
      this.tfExtra.setText(tfExtra);
   }

   /**
    * Setea un mensaje de salida para el usuario.
    * Si @param messageType < -1 || > 3 no hace NADA.
    * @param msg,.. mensaje.
    * @param titulo (puede tener título o no).
    * @param messageType =3 (sin título), sino -1=PLAIN,0=ERROR, 1=INFO,2=WARRNING.
    */
   public void showMessage(String msg, String titulo, int messageType) {
      if (messageType < -1 || messageType > 3) {
         return;
      }
      if (messageType == 3) {
         javax.swing.JOptionPane.showMessageDialog(this, msg);
      } else {
         javax.swing.JOptionPane.showMessageDialog(this, msg, titulo, messageType);
      }

   }

   private void toUpperCase(KeyEvent evt) {
      evt.setKeyChar(String.valueOf(evt.getKeyChar()).toUpperCase().charAt(0));
   }

   public void hideFieldNombre() {
      jLabel1.setVisible(false);
      tfNombre.setVisible(false);
   }

   public void hideFieldCodigo() {
      jLabelCodigo.setVisible(false);
      tfCodigo.setVisible(false);
   }

   public void hideFieldExtra() {
      jLabelExtra.setVisible(false);
      tfExtra.setVisible(false);
   }

   public void hideBtnLock() {
      btnLock.setVisible(false);

   }

   public void clearPanelFields() {
      tfNombre.setText("");
      tfCodigo.setText("");
      tfExtra.setText("");
   }

   public void tfNombreRequestFocus() {
      tfNombre.requestFocus();
   }

   public JButton getBtnLock() {
      return btnLock;
   }

   public JButton getbEliminar() {
      return bEliminar;
   }

   public void hideBtnElimiar() {
      bEliminar.setVisible(false);
   }

   public JButton getbAceptar() {
      return bAceptar;
   }

   public JButton getbCancelar() {
      return bCancelar;
   }

   public JButton getbNuevo() {
      return bNuevo;
   }

   public JLabel getjLabel1() {
      return jLabel1;
   }

   public JLabel getjLabelCodigo() {
      return jLabelCodigo;
   }

   public JLabel getjLabelExtra() {
      return jLabelExtra;
   }

}