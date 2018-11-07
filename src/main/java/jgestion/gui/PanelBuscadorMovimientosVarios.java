
/*
 * PanelBuscadorMovimientosVarios.java
 *
 * Created on 05/05/2010, 17:59:36
 */

package jgestion.gui;

import java.util.Date;
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
        labelReRe4 = new javax.swing.JLabel();
        cbCuenta = new javax.swing.JComboBox();
        labelReRe5 = new javax.swing.JLabel();
        cbSubCuenta = new javax.swing.JComboBox();
        cbUnidadDeNegocio = new javax.swing.JComboBox();
        labelReRe6 = new javax.swing.JLabel();

        jLabel9.setText("Caja");

        jLabel3.setText("Desde");

        cbIngresoEgreso.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ambos", "Ingreso", "Egreso" }));

        labelReRe2.setText("Tipo");

        jLabel4.setText("Hasta");

        labelReRe3.setText("Estado de caja");

        cbEstadoCaja.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Abierta", "Cerrada", "Ambas" }));

        labelReRe4.setText("Cuenta");

        labelReRe5.setText("Sub Cuenta");

        labelReRe6.setText("U. de Negocio");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelReRe2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelReRe6, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbUnidadDeNegocio, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbIngresoEgreso, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelReRe5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelReRe4, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbSubCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelReRe3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbEstadoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labelReRe3)
                    .addComponent(cbCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbEstadoCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(labelReRe4)
                    .addComponent(cbCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(labelReRe2)
                    .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbSubCuenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbIngresoEgreso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelReRe5)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(labelReRe6)
                    .addComponent(cbUnidadDeNegocio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbCaja;
    private javax.swing.JComboBox cbCuenta;
    private javax.swing.JComboBox cbEstadoCaja;
    private javax.swing.JComboBox cbIngresoEgreso;
    private javax.swing.JComboBox cbSubCuenta;
    private javax.swing.JComboBox cbUnidadDeNegocio;
    private com.toedter.calendar.JDateChooser dcDesde;
    private com.toedter.calendar.JDateChooser dcHasta;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel labelReRe2;
    private javax.swing.JLabel labelReRe3;
    private javax.swing.JLabel labelReRe4;
    private javax.swing.JLabel labelReRe5;
    private javax.swing.JLabel labelReRe6;
    // End of variables declaration//GEN-END:variables

   public JComboBox getCbCaja() {
      return cbCaja;
   }

   public JComboBox getCbIngresoEgreso() {
      return cbIngresoEgreso;
   }

   public Date getDcDesde() {
      return dcDesde.getDate();
   }

   public Date getDcHasta() {
      return dcHasta.getDate();
   }

   public JComboBox getCbEstadoCaja() {
      return cbEstadoCaja;
   }

   public JComboBox getCbCuenta() {
      return cbCuenta;
   }

    public JComboBox getCbSubCuenta() {
        return cbSubCuenta;
    }

    public JComboBox getCbUnidadDeNegocio() {
        return cbUnidadDeNegocio;
    }
   
   public void setDatesToNull() {
      dcDesde.setDate(null);
      dcHasta.setDate(null);
   }
}