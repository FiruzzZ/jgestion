/*
 * JDBuscadorReRe.java
 *
 * Created on 05/04/2010, 12:13:39
 */
package gui;

import generics.UTIL;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 *
 * @author FiruzzZ
 */
public class JDBuscadorReRe extends javax.swing.JDialog {

   /**
    * Bucador para Remesas y Recibos
    * @param owner
    * @param title
    * @param modal
    * @param labelClieProv
    * @param labelReRe
    */
   public JDBuscadorReRe(Dialog owner, String title, boolean modal, String labelClieProv, String labelReRe) {
      super(owner, modal);
      initComponents();
      init(title, labelClieProv, labelReRe);
      cbFormasDePago.setVisible(false);
      labelFormasDePago.setVisible(false);
   }

   /**
    * Este constructor es para facturas compra/venta.
    * @param owner
    * @param title
    * @param modal
    * @param labelClieProv
    * @param labelReRe
    */
   public JDBuscadorReRe(Frame owner, String title, boolean modal, String labelClieProv, String labelReRe) {
      super(owner, modal);
      initComponents();
      init(title, labelClieProv, labelReRe);
      labelN_Factura.setText("Nº Movim.");
      tfFactu4.setToolTipText("Nº de movimiento interno");
      tfFactu8.setVisible(false);
   }

