
/*
 * PanelABMSucursal.java
 *
 * Created on 18/02/2010, 11:25:07
 */
package gui;

import utilities.general.UTIL;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import utilities.gui.SwingUtil;

/**
 *
 * @author FiruzzZ
 */
public class PanelABMSucursal extends javax.swing.JPanel {

    /**
     * Creates new form PanelABMSucursal Cargar comboBox Provincia Setear Listener
     */
    public PanelABMSucursal() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        cbProvincias = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        cbDepartamentos = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        tfDireccion = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        tfTele1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        tfTele2 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        tfInterno1 = new javax.swing.JTextField();
        tfInterno2 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfNombre = new javax.swing.JTextField();
        tfEncargado = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        tfEmail = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        cbMunicipios = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        tfPuntoVenta = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        tfInicialFacturaB = new javax.swing.JTextField();
        tfInicialFacturaA = new javax.swing.JTextField();
        tfInicialRemito = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        tfInicialRecibo = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        tfInicialNotaCredito = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        btnVerNumeracionActual = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        tfInicialNotaDebitoA = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        tfInicialNotaDebitoB = new javax.swing.JTextField();

        jLabel6.setText("Provincia");

        cbProvincias.setName("cbProvincias"); // NOI18N

        jLabel7.setText("Depto");

        cbDepartamentos.setName("cbDepartamentos"); // NOI18N

        jLabel8.setText("Dirección");

        jLabel4.setText("Teléfono 1");

