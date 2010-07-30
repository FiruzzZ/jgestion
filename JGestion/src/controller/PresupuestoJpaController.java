package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.Cliente;
import entity.Presupuesto;
import entity.Sucursal;
import entity.Usuario;
import gui.JFP;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.DetallePresupuesto;
import entity.ListaPrecios;
import entity.UTIL;
import gui.JDBuscadorReRe;
import gui.JDFacturaVenta;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrador
 */
public class PresupuestoJpaController implements ActionListener, KeyListener {

   public static final String CLASS_NAME = "Presupuesto";
   private JDFacturaVenta jdFacturaVenta;
   private final FacturaVentaJpaController facturaVentaController;
   private JDBuscadorReRe buscador;
   private boolean MODO_VISTA = false;
   private Presupuesto selectedPresupuesto;

   public PresupuestoJpaController() {
      facturaVentaController = new FacturaVentaJpaController();
   }

   // <editor-fold defaultstate="collapsed" desc="CRUD..">
   private Presupuesto create(Presupuesto presupuesto) throws PreexistingEntityException, Exception {
      if (presupuesto.getDetallePresupuestoList() == null) {
         presupuesto.setDetallePresupuestoList(new ArrayList<DetallePresupuesto>());
      }
      EntityManager em = null;
      try {
         em = DAO.getEntityManager();
         em.getTransaction().begin();
         List<DetallePresupuesto> toPers = presupuesto.getDetallePresupuestoList();
         presupuesto.setDetallePresupuestoList(new ArrayList<DetallePresupuesto>());
         em.persist(presupuesto);
         em.getTransaction().commit();
         for (DetallePresupuesto detallePresupuestoListDetallePresupuesto : toPers) {
            detallePresupuestoListDetallePresupuesto.setPresupuesto(presupuesto);
            new DetallePresupuestoJpaController().create(detallePresupuestoListDetallePresupuesto);
         }
      } catch (Exception ex) {
         if(em.getTransaction().isActive())
            em.getTransaction().rollback();

         if(findPresupuesto(presupuesto.getId()) != null) {
            throw new PreexistingEntityException("Presupuesto " + presupuesto + " already exists.", ex);
         }
         throw ex;
      } finally { if (em != null) { em.close();  }    }

      return presupuesto;
   }

