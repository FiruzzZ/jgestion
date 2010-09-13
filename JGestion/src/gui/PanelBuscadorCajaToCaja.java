
/*
 * PanelBuscadorCajaToCaja.java
 *
 * Created on 07/07/2010, 11:28:42
 */

package gui;


/**
 *
 * @author FiruzzZ
 */
public class PanelBuscadorCajaToCaja extends javax.swing.JPanel {

    /** Creates new form PanelBuscadorCajaToCaja */
    public PanelBuscadorCajaToCaja() {
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

      dcHasta = new com.toedter.calendar.JDateChooser();
      jLabel3 = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      dcDesde = new com.toedter.calendar.JDateChooser();
      jLabel9 = new javax.swing.JLabel();
      cbCajaOrigen = new javax.swing.JComboBox();
      jLabel10 = new javax.swing.JLabel();
      cbCajaDestino = new javax.swing.JComboBox();

      dcHasta.setDateFormatString(entity.UTIL.DATE_FORMAT.toPattern());

      jLabel3.setText("Desde");

      jLabel4.setText("Hasta");

      dcDesde.setDateFormatString(entity.UTIL.DATE_FORMAT.toPattern());

      jLabel9.setText("Caja Origen");

      jLabel10.setText("Caja Destino");

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(jLabel10)
               .addComponent(jLabel9))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(cbCajaOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(cbCajaDestino, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jLabel3)
               .addComponent(jLabel4))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
               .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel9)
               .addComponent(cbCajaOrigen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel3))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
               .addComponent(cbCajaDestino, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel10)
               .addComponent(jLabel4)
               .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox cbCajaDestino;
   private javax.swing.JComboBox cbCajaOrigen;
   private com.toedter.calendar.JDateChooser dcDesde;
   private com.toedter.calendar.JDateChooser dcHasta;
   private javax.swing.JLabel jLabel10;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel9;
   // End of variables declaration//GEN-END:variables

   public javax.swing.JComboBox getCbCajaDestino() {
      return cbCajaDestino;
   }

   public javax.swing.JComboBox getCbCajaOrigen() {
      return cbCajaOrigen;
   }

   public java.util.Date getDcDesde() {
      return dcDesde.getDate();
   }

   public java.util.Date getDcHasta() {
      return dcHasta.getDate();
   }


}