
/*
 * JPArticulos.java
 *
 * Created on 19/11/2009, 16:58:46
 */
package gui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class PanelABMProductos extends javax.swing.JPanel {

    /**
     * Indica si se agregó una descripción
     */
    private boolean conDescripcion;

    /** Creates new form JPArticulos */
    public PanelABMProductos() {
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

        jPanel1 = new javax.swing.JPanel();
        jLabelFoto = new javax.swing.JLabel();
        bBuscarFoto = new javax.swing.JButton();
        bQuitarFoto = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        cbMedicion = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        tfStockMinimo = new javax.swing.JTextField();
        tfStockActual = new javax.swing.JTextField();
        tfStockMax = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        cbRubro = new javax.swing.JComboBox();
        cbSucursal = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        cbTipoMargen = new javax.swing.JComboBox();
        tfMargen = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        cbIVA = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        tfCodigo = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tfNombre = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cbSubRubro = new javax.swing.JComboBox();
        cbMarcas = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        bMarcas = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        tfPrecio = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        tfCostoCompra = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        dateUltimaCompra = new com.toedter.calendar.JDateChooser();
        checkBoxSucursalTodas = new javax.swing.JCheckBox();
        bStockGral = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabelFoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelFoto.setText("[ Sin imagen ]");
        jLabelFoto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

        bBuscarFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        bBuscarFoto.setToolTipText("Buscar foto");
        bBuscarFoto.setName("buscarFoto"); // NOI18N

        bQuitarFoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_button_cancel.png"))); // NOI18N
        bQuitarFoto.setToolTipText("Quitar foto");
        bQuitarFoto.setName("quitarFoto"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(bBuscarFoto, 0, 0, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(bQuitarFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabelFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bQuitarFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bBuscarFoto, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel6.setText("Stock Min");

        jLabel5.setText("U. Medida");

        tfStockMinimo.setColumns(4);
        tfStockMinimo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfStockMinimo.setText("0");
        tfStockMinimo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfStockMinimoKeyTyped(evt);
            }
        });

        tfStockActual.setColumns(4);
        tfStockActual.setEditable(false);
        tfStockActual.setFocusable(false);

        tfStockMax.setColumns(4);
        tfStockMax.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfStockMax.setText("0");

        jLabel7.setText("Stock Max");

        jLabel8.setText("Stock Gral.");

        cbSucursal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSucursalActionPerformed(evt);
            }
        });

        jLabel9.setText("Sub Rubro");

        jLabel15.setText("Sucursal");

        jLabel4.setText("Rubro");

        jLabel13.setText("Descrip.");

        cbTipoMargen.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "%", "$" }));
        cbTipoMargen.setToolTipText("<html>Margen aplicado al precio\n<br>% - Porcentual\n<br>$  - Monto fijo \n</html>");
        cbTipoMargen.setVisible(false);

        tfMargen.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfMargen.setText("0");
        tfMargen.setToolTipText("margen de ganancia del producto");
        tfMargen.setName("tfmargen"); // NOI18N
        tfMargen.setVisible(false);

        jLabel11.setText("Margen");
        jLabel11.setVisible(false);

        jLabel1.setText("Código");

        jLabel2.setText("Nombre");

        jLabel3.setText("Marca");

        cbMarcas.setName("cbMarcas"); // NOI18N

        jLabel10.setText("IVA %");

        bMarcas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_add.png"))); // NOI18N
        bMarcas.setName("Marcas"); // NOI18N
        bMarcas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bMarcasActionPerformed(evt);
            }
        });

        jLabel16.setText("Precio");

        tfPrecio.setColumns(4);
        tfPrecio.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfPrecio.setName("tfprecio"); // NOI18N
        tfPrecio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfPrecioKeyTyped(evt);
            }
        });

        jLabel17.setText("Costo compra");

        tfCostoCompra.setColumns(4);
        tfCostoCompra.setEditable(false);
        tfCostoCompra.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfCostoCompra.setText("0.0");
        tfCostoCompra.setFocusable(false);

        jLabel18.setText("Última compra");

        dateUltimaCompra.setEnabled(false);
        dateUltimaCompra.setFocusable(false);

        checkBoxSucursalTodas.setText("Todas");

        bStockGral.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/view_detail_16x.png"))); // NOI18N
        bStockGral.setToolTipText("Ver stock gral. (Sucursales)");
        bStockGral.setName("bstockGral"); // NOI18N
        bStockGral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStockGralActionPerformed(evt);
            }
        });

        jEditorPane1.setContentType("text/html");
        jEditorPane1.setEditable(false);
        jEditorPane1.setText("<html>\r\n  <head>\r\n\r\n  </head>\r\n  <body>\r\n    <p style=\"margin-top: 0\">\r\n      \r<p align=\"center\">\n  <b>[Doble click para editar]</b>\n</p>\n    </p>\r\n  </body>\r\n</html>\r\n");
        jScrollPane2.setViewportView(jEditorPane1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel16)
                    .addComponent(jLabel13)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2)
                    .addComponent(jLabel15)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(tfPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfCostoCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbTipoMargen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfMargen, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cbSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxSucursalTodas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dateUltimaCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(tfCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(10, 10, 10)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbMarcas, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbIVA, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bMarcas, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbRubro, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbSubRubro, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 430, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cbMedicion, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfStockMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfStockMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfStockActual, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bStockGral, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tfCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(cbMarcas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bMarcas)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(cbIVA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbMedicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(tfStockMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7)
                        .addComponent(tfStockMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8)
                        .addComponent(tfStockActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(bStockGral, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbRubro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSubRubro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(tfPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(cbTipoMargen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfMargen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfCostoCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel18)
                    .addComponent(dateUltimaCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxSucursalTodas)
                    .addComponent(cbSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbSucursalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSucursalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbSucursalActionPerformed

    private void bMarcasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMarcasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bMarcasActionPerformed

    private void tfPrecioKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPrecioKeyTyped
    }//GEN-LAST:event_tfPrecioKeyTyped

    private void tfStockMinimoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfStockMinimoKeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfStockMinimoKeyTyped

    private void bStockGralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bStockGralActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bStockGralActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBuscarFoto;
    private javax.swing.JButton bMarcas;
    private javax.swing.JButton bQuitarFoto;
    private javax.swing.JButton bStockGral;
    private javax.swing.JComboBox cbIVA;
    private javax.swing.JComboBox cbMarcas;
    private javax.swing.JComboBox cbMedicion;
    private javax.swing.JComboBox cbRubro;
    private javax.swing.JComboBox cbSubRubro;
    private javax.swing.JComboBox cbSucursal;
    private javax.swing.JComboBox cbTipoMargen;
    private javax.swing.JCheckBox checkBoxSucursalTodas;
    private com.toedter.calendar.JDateChooser dateUltimaCompra;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelFoto;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField tfCodigo;
    private javax.swing.JTextField tfCostoCompra;
    private javax.swing.JTextField tfMargen;
    private javax.swing.JTextField tfNombre;
    private javax.swing.JTextField tfPrecio;
    private javax.swing.JTextField tfStockActual;
    private javax.swing.JTextField tfStockMax;
    private javax.swing.JTextField tfStockMinimo;
    // End of variables declaration//GEN-END:variables

    public void resetFields() {
        tfCodigo.setText("");
        tfNombre.setText("");
        tfStockActual.setText("0");
        tfStockMax.setText("0");
//        tfDeposito.setText("");
        jEditorPane1.setText("<p align=\"center\"><b>[Doble click para editar]</b></p>");
        tfMargen.setText("");
        tfStockActual.setText("0");
        tfPrecio.setText("");
        tfCostoCompra.setText("");
        //los cb (comboBox) se dejan por conveniencia en su última selección)
    }

    public void setListeners(Object o) {
        bBuscarFoto.addActionListener((ActionListener) o);
        bQuitarFoto.addActionListener((ActionListener) o);
        bMarcas.addActionListener((ActionListener) o);
        tfPrecio.addKeyListener((KeyListener) o);
        bStockGral.addActionListener((ActionListener) o);
    }

    // <editor-fold defaultstate="collapsed" desc="SETTERS">
    public void setTaDescrip(String taDescrip) {
        this.jEditorPane1.setText(taDescrip);
    }

    public void setTfCodigo(String tfCodigo) {
        this.tfCodigo.setText(tfCodigo);
    }

    public void setTfMargen(String tfMargen) {
        this.tfMargen.setText(tfMargen);
    }

    public void setTfNombre(String tfNombre) {
        this.tfNombre.setText(tfNombre);
    }

    public void setTfStockActual(String tfStockActual) {
        this.tfStockActual.setText(tfStockActual);
    }

    public void setTfStockMax(String tfStockMax) {
        this.tfStockMax.setText(tfStockMax);
    }

    public void setTfStockMinimo(String tfStockMinimo) {
        this.tfStockMinimo.setText(tfStockMinimo);
    }

    public void setDateUltimaCompra(java.util.Date dateUltimaCompra) {
        this.dateUltimaCompra.setDate(dateUltimaCompra);
    }

    public void setTfCostoCompra(String tfCostoCompra) {
        this.tfCostoCompra.setText(tfCostoCompra);
    }

    public void setTfPrecio(String tfPrecio) {
        this.tfPrecio.setText(tfPrecio);
    }

    public void setjLabelFoto(JLabel jLabelFoto) {
        this.jLabelFoto = jLabelFoto;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="GETTERS">
    public JLabel getjLabelFoto() {
        return jLabelFoto;
    }

    public String getTfPrecio() {
        return tfPrecio.getText().trim();
    }

    public String getTaDescrip() {
        return jEditorPane1.getText().trim();
    }

    public String getTfCodigo() {
        return tfCodigo.getText().trim();
    }

    public String getTfMargen() {
        return tfMargen.getText().trim();
    }

    public String getTfNombre() {
        return tfNombre.getText().trim();
    }

    public String getTfStockMax() {
        return tfStockMax.getText().trim();
    }

    public String getTfStockMinimo() {
        return tfStockMinimo.getText().trim();
    }

    public JComboBox getCbIVA() {
        return cbIVA;
    }

    public JComboBox getCbMarcas() {
        return cbMarcas;
    }

    public JComboBox getCbMedicion() {
        return cbMedicion;
    }

    public JComboBox getCbRubro() {
        return cbRubro;
    }

    public JComboBox getCbSubRubro() {
        return cbSubRubro;
    }

    public JComboBox getCbSucursal() {
        return cbSucursal;
    }

    public JComboBox getCbTipoMargen() {
        return cbTipoMargen;
    }

    public java.util.Date getDateUltimaCompra() {
        return dateUltimaCompra.getDate();
    }

    public JButton getbBuscarFoto() {
        return bBuscarFoto;
    }

    public JButton getbMarcas() {
        return bMarcas;
    }

    public JButton getbQuitarFoto() {
        return bQuitarFoto;
    }

    public JButton getbStockGral() {
        return bStockGral;
    }

    public boolean isConDescripcion() {
        return conDescripcion;
    }
    // </editor-fold>

    private void soloNumeros(KeyEvent evt) {
        UTIL.soloNumeros(evt);
    }

    public void hideSucursal() {
        jLabel15.setVisible(false);
        cbSucursal.setVisible(false);
        checkBoxSucursalTodas.setVisible(false);
    }

    public JEditorPane getTaDescripcion() {
        return jEditorPane1;
    }

    public void setConDescripcion(boolean b) {
        conDescripcion = b;
    }
}
