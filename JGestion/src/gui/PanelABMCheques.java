/*
 * PanelABMCheques.java
 *
 * Created on 20/09/2011, 11:44:12
 */
package gui;

import com.toedter.calendar.JDateChooser;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author FiruzzZ
 */
public class PanelABMCheques extends javax.swing.JPanel {

    /** Creates new form PanelABMCheques */
    public PanelABMCheques() {
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
        tfNumero = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cbBancos = new javax.swing.JComboBox();
        bAddBanco = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        cbPlazas = new javax.swing.JComboBox();
        bAddPlaza = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        dcIssued = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        dcExpiration = new com.toedter.calendar.JDateChooser();
        labelIssuer = new javax.swing.JLabel();
        checkPropio = new javax.swing.JCheckBox();
        cbIssuer = new javax.swing.JComboBox();
        labelReceptor = new javax.swing.JLabel();
        checkAlPortador = new javax.swing.JCheckBox();
        cbReceptor = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        tfImporte = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tfEstado = new javax.swing.JTextField();
        checkCruzado = new javax.swing.JCheckBox();
        checkEndosado = new javax.swing.JCheckBox();

        jLabel1.setText("N°");

        jLabel2.setText("Banco");

        bAddBanco.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_add.png"))); // NOI18N
        bAddBanco.setName("Marcas"); // NOI18N

        jLabel3.setText("Plaza");

        bAddPlaza.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_add.png"))); // NOI18N
        bAddPlaza.setName("Marcas"); // NOI18N

        jLabel4.setText("Emisión");

        jLabel5.setText("Vencimiento");

        labelIssuer.setText("Emisor");

        checkPropio.setText("Propio");

        labelReceptor.setText("Receptor");

        checkAlPortador.setText("Al portador");

        jLabel7.setText("Importe");

        tfImporte.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfImporte.setText("0.00");

        jLabel9.setText("Estado");

        tfEstado.setFont(new java.awt.Font("Tahoma", 1, 11));
        tfEstado.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tfEstado.setText("DEPOSITADO");

        checkCruzado.setText("Cruzado");

        checkEndosado.setText("Endosado");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(labelReceptor)
                    .addComponent(labelIssuer)
                    .addComponent(jLabel3)
                    .addComponent(jLabel9)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(tfEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(17, 17, 17)
                                .addComponent(checkCruzado)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkEndosado))
                            .addComponent(tfImporte, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(79, 79, 79))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(cbReceptor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkAlPortador, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbPlazas, javax.swing.GroupLayout.Alignment.LEADING, 0, 305, Short.MAX_VALUE)
                            .addComponent(tfNumero, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(dcIssued, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dcExpiration, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cbBancos, javax.swing.GroupLayout.Alignment.LEADING, 0, 305, Short.MAX_VALUE)
                            .addComponent(checkPropio, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbIssuer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bAddBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bAddPlaza, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(cbBancos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bAddBanco, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbPlazas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel4)
                            .addComponent(dcExpiration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(dcIssued, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelIssuer)
                            .addComponent(checkPropio))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbIssuer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelReceptor)
                            .addComponent(checkAlPortador))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbReceptor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfImporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)
                            .addComponent(checkCruzado)
                            .addComponent(checkEndosado))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)))
                    .addComponent(bAddPlaza, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public JButton getbAddBanco() {
        return bAddBanco;
    }

    public JButton getbAddPlaza() {
        return bAddPlaza;
    }

    public JComboBox getCbBancos() {
        return cbBancos;
    }

    public JComboBox getCbIssuer() {
        return cbIssuer;
    }

    public JComboBox getCbPlazas() {
        return cbPlazas;
    }

    public JComboBox getCbReceptor() {
        return cbReceptor;
    }

    public JCheckBox getCheckAlPortador() {
        return checkAlPortador;
    }

    public JCheckBox getCheckCruzado() {
        return checkCruzado;
    }

    public JCheckBox getCheckEndosado() {
        return checkEndosado;
    }

    public JCheckBox getCheckPropio() {
        return checkPropio;
    }

    public JDateChooser getDcExpiration() {
        return dcExpiration;
    }

    public JDateChooser getDcIssued() {
        return dcIssued;
    }

    public JTextField getTfEstado() {
        return tfEstado;
    }

    public JTextField getTfImporte() {
        return tfImporte;
    }

    public JTextField getTfNumero() {
        return tfNumero;
    }

    public JLabel getLabelIssuer() {
        return labelIssuer;
    }

    public JLabel getLabelReceptor() {
        return labelReceptor;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAddBanco;
    private javax.swing.JButton bAddPlaza;
    private javax.swing.JComboBox cbBancos;
    private javax.swing.JComboBox cbIssuer;
    private javax.swing.JComboBox cbPlazas;
    private javax.swing.JComboBox cbReceptor;
    private javax.swing.JCheckBox checkAlPortador;
    private javax.swing.JCheckBox checkCruzado;
    private javax.swing.JCheckBox checkEndosado;
    private javax.swing.JCheckBox checkPropio;
    private com.toedter.calendar.JDateChooser dcExpiration;
    private com.toedter.calendar.JDateChooser dcIssued;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel labelIssuer;
    private javax.swing.JLabel labelReceptor;
    private javax.swing.JTextField tfEstado;
    private javax.swing.JTextField tfImporte;
    private javax.swing.JTextField tfNumero;
    // End of variables declaration//GEN-END:variables
}
