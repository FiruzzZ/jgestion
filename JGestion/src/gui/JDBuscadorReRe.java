/*
 * JDBuscadorReRe.java
 *
 * Created on 05/04/2010, 12:13:39
 */
package gui;

import utilities.general.UTIL;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilities.gui.SwingUtil;

/**
 *
 * @author FiruzzZ
 */
public class JDBuscadorReRe extends javax.swing.JDialog {

    public JDBuscadorReRe(Window owner, String title, boolean modal, String labelClieProv, String labelReRe) {
        super(owner, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        initComponents();
        init(title, labelClieProv, labelReRe);
    }

    private void init(String title, String labelClieProv, String labelReRe) {
        this.setTitle(title);
        bImprimir.setVisible(false);
        this.labelClieProv.setText(labelClieProv);
        this.labelReRe.setText(labelReRe);
        rootPane.setDefaultButton(bBuscar);
        if (getOwner() != null) {
            this.setLocationRelativeTo(getOwner());
            setLocation(getOwner().getX() + 100, getY() + 50);
        }

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
        labelCaja = new javax.swing.JLabel();
        cbCaja = new javax.swing.JComboBox();
        cbClieProv = new javax.swing.JComboBox();
        labelClieProv = new javax.swing.JLabel();
        cbSucursal = new javax.swing.JComboBox();
        labelSucursal = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        dcDesde = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        dcHasta = new com.toedter.calendar.JDateChooser();
        tfOcteto = new javax.swing.JTextField();
        labelReRe = new javax.swing.JLabel();
        tfCuarto = new javax.swing.JTextField();
        labelN_Factura = new javax.swing.JLabel();
        tfFactu4 = new javax.swing.JTextField();
        tfFactu8 = new javax.swing.JTextField();
        checkAnulada = new javax.swing.JCheckBox();
        labelFormasDePago = new javax.swing.JLabel();
        cbFormasDePago = new javax.swing.JComboBox();
        dcDesdeSistema = new com.toedter.calendar.JDateChooser();
        jLabel4 = new javax.swing.JLabel();
        dcHastaSistema = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        labelUnidadDeNegocio = new javax.swing.JLabel();
        labelCuenta = new javax.swing.JLabel();
        cbUnidadDeNegocio = new javax.swing.JComboBox();
        cbCuenta = new javax.swing.JComboBox();
        labelSubCuenta = new javax.swing.JLabel();
        cbSubCuenta = new javax.swing.JComboBox();
        labelVendedor = new javax.swing.JLabel();
        cbVendedor = new javax.swing.JComboBox();
        bLimpiar = new javax.swing.JButton();
        bBuscar = new javax.swing.JButton();
        bImprimir = new javax.swing.JButton();
        bExtra = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        btnToExcel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Filtro"));

        labelCaja.setText("Caja");

        labelClieProv.setText("Clie/provee");

        labelSucursal.setText("Sucursal");

        jLabel2.setText("Desde");

        jLabel3.setText("Hasta");

        tfOcteto.setColumns(8);
        tfOcteto.setName("tfocteto"); // NOI18N
        tfOcteto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfOctetoFocusLost(evt);
            }
        });
        tfOcteto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfOctetoKeyTyped(evt);
            }
        });

        labelReRe.setText("Nº ReReRe");

        tfCuarto.setColumns(4);
        tfCuarto.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfCuartoFocusLost(evt);
            }
        });
        tfCuarto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfCuartoKeyTyped(evt);
            }
        });

        labelN_Factura.setText("Nº Factura");

        tfFactu4.setColumns(4);
        tfFactu4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfFactu4KeyTyped(evt);
            }
        });

        tfFactu8.setColumns(8);
        tfFactu8.setName("tfFactu8"); // NOI18N
        tfFactu8.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfFactu8KeyTyped(evt);
            }
        });

        checkAnulada.setText("Solo anuladas");

        labelFormasDePago.setText("Forma pago");

        jLabel4.setText("Sistema Desde");

        jLabel5.setText("Sistema Hasta");

        labelUnidadDeNegocio.setText("Unid. de Neg.");

        labelCuenta.setText("Cuenta");

        labelSubCuenta.setText("SubCuenta");

        labelVendedor.setText("Vendedor");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelCaja)
                    .addComponent(labelReRe)
                    .addComponent(labelN_Factura)
                    .addComponent(labelFormasDePago))
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(tfFactu4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfFactu8, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(tfCuarto, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfOcteto, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(cbFormasDePago, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelSucursal)
                            .addComponent(labelClieProv)
                            .addComponent(labelUnidadDeNegocio)
                            .addComponent(labelCuenta)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(checkAnulada)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelSubCuenta)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbSubCuenta, 0, 220, Short.MAX_VALUE)
                    .addComponent(cbUnidadDeNegocio, 0, 220, Short.MAX_VALUE)
                    .addComponent(cbSucursal, 0, 220, Short.MAX_VALUE)
                    .addComponent(cbCuenta, 0, 220, Short.MAX_VALUE)
                    .addComponent(cbClieProv, 0, 220, Short.MAX_VALUE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelVendedor, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dcDesdeSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbVendedor, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addComponent(dcHastaSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel3)
                            .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbClieProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfCuarto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelReRe)
                            .addComponent(tfOcteto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelClieProv))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(cbCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelCaja)
                            .addComponent(labelUnidadDeNegocio)
                            .addComponent(cbUnidadDeNegocio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labelN_Factura)
                    .addComponent(tfFactu4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfFactu8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(dcDesdeSistema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelSucursal)
                    .addComponent(cbSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel5)
                    .addComponent(dcHastaSistema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelFormasDePago)
                    .addComponent(cbFormasDePago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelCuenta)
                    .addComponent(cbCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(checkAnulada)
                    .addComponent(cbSubCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelSubCuenta)
                    .addComponent(labelVendedor)
                    .addComponent(cbVendedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        bLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/broom32x32.png"))); // NOI18N
        bLimpiar.setText("Limpiar");
        bLimpiar.setFocusable(false);
        bLimpiar.setName("limpiarReRe"); // NOI18N
        bLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bLimpiarActionPerformed(evt);
            }
        });

        bBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        bBuscar.setMnemonic('b');
        bBuscar.setText("Buscar");
        bBuscar.setName("filtrarReRe"); // NOI18N

        bImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/impresora.png"))); // NOI18N
        bImprimir.setMnemonic('i');
        bImprimir.setText("Imprimir");
        bImprimir.setName("imprimirFiltro"); // NOI18N

        bExtra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px_configure.png"))); // NOI18N
        bExtra.setVisible(false);
        bExtra.setMnemonic('e');
        bExtra.setText("Editar");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jTable1ComponentResized(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel1.setText("Nº Registros: 0");

        btnToExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px_excel.png"))); // NOI18N
        btnToExcel.setMnemonic('x');
        btnToExcel.setText("A Excel");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bImprimir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bLimpiar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bExtra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnToExcel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bExtra, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnToExcel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLimpiarActionPerformed
        limpiarVentana();
}//GEN-LAST:event_bLimpiarActionPerformed

    private void tfCuartoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCuartoKeyTyped
        SwingUtil.checkInputDigit(evt, false, 4);
    }//GEN-LAST:event_tfCuartoKeyTyped

    private void tfOctetoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfOctetoKeyTyped
        SwingUtil.checkInputDigit(evt, false, 8);
    }//GEN-LAST:event_tfOctetoKeyTyped

    private void tfFactu4KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfFactu4KeyTyped
        SwingUtil.checkInputDigit(evt, false, 4);
    }//GEN-LAST:event_tfFactu4KeyTyped

    private void tfFactu8KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfFactu8KeyTyped
        SwingUtil.checkInputDigit(evt, false, 8);
    }//GEN-LAST:event_tfFactu8KeyTyped

    private void tfCuartoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfCuartoFocusLost
        if (tfCuarto.getText().length() > 0) {
            tfCuarto.setText(UTIL.AGREGAR_CEROS(tfCuarto.getText().trim(), 4));
        }
    }//GEN-LAST:event_tfCuartoFocusLost

    private void tfOctetoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfOctetoFocusLost
        if (tfOcteto.getText().length() > 0) {
            tfOcteto.setText(UTIL.AGREGAR_CEROS(tfOcteto.getText().trim(), 8));
        }
    }//GEN-LAST:event_tfOctetoFocusLost

    private void jTable1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jTable1ComponentResized
        jLabel1.setText("Nº Registros: " + getDtm().getRowCount());
    }//GEN-LAST:event_jTable1ComponentResized
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBuscar;
    private javax.swing.JButton bExtra;
    private javax.swing.JButton bImprimir;
    private javax.swing.JButton bLimpiar;
    private javax.swing.JButton btnToExcel;
    private javax.swing.JComboBox cbCaja;
    private javax.swing.JComboBox cbClieProv;
    private javax.swing.JComboBox cbCuenta;
    private javax.swing.JComboBox cbFormasDePago;
    private javax.swing.JComboBox cbSubCuenta;
    private javax.swing.JComboBox cbSucursal;
    private javax.swing.JComboBox cbUnidadDeNegocio;
    private javax.swing.JComboBox cbVendedor;
    private javax.swing.JCheckBox checkAnulada;
    private com.toedter.calendar.JDateChooser dcDesde;
    private com.toedter.calendar.JDateChooser dcDesdeSistema;
    private com.toedter.calendar.JDateChooser dcHasta;
    private com.toedter.calendar.JDateChooser dcHastaSistema;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelCaja;
    private javax.swing.JLabel labelClieProv;
    private javax.swing.JLabel labelCuenta;
    private javax.swing.JLabel labelFormasDePago;
    private javax.swing.JLabel labelN_Factura;
    private javax.swing.JLabel labelReRe;
    private javax.swing.JLabel labelSubCuenta;
    private javax.swing.JLabel labelSucursal;
    private javax.swing.JLabel labelUnidadDeNegocio;
    private javax.swing.JLabel labelVendedor;
    private javax.swing.JTextField tfCuarto;
    private javax.swing.JTextField tfFactu4;
    private javax.swing.JTextField tfFactu8;
    private javax.swing.JTextField tfOcteto;
    // End of variables declaration//GEN-END:variables

    /**
     * Setea un mensaje de información. Si
     *
     * @param msg ,.. mensaje.
     * @param titulo (puede tener título o no).
     * @param messageType -1=PLAIN, 0=ERROR, 1=INFO, 2=WARRNING.
     */
    public void showMessage(String msg, String titulo, int messageType) {
        if (messageType >= -1 && messageType <= 2) {
            javax.swing.JOptionPane.showMessageDialog(this, msg, titulo,
                    messageType);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, msg);
        }
    }

    public void setListeners(Object o) {
        bBuscar.addActionListener((ActionListener) o);
        bLimpiar.addActionListener((ActionListener) o);
        bImprimir.addActionListener((ActionListener) o);
        btnToExcel.addActionListener((ActionListener) o);
        bExtra.addActionListener((ActionListener) o);
        try {
            jTable1.addMouseListener((MouseListener) o);
        } catch (ClassCastException ex) {
            System.out.println(o.getClass() + " no implementa MouseListener");
        }
        try {
//         tfCuarto.addFocusListener((FocusListener) o);
            tfOcteto.addFocusListener((FocusListener) o);
        } catch (ClassCastException ex) {
            System.out.println(o.getClass() + " no implementa FocusListener");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="GETTERS">
    public JButton getbBuscar() {
        return bBuscar;
    }

    public JButton getbImprimir() {
        return bImprimir;
    }

    public JButton getbExtra() {
        return bExtra;
    }

    public JButton getbLimpiar() {
        return bLimpiar;
    }

    public JButton getBtnToExcel() {
        return btnToExcel;
    }

    public DefaultTableModel getDtm() {
        return (DefaultTableModel) jTable1.getModel();
    }

    public JTable getjTable1() {
        return jTable1;
    }

    /**
     * Cuando es usado para buscar Remitos, referencia si estos fueron
     * facturados. Es decir si están relacionados con una FacturaVenta.
     *
     * @return
     */
    public JComboBox getCbFormasDePago() {
        return cbFormasDePago;
    }

    public JComboBox getCbCaja() {
        return cbCaja;
    }

    public JComboBox getCbClieProv() {
        return cbClieProv;
    }

    public JComboBox getCbCuenta() {
        return cbCuenta;
    }

    public JComboBox getCbSubCuenta() {
        return cbSubCuenta;
    }

    public JComboBox getCbSucursal() {
        return cbSucursal;
    }

    public JComboBox getCbUnidadDeNegocio() {
        return cbUnidadDeNegocio;
    }

    public JComboBox getCbVendedor() {
        return cbVendedor;
    }

    public Date getDcDesde() {
        return dcDesde.getDate();
    }

    public Date getDcHasta() {
        return dcHasta.getDate();
    }

    public String getTfCuarto() {
        return tfCuarto.getText().trim();
    }

    public String getTfOcteto() {
        return tfOcteto.getText().trim();
    }

    public String getTfFactu4() {
        return tfFactu4.getText().trim();
    }

    public String getTfFactu8() {
        return tfFactu8.getText().trim();
    }

    public JCheckBox getCheckAnulada() {
        return checkAnulada;
    }

    public boolean isCheckAnuladaSelected() {
        return checkAnulada.isSelected();
    }

    public JLabel getLabelFormasDePago() {
        return labelFormasDePago;
    }

    public JLabel getLabelReRe() {
        return labelReRe;
    }

    public JTextField getjTfCuarto() {
        return tfCuarto;
    }

    public JTextField getjTfOcteto() {
        return tfOcteto;
    }

    public Date getDcDesdeSistema() {
        return dcDesdeSistema.getDate();
    }

    public Date getDcHastaSistema() {
        return dcHastaSistema.getDate();
    }
    // </editor-fold>

    void limpiarVentana() {
        tfCuarto.setText("");
        tfOcteto.setText("");
        tfFactu4.setText("");
        tfFactu8.setText("");
        try {
            cbCaja.setSelectedIndex(0);
        } catch (IllegalArgumentException e) {
        }
        cbClieProv.setSelectedIndex(0);
        cbSucursal.setSelectedIndex(0);
        dcDesde.setDate(null);
        dcHasta.setDate(null);
        checkAnulada.setSelected(false);
        dtmRemoveAll();
    }

    public void dtmRemoveAll() {
        getDtm().setRowCount(0);
    }

    public void setTfOcteto(String octeto) {
        tfOcteto.setText(octeto);
    }

    public void hideCheckAnulado() {
        checkAnulada.setVisible(false);
    }

    public void hideFactura() {
        labelN_Factura.setVisible(false);
        tfFactu4.setVisible(false);
        tfFactu8.setVisible(false);
    }

    public void hideFormaPago() {
        labelFormasDePago.setVisible(false);
        cbFormasDePago.setVisible(false);
    }

    public void hideCaja() {
        labelCaja.setVisible(false);
        cbCaja.setVisible(false);
    }

    public void setParaNotaCreditoCliente() {
        setParaNotaCreditoProveedor();
        this.setTitle("Buscador de Notas de Crédito de Clientes");
        labelN_Factura.setText("N° Recibo");
    }

    public void setParaNotaCreditoProveedor() {
        this.setTitle("Buscador de Notas de Crédito de Proveedores");
        labelReRe.setText("Nº Nota Crédito");
        hideCaja();
        labelSucursal.setVisible(false);
        cbSucursal.setVisible(false);
        labelN_Factura.setText("N° Remesa");
        labelVendedor.setVisible(false);
        cbVendedor.setVisible(false);
        hideCheckAnulado();
        //reutilización del combo FormaDePago para saber si la NotaCredito fue acreditada (utilizada)
        labelFormasDePago.setText("Acreditada");
        labelFormasDePago.setVisible(true);
        cbFormasDePago.setVisible(true);
        cbFormasDePago.removeAllItems();
        cbFormasDePago.addItem("<Elegir>");
        cbFormasDePago.addItem("No");
        cbFormasDePago.addItem("Si");
        hideUDNCuentaSubCuenta();
    }

    /**
     * esconde: label y combo FormaDePago label Nº Factura TextField tfFactu4
     */
    public void setParaRecibos() {
        labelFormasDePago.setVisible(false);
        cbFormasDePago.setVisible(false);
        labelN_Factura.setVisible(false);
        tfFactu4.setVisible(false);
        tfFactu8.setVisible(false);
        labelVendedor.setVisible(false);
        cbVendedor.setVisible(false);
        labelUnidadDeNegocio.setVisible(false);
        cbUnidadDeNegocio.setVisible(false);
        labelCuenta.setVisible(false);
        cbCuenta.setVisible(false);
        labelSubCuenta.setVisible(false);
        cbSubCuenta.setVisible(false);
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<String, Object>();

        return data;
    }

    public void setFechaSistemaFieldsVisible(boolean visible) {
        jLabel4.setVisible(visible);
        jLabel5.setVisible(visible);
        dcDesdeSistema.setVisible(visible);
        dcHastaSistema.setVisible(visible);
    }

    public void setToFacturaVenta() {
        labelN_Factura.setText("Nº Movim.");
        tfFactu4.setToolTipText("Nº de movimiento interno");
        tfFactu8.setVisible(false);
    }

    @SuppressWarnings("unchecked")
    public void setParaRemito() {
        labelReRe.setText("Nº Remito");
        hideCaja();
        hideFactura();
        //reutilización del combo FormaDePago para saber si el Remito fue facturado
        labelFormasDePago.setText("Facturado");
        labelFormasDePago.setVisible(true);
        cbFormasDePago.setVisible(true);
        cbFormasDePago.removeAllItems();
        cbFormasDePago.addItem("<Elegir>");
        cbFormasDePago.addItem("No");
        cbFormasDePago.addItem("Si");
        checkAnulada.setVisible(true);
        labelUnidadDeNegocio.setVisible(false);
        cbUnidadDeNegocio.setVisible(false);
        labelCuenta.setVisible(false);
        cbCuenta.setVisible(false);
        labelSubCuenta.setVisible(false);
        cbSubCuenta.setVisible(false);
    }

    @SuppressWarnings("unchecked")
    public void setParaNotaDebito() {
        hideCaja();
        hideFactura();
        //reutilización del combo FormaDePago para saber si el Remito fue facturado
        labelFormasDePago.setText("Facturado");
        labelFormasDePago.setVisible(true);
        cbFormasDePago.setVisible(true);
        cbFormasDePago.removeAllItems();
        cbFormasDePago.addItem("<Elegir>");
        cbFormasDePago.addItem("No");
        cbFormasDePago.addItem("Si");
        checkAnulada.setVisible(true);
        labelUnidadDeNegocio.setVisible(false);
        cbUnidadDeNegocio.setVisible(false);
        labelCuenta.setVisible(false);
        cbCuenta.setVisible(false);
        labelSubCuenta.setVisible(false);
        cbSubCuenta.setVisible(false);
        labelVendedor.setVisible(false);
        cbVendedor.setVisible(false);
    }

    public JLabel getLabelVendedor() {
        return labelVendedor;
    }

    public void hideVendedor() {
        labelVendedor.setVisible(false);
        cbVendedor.setVisible(false);
    }

    public void hideUDNCuentaSubCuenta() {
        labelUnidadDeNegocio.setVisible(false);
        cbUnidadDeNegocio.setVisible(false);
        labelCuenta.setVisible(false);
        cbCuenta.setVisible(false);
        labelSubCuenta.setVisible(false);
        cbSubCuenta.setVisible(false);
    }
}
