/*
 * PanelABMListaPrecio.java
 *
 * Created on 18/03/2010, 16:39:33
 */

package gui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import javax.swing.JCheckBox;
import javax.swing.JTable;

/**
 *
 * @author FiruzzZ
 */
public class PanelABMListaPrecio extends javax.swing.JPanel {

    /** Creates new form PanelABMListaPrecio */
    public PanelABMListaPrecio() {
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

      jLabel1 = new javax.swing.JLabel();
      tfNombre = new javax.swing.JTextField();
      jLabel2 = new javax.swing.JLabel();
      btnADD = new javax.swing.JButton();
      btnDEL = new javax.swing.JButton();
      tfMargenGeneral = new javax.swing.JTextField();
      jLabel4 = new javax.swing.JLabel();
      jSeparator1 = new javax.swing.JSeparator();
      checkMargenGeneral = new javax.swing.JCheckBox();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTable1 = new javax.swing.JTable();
      jScrollPane2 = new javax.swing.JScrollPane();
      jTable2 = new javax.swing.JTable();
      tfMargenPorRubro = new javax.swing.JTextField();
      jLabel3 = new javax.swing.JLabel();
      jLabel5 = new javax.swing.JLabel();

      jLabel1.setText("Nombre");

      tfNombre.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfNombreKeyTyped(evt);
         }
      });

      jLabel2.setText("Rubros");

      btnADD.setText(">>");
      btnADD.setName("addRubro"); // NOI18N

      btnDEL.setText("<<");
      btnDEL.setName("delRubro"); // NOI18N

      tfMargenGeneral.setEnabled(false);
      tfMargenGeneral.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfMargenGeneralKeyTyped(evt);
         }
      });

      jLabel4.setText("Rubros afectados");

      checkMargenGeneral.setText("Margen general   %");
      checkMargenGeneral.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(java.awt.event.ItemEvent evt) {
            checkMargenGeneralItemStateChanged(evt);
         }
      });
      checkMargenGeneral.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            checkMargenGeneralActionPerformed(evt);
         }
      });

      jTable1.setModel(new javax.swing.table.DefaultTableModel());
      jScrollPane1.setViewportView(jTable1);

      jTable2.setModel(new javax.swing.table.DefaultTableModel());
      jScrollPane2.setViewportView(jTable2);

      tfMargenPorRubro.setColumns(2);
      tfMargenPorRubro.setToolTipText("Margen por rubro");
      tfMargenPorRubro.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfMargenPorRubroKeyTyped(evt);
         }
      });

      jLabel3.setText("%");

      jLabel5.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
      jLabel5.setForeground(new java.awt.Color(0, 102, 255));
      jLabel5.setText("<html>\nSi tanto Rubros como Sub-Rubro de  un producto\nse encuentran en la Lista de Precios.<br>\nEl margen será afecta por el primer Rubro (no por ambos).\n</html>");

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(checkMargenGeneral)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfMargenGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel2)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addGap(18, 18, 18)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(tfMargenPorRubro, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnADD, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnDEL, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel3))
                  .addGap(18, 18, 18)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel4)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addContainerGap()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(checkMargenGeneral)
                     .addComponent(tfMargenGeneral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGap(42, 42, 42)
                  .addComponent(jLabel3)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(tfMargenPorRubro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(btnADD)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(btnDEL)
                  .addContainerGap())
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                  .addContainerGap())))
      );
   }// </editor-fold>//GEN-END:initComponents

    private void checkMargenGeneralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMargenGeneralActionPerformed
        tfMargenGeneral.setEnabled(checkMargenGeneral.isSelected());
        jTable1.setEnabled(!checkMargenGeneral.isSelected());
        jTable2.setEnabled(!checkMargenGeneral.isSelected());
        btnADD.setEnabled(!checkMargenGeneral.isSelected());
        btnDEL.setEnabled(!checkMargenGeneral.isSelected());
        tfMargenPorRubro.setEnabled(!checkMargenGeneral.isSelected());
    }//GEN-LAST:event_checkMargenGeneralActionPerformed

    private void tfNombreKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfNombreKeyTyped
        evt.setKeyChar((String.valueOf(evt.getKeyChar()).toUpperCase()).charAt(0));
    }//GEN-LAST:event_tfNombreKeyTyped

    private void tfMargenGeneralKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfMargenGeneralKeyTyped
        numeros_puntos_menos(evt);
    }//GEN-LAST:event_tfMargenGeneralKeyTyped

    private void tfMargenPorRubroKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfMargenPorRubroKeyTyped
        numeros_puntos_menos(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfMargenPorRubroKeyTyped

    private void checkMargenGeneralItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_checkMargenGeneralItemStateChanged
        checkMargenGeneralActionPerformed(null);
    }//GEN-LAST:event_checkMargenGeneralItemStateChanged


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton btnADD;
   private javax.swing.JButton btnDEL;
   private javax.swing.JCheckBox checkMargenGeneral;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JSeparator jSeparator1;
   private javax.swing.JTable jTable1;
   private javax.swing.JTable jTable2;
   private javax.swing.JTextField tfMargenGeneral;
   private javax.swing.JTextField tfMargenPorRubro;
   private javax.swing.JTextField tfNombre;
   // End of variables declaration//GEN-END:variables

    public void setListener(Object o) {
        btnADD.addActionListener((ActionListener) o);
        btnDEL.addActionListener((ActionListener) o);
//        jTable1.addMouseListener((MouseListener) o);
//        jTable2.addMouseListener((MouseListener) o);
//        checkMargenGeneral.addActionListener((ActionListener) o);
    }
    public JCheckBox getCheckMargenGeneral() {
        return checkMargenGeneral;
    }

    public JTable getjTable1() {
        return jTable1;
    }

    public javax.swing.table.DefaultTableModel getDTMRubros() {
        return (javax.swing.table.DefaultTableModel) jTable1.getModel();
    }
    
    public javax.swing.table.DefaultTableModel getDTMAfectados() {
        return (javax.swing.table.DefaultTableModel) jTable2.getModel();
    }
    public JTable getjTable2() {
        return jTable2;
    }

    public String getTfMargenPorRubro() {
        return tfMargenPorRubro.getText();
    }

    public String getTfMargenGeneral() {
        return tfMargenGeneral.getText();
    }

    public String getTfNombre() {
        return tfNombre.getText();
    }

    public void setTfMargenPorRubro(String tfMargenPorRubro) {
        this.tfMargenPorRubro.setText(tfMargenPorRubro);
    }

    public void setTfMargenGeneral(String tfMargenGeneral) {
        this.tfMargenGeneral.setText(tfMargenGeneral);
    }

    public void setTfNombre(String tfNombre) {
        this.tfNombre.setText(tfNombre);
    }

    public Object getSelectedRubro() {
        return jTable1.getValueAt(jTable1.getSelectedRow(), 0);
    }

    private void numeros_puntos_menos(KeyEvent evt) {
        int k = evt.getKeyChar();
        if( (k < 48 || k > 57) && (k != 45) && (k != 46) )
            evt.setKeyChar((char)java.awt.event.KeyEvent.VK_CLEAR);
    }


}