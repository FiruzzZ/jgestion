package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.Cliente;
import entity.Remito;
import gui.JDFacturaVenta;
import gui.JFP;
import java.awt.event.MouseEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.DetalleRemito;
import entity.Sucursal;
import entity.UTIL;
import entity.Usuario;
import gui.JDBuscadorReRe;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrador
 */
public class RemitoJpaController implements ActionListener, KeyListener {

   public static final String CLASS_NAME = "Remito";
   private final FacturaVentaJpaController facturaVentaController;
   private JDFacturaVenta contenedor;
   private JDBuscadorReRe buscador;
   private boolean MODO_VISTA;
   private Remito selectedRemito;
   private JDFacturaVenta jdFacturaVenta;
   private boolean toFacturar;

   public RemitoJpaController() {
      facturaVentaController = new FacturaVentaJpaController();
   }

   // <editor-fold defaultstate="collapsed" desc="CRUD..">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public Remito create(Remito remito) throws PreexistingEntityException, Exception {
      if (remito.getDetalleRemitoList() == null) {
         remito.setDetalleRemitoList(new ArrayList<DetalleRemito>());
      }
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         List<DetalleRemito> attachedDetalleRemitoList = new ArrayList<DetalleRemito>();
         for (DetalleRemito detalleRemitoListDetalleRemitoToAttach : remito.getDetalleRemitoList()) {
            detalleRemitoListDetalleRemitoToAttach = em.merge(detalleRemitoListDetalleRemitoToAttach);
            attachedDetalleRemitoList.add(detalleRemitoListDetalleRemitoToAttach);
         }
         remito.setDetalleRemitoList(attachedDetalleRemitoList);
         em.persist(remito);
         for (DetalleRemito detalleRemitoListDetalleRemito : remito.getDetalleRemitoList()) {
            Remito oldRemitoOfDetalleRemitoListDetalleRemito = detalleRemitoListDetalleRemito.getRemito();
            detalleRemitoListDetalleRemito.setRemito(remito);
            detalleRemitoListDetalleRemito = em.merge(detalleRemitoListDetalleRemito);
            if (oldRemitoOfDetalleRemitoListDetalleRemito != null) {
               oldRemitoOfDetalleRemitoListDetalleRemito.getDetalleRemitoList().remove(detalleRemitoListDetalleRemito);
               oldRemitoOfDetalleRemitoListDetalleRemito = em.merge(oldRemitoOfDetalleRemitoListDetalleRemito);
            }
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         if (findRemito(remito.getId()) != null) {
            throw new PreexistingEntityException("Remito " + remito + " already exists.", ex);
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
      return remito;
   }

   public void edit(Remito remito) throws IllegalOrphanException, NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.find(Remito.class, remito.getId());
         remito = em.merge(remito);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = remito.getId();
            if (findRemito(id) == null) {
               throw new NonexistentEntityException("The remito with id " + id + " no longer exists.");
            }
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Remito remito;
         try {
            remito = em.getReference(Remito.class, id);
            remito.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The remito with id " + id + " no longer exists.", enfe);
         }
         List<String> illegalOrphanMessages = null;
         List<DetalleRemito> detalleRemitoListOrphanCheck = remito.getDetalleRemitoList();
         for (DetalleRemito detalleRemitoListOrphanCheckDetalleRemito : detalleRemitoListOrphanCheck) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("This Remito (" + remito + ") cannot be destroyed since the DetalleRemito " + detalleRemitoListOrphanCheckDetalleRemito + " in its detalleRemitoList field has a non-nullable remito field.");
         }
         if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
         }
         em.remove(remito);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Remito> findRemitoEntities() {
      return findRemitoEntities(true, -1, -1);
   }

   public List<Remito> findRemitoEntities(int maxResults, int firstResult) {
      return findRemitoEntities(false, maxResults, firstResult);
   }

   private List<Remito> findRemitoEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from Remito as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Remito findRemito(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(Remito.class, id);
      } finally {
         em.close();
      }
   }

   public int getRemitoCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from Remito as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   public JDBuscadorReRe getBuscador() {
      return buscador;
   }

   public void initRemito(javax.swing.JFrame frame, boolean modal) throws MessageException {
      facturaVentaController.initFacturaVenta(frame, modal, this, 3, true);
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      //solo se hace cargo de persistir la entity Producto
      //todo las demás acciones son manejadas (delegadas) -> FacturaVentaJpaController
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               doRemito();
            } catch (MessageException ex) {
               facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
               ex.printStackTrace();
            }
         } else if (boton.getName().equalsIgnoreCase("filtrarReRe")) {
            try {
               armarQuery();
            } catch (MessageException ex) {
               buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
            } catch (Exception ex) {
               buscador.showMessage(ex.getMessage(), "Buscador - " + CLASS_NAME, 0);
               ex.printStackTrace();
            }
         } else {
            facturaVentaController.actionPerformed(e);
         }
      } else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
         // no hacer NADA
      }
   }

   public void keyTyped(KeyEvent e) {
      facturaVentaController.keyTyped(e);
   }

   public void keyReleased(KeyEvent e) {
      facturaVentaController.keyReleased(e);
   }

   @Deprecated
   public void keyPressed(KeyEvent e) {
   }

   private void doRemito() throws MessageException, Exception {
      if (MODO_VISTA) {
         doImprimir(selectedRemito);
      } else {
         //obtiene el la Vista que contiene todos los datos
         contenedor = facturaVentaController.getContenedor();
         Cliente selectedCliente;
         Sucursal selectedSucursal;


         // <editor-fold defaultstate="collapsed" desc="CONTROLES">
         try {
            selectedCliente = (Cliente) contenedor.getCbCliente().getSelectedItem();
         } catch (ClassCastException ex) {
               throw new MessageException("Cliente no válido");
         }
         try {
            selectedSucursal = (Sucursal) contenedor.getCbSucursal().getSelectedItem();
         } catch (ClassCastException ex) {
               throw new MessageException("Sucursal no válido");
         }

         if (contenedor.getDcFechaFactura() == null) {
            throw new MessageException("Fecha no válida");
         }

         javax.swing.table.DefaultTableModel dtm = contenedor.getDTM();
         if (dtm.getRowCount() < 1) {
            throw new MessageException(CLASS_NAME + " debe tener al menos un item.");
         }
         // </editor-fold>

         Remito p = new Remito();
         p.setId(Integer.valueOf(contenedor.getTfNumMovimiento()));
         p.setNumero(Long.valueOf(contenedor.getTfFacturaCuarto() + contenedor.getTfFacturaOcteto()));
         p.setCliente(selectedCliente);
         p.setSucursal(selectedSucursal);
         p.setFechaCreacion(contenedor.getDcFechaFactura());
         p.setHoraCreacion(new java.util.Date());
         p.setUsuario((Usuario) contenedor.getCbUsuario().getSelectedItem());
         p.setDetalleRemitoList(new ArrayList<DetalleRemito>(dtm.getRowCount()));
         // carga de detalleVenta
         DetalleRemito detalleRemito;
         for (int i = 0; i < dtm.getRowCount(); i++) {
            detalleRemito = new DetalleRemito();
            detalleRemito.setProducto(new ProductoJpaController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
            detalleRemito.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
            p.getDetalleRemitoList().add(detalleRemito);
         }
         try {
            p = create(p);
         } catch (PreexistingEntityException ex) {
            Logger.getLogger(PresupuestoJpaController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
            throw ex;
         }
         doImprimir(p);
         limpiarPanel();
      }
   }

   public Long getNextNumero() {
      EntityManager em = getEntityManager();
      Long nextRemitoNumero = 100000001L;
      try {
         nextRemitoNumero = 1 + (Long) em.createQuery("SELECT MAX(o.numero) FROM Remito o").getSingleResult();
      } catch (NullPointerException ex) {
         System.out.println("pintó el 1er Remito ");
      } finally {
         em.close();
      }
      return nextRemitoNumero;
   }

   private void doImprimir(Remito p) {
      try {
         Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Remito.jasper", "Remito");
         r.addParameter("REMITO_ID", p.getId());
         r.printReport();
      } catch (Exception ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         Logger.getLogger(PresupuestoJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   private void limpiarPanel() {
      facturaVentaController.borrarDetalles();
      facturaVentaController.getContenedor().setTfNumMovimiento(String.valueOf(getRemitoCount() + 1));
      facturaVentaController.setNextNumeroFactura(getNextNumero());

   }

   public void initBuscador(javax.swing.JDialog dialog, boolean modal, boolean setVisible) {
      buscador = new JDBuscadorReRe(dialog, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
      initBuscador(setVisible);
   }
   public void initBuscador(javax.swing.JFrame frame, boolean modal, boolean setVisible) {
      buscador = new JDBuscadorReRe(frame, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
      initBuscador(setVisible);
   }
   
   private void initBuscador(boolean setVisible) {
      //add a listener para capturar la fila seleccionada
      buscador.getjTable1().addMouseListener(new java.awt.event.MouseAdapter() {
         @Override
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            mouseClickedBuscadorRemito(evt);
         }
      });

      //personalizando vista de Buscador
      buscador.setParaRemitos();
      UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteJpaController().findClienteEntities(), true);
      UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
      try {
         UTIL.getDefaultTableModel(
                 buscador.getjTable1(),
                 new String[]{"Nº " + CLASS_NAME, "Nº Factura", "Cliente", "Fecha", "Sucursal", "Usuario"},
                 new int[]{15, 20, 50, 50, 80, 50}
         );
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      MODO_VISTA = true;
      buscador.setListeners(this);
//      if(setVisible)                         //
         buscador.setVisible(setVisible);
   }

   private void mouseClickedBuscadorRemito(MouseEvent evt) {
      if (evt.getClickCount() >= 2) {
         selectedRemito = (Remito) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0);
         if (toFacturar) {
            buscador.dispose();
         } else {
            setDatos(selectedRemito);
         }
      }
   }

   private void setDatos(Remito remito) {
      int REMITO_MODE = 3;
      try {
         facturaVentaController.initFacturaVenta(null, true, this, REMITO_MODE, false);
      } catch (MessageException ex) {
         // no va saltar nunca este
      }
      jdFacturaVenta = facturaVentaController.getContenedor();
      jdFacturaVenta.setTfNumMovimiento(remito.getId().toString());
      jdFacturaVenta.setLocationRelativeTo(buscador);
      String numFactura = UTIL.AGREGAR_CEROS(remito.getNumero(), 12);
      jdFacturaVenta.setTfFacturaCuarto(numFactura.substring(0, 4));
      jdFacturaVenta.setTfFacturaOcteto(numFactura.substring(4));
      jdFacturaVenta.getCbCliente().removeAllItems();
      jdFacturaVenta.getCbCliente().addItem(remito.getCliente());
      jdFacturaVenta.getCbSucursal().removeAllItems();
      jdFacturaVenta.getCbSucursal().addItem(remito.getSucursal());
      jdFacturaVenta.getCbUsuario().addItem(remito.getUsuario());
      jdFacturaVenta.setDcFechaFactura(remito.getFechaCreacion());

      //Los Remitos no tienen Nº de Movimiento
//      jdFacturaVenta.setTfNumMovimiento(String.valueOf(remito.getId()));
      //Ni ListaPrecio
//      jdFacturaVenta.getCbListaPrecio().addItem(remito.getListaPrecios());
      List<DetalleRemito> lista = remito.getDetalleRemitoList();
      javax.swing.table.DefaultTableModel dtm = jdFacturaVenta.getDTM();
      for (DetalleRemito detallesPresupuesto : lista) {
         dtm.addRow(new Object[]{
                    null,
                    detallesPresupuesto.getProducto().getCodigo(),
                    detallesPresupuesto.getProducto(),
                    detallesPresupuesto.getCantidad(),
                    0,
                    0,
                    0,
                    0
                 });
      }

      jdFacturaVenta.modoVista();
      jdFacturaVenta.setListener(this);
      jdFacturaVenta.setLocationByPlatform(true);
      jdFacturaVenta.setVisible(true);
   }

   private void armarQuery() throws MessageException {
      String query = "SELECT o.* FROM remito o"
              + " WHERE o.id > -1";

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

      if (buscador.getDcDesde() != null) {
         query += " AND o.fecha_creacion >= '" + buscador.getDcDesde() + "'";
      }
      if (buscador.getDcHasta() != null) {
         query += " AND o.fecha_creacion <= '" + buscador.getDcHasta() + "'";
      }
      if (buscador.getCbSucursal().getSelectedIndex() > 0) {
         query += " AND o.sucursal = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId();
      }

      if (buscador.getCbClieProv().getSelectedIndex() > 0) {
         query += " AND o.cliente = " + ((Cliente) buscador.getCbClieProv().getSelectedItem()).getId();
      }

      if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
         if (buscador.getCbFormasDePago().getSelectedIndex() == 1)
            query += " AND o.factura_venta IS NULL";
         else
            query += " AND o.factura_venta IS NOT NULL";
      }
      query += " ORDER BY o.id";
      System.out.println("QUERY: " + query);
      cargarDtmBuscador(query);
   }

   private void cargarDtmBuscador(String query) {
      buscador.dtmRemoveAll();
      javax.swing.table.DefaultTableModel dtm = buscador.getDtm();
      List<Remito> list = DAO.getEntityManager().createNativeQuery(query, Remito.class).getResultList();
      for (Remito remito : list) {
         dtm.addRow(new Object[]{
                    remito, // <--- no es visible
                    remito.getFacturaVenta(),
                    remito.getCliente(),
                    UTIL.DATE_FORMAT.format(remito.getFechaCreacion()),
                    remito.getSucursal(),
                    remito.getUsuario(),});
      }
   }

   /**
    * Para distingir cuando una selección de "Visualización de un Remito" y
    * una de "Para relaciónar un Remito a una Factura Venta".
    * @param toFacturar
    */
   public void setToFacturar() {
      this.toFacturar = true;
   }

   public Remito getSelectedRemito() {
      return selectedRemito;
   }

   void initBuscadorToFacturar(javax.swing.JDialog dialog, boolean modal, Cliente cliente) {
      buscador = new JDBuscadorReRe(dialog, "Buscador - " + CLASS_NAME, true, "Cliente", "Nº " + CLASS_NAME);
      initBuscador(false);
      setToFacturar();
      UTIL.setSelectedItem(buscador.getCbClieProv(), cliente);
      buscador.getCbFormasDePago().setSelectedIndex(1); // los NO facturados
      buscador.getCbFormasDePago().setEnabled(false);
      buscador.getCbClieProv().setEnabled(false);
      buscador.setVisible(true);
   }
}
