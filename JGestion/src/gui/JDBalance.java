/*
 * JDBalance.java
 *
 * Created on 06/12/2010, 11:38:35
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author FiruzzZ
 */
public class JDBalance extends javax.swing.JDialog {

   /** Creates new form JDBalance */
   public JDBalance(java.awt.Frame parent, boolean modal, JPanel panel) {
      super(parent, modal);
      this.getContentPane().add(panel, BorderLayout.NORTH, 0);
      pack();
      initComponents();
      ajustarAlPanel(panel.getWidth(), panel.getHeight());
      this.getRootPane().setDefaultButton(bBuscar);
   }

   private void ajustarAlPanel(int width, int height) {
      this.setSize(width + (this.getWidth() - 500), height + (this.getHeight() - 60));
      jScrollPane1.setBounds(jScrollPane1.getX(), height + 30, jScrollPane1.getWidth(), jScrollPane1.getHeight());
      this.setMinimumSize(new Dimension(this.getWidth(), this.getHeight()));
      this.setLocation(this.getOwner().getX() + 100, this.getOwner().getY() + 50);
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bImprimir = new javax.swing.JButton();
        bBuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        tfTotal = new javax.swing.JTextField();
        labelTotal = new javax.swing.JLabel();
        labelCtaCte = new javax.swing.JLabel();
        tfCtaCte = new javax.swing.JTextField();
        labelEfectivo = new javax.swing.JLabel();
        tfEfectivo = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        tfEgresos = new javax.swing.JTextField();
        labelEgresos = new javax.swing.JLabel();
        tfIngresos = new javax.swing.JTextField();
        labelIngresos = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        bImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/impresora.png"))); // NOI18N
        bImprimir.setMnemonic('p');
        bImprimir.setText("Imprimir");
        bImprimir.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        bImprimir.setName("Print"); // NOI18N

        bBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
        bBuscar.setMnemonic('v');
        bBuscar.setText("Ver");
        bBuscar.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        bBuscar.setName("verCtaCtes"); // NOI18N

        jTable1.setModel(new javax.swing.table.DefaultTableModel());
        jTable1.setFocusable(false);
        jTable1.setRequestFocusEnabled(false);
        jTable1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jTable1ComponentResized(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        tfTotal.setEditable(false);
        tfTotal.setColumns(10);
        tfTotal.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tfTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        labelTotal.setText("TOTAL");

        labelCtaCte.setText("Cta. Cte.");

        tfCtaCte.setEditable(false);
        tfCtaCte.setColumns(10);
        tfCtaCte.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tfCtaCte.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        labelEfectivo.setText("Efectivo");

        tfEfectivo.setEditable(false);
        tfEfectivo.setColumns(10);
        tfEfectivo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tfEfectivo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jLabel7.setText("Nº Registros: 0");

        tfEgresos.setEditable(false);
        tfEgresos.setColumns(10);
        tfEgresos.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tfEgresos.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        labelEgresos.setText("Egresos");

        tfIngresos.setEditable(false);
        tfIngresos.setColumns(10);
        tfIngresos.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        tfIngresos.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        labelIngresos.setText("Ingresos");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(bBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(bImprimir))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(120, 120, 120)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelIngresos)
                            .addComponent(tfIngresos, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelEgresos, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfEgresos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelEfectivo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfCtaCte, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelCtaCte))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelTotal))
                        .addGap(4, 4, 4)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(labelEgresos)
                    .addComponent(labelEfectivo)
                    .addComponent(labelCtaCte)
                    .addComponent(labelTotal)
                    .addComponent(labelIngresos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfCtaCte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfEgresos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfIngresos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTable1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jTable1ComponentResized
       jLabel7.setText("Nº Registros: " + jTable1.getModel().getRowCount());
    }//GEN-LAST:event_jTable1ComponentResized
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBuscar;
    private javax.swing.JButton bImprimir;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelCtaCte;
    private javax.swing.JLabel labelEfectivo;
    private javax.swing.JLabel labelEgresos;
    private javax.swing.JLabel labelIngresos;
    private javax.swing.JLabel labelTotal;
    private javax.swing.JTextField tfCtaCte;
    private javax.swing.JTextField tfEfectivo;
    private javax.swing.JTextField tfEgresos;
    private javax.swing.JTextField tfIngresos;
    private javax.swing.JTextField tfTotal;
    // End of variables declaration//GEN-END:variables

   public JButton getbBuscar() {
      return bBuscar;
   }

   public JButton getbImprimir() {
      return bImprimir;
   }

   public JTable getjTable1() {
      return jTable1;
   }

   public JTextField getTfCtaCte() {
      return tfCtaCte;
   }

   public JTextField getTfEfectivo() {
      return tfEfectivo;
   }

   public JTextField getTfTotal() {
      return tfTotal;
   }

   public JLabel getLabelEgresos() {
      return labelEgresos;
   }

   public JLabel getLabelCtaCte() {
      return labelCtaCte;
   }

   public JLabel getLabelEfectivo() {
      return labelEfectivo;
   }

   public JLabel getLabelTotal() {
      return labelTotal;
   }

   public JTextField getTfEgresos() {
      return tfEgresos;
   }

    public JLabel getLabelIngresos() {
        return labelIngresos;
    }

    public JTextField getTfIngresos() {
        return tfIngresos;
    }

}
