package gui;

import com.toedter.calendar.JDateChooser;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author FiruzzZ
 */
public class PanelInformeFlujoVentas extends javax.swing.JPanel {

    /**
     * Creates new form PanelInformeFlujoVentas
     */
    public PanelInformeFlujoVentas() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        dcDesde = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        dcHasta = new com.toedter.calendar.JDateChooser();
        jPanel1 = new javax.swing.JPanel();
        checkEfectivo = new javax.swing.JCheckBox();
        checkPropios = new javax.swing.JCheckBox();
        checkTerceros = new javax.swing.JCheckBox();
        checkRetencion = new javax.swing.JCheckBox();
        checkEspecie = new javax.swing.JCheckBox();
        checkNotaCredito = new javax.swing.JCheckBox();
        checkTransferencia = new javax.swing.JCheckBox();

        jLabel2.setText("Flujos Desde");

        jLabel3.setText("Flujos Hasta");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Tipos de Flujo"));

        checkEfectivo.setSelected(true);
        checkEfectivo.setText("Efectivo");

        checkPropios.setSelected(true);
        checkPropios.setText("Cheques Propios");

        checkTerceros.setSelected(true);
        checkTerceros.setText("Cheques Terceros");

        checkRetencion.setSelected(true);
        checkRetencion.setText("Retenciones");

        checkEspecie.setSelected(true);
        checkEspecie.setText("Especie");

        checkNotaCredito.setSelected(true);
        checkNotaCredito.setText("Notas de Crédito");

        checkTransferencia.setSelected(true);
        checkTransferencia.setText("Transferencias");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkEfectivo)
                    .addComponent(checkEspecie))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkPropios)
                    .addComponent(checkNotaCredito))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkTransferencia)
                    .addComponent(checkTerceros))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkRetencion)
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkEfectivo)
                    .addComponent(checkPropios)
                    .addComponent(checkTerceros)
                    .addComponent(checkRetencion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkEspecie)
                    .addComponent(checkNotaCredito)
                    .addComponent(checkTransferencia))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkEfectivo;
    private javax.swing.JCheckBox checkEspecie;
    private javax.swing.JCheckBox checkNotaCredito;
    private javax.swing.JCheckBox checkPropios;
    private javax.swing.JCheckBox checkRetencion;
    private javax.swing.JCheckBox checkTerceros;
    private javax.swing.JCheckBox checkTransferencia;
    private com.toedter.calendar.JDateChooser dcDesde;
    private com.toedter.calendar.JDateChooser dcHasta;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    public JDateChooser getDcDesde() {
        return dcDesde;
    }

    public JDateChooser getDcHasta() {
        return dcHasta;
    }
    
    public Map<String,Object> getData() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("flujoDesde", dcDesde.getDate());
        data.put("flujoHasta", dcHasta.getDate());
        data.put("efectivo", checkEfectivo.isSelected());
        data.put("especie", checkEspecie.isSelected());
        data.put("nota", checkNotaCredito.isSelected());
        data.put("propio", checkPropios.isSelected());
        data.put("retencion", checkRetencion.isSelected());
        data.put("terceros", checkTerceros.isSelected());
        data.put("transferencia", checkTransferencia.isSelected());
        return data;
    }
}
