package controller;

import controller.exceptions.*;
import entity.Caja;
import entity.CajaMovimientos;
import entity.DetallesCompra;
import entity.FacturaCompra;
import entity.Proveedor;
import entity.Sucursal;
import java.util.Arrays;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class FacturaCompraJpaController implements ActionListener, KeyListener {

   public static final List<String> TIPOS_FACTURA;
   public static final List<String> FORMAS_PAGO;
   public static final String CLASS_NAME = FacturaCompra.class.getSimpleName();
   private final String[] colsName = {"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod"};
   private final int[] colsWidth = {10, 70, 180, 10, 30, 30, 10};
   private JDFacturaCompra jdFactura;
   private Producto producto_selected;
   private FacturaCompra EL_OBJECT;
   private JDBuscadorReRe buscador;

   static {
      String[] tipos = {"A", "B", "C", "M"};
      TIPOS_FACTURA = new ArrayList<String>();
      TIPOS_FACTURA.addAll(Arrays.asList(tipos));
      String[] formas = {"Contado", "Cta. Cte."};
      FORMAS_PAGO = new ArrayList<String>();
      FORMAS_PAGO.addAll(Arrays.asList(formas));
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
            detallesCompra.setFactura(facturaCompra);
            dcController.create(detallesCompra);
         }
      } catch (Exception ex) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
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
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>

      jdFactura = new JDFacturaCompra(frame, modal, 1);
      UTIL.getDefaultTableModel(jdFactura.getjTable1(), colsName, colsWidth);
      //esconde la columna IVA-Producto
      UTIL.hideColumnTable(jdFactura.getjTable1(), 0);
      //set next nº movimiento
      jdFactura.setTfNumMovimiento(String.valueOf(getFacturaCompraCount() + 1));

      UTIL.loadComboBox(jdFactura.getCbProveedor(),
              new ProveedorJpaController().findProveedorEntities(), false);
      UTIL.loadComboBox(jdFactura.getCbSucursal(),
              new SucursalJpaController().findSucursalEntities(), false);
      UTIL.loadComboBox(jdFactura.getCbFacturaTipo(), TIPOS_FACTURA, false);
      UTIL.loadComboBox(jdFactura.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);
      UTIL.loadComboBox(jdFactura.getCbCaja(),
              new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), false);

      jdFactura.setListener(this);
      jdFactura.setLocationByPlatform(true);
      jdFactura.setVisible(true);
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
         cantidad = Integer.valueOf(jdFactura.getTfCantidad());
         if (cantidad < 1) {
            throw new MessageException("La cantidad no puede ser menor a 1");
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Cantidad no válida (solo números enteros)");
      }
      try {
         precioUnitario = Double.valueOf(jdFactura.getTfPrecioUnitario());
         if (precioUnitario < 0) {
            throw new MessageException("El precio unitario no puede ser menor a 0");
         }

      } catch (NumberFormatException ex) {
         throw new MessageException("Precio Unitario no válido");
      }

      // agregando a la tabla el producto
      jdFactura.getDTM().addRow(new Object[]{
                 producto_selected.getIva().toString(),
                 producto_selected.getCodigo(),
                 producto_selected.getNombre(),
                 cantidad,
                 UTIL.PRECIO_CON_PUNTO.format(precioUnitario),
                 UTIL.PRECIO_CON_PUNTO.format((cantidad * precioUnitario)),
                 jdFactura.getCbCambioPrecio().getSelectedIndex() + 1 //se le suma uno para q coincidan con las variables de cambio precio
              });

      refreshResumen();
   }

   private void deleteProductoFromLista() {
      if (jdFactura.getjTable1().getSelectedRow() >= 0) {
         jdFactura.getDTM().removeRow(jdFactura.getjTable1().getSelectedRow());
         refreshResumen();
      }
   }

   private void setProducto() {
      if (producto_selected != null) {
         jdFactura.setTfProductoCodigo(producto_selected.getCodigo());
         jdFactura.setTfProductoNombre(producto_selected.getNombre());
         jdFactura.setTfProductoIVA(producto_selected.getIva().getIva().toString());
         jdFactura.setTfProductoPrecioActual(UTIL.PRECIO_CON_PUNTO.format(producto_selected.getCostoCompra()));
      } else {
         jdFactura.setTfProductoNombre("- producto no encontrado -");
         jdFactura.setTfProductoIVA("");
         jdFactura.setTfProductoPrecioActual("");
      }
   }

   private void refreshResumen() {
      Double gravado = 0.0;//Double.valueOf(contenedor.getTfGravado());
      Double iva10 = 0.0;//Double.valueOf(contenedor.getTfTotalIVA105());
      Double iva21 = 0.0;//Double.valueOf(contenedor.getTfTotalIVA21());
      javax.swing.table.DefaultTableModel dtm = jdFactura.getDTM();
      for (int i = (dtm.getRowCount() - 1); i > -1; i--) {
         gravado += Double.valueOf(dtm.getValueAt(i, 5).toString());

         if (dtm.getValueAt(i, 0).toString().equalsIgnoreCase("10.5")) {
            iva10 += UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 5).toString()), 10.5);
         } else if (dtm.getValueAt(i, 0).toString().equalsIgnoreCase("21.0")) {
            iva21 += UTIL.getPorcentaje(Double.valueOf(dtm.getValueAt(i, 5).toString()), 21);
         }
      }
      jdFactura.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(gravado));
      jdFactura.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(iva10));
      jdFactura.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(iva21));
      jdFactura.setTfTotal(UTIL.PRECIO_CON_PUNTO.format((gravado + iva10 + iva21)));
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
      EL_OBJECT.setFacturaCuarto(Short.valueOf(jdFactura.getTfFacturaCuarto()));
      EL_OBJECT.setFacturaOcteto(Integer.valueOf(jdFactura.getTfFacturaOcteto()));
      EL_OBJECT.setFechaCompra(jdFactura.getDcFechaFactura());
      EL_OBJECT.setFechaalta(new java.util.Date());
      EL_OBJECT.setHoraalta(new java.util.Date());
      EL_OBJECT.setAnulada(false);
      //set entities
      EL_OBJECT.setProveedor((Proveedor) jdFactura.getCbProveedor().getSelectedItem());
      EL_OBJECT.setSucursal((Sucursal) jdFactura.getCbSucursal().getSelectedItem());
      EL_OBJECT.setUsuario(UsuarioJpaController.getCurrentUser());
      EL_OBJECT.setCaja((Caja) jdFactura.getCbCaja().getSelectedItem());

      EL_OBJECT.setImporte(Double.valueOf(jdFactura.getTfTotal()));
      EL_OBJECT.setIva10(Double.valueOf(jdFactura.getTfTotalIVA105()));
      EL_OBJECT.setIva21(Double.valueOf(jdFactura.getTfTotalIVA21()));
      EL_OBJECT.setPercDgr(0.0); // <--- no corresponde en la COMPRA ..BURRO!!
      EL_OBJECT.setPercIva(Double.valueOf(jdFactura.getTfPercIVA()));
      EL_OBJECT.setRemito(0L);
      EL_OBJECT.setTipo(jdFactura.getCbFacturaTipo().getSelectedItem().toString().charAt(0));
      EL_OBJECT.setNumero(Long.valueOf(
              jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto()));
      EL_OBJECT.setActualizaStock(jdFactura.getCheckActualizaStock().isSelected());
      EL_OBJECT.setFormaPago((short) ((Valores.FormaPago) jdFactura.getCbFormaPago().getSelectedItem()).getId());
      if (EL_OBJECT.getFormaPago() == Valores.FormaPago.CTA_CTE.getId()) {
         EL_OBJECT.setDiasCtaCte(Short.valueOf(jdFactura.getTfDias()));
      }
      EL_OBJECT.setMovimiento(Integer.valueOf(jdFactura.getTfNumMovimiento()));
      EL_OBJECT.setDetallesCompraList(new ArrayList<DetallesCompra>());

      //carga detalleCompra
      DetallesCompra detalleCompra;
      DefaultTableModel dtm = jdFactura.getDTM();
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
         ((Proveedor) jdFactura.getCbProveedor().getSelectedItem()).getId();
      } catch (ClassCastException ex) {
         throw new MessageException("Proveedor no válido");
      }
      try {
         ((Sucursal) jdFactura.getCbSucursal().getSelectedItem()).getId();
      } catch (ClassCastException ex) {
         throw new MessageException("Sucursal no válido");
      }
      if (jdFactura.getDcFechaFactura() == null) {
         throw new MessageException("Fecha de factura no válida");
      }

      if (jdFactura.getTfFacturaCuarto().length() > 4) {
         throw new MessageException("Número de factura no válido: " + jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto());
      }

      if (jdFactura.getTfFacturaCuarto().length() > 8) {
         throw new MessageException("Número de factura no válido");
      }

      try {
         if (Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto()) < 100000001) {
            throw new MessageException("Número de factura no válido");
         }

      } catch (NumberFormatException e) {
         throw new MessageException("Los primeros 4 números de la factura no válido");
      }
      try {
         if (Integer.valueOf(jdFactura.getTfFacturaOcteto()) < 1) {
            throw new MessageException("Número de factura no válido");
         }

      } catch (NumberFormatException e) {
         throw new MessageException("Los primeros 4 números de la factura no válido");
      }
      try {
         if (jdFactura.getCbFormaPago().getSelectedItem().toString().equalsIgnoreCase(Valores.FormaPago.CTA_CTE.getNombre())) {
            if (Short.valueOf(jdFactura.getTfDias()) < 1) {
               throw new MessageException("Días no válidos para Forma de pago: " + FORMAS_PAGO.get(1));
            }
         }

      } catch (NumberFormatException e) {
         throw new MessageException("Días no válidos para Forma de pago: " + FORMAS_PAGO.get(1));
      }
      try {
         Double.valueOf(jdFactura.getTfPercIVA());
      } catch (NumberFormatException e) {
         throw new MessageException("Monto de Percepción IVA no válido");
      }

      //si hay productos cargados en la lista de compra!!
      if (jdFactura.getDTM().getRowCount() < 1) {
         throw new MessageException("No hay productos cargados");
      }

      long numeroFactura = Long.valueOf(jdFactura.getTfFacturaCuarto() + jdFactura.getTfFacturaOcteto());
      if (findFacturaCompra(((Proveedor) jdFactura.getCbProveedor().getSelectedItem()).getId(),
              numeroFactura) != null) {
         throw new MessageException("Ya existe la factura Nº: " + numeroFactura
                 + " con el Proveedor " + (Proveedor) jdFactura.getCbProveedor().getSelectedItem());
      }

   }

   private void limpiarPanel() {
      jdFactura.limpiarPanelDatos();
      jdFactura.limpiarPanelProducto();
      jdFactura.limpiarTabla();
      jdFactura.limpiarResumen();
      jdFactura.setTfNumMovimiento(String.valueOf(getFacturaCompraCount() + 1));
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               checkConstraints();
               setEntityAndPersist();
               jdFactura.showMessage("Factura cargada..", CLASS_NAME, 1);
               limpiarPanel();
            } catch (MessageException ex) {
               jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("add")) {
            try {
               addProductoToList();
            } catch (MessageException ex) {
               jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }

         } else if (boton.getName().equalsIgnoreCase("del")) {
            deleteProductoFromLista();

         } else if (boton.getName().equalsIgnoreCase("cancelar")) {
            jdFactura = null;
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

   public void initBuscador(JFrame frame, final boolean modal, final boolean paraAnular) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.COMPRA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>

      buscador = new JDBuscadorReRe(frame, "Buscador - Factura compra", modal, "Proveedor", "Nº Factura");
      UTIL.loadComboBox(buscador.getCbClieProv(), new ProveedorJpaController().findProveedorEntities(), true);
      UTIL.loadComboBox(buscador.getCbCaja(), new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);
      UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
      UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
      UTIL.getDefaultTableModel(
              buscador.getjTable1(),
              new String[]{"facturaID", "Nº factura", "Mov.", "Proveedor", "Importe", "Fecha", "Sucursal", "Caja", "Usuario", "Fecha (Sistema)"},
              new int[]{1, 30, 10, 50, 50, 50, 80, 80, 50, 70});
      //escondiendo facturaID
      UTIL.hideColumnTable(buscador.getjTable1(), 0);
      buscador.getjTable1().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
               if (buscador.getjTable1().getSelectedRow() > -1) {
                  EL_OBJECT = findFacturaCompra(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                  setDatosFactura(EL_OBJECT, paraAnular);
               }
            }
         }
      });
      if (paraAnular) {
         //solo buscar facturas NO anuladas
         buscador.getCheckAnulada().setEnabled(false);
      }
      buscador.setListeners(this);
      buscador.setLocationRelativeTo(frame);
      buscador.setVisible(true);
   }

   private void cargarDtmBuscador(String query) {
      buscador.dtmRemoveAll();
      DefaultTableModel dtm = buscador.getDtm();
      List<FacturaCompra> l = DAO.getEntityManager().createNativeQuery(query, FacturaCompra.class).getResultList();
      for (FacturaCompra facturaCompra : l) {
         dtm.addRow(new Object[]{
                    facturaCompra.getId(),
                    UTIL.AGREGAR_CEROS(facturaCompra.getNumero(), 12),
                    facturaCompra.getMovimiento(),
                    facturaCompra.getProveedor(),
                    facturaCompra.getImporte(),
                    UTIL.DATE_FORMAT.format(facturaCompra.getFechaCompra()),
                    facturaCompra.getSucursal(),
                    facturaCompra.getCaja(),
                    facturaCompra.getUsuario(),
                    UTIL.DATE_FORMAT.format(facturaCompra.getFechaalta()) + " (" + UTIL.TIME_FORMAT.format(facturaCompra.getHoraalta()) + ")"
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


      if (buscador.getTfFactu4().trim().length() > 0) {
         try {
            query += " AND o.movimiento = " + Integer.valueOf(buscador.getTfFactu4());
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de movimiento no válido");
         }
      }
      query += //" GROUP BY o.id, o.fecha_carga, o.hora_carga, o.monto_entrega, o.usuario, o.caja, o.sucursal, o.fecha_remesa, o.estado"
              " ORDER BY o.id";
      System.out.println("QUERY: " + query);
      cargarDtmBuscador(query);
   }

   private void setDatosFactura(final FacturaCompra factura, boolean paraAnular) {
      jdFactura = new JDFacturaCompra(null, true, 1);
      jdFactura.setLocationRelativeTo(buscador);
      UTIL.getDefaultTableModel(jdFactura.getjTable1(), colsName, colsWidth);
      //esconde la columna IVA-Producto
      UTIL.hideColumnTable(jdFactura.getjTable1(), 0);
      if (paraAnular) {
         jdFactura.getBtnAnular().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                  String msg_extra_para_ctacte = factura.getFormaPago() == Valores.FormaPago.CTA_CTE.getId() ? "\n- Remesas de pago de Cta.Cte." : "";
                  if (0 == JOptionPane.showOptionDialog(jdFactura, "- Factura Nº:" + UTIL.AGREGAR_CEROS(factura.getNumero() + "\n- Movimiento de Caja\n- Movimiento de Stock" + msg_extra_para_ctacte, 12), "Confirmación de anulación", JOptionPane.YES_OPTION, 2, null, null, null)) {
                     anular(factura);
                  }
                  jdFactura.showMessage("Anulada", CLASS_NAME, 2);
                  jdFactura.dispose();
               } catch (MessageException ex) {
                  jdFactura.showMessage(ex.getMessage(), CLASS_NAME, 2);
               }
            }
         });
      }
      // seteando datos de FacturaCompra
      jdFactura.getCbProveedor().addItem(factura.getProveedor());
      jdFactura.getCbSucursal().addItem(factura.getSucursal());
      jdFactura.setDcFechaFactura(factura.getFechaCompra());
      String numFactura = UTIL.AGREGAR_CEROS(factura.getNumero(), 12);
      jdFactura.setTfFacturaCuarto(numFactura.substring(0, 4));
      jdFactura.setTfFacturaOcteto(numFactura.substring(4));
      jdFactura.setTfNumMovimiento(String.valueOf(factura.getMovimiento()));

      jdFactura.getCbFacturaTipo().addItem(factura.getTipo());
      jdFactura.getCbCaja().addItem(factura.getCaja());
      UTIL.loadComboBox(jdFactura.getCbFormaPago(), Valores.FormaPago.getFormasDePago(), false);

      jdFactura.getCbFormaPago().setSelectedIndex(factura.getFormaPago() - 1);
      List<DetallesCompra> lista = new DetallesCompraJpaController().findByFactura(factura);
      DefaultTableModel dtm = jdFactura.getDTM();
      for (DetallesCompra detallesCompra : lista) {
         //"IVA", "Cód. Producto", "Producto", "Cantidad", "Precio U.", "Sub total", "Mod
         dtm.addRow(new Object[]{
                    null,
                    detallesCompra.getProducto().getCodigo(),
                    detallesCompra.getProducto(),
                    detallesCompra.getCantidad(),
                    detallesCompra.getPrecioUnitario(),
                    UTIL.PRECIO_CON_PUNTO.format((detallesCompra.getCantidad() * detallesCompra.getPrecioUnitario())),});
      }
      //totales
      jdFactura.setTfGravado(
              UTIL.PRECIO_CON_PUNTO.format(factura.getImporte() - (factura.getIva10() + factura.getIva21())));
      jdFactura.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(factura.getIva10()));
      jdFactura.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(factura.getIva21()));
      jdFactura.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(factura.getImporte()));
      jdFactura.modoVista(false);
      jdFactura.setListener(this);
      jdFactura.setVisible(true);

   }

   public void anular(FacturaCompra factura) throws MessageException {
      if (factura.getAnulada()) {
         throw new MessageException("Ya está anulada esta factura");
      }

      Caja oldCaja = factura.getCaja();
      List<Caja> cajasPermitidasList = new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true);
      if (cajasPermitidasList.isEmpty()) {
         throw new MessageException("No tiene acceso a ninguna Caja del sistema");
      }
      if (!cajasPermitidasList.contains(oldCaja)) {
         throw new MessageException("No tiene permiso para modificar la Caja " + oldCaja + ", con la que fue realizada la venta.");
      }
      CajaMovimientosJpaController cmController = new CajaMovimientosJpaController();
      CajaMovimientos cajaMovimientoActiva;
      try {
         cajaMovimientoActiva = cmController.findCajaMovimientoAbierta(oldCaja);
      } catch (NoResultException e) {
         throw new MessageException("ERROR DE SISTEMA\nHay un problema con la Caja " + oldCaja.getNombre());
      }
      if (cajaMovimientoActiva.getCaja().isBaja() || (!cajaMovimientoActiva.getCaja().getEstado())) {
         cajaMovimientoActiva = new FacturaVentaJpaController().initReAsignacionCajaMovimiento(cajasPermitidasList);
         if (cajaMovimientoActiva == null) {
            return;
         }
      }
      try {
         cmController.anular(factura, cajaMovimientoActiva);
      } catch (Exception ex) {
         Logger.getLogger(FacturaVentaJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }

   }

   private void imprimirFactura() {
      if (EL_OBJECT == null) {
         return;
      }
      try {
         Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_FacturaCompra.jasper", "Factura compra");
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

   public JDFacturaCompra getContenedor() {
      return jdFactura;
   }
}
