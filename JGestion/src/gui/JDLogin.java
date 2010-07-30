
/*
 * JDLogin.java
 *
 * Created on 17/12/2009, 09:06:09
 */
package gui;

import java.awt.event.KeyListener;
import javax.swing.JLabel;

/**
 *
 * @author Administrador
 */
public class JDLogin extends javax.swing.JDialog {

   /** Creates new form JDLogin */
   public JDLogin(java.awt.Frame parent, boolean modal) {
      super(parent, modal);
      initComponents();
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      tfNombre = new javax.swing.JTextField();
      jLabel1 = new javax.swing.JLabel();
      jLabel2 = new javax.swing.JLabel();
      jButton1 = new javax.swing.JButton();
      tfPwd = new javax.swing.JPasswordField();
      jLabel3 = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Identificación de usuario");

      tfNombre.setName("ulogin"); // NOI18N
      tfNombre.setNextFocusableComponent(tfPwd);

      jLabel1.setText("Usuario");

      jLabel2.setText("Contraseña");

      jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_editdelete.png"))); // NOI18N
      jButton1.setText("Salir");
      jButton1.setNextFocusableComponent(tfNombre);
      jButton1.setOpaque(false);
      jButton1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton1ActionPerformed(evt);
         }
      });

      tfPwd.setName("plogin"); // NOI18N
      tfPwd.setNextFocusableComponent(jButton1);

      jLabel3.setForeground(new java.awt.Color(255, 0, 0));
      jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel1)
                     .addComponent(jLabel2))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(tfPwd)
                     .addComponent(tfNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                  .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(tfPwd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addComponent(jButton1))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       dispose();
       System.exit(0);
    }//GEN-LAST:event_jButton1ActionPerformed
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton jButton1;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JTextField tfNombre;
   private javax.swing.JPasswordField tfPwd;
   // End of variables declaration//GEN-END:variables

   public String getTfU() {
      return tfNombre.getText();
   }

   public String getPass() {
      return String.valueOf(tfPwd.getPassword());
   }

   public JLabel getjLabel3() {
      return jLabel3;
   }

   public void setListener(Object o) {
      tfNombre.addKeyListener((KeyListener) o);
      tfPwd.addKeyListener((KeyListener) o);
   }

       /**
     * Setea un mensaje de salida para el usuario.
     * Si @param messageType < -1 || > 3 no hace NADA.
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
}
