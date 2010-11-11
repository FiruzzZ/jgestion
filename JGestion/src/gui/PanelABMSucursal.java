
/*
 * PanelABMSucursal.java
 *
 * Created on 18/02/2010, 11:25:07
 */

package gui;

import generics.UTIL;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JComboBox;

/**
 *
 * @author FiruzzZ
 */
public class PanelABMSucursal extends javax.swing.JPanel {

    /** Creates new form PanelABMSucursal
     * Cargar comboBox Provincia
     * Setear Listener
     */
    public PanelABMSucursal() {
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

      jLabel6.setText("Provincia");

      cbProvincias.setName("cbProvincias"); // NOI18N

      jLabel7.setText("Depto");

      cbDepartamentos.setName("cbDepartamentos"); // NOI18N

      jLabel8.setText("Dirección");

      jLabel4.setText("Teléfono 1");

      tfTele1.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfTele1KeyTyped(evt);
         }
      });

      jLabel5.setText("Teléfono 2");

      tfTele2.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfTele2KeyTyped(evt);
         }
      });

      jLabel9.setText("Int.");

      tfInterno1.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfInterno1KeyTyped(evt);
         }
      });

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

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addComponent(jLabel11)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(tfEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel7)
                     .addComponent(jLabel6)
                     .addComponent(jLabel4)
                     .addComponent(jLabel1)
                     .addComponent(jLabel2)
                     .addComponent(jLabel8)
                     .addComponent(jLabel5)
                     .addComponent(jLabel12))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                              .addComponent(tfInterno1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                           .addGroup(layout.createSequentialGroup()
                              .addComponent(jLabel10)
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(tfInterno2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))))
                     .addComponent(tfNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfEncargado, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(cbMunicipios, 0, 239, Short.MAX_VALUE)
                     .addComponent(cbDepartamentos, 0, 239, Short.MAX_VALUE)
                     .addComponent(cbProvincias, 0, 239, Short.MAX_VALUE))))
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
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents

    private void tfTele1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTele1KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfTele1KeyTyped

    private void tfTele2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfTele2KeyTyped
        soloNumeros(evt);        // TODO add your handling code here:
}//GEN-LAST:event_tfTele2KeyTyped

    private void tfInterno1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInterno1KeyTyped
               soloNumeros(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfInterno1KeyTyped

    private void tfInterno2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfInterno2KeyTyped
               soloNumeros(evt);        // TODO add your handling code here:
    }//GEN-LAST:event_tfInterno2KeyTyped

    private void tfNombreKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfNombreKeyTyped
        evt.setKeyChar(UTIL.TO_UPPER_CASE(evt.getKeyChar()));
    }//GEN-LAST:event_tfNombreKeyTyped


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox cbDepartamentos;
   private javax.swing.JComboBox cbMunicipios;
   private javax.swing.JComboBox cbProvincias;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel10;
   private javax.swing.JLabel jLabel11;
   private javax.swing.JLabel jLabel12;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JLabel jLabel6;
   private javax.swing.JLabel jLabel7;
   private javax.swing.JLabel jLabel8;
   private javax.swing.JLabel jLabel9;
   private javax.swing.JTextField tfDireccion;
   private javax.swing.JTextField tfEmail;
   private javax.swing.JTextField tfEncargado;
   private javax.swing.JTextField tfInterno1;
   private javax.swing.JTextField tfInterno2;
   private javax.swing.JTextField tfNombre;
   private javax.swing.JTextField tfTele1;
   private javax.swing.JTextField tfTele2;
   // End of variables declaration//GEN-END:variables

    private void soloNumeros(KeyEvent evt) {
        int k = evt.getKeyChar();
        if(k < 48 || k > 57) evt.setKeyChar((char)java.awt.event.KeyEvent.VK_CLEAR);
    }

    /**
     * Limpia todos los TextField del panel
     */
    public void clearPanel() {
        tfNombre.setText("");
        tfEncargado.setText("");
        tfDireccion.setText("");
        tfTele1.setText("");
        tfTele2.setText("");
        tfInterno1.setText("");
        tfInterno2.setText("");
        tfEmail.setText("");
    }

    public void setListener(Object o) {
        cbProvincias.addActionListener((ActionListener) o);
        cbDepartamentos.addActionListener((ActionListener) o);
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
    // </editor-fold>



}
