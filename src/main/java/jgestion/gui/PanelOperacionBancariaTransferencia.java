package jgestion.gui;

import com.toedter.calendar.JDateChooser;
import jgestion.controller.BancoController;
import jgestion.entity.Banco;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import jgestion.JGestionUtils;
import jgestion.jpa.controller.BancoJpaController;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;
import utilities.general.EntityWrapper;

/**
 *
 * @author FiruzzZ
 */
public class PanelOperacionBancariaTransferencia extends javax.swing.JPanel {

    /**
     * Creates new form PanelOperacionBancariaTransferencia
     */
    public PanelOperacionBancariaTransferencia() {
        initComponents();
        List<EntityWrapper<Banco>> l = JGestionUtils.getWrappedBancos(new BancoController().findWithCuentasBancarias(true));
        UTIL.loadComboBox(cbBancos, l, false);
        UTIL.loadComboBox(cbDestinoBancosCuentaPropia, l, false);
        UTIL.loadComboBox(cbDestinoBancosExternos, JGestionUtils.getWrappedBancos(new BancoJpaController().findAll()), false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgDestino = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cbCuentabancaria = new javax.swing.JComboBox();
        tfMonto = new javax.swing.JTextField();
        dcFechaOperacion = new com.toedter.calendar.JDateChooser();
        labelMonto = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        cbBancos = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        cbDestinoBancosCuentaPropia = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        cbCuentabancariaDestino = new javax.swing.JComboBox();
        rbPropia = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        tfCuentaExterna = new javax.swing.JTextField();
        cbDestinoBancosExternos = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        rbExterna = new javax.swing.JRadioButton();
        tfDescripcionMov = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        bgDestino.add(rbPropia);
        bgDestino.add(rbExterna);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Origen", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N

        jLabel1.setText("N° Cuenta");

        jLabel6.setText("Fecha Operación");

        tfMonto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfMonto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfMontoKeyTyped(evt);
            }
        });

        labelMonto.setText("Monto");

        jLabel10.setText("Banco");

        cbBancos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbBancosActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelMonto, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dcFechaOperacion, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbBancos, 0, 309, Short.MAX_VALUE)
                    .addComponent(cbCuentabancaria, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tfMonto, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(cbBancos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbCuentabancaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel6)
                    .addComponent(dcFechaOperacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfMonto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelMonto)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Destino", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N

        jLabel11.setText("Banco");

        cbDestinoBancosCuentaPropia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbDestinoBancosCuentaPropiaActionPerformed(evt);
            }
        });

        jLabel2.setText("N° Cuenta");

        cbCuentabancariaDestino.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCuentabancariaDestinoActionPerformed(evt);
            }
        });

        rbPropia.setSelected(true);
        rbPropia.setText("Cuenta propia");
        rbPropia.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbPropiaStateChanged(evt);
            }
        });

        jLabel3.setText("N° Cuenta");

        tfCuentaExterna.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfCuentaExterna.setEnabled(false);
        tfCuentaExterna.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfCuentaExternaKeyTyped(evt);
            }
        });

        jLabel12.setText("Banco");

        rbExterna.setText("Cuenta externa");
        rbExterna.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbExternaStateChanged(evt);
            }
        });
        rbExterna.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbExternaActionPerformed(evt);
            }
        });

        jLabel8.setText("Descripción");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbDestinoBancosCuentaPropia, javax.swing.GroupLayout.Alignment.TRAILING, 0, 309, Short.MAX_VALUE)
                    .addComponent(cbCuentabancariaDestino, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbDestinoBancosExternos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tfCuentaExterna)
                    .addComponent(tfDescripcionMov))
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(rbPropia))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(rbExterna)))
                .addGap(307, 307, 307))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(rbPropia)
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(cbDestinoBancosCuentaPropia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cbCuentabancariaDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbExterna)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbDestinoBancosExternos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfCuentaExterna, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDescripcionMov, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addContainerGap())
        );

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/information.png"))); // NOI18N
        jLabel4.setText("<html>La descripción de la transferencia será precedida por el <b>Banco</b> + <b>N° Cuenta</b>.<br>No es necesario que vuelva a colocar esta información en el campo <b>Descripción</b>.</html>");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbBancosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbBancosActionPerformed
        if (cbBancos.getItemCount() > 0) {
            Banco b = ((EntityWrapper<Banco>) cbBancos.getSelectedItem()).getEntity();
            UTIL.loadComboBox(cbCuentabancaria, JGestionUtils.getWrappedCuentasBancarias(b.getCuentasbancaria()), false);
        }
    }//GEN-LAST:event_cbBancosActionPerformed

    private void tfMontoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfMontoKeyTyped
        SwingUtil.checkInputDigit(evt, true);
    }//GEN-LAST:event_tfMontoKeyTyped

    private void cbDestinoBancosCuentaPropiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbDestinoBancosCuentaPropiaActionPerformed
        if (cbDestinoBancosCuentaPropia.getItemCount() > 0) {
            Banco b = ((EntityWrapper<Banco>) cbDestinoBancosCuentaPropia.getSelectedItem()).getEntity();
            UTIL.loadComboBox(cbCuentabancariaDestino, JGestionUtils.getWrappedCuentasBancarias(b.getCuentasbancaria()), false);
        }
    }//GEN-LAST:event_cbDestinoBancosCuentaPropiaActionPerformed

    private void rbExternaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rbExternaStateChanged
        habilitarDestino();
    }//GEN-LAST:event_rbExternaStateChanged

    private void cbCuentabancariaDestinoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCuentabancariaDestinoActionPerformed
        if (cbCuentabancariaDestino.getItemCount() > 0) {
            tfDescripcionMov.setText("Interna:" + cbDestinoBancosCuentaPropia.getSelectedItem().toString() + " N° " + cbCuentabancariaDestino.getSelectedItem().toString());
        }
    }//GEN-LAST:event_cbCuentabancariaDestinoActionPerformed

    private void rbExternaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbExternaActionPerformed
    }//GEN-LAST:event_rbExternaActionPerformed

    private void tfCuentaExternaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCuentaExternaKeyTyped
        SwingUtil.checkInputDigit(evt, false, 22);
    }//GEN-LAST:event_tfCuentaExternaKeyTyped

    private void rbPropiaStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rbPropiaStateChanged
        habilitarDestino();
    }//GEN-LAST:event_rbPropiaStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgDestino;
    private javax.swing.JComboBox cbBancos;
    private javax.swing.JComboBox cbCuentabancaria;
    private javax.swing.JComboBox cbCuentabancariaDestino;
    private javax.swing.JComboBox cbDestinoBancosCuentaPropia;
    private javax.swing.JComboBox cbDestinoBancosExternos;
    private com.toedter.calendar.JDateChooser dcFechaOperacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel labelMonto;
    private javax.swing.JRadioButton rbExterna;
    private javax.swing.JRadioButton rbPropia;
    private javax.swing.JTextField tfCuentaExterna;
    private javax.swing.JTextField tfDescripcionMov;
    private javax.swing.JTextField tfMonto;
    // End of variables declaration//GEN-END:variables

    private void habilitarDestino() {
        cbDestinoBancosCuentaPropia.setEnabled(!rbExterna.isSelected());
        cbCuentabancariaDestino.setEnabled(!rbExterna.isSelected());
        cbDestinoBancosExternos.setEnabled(rbExterna.isSelected());
        tfCuentaExterna.setEnabled(rbExterna.isSelected());
    }

    public JRadioButton getRbPropia() {
        return rbPropia;
    }

    public JRadioButton getRbExterna() {
        return rbExterna;
    }

    public JComboBox getCbCuentabancaria() {
        return cbCuentabancaria;
    }

    public JComboBox getCbCuentabancariaDestino() {
        return cbCuentabancariaDestino;
    }

    public JDateChooser getDcFechaOperacion() {
        return dcFechaOperacion;
    }

    public JTextField getTfCuentaExterna() {
        return tfCuentaExterna;
    }

    public JTextField getTfDescripcionMov() {
        return tfDescripcionMov;
    }

    public JTextField getTfMonto() {
        return tfMonto;
    }

    public JComboBox getCbDestinoBancosExternos() {
        return cbDestinoBancosExternos;
    }

    public void invertirOrigenDestino() {
        TitledBorder tb = (TitledBorder) jPanel1.getBorder();
        tb.setTitle("Destino");
        tb = (TitledBorder) jPanel2.getBorder();
        tb.setTitle("Origen");
    }
}
