package jgestion.gui;

import com.toedter.calendar.JDateChooser;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author FiruzzZ
 */
public class PanelDepositoCheque extends javax.swing.JPanel {

    /**
     * Creates new form PanelDepositoCheque
     */
    public PanelDepositoCheque() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        labelNcuentaawefwe = new javax.swing.JLabel();
        cbDepositoCuentaBancaria = new javax.swing.JComboBox();
        dcFechaOperacion = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        cbDepositoBancos = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        cbOperacionesBancarias = new javax.swing.JComboBox();
        tfDescripcion = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        dcFechaCreditoDebito = new com.toedter.calendar.JDateChooser();
        panelInfoCheque = new javax.swing.JPanel();
        labelEstado = new javax.swing.JLabel();
        labelEmisor = new javax.swing.JLabel();
        dcCheque = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        dcCobro = new com.toedter.calendar.JDateChooser();
        jLabel7 = new javax.swing.JLabel();
        checkEndosado = new javax.swing.JCheckBox();
        tfEndosatario = new javax.swing.JTextField();
        cbBancos = new javax.swing.JComboBox();
        cbCuentaBancaria = new javax.swing.JComboBox();
        cbChequeEstados = new javax.swing.JComboBox();
        dcEndoso = new com.toedter.calendar.JDateChooser();
        jLabel12 = new javax.swing.JLabel();
        labelNcuenta = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        tfNumero = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tfImporte = new javax.swing.JTextField();
        checkCruzado = new javax.swing.JCheckBox();
        cbEmisor = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        taObservacion = new javax.swing.JTextArea();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos de depósito", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N

        jLabel2.setText("Banco");

        labelNcuentaawefwe.setText("N° Cuenta");

        dcFechaOperacion.setMinSelectableDate(new java.util.Date(-2177433676000L));

        jLabel4.setText("F. Operación");

        jLabel1.setText("Operación");

        cbOperacionesBancarias.setEnabled(false);

        jLabel10.setText("Descripción");

        jLabel13.setText("F. Crédito/Débito");

