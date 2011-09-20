
/*
 * PanelABMProveedores.java
 *
 * Created on 20/11/2009, 10:17:56
 */

package gui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 *
 * @author Administrador
 */
public class PanelABMProveedores extends javax.swing.JPanel {

    /**
     * //CARGAR contribuyentes...
     * //CARGAR provincias...
     * //CARGAR departamento...
     * //       municipio..
     * Creates new form PanelABMProveedores */
    public PanelABMProveedores() {
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

        jLabel5 = new javax.swing.JLabel();
        cbProvincias = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        tfEmail = new javax.swing.JTextField();
        tfDireccion = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        bMunicipios = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        tfInterno2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        cbMunicipios = new javax.swing.JComboBox();
        cbDepartamentos = new javax.swing.JComboBox();
        checkRetencionDGR = new javax.swing.JCheckBox();
        cbCondicIVA = new javax.swing.JComboBox();
        checkRetencionIVA = new javax.swing.JCheckBox();
        tfNumDocumento = new javax.swing.JTextField();
        tfTele2 = new javax.swing.JTextField();
        tfTele1 = new javax.swing.JTextField();
        tfWEB = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        tfCP = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taObservacion = new javax.swing.JTextArea();
        tfContacto = new javax.swing.JTextField();
        tfInterno1 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        bDepartamentos = new javax.swing.JButton();
        tfNombre = new javax.swing.JTextField();
        tfCodigo = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        labelCUITValidador = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        cbTipoDocumento = new javax.swing.JComboBox();

        jLabel5.setText("Teléfono 2");

        cbProvincias.setName("cbProvincias"); // NOI18N

        jLabel6.setText("Provincia");

        jLabel3.setText("Nº Doc.");

        jLabel2.setText("Nombre");

        jLabel10.setText("Web");

        jLabel9.setText("E-mail");

        jLabel8.setText("Dirección");

        jLabel7.setText("Depto");

        bMunicipios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_add.png"))); // NOI18N
        bMunicipios.setName("bMunicipios"); // NOI18N
        bMunicipios.setPreferredSize(new java.awt.Dimension(40, 40));
        bMunicipios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bMunicipiosActionPerformed(evt);
            }
        });

        jLabel17.setText("Municipio");

        jLabel16.setText("Interno 2");

        tfInterno2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInterno2KeyTyped(evt);
            }
        });

        jLabel1.setText("Código");

        cbMunicipios.setName("cbProvincia"); // NOI18N
        cbMunicipios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMunicipiosActionPerformed(evt);
            }
        });

        cbDepartamentos.setName("cbDepartamentos"); // NOI18N

        checkRetencionDGR.setText("Retención DGR");

        checkRetencionIVA.setText("Retención IVA");

        tfNumDocumento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfNumDocumentoKeyTyped(evt);
            }
        });

        tfTele2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfTele2KeyTyped(evt);
            }
        });

        tfTele1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfTele1KeyTyped(evt);
            }
        });

        tfWEB.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfWEBKeyTyped(evt);
            }
        });

        jLabel14.setText("Contacto");

        jLabel12.setText("Observ.");

        jLabel13.setText("C.P.");

        tfCP.setColumns(4);
        tfCP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfCPKeyTyped(evt);
            }
        });

        jLabel4.setText("Teléfono 1");

        taObservacion.setColumns(20);
        taObservacion.setLineWrap(true);
        taObservacion.setRows(3);
        taObservacion.setRequestFocusEnabled(false);
        jScrollPane1.setViewportView(taObservacion);

        tfInterno1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfInterno1KeyTyped(evt);
            }
        });

        jLabel15.setText("Interno 1");

        bDepartamentos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_add.png"))); // NOI18N
        bDepartamentos.setName("bDepartamentos"); // NOI18N
        bDepartamentos.setPreferredSize(new java.awt.Dimension(40, 40));
        bDepartamentos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bDepartamentosActionPerformed(evt);
            }
        });

        tfNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfNombreKeyTyped(evt);
            }
        });

        jLabel11.setText("Condic. IVA");

        jLabel18.setText("Tipo Doc.");

        cbTipoDocumento.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DNI", "CUIT" }));
        cbTipoDocumento.setSelectedIndex(1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbCondicIVA, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(tfDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel13)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tfCP))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(tfCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(checkRetencionDGR)
                        .addGap(32, 32, 32)
                        .addComponent(checkRetencionIVA))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(tfTele2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfTele1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(tfInterno1)
                            .addComponent(tfInterno2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(tfWEB, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tfEmail, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tfContacto, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cbMunicipios, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbDepartamentos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbProvincias, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(bMunicipios, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(bDepartamentos, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfNumDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelCUITValidador, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tfCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel13)
                    .addComponent(tfCP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(cbCondicIVA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18)
                        .addComponent(cbTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(labelCUITValidador, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tfNumDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(tfTele1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(tfTele2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfInterno1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(tfInterno2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cbProvincias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbDepartamentos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(bDepartamentos, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbMunicipios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfContacto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)))
                    .addComponent(bMunicipios, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(tfWEB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkRetencionDGR)
                    .addComponent(checkRetencionIVA))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tfNumDocumentoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfNumDocumentoKeyTyped
        soloNumeros(evt);
    }//GEN-LAST:event_tfNumDocumentoKeyTyped

    private void tfTele1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTele1KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfTele1KeyTyped

    private void tfCPKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCPKeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfCPKeyTyped

    private void tfTele2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTele2KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfTele2KeyTyped

    private void tfInterno1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInterno1KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfInterno1KeyTyped

    private void tfInterno2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInterno2KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfInterno2KeyTyped

    private void bMunicipiosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMunicipiosActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_bMunicipiosActionPerformed

    private void tfNombreKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfNombreKeyTyped
        toUpperCase(evt);
    }//GEN-LAST:event_tfNombreKeyTyped

    private void tfWEBKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfWEBKeyTyped
        toUpperCase(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfWEBKeyTyped

    private void bDepartamentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDepartamentosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bDepartamentosActionPerformed

    private void cbMunicipiosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMunicipiosActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_cbMunicipiosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bDepartamentos;
    private javax.swing.JButton bMunicipios;
    private javax.swing.JComboBox cbCondicIVA;
    private javax.swing.JComboBox cbDepartamentos;
    private javax.swing.JComboBox cbMunicipios;
    private javax.swing.JComboBox cbProvincias;
    private javax.swing.JComboBox cbTipoDocumento;
    private javax.swing.JCheckBox checkRetencionDGR;
    private javax.swing.JCheckBox checkRetencionIVA;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelCUITValidador;
    private javax.swing.JTextArea taObservacion;
    private javax.swing.JTextField tfCP;
    private javax.swing.JTextField tfCodigo;
    private javax.swing.JTextField tfContacto;
    private javax.swing.JTextField tfDireccion;
    private javax.swing.JTextField tfEmail;
    private javax.swing.JTextField tfInterno1;
    private javax.swing.JTextField tfInterno2;
    private javax.swing.JTextField tfNombre;
    private javax.swing.JTextField tfNumDocumento;
    private javax.swing.JTextField tfTele1;
    private javax.swing.JTextField tfTele2;
    private javax.swing.JTextField tfWEB;
    // End of variables declaration//GEN-END:variables

    private void soloNumeros(KeyEvent evt) {
        int k = evt.getKeyChar();
        if(k<48 || k>57) evt.setKeyChar((char)java.awt.event.KeyEvent.VK_CLEAR);
    }

    public Object cargarObjeto() {
        return null;
    }

    private void toUpperCase(KeyEvent evt) {
        evt.setKeyChar(String.valueOf(evt.getKeyChar()).toUpperCase().charAt(0));
    }

    public void setIconoValidadorCUIT(boolean valido, String msj) {
        labelCUITValidador.setToolTipText(msj);
        if(valido)
            labelCUITValidador.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/accept.png")));
        else
            labelCUITValidador.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/cancel.png")));
    }

    // <editor-fold defaultstate="collapsed" desc="GETTERS">

    public JLabel getLabelCUITValidador() {
        return labelCUITValidador;
    }

    public JComboBox getCbCondicIVA() {
        return cbCondicIVA;
    }

    public JComboBox getCbDepartamentos() {
        return cbDepartamentos;
    }

    public JComboBox getCbMunicipios() {
        return cbMunicipios;
    }

    public JComboBox getCbProvincias() {
        return cbProvincias;
    }
    
    public Object getSelectedCondicIVA() {
        return cbCondicIVA.getSelectedItem();
    }

    public Object getSelectedDepartamento() {
        return cbDepartamentos.getSelectedItem();
    }

    public Object getSelectedMunicipio() {
        return cbMunicipios.getSelectedItem();
    }

    public Object getSelectedProvincia() {
        return cbProvincias.getSelectedItem();
    }

    public String getTaObservacion() {
        return taObservacion.getText().trim();
    }

    public String getTfCP() {
        return tfCP.getText().trim();
    }

    public String getTfNumDocumento() {
        return tfNumDocumento.getText().trim();
    }

    public String getTfCodigo() {
        return tfCodigo.getText().trim();
    }

    public String getTfContacto() {
        return tfContacto.getText().trim();
    }

    public String getTfDireccion() {
        return tfDireccion.getText().trim();
    }

    public String getTfEmail() {
        return tfEmail.getText().trim();
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

    public String getTfWEB() {
        return tfWEB.getText().trim();
    }

    public JCheckBox getCheckRetencionDGR() {
        return checkRetencionDGR;
    }

    public JCheckBox getCheckRetencionIVA() {
        return checkRetencionIVA;
    }

    public JComboBox getCbTipoDocumento() {
        return cbTipoDocumento;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="SETTERS">
    public void setTaObservacion(String taObservacion) {
        this.taObservacion.setText(taObservacion);
    }

    public void setTfCP(String tfCP) {
        this.tfCP.setText(tfCP);
    }

    public void setTfNumDocumento(String tfCUIT) {
        this.tfNumDocumento.setText(tfCUIT);
    }

    public void setTfCodigo(String tfCodigo) {
        this.tfCodigo.setText(tfCodigo);
    }

    public void setTfContacto(String tfContacto) {
        this.tfContacto.setText(tfContacto);
    }

    public void setTfDireccion(String tfDireccion) {
        this.tfDireccion.setText(tfDireccion);
    }

    public void setTfEmail(String tfEmail) {
        this.tfEmail.setText(tfEmail);
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

    public void setTfWEB(String tfWEB) {
        this.tfWEB.setText(tfWEB);
    }

    public void setListener(Object o) {
        cbProvincias.addActionListener((ActionListener) o);
        cbDepartamentos.addActionListener((ActionListener) o);
        try {
            tfNumDocumento.addKeyListener((KeyListener) o);
        } catch (ClassCastException e) {
            //ignored...
        }
        bMunicipios.addActionListener((ActionListener) o);
        bDepartamentos.addActionListener((ActionListener) o);
    }
    // </editor-fold>

}