   private void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
      EntityManager em = null;
      try {
         em = DAO.getEntityManager();
         em.getTransaction().begin();
         Presupuesto presupuesto;
         try {
            presupuesto = em.getReference(Presupuesto.class, id);
            presupuesto.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The presupuesto with id " + id + " no longer exists.", enfe);
         }
         List<String> illegalOrphanMessages = null;
         List<DetallePresupuesto> detallePresupuestoListOrphanCheck = presupuesto.getDetallePresupuestoList();
         for (DetallePresupuesto detallePresupuestoListOrphanCheckDetallePresupuesto : detallePresupuestoListOrphanCheck) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("This Presupuesto (" + presupuesto + ") cannot be destroyed since the DetallePresupuesto " + detallePresupuestoListOrphanCheckDetallePresupuesto + " in its detallePresupuestoList field has a non-nullable presupuesto field.");
         }
         if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
         }
         em.remove(presupuesto);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Presupuesto> findPresupuestoEntities() {
      return findPresupuestoEntities(true, -1, -1);
   }

   public List<Presupuesto> findPresupuestoEntities(int maxResults, int firstResult) {
      return findPresupuestoEntities(false, maxResults, firstResult);
   }

   private List<Presupuesto> findPresupuestoEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = DAO.getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from Presupuesto as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Presupuesto findPresupuesto(Integer id) {
      EntityManager em = DAO.getEntityManager();
      try {
         return em.find(Presupuesto.class, id);
      } finally {
         em.close();
      }
   }

   public int getPresupuestoCount() {
      EntityManager em = DAO.getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from Presupuesto as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   public void initPresupuesto(javax.swing.JFrame frame, boolean modal, boolean setVisible) throws MessageException {
      facturaVentaController.initFacturaVenta(frame, modal, this, 2, setVisible);
   }

   public void actionPerformed(ActionEvent e) {
      //solo se hace cargo de persistir la entity Presupuesto
      //todo las demás acciones son manejadas (delegadas) -> FacturaVentaJpaController
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               doPresupuesto();
            } catch(MessageException ex) {
               facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch(Exception ex) {
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
            if( ! MODO_VISTA)
            facturaVentaController.actionPerformed(e);
         }
      } else {
         if( ! MODO_VISTA)
            facturaVentaController.actionPerformed(e);
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

   private void doPresupuesto() throws MessageException, Exception {
      Presupuesto newPresupuesto = selectedPresupuesto;
      if ( !MODO_VISTA) {
         jdFacturaVenta = facturaVentaController.getContenedor();

         // <editor-fold defaultstate="collapsed" desc="CONTROLES">

         if (jdFacturaVenta.getDcFechaFactura() == null) {
            throw new MessageException("Fecha de factura no válida");
         }

         javax.swing.table.DefaultTableModel dtm = jdFacturaVenta.getDTM();
         if (dtm.getRowCount() < 1) {
            throw new MessageException(CLASS_NAME + " debe tener al menos un item.");
         }

         if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CTA_CTE)) {
            try {
               if (Short.valueOf(jdFacturaVenta.getTfDias()) < 1) {
                  throw new MessageException("Cantidad de días de Cta. Cte. no válida. Debe ser mayor a 0");
               }
            } catch (NumberFormatException ex) {
               throw new MessageException("Cantidad de días de Cta. Cte. no válida");
            }
         }
         // </editor-fold>

         newPresupuesto = new Presupuesto();
         newPresupuesto.setCliente((Cliente) jdFacturaVenta.getCbCliente().getSelectedItem());
         if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CONTADO)) {
            newPresupuesto.setFormaPago((short) Valores.FormaPago.CONTADO.getId());
         } else if (((Valores.FormaPago) jdFacturaVenta.getCbFormaPago().getSelectedItem()).equals(Valores.FormaPago.CTA_CTE)) {
            newPresupuesto.setFormaPago((short) Valores.FormaPago.CTA_CTE.getId());
            newPresupuesto.setDias(Short.parseShort(jdFacturaVenta.getTfDias()));
         }
         newPresupuesto.setDescuento(Double.valueOf(jdFacturaVenta.getTfTotalDesc()));
         newPresupuesto.setFechaCreacion(null);
         newPresupuesto.setHoraCreacion(null);
         newPresupuesto.setImporte(Double.valueOf(jdFacturaVenta.getTfTotal()));
         newPresupuesto.setIva10(Double.valueOf(jdFacturaVenta.getTfTotalIVA105()));
         newPresupuesto.setIva21(Double.valueOf(jdFacturaVenta.getTfTotalIVA21()));
         newPresupuesto.setListaPrecios((ListaPrecios) jdFacturaVenta.getCbListaPrecio().getSelectedItem());
         newPresupuesto.setSucursal((Sucursal) jdFacturaVenta.getCbSucursal().getSelectedItem());
         newPresupuesto.setUsuario((Usuario) jdFacturaVenta.getCbUsuario().getSelectedItem());
         newPresupuesto.setDetallePresupuestoList(new ArrayList<DetallePresupuesto>(dtm.getRowCount()));
         // carga de detalleVenta
         DetallePresupuesto detallePresupuesto;
         for (int i = 0; i < dtm.getRowCount(); i++) {
            detallePresupuesto = new DetallePresupuesto();
            detallePresupuesto.setProducto(new ProductoJpaController().findProductoByCodigo(dtm.getValueAt(i, 1).toString()));
            detallePresupuesto.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
            detallePresupuesto.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
            detallePresupuesto.setDescuento(Double.valueOf(dtm.getValueAt(i, 6).toString()));
            detallePresupuesto.setTipoDesc(Integer.valueOf(dtm.getValueAt(i, 8).toString()));
            newPresupuesto.getDetallePresupuestoList().add(detallePresupuesto);
         }
         try {
            newPresupuesto = create(newPresupuesto);
         } catch (PreexistingEntityException ex) {
            Logger.getLogger(PresupuestoJpaController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
            throw ex;
         }
      }
      doReport(newPresupuesto);
   }

   private void doReport(Presupuesto p) {
      try {
         Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Presupuesto.jasper", "Presupuesto");
         r.addParameter("PRESUPUESTO_ID", p.getId());
         r.printReport();
      } catch (Exception ex) {
         Logger.getLogger(PresupuestoJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   private void buscadorPresupuestoMouseClicked(MouseEvent evt) {
      if (evt.getClickCount() >= 2) {
         selectedPresupuesto = (Presupuesto) buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0);
         setDatos(selectedPresupuesto);
      }
   }
   public void initBuscador(javax.swing.JFrame frame) {
      buscador = new JDBuscadorReRe(frame, "Buscador - " + CLASS_NAME, true, "Cliente", "Nº " + CLASS_NAME);
      buscador.getjTable1().addMouseListener(new java.awt.event.MouseAdapter() {
         @Override
         public void mouseClicked(java.awt.event.MouseEvent evt) {
            buscadorPresupuestoMouseClicked(evt);
         }
      });
      //personalizando vista de Buscador
      buscador.hideFactura();
      buscador.hideCaja();
      buscador.hideFormaPago();
      buscador.getjTfOcteto().setVisible(false);
      UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteJpaController().findClienteEntities(), true);
      UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
      try {
         UTIL.getDefaultTableModel(
                 new String[]{"Nº " + CLASS_NAME, "Cliente", "Importe", "Fecha", "Sucursal", "Usuario"},
                 new int[]{      15             , 50        , 50     , 50      , 80       , 50     },
                 buscador.getjTable1());
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      MODO_VISTA = true;
      buscador.setListeners(this);
      buscador.setVisible(true);
   }

   private void armarQuery() throws MessageException {
      String query = "SELECT o.* FROM presupuesto o"
              + " WHERE o.id > -1";

      long presupuestoID;
      //filtro por nº de ReRe
      if (buscador.getTfCuarto().length() > 0) {
         try {
            presupuestoID = Long.parseLong(buscador.getTfCuarto());
            query += " AND o.id = " + presupuestoID;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido.\n\n" + ex.getMessage());
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
      query += " ORDER BY o.id";
      System.out.println("QUERY: " + query);
      cargarDtmBuscador(query);
   }

   private void cargarDtmBuscador(String query) {
      buscador.dtmRemoveAll();
      javax.swing.table.DefaultTableModel dtm = buscador.getDtm();
      List<Presupuesto> l = DAO.getEntityManager().createNativeQuery(query, Presupuesto.class).getResultList();
      for (Presupuesto presupuesto : l) {
         dtm.addRow(new Object[]{
                    presupuesto, // <--- no es visible
                    presupuesto.getCliente(),
                    presupuesto.getImporte(),
                    UTIL.DATE_FORMAT.format(presupuesto.getFechaCreacion()),
                    presupuesto.getSucursal(),
                    presupuesto.getUsuario(),
                 });
      }
   }

   private void setDatos(Presupuesto presupuesto) {
      try {
         facturaVentaController.initFacturaVenta(null, true, this, 2, false);
      } catch (MessageException ex) {
         Logger.getLogger(PresupuestoJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      jdFacturaVenta = facturaVentaController.getContenedor();
      jdFacturaVenta.setLocationRelativeTo(buscador);
      jdFacturaVenta.getCbCliente().addItem(presupuesto.getCliente());
      jdFacturaVenta.getCbSucursal().addItem(presupuesto.getSucursal());
      jdFacturaVenta.getCbListaPrecio().addItem(presupuesto.getListaPrecios());  //<---
      jdFacturaVenta.getCbUsuario().addItem(presupuesto.getUsuario());           //<---
      jdFacturaVenta.setDcFechaFactura(presupuesto.getFechaCreacion());
      for (Valores.FormaPago formaPago : Valores.FormaPago.getFormasDePago()) {
         if(formaPago.getId() == (presupuesto.getFormaPago())) {
            jdFacturaVenta.getCbFormaPago().addItem(formaPago);
            if(presupuesto.getDias() != null && presupuesto.getDias() > 0)
               jdFacturaVenta.setTfDias(presupuesto.getDias().toString());
            else
               jdFacturaVenta.setTfDias("");
         }
      }
      jdFacturaVenta.setTfNumMovimiento(String.valueOf(presupuesto.getId()));
      List<DetallePresupuesto> lista = presupuesto.getDetallePresupuestoList();
      javax.swing.table.DefaultTableModel dtm = jdFacturaVenta.getDTM();
      for (DetallePresupuesto detallesPresupuesto : lista) {
         double productoConIVA = detallesPresupuesto.getPrecioUnitario() + UTIL.getPorcentaje(detallesPresupuesto.getPrecioUnitario(), detallesPresupuesto.getProducto().getIva().getIva());
//         "IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
         dtm.addRow(new Object[]{
                    null,
                    detallesPresupuesto.getProducto().getCodigo(),
                    detallesPresupuesto.getProducto(),
                    detallesPresupuesto.getCantidad(),
                    detallesPresupuesto.getPrecioUnitario(),
                    productoConIVA,
                    detallesPresupuesto.getDescuento(),
                    ((detallesPresupuesto.getCantidad() * productoConIVA) - detallesPresupuesto.getDescuento())
                 });
      }
      //totales
      jdFacturaVenta.setTfGravado(
              UTIL.PRECIO_CON_PUNTO.format(presupuesto.getImporte() - (presupuesto.getIva10() + presupuesto.getIva21())));
      jdFacturaVenta.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(presupuesto.getIva10()));
      jdFacturaVenta.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(presupuesto.getIva21()));
      jdFacturaVenta.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(presupuesto.getImporte()));
      jdFacturaVenta.modoVista(); //   <----------------------
      jdFacturaVenta.setLocationByPlatform(true);
      jdFacturaVenta.setVisible(true);
   }

}

