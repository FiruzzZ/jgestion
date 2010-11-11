
/*
 * PanelBuscadorMovimientosVarios.java
 *
 * Created on 05/05/2010, 17:59:36
 */

package gui;

import javax.swing.JComboBox;

/**
 *
 * @author FiruzzZ
 */
public class PanelBuscadorMovimientosVarios extends javax.swing.JPanel {

    /** Creates new form PanelBuscadorMovimientosVarios */
    public PanelBuscadorMovimientosVarios() {
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

      jLabel9 = new javax.swing.JLabel();
      cbCaja = new javax.swing.JComboBox();
      dcDesde = new com.toedter.calendar.JDateChooser();
      jLabel3 = new javax.swing.JLabel();
      cbIngresoEgreso = new javax.swing.JComboBox();
      labelReRe2 = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      dcHasta = new com.toedter.calendar.JDateChooser();
      labelReRe3 = new javax.swing.JLabel();
      cbEstadoCaja = new javax.swing.JComboBox();

      jLabel9.setText("Caja");

      dcDesde.setDateFormatString(generics.UTIL.DATE_FORMAT.toPattern());

      jLabel3.setText("Desde");

      cbIngresoEgreso.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ambos", "Ingreso", "Egreso" }));

      labelReRe2.setText("Tipo");

      jLabel4.setText("Hasta");

      dcHasta.setDateFormatString(generics.UTIL.DATE_FORMAT.toPattern());

      labelReRe3.setText("Estado de caja");

      cbEstadoCaja.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Abierta", "Cerrada", "Ambas" }));

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel9)
                     .addComponent(labelReRe2))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(cbCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(cbIngresoEgreso, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addGap(18, 18, 18)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelReRe3)
                     .addComponent(jLabel3))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(cbEstadoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addComponent(jLabel4)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addContainerGap()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel9)
                     .addComponent(cbCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(labelReRe3)
                     .addComponent(cbEstadoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(labelReRe2)
                     .addComponent(cbIngresoEgreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(jLabel4)
                     .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addGroup(layout.createSequentialGroup()
                  .addGap(37, 37, 37)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel3))))
            .addContainerGap(16, Short.MAX_VALUE))
      );
   }// </editor-fold>//GEN-END:initComponents


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox cbCaja;
   private javax.swing.JComboBox cbEstadoCaja;
   private javax.swing.JComboBox cbIngresoEgreso;
   private com.toedter.calendar.JDateChooser dcDesde;
   private com.toedter.calendar.JDateChooser dcHasta;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel9;
   private javax.swing.JLabel labelReRe2;
   private javax.swing.JLabel labelReRe3;
   // End of variables declaration//GEN-END:variables

   public JComboBox getCbCaja() {
      return cbCaja;
   }

   public JComboBox getCbIngresoEgreso() {
      return cbIngresoEgreso;
   }

   public java.util.Date getDcDesde() {
      return dcDesde.getDate();
   }

   public java.util.Date getDcHasta() {
      return dcHasta.getDate();
   }

   public JComboBox getCbEstadoCaja() {
      return cbEstadoCaja;
   }

   public void setDatesToNull() {
      dcDesde.setDate(null);
      dcHasta.setDate(null);
   }
}
