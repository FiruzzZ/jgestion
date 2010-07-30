package controller;

import controller.exceptions.*;
import entity.Caja;
import entity.DetallesCompra;
import entity.FacturaCompra;
import entity.Proveedor;
import entity.Sucursal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import entity.Producto;
import entity.UTIL;
import gui.JDBuscadorReRe;
import gui.JDFacturaCompra;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class FacturaCompraJpaController
        implements ActionListener, MouseListener, KeyListener {

   public static final List<String> TIPOS_FACTURA;
   public static final List<String> FORMAS_PAGO;
   public static final String CLASS_NAME = "FacturaCompra";
   private final String[] colsName = {"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod"};
   private final int[] colsWidth = {10, 70, 180, 10, 30, 30, 10};
   private JDFacturaCompra contenedor;
   private Producto producto_selected;
   private FacturaCompra EL_OBJECT;
   private JDBuscadorReRe buscador;

   static {
      String[] tipos = {"A", "B", "C", "M"};
      TIPOS_FACTURA = new ArrayList<String>();
      for (String string : tipos) {
         TIPOS_FACTURA.add(string);
      }
      String[] formas = {"Contado", "Cta. Cte."};
      FORMAS_PAGO = new ArrayList<String>();
      for (String string : formas) {
         FORMAS_PAGO.add(string);
      }
   }

   // <editor-fold defaultstate="collapsed" desc="CRUD, List's">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(FacturaCompra facturaCompra) throws Exception {
      if (facturaCompra.getDetallesCompraList() == null) {
         //nunca debería pasar esto....
      }
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         List<DetallesCompra> detallesCompraListToPersist = facturaCompra.getDetallesCompraList();
         facturaCompra.setDetallesCompraList(new ArrayList<DetallesCompra>());
         em.persist(facturaCompra);
         em.getTransaction().commit();
         DetallesCompraJpaController dcController = new DetallesCompraJpaController();
         for (DetallesCompra detallesCompra : detallesCompraListToPersist) {
            detallesCompra.setFacturaCompra(facturaCompra);
            dcController.create(detallesCompra);
         }
      } catch(Exception ex) {
         if(em.getTransaction().isActive())
            em.getTransaction().rollback();
         throw ex;
      } finally { if (em != null) {  em.close();  }
      }
   }

   public void edit(FacturaCompra facturaCompra) throws IllegalOrphanException, NonexistentEntityException, Exception {
      //no se edita
   }

   public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
      // ni se borra
   }

   public List<FacturaCompra> findFacturaCompraEntities() {
      return findFacturaCompraEntities(true, -1, -1);
   }

   public List<FacturaCompra> findFacturaCompraEntities(int maxResults, int firstResult) {
      return findFacturaCompraEntities(false, maxResults, firstResult);
   }

   private List<FacturaCompra> findFacturaCompraEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from FacturaCompra as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public FacturaCompra findFacturaCompra(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(FacturaCompra.class, id);
      } finally {
         em.close();
      }
   }

   public Integer getFacturaCompraCount() {
      EntityManager em = getEntityManager();
      try {
         return ((Long) em.createQuery("select count(o) from FacturaCompra as o").getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   public void initJDFacturaCompra(java.awt.Frame frame, boolean modal) {

      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.COMPRA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage());
         return;
      }// </editor-fold>

      contenedor = new JDFacturaCompra(frame, modal);
      UTIL.getDefaultTableModel(colsName, colsWidth, contenedor.getjTable1());
      //esconde la columna IVA-Producto
      UTIL.hideColumnTable(contenedor.getjTable1(), 0);
      //set next nº movimiento
      contenedor.setTfNumMovimiento(String.valueOf(getFacturaCompraCount() + 1));

      UTIL.loadComboBox(contenedor.getCbProveedor(),
              new ProveedorJpaController().findProveedorEntities(), false);
      UTIL.loadComboBox(contenedor.getCbSucursal(),
              new SucursalJpaController().findSucursalEntities(), false);
      UTIL.loadComboBox(contenedor.getCbFacturaTipo(), TIPOS_FACTURA, false);
      UTIL.loadComboBox(contenedor.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
      UTIL.loadComboBox(contenedor.getCbCaja(),
              new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), false);

      contenedor.setListener(this);
      contenedor.setLocationByPlatform(true);
      contenedor.setVisible(true);
   }

   private void buscarProducto(String codigoProducto) {
      producto_selected = new ProductoJpaController().findProductoByCodigo(codigoProducto);
      setProducto();
   }

   private void addProductoToList() throws MessageException {
      if (producto_selected == null) {
         throw new MessageException("Seleccione un producto");
      }

      int cantidad;
      double precioUnitario;
      try {
         cantidad = Integer.valueOf(contenedor.getTfCantidad());
         if (cantidad < 1) {
            throw new MessageException("La cantidad no puede ser menor a 1");
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Cantidad no válida (solo números enteros)");
      }
      try {
         precioUnitario = Double.valueOf(contenedor.getTfPrecioUnitario());
         if (precioUnitario < 0) {
            throw new MessageException("El precio unitario no puede ser menor a 0");
         }

      } catch (NumberFormatException ex) {
         throw new MessageException("Precio Unitario no válido");
      }

      // agregando a la tabla el producto
      contenedor.getDTM().addRow(new Object[]{
                 producto_selected.getIva().toString(),
                 producto_selected.getCodigo(),
                 producto_selected.getNombre(),
                 cantidad,
                 UTIL.PRECIO_CON_PUNTO.format(precioUnitario),
                 UTIL.PRECIO_CON_PUNTO.format((cantidad * precioUnitario)),
                 contenedor.getCbCambioPrecio().getSelectedIndex() + 1 //se le suma uno para q coincidan con las variables de cambio precio
              });

      refreshResumen();
   }

   private void deleteProductoFromLista() {
      if (contenedor.getjTable1().getSelectedRow() >= 0) {
         contenedor.getDTM().removeRow(contenedor.getjTable1().getSelectedRow());
         refreshResumen();
      }
   }

   private void setProducto() {
      if (producto_selected != null) {
         contenedor.setTfProductoCodigo(producto_selected.getCodigo());
         contenedor.setTfProductoNombre(producto_selected.getNombre());
         contenedor.setTfProductoIVA(producto_selected.getIva().getIva().toString());
         contenedor.setTfProductoPrecioActual(UTIL.PRECIO_CON_PUNTO.format(producto_selected.getCostoCompra()));
      } else {
         contenedor.setTfProductoNombre("- producto no encontrado -");
         contenedor.setTfProductoIVA("");
         contenedor.setTfProductoPrecioActual("");
      }
   }

   private void refreshResumen() {
      Double gravado = 0.0;//Double.valueOf(contenedor.getTfGravado());
      Double iva10 = 0.0;//Double.valueOf(contenedor.getTfTotalIVA105());
      Double iva21 = 0.0;//Double.valueOf(contenedor.getTfTotalIVA21());
      javax.swing.table.DefaultTableModel dtm = contenedor.getDTM();
      for (int i = (dtm.getRowCount() - 1); i > -1; i--) {
         gravado += Double.valueOf(dtm.getValueAt(i, 5).toString());

         if (dtm.getValueAt(i, 0).toString().equalsIgnoreCase("10.5")) {
            iva10 += UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 5).toString()), 10.5);
         } else if (dtm.getValueAt(i, 0).toString().equalsIgnoreCase("21.0")) {
            iva21 += UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 5).toString()), 21);
         }
      }
      contenedor.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(gravado));
      contenedor.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(iva10));
      contenedor.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(iva21));
      contenedor.setTfTotal(UTIL.PRECIO_CON_PUNTO.format((gravado + iva10 + iva21)));
   }

   public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() >= 2) {
         if (buscador != null) {
            getSelectedFacturaCompraFromBuscador();
         }
      }
   }

   public void mousePressed(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {
   }

   public void keyTyped(KeyEvent e) {
      if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
         if (tf.getName().equalsIgnoreCase("cuarteto")) {
            if (tf.getText().length() < 4) {
               UTIL.soloNumeros(e);
            } else {
               e.setKeyChar((char) KeyEvent.VK_CLEAR);
            }
         } else if (tf.getName().equalsIgnoreCase("octeto")) {
            if (tf.getText().length() < 8) {
               UTIL.soloNumeros(e);
            } else {
               e.setKeyChar((char) KeyEvent.VK_CLEAR);
            }
         } else if (tf.getName().equalsIgnoreCase("dias")) {
            if (tf.getText().length() >= 3) {
               e.setKeyChar((char) KeyEvent.VK_CLEAR);
            }
         }
      }
   }

   public void keyReleased(KeyEvent e) {
      if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
         if (tf.getName().equalsIgnoreCase("productoCodigo") && tf.isFocusOwner()) {
            if (tf.getText().length() > 0 && e.getKeyCode() == 10) {
               buscarProducto(tf.getText());
            }
         }
      }
   }

   public void keyPressed(KeyEvent e) {
   }

   private void setEntityAndPersist() throws Exception {
      EL_OBJECT = new FacturaCompra();
      EL_OBJECT.setFacturaCuarto(Short.valueOf(contenedor.getTfFacturaCuarto()));
      EL_OBJECT.setFacturaOcteto(Integer.valueOf(contenedor.getTfFacturaOcteto()));
      EL_OBJECT.setFechaCompra(contenedor.getDcFechaFactura());
      EL_OBJECT.setFechaalta(new java.util.Date());
      EL_OBJECT.setHoraalta(new java.util.Date());
      EL_OBJECT.setAnulada(false);
      //set entities
      EL_OBJECT.setProveedor((Proveedor) contenedor.getCbProveedor().getSelectedItem());
      EL_OBJECT.setSucursal((Sucursal) contenedor.getCbSucursal().getSelectedItem());
      EL_OBJECT.setUsuario(UsuarioJpaController.getCurrentUser());
      EL_OBJECT.setCaja((Caja) contenedor.getCbCaja().getSelectedItem());

      EL_OBJECT.setImporte(Double.valueOf(contenedor.getTfTotal()));
      EL_OBJECT.setIva10(Double.valueOf(contenedor.getTfTotalIVA105()));
      EL_OBJECT.setIva21(Double.valueOf(contenedor.getTfTotalIVA21()));
      EL_OBJECT.setPercDgr(0.0); // <--- no corresponde en la COMPRA ..BURRO!!
      EL_OBJECT.setPercIva(Double.valueOf(contenedor.getTfPercIVA()));
      EL_OBJECT.setRemito(0L);
      EL_OBJECT.setTipo(contenedor.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
      EL_OBJECT.setNumero(Long.valueOf(
              contenedor.getTfFacturaCuarto() + contenedor.getTfFacturaOcteto()));
      EL_OBJECT.setActualizaStock(contenedor.getCheckActualizaStock().isSelected());
      EL_OBJECT.setFormaPago((short) ((Valores.FormaPago) contenedor.getCbFormaPago().getSelectedItem()).getId());
      if (EL_OBJECT.getFormaPago() == Valores.FormaPago.CTA_CTE.getId()) {
         EL_OBJECT.setDiasCtaCte(Short.valueOf(contenedor.getTfDias()));
      }
      EL_OBJECT.setMovimiento(Integer.valueOf(contenedor.getTfNumMovimiento()));
      EL_OBJECT.setDetallesCompraList(new ArrayList<DetallesCompra>());

      //carga detalleCompra
      DetallesCompra detalleCompra;
      DefaultTableModel dtm = contenedor.getDTM();
      ProductoJpaController productoCtrl = new ProductoJpaController();
      for (int i = 0; i < dtm.getRowCount(); i++) {
         detalleCompra = new DetallesCompra();
         detalleCompra.setProducto(new ProductoJpaController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
         detalleCompra.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
         detalleCompra.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
         EL_OBJECT.getDetallesCompraList().add(detalleCompra);
         productoCtrl.updateCostoCompra(detalleCompra.getProducto(),
                                       detalleCompra.getPrecioUnitario(),
                                       detalleCompra.getCantidad(),
                                       Integer.parseInt(dtm.getValueAt(i, 6).toString()));
      }

      // 1- PERSIST, 2- UPDATE STOCK, 3- UPDATE CAJA
      try {
         //persistiendo
         create(EL_OBJECT);

         //actualiza Stock
         //y también la variable Producto.stockActual
         if (EL_OBJECT.getActualizaStock()) {
            new StockJpaController().updateStock(EL_OBJECT);
         }

         //asiento en caja..
         asentarSegunFormaDePago(EL_OBJECT.getFormaPago());

      } catch (Exception ex) {
         throw ex;
      }
   }

   private void asentarSegunFormaDePago(int formaPago) throws Exception {
      switch (formaPago) {
         case 1: { // CONTADO
            new CajaMovimientosJpaController().asentarMovimiento(EL_OBJECT);
            break;
         }
         case 2: { // CTA CTE Proveedor (NO HAY NINGÚN MOVIMIENTO DE CAJA)
            new CtacteProveedorJpaController().nuevaCtaCte(EL_OBJECT);
            break;
         }
         case 3: { // CHEQUE
            //UNIMPLEMENTED !!!....yet
         }
         default: {
            // acá se pudre todo....
         }
      }
   }

   private void checkConstraints() throws MessageException, Exception {
      try {
         ((Proveedor) contenedor.getCbProveedor().getSelectedItem()).getId();
      } catch (ClassCastException ex) {
         throw new MessageException("Proveedor no válido");
      }
      try {
         ((Sucursal) contenedor.getCbSucursal().getSelectedItem()).getId();
      } catch (ClassCastException ex) {
         throw new MessageException("Sucursal no válido");
      }
      if (contenedor.getDcFechaFactura() == null) {
         throw new MessageException("Fecha de factura no válida");
      }

      if (contenedor.getTfFacturaCuarto().length() > 4) {
         throw new MessageException("Número de factura no válido: " + contenedor.getTfFacturaCuarto() + contenedor.getTfFacturaOcteto());
      }

      if (contenedor.getTfFacturaCuarto().length() > 8) {
         throw new MessageException("Número de factura no válido");
      }

      try {
         if (Long.valueOf(contenedor.getTfFacturaCuarto() + contenedor.getTfFacturaOcteto()) < 100000001) {
            throw new MessageException("Número de factura no válido");
         }

      } catch (NumberFormatException e) {
         throw new MessageException("Los primeros 4 números de la factura no válido");
      }
      try {
         if (Integer.valueOf(contenedor.getTfFacturaOcteto()) < 1) {
            throw new MessageException("Número de factura no válido");
         }

      } catch (NumberFormatException e) {
         throw new MessageException("Los primeros 4 números de la factura no válido");
      }
      try {
         if (contenedor.getCbFormaPago().getSelectedItem().toString()
                 .equalsIgnoreCase(Valores.FormaPago.CTA_CTE.getNombre())) {
            if (Short.valueOf(contenedor.getTfDias()) < 1) {
               throw new MessageException("Días no válidos para Forma de pago: " + FORMAS_PAGO.get(1));
            }
         }

      } catch (NumberFormatException e) {
         throw new MessageException("Días no válidos para Forma de pago: " + FORMAS_PAGO.get(1));
      }
      try {
         Double.valueOf(contenedor.getTfPercIVA());
      } catch (NumberFormatException e) {
         throw new MessageException("Monto de Percepción IVA no válido");
      }

      //si hay productos cargados en la lista de compra!!
      if (contenedor.getDTM().getRowCount() < 1) {
         throw new MessageException("No hay productos cargados");
      }

      long numeroFactura = Long.valueOf(contenedor.getTfFacturaCuarto() + contenedor.getTfFacturaOcteto());
      if (findFacturaCompra(((Proveedor) contenedor.getCbProveedor().getSelectedItem()).getId(),
              numeroFactura) != null) {
         throw new MessageException("Ya existe la factura Nº: " + numeroFactura
                 + " con el Proveedor " + (Proveedor) contenedor.getCbProveedor().getSelectedItem());
      }

   }

   private void limpiarPanel() {
      contenedor.limpiarPanelDatos();
      contenedor.limpiarPanelProducto();
      contenedor.limpiarTabla();
      contenedor.limpiarResumen();
      contenedor.setTfNumMovimiento(String.valueOf(getFacturaCompraCount() + 1));
   }

   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               checkConstraints();
               setEntityAndPersist();
               contenedor.showMessage("Factura cargada..", CLASS_NAME, 1);
               limpiarPanel();
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("add")) {
            try {
               addProductoToList();
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }

         } else if (boton.getName().equalsIgnoreCase("del")) {
            deleteProductoFromLista();

         } else if (boton.getName().equalsIgnoreCase("cancelar")) {
            contenedor = null;
            EL_OBJECT = null;
         } else if (boton.getName().equalsIgnoreCase("buscarProducto")) {
            initBuscadorProducto();
         } else if (boton.getName().equalsIgnoreCase("filtrarReRe")) {
            try {
               armarQuery();
            } catch (MessageException ex) {
               Logger.getLogger(FacturaCompraJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }

         return;
      }// </editor-fold>

      // <editor-fold defaultstate="collapsed" desc="JTextField">
      if (e.getSource().equals(javax.swing.JTextField.class)) {
         javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
         if (tf.getName().equalsIgnoreCase("productoCodigo")) {
         }
      }// </editor-fold>

   }

   private FacturaCompra findFacturaCompra(Integer idProveedor, Long numeroFactura) {
      try {
         return (FacturaCompra) DAO.getEntityManager().createNativeQuery(
                 "SELECT * from factura_compra o where o.proveedor =" + idProveedor
                 + " AND o.numero = " + numeroFactura, FacturaCompra.class).getSingleResult();
      } catch (NoResultException ex) {
         return null;
      }
   }

   public FacturaCompra findFacturaCompra(long numero, Proveedor p) {
      return (FacturaCompra) DAO.getEntityManager().createNamedQuery("FacturaCompra.findByNumeroProveedor").setParameter("numero", numero).setParameter("proveedor", p.getId()).getSingleResult();

   }

   public void initBuscador(java.awt.Frame aThis) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.COMPRA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage());
         return;
      }// </editor-fold>
      buscador = new JDBuscadorReRe(aThis, "Buscador - Factura compra", true, "Proveedor", "Nº Factura");
      UTIL.loadComboBox(buscador.getCbClieProv(), new ProveedorJpaController().findProveedorEntities(), true);
      UTIL.loadComboBox(buscador.getCbCaja(), new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);
      UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
      UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
      try {
         UTIL.getDefaultTableModel(
                 new String[]{"facturaID", "Nº factura","Mov.", "Proveedor", "Importe", "Fecha", "Sucursal", "Caja", "Usuario", "Fecha (Sistema)"},
                 new int[]{1, 30, 10, 50, 50, 50, 80, 80, 50, 70},
                 buscador.getjTable1());
         //escondiendo facturaID
         UTIL.hideColumnTable(buscador.getjTable1(), 0);
      } catch (Exception ex) {
         Logger.getLogger(FacturaCompraJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      buscador.setListeners(this);
      buscador.setLocationRelativeTo(aThis);
      buscador.setVisible(true);
   }

   private void cargarDtmBuscador(String query) {
      buscador.dtmRemoveAll();
      DefaultTableModel dtm = buscador.getDtm();
      List<FacturaCompra> l = DAO.getEntityManager().createNativeQuery(query, FacturaCompra.class).getResultList();
      for (FacturaCompra facturaCompra : l) {
         dtm.addRow(new Object[]{
              facturaCompra.getId(),
              facturaCompra.getNumero(),
              facturaCompra.getMovimiento(),
              facturaCompra.getProveedor(),
              facturaCompra.getImporte(),
              UTIL.DATE_FORMAT.format(facturaCompra.getFechaCompra()),
              facturaCompra.getSucursal(),
              facturaCompra.getCaja(),
              facturaCompra.getUsuario(),
              UTIL.DATE_FORMAT.format(facturaCompra.getFechaalta()) + " (" + UTIL.TIME_FORMAT.format(facturaCompra.getHoraalta())+")"
           });
      }
   }

   private void armarQuery() throws MessageException {
      String query = "SELECT o.* FROM factura_compra o"
              + " WHERE o.anulada = " + buscador.isCheckAnuladaSelected();

      long numero;
      //filtro por nº de ReRe
      if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
            query += " AND o.numero = " + numero;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido");
         }
      }

      //filtro por nº de factura
      if (buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
            query += " AND o.numero = " + numero;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido");
         }
      }
      if (buscador.getDcDesde() != null) {
         query += " AND o.fecha_compra >= '" + buscador.getDcDesde() + "'";
      }
      if (buscador.getDcHasta() != null) {
         query += " AND o.fecha_compra <= '" + buscador.getDcHasta() + "'";
      }
      if (buscador.getCbCaja().getSelectedIndex() > 0) {
         query += " AND o.caja = " + ((Caja) buscador.getCbCaja().getSelectedItem()).getId();
      }
      if (buscador.getCbSucursal().getSelectedIndex() > 0) {
         query += " AND o.sucursal = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId();
      }

      if (buscador.getCbClieProv().getSelectedIndex() > 0) {
         query += " AND o.proveedor = " + ((Proveedor) buscador.getCbClieProv().getSelectedItem()).getId();
      }

      if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
         query += " AND o.forma_pago = " + ((Valores.FormaPago) buscador.getCbFormasDePago().getSelectedItem()).getId();
      }

      
      if(buscador.getTfFactu4().trim().length() > 0) {
         try {
            query += " AND o.movimiento = " + Integer.valueOf(buscador.getTfFactu4());
         } catch(NumberFormatException ex) {
            throw new MessageException("Número de movimiento no válido");
         }
      }
      query += //" GROUP BY o.id, o.fecha_carga, o.hora_carga, o.monto_entrega, o.usuario, o.caja, o.sucursal, o.fecha_remesa, o.estado"
              " ORDER BY o.id";
      System.out.println("QUERY: " + query);
      cargarDtmBuscador(query);
   }

   private void getSelectedFacturaCompraFromBuscador() {
      if (buscador.getjTable1().getSelectedRow() > -1) {
         EL_OBJECT = findFacturaCompra(Integer.valueOf(
                 buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
         setDatosFactura();
      }
   }

   private void setDatosFactura() {
      contenedor = new JDFacturaCompra(null, true);
      contenedor.setLocationRelativeTo(buscador);
      try {
         UTIL.getDefaultTableModel(colsName, colsWidth, contenedor.getjTable1());
         //esconde la columna IVA-Producto
         UTIL.hideColumnTable(contenedor.getjTable1(), 0);
      } catch (Exception ex) {
         Logger.getLogger(FacturaCompraJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }

      // seteando datos de FacturaCompra
      contenedor.getCbProveedor().addItem(EL_OBJECT.getProveedor());
      contenedor.getCbSucursal().addItem(EL_OBJECT.getSucursal());
      contenedor.setDcFechaFactura(EL_OBJECT.getFechaCompra());
      String numFactura = UTIL.AGREGAR_CEROS(EL_OBJECT.getNumero(), 12);
      contenedor.setTfFacturaCuarto(numFactura.substring(0, 4));
      contenedor.setTfFacturaOcteto(numFactura.substring(4));
      contenedor.setTfNumMovimiento(String.valueOf(EL_OBJECT.getMovimiento()));

      contenedor.getCbFacturaTipo().addItem(EL_OBJECT.getTipo());
      contenedor.getCbCaja().addItem(EL_OBJECT.getCaja());
      UTIL.loadComboBox(contenedor.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
      
      contenedor.getCbFormaPago().setSelectedIndex(EL_OBJECT.getFormaPago() - 1);
      List<DetallesCompra> lista = EL_OBJECT.getDetallesCompraList();
      DefaultTableModel dtm = contenedor.getDTM();
      for (DetallesCompra detallesCompra : lista) {
         //"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod
         dtm.addRow(new Object[] {
            null,
            detallesCompra.getProducto().getCodigo(),
            detallesCompra.getProducto(),
            detallesCompra.getCantidad(),
            detallesCompra.getPrecioUnitario(),
            UTIL.PRECIO_CON_PUNTO.format((detallesCompra.getCantidad() * detallesCompra.getPrecioUnitario())),
         });
      }
      //totales
      contenedor.setTfGravado(
              UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getImporte() - (EL_OBJECT.getIva10() + EL_OBJECT.getIva21())));
      contenedor.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getIva10()));
      contenedor.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getIva21()));
      contenedor.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(EL_OBJECT.getImporte()));
      contenedor.modoVista(false); //   <----------------------
      contenedor.setListener(this);

      contenedor.setVisible(true);

   }


   private void imprimirFactura() {
      if(EL_OBJECT == null)
         return;
      try {
         Reportes r = new Reportes(Reportes.FOLDER_REPORTES+"JGestion_FacturaCompra.jasper" , "Factura compra");
         r.addParameter("FACTURA_ID", EL_OBJECT.getId());
         r.addParameter("FORMA_PAGO", Valores.FormaPago.getFormasDePago(EL_OBJECT.getFormaPago()).getNombre());
         r.viewReport();
      } catch (Exception ex) {
         Logger.getLogger(FacturaCompraJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   private void initBuscadorProducto() {
      ProductoJpaController p = new ProductoJpaController();
      p.initContenedor(null, true, true);
      producto_selected = p.getProductoSelected();
      setProducto();
   }
}
