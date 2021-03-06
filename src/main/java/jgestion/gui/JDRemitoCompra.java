
/*
 * JDRemitoCompra.java
 *
 * Created on Apr 3, 2015, 1:49:42 AM
 */
package jgestion.gui;

import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Producto;
import jgestion.entity.RemitoCompraDetalle;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;

/**
 *
 * @author FiruzzZ
 */
public class JDRemitoCompra extends javax.swing.JDialog {

    private Producto selectedProducto;

    public JDRemitoCompra(java.awt.Window owner, boolean modal) {
        super(owner, modal ? DEFAULT_MODALITY_TYPE : ModalityType.MODELESS);
        initComponents();
        SwingUtil.addDigitsInputListener(tfCantidad, 6);
        SwingUtil.addDigitsInputListener(tfFacturaCuarto, 4);
        SwingUtil.addDigitsInputListener(tfFacturaOcteto, 8);
        UTIL.getDefaultTableModel(jTable1,
                new String[]{"RemitoCompraDetalle.instance", "Producto", "Cantidad", "Bonif."},
                new int[]{1, 200, 40, 30},
                new Class<?>[]{null, null, Integer.class, Boolean.class});
        UTIL.hideColumnTable(jTable1, 0);
        setLocationRelativeTo(owner);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelDatosCompra = new javax.swing.JPanel();
        cbProveedor = new javax.swing.JComboBox();
        labelProveedor = new javax.swing.JLabel();
        labelFecha = new javax.swing.JLabel();
        dcFechaRemito = new com.toedter.calendar.JDateChooser();
        labelSucursal = new javax.swing.JLabel();
        cbSucursal = new javax.swing.JComboBox();
        labelObservacion = new javax.swing.JLabel();
        tfObservacion = new javax.swing.JTextField();
        labelObservacionCharactersCount = new javax.swing.JLabel();
        labelFacturaNumero = new javax.swing.JLabel();
        tfFacturaCuarto = new javax.swing.JTextField();
        tfFacturaOcteto = new javax.swing.JTextField();
        btnCancelar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnAceptar = new javax.swing.JButton();
        btnAnular = new javax.swing.JButton();
        panelProducto = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        tfProductoCodigo = new javax.swing.JTextField();
        bBuscarProducto = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        tfCantidad = new javax.swing.JTextField();
        btnADD = new javax.swing.JButton();
        btnDEL = new javax.swing.JButton();
        cbProductos = new javax.swing.JComboBox();
        labelCodigoNoRegistrado = new javax.swing.JLabel();
        labelCodigoNoRegistrado.setVisible(false);
        checkBonificado = new javax.swing.JCheckBox();
        labelItemsCount = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        checkAcuenta = new javax.swing.JCheckBox();
        checkActualizaStock = new javax.swing.JCheckBox();
        checkAnulada = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panelDatosCompra.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del Remito"));

        labelProveedor.setText("Proveedor");

        labelFecha.setText("Fecha");

        dcFechaRemito.setDateFormatString("dd/MM/yyyy");
        dcFechaRemito.setMaxSelectableDate(new java.util.Date(4070923289000L));
        dcFechaRemito.setMinSelectableDate(new java.util.Date(-2208969703000L));

        labelSucursal.setText("Sucursal");

        labelObservacion.setText("Observación");

        tfObservacion.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfObservacionFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfObservacionFocusLost(evt);
            }
        });
        tfObservacion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfObservacionKeyReleased(evt);
            }
        });

        labelObservacionCharactersCount.setText("0/100");
        labelObservacionCharactersCount.setFocusable(false);
        labelObservacionCharactersCount.setRequestFocusEnabled(false);

        labelFacturaNumero.setText("Nº");

        tfFacturaCuarto.setColumns(4);
        tfFacturaCuarto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfFacturaCuarto.setName("cuarteto"); // NOI18N

        tfFacturaOcteto.setColumns(8);
        tfFacturaOcteto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfFacturaOcteto.setName("octeto"); // NOI18N

        javax.swing.GroupLayout panelDatosCompraLayout = new javax.swing.GroupLayout(panelDatosCompra);
        panelDatosCompra.setLayout(panelDatosCompraLayout);
        panelDatosCompraLayout.setHorizontalGroup(
            panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosCompraLayout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelSucursal, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelProveedor, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelObservacion, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelFacturaNumero, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDatosCompraLayout.createSequentialGroup()
                        .addComponent(tfFacturaCuarto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFacturaOcteto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelFecha)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dcFechaRemito, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelDatosCompraLayout.createSequentialGroup()
                        .addGroup(panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(cbProveedor, javax.swing.GroupLayout.Alignment.LEADING, 0, 424, Short.MAX_VALUE)
                            .addComponent(cbSucursal, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tfObservacion, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelObservacionCharactersCount)))
                .addGap(35, 35, 35))
        );
        panelDatosCompraLayout.setVerticalGroup(
            panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosCompraLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelProveedor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tfFacturaCuarto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfFacturaOcteto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelFacturaNumero)
                    .addComponent(dcFechaRemito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelFecha))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelSucursal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labelObservacion)
                    .addComponent(tfObservacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelObservacionCharactersCount))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cancelar.png"))); // NOI18N
        btnCancelar.setMnemonic('c');
        btnCancelar.setText("Cancelar");
        btnCancelar.setName("cancelar"); // NOI18N

        jScrollPane1.setAutoscrolls(true);

        jTable1.setFocusable(false);
        jTable1.setRequestFocusEnabled(false);
        jTable1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jTable1ComponentResized(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        btnAceptar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_apply.png"))); // NOI18N
        btnAceptar.setMnemonic('a');
        btnAceptar.setText("Aceptar");
        btnAceptar.setName("aceptar"); // NOI18N

        btnAnular.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/32px-Crystal_Clear_action_editdelete.png"))); // NOI18N
        btnAnular.setText("Anular");
        btnAnular.setEnabled(false);
        btnAnular.setName("anular"); // NOI18N

        panelProducto.setBorder(javax.swing.BorderFactory.createTitledBorder("Producto"));

        jLabel7.setText("Código");

        tfProductoCodigo.setColumns(4);
        tfProductoCodigo.setName("productoCodigo"); // NOI18N
        tfProductoCodigo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfProductoCodigoFocusGained(evt);
            }
        });

        bBuscarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        bBuscarProducto.setName("buscarProducto"); // NOI18N

        jLabel8.setText("Nombre");

        jLabel9.setText("Cantidad");

        tfCantidad.setColumns(4);
        tfCantidad.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfCantidad.setName("productoCant"); // NOI18N

        btnADD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/add.png"))); // NOI18N
        btnADD.setToolTipText("Agregar producto");
        btnADD.setName("add"); // NOI18N
        btnADD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnADDActionPerformed(evt);
            }
        });

        btnDEL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/delete.png"))); // NOI18N
        btnDEL.setToolTipText("Quitar producto");
        btnDEL.setName("del"); // NOI18N
        btnDEL.setRequestFocusEnabled(false);
        btnDEL.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                btnDELFocusLost(evt);
            }
        });
        btnDEL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDELActionPerformed(evt);
            }
        });

        labelCodigoNoRegistrado.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        labelCodigoNoRegistrado.setForeground(new java.awt.Color(255, 51, 0));
        labelCodigoNoRegistrado.setText("¡CÓDIGO NO REGISTRADO!");

        checkBonificado.setText("Bonificado");
        checkBonificado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBonificadoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelProductoLayout = new javax.swing.GroupLayout(panelProducto);
        panelProducto.setLayout(panelProductoLayout);
        panelProductoLayout.setHorizontalGroup(
            panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelProductoLayout.createSequentialGroup()
                        .addComponent(tfCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBonificado)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnADD, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDEL, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelProductoLayout.createSequentialGroup()
                        .addGroup(panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelProductoLayout.createSequentialGroup()
                                .addComponent(tfProductoCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelCodigoNoRegistrado)))
                        .addGap(0, 57, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelProductoLayout.setVerticalGroup(
            panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductoLayout.createSequentialGroup()
                .addGroup(panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tfProductoCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(bBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelCodigoNoRegistrado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelProductoLayout.createSequentialGroup()
                        .addGroup(panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(cbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(tfCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkBonificado)))
                    .addComponent(btnADD, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDEL, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        labelItemsCount.setText("Items:");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Opciones"));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_blue_system-help.png"))); // NOI18N
        jLabel1.setText("info");
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        checkAcuenta.setText("Productos a cuenta");

        checkActualizaStock.setSelected(true);
        checkActualizaStock.setText("Actualiza stock");
        checkActualizaStock.setToolTipText("Si la carga de factura afectará el stock");

        checkAnulada.setText("Anulada");
        checkAnulada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAnuladaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(checkAcuenta)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addComponent(checkActualizaStock)
                    .addComponent(checkAnulada))
                .addContainerGap(56, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(checkAnulada)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(checkAcuenta)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkActualizaStock)
                .addContainerGap(231, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAnular)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 266, Short.MAX_VALUE)
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar))
                    .addComponent(panelDatosCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelItemsCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelDatosCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelItemsCount)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAnular, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tfObservacionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfObservacionFocusGained
        controlDeCaracteres(tfObservacion);
    }//GEN-LAST:event_tfObservacionFocusGained

    private void tfObservacionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfObservacionFocusLost
        controlDeCaracteres(tfObservacion);
    }//GEN-LAST:event_tfObservacionFocusLost

    private void tfObservacionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfObservacionKeyReleased
        controlDeCaracteres(tfObservacion);
    }//GEN-LAST:event_tfObservacionKeyReleased

    private void tfProductoCodigoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProductoCodigoFocusGained
        tfProductoCodigo.setSelectionStart(0);
    }//GEN-LAST:event_tfProductoCodigoFocusGained

    private void jTable1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jTable1ComponentResized
        labelItemsCount.setText("Items: " + jTable1.getRowCount());
    }//GEN-LAST:event_jTable1ComponentResized

    private void checkBonificadoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBonificadoActionPerformed
        if (checkBonificado.isSelected() && !checkAcuenta.isSelected()) {
            JOptionPane.showMessageDialog(this, "La bonificación de productos solo es válida si la opción \"Productos a Cuenta\" está seleccionada", null, JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_checkBonificadoActionPerformed

    private void btnADDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnADDActionPerformed
        try {
            if (selectedProducto == null) {
                throw new MessageException("Seleccione un producto");
            }
            int cantidad;
            try {
                cantidad = Integer.valueOf(tfCantidad.getText());
                if (cantidad < 1) {
                    throw new MessageException("La cantidad no puede ser menor a 1");
                }
            } catch (NumberFormatException ex) {
                throw new MessageException("Cantidad no válida (solo números enteros)");
            }
            RemitoCompraDetalle item = new RemitoCompraDetalle(selectedProducto, cantidad, checkBonificado.isSelected());
            addItem(item);
        } catch (MessageException ex) {
            ex.displayMessage(this);
        }
    }//GEN-LAST:event_btnADDActionPerformed

    private void btnDELActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDELActionPerformed
        UTIL.removeSelectedRows(jTable1);
    }//GEN-LAST:event_btnDELActionPerformed

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        JOptionPane.showMessageDialog(this, "Los remitos de compra pueden ser cargador de 2 maneras:"
                + "\n1. Sin seleccionar la opción \"Productos a cuenta\", lo cual permite que el Remito sea relacionado a una Factura de Compra."
                + "\nUna Factura de Compra puede relacionarse a varios Remitos, pero un Remito solo puede ser relacionado a una Factura de Compra."
                + "\n2. Seleccionando la opción, las cantidades de los productos ingresados son acumulados en una Cuenta del Proveedor y"
                + "\nal momento de realizar el pago (Factura Compra), se puede consultar los productos que fueron recibidos a cuentas y"
                + "\nrealizar el pago en relación a estas cantidades (lo cual reduce la Cuenta).");
    }//GEN-LAST:event_jLabel1MouseClicked

    private void checkAnuladaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAnuladaActionPerformed
        if (checkAnulada.isSelected()) {
//            checkAnulada.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
            checkAnulada.setForeground(new java.awt.Color(255, 51, 0));
        } else {
            checkAnulada.setForeground(null);
        }
    }//GEN-LAST:event_checkAnuladaActionPerformed

    private void btnDELFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_btnDELFocusLost
        tfProductoCodigo.requestFocusInWindow();
    }//GEN-LAST:event_btnDELFocusLost


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBuscarProducto;
    private javax.swing.JButton btnADD;
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnAnular;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnDEL;
    private javax.swing.JComboBox cbProductos;
    private javax.swing.JComboBox cbProveedor;
    private javax.swing.JComboBox cbSucursal;
    private javax.swing.JCheckBox checkActualizaStock;
    private javax.swing.JCheckBox checkAcuenta;
    private javax.swing.JCheckBox checkAnulada;
    private javax.swing.JCheckBox checkBonificado;
    private com.toedter.calendar.JDateChooser dcFechaRemito;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelCodigoNoRegistrado;
    private javax.swing.JLabel labelFacturaNumero;
    private javax.swing.JLabel labelFecha;
    private javax.swing.JLabel labelItemsCount;
    private javax.swing.JLabel labelObservacion;
    private javax.swing.JLabel labelObservacionCharactersCount;
    private javax.swing.JLabel labelProveedor;
    private javax.swing.JLabel labelSucursal;
    private javax.swing.JPanel panelDatosCompra;
    private javax.swing.JPanel panelProducto;
    private javax.swing.JTextField tfCantidad;
    private javax.swing.JTextField tfFacturaCuarto;
    private javax.swing.JTextField tfFacturaOcteto;
    private javax.swing.JTextField tfObservacion;
    private javax.swing.JTextField tfProductoCodigo;
    // End of variables declaration//GEN-END:variables

    private void controlDeCaracteres(JTextField textField) {
        int length = textField.getText().length();
        labelObservacionCharactersCount.setText(length + "/100");
        if (length > 100) {
            tfObservacion.setBackground(Color.RED);
        } else {
            tfObservacion.setBackground(null);
        }
    }
    //<editor-fold defaultstate="collapsed" desc="getters">

    public JComboBox getCbProductos() {
        return cbProductos;
    }

    public JComboBox getCbProveedor() {
        return cbProveedor;
    }

    public JComboBox getCbSucursal() {
        return cbSucursal;
    }

    public JCheckBox getCheckAcuenta() {
        return checkAcuenta;
    }

    public JCheckBox getCheckAnulada() {
        return checkAnulada;
    }

    public JCheckBox getCheckActualizaStock() {
        return checkActualizaStock;
    }

    public JTextField getTfProductoCodigo() {
        return tfProductoCodigo;
    }

    public JTextField getTfFacturaCuarto() {
        return tfFacturaCuarto;
    }

    public JTextField getTfFacturaOcteto() {
        return tfFacturaOcteto;
    }

    public JTable getjTable1() {
        return jTable1;
    }

    public JButton getBtnAceptar() {
        return btnAceptar;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }

    public JDateChooser getDcFechaRemito() {
        return dcFechaRemito;
    }

    public JTextField getTfObservacion() {
        return tfObservacion;
    }
//</editor-fold>

    public void setProducto(Producto producto) {
        selectedProducto = producto;
        if (producto != null) {
            labelCodigoNoRegistrado.setVisible(false);
            tfProductoCodigo.setText(producto.getCodigo());
            UTIL.setSelectedItem(getCbProductos(), producto.getNombre());
            tfCantidad.requestFocus();
        } else {
            labelCodigoNoRegistrado.setVisible(true);
        }
    }

    public void addItem(RemitoCompraDetalle item) throws MessageException {
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        for (int row = 0; row < dtm.getRowCount(); row++) {
            RemitoCompraDetalle old = (RemitoCompraDetalle) dtm.getValueAt(row, 0);
            if (old.getProducto().equals(item.getProducto()) && old.isBonificado() == item.isBonificado()) {
                String bonifText = (item.isBonificado() ? " (como Bonificado)" : "");
                throw new MessageException("Este producto ya ha sido agregado al detalle" + bonifText);
            }
        }
        dtm.addRow(new Object[]{item, item.getProducto().getNombre(), item.getCantidad(), item.isBonificado()});
    }

    public Long getNumero() {
        if (tfFacturaCuarto.getText().isEmpty()) {
            return null;
        }
        if (tfFacturaOcteto.getText().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(tfFacturaCuarto.getText().trim() + tfFacturaOcteto.getText().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public List<RemitoCompraDetalle> getItems() {
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        List<RemitoCompraDetalle> l = new ArrayList<>(dtm.getRowCount());
        for (int row = 0; row < dtm.getRowCount(); row++) {
            l.add((RemitoCompraDetalle) dtm.getValueAt(row, 0));

        }
        return l;
    }
}