        tfTele1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfTele1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfTele1KeyTyped(evt);
            }
        });

        jLabel5.setText("Teléfono 2");

        tfTele2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfTele2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfTele2KeyTyped(evt);
            }
        });

        jLabel9.setText("Int.");

        tfInterno1.setColumns(4);
        tfInterno1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInterno1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInterno1KeyTyped(evt);
            }
        });

        tfInterno2.setColumns(4);
        tfInterno2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInterno2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInterno2KeyTyped(evt);
            }
        });

        jLabel10.setText("Int.");

        jLabel1.setText("Nombre");

        jLabel2.setText("Encargado");

        tfNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfNombreKeyTyped(evt);
            }
        });

        jLabel11.setText("Email");

        jLabel12.setText("Municipios");

        cbMunicipios.setName("cbMunicipios"); // NOI18N

        jLabel3.setText("Punto de Venta");

        tfPuntoVenta.setColumns(4);
        tfPuntoVenta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfPuntoVenta.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfPuntoVentaFocusLost(evt);
            }
        });
        tfPuntoVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfPuntoVentaKeyTyped(evt);
            }
        });

        jLabel13.setText("Numeración Inicial");

        tfInicialFacturaB.setColumns(8);
        tfInicialFacturaB.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialFacturaB.setText("1");
        tfInicialFacturaB.setSelectionStart(0);
        tfInicialFacturaB.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfInicialFacturaBFocusLost(evt);
            }
        });
        tfInicialFacturaB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInicialFacturaBKeyTyped(evt);
            }
        });

        tfInicialFacturaA.setColumns(8);
        tfInicialFacturaA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialFacturaA.setText("1");
        tfInicialFacturaA.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfInicialFacturaAFocusLost(evt);
            }
        });
        tfInicialFacturaA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInicialFacturaAKeyTyped(evt);
            }
        });

        tfInicialRemito.setColumns(8);
        tfInicialRemito.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialRemito.setText("1");
        tfInicialRemito.setSelectionStart(0);
        tfInicialRemito.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfInicialRemitoFocusLost(evt);
            }
        });
        tfInicialRemito.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInicialRemitoKeyTyped(evt);
            }
        });

        jLabel16.setText("Factura \"A\"");

        jLabel17.setText("Factura \"B\"");

        jLabel18.setText("Remito");

        jLabel19.setText("Recibo");

        tfInicialRecibo.setColumns(8);
        tfInicialRecibo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialRecibo.setText("1");
        tfInicialRecibo.setSelectionStart(0);
        tfInicialRecibo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfInicialReciboFocusLost(evt);
            }
        });
        tfInicialRecibo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInicialReciboKeyTyped(evt);
            }
        });

        jLabel20.setText("Nota Crédito");

        tfInicialNotaCredito.setColumns(8);
        tfInicialNotaCredito.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaCredito.setText("1");
        tfInicialNotaCredito.setSelectionStart(0);
        tfInicialNotaCredito.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfInicialNotaCreditoFocusLost(evt);
            }
        });
        tfInicialNotaCredito.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInicialNotaCreditoKeyTyped(evt);
            }
        });

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_blue_system-help.png"))); // NOI18N
        jLabel21.setText("Ayuda");

        btnVerNumeracionActual.setText("Ver Numeración actual");
        btnVerNumeracionActual.setName("verNumeracionActual"); // NOI18N

        jLabel14.setForeground(new java.awt.Color(255, 0, 51));
        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_blue_system-help.png"))); // NOI18N
        jLabel14.setText("<html>Una vez que se haya cargado el primer comprobante, la numeración inicial no podrás ser re-configurada con un número <b>anterior</b> a algún comprobante existente. (Doble click para ver ejemplo).</html>");
        jLabel14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel14MouseClicked(evt);
            }
        });

        jLabel22.setText("Nota Débito \"A\"");

        tfInicialNotaDebitoA.setColumns(8);
        tfInicialNotaDebitoA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaDebitoA.setText("1");
        tfInicialNotaDebitoA.setSelectionStart(0);
        tfInicialNotaDebitoA.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfInicialNotaDebitoAFocusLost(evt);
            }
        });
        tfInicialNotaDebitoA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInicialNotaDebitoAKeyTyped(evt);
            }
        });

        jLabel23.setText("Nota Débito \"B\"");

        tfInicialNotaDebitoB.setColumns(8);
        tfInicialNotaDebitoB.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaDebitoB.setText("1");
        tfInicialNotaDebitoB.setSelectionStart(0);
        tfInicialNotaDebitoB.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfInicialNotaDebitoBFocusLost(evt);
            }
        });
        tfInicialNotaDebitoB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInicialNotaDebitoBKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel8)
                            .addComponent(jLabel5)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11)
                            .addComponent(jLabel3)
                            .addComponent(jLabel13)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbMunicipios, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbDepartamentos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbProvincias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tfEmail)
                            .addComponent(tfDireccion)
                            .addComponent(tfNombre)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfInicialFacturaA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInicialFacturaB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInicialRemito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInicialNotaDebitoA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfInicialNotaCredito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInicialRecibo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInicialNotaDebitoB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 51, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(tfTele2)
                                        .addComponent(tfTele1, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                                    .addGap(18, 18, 18)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel9)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(tfInterno1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(jLabel10)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(tfInterno2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel21)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnVerNumeracionActual))
                                .addComponent(tfPuntoVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tfEncargado))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tfPuntoVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel21)
                    .addComponent(btnVerNumeracionActual))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel16)
                    .addComponent(tfInicialFacturaA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(tfInicialRecibo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel17)
                    .addComponent(tfInicialFacturaB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(tfInicialNotaCredito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfInicialRemito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(tfInicialNotaDebitoA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(tfInicialNotaDebitoB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfEncargado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(tfTele1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfTele2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(tfInterno1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(tfInterno2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cbProvincias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cbDepartamentos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbMunicipios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tfTele1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTele1KeyTyped
        SwingUtil.checkInputDigit(evt, 12);
}//GEN-LAST:event_tfTele1KeyTyped

    private void tfTele2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTele2KeyTyped
        SwingUtil.checkInputDigit(evt, 12);
}//GEN-LAST:event_tfTele2KeyTyped

    private void tfInterno1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInterno1KeyTyped
        SwingUtil.checkInputDigit(evt, 4);
    }//GEN-LAST:event_tfInterno1KeyTyped

    private void tfInterno2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInterno2KeyTyped
        SwingUtil.checkInputDigit(evt, 4);
    }//GEN-LAST:event_tfInterno2KeyTyped

    private void tfNombreKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfNombreKeyTyped
        evt.setKeyChar(String.valueOf(evt.getKeyChar()).toUpperCase().charAt(0));
    }//GEN-LAST:event_tfNombreKeyTyped

    private void tfPuntoVentaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPuntoVentaKeyTyped
        SwingUtil.checkInputDigit(evt, 4);
    }//GEN-LAST:event_tfPuntoVentaKeyTyped

    private void tfPuntoVentaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfPuntoVentaFocusLost
        UTIL.AGREGAR_CEROS(((JTextField) evt.getSource()).getText(), 8);
    }//GEN-LAST:event_tfPuntoVentaFocusLost

    private void tfInicialFacturaBFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfInicialFacturaBFocusLost
        UTIL.AGREGAR_CEROS(((JTextField) evt.getSource()).getText(), 8);
    }//GEN-LAST:event_tfInicialFacturaBFocusLost

    private void tfInicialFacturaBKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInicialFacturaBKeyTyped
        SwingUtil.checkInputDigit(evt, 8);
    }//GEN-LAST:event_tfInicialFacturaBKeyTyped

    private void tfInicialFacturaAFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfInicialFacturaAFocusLost
        UTIL.AGREGAR_CEROS(((JTextField) evt.getSource()).getText(), 8);
    }//GEN-LAST:event_tfInicialFacturaAFocusLost

    private void tfInicialFacturaAKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInicialFacturaAKeyTyped
        SwingUtil.checkInputDigit(evt, 8);
    }//GEN-LAST:event_tfInicialFacturaAKeyTyped

    private void tfInicialRemitoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInicialRemitoKeyTyped
        SwingUtil.checkInputDigit(evt, 8);
    }//GEN-LAST:event_tfInicialRemitoKeyTyped

    private void tfInicialReciboFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfInicialReciboFocusLost
        UTIL.AGREGAR_CEROS(((JTextField) evt.getSource()).getText(), 8);
    }//GEN-LAST:event_tfInicialReciboFocusLost

    private void tfInicialReciboKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInicialReciboKeyTyped
        SwingUtil.checkInputDigit(evt, 8);
    }//GEN-LAST:event_tfInicialReciboKeyTyped

    private void tfInicialNotaCreditoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfInicialNotaCreditoFocusLost
        UTIL.AGREGAR_CEROS(((JTextField) evt.getSource()).getText(), 8);
    }//GEN-LAST:event_tfInicialNotaCreditoFocusLost

    private void tfInicialNotaCreditoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInicialNotaCreditoKeyTyped
        SwingUtil.checkInputDigit(evt, 8);
    }//GEN-LAST:event_tfInicialNotaCreditoKeyTyped

    private void tfInicialRemitoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfInicialRemitoFocusLost
        UTIL.AGREGAR_CEROS(((JTextField) evt.getSource()).getText(), 8);

    }//GEN-LAST:event_tfInicialRemitoFocusLost

    private void jLabel14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel14MouseClicked
        if (evt.getClickCount() > 1) {
            JOptionPane.showMessageDialog(this,
                    "<html>Si se configuró para que las Facturas \"A\" comiencen a partir del 100"
                    + "<br>y esta fue emitida (generada) por el sistema no se podrá ingresar"
                    + "<br>un valor menor a <b>101</b> en el campo Facturas \"A\""
                    + "</html>", "Ejemplo", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jLabel14MouseClicked

    private void tfInicialNotaDebitoAFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfInicialNotaDebitoAFocusLost
        UTIL.AGREGAR_CEROS(((JTextField) evt.getSource()).getText(), 8);
    }//GEN-LAST:event_tfInicialNotaDebitoAFocusLost

    private void tfInicialNotaDebitoAKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInicialNotaDebitoAKeyTyped
        SwingUtil.checkInputDigit(evt, 8);
    }//GEN-LAST:event_tfInicialNotaDebitoAKeyTyped

    private void tfInicialNotaDebitoBFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfInicialNotaDebitoBFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_tfInicialNotaDebitoBFocusLost

    private void tfInicialNotaDebitoBKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInicialNotaDebitoBKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_tfInicialNotaDebitoBKeyTyped
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnVerNumeracionActual;
    private javax.swing.JComboBox cbDepartamentos;
    private javax.swing.JComboBox cbMunicipios;
    private javax.swing.JComboBox cbProvincias;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField tfDireccion;
    private javax.swing.JTextField tfEmail;
    private javax.swing.JTextField tfEncargado;
    private javax.swing.JTextField tfInicialFacturaA;
    private javax.swing.JTextField tfInicialFacturaB;
    private javax.swing.JTextField tfInicialNotaCredito;
    private javax.swing.JTextField tfInicialNotaDebitoA;
    private javax.swing.JTextField tfInicialNotaDebitoB;
    private javax.swing.JTextField tfInicialRecibo;
    private javax.swing.JTextField tfInicialRemito;
    private javax.swing.JTextField tfInterno1;
    private javax.swing.JTextField tfInterno2;
    private javax.swing.JTextField tfNombre;
    private javax.swing.JTextField tfPuntoVenta;
    private javax.swing.JTextField tfTele1;
    private javax.swing.JTextField tfTele2;
    // End of variables declaration//GEN-END:variables

    public void setListener(ActionListener o) {
        cbProvincias.addActionListener(o);
        cbDepartamentos.addActionListener(o);
        btnVerNumeracionActual.addActionListener(o);
    }

    // <editor-fold defaultstate="collapsed" desc="SETTERS">
    public void setTfDireccion(String tfDireccion) {
        this.tfDireccion.setText(tfDireccion);
    }

    public void setTfEmail(String tfEmail) {
        this.tfEmail.setText(tfEmail);
    }

    public void setTfEncargado(String tfEncargado) {
        this.tfEncargado.setText(tfEncargado);
    }

    public void setTfInterno1(String tfInterno1) {
        this.tfInterno1.setText(tfInterno1);
    }

    public void setTfInterno2(String tfInterno2) {
        this.tfInterno2.setText(tfInterno2);
    }

    public void setTfNombre(String tfNombre) {
        this.tfNombre.setText(tfNombre);
    }

    public void setTfTele1(String tfTele1) {
        this.tfTele1.setText(tfTele1);
    }

    public void setTfTele2(String tfTele2) {
        this.tfTele2.setText(tfTele2);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="GETTERS">
    public JComboBox getCbDepartamentos() {
        return cbDepartamentos;
    }

    public JComboBox getCbProvincias() {
        return cbProvincias;
    }

    public JComboBox getCbMunicipios() {
        return cbMunicipios;
    }

    public Object getSelectedMunicipio() {
        return cbMunicipios.getSelectedItem();
    }

    public Object getSelectedDepartamento() {
        return cbDepartamentos.getSelectedItem();
    }

    public Object getSelectedProvincia() {
        return cbProvincias.getSelectedItem();
    }

    public String getTfDireccion() {
        return tfDireccion.getText().trim();
    }

    public String getTfEmail() {
        return tfEmail.getText().trim();
    }

    public String getTfEncargado() {
        return tfEncargado.getText().trim();
    }

    public String getTfInterno1() {
        return tfInterno1.getText().trim();
    }

    public String getTfInterno2() {
        return tfInterno2.getText().trim();
    }

    public String getTfNombre() {
        return tfNombre.getText().trim();
    }

    public String getTfTele1() {
        return tfTele1.getText().trim();
    }

    public String getTfTele2() {
        return tfTele2.getText().trim();
    }

    public JTextField getTfPuntoVenta() {
        return tfPuntoVenta;
    }

    public JTextField getTfInicialFacturaA() {
        return tfInicialFacturaA;
    }

    public JTextField getTfInicialFacturaB() {
        return tfInicialFacturaB;
    }

    public JTextField getTfInicialNotaCredito() {
        return tfInicialNotaCredito;
    }

    public JTextField getTfInicialNotaDebitoA() {
        return tfInicialNotaDebitoA;
    }

    public JTextField getTfInicialNotaDebitoB() {
        return tfInicialNotaDebitoB;
    }

    public JTextField getTfInicialRecibo() {
        return tfInicialRecibo;
    }

    public JTextField getTfInicialRemito() {
        return tfInicialRemito;
    }
    // </editor-fold>
}
