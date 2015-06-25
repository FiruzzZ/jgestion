
/*
 * PanelABMSucursal.java
 *
 * Created on 18/02/2010, 11:25:07
 */
package jgestion.gui;

import utilities.general.UTIL;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
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
        tfInicialReciboA = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        tfInicialNotaCreditoA = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        btnVerNumeracionActual = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        tfInicialNotaDebitoA = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        tfInicialNotaDebitoB = new javax.swing.JTextField();
        checkWebServices = new javax.swing.JCheckBox();
        jLabel24 = new javax.swing.JLabel();
        tfInicialFacturaC = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        tfInicialReciboB = new javax.swing.JTextField();
        tfInicialReciboC = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        tfInicialNotaCreditoB = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        tfInicialNotaCreditoC = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        tfInicialNotaDebitoC = new javax.swing.JTextField();

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

        jLabel2.setText("Responsable");

        tfNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfNombreKeyTyped(evt);
            }
        });

        jLabel11.setText("Email");

        jLabel12.setText("Municipios");

        cbMunicipios.setName("cbMunicipios"); // NOI18N

        jLabel3.setText("Punto de Venta");

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

        tfInicialFacturaA.setColumns(8);
        tfInicialFacturaA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialFacturaA.setText("1");

        tfInicialRemito.setColumns(8);
        tfInicialRemito.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialRemito.setText("1");
        tfInicialRemito.setSelectionStart(0);
        tfInicialRemito.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInicialRemitoKeyTyped(evt);
            }
        });

        jLabel16.setText("Facturas: \"A\"");

        jLabel17.setText("\"B\"");

        jLabel18.setText("Remito");

        jLabel19.setText("Recibos: \"A\"");

        tfInicialReciboA.setColumns(8);
        tfInicialReciboA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialReciboA.setText("1");
        tfInicialReciboA.setSelectionStart(0);

        jLabel20.setText("Nota Crédito: \"A\"");

        tfInicialNotaCreditoA.setColumns(8);
        tfInicialNotaCreditoA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaCreditoA.setText("1");
        tfInicialNotaCreditoA.setSelectionStart(0);

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

        jLabel22.setText("Nota Débito: \"A\"");

        tfInicialNotaDebitoA.setColumns(8);
        tfInicialNotaDebitoA.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaDebitoA.setText("1");
        tfInicialNotaDebitoA.setSelectionStart(0);

        jLabel23.setText("\"B\"");

        tfInicialNotaDebitoB.setColumns(8);
        tfInicialNotaDebitoB.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaDebitoB.setText("1");
        tfInicialNotaDebitoB.setSelectionStart(0);

        checkWebServices.setText("habilitado para Facturación Electrónica (AFIP)");

        jLabel24.setText("\"C\"");

        tfInicialFacturaC.setColumns(8);
        tfInicialFacturaC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialFacturaC.setText("1");
        tfInicialFacturaC.setSelectionStart(0);

        jLabel25.setText("\"B\"");

        jLabel26.setText("\"C\"");

        tfInicialReciboB.setColumns(8);
        tfInicialReciboB.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialReciboB.setText("1");
        tfInicialReciboB.setSelectionStart(0);

        tfInicialReciboC.setColumns(8);
        tfInicialReciboC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialReciboC.setText("1");
        tfInicialReciboC.setSelectionStart(0);

        jLabel27.setText("\"B\"");

        tfInicialNotaCreditoB.setColumns(8);
        tfInicialNotaCreditoB.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaCreditoB.setText("1");
        tfInicialNotaCreditoB.setSelectionStart(0);

        jLabel28.setText("\"C\"");

        tfInicialNotaCreditoC.setColumns(8);
        tfInicialNotaCreditoC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaCreditoC.setText("1");
        tfInicialNotaCreditoC.setSelectionStart(0);

        jLabel29.setText("\"C\"");

        tfInicialNotaDebitoC.setColumns(8);
        tfInicialNotaDebitoC.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfInicialNotaDebitoC.setText("1");
        tfInicialNotaDebitoC.setSelectionStart(0);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel22, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbMunicipios, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbDepartamentos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbProvincias, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tfEmail)
                    .addComponent(tfDireccion)
                    .addComponent(tfNombre)
                    .addComponent(tfEncargado)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfInicialNotaCreditoA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel27)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfInicialNotaCreditoB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel28)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfInicialNotaCreditoC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                            .addComponent(checkWebServices)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfInicialFacturaA, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfInicialRemito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel17)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfInicialFacturaB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfInicialFacturaC, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfInicialReciboA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel25)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfInicialReciboB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfInicialReciboC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfInicialNotaDebitoA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfInicialNotaDebitoB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel29)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfInicialNotaDebitoC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tfPuntoVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 107, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(10, 10, 10))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkWebServices)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                    .addComponent(jLabel17)
                    .addComponent(tfInicialFacturaB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel24)
                        .addComponent(tfInicialFacturaC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel19)
                    .addComponent(tfInicialReciboA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(tfInicialReciboB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel26)
                        .addComponent(tfInicialReciboC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfInicialNotaCreditoA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(jLabel27)
                    .addComponent(tfInicialNotaCreditoB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28)
                    .addComponent(tfInicialNotaCreditoC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(tfInicialNotaDebitoA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(tfInicialNotaDebitoB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(tfInicialNotaDebitoC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfInicialRemito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void tfInicialRemitoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInicialRemitoKeyTyped
        SwingUtil.checkInputDigit(evt, 8);
    }//GEN-LAST:event_tfInicialRemitoKeyTyped

    private void jLabel14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel14MouseClicked
        if (evt.getClickCount() > 1) {
            JOptionPane.showMessageDialog(this,
                    "<html>Si se configuró para que las Facturas \"A\" comiencen a partir del 100"
                    + "<br>y esta fue emitida (generada) por el sistema, posteriormente no se podrá ingresar"
                    + "<br>un valor menor a <b>101</b> en el campo Facturas \"A\""
                    + "</html>", "Ejemplo", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jLabel14MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnVerNumeracionActual;
    private javax.swing.JComboBox cbDepartamentos;
    private javax.swing.JComboBox cbMunicipios;
    private javax.swing.JComboBox cbProvincias;
    private javax.swing.JCheckBox checkWebServices;
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
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
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
    private javax.swing.JTextField tfInicialFacturaC;
    private javax.swing.JTextField tfInicialNotaCreditoA;
    private javax.swing.JTextField tfInicialNotaCreditoB;
    private javax.swing.JTextField tfInicialNotaCreditoC;
    private javax.swing.JTextField tfInicialNotaDebitoA;
    private javax.swing.JTextField tfInicialNotaDebitoB;
    private javax.swing.JTextField tfInicialNotaDebitoC;
    private javax.swing.JTextField tfInicialReciboA;
    private javax.swing.JTextField tfInicialReciboB;
    private javax.swing.JTextField tfInicialReciboC;
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

    public JTextField getTfInicialFacturaC() {
        return tfInicialFacturaC;
    }

    public JTextField getTfInicialNotaCreditoB() {
        return tfInicialNotaCreditoB;
    }

    public JTextField getTfInicialNotaCreditoC() {
        return tfInicialNotaCreditoC;
    }

    public JTextField getTfInicialNotaDebitoC() {
        return tfInicialNotaDebitoC;
    }

    public JTextField getTfInicialReciboB() {
        return tfInicialReciboB;
    }

    public JTextField getTfInicialReciboC() {
        return tfInicialReciboC;
    }

    public JTextField getTfInicialNotaCreditoA() {
        return tfInicialNotaCreditoA;
    }

    public JTextField getTfInicialNotaDebitoA() {
        return tfInicialNotaDebitoA;
    }

    public JTextField getTfInicialNotaDebitoB() {
        return tfInicialNotaDebitoB;
    }

    public JTextField getTfInicialReciboA() {
        return tfInicialReciboA;
    }

    public JTextField getTfInicialRemito() {
        return tfInicialRemito;
    }

    public JCheckBox getCheckWebServices() {
        return checkWebServices;
    }
    // </editor-fold>
}
