
/*
 * JDResumenReRe.java
 *
 * Created on 13/05/2010, 10:41:40
 */
package gui;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 *
 * @author FiruzzZ
 */
public class JDResumenCtaCtes extends javax.swing.JDialog {

   /**
    * Ventana de resumen de Ctas Ctes de Clientes/Proveedores
    * @param parent 
    * @param modal
    * @param isCliente si es true Cliente/Recibo, sino Proveedor/Remesa.
    */
   public JDResumenCtaCtes(java.awt.Frame parent, boolean modal, boolean isCliente) {
      super(parent, modal);
      initComponents();
      getRootPane().setDefaultButton(bBuscar);
      String cliente_o_proveedor;
      String Recibo_Remesa;
      if(isCliente) {
         cliente_o_proveedor = "Cliente";
         Recibo_Remesa = "Recibo";
      } else {
         cliente_o_proveedor = "Proveedor";
         Recibo_Remesa = "Remesa";
      }

      labelClieProv.setText(cliente_o_proveedor);
      this.setTitle(this.getTitle() + cliente_o_proveedor);
      labelReRe.setText(Recibo_Remesa);
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      bBuscar = new javax.swing.JButton();
      bImprimir = new javax.swing.JButton();
      jSplitPane1 = new javax.swing.JSplitPane();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTableResumen = new javax.swing.JTable();
      jPanel2 = new javax.swing.JPanel();
      labelReciboAnulado = new javax.swing.JLabel();
      tfReciboFecha = new javax.swing.JTextField();
      tfReciboMonto = new javax.swing.JTextField();
      labelReRe = new javax.swing.JLabel();
      cbReRes = new javax.swing.JComboBox();
      jScrollPane2 = new javax.swing.JScrollPane();
      jTableDetalle = new javax.swing.JTable();
      labelClieProv = new javax.swing.JLabel();
      cbClieProv = new javax.swing.JComboBox();
      jLabel2 = new javax.swing.JLabel();
      dcDesde = new com.toedter.calendar.JDateChooser();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Resumen de Cta. Cte.");
      setMinimumSize(new java.awt.Dimension(605, 458));

      bBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
      bBuscar.setText("Ver");
      bBuscar.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      bBuscar.setName("verCtaCtes"); // NOI18N

      bImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/impresora.png"))); // NOI18N
      bImprimir.setText("Imprimir");
      bImprimir.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      bImprimir.setName("Print"); // NOI18N

      jSplitPane1.setDividerLocation(180);
      jSplitPane1.setDividerSize(7);
      jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

      jTableResumen.setModel(new javax.swing.table.DefaultTableModel());
      jTableResumen.setName("jTableResumen"); // NOI18N
      jScrollPane1.setViewportView(jTableResumen);

      jSplitPane1.setTopComponent(jScrollPane1);

      labelReciboAnulado.setFont(new java.awt.Font("Tahoma", 1, 11));
      labelReciboAnulado.setForeground(new java.awt.Color(255, 0, 0));
      labelReciboAnulado.setText("ANULADO");
      labelReciboAnulado.setFocusable(false);
      labelReciboAnulado.setRequestFocusEnabled(false);
      labelReciboAnulado.setVisible(false);

      tfReciboFecha.setColumns(9);
      tfReciboFecha.setEditable(false);

      tfReciboMonto.setColumns(9);
      tfReciboMonto.setEditable(false);

      labelReRe.setText("ReReRe Nº:");

      cbReRes.setName("cbReRes"); // NOI18N

      jTableDetalle.setModel(new javax.swing.table.DefaultTableModel());
      jTableDetalle.setName("jTableDetalle"); // NOI18N
      jScrollPane2.setViewportView(jTableDetalle);

      javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(labelReRe)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(cbReRes, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(tfReciboFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(tfReciboMonto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(labelReciboAnulado)
            .addContainerGap(334, Short.MAX_VALUE))
         .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 802, Short.MAX_VALUE)
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
               .addComponent(tfReciboFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(cbReRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(labelReRe)
               .addComponent(tfReciboMonto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(labelReciboAnulado))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
      );

      jSplitPane1.setRightComponent(jPanel2);

      labelClieProv.setText("Clie/provee");

      cbClieProv.setName("cbClieProv"); // NOI18N

      jLabel2.setText("Desde:");

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE)
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(labelClieProv)
                     .addComponent(jLabel2))
                  .addGap(18, 18, 18)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(cbClieProv, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 326, Short.MAX_VALUE)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(bImprimir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(bBuscar))))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(bBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(bImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(cbClieProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(labelClieProv))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel2))))
            .addGap(17, 17, 17)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
            .addContainerGap())
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton bBuscar;
   private javax.swing.JButton bImprimir;
   private javax.swing.JComboBox cbClieProv;
   private javax.swing.JComboBox cbReRes;
   private com.toedter.calendar.JDateChooser dcDesde;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JSplitPane jSplitPane1;
   private javax.swing.JTable jTableDetalle;
   private javax.swing.JTable jTableResumen;
   private javax.swing.JLabel labelClieProv;
   private javax.swing.JLabel labelReRe;
   private javax.swing.JLabel labelReciboAnulado;
   private javax.swing.JTextField tfReciboFecha;
   private javax.swing.JTextField tfReciboMonto;
   // End of variables declaration//GEN-END:variables

   public void setListener(Object o) {
      bBuscar.addActionListener((ActionListener) o);
      bImprimir.addActionListener((ActionListener) o);
      cbReRes.addActionListener((ActionListener) o);
      try {
         jTableResumen.addMouseListener((MouseListener) o);
      } catch (ClassCastException ex) {
         System.out.println(ex.getClass() + " Cause:" + ex.getCause());
      }
//      jTableDetalle.addMouseListener((MouseListener) o);
   }

   public JComboBox getCbClieProv() {
      return cbClieProv;
   }

   public JComboBox getCbReRes() {
      return cbReRes;
   }

   public java.util.Date getDcDesde() {
      return dcDesde.getDate();
   }

//   public java.util.Date getDcHasta() {
//      return dcHasta.getDate();
//   }

   public JTable getjTableDetalle() {
      return jTableDetalle;
   }

   public JTable getjTableResumen() {
      return jTableResumen;
   }

   public javax.swing.table.DefaultTableModel getDtmResumen() {
      return (javax.swing.table.DefaultTableModel) jTableResumen.getModel();
   }

   public javax.swing.table.DefaultTableModel getDtmDetalle() {
      return (javax.swing.table.DefaultTableModel) jTableDetalle.getModel();
   }

   public void setTfReciboFecha(String reciboFecha) {
      tfReciboFecha.setText(reciboFecha);
   }

   public void setTfReciboMonto(String importe) {
      tfReciboMonto.setText(importe);
   }

   /**
    * Setea un mensaje de información.
    * Si @param messageType < -1 || >3 no hace NADA.
    * @param msg,.. mensaje.
    * @param titulo (puede tener título o no).
    * @param messageType =3 (sin título), sino -1=PLAIN,0=ERROR, 1=INFO,2=WARRNING.
    */
   public void showMessage(String msg, String titulo, int messageType) {
      if (messageType < -1 || messageType > 3) {
         return;
      }
      if (messageType == 3) {
         javax.swing.JOptionPane.showMessageDialog(this, msg);
      } else {
         javax.swing.JOptionPane.showMessageDialog(this, msg, titulo, messageType);
      }
   }

   public JButton getbBuscar() {
      return bBuscar;
   }

   public JButton getbImprimir() {
      return bImprimir;
   }

   public JLabel getLabelReciboAnulado() {
      return labelReciboAnulado;
   }

}
