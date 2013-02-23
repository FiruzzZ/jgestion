
/*
 * JDInfo.java
 *
 * Created on 08/04/2010, 11:03:26
 */

package gui;

import java.awt.Dialog;

/**
 *
 * @author FiruzzZ
 */
public class JDInfo extends javax.swing.JDialog {

    /** Creates new form JDInfo */
    public JDInfo(java.awt.Frame parent, boolean modal, String mensaje) {
        super(parent, modal);
        initComponents();
        jLabel1.setText(mensaje);
        this.setVisible(true);
    }

   public JDInfo(Dialog owner, boolean modal,String mensaje) {
      super(owner, modal);
      initComponents();
      jLabel1.setText(mensaje);
      this.setVisible(true);
   }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jLabel1 = new javax.swing.JLabel();
      checkMsj = new javax.swing.JCheckBox();
      jButton1 = new javax.swing.JButton();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("y el título?");

      jLabel1.setText("<html>Mensaje de información de ventana.<br>Entendés?</html>");

      checkMsj.setText("No volver a mostrar este mensaje");

      jButton1.setText("Cerrar");
      jButton1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton1ActionPerformed(evt);
         }
      });

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap(143, Short.MAX_VALUE)
            .addComponent(jButton1)
            .addContainerGap())
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(checkMsj)
               .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(19, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
            .addComponent(checkMsj)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jButton1)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
      dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JCheckBox checkMsj;
   private javax.swing.JButton jButton1;
   private javax.swing.JLabel jLabel1;
   // End of variables declaration//GEN-END:variables

   public boolean getjCheckMsj() {
      return checkMsj.isSelected();
   }


}
