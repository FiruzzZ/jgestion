/*
 * JDCatalogoWEB.java
 *
 * Created on 09/02/2011, 08:00:59
 */
package gui;

import controller.DAO;
import controller.FacturaVentaJpaController;
import controller.ListaPreciosJpaController;
import controller.ProductoJpaController;
import controller.ProductosWebJpaController;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.ListaPrecios;
import entity.Producto;
import entity.ProductosWeb;
import generics.UTIL;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class JDCatalogoWEB extends javax.swing.JDialog {

   private ProductoJpaController productroController;
   private ProductosWebJpaController productosWebJpaController;

   /** Creates new form JDCatalogoWEB
    * @throws SQLException pasa.. en las mejores familias
    */
   public JDCatalogoWEB(java.awt.Frame parent, boolean modal) throws SQLException {
      super(parent, modal);
      initComponents();
      productroController = new ProductoJpaController();
      productosWebJpaController = new ProductosWebJpaController();
      UTIL.hideColumnTable(tableCatalogo, 0); //columna que contiene el productoID
      cargarTablaProductos(null);
      cargarTablaCatalogo(null);
   }

   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jLabel1 = new javax.swing.JLabel();
      jScrollPane1 = new javax.swing.JScrollPane();
      tableProductos = new javax.swing.JTable();
      btnActualizarCatalogo = new javax.swing.JButton();
      jScrollPane2 = new javax.swing.JScrollPane();
      tableCatalogo = new javax.swing.JTable();
      jLabel2 = new javax.swing.JLabel();
      jLabel3 = new javax.swing.JLabel();
      btnAdd = new javax.swing.JButton();
      btnDel = new javax.swing.JButton();
      tfFiltroCatalogo = new javax.swing.JTextField();
      jLabel4 = new javax.swing.JLabel();
      tfFiltroProductos = new javax.swing.JTextField();
      checkSoloHabilitados = new javax.swing.JCheckBox();

      setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      setTitle("Administración: Catálogo Web");
      setResizable(false);

      jLabel1.setText("<html>Para que el Producto pueda formar parte del Catálogo WEB debe tener: Precio de venta, Rubro, Sub-Rubro, Imagen y Descripción.\n<p>Los precios de venta de los productos en la tabla ya fueron calculados según la Lista de Precios asignada para CatálogoWeb.\n</html>");

      tableProductos.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null},
            {null, null, null, null, null, null, null}
         },
         new String [] {
            "Código", "Producto", "Precio venta", "Rubro", "Sub-Rubro", "Imagen", "Descrip."
         }
      ) {
         Class[] types = new Class [] {
            java.lang.Object.class, java.lang.Object.class, java.lang.Double.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class, java.lang.Boolean.class
         };
         boolean[] canEdit = new boolean [] {
            false, false, false, false, false, false, false
         };

         public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
         }

         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
         }
      });
      tableProductos.getTableHeader().setReorderingAllowed(false);
      jScrollPane1.setViewportView(tableProductos);
      tableProductos.getColumnModel().getColumn(0).setPreferredWidth(40);
      tableProductos.getColumnModel().getColumn(1).setPreferredWidth(300);
      tableProductos.getColumnModel().getColumn(2).setPreferredWidth(35);
      tableProductos.getColumnModel().getColumn(5).setPreferredWidth(15);
      tableProductos.getColumnModel().getColumn(6).setPreferredWidth(15);

      btnActualizarCatalogo.setText("Actualizar Catálogo");
      btnActualizarCatalogo.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnActualizarCatalogoActionPerformed(evt);
         }
      });

      tableCatalogo.setModel(new javax.swing.table.DefaultTableModel(
         new Object [][] {
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null},
            {null, null, null, null, null, null}
         },
         new String [] {
            "id", "Código", "Producto", "Precio venta", "Destacado", "Oferta"
         }
      ) {
         Class[] types = new Class [] {
            java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Double.class, java.lang.Boolean.class, java.lang.Boolean.class
         };
         boolean[] canEdit = new boolean [] {
            false, false, false, false, false, false
         };

         public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
         }

         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
         }
      });
      tableCatalogo.getTableHeader().setReorderingAllowed(false);
      jScrollPane2.setViewportView(tableCatalogo);
      tableCatalogo.getColumnModel().getColumn(0).setPreferredWidth(0);
      tableCatalogo.getColumnModel().getColumn(1).setPreferredWidth(40);
      tableCatalogo.getColumnModel().getColumn(2).setPreferredWidth(300);
      tableCatalogo.getColumnModel().getColumn(3).setPreferredWidth(35);
      tableCatalogo.getColumnModel().getColumn(4).setPreferredWidth(15);
      tableCatalogo.getColumnModel().getColumn(5).setPreferredWidth(15);

      jLabel2.setText("Buscar");

      jLabel3.setFont(new java.awt.Font("Tahoma", 0, 9)); // NOI18N
      jLabel3.setForeground(new java.awt.Color(0, 0, 255));
      jLabel3.setText("* Mantener CTRL presionado para seleccionar mas de un Producto.");
      jLabel3.setToolTipText("Mantener SHIFT presionado para seleccionar mas de un Producto.");

      btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_add_circular.png"))); // NOI18N
      btnAdd.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnAddActionPerformed(evt);
         }
      });

      btnDel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/16px_delete_circular.png"))); // NOI18N
      btnDel.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            btnDelActionPerformed(evt);
         }
      });

      tfFiltroCatalogo.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyReleased(java.awt.event.KeyEvent evt) {
            tfFiltroCatalogoKeyReleased(evt);
         }
      });

      jLabel4.setText("Buscar");

      tfFiltroProductos.addKeyListener(new java.awt.event.KeyAdapter() {
         public void keyReleased(java.awt.event.KeyEvent evt) {
            tfFiltroProductosKeyReleased(evt);
         }
      });

      checkSoloHabilitados.setText("Ver solo habilitados");
      checkSoloHabilitados.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            checkSoloHabilitadosActionPerformed(evt);
         }
      });

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(jLabel2)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(tfFiltroCatalogo, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addComponent(jLabel3)
               .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfFiltroProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(83, 83, 83)
                        .addComponent(checkSoloHabilitados))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 829, Short.MAX_VALUE)
                     .addComponent(btnActualizarCatalogo, javax.swing.GroupLayout.Alignment.TRAILING))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(btnAdd)
                     .addComponent(btnDel))))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(9, 9, 9)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(jLabel4)
               .addComponent(tfFiltroProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(checkSoloHabilitados))
            .addGap(6, 6, 6)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel3)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(tfFiltroCatalogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addComponent(btnAdd))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(btnDel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(btnActualizarCatalogo)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   private void tfFiltroProductosKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfFiltroProductosKeyReleased
      try {
         filtrarTablaProductos();
      } catch (SQLException ex) {
         JOptionPane.showMessageDialog(null, ex.getMessage(), "SQL EXCE", 0);
      }
   }//GEN-LAST:event_tfFiltroProductosKeyReleased

   private void tfFiltroCatalogoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfFiltroCatalogoKeyReleased
      try {
         filtrarTablaCatalogo();
      } catch (SQLException ex) {
         JOptionPane.showMessageDialog(null, ex.getMessage(), "SQL EXCE", 0);
      }
   }//GEN-LAST:event_tfFiltroCatalogoKeyReleased

   private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
      if (tableProductos.getSelectedRowCount() < 1) {
         JOptionPane.showMessageDialog(this, "No ha seleccionado ningún producto", "!", 2);
      } else {
         try {
            agregarToCatalogo();
            filtrarTablaProductos();
            cargarTablaCatalogo(null);
         } catch (MessageException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), null, 2);
         } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), null, 0);
         } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), null, 0);
            ex.printStackTrace();
         }
      }
   }//GEN-LAST:event_btnAddActionPerformed

   private void checkSoloHabilitadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkSoloHabilitadosActionPerformed
      checkSoloHabilitados.setEnabled(false);
      try {
         filtrarTablaProductos();
      } catch (SQLException ex) {
         JOptionPane.showMessageDialog(null, ex.getMessage(), "SQL EXCEption", 0);
      }
      checkSoloHabilitados.setEnabled(true);
   }//GEN-LAST:event_checkSoloHabilitadosActionPerformed

   private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
      if (tableCatalogo.getSelectedRowCount() > 0) {
         try {
            quitarFromCatalogo();
            filtrarTablaProductos();
            cargarTablaCatalogo(null);
         } catch (NonexistentEntityException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "ERROR", 2);
         } catch (Exception ex) {
            org.apache.log4j.Logger.getLogger(JDCatalogoWEB.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "ERROR", 2);
         }
      } else {
         JOptionPane.showMessageDialog(this, "No ha seleccionado ningún Producto del catálogo", "OJO!", 2);
      }
   }//GEN-LAST:event_btnDelActionPerformed

   private void btnActualizarCatalogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActualizarCatalogoActionPerformed
      btnActualizarCatalogo.setEnabled(false);
      try {
         String data = ProductosWebJpaController.UPDATE_CATALOGOWEB();
         JOptionPane.showMessageDialog(this, data);
      } catch (Exception e) {
         JOptionPane.showMessageDialog(this, e.getMessage(), "Error en el módulo de actualización web", 0);
         org.apache.log4j.Logger.getLogger(JDCatalogoWEB.class).log(org.apache.log4j.Level.ERROR, e);
      } finally {
         btnActualizarCatalogo.setEnabled(true);
      }
   }//GEN-LAST:event_btnActualizarCatalogoActionPerformed
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton btnActualizarCatalogo;
   private javax.swing.JButton btnAdd;
   private javax.swing.JButton btnDel;
   private javax.swing.JCheckBox checkSoloHabilitados;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JScrollPane jScrollPane2;
   private javax.swing.JTable tableCatalogo;
   private javax.swing.JTable tableProductos;
   private javax.swing.JTextField tfFiltroCatalogo;
   private javax.swing.JTextField tfFiltroProductos;
   // End of variables declaration//GEN-END:variables

   /**
    * Cargar la tabla con los Productos que no estén en el catálogo web
    * (incluyendo destacados y ofertas).
    * @param filterQuery an SQL statement to be sent to the database, typically
    * a static SQL SELECT statement.
    * @throws SQLException
    */
   private void cargarTablaProductos(String filterQuery) throws SQLException {
      ListaPrecios listaPrecios = new ListaPreciosJpaController().findListaPreciosParaCatalogo();
      ResultSet rs;
      if (filterQuery != null) {
         rs = DAO.getJDBCConnection().createStatement().executeQuery(filterQuery);
      } else {
         rs = DAO.getJDBCConnection().createStatement().executeQuery("SELECT o.id FROM producto o "
                 + " WHERE o.id NOT IN (SELECT pw.producto FROM productos_web pw WHERE pw.estado <> " + ProductosWeb.BAJA + ")"
                 + " ORDER BY o.nombre");
      }
      UTIL.limpiarDtm(tableProductos);
      DefaultTableModel dtm = UTIL.getDtm(tableProductos);
      while (rs.next()) {
         Integer id = rs.getInt("id");
         Producto producto = productroController.findProducto(id);
         dtm.addRow(new Object[]{
                    producto.getCodigo(),
                    producto,
                    getPrecioFinal(producto, listaPrecios),
                    producto.getRubro(),
                    producto.getSubrubro() != null ? producto.getSubrubro() : null,
                    (producto.getFoto() != null),
                    (producto.getDescripcion() != null)
                 });
      }
   }

   private void cargarTablaCatalogo(String filterQuery) throws SQLException {
      ResultSet rs;
      if (filterQuery != null) {
         rs = DAO.getJDBCConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                 ResultSet.CONCUR_READ_ONLY).executeQuery(filterQuery);
      } else {
         rs = DAO.getJDBCConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                 ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT o.*, p.codigo, p.nombre"
                 + " FROM productos_web o JOIN producto p ON o.producto = p.id"
                 + " WHERE o.estado <> 3"
                 + " ORDER BY nombre");
      }
      UTIL.limpiarDtm(tableCatalogo);
      DefaultTableModel dtm = UTIL.getDtm(tableCatalogo);
      while (rs.next()) {
         dtm.addRow(new Object[]{
                    rs.getInt("producto"),
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getDouble("precio"),
                    rs.getBoolean("destacado"),
                    rs.getBoolean("oferta")
                 });
      }
   }

   private void filtrarTablaProductos() throws SQLException {
      String query = null;
      query = "SELECT o.id from producto o "
              + "where o.id not in (select pw.producto from productos_web pw WHERE pw.estado <> " + ProductosWeb.BAJA + ")"
              + (tfFiltroProductos.getText().trim().length() > 0 ? " AND o.nombre ILIKE '" + tfFiltroProductos.getText().trim() + "%'" : "")
              + (checkSoloHabilitados.isSelected() ? " AND o.subrubro IS NOT NULL AND o.foto IS NOT NULL AND o.descripcion IS NOT NULL " : "")
              + " ORDER BY o.nombre";
      cargarTablaProductos(query);
   }

   private void filtrarTablaCatalogo() throws SQLException {
      String query = null;
      query = "SELECT o.*, p.codigo, p.nombre"
              + " WHERE productos_web o JOIN producto p ON o.producto = p.id"
              + " AND o.estado <> " + ProductosWeb.BAJA
              + (tfFiltroCatalogo.getText().trim().length() > 0 ? " AND p.nombre ILIKE '" + tfFiltroProductos.getText().trim() + "%'" : "")
              + " ORDER BY destacado, oferta, nombre";
      cargarTablaCatalogo(query);
   }

   private void agregarToCatalogo() throws MessageException, SQLException, Exception {
      TableModel model = tableProductos.getModel();
      for (int rowIndex : tableProductos.getSelectedRows()) {
         if ((model.getValueAt(rowIndex, 4) == null) //si no tiene subRu
                 || !(Boolean) model.getValueAt(rowIndex, 5) // o foto
                 || !(Boolean) model.getValueAt(rowIndex, 6)) { // o descrip
            throw new MessageException("Ha seleccionado Producto/s que no cumple/n los requisitos para formar parte del Catálogo.");
         }
      }
      Producto producto;
      ProductosWeb productosWeb;
      ProductosWeb oldProductosWeb;
      for (int rowIndex : tableProductos.getSelectedRows()) {
         producto = (Producto) model.getValueAt(rowIndex, 1);
         productosWeb = new ProductosWeb();
         productosWeb.setId(producto.getId());
         productosWeb.setProducto(producto);
         productosWeb.setEstado(ProductosWeb.ALTA);
         productosWeb.setChequeado((short) 0);
         productosWeb.setDestacado(false);
         productosWeb.setOferta(false);
         productosWeb.setPrecio((Double) model.getValueAt(rowIndex, 2));
         oldProductosWeb = productosWebJpaController.findProductosWeb(producto.getId());
         if (oldProductosWeb == null) {
            productosWebJpaController.create(productosWeb);
         } else {
            if (oldProductosWeb.getEstado() == ProductosWeb.BAJA) {
               Logger.getLogger(JDCatalogoWEB.class).log(Level.TRACE, "ProductoWeb.id=" + oldProductosWeb.getId() + ", estado=BAJA :::VA A SER REVIVIDO:::");
               productosWeb.setEstado(ProductosWeb.ALTA);
               productosWebJpaController.edit(productosWeb);
            }
         }
      }
   }

   private Double getPrecioFinal(Producto producto, ListaPrecios listaPrecios) {
//      try {
      Double precioFinal = producto.getPrecioVenta() + FacturaVentaJpaController.GET_MARGEN_SEGUN_LISTAPRECIOS(listaPrecios, producto, null);
      Double iva = UTIL.getPorcentaje(precioFinal, producto.getIva().getIva());
      return precioFinal + iva;
//      } catch (NullPointerException ex) {
//         Logger.getLogger(JDCatalogoWEB.class).log(Level.TRACE, "Producto.id=" + producto + ", precioVenta=" + producto.getPrecioVenta());
//      }
//      return null;
   }

   private void quitarFromCatalogo() throws NonexistentEntityException, Exception {
      TableModel model = tableCatalogo.getModel();
      for (int rowIndex : tableCatalogo.getSelectedRows()) {
         int productoID = (Integer) model.getValueAt(rowIndex, 0);
         productosWebJpaController.bajarFromCatalogo(productoID);
      }
   }
}