        dcFechaCreditoDebito.setMinSelectableDate(new java.util.Date(-2177433676000L));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(labelNcuentaawefwe)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tfDescripcion)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dcFechaOperacion, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dcFechaCreditoDebito, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbDepositoCuentaBancaria, 0, 375, Short.MAX_VALUE)
                    .addComponent(cbOperacionesBancarias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbDepositoBancos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbDepositoBancos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labelNcuentaawefwe)
                    .addComponent(cbDepositoCuentaBancaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(dcFechaOperacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel13)
                    .addComponent(dcFechaCreditoDebito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbOperacionesBancarias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelInfoCheque.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Datos de cheque", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N

        labelEstado.setText("Estado");

        labelEmisor.setText("Cliente");

        dcCheque.setMinSelectableDate(new java.util.Date(-2177433676000L));

        jLabel6.setText("Observación");

        jLabel5.setText("Fecha de emisión");

        jLabel11.setText("Endosatario");

        dcCobro.setMinSelectableDate(new java.util.Date(-2177433676000L));

        jLabel7.setText("Fecha de Cobro");

        checkEndosado.setMnemonic('d');
        checkEndosado.setText("Endosado");

        tfEndosatario.setEnabled(false);

        dcEndoso.setEnabled(false);

        jLabel12.setText("Fecha de endoso");

        labelNcuenta.setText("N° Cuenta");

        jLabel3.setText("Banco");

        jLabel8.setText("N° de Cheque");

        tfNumero.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel9.setText("Importe");

        tfImporte.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        checkCruzado.setMnemonic('z');
        checkCruzado.setText("Cruzado");

        taObservacion.setColumns(20);
        taObservacion.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        taObservacion.setLineWrap(true);
        taObservacion.setRows(3);
        taObservacion.setWrapStyleWord(true);
        jScrollPane1.setViewportView(taObservacion);

        javax.swing.GroupLayout panelInfoChequeLayout = new javax.swing.GroupLayout(panelInfoCheque);
        panelInfoCheque.setLayout(panelInfoChequeLayout);
        panelInfoChequeLayout.setHorizontalGroup(
            panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoChequeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel8)
                    .addComponent(jLabel6)
                    .addComponent(labelEstado)
                    .addComponent(labelEmisor)
                    .addComponent(jLabel5)
                    .addComponent(jLabel12)
                    .addComponent(jLabel11)
                    .addComponent(labelNcuenta))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelInfoChequeLayout.createSequentialGroup()
                        .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelInfoChequeLayout.createSequentialGroup()
                                .addComponent(dcCheque, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dcCobro, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelInfoChequeLayout.createSequentialGroup()
                                .addComponent(checkEndosado)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkCruzado))
                            .addComponent(dcEndoso, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(41, 41, 41))
                    .addGroup(panelInfoChequeLayout.createSequentialGroup()
                        .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelInfoChequeLayout.createSequentialGroup()
                                .addComponent(tfNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfImporte, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(cbEmisor, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tfEndosatario)
                            .addComponent(jScrollPane1)
                            .addComponent(cbBancos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbCuentaBancaria, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbChequeEstados, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        panelInfoChequeLayout.setVerticalGroup(
            panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInfoChequeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbEmisor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelEmisor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel5)
                    .addComponent(dcCheque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcCobro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbBancos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labelNcuenta)
                    .addComponent(cbCuentaBancaria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(tfImporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbChequeEstados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelEstado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkEndosado)
                    .addComponent(checkCruzado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfEndosatario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInfoChequeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel12)
                    .addComponent(dcEndoso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelInfoCheque, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelInfoCheque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbBancos;
    private javax.swing.JComboBox cbChequeEstados;
    private javax.swing.JComboBox cbCuentaBancaria;
    private javax.swing.JComboBox cbDepositoBancos;
    private javax.swing.JComboBox cbDepositoCuentaBancaria;
    private javax.swing.JComboBox cbEmisor;
    private javax.swing.JComboBox cbOperacionesBancarias;
    private javax.swing.JCheckBox checkCruzado;
    private javax.swing.JCheckBox checkEndosado;
    private com.toedter.calendar.JDateChooser dcCheque;
    private com.toedter.calendar.JDateChooser dcCobro;
    private com.toedter.calendar.JDateChooser dcEndoso;
    private com.toedter.calendar.JDateChooser dcFechaCreditoDebito;
    private com.toedter.calendar.JDateChooser dcFechaOperacion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelEmisor;
    private javax.swing.JLabel labelEstado;
    private javax.swing.JLabel labelNcuenta;
    private javax.swing.JLabel labelNcuentaawefwe;
    private javax.swing.JPanel panelInfoCheque;
    private javax.swing.JTextArea taObservacion;
    private javax.swing.JTextField tfDescripcion;
    private javax.swing.JTextField tfEndosatario;
    private javax.swing.JTextField tfImporte;
    private javax.swing.JTextField tfNumero;
    // End of variables declaration//GEN-END:variables

    public JComboBox getCbBancos() {
        return cbBancos;
    }

    public JComboBox getCbChequeEstados() {
        return cbChequeEstados;
    }

    public JComboBox getCbCuentaBancaria() {
        return cbCuentaBancaria;
    }

    public JComboBox getCbDepositoBancos() {
        return cbDepositoBancos;
    }

    public JComboBox getCbDepositoCuentaBancaria() {
        return cbDepositoCuentaBancaria;
    }

    public JComboBox getCbEmisor() {
        return cbEmisor;
    }

    public JComboBox getCbOperacionesBancarias() {
        return cbOperacionesBancarias;
    }

    public JCheckBox getCheckCruzado() {
        return checkCruzado;
    }

    public JCheckBox getCheckEndosado() {
        return checkEndosado;
    }

    public JDateChooser getDcCheque() {
        return dcCheque;
    }

    public JDateChooser getDcCobro() {
        return dcCobro;
    }

    public JDateChooser getDcEndoso() {
        return dcEndoso;
    }

    public JDateChooser getDcFechaOperacion() {
        return dcFechaOperacion;
    }

    public JLabel getLabelNcuenta() {
        return labelNcuenta;
    }

    public JLabel getLabelEmisor() {
        return labelEmisor;
    }

    public JTextArea getTaObservacion() {
        return taObservacion;
    }

    public JTextField getTfEndosatario() {
        return tfEndosatario;
    }

    public JTextField getTfImporte() {
        return tfImporte;
    }

    public JTextField getTfNumero() {
        return tfNumero;
    }

    public JTextField getTfDescripcion() {
        return tfDescripcion;
    }

    public JDateChooser getDcFechaCreditoDebito() {
        return dcFechaCreditoDebito;
    }

    public JPanel getPanelInfoCheque() {
        return panelInfoCheque;
    }
}
