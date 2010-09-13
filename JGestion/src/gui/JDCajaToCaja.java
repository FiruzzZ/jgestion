/*
 * JDCajaToCaja.java
 *
 * Created on 21/04/2010, 11:01:01
 */

package gui;

import com.toedter.calendar.JDateChooser;
import entity.UTIL;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/**
 *
 * @author FiruzzZ
 */
public class JDCajaToCaja extends javax.swing.JDialog {

    /** Creates new form JDCajaToCaja */
    public JDCajaToCaja(java.awt.Frame parent, boolean modal) {
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

      bBuscar = new javax.swing.JButton();
      bAceptar = new javax.swing.JButton();
      bCancelar = new javax.swing.JButton();
      labelReRe = new javax.swing.JLabel();
      tfMovimiento = new javax.swing.JTextField();
      jLabel15 = new javax.swing.JLabel();
      cbCajaOrigen = new javax.swing.JComboBox();
      jLabel7 = new javax.swing.JLabel();
      dcOrigen = new com.toedter.calendar.JDateChooser();
      jLabel1 = new javax.swing.JLabel();
      tfTotalOrigen = new javax.swing.JTextField();
      jLabel16 = new javax.swing.JLabel();
      cbCajaDestino = new javax.swing.JComboBox();
      jLabel2 = new javax.swing.JLabel();
      tfTotalDestino = new javax.swing.JTextField();
      jLabel10 = new javax.swing.JLabel();
      dcDestino = new com.toedter.calendar.JDateChooser();
      jSeparator1 = new javax.swing.JSeparator();
      jLabel8 = new javax.swing.JLabel();
      tfMontoMovimiento = new javax.swing.JTextField();
      tfObservacion = new javax.swing.JTextField();
      jLabel9 = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Movimiento entre Cajas");

      bBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
      bBuscar.setText("Buscar");
      bBuscar.setToolTipText("Buscar comprobante");
      bBuscar.setName("buscar"); // NOI18N

      bAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_apply.png"))); // NOI18N
      bAceptar.setText("Aceptar");
      bAceptar.setName("aceptar"); // NOI18N

      bCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cancelar.png"))); // NOI18N
      bCancelar.setText("Cancelar");
      bCancelar.setName("cancelar"); // NOI18N
      bCancelar.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            bCancelarActionPerformed(evt);
         }
      });

      labelReRe.setText("Nº Movimiento");

      tfMovimiento.setEditable(false);
      tfMovimiento.setFont(new java.awt.Font("Tahoma", 1, 11));
      tfMovimiento.setHorizontalAlignment(javax.swing.JTextField.CENTER);
      tfMovimiento.setText("0");
      tfMovimiento.setRequestFocusEnabled(false);

      jLabel15.setText("Caja origen");

      cbCajaOrigen.setName("cajaOrigen"); // NOI18N

      jLabel7.setText("Fecha Apertura");
      jLabel7.setRequestFocusEnabled(false);

      dcOrigen.setDateFormatString(entity.UTIL.DATE_FORMAT.toPattern());
      dcOrigen.setEnabled(false);
      dcOrigen.setRequestFocusEnabled(false);

      jLabel1.setText("Total");
      jLabel1.setRequestFocusEnabled(false);

      tfTotalOrigen.setEditable(false);
      tfTotalOrigen.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

      jLabel16.setText("Caja destino");

      cbCajaDestino.setName("cajaDestino"); // NOI18N

      jLabel2.setText("Total");
      jLabel2.setRequestFocusEnabled(false);

      tfTotalDestino.setEditable(false);
      tfTotalDestino.setFont(new java.awt.Font("Tahoma", 1, 11));

      jLabel10.setText("Fecha Apertura");
      jLabel10.setRequestFocusEnabled(false);

      dcDestino.setDateFormatString(entity.UTIL.DATE_FORMAT.toPattern());
      dcDestino.setEnabled(false);
      dcDestino.setRequestFocusEnabled(false);

      jLabel8.setText("Monto");

      tfMontoMovimiento.setColumns(7);
      tfMontoMovimiento.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
      tfMontoMovimiento.setToolTipText("Monto ($)");
      tfMontoMovimiento.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            tfMontoMovimientoFocusLost(evt);
         }
      });

      jLabel9.setText("Observación");

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(tfObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfMontoMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addComponent(bAceptar)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(bCancelar))
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                  .addGroup(layout.createSequentialGroup()
                     .addComponent(labelReRe)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(tfMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 234, Short.MAX_VALUE)
                     .addComponent(bBuscar))
                  .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                  .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(cbCajaOrigen, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbCajaDestino, 0, 174, Short.MAX_VALUE)
                        .addComponent(jLabel16)
                        .addComponent(jLabel15))
                     .addGap(18, 18, 18)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                           .addComponent(jLabel1)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                           .addComponent(tfTotalOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                           .addComponent(jLabel7)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                           .addComponent(dcOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                           .addComponent(jLabel2)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                           .addComponent(tfTotalDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                           .addComponent(jLabel10)
                           .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                           .addComponent(dcDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))))))
            .addContainerGap(10, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(bBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(labelReRe)
                  .addComponent(tfMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(6, 6, 6)
            .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel15)
            .addGap(4, 4, 4)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(cbCajaOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel16))
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(jLabel1)
                  .addComponent(tfTotalOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jLabel7))
               .addComponent(dcOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(cbCajaDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel8)
                     .addComponent(tfMontoMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addComponent(dcDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(jLabel2)
                  .addComponent(tfTotalDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jLabel10)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jLabel9)
               .addComponent(tfObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(bCancelar)
               .addComponent(bAceptar))
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void bCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCancelarActionPerformed
         resetPanel();
    }//GEN-LAST:event_bCancelarActionPerformed

    private void tfMontoMovimientoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfMontoMovimientoFocusLost
      try {
         tfMontoMovimiento.setText(UTIL.PRECIO_CON_PUNTO.format(Double.valueOf(tfMontoMovimiento.getText())));
      } catch (NumberFormatException ex) {
         System.out.println(ex.getClass()+" no safó montoMovimiento");
      }
    }//GEN-LAST:event_tfMontoMovimientoFocusLost

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton bAceptar;
   private javax.swing.JButton bBuscar;
   private javax.swing.JButton bCancelar;
   private javax.swing.JComboBox cbCajaDestino;
   private javax.swing.JComboBox cbCajaOrigen;
   private com.toedter.calendar.JDateChooser dcDestino;
   private com.toedter.calendar.JDateChooser dcOrigen;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel10;
   private javax.swing.JLabel jLabel15;
   private javax.swing.JLabel jLabel16;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel7;
   private javax.swing.JLabel jLabel8;
   private javax.swing.JLabel jLabel9;
   private javax.swing.JSeparator jSeparator1;
   private javax.swing.JLabel labelReRe;
   private javax.swing.JTextField tfMontoMovimiento;
   private javax.swing.JTextField tfMovimiento;
   private javax.swing.JTextField tfObservacion;
   private javax.swing.JTextField tfTotalDestino;
   private javax.swing.JTextField tfTotalOrigen;
   // End of variables declaration//GEN-END:variables

   public JComboBox getCbCajaDestino() {
      return cbCajaDestino;
   }

   public JComboBox getCbCajaOrigen() {
      return cbCajaOrigen;
   }

   public JDateChooser getDcDestino() {
      return dcDestino;
   }

   public JDateChooser getDcOrigen() {
      return dcOrigen;
   }

   public JTextField getTfMontoMovimiento() {
      return tfMontoMovimiento;
   }

   public JTextField getTfMovimiento() {
      return tfMovimiento;
   }

   public JTextField getTfObservacion() {
      return tfObservacion;
   }

   public JTextField getTfTotalDestino() {
      return tfTotalDestino;
   }

   public JTextField getTfTotalOrigen() {
      return tfTotalOrigen;
   }

   public void setListener(Object o) {
      bAceptar.addActionListener((ActionListener) o);
      bBuscar.addActionListener((ActionListener) o);
      cbCajaOrigen.addActionListener((ActionListener) o);
      cbCajaDestino.addActionListener((ActionListener) o);
   }

   public void resetPanel() {
      cbCajaOrigen.setSelectedIndex(0);
      cbCajaDestino.setSelectedIndex(0);
      tfMontoMovimiento.setText("");
      tfObservacion.setText("");
   }

   public void showMessage(String msg, String titulo, int messageType) {
      if(messageType >= -1 && messageType <= 2)
         javax.swing.JOptionPane.showMessageDialog(this, msg, titulo, messageType);
      else
         javax.swing.JOptionPane.showMessageDialog(this, msg);
   }
}