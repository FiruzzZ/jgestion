/*
 * PanelDistribucionStock.java
 *
 * Created on 06/03/2012, 12:53:33
 */
package jgestion.gui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jgestion.entity.Producto;
import utilities.general.UTIL;
import utilities.gui.SwingUtil;

/**
 *
 * @author FiruzzZ
 */
public class PanelDistribucionStock extends javax.swing.JPanel {

    /**
     * Creates new form PanelDistribucionStock
     */
    public PanelDistribucionStock() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cbSucursalOrigen = new javax.swing.JComboBox();
        cbSucursalDestino = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        tfStockOrigen = new javax.swing.JTextField();
        tfStockDestino = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        tfProductoCodigo = new javax.swing.JTextField();
        labelCodigoNoRegistrado = new javax.swing.JLabel();
        labelCodigoNoRegistrado.setVisible(false);
        jLabel10 = new javax.swing.JLabel();
        cbProductos = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        tfMarca = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        tfCantidad = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        tfRubro = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        tfStockActual = new javax.swing.JTextField();
        btnStockGral = new javax.swing.JButton();
        btnHistorial = new javax.swing.JButton();

        jLabel1.setText("Sucursal origen");

        jLabel2.setText("Sucursal destino");

        jLabel3.setText("Stock actual");

        tfStockOrigen.setEditable(false);
        tfStockOrigen.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tfStockOrigen.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfStockOrigen.setText("0");

        tfStockDestino.setEditable(false);
        tfStockDestino.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tfStockDestino.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfStockDestino.setText("0");

        jLabel4.setText("Stock actual");

        jLabel9.setText("Código");

        tfProductoCodigo.setColumns(4);
        tfProductoCodigo.setName("productoCodigo"); // NOI18N
        tfProductoCodigo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfProductoCodigoFocusGained(evt);
            }
        });

        labelCodigoNoRegistrado.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        labelCodigoNoRegistrado.setForeground(new java.awt.Color(255, 51, 0));
        labelCodigoNoRegistrado.setText("¡CÓDIGO NO REGISTRADO!");

        jLabel10.setText("Nombre");

        jLabel13.setText("Marca");

        tfMarca.setEditable(false);
        tfMarca.setFocusable(false);
        tfMarca.setRequestFocusEnabled(false);

        jLabel5.setText("Cantidad a mover");

        tfCantidad.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tfCantidad.setText("0");
        tfCantidad.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfCantidadFocusGained(evt);
            }
        });
        tfCantidad.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfCantidadKeyTyped(evt);
            }
        });

        jLabel6.setText("Rubro");

        tfRubro.setEditable(false);
        tfRubro.setRequestFocusEnabled(false);

        jLabel8.setText("Stock Gral.");

        tfStockActual.setEditable(false);
        tfStockActual.setColumns(4);
        tfStockActual.setFocusable(false);

        btnStockGral.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/view_detail_16x.png"))); // NOI18N
        btnStockGral.setToolTipText("Ver stock gral. (Sucursales)");
        btnStockGral.setName("bstockGral"); // NOI18N

        btnHistorial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        btnHistorial.setText("Historial de Distribuciones");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel13)
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(tfProductoCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelCodigoNoRegistrado))
                                    .addComponent(cbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(tfRubro, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(tfMarca, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfStockActual, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnStockGral, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbSucursalOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(tfStockOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel5)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(tfCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2)
                                        .addComponent(cbSucursalDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addComponent(tfStockDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 47, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnHistorial)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnHistorial, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tfProductoCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(labelCodigoNoRegistrado))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(cbProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel13)
                    .addComponent(btnStockGral, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfStockActual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(tfMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfRubro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSucursalOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfStockOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbSucursalDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfStockDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tfProductoCodigoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfProductoCodigoFocusGained
        tfProductoCodigo.setSelectionStart(0);
    }//GEN-LAST:event_tfProductoCodigoFocusGained

    private void tfCantidadKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCantidadKeyTyped
        UTIL.soloNumeros(evt);
    }//GEN-LAST:event_tfCantidadKeyTyped

    private void tfCantidadFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfCantidadFocusGained
        SwingUtil.setSelectedAll(tfCantidad);
    }//GEN-LAST:event_tfCantidadFocusGained
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHistorial;
    private javax.swing.JButton btnStockGral;
    private javax.swing.JComboBox cbProductos;
    private javax.swing.JComboBox cbSucursalDestino;
    private javax.swing.JComboBox cbSucursalOrigen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelCodigoNoRegistrado;
    private javax.swing.JTextField tfCantidad;
    private javax.swing.JTextField tfMarca;
    private javax.swing.JTextField tfProductoCodigo;
    private javax.swing.JTextField tfRubro;
    private javax.swing.JTextField tfStockActual;
    private javax.swing.JTextField tfStockDestino;
    private javax.swing.JTextField tfStockOrigen;
    // End of variables declaration//GEN-END:variables

    public JComboBox getCbSucursalDestino() {
        return cbSucursalDestino;
    }

    public JComboBox getCbSucursalOrigen() {
        return cbSucursalOrigen;
    }

    public JComboBox getCbProductos() {
        return cbProductos;
    }

    public JTextField getTfCantidad() {
        return tfCantidad;
    }

    public JTextField getTfMarca() {
        return tfMarca;
    }

    public JTextField getTfProductoCodigo() {
        return tfProductoCodigo;
    }

    public JTextField getTfStockDestino() {
        return tfStockDestino;
    }

    public JTextField getTfStockOrigen() {
        return tfStockOrigen;
    }

    public JButton getBtnStockGral() {
        return btnStockGral;
    }

    public JTextField getTfStockActual() {
        return tfStockActual;
    }

    public void resetUI() {
        tfStockOrigen.setText(null);
        tfStockDestino.setText(null);
        tfCantidad.setText(null);
    }

    public JLabel getLabelCodigoNoRegistrado() {
        return labelCodigoNoRegistrado;
    }

    public void setProductoFields(Producto p) {
        tfProductoCodigo.setText(p.getCodigo());
        tfMarca.setText(p.getMarca().getNombre());
        tfRubro.setText(p.getRubro().getNombre());
    }
}