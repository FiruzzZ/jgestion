
/*
 * PanelABMClientes.java
 *
 * Created on 08/03/2010, 08:54:23
 */

package gui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;

/**
 *
 * @author FiruzzZ
 */
public class PanelABMClientes extends javax.swing.JPanel {

    /** Creates new form PanelABMClientes */
    public PanelABMClientes() {
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

      jLabel4 = new javax.swing.JLabel();
      cbProvincias = new javax.swing.JComboBox();
      jLabel5 = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      tfEmail = new javax.swing.JTextField();
      tfDireccion = new javax.swing.JTextField();
      jLabel2 = new javax.swing.JLabel();
      jLabel9 = new javax.swing.JLabel();
      jLabel8 = new javax.swing.JLabel();
      jLabel7 = new javax.swing.JLabel();
      jLabel6 = new javax.swing.JLabel();
      jLabel17 = new javax.swing.JLabel();
      jLabel16 = new javax.swing.JLabel();
      tfInterno2 = new javax.swing.JTextField();
      jLabel10 = new javax.swing.JLabel();
      jLabel1 = new javax.swing.JLabel();
      bMunicipios = new javax.swing.JButton();
      cbDepartamentos = new javax.swing.JComboBox();
      cbMunicipios = new javax.swing.JComboBox();
      cbContribuyente = new javax.swing.JComboBox();
      tfNumDoc = new javax.swing.JTextField();
      tfTele2 = new javax.swing.JTextField();
      tfTele1 = new javax.swing.JTextField();
      tfWEB = new javax.swing.JTextField();
      jLabel14 = new javax.swing.JLabel();
      jLabel12 = new javax.swing.JLabel();
      jLabel13 = new javax.swing.JLabel();
      tfCP = new javax.swing.JTextField();
      jScrollPane1 = new javax.swing.JScrollPane();
      taObservacion = new javax.swing.JTextArea();
      tfContacto = new javax.swing.JTextField();
      tfInterno1 = new javax.swing.JTextField();
      jLabel15 = new javax.swing.JLabel();
      tfNombre = new javax.swing.JTextField();
      tfCodigo = new javax.swing.JTextField();
      jLabel11 = new javax.swing.JLabel();
      bDepartamentos = new javax.swing.JButton();
      jLabel18 = new javax.swing.JLabel();
      cbTipoDocumento = new javax.swing.JComboBox();

      jLabel4.setText("Teléfono 1");

      cbProvincias.setName("cbProvincias"); // NOI18N

      jLabel5.setText("Teléfono 2");

      jLabel3.setText("Nº");

      tfDireccion.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfDireccionKeyTyped(evt);
         }
      });

      jLabel2.setText("Nombre");

      jLabel9.setText("E-mail");

      jLabel8.setText("Dirección");

      jLabel7.setText("Depto");

      jLabel6.setText("Provincia");

      jLabel17.setText("Municipio");

      jLabel16.setText("Int.");

      tfInterno2.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfInterno2KeyTyped(evt);
         }
      });

      jLabel10.setText("Web");

      jLabel1.setText("Código");

      bMunicipios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_add.png"))); // NOI18N
      bMunicipios.setName("municipio"); // NOI18N
      bMunicipios.setPreferredSize(new java.awt.Dimension(40, 40));
      bMunicipios.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            bMunicipiosActionPerformed(evt);
         }
      });

      cbDepartamentos.setName("cbDepartamentos"); // NOI18N

      cbMunicipios.setName("cbProvincia"); // NOI18N

      tfNumDoc.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfNumDocKeyTyped(evt);
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

      tfWEB.setToolTipText("Persona de contacto");
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

      taObservacion.setColumns(20);
      taObservacion.setLineWrap(true);
      taObservacion.setRows(4);
      jScrollPane1.setViewportView(taObservacion);

      tfInterno1.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfInterno1KeyTyped(evt);
         }
      });

      jLabel15.setText("Int.");

      tfNombre.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfNombreKeyTyped(evt);
         }
      });

      tfCodigo.setFont(new java.awt.Font("Tahoma", 1, 11));

      jLabel11.setText("Contribuyente");

      bDepartamentos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_add.png"))); // NOI18N
      bDepartamentos.setName("departamento"); // NOI18N
      bDepartamentos.setPreferredSize(new java.awt.Dimension(40, 40));
      bDepartamentos.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            bDepartamentosActionPerformed(evt);
         }
      });

      jLabel18.setText("Tipo Doc.");

      cbTipoDocumento.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DNI", "CUIT" }));
      cbTipoDocumento.setSelectedIndex(1);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(jLabel18)
               .addComponent(jLabel1)
               .addComponent(jLabel4)
               .addComponent(jLabel5)
               .addComponent(jLabel17)
               .addComponent(jLabel6)
               .addComponent(jLabel9)
               .addComponent(jLabel8)
               .addComponent(jLabel12))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(tfDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel13)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(tfCP, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE))
               .addGroup(layout.createSequentialGroup()
                  .addComponent(tfCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(jLabel2)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(tfNombre, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
               .addGroup(layout.createSequentialGroup()
                  .addComponent(cbTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel3)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(tfNumDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(jLabel11)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(cbContribuyente, 0, 126, Short.MAX_VALUE))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(cbProvincias, javax.swing.GroupLayout.Alignment.LEADING, 0, 116, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(tfTele2)
                        .addComponent(tfTele1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(cbMunicipios, javax.swing.GroupLayout.Alignment.LEADING, 0, 133, Short.MAX_VALUE))
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addComponent(jLabel15)
                           .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addComponent(tfInterno2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                           .addComponent(tfInterno1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(bMunicipios, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfWEB, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbDepartamentos, 0, 152, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bDepartamentos, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
               .addGroup(layout.createSequentialGroup()
                  .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(jLabel10)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(tfContacto, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
               .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE))
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
               .addComponent(jLabel8)
               .addComponent(tfDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel13)
               .addComponent(tfCP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
               .addComponent(jLabel11)
               .addComponent(cbContribuyente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(cbTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel18)
               .addComponent(tfNumDoc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel3))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jLabel4)
               .addComponent(tfTele1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel15)
               .addComponent(tfInterno1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jLabel5)
               .addComponent(tfTele2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel16)
               .addComponent(tfInterno2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(bDepartamentos, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(jLabel6)
                  .addComponent(cbProvincias)
                  .addComponent(cbDepartamentos)
                  .addComponent(jLabel7)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(jLabel17)
                  .addComponent(cbMunicipios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(tfWEB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jLabel14))
               .addComponent(bMunicipios, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jLabel9)
               .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel10)
               .addComponent(tfContacto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jLabel12)
               .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents

    private void tfInterno2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInterno2KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfInterno2KeyTyped

    private void bMunicipiosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMunicipiosActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_bMunicipiosActionPerformed

    private void tfNumDocKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfNumDocKeyTyped
        soloNumeros(evt);
}//GEN-LAST:event_tfNumDocKeyTyped

    private void tfTele2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTele2KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfTele2KeyTyped

    private void tfTele1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTele1KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfTele1KeyTyped

    private void tfWEBKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfWEBKeyTyped
        toUpperCase(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfWEBKeyTyped

    private void tfCPKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCPKeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfCPKeyTyped

    private void tfInterno1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInterno1KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfInterno1KeyTyped

    private void tfNombreKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfNombreKeyTyped
        toUpperCase(evt);
}//GEN-LAST:event_tfNombreKeyTyped

    private void bDepartamentosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bDepartamentosActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_bDepartamentosActionPerformed

    private void tfDireccionKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfDireccionKeyTyped
        toUpperCase(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfDireccionKeyTyped


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton bDepartamentos;
   private javax.swing.JButton bMunicipios;
   private javax.swing.JComboBox cbContribuyente;
   private javax.swing.JComboBox cbDepartamentos;
   private javax.swing.JComboBox cbMunicipios;
   private javax.swing.JComboBox cbProvincias;
   private javax.swing.JComboBox cbTipoDocumento;
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
   private javax.swing.JTextArea taObservacion;
   private javax.swing.JTextField tfCP;
   private javax.swing.JTextField tfCodigo;
   private javax.swing.JTextField tfContacto;
   private javax.swing.JTextField tfDireccion;
   private javax.swing.JTextField tfEmail;
   private javax.swing.JTextField tfInterno1;
   private javax.swing.JTextField tfInterno2;
   private javax.swing.JTextField tfNombre;
   private javax.swing.JTextField tfNumDoc;
   private javax.swing.JTextField tfTele1;
   private javax.swing.JTextField tfTele2;
   private javax.swing.JTextField tfWEB;
   // End of variables declaration//GEN-END:variables

    private void soloNumeros(KeyEvent evt) {
        int k = evt.getKeyChar();
        if(k < 48 || k > 57) evt.setKeyChar((char)java.awt.event.KeyEvent.VK_CLEAR);
    }

    private void toUpperCase(KeyEvent evt) {
        evt.setKeyChar(String.valueOf(evt.getKeyChar()).toUpperCase().charAt(0));
    }

    // <editor-fold defaultstate="collapsed" desc="GETTERS">
    public int getSelectedTipDocumento() {
        return cbTipoDocumento.getSelectedIndex();
    }

    public JComboBox getCbContribuyente() {
        return cbContribuyente;
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

    public Object getSelectedCbContribuyente() {
        return cbContribuyente.getSelectedItem();
    }

    public Object getSelectedCbDepartamentos() {
        return cbDepartamentos.getSelectedItem();
    }

    public Object getSelectedCbMunicipios() {
        return cbMunicipios.getSelectedItem();
    }

    public Object getSelectedCbProvincias() {
        return cbProvincias.getSelectedItem();
    }

    public String getTaObservacion() {
        return taObservacion.getText().trim();
    }

    public String getTfCP() {
        return tfCP.getText().trim();
    }

    public String getTfNumDoc() {
        return tfNumDoc.getText().trim();
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="SETTERS">

    //los combobox se pasan por componente completo......
    //..................
    public void setCbTipoDocumento(int index) {
        this.cbTipoDocumento.setSelectedIndex(index);
    }
    
    public void setTaObservacion(String taObservacion) {
        this.taObservacion.setText(taObservacion);
    }

    public void setTfCP(String tfCP) {
        this.tfCP.setText(tfCP);
    }

    public void setTfNumDoc(String tfCUIT) {
        this.tfNumDoc.setText(tfCUIT);
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
        bDepartamentos.addActionListener((ActionListener) o);
        bMunicipios.addActionListener((ActionListener) o);
    }
    // </editor-fold>


}