   private void init(String title, String labelClieProv, String labelReRe) {
      this.setLocationRelativeTo(getOwner());
      this.setTitle(title);
      bImprimir.setVisible(false);
      this.labelClieProv.setText(labelClieProv);
      this.labelReRe.setText(labelReRe);
      rootPane.setDefaultButton(bBuscar);
      setLocation(getOwner().getX() + 100, getY() + 50);

   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jPanel1 = new javax.swing.JPanel();
      labelCaja = new javax.swing.JLabel();
      cbCaja = new javax.swing.JComboBox();
      cbClieProv = new javax.swing.JComboBox();
      labelClieProv = new javax.swing.JLabel();
      cbSucursal = new javax.swing.JComboBox();
      jLabel8 = new javax.swing.JLabel();
      jLabel2 = new javax.swing.JLabel();
      dcDesde = new com.toedter.calendar.JDateChooser();
      jLabel3 = new javax.swing.JLabel();
      dcHasta = new com.toedter.calendar.JDateChooser();
      tfOcteto = new javax.swing.JTextField();
      labelReRe = new javax.swing.JLabel();
      tfCuarto = new javax.swing.JTextField();
      labelN_Factura = new javax.swing.JLabel();
      tfFactu4 = new javax.swing.JTextField();
      tfFactu8 = new javax.swing.JTextField();
      checkAnulada = new javax.swing.JCheckBox();
      labelFormasDePago = new javax.swing.JLabel();
      cbFormasDePago = new javax.swing.JComboBox();
      bLimpiar = new javax.swing.JButton();
      bBuscar = new javax.swing.JButton();
      jScrollPane1 = new javax.swing.JScrollPane();
      jTable1 = new javax.swing.JTable();
      bImprimir = new javax.swing.JButton();
      jLabel1 = new javax.swing.JLabel();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setMinimumSize(new java.awt.Dimension(900, 300));

      jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Filtro", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N

      labelCaja.setText("Caja");

      labelClieProv.setText("Clie/provee");

      jLabel8.setText("Sucursal");

      jLabel2.setText("Desde");

      dcDesde.setDateFormatString(generics.UTIL.DATE_FORMAT.toPattern());

      jLabel3.setText("Hasta");

      dcHasta.setDateFormatString(generics.UTIL.DATE_FORMAT.toPattern());

      tfOcteto.setColumns(8);
      tfOcteto.setName("tfocteto"); // NOI18N
      tfOcteto.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            tfOctetoFocusLost(evt);
         }
      });
      tfOcteto.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfOctetoKeyTyped(evt);
         }
      });

      labelReRe.setText("Nº ReReRe");

      tfCuarto.setColumns(4);
      tfCuarto.addFocusListener(new java.awt.event.FocusAdapter() {
         public void focusLost(java.awt.event.FocusEvent evt) {
            tfCuartoFocusLost(evt);
         }
      });
      tfCuarto.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfCuartoKeyTyped(evt);
         }
      });

      labelN_Factura.setText("Nº Factura");

      tfFactu4.setColumns(4);
      tfFactu4.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfFactu4KeyTyped(evt);
         }
      });

      tfFactu8.setColumns(8);
      tfFactu8.setName("tfFactu8"); // NOI18N
      tfFactu8.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyTyped(java.awt.event.KeyEvent evt) {
            tfFactu8KeyTyped(evt);
         }
      });

      checkAnulada.setText("Solo anuladas");

      labelFormasDePago.setText("Forma pago");

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(labelCaja)
               .addComponent(labelReRe)
               .addComponent(labelN_Factura))
            .addGap(16, 16, 16)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addGroup(jPanel1Layout.createSequentialGroup()
                  .addComponent(tfFactu4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(tfFactu8))
               .addGroup(jPanel1Layout.createSequentialGroup()
                  .addComponent(tfCuarto, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(tfOcteto))
               .addComponent(cbCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(labelClieProv, javax.swing.GroupLayout.Alignment.TRAILING)
                  .addGroup(jPanel1Layout.createSequentialGroup()
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel8)))
               .addComponent(labelFormasDePago))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(cbFormasDePago, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(cbClieProv, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(cbSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jLabel3)
               .addComponent(jLabel2))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(checkAnulada)
               .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(jPanel1Layout.createSequentialGroup()
                  .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(jLabel3)
                     .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(checkAnulada))
               .addGroup(jPanel1Layout.createSequentialGroup()
                  .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfCuarto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(labelReRe)
                     .addComponent(tfOcteto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(cbCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(labelCaja))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(labelN_Factura)
                     .addComponent(tfFactu4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tfFactu8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addGroup(jPanel1Layout.createSequentialGroup()
                  .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(labelClieProv))
                     .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                           .addComponent(cbClieProv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                           .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                           .addComponent(jLabel8)
                           .addComponent(cbSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                     .addComponent(cbFormasDePago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(labelFormasDePago))))
            .addContainerGap(18, Short.MAX_VALUE))
      );

      bLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/broom32x32.png"))); // NOI18N
      bLimpiar.setText("Limpiar");
      bLimpiar.setFocusable(false);
      bLimpiar.setName("limpiarReRe"); // NOI18N
      bLimpiar.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            bLimpiarActionPerformed(evt);
         }
      });

      bBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/lupa.png"))); // NOI18N
      bBuscar.setText("Buscar");
      bBuscar.setName("filtrarReRe"); // NOI18N
      bBuscar.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            bBuscarActionPerformed(evt);
         }
      });

      jTable1.setModel(new javax.swing.table.DefaultTableModel());
      jTable1.addComponentListener(new java.awt.event.ComponentAdapter() {
         public void componentResized(java.awt.event.ComponentEvent evt) {
            jTable1ComponentResized(evt);
         }
      });
      jScrollPane1.setViewportView(jTable1);

      bImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/impresora.png"))); // NOI18N
      bImprimir.setText("Imprimir");
      bImprimir.setName("imprimirFiltro"); // NOI18N
      bImprimir.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            bImprimirActionPerformed(evt);
         }
      });

      jLabel1.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
      jLabel1.setText("Nº Registros: 0");

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 902, Short.MAX_VALUE)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(bImprimir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(bBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(bLimpiar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
               .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(bLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(bBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(bImprimir, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel1))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

    private void bBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bBuscarActionPerformed
}//GEN-LAST:event_bBuscarActionPerformed

    private void bLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLimpiarActionPerformed
       limpiarVentana();
}//GEN-LAST:event_bLimpiarActionPerformed

    private void tfCuartoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfCuartoKeyTyped
       if (tfCuarto.getText().trim().length() < 4) {
          soloNumeros(evt);       // TODO add your handling code here:
       } else {
          evt.setKeyChar((char) java.awt.event.KeyEvent.VK_CLEAR);
       }
    }//GEN-LAST:event_tfCuartoKeyTyped

    private void tfOctetoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfOctetoKeyTyped
       if (tfOcteto.getText().trim().length() < 8) {
          soloNumeros(evt);       // TODO add your handling code here:
       } else {
          evt.setKeyChar((char) java.awt.event.KeyEvent.VK_CLEAR);
       }
    }//GEN-LAST:event_tfOctetoKeyTyped

    private void bImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bImprimirActionPerformed
       // TODO add your handling code here:
    }//GEN-LAST:event_bImprimirActionPerformed

    private void tfFactu4KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfFactu4KeyTyped
       if (tfFactu4.getText().trim().length() <= 4) {
          soloNumeros(evt);       // TODO add your handling code here:
       } else {
          evt.setKeyChar((char) java.awt.event.KeyEvent.VK_CLEAR);
       }
    }//GEN-LAST:event_tfFactu4KeyTyped

    private void tfFactu8KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfFactu8KeyTyped
       // TODO add your handling code here:
    }//GEN-LAST:event_tfFactu8KeyTyped

    private void tfCuartoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfCuartoFocusLost
       if (tfCuarto.getText().length() > 0) {
          tfCuarto.setText(UTIL.AGREGAR_CEROS(tfCuarto.getText().trim(), 4));
       }
    }//GEN-LAST:event_tfCuartoFocusLost

    private void tfOctetoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfOctetoFocusLost
       if (tfOcteto.getText().length() > 0) {
          tfOcteto.setText(UTIL.AGREGAR_CEROS(tfOcteto.getText().trim(), 8));
       }
    }//GEN-LAST:event_tfOctetoFocusLost

    private void jTable1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jTable1ComponentResized
      jLabel1.setText("Nº Registros: " + getDtm().getRowCount());
    }//GEN-LAST:event_jTable1ComponentResized

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton bBuscar;
   private javax.swing.JButton bImprimir;
   private javax.swing.JButton bLimpiar;
   private javax.swing.JComboBox cbCaja;
   private javax.swing.JComboBox cbClieProv;
   private javax.swing.JComboBox cbFormasDePago;
   private javax.swing.JComboBox cbSucursal;
   private javax.swing.JCheckBox checkAnulada;
   private com.toedter.calendar.JDateChooser dcDesde;
   private com.toedter.calendar.JDateChooser dcHasta;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel8;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JTable jTable1;
   private javax.swing.JLabel labelCaja;
   private javax.swing.JLabel labelClieProv;
   private javax.swing.JLabel labelFormasDePago;
   private javax.swing.JLabel labelN_Factura;
   private javax.swing.JLabel labelReRe;
   private javax.swing.JTextField tfCuarto;
   private javax.swing.JTextField tfFactu4;
   private javax.swing.JTextField tfFactu8;
   private javax.swing.JTextField tfOcteto;
   // End of variables declaration//GEN-END:variables

   /**
    * Setea un mensaje de información.
    * Si @param messageType < -1 || > 3 no hace NADA.
    * @param msg,.. mensaje.
    * @param titulo (puede tener título o no).
    * @param messageType -1=PLAIN, 0=ERROR, 1=INFO, 2=WARRNING.
    */
   public void showMessage(String msg, String titulo, int messageType) {
      if (messageType >= -1 && messageType <= 2) {
         javax.swing.JOptionPane.showMessageDialog(this, msg, titulo,
                 messageType);
      } else {
         javax.swing.JOptionPane.showMessageDialog(this, msg);
      }
   }

   public void setListeners(Object o) {
      bBuscar.addActionListener((ActionListener) o);
      bLimpiar.addActionListener((ActionListener) o);
      try {
         jTable1.addMouseListener((MouseListener) o);
      } catch (ClassCastException ex) {
         System.out.println(o.getClass() + " no implementa MouseListener");
      }
      try {
//         tfCuarto.addFocusListener((FocusListener) o);
         tfOcteto.addFocusListener((FocusListener) o);
      } catch (ClassCastException ex) {
         System.out.println(o.getClass() + " no implementa FocusListener");
      }
   }

   // <editor-fold defaultstate="collapsed" desc="GETTERS">
   public JButton getbBuscar() {
      return bBuscar;
   }

   public JButton getbImprimir() {
      return bImprimir;
   }

   public JButton getbLimpiar() {
      return bLimpiar;
   }

   public javax.swing.table.DefaultTableModel getDtm() {
      return (javax.swing.table.DefaultTableModel) jTable1.getModel();
   }

   public JTable getjTable1() {
      return jTable1;
   }

   /**
    * Cuando es usado para buscar Remitos, referencia si estos fueron facturados.
    * Es decir si están relacionados con una FacturaVenta.
    * @return
    */
   public JComboBox getCbFormasDePago() {
      return cbFormasDePago;
   }

   public JComboBox getCbCaja() {
      return cbCaja;
   }

   public JComboBox getCbClieProv() {
      return cbClieProv;
   }

   public JComboBox getCbSucursal() {
      return cbSucursal;
   }

   public java.util.Date getDcDesde() {
      return dcDesde.getDate();
   }

   public java.util.Date getDcHasta() {
      return dcHasta.getDate();
   }

   public String getTfCuarto() {
      return tfCuarto.getText().trim();
   }

   public String getTfOcteto() {
      return tfOcteto.getText().trim();
   }

   public String getTfFactu4() {
      return tfFactu4.getText().trim();
   }

   public String getTfFactu8() {
      return tfFactu8.getText().trim();
   }

   public JCheckBox getCheckAnulada() {
      return checkAnulada;
   }

   public boolean isCheckAnuladaSelected() {
      return checkAnulada.isSelected();
   }// </editor-fold>

   void limpiarVentana() {
      tfCuarto.setText("");
      tfOcteto.setText("");
      tfFactu4.setText("");
      tfFactu8.setText("");
      try {
         cbCaja.setSelectedIndex(0);
      } catch (IllegalArgumentException e) {
      }
      cbClieProv.setSelectedIndex(0);
      cbSucursal.setSelectedIndex(0);
      dcDesde.setDate(null);
      dcHasta.setDate(null);
      checkAnulada.setSelected(false);
      dtmRemoveAll();
   }

   private void soloNumeros(KeyEvent evt) {
      int k = evt.getKeyChar();
      if (k < 48 || k > 57) {
         evt.setKeyChar((char) java.awt.event.KeyEvent.VK_CLEAR);
      }
   }

   public void dtmRemoveAll() {
      for (int i = getDtm().getRowCount() - 1; i > -1; i--) {
         getDtm().removeRow(i);
      }
   }

   public void setTfOcteto(String octeto) {
      tfOcteto.setText(octeto);
   }

   public void hideCheckAnulado() {
      checkAnulada.setVisible(false);
   }

   public void hideFactura() {
      labelN_Factura.setVisible(false);
      tfFactu4.setVisible(false);
      tfFactu8.setVisible(false);
   }

   public void hideFormaPago() {
      labelFormasDePago.setVisible(false);
      cbFormasDePago.setVisible(false);
   }

   public void hideCaja() {
      labelCaja.setVisible(false);
      cbCaja.setVisible(false);
   }

   public JLabel getLabelReRe() {
      return labelReRe;
   }

   public JTextField getjTfCuarto() {
      return tfCuarto;
   }

   public JTextField getjTfOcteto() {
      return tfOcteto;
   }

   public void setPanelInfoParaRemitos() {
      labelReRe.setText("Nº Remito");
      hideCaja();
      hideFactura();
      hideCheckAnulado();
      //reutilización del combo FormaDePago para saber si el Remito fue facturado
      labelFormasDePago.setText("Facturado");
      labelFormasDePago.setVisible(true);
      cbFormasDePago.setVisible(true);
      cbFormasDePago.removeAllItems();
      cbFormasDePago.addItem("<Elegir>");
      cbFormasDePago.addItem("No");
      cbFormasDePago.addItem("Si");
   }

   /**
    * esconde:
    * label y combo FormaDePago
    * label Nº Factura
    * TextField tfFactu4
    */
   public void setParaRecibos() {
      labelFormasDePago.setVisible(false);
      cbFormasDePago.setVisible(false);
      labelN_Factura.setVisible(false);
      tfFactu4.setVisible(false);
   }
   
}
