/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JDialogTable.java
 *
 * Created on 13/04/2011, 08:43:36
 */
package gui.generics;

import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class JDialogTable extends javax.swing.JDialog {

   private DefaultTableModel defaultTableModel;

   /**
    * Default settings
    * Size: 500,220
    * LocationRelationTo: owner
    * @param owner
    * @param title
    * @param modal
    * @param defaultTableModel
    */
   public JDialogTable(Frame owner, String title, boolean modal, DefaultTableModel defaultTableModel) {
      super(owner, modal);
      this.setTitle(title);
      this.defaultTableModel = defaultTableModel;
      initComponents();
   }

   public JDialogTable(JDialog owner, String title, boolean modal, DefaultTableModel defaultTableModel) {
      super(owner, modal);
      this.setTitle(title);
      this.defaultTableModel = defaultTableModel;
      initComponents();
      init();
   }

   private void init() {
      setLocationRelativeTo(getOwner());
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jScrollPane1 = new javax.swing.JScrollPane();
      jTable1 = new javax.swing.JTable();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

      jTable1.setModel(defaultTableModel);
      jScrollPane1.setViewportView(jTable1);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE)
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JTable jTable1;
   // End of variables declaration//GEN-END:variables

   public JTable getjTable1() {
      return jTable1;
   }
}
