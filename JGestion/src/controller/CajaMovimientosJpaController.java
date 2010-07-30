package controller;

import controller.exceptions.*;
import entity.CajaMovimientos;
import entity.FacturaCompra;
import entity.FacturaVenta;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Caja;
import entity.DetalleCajaMovimientos;
import entity.Recibo;
import entity.Remesa;
import entity.UTIL;
import gui.JDABM;
import gui.JDBuscador;
import gui.JDCajaToCaja;
import gui.JDCierreCaja;
import gui.PanelBuscadorCajaToCaja;
import gui.PanelBuscadorCajasCerradas;
import gui.PanelBuscadorMovimientosVarios;
import gui.PanelMovimientosVarios;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.swing.table.DefaultTableModel;

/**
 * Encargada de registrar todos los asientos (ingresos/egresos) de las cajas..
 * @author FiruzzZ
 */
public class CajaMovimientosJpaController implements ActionListener {
//   private final String[]  colsName = {"Nº","Código","Nombre","Marca","Stock Gral."};
//   private final int[] colsWidth = {15    ,50     ,100      ,50     ,20};

   private JDCierreCaja jdCierreCaja;
   private JDCajaToCaja JDcajaToCaja;
   private JDABM abm;
   private PanelMovimientosVarios panelMovVarios;
   private JDBuscador buscador;
   private PanelBuscadorMovimientosVarios panelBuscadorMovimientosVarios;
   private PanelBuscadorCajasCerradas panelBuscadorCajasCerradas;
   private CajaMovimientos selectedCajaMovimientos;
   private PanelBuscadorCajaToCaja panelBuscadorCajaToCaja;

//   public CajaMovimientosJpaController() {
//      emf = Persistence.createEntityManagerFactory("JGestionPU");
//   }
//   private EntityManagerFactory emf = null;
   // <editor-fold defaultstate="collapsed" desc="CRUD..">
   public EntityManager getEntityManager() {
//      return emf.createEntityManager();
      return DAO.getEntityManager();
   }

   public void create(CajaMovimientos cajaMovimientos) {
      if (cajaMovimientos.getDetalleCajaMovimientosList() == null) {
         cajaMovimientos.setDetalleCajaMovimientosList(new ArrayList<DetalleCajaMovimientos>());
      }
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Caja caja = cajaMovimientos.getCaja();
         if (caja != null) {
            caja = em.getReference(caja.getClass(), caja.getId());
            cajaMovimientos.setCaja(caja);
         }
         List<DetalleCajaMovimientos> attachedDetalleCajaMovimientosList = new ArrayList<DetalleCajaMovimientos>();
         for (DetalleCajaMovimientos detalleCajaMovimientosListDetalleCajaMovimientosToAttach : cajaMovimientos.getDetalleCajaMovimientosList()) {
            detalleCajaMovimientosListDetalleCajaMovimientosToAttach = em.merge(detalleCajaMovimientosListDetalleCajaMovimientosToAttach);//.getClass(), detalleCajaMovimientosListDetalleCajaMovimientosToAttach.getId());
            attachedDetalleCajaMovimientosList.add(detalleCajaMovimientosListDetalleCajaMovimientosToAttach);
         }
         cajaMovimientos.setDetalleCajaMovimientosList(attachedDetalleCajaMovimientosList);
         em.persist(cajaMovimientos);
         if (caja != null) {
            caja.getCajaMovimientosList().add(cajaMovimientos);
            caja = em.merge(caja);
         }
         for (DetalleCajaMovimientos detalleCajaMovimientosListDetalleCajaMovimientos : cajaMovimientos.getDetalleCajaMovimientosList()) {
            CajaMovimientos oldCajaMovimientosOfDetalleCajaMovimientosListDetalleCajaMovimientos = detalleCajaMovimientosListDetalleCajaMovimientos.getCajaMovimientos();
            detalleCajaMovimientosListDetalleCajaMovimientos.setCajaMovimientos(cajaMovimientos);
            detalleCajaMovimientosListDetalleCajaMovimientos = em.merge(detalleCajaMovimientosListDetalleCajaMovimientos);
            if (oldCajaMovimientosOfDetalleCajaMovimientosListDetalleCajaMovimientos != null) {
               oldCajaMovimientosOfDetalleCajaMovimientosListDetalleCajaMovimientos.getDetalleCajaMovimientosList().remove(detalleCajaMovimientosListDetalleCajaMovimientos);
               oldCajaMovimientosOfDetalleCajaMovimientosListDetalleCajaMovimientos = em.merge(oldCajaMovimientosOfDetalleCajaMovimientosListDetalleCajaMovimientos);
            }
         }
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(CajaMovimientos cajaMovimientos) throws IllegalOrphanException, NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
//         CajaMovimientos persistentCajaMovimientos = em.find(CajaMovimientos.class, cajaMovimientos.getId());
//         Caja cajaOld = persistentCajaMovimientos.getCaja();
//         Caja cajaNew = cajaMovimientos.getCaja();
/////////////no es necesiario comprar la lista de detalleCajaMovimientos............
//         List<DetalleCajaMovimientos> detalleCajaMovimientosListOld = persistentCajaMovimientos.getDetalleCajaMovimientosList();
//         List<DetalleCajaMovimientos> detalleCajaMovimientosListNew = cajaMovimientos.getDetalleCajaMovimientosList();
//         List<String> illegalOrphanMessages = null;
//         for (DetalleCajaMovimientos detalleCajaMovimientosListOldDetalleCajaMovimientos : detalleCajaMovimientosListOld) {
//            if (!detalleCajaMovimientosListNew.contains(detalleCajaMovimientosListOldDetalleCajaMovimientos)) {
//               if (illegalOrphanMessages == null) {
//                  illegalOrphanMessages = new ArrayList<String>();
//               }
//               illegalOrphanMessages.add("You must retain DetalleCajaMovimientos " + detalleCajaMovimientosListOldDetalleCajaMovimientos + " since its cajaMovimientos field is not nullable.");
//            }
//         }
//         if (illegalOrphanMessages != null) {
//            throw new IllegalOrphanException(illegalOrphanMessages);
//         }
//         if (cajaNew != null) {
//            cajaNew = em.getReference(cajaNew.getClass(), cajaNew.getId());
//            cajaMovimientos.setCaja(cajaNew);
//         }
//         List<DetalleCajaMovimientos> attachedDetalleCajaMovimientosListNew = new ArrayList<DetalleCajaMovimientos>();
//         for (DetalleCajaMovimientos detalleCajaMovimientosListNewDetalleCajaMovimientosToAttach : detalleCajaMovimientosListNew) {
//
//            detalleCajaMovimientosListNewDetalleCajaMovimientosToAttach = em.getReference(detalleCajaMovimientosListNewDetalleCajaMovimientosToAttach.getClass(), detalleCajaMovimientosListNewDetalleCajaMovimientosToAttach.getId());
//            attachedDetalleCajaMovimientosListNew.add(detalleCajaMovimientosListNewDetalleCajaMovimientosToAttach);
//         }
//         detalleCajaMovimientosListNew = attachedDetalleCajaMovimientosListNew;
//         cajaMovimientos.setDetalleCajaMovimientosList(detalleCajaMovimientosListNew);
         cajaMovimientos = em.merge(cajaMovimientos);
//         if (cajaOld != null && !cajaOld.equals(cajaNew)) {
//            cajaOld.getCajaMovimientosList().remove(cajaMovimientos);
//            cajaOld = em.merge(cajaOld);
//         }
//         if (cajaNew != null && !cajaNew.equals(cajaOld)) {
//            cajaNew.getCajaMovimientosList().add(cajaMovimientos);
//            cajaNew = em.merge(cajaNew);
//         }
//         for (DetalleCajaMovimientos detalleCajaMovimientosListNewDetalleCajaMovimientos : detalleCajaMovimientosListNew) {
//            if (!detalleCajaMovimientosListOld.contains(detalleCajaMovimientosListNewDetalleCajaMovimientos)) {
//               CajaMovimientos oldCajaMovimientosOfDetalleCajaMovimientosListNewDetalleCajaMovimientos = detalleCajaMovimientosListNewDetalleCajaMovimientos.getCajaMovimientos();
//               detalleCajaMovimientosListNewDetalleCajaMovimientos.setCajaMovimientos(cajaMovimientos);
//               detalleCajaMovimientosListNewDetalleCajaMovimientos = em.merge(detalleCajaMovimientosListNewDetalleCajaMovimientos);
//               if (oldCajaMovimientosOfDetalleCajaMovimientosListNewDetalleCajaMovimientos != null && !oldCajaMovimientosOfDetalleCajaMovimientosListNewDetalleCajaMovimientos.equals(cajaMovimientos)) {
//                  oldCajaMovimientosOfDetalleCajaMovimientosListNewDetalleCajaMovimientos.getDetalleCajaMovimientosList().remove(detalleCajaMovimientosListNewDetalleCajaMovimientos);
//                  oldCajaMovimientosOfDetalleCajaMovimientosListNewDetalleCajaMovimientos = em.merge(oldCajaMovimientosOfDetalleCajaMovimientosListNewDetalleCajaMovimientos);
//               }
//            }
//         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = cajaMovimientos.getId();
            if (findCajaMovimientos(id) == null) {
               throw new NonexistentEntityException("The cajaMovimientos with id " + id + " no longer exists.");
            }
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void merge(CajaMovimientos cajaMovimientos) throws Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.merge(cajaMovimientos);
         em.getTransaction().commit();
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

   public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         CajaMovimientos cajaMovimientos;
         try {
            cajaMovimientos = em.getReference(CajaMovimientos.class, id);
            cajaMovimientos.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The cajaMovimientos with id " + id + " no longer exists.", enfe);
         }
         List<String> illegalOrphanMessages = null;
         List<DetalleCajaMovimientos> detalleCajaMovimientosListOrphanCheck = cajaMovimientos.getDetalleCajaMovimientosList();
         for (DetalleCajaMovimientos detalleCajaMovimientosListOrphanCheckDetalleCajaMovimientos : detalleCajaMovimientosListOrphanCheck) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("This CajaMovimientos (" + cajaMovimientos + ") cannot be destroyed since the DetalleCajaMovimientos " + detalleCajaMovimientosListOrphanCheckDetalleCajaMovimientos + " in its detalleCajaMovimientosList field has a non-nullable cajaMovimientos field.");
         }
         if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
         }
         Caja caja = cajaMovimientos.getCaja();
         if (caja != null) {
            caja.getCajaMovimientosList().remove(cajaMovimientos);
            caja = em.merge(caja);
         }
         em.remove(cajaMovimientos);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<CajaMovimientos> findCajaMovimientosEntities() {
      return findCajaMovimientosEntities(true, -1, -1);
   }

   public List<CajaMovimientos> findCajaMovimientosEntities(int maxResults, int firstResult) {
      return findCajaMovimientosEntities(false, maxResults, firstResult);
   }

   private List<CajaMovimientos> findCajaMovimientosEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from CajaMovimientos as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public CajaMovimientos findCajaMovimientos(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(CajaMovimientos.class, id);
      } finally {
         em.close();
      }
   }

   public int getCajaMovimientosCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from CajaMovimientos as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   void asentarMovimiento(FacturaCompra facturaCompra) throws Exception {
      System.out.println("asentarMovimiento (FacturaCompra): " + facturaCompra.getId() + " " + facturaCompra.getNumero());
      //caja_MOVIMIENTO ABIERTA en la q se va asentar
      CajaMovimientos cm = findCajaMovimientoAbierta(facturaCompra.getCaja());
      EntityManager em = getEntityManager();
      try {
         em.getTransaction().begin();
         CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
         DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
         newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
         newDetalleCajaMovimiento.setIngreso(false);
         //el importe de las FacturaCompra son siempre negativos.. (egresos de $$)
         newDetalleCajaMovimiento.setMonto(-facturaCompra.getImporte());
         newDetalleCajaMovimiento.setNumero(facturaCompra.getId());
         newDetalleCajaMovimiento.setFecha(new Date());
         newDetalleCajaMovimiento.setHora(new Date());
         newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.FACTU_COMPRA);
         newDetalleCajaMovimiento.setDescripcion("F" + facturaCompra.getTipo() + facturaCompra.getNumero());
         newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
         new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
      } catch (Exception e) {
         em.getTransaction().rollback();
         throw e;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   void asentarMovimiento(FacturaVenta facturaVenta) throws Exception {
      System.out.println("asentarMovimiento (FacturaVenta)");
      //caja en la q se va asentar
      CajaMovimientos cm = findCajaMovimientoAbierta(facturaVenta.getCaja());
      EntityManager em = getEntityManager();
      try {
         em.getTransaction().begin();
         CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
         DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
         newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
         newDetalleCajaMovimiento.setIngreso(true);
         newDetalleCajaMovimiento.setMonto(facturaVenta.getImporte());
         newDetalleCajaMovimiento.setNumero(facturaVenta.getId());
         newDetalleCajaMovimiento.setFecha(new Date());
         newDetalleCajaMovimiento.setHora(new Date());
         newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.FACTU_VENTA);
         newDetalleCajaMovimiento.setDescripcion(getDescripcion(facturaVenta));
         newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
         new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
      } catch (Exception e) {
         em.getTransaction().rollback();
         throw e;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   void asentarMovimiento(Recibo recibo) throws Exception {
      System.out.println("asentarMovimiento (Recibo)");
      //caja en la q se va asentar
      CajaMovimientos cm = findCajaMovimientoAbierta(recibo.getCaja());
      EntityManager em = getEntityManager();
      try {
         em.getTransaction().begin();
         CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
         DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
         newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
         newDetalleCajaMovimiento.setIngreso(true);
         newDetalleCajaMovimiento.setMonto(recibo.getMonto());
         newDetalleCajaMovimiento.setNumero(recibo.getId());
         newDetalleCajaMovimiento.setFecha(new Date());
         newDetalleCajaMovimiento.setHora(new Date());
         newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.RECIBO);
         newDetalleCajaMovimiento.setDescripcion("R" + UTIL.AGREGAR_CEROS(recibo.getId(), 12));
         newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
         new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
      } catch (Exception e) {
         em.getTransaction().rollback();
         throw e;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   void asentarMovimiento(Remesa remesa) throws Exception {
      System.out.println("asentarMovimiento (Remesa)");
      //caja en la q se va asentar
      CajaMovimientos cm = findCajaMovimientoAbierta(remesa.getCaja());
      EntityManager em = getEntityManager();
      try {
         em.getTransaction().begin();
         CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
         DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
         newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
         newDetalleCajaMovimiento.setIngreso(false);
         newDetalleCajaMovimiento.setMonto(-remesa.getMontoEntrega());
         newDetalleCajaMovimiento.setNumero(remesa.getId());
         newDetalleCajaMovimiento.setFecha(new Date());
         newDetalleCajaMovimiento.setHora(new Date());
         newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.REMESA);
         newDetalleCajaMovimiento.setDescripcion("R" + UTIL.AGREGAR_CEROS(remesa.getId(), 12));
         newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
         new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
      } catch (Exception e) {
         em.getTransaction().rollback();
         throw e;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   void anular(Recibo recibo) throws MessageException, Exception {
      if (recibo.getEstado()) {
         throw new MessageException("No se puede anular el recibo Nº" + recibo.getId() + " porque ESTADO =" + recibo.getEstado());
      }

      System.out.println("anular " + recibo.getClass());
      //caja en la q se va asentar
      CajaMovimientos cm = findCajaMovimientoAbierta(recibo.getCaja());
      EntityManager em = getEntityManager();
      try {
         em.getTransaction().begin();
         CajaMovimientos cajaMovimientoActual = em.find(CajaMovimientos.class, cm.getId());
         DetalleCajaMovimientos newDetalleCajaMovimiento = new DetalleCajaMovimientos();
         newDetalleCajaMovimiento.setCajaMovimientos(cajaMovimientoActual);
         newDetalleCajaMovimiento.setIngreso(false);
         newDetalleCajaMovimiento.setMonto(-recibo.getMonto());
         newDetalleCajaMovimiento.setNumero(recibo.getId());
         newDetalleCajaMovimiento.setFecha(new Date());
         newDetalleCajaMovimiento.setHora(new Date());
         newDetalleCajaMovimiento.setTipo(DetalleCajaMovimientosJpaController.RECIBO);
         newDetalleCajaMovimiento.setDescripcion("R" + UTIL.AGREGAR_CEROS(recibo.getId(), 12) + " ANULADO");
         newDetalleCajaMovimiento.setUsuario(UsuarioJpaController.getCurrentUser());
         new DetalleCajaMovimientosJpaController().create(newDetalleCajaMovimiento);
      } catch (Exception e) {
         em.getTransaction().rollback();
         throw e;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   /**
    * Arma la descripción del detalleCajaMovimiento.
    * Si se imprime factura ej: F + [letra de factura] + [número de factura]
    * Si es interno ej: I + [número de movimento interno]
    * @param factura
    * @return Un String con la descripción.
    */
   private String getDescripcion(FacturaVenta facturaVenta) {
      String codigo_descrip = "";
      if (facturaVenta.getNumero() == 0) {
         codigo_descrip = "I" + facturaVenta.getMovimientoInterno();
      } else {
         codigo_descrip = "F" + facturaVenta.getTipo()
                 + UTIL.AGREGAR_CEROS(String.valueOf(facturaVenta.getNumero()), 12);
      }
      return codigo_descrip;
   }

   /**
    * Crea (abre) una <code>CajaMovimientos</code> implicitamente con la creación
    * de una <code>Caja</code>, así como el 1er
    * <code>DetalleCajaMovimientos</code> con monto inicial $0.
    * @param caja a la cual se van a vincular la <code>CajaMovimientos</code> y
    * <code>DetalleCajaMovimientos</code>.
    */
   void nueva(Caja caja) {
      CajaMovimientos cm = new CajaMovimientos();
      cm.setCaja(caja);
      cm.setFechaApertura(new Date());
      cm.setMontoApertura(0.0);
      cm.setSistemaFechaApertura(new Date());
      cm.setDetalleCajaMovimientosList(new ArrayList<DetalleCajaMovimientos>());
      //creando el 1er movimiento de la caja (apertura en $0)
      DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
      dcm.setDescripcion("Apertura de caja (creación)");
      dcm.setFecha(new Date());
      dcm.setHora(new Date());
      dcm.setIngreso(true);
      dcm.setMonto(0);
      dcm.setNumero(-1); //meaningless yet...
      dcm.setTipo(DetalleCajaMovimientosJpaController.APERTURA_CAJA);
      dcm.setUsuario(UsuarioJpaController.getCurrentUser());
      cm.getDetalleCajaMovimientosList().add(dcm);
      create(cm);
   }

   /**
    * Crea (abre) LA SIGUIENTE <code>CajaMovimientos</code> implicita POST cierre de la actual.
    * El montoApertura de la nueva <code>CajaMovimientos</code> es == al montoCierre de la anterior.
    * La fechaApertura de la nueva es == a un día después de la anterior.
    * @param cajaMovimiento la que precede a la que se va abrir.
    */
   private void abrirNextCajaMovimiento(CajaMovimientos cajaMovimiento) {
      CajaMovimientos cm = new CajaMovimientos();
      cm.setCaja(cajaMovimiento.getCaja());
      cm.setFechaApertura(UTIL.customDateByDays(cajaMovimiento.getFechaCierre(), +1));
      cm.setMontoApertura(cajaMovimiento.getMontoCierre());
      cm.setSistemaFechaApertura(new Date());
      cm.setDetalleCajaMovimientosList(new ArrayList<DetalleCajaMovimientos>());

      //creando el 1er detalleCajaMovimiento..
      DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
      dcm.setDescripcion("Apertura de caja");
      dcm.setFecha(new Date());
      dcm.setHora(new Date());
      dcm.setIngreso(true);
      dcm.setMonto(cm.getMontoApertura());
      dcm.setNumero(-1); //meaningless yet...
      dcm.setTipo(DetalleCajaMovimientosJpaController.APERTURA_CAJA);
      dcm.setUsuario(UsuarioJpaController.getCurrentUser());
      cm.getDetalleCajaMovimientosList().add(dcm);
      create(cm);
   }

   public CajaMovimientos findCajaMovimientoAbierta(Caja o) {
      EntityManager em = getEntityManager();
      CajaMovimientos cm = null;
      try {
         //busca la cajaMovimiento cuya fechaCierre != NULL (debería haber solo UNA)
         cm = (CajaMovimientos) em.createNativeQuery("SELECT * FROM caja_movimientos o"
                 + " where o.fecha_cierre is null AND o.caja =" + o.getId(), CajaMovimientos.class).getSingleResult();
      } catch (NoResultException ex) {
         System.out.println("NoResult -> findUltimaAbierta -> Caja " + o);
         ex.printStackTrace();
      } catch (NonUniqueResultException ex) {
         System.out.println("HAY MAS DE 1 ABIERTA!!! -> CAJA: " + o);
         ex.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (em != null) {
            em.close();
         }
      }
      return cm;
   }

   public void initCierreCaja(java.awt.Frame aThis, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.CERRAR_CAJAS);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      jdCierreCaja = new JDCierreCaja(aThis, modal);
      try {
         UTIL.getDefaultTableModel(
                 new String[]{"Descripción", "Monto", "Fecha (Hora)", "Usuario"},
                 new int[]{180, 20, 60, 40},
                 jdCierreCaja.getjTable1());
      } catch (Exception ex) {
         Logger.getLogger(CajaMovimientosJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      setCajaMovimientosPermitidas(jdCierreCaja.getCbCaja());
      jdCierreCaja.setListener(this);
      jdCierreCaja.setVisible(true);
   }

   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         // <editor-fold defaultstate="collapsed" desc="Panel Cierre de caja">
         if (jdCierreCaja != null) {
            if (boton.getName().equalsIgnoreCase("cerrar")) {
               if (jdCierreCaja.getCbCaja().getSelectedIndex() > 0) {
                  try {
                     cerrarCajaMovimiento((CajaMovimientos) jdCierreCaja.getCbCaja().getSelectedItem());
                  } catch (MessageException ex) {
                     jdCierreCaja.showMessage(ex.getMessage(), "Error", 2);
                  } catch (Exception ex) {
                     jdCierreCaja.showMessage(ex.getMessage(), "Error.Exception", 0);
                  }

               } else {
                  jdCierreCaja.showMessage("No hay Caja seleccionada", "Error", 2);
               }
            } else if (boton.getName().equalsIgnoreCase("buscar")) {
               initBuscadorCierreCaja(jdCierreCaja);
            } else if (boton.getName().equalsIgnoreCase("imprimir")) {
               try {
                  if (selectedCajaMovimientos != null) {
                     imprimirCierreCaja(selectedCajaMovimientos);
                  } else {
                     imprimirCierreCaja((CajaMovimientos) jdCierreCaja.getCbCaja().getSelectedItem());
                  }
               } catch (ClassCastException ex) {
                  jdCierreCaja.showMessage("No hay ninguna Caja seleccionada", null, 2);
               } catch (Exception ex) {
                  jdCierreCaja.showMessage(ex.getMessage(), "Error - Reporte", 0);
                  Logger.getLogger(CajaMovimientosJpaController.class.getName()).log(Level.SEVERE, null, ex);
               }

            } else if (boton.getName().equalsIgnoreCase("buscarBuscador")) {
               armarQueryCierreCajas();
            }
         }// </editor-fold>
   
         // <editor-fold defaultstate="collapsed" desc="Panel y Buscador CajaToCaja">
         else if (JDcajaToCaja != null) {
            if (boton.getName().equalsIgnoreCase("aceptar")) {
               try {
                  asentarMovimientoCajaToCaja();
               } catch (MessageException ex) {
                  JDcajaToCaja.showMessage(ex.getMessage(), "Error", 2);
               } catch (Exception ex) {
                  JDcajaToCaja.showMessage(ex.getMessage(), "Error", 0);
                  ex.printStackTrace();
               }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
               // se maneja en la vista (Panel)
            } else if (boton.getName().equalsIgnoreCase("buscar")) {
               initBuscadorCajaToCaja(JDcajaToCaja);
            } else if (boton.getName().equalsIgnoreCase("buscarBuscador")) {
               try {
                  armarQueryMovimientosCajaToCaja(false);
               } catch (Exception ex) {
                  ex.printStackTrace();
               }
            } else if (boton.getName().equalsIgnoreCase("imprimirBuscador")) {
               try {
                  armarQueryMovimientosCajaToCaja(true);
               } catch (Exception ex) {
                  abm.showMessage(ex.getMessage(), "CajaMovimientos -> Buscador Movimientos entre Cajas", 0);
               }
            }
         }// </editor-fold>

         // <editor-fold defaultstate="collapsed" desc="Panel MovimientosVarios y Buscador">
         else if (abm != null && panelMovVarios != null) {
            if (boton.getName().equalsIgnoreCase("aceptar")) {
               try {
                  asentarMovimientoVarios();
                  abm.showMessage("Agregado..", "Movimientos Varios", 1);
                  cleanPanelMovimientosVarios();
               } catch (MessageException ex) {
                  abm.showMessage(ex.getMessage(), "Error", 2);
               } catch (Exception ex) {
                  abm.showMessage(ex.getMessage(), "Error", 0);
                  ex.printStackTrace();
               }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
               // se maneja en la vista (Panel)
            } else if (boton.getName().equalsIgnoreCase("buscar")) {
               initBuscadorMovimientosVarios(abm);
               // eventos de la GUI Buscador
            } else if (boton.getName().equalsIgnoreCase("buscarBuscador")) {
               try {
                  armarQueryMovimientosVarios(false);
               } catch (Exception ex) {
                  // acá no pasa nada...
               }
            } else if (boton.getName().equalsIgnoreCase("imprimirBuscador")) {
               try {
                  armarQueryMovimientosVarios(true);
               } catch (Exception ex) {
                  abm.showMessage(ex.getMessage(), "CajaMovimientos -> Buscador Movimientos varios", 0);
               }
            } else if (boton.getName().equalsIgnoreCase("limpiarBuscador")) {
               resetBuscadorMovimientosVarios();
            }
         }// </editor-fold>

      }// </editor-fold>
      else // <editor-fold defaultstate="collapsed" desc="JComboBox">
      if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
         javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
         // <editor-fold defaultstate="collapsed" desc="Cierre de caja">
         if (combo.getName().equalsIgnoreCase("caja")) {
            if (combo.getSelectedIndex() > 0) {
               setInfoCajaMovimientos((CajaMovimientos) combo.getSelectedItem());
            } else {
               UTIL.limpiarDtm(jdCierreCaja.getDtm());
               jdCierreCaja.setDcApertura(null);
            }
         } // </editor-fold>
         // <editor-fold defaultstate="collapsed" desc="Panel CajaToCaja">
         else if (combo.getName().equalsIgnoreCase("cajaOrigen")) {
            setDatosCajaToCajaCombo("cajaOrigen");

         } else if (combo.getName().equalsIgnoreCase("cajaDestino")) {
            setDatosCajaToCajaCombo("y si no es origen tiene q ser destino.. no ?");
         }// </editor-fold>
      }// </editor-fold>
   }

   private void setInfoCajaMovimientos(CajaMovimientos cajaMovimientos) {
      DefaultTableModel dtm = jdCierreCaja.getDtm();
      UTIL.limpiarDtm(dtm);
      jdCierreCaja.setDcApertura(cajaMovimientos.getFechaApertura());

      // el detalleCajaMov de "apertura de caja" es ingreso = true
      // PERO NO DEBE ser parte del Total de Ingresos de la Caja
      // por eso se le resta una vez ... y luego se suma..
      Double totalIngresos = 0.0 - (cajaMovimientos.getMontoApertura());
      Double totalEgresos = 0.0;
      List<DetalleCajaMovimientos> //detalleCajaMovimientoList = cajaMovimientos.getDetalleCajaMovimientosList();
              detalleCajaMovimientoList = new DetalleCajaMovimientosJpaController().getDetalleCajaMovimientosByCajaMovimiento(cajaMovimientos.getId());
      for (DetalleCajaMovimientos detalleCajaMovimientos : detalleCajaMovimientoList) {
         //sumando totales..
         if (detalleCajaMovimientos.getIngreso()) {
            totalIngresos += detalleCajaMovimientos.getMonto();
         } else //los montos "egreso" son negativos
         {
            totalEgresos += detalleCajaMovimientos.getMonto();
         }

         //carga de tabla..............
         dtm.addRow(new Object[]{
                    detalleCajaMovimientos.getDescripcion(),
                    UTIL.PRECIO_CON_PUNTO.format(detalleCajaMovimientos.getMonto()),
                    UTIL.DATE_FORMAT.format(detalleCajaMovimientos.getFecha()) + " (" + UTIL.TIME_FORMAT.format(detalleCajaMovimientos.getHora()) + ")",
                    detalleCajaMovimientos.getUsuario()
                 });
      }
      dtm.addRow(new Object[]{"Total Ingresos", UTIL.PRECIO_CON_PUNTO.format(totalIngresos)});
      dtm.addRow(new Object[]{"Total Egresos", UTIL.PRECIO_CON_PUNTO.format(totalEgresos)});
      dtm.addRow(new Object[]{"Total", UTIL.PRECIO_CON_PUNTO.format(
                 cajaMovimientos.getMontoApertura() + totalIngresos + totalEgresos)});
      if (cajaMovimientos.getFechaCierre() != null) {
         selectedCajaMovimientos = cajaMovimientos;
         jdCierreCaja.setDcCierre(cajaMovimientos.getFechaCierre());
         jdCierreCaja.getDcCierre().setEnabled(false);
         jdCierreCaja.getbCerrar().setEnabled(false);
         dtm.addRow(new Object[]{"----CAJA CERRADA----CAJA CERRADA----CAJA CERRADA----CAJA CERRADA----"});

         jdCierreCaja.getLabelCAJACERRADA().setText("CAJA Nº" + selectedCajaMovimientos.getId() + " CERRADA");
         jdCierreCaja.getLabelCAJACERRADA().setVisible(true);
      } else {
         selectedCajaMovimientos = null;
         jdCierreCaja.setDcCierre(null);
         jdCierreCaja.getDcCierre().setEnabled(true);
         jdCierreCaja.getbCerrar().setEnabled(true);
         jdCierreCaja.getLabelCAJACERRADA().setVisible(false);
      }
   }

   private void cerrarCajaMovimiento(CajaMovimientos cajaMovimientos) throws MessageException, Exception {
      if (cajaMovimientos == null) {
         throw new MessageException("Debe elegir una Caja para cerrar");
      }

      if (jdCierreCaja.getFechaCierre() == null) {
         throw new MessageException("Fecha de cierre no válida");
      }

      if (jdCierreCaja.getDcApertura().after(jdCierreCaja.getFechaCierre())) {
         throw new MessageException("La fecha de cierre de caja no puede ser anterior a la de apertura");
      }


      int imprimir_caja_OK = javax.swing.JOptionPane.showConfirmDialog(jdCierreCaja,
              "¿Imprimir cierre de caja?",
              "Cierre de Caja",
              javax.swing.JOptionPane.OK_CANCEL_OPTION);

      //cerrar caja_mov (completar datos de cierre)
      cajaMovimientos.setFechaCierre(jdCierreCaja.getFechaCierre());
      cajaMovimientos.setMontoCierre(getTotal());
      //datos implicitos
      cajaMovimientos.setHoraCierre(new Date());
      cajaMovimientos.setSistemaFechaCierre(new Date());
      cajaMovimientos.setUsuarioCierre(UsuarioJpaController.getCurrentUser());
      merge(cajaMovimientos);

      if (imprimir_caja_OK == 0) {
         imprimirCierreCaja(cajaMovimientos);
      }
      //re abrir la prox...
      abrirNextCajaMovimiento(cajaMovimientos);

      //refresh jdCierreCaja...................
      reloadJDCierreCaja();

   }

   private Double getTotal() {
      return Double.valueOf(jdCierreCaja.getjTable1().getValueAt(jdCierreCaja.getjTable1().getRowCount() - 1, 1).toString());
   }

   private void setCajaMovimientosPermitidas(javax.swing.JComboBox combo) {
      //get cajas permitidas para ESTE usuario
      List<Caja> cajasPermitidasList = new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true);
      List<CajaMovimientos> cajaMovimientosAbiertasList = null;
      for (Caja caja : cajasPermitidasList) {
         //get cajaMovim abierta correspondiente a cada Caja
         CajaMovimientos cm = new CajaMovimientosJpaController().findCajaMovimientoAbierta(caja);
         if (cajaMovimientosAbiertasList == null) {
            cajaMovimientosAbiertasList = new ArrayList<CajaMovimientos>();
         }
         if (cm != null) {
            cajaMovimientosAbiertasList.add(cm);
         }
      }
      UTIL.loadComboBox(combo, cajaMovimientosAbiertasList, true);
      combo.setSelectedIndex(0);
   }

   private void reloadJDCierreCaja() {
      setCajaMovimientosPermitidas(jdCierreCaja.getCbCaja());
      jdCierreCaja.setDcApertura(null);
      jdCierreCaja.setDcCierre(null);
   }

   public void initCajaToCaja(java.awt.Frame frame, boolean modal) {
      JDcajaToCaja = new JDCajaToCaja(frame, modal);
      JDcajaToCaja.setLocationRelativeTo(frame);
      setCajaMovimientosPermitidas(JDcajaToCaja.getCbCajaOrigen());
      setCajaMovimientosPermitidas(JDcajaToCaja.getCbCajaDestino());
      JDcajaToCaja.getTfMovimiento().setText(String.valueOf(getNextMovimientoCajaToCaja()));
      JDcajaToCaja.setListener(this);
      JDcajaToCaja.setVisible(true);
      JDcajaToCaja.dispose();
   }

   private void asentarMovimientoCajaToCaja() throws MessageException, Exception {
      if (JDcajaToCaja.getCbCajaOrigen().getSelectedIndex() < 1) {
         throw new MessageException("Elegir Caja de origen");
      }
      if (JDcajaToCaja.getCbCajaDestino().getSelectedIndex() < 1) {
         throw new MessageException("Elegir Caja de destino");
      }
      if (JDcajaToCaja.getCbCajaOrigen().getSelectedIndex() == JDcajaToCaja.getCbCajaDestino().getSelectedIndex()) {
         throw new MessageException("La Caja de destino no puede ser la misma que la de origen");
      }
      if (JDcajaToCaja.getTfMontoMovimiento().getText().length() < 1) {
         throw new MessageException("Ingresar monto del movimiento");
      }
      double monto;
      try {
         monto = Double.valueOf(JDcajaToCaja.getTfMontoMovimiento().getText().trim());
         if (monto <= 0) {
            throw new MessageException("El monto de movimiento debe ser mayor a 0");
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Monto de movimiento no válido");
      }

      CajaMovimientos cajaOrigen = (CajaMovimientos) JDcajaToCaja.getCbCajaOrigen().getSelectedItem();
      CajaMovimientos cajaDestino = (CajaMovimientos) JDcajaToCaja.getCbCajaDestino().getSelectedItem();

      String observ = "";
      if (JDcajaToCaja.getTfObservacion().getText().trim().length() > 0) {
         observ = JDcajaToCaja.getTfObservacion().getText().trim();
      }
      // formato: N#-{cajaOrigen.nombre} -> {cajaDestino.nombre} [observacio]
      String descripcion = "N" + JDcajaToCaja.getTfMovimiento().getText() + "- " + cajaOrigen.toString() + " -> " + cajaDestino.toString();
      if (observ.length() > 0) {
         descripcion += " " + observ;
      }

      DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
      // <editor-fold defaultstate="collapsed" desc="Origen to Destino - EGRESO">
      dcm.setCajaMovimientos(cajaOrigen);
      dcm.setDescripcion(descripcion);
      dcm.setFecha(new java.util.Date());
      dcm.setHora(new java.util.Date());
      dcm.setIngreso(false);
      dcm.setMonto(-monto); // <--- NEGATIVIZAR!
      dcm.setNumero(Integer.parseInt(JDcajaToCaja.getTfMovimiento().getText()));
      dcm.setTipo(DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA);
      dcm.setUsuario(UsuarioJpaController.getCurrentUser());
      new DetalleCajaMovimientosJpaController().create(dcm);// </editor-fold>

      // <editor-fold defaultstate="collapsed" desc="Destino to Origen - INGRESO">
      dcm = new DetalleCajaMovimientos();
      dcm.setCajaMovimientos(cajaDestino);
      dcm.setDescripcion(descripcion);
      dcm.setFecha(new java.util.Date());
      dcm.setHora(new java.util.Date());
      dcm.setIngreso(true);
      dcm.setMonto(monto);
      dcm.setNumero(Integer.parseInt(JDcajaToCaja.getTfMovimiento().getText()));
      dcm.setTipo(DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA);
      dcm.setUsuario(UsuarioJpaController.getCurrentUser());
      new DetalleCajaMovimientosJpaController().create(dcm);// </editor-fold>

      JDcajaToCaja.showMessage("Realizado", "Movimiento entre cajas", 1);
      JDcajaToCaja.resetPanel();
      JDcajaToCaja.getTfMovimiento().setText(String.valueOf(getNextMovimientoCajaToCaja()));
      DAO.getEntityManager().clear();
   }

   private int getNextMovimientoCajaToCaja() {
      EntityManager em = getEntityManager();
      try { //se especifica o.ingreso = true .. porque cada movimiento Caja to Caja
         // genera 2 movimientos (un ingreso y un egreso) ... lo cual duplica la cantidad real
         Object o = em.createQuery("SELECT COUNT(o.id) FROM DetalleCajaMovimientos o "
                 + " WHERE o.ingreso = true AND o.tipo = " + DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA).getSingleResult();
         if (o == null) {
            return 1;
         } else {
            return 1 + Integer.valueOf(o.toString());
         }
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   private int getNextMovimientoVarios() {
      EntityManager em = getEntityManager();
      try {
         Object o = em.createQuery("SELECT COUNT(o.id) FROM DetalleCajaMovimientos o "
                 + " WHERE o.ingreso = TRUE AND o.tipo = " + DetalleCajaMovimientosJpaController.MOVIMIENTO_VARIOS).getSingleResult();
         return 1 + Integer.valueOf(o.toString());
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   private void setDatosCajaToCajaCombo(String name) {
      CajaMovimientos cajaMovimientos = null;
      String balanceCajaActual = null;
      Date fechaApertura = null;
      try {
         if (name.equalsIgnoreCase("cajaOrigen")) {
            cajaMovimientos = (CajaMovimientos) JDcajaToCaja.getCbCajaOrigen().getSelectedItem();
         } else {
            cajaMovimientos = (CajaMovimientos) JDcajaToCaja.getCbCajaDestino().getSelectedItem();
         }
         //si no saltó la ClassCastException...
         fechaApertura = cajaMovimientos.getFechaApertura();

         Double totalIngresos = 0.0; // VA INCLUIR monto de apertura
         Double totalEgresos = 0.0;
         List<DetalleCajaMovimientos> detalleCajaMovimientoList = cajaMovimientos.getDetalleCajaMovimientosList();
         for (DetalleCajaMovimientos detalleCajaMovimientos : detalleCajaMovimientoList) {
            if (detalleCajaMovimientos.getIngreso()) {
               totalIngresos += detalleCajaMovimientos.getMonto();
            } else //siempre son montos negativos
            {
               totalEgresos += detalleCajaMovimientos.getMonto();
            }
         }
         balanceCajaActual = UTIL.PRECIO_CON_PUNTO.format(totalIngresos + totalEgresos);
      } catch (ClassCastException e) {
         System.out.println(e.getClass() + " .....CajaToCaja");
      }

      if (name.equalsIgnoreCase("CajaOrigen")) {
         JDcajaToCaja.getTfTotalOrigen().setText(balanceCajaActual);
         JDcajaToCaja.getDcOrigen().setDate(fechaApertura);
      } else {
         JDcajaToCaja.getTfTotalDestino().setText(balanceCajaActual);
         JDcajaToCaja.getDcDestino().setDate(fechaApertura);
      }
   }

   /**
    * Desplega la GUI para realizar Movimientos varios
    * @param frame papi Component
    * @param modal
    */
   public void initMovimientosVarios(javax.swing.JFrame frame, boolean modal) {
      panelMovVarios = new PanelMovimientosVarios();
      UTIL.loadComboBox(panelMovVarios.getCbCaja(), new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), false);
      panelMovVarios.setListener(this);
      abm = new JDABM(frame, modal, panelMovVarios);
      abm.setTitle("Movimientos varios");
      abm.setLocationRelativeTo(frame);
      abm.setListener(this);
      abm.setVisible(true);
   }

   private void asentarMovimientoVarios() throws MessageException {
      //ctrl's................
      double monto;
      try {
         monto = Double.valueOf(panelMovVarios.getTfMontoMovimiento());
         if (monto <= 0) {
            throw new MessageException("El monto debe ser mayor a 0");
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Monto no válido");
      }

      if (panelMovVarios.getTfDescripcion().length() < 1) {
         throw new MessageException("Debe ingresar una Descripción");
      }

      //setting entity.....
      DetalleCajaMovimientos dcm = new DetalleCajaMovimientos();
      dcm.setCajaMovimientos(findCajaMovimientoAbierta((Caja) panelMovVarios.getCbCaja().getSelectedItem()));
      dcm.setIngreso(panelMovVarios.isIngreso());
      dcm.setDescripcion("MV" + (dcm.getIngreso() ? "I" : "E") + "-" + panelMovVarios.getTfDescripcion());
      dcm.setFecha(new java.util.Date());
      dcm.setHora(new java.util.Date());
      dcm.setMonto(dcm.getIngreso() ? monto : -monto);
      dcm.setNumero(-1);
      dcm.setTipo(DetalleCajaMovimientosJpaController.MOVIMIENTO_VARIOS);
      dcm.setUsuario(UsuarioJpaController.getCurrentUser());
      new DetalleCajaMovimientosJpaController().create(dcm);
   }

   private void initBuscadorMovimientosVarios(javax.swing.JDialog papiComponent) {
      panelBuscadorMovimientosVarios = new PanelBuscadorMovimientosVarios();
      UTIL.loadComboBox(panelBuscadorMovimientosVarios.getCbCaja(),
              new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);

      buscador = new JDBuscador(papiComponent, false, panelBuscadorMovimientosVarios, "Buscardor - Movimientos varios");
      try {
         UTIL.getDefaultTableModel(
                 new String[]{"Caja", "Descripción", "Monto", "Fecha (Hora)", "Usurio"},
                 new int[]{70, 160, 20, 60, 50},
                 buscador.getjTable1());
      } catch (Exception ex) {
         Logger.getLogger(CajaMovimientosJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      buscador.hideLimpiar();
      buscador.setListener(this);
      buscador.setLocationRelativeTo((Component) papiComponent);
      buscador.setVisible(true);
   }

   private void initBuscadorCajaToCaja(javax.swing.JDialog papiComponent) {
      panelBuscadorCajaToCaja = new PanelBuscadorCajaToCaja();
      UTIL.loadComboBox(panelBuscadorCajaToCaja.getCbCajaOrigen(),
              new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);
      UTIL.loadComboBox(panelBuscadorCajaToCaja.getCbCajaDestino(),
              new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);

      buscador = new JDBuscador(papiComponent, false, panelBuscadorCajaToCaja, "Buscardor - Movimientos entre Cajas");
      try {
         UTIL.getDefaultTableModel(
                 new String[]{"Descripción", "Monto", "Fecha (Hora)", "Usuario"},
                 new int[]{    150,           20,       60,            50},
                 buscador.getjTable1());
      } catch (Exception ex) {
         Logger.getLogger(CajaMovimientosJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      buscador.hideLimpiar();
      buscador.setListener(this);
      buscador.setLocationRelativeTo((Component) papiComponent);
      buscador.setVisible(true);
   }

   private void armarQueryMovimientosVarios(boolean doReport) throws Exception {
      String query = "SELECT o.*, caja.nombre as cajanombre"
              + " FROM detalle_caja_movimientos o, caja_movimientos cm , caja"
              + " WHERE o.caja_movimientos = cm.id "
              + " AND cm.caja = caja.id AND o.tipo = " + DetalleCajaMovimientosJpaController.MOVIMIENTO_VARIOS;

      if (panelBuscadorMovimientosVarios.getCbCaja().getSelectedIndex() > 0) {
         query += " AND caja.id=" + ((Caja) panelBuscadorMovimientosVarios.getCbCaja().getSelectedItem()).getId();
      } else {
         // carga todas las Cajas que tiene permitidas..
         query += " AND (";
         for (int i = 1; i < panelBuscadorMovimientosVarios.getCbCaja().getItemCount(); i++) {
            if (i > 1) {
               query += " OR ";
            }
            query += " caja.id =" + ((Caja) panelBuscadorMovimientosVarios.getCbCaja().getItemAt(i)).getId();
         }
         query += ")";
      }

      if (panelBuscadorMovimientosVarios.getCbEstadoCaja().getSelectedIndex() == 0) {
         query += " AND cm.fecha_cierre IS NULL ";
      } else if (panelBuscadorMovimientosVarios.getCbEstadoCaja().getSelectedIndex() == 1) {
         query += " AND cm.fecha_cierre IS NOT NULL ";
      }

      if (panelBuscadorMovimientosVarios.getCbIngresoEgreso().getSelectedIndex() == 1) {
         query += " AND o.ingreso = TRUE";
      } else if (panelBuscadorMovimientosVarios.getCbIngresoEgreso().getSelectedIndex() == 2) {
         query += " AND o.ingreso = FALSE";
      }

      if (panelBuscadorMovimientosVarios.getDcDesde() != null) {
         query += " AND o.fecha >='" + panelBuscadorMovimientosVarios.getDcDesde() + "'";
      }

      if (panelBuscadorMovimientosVarios.getDcHasta() != null) {
         query += " AND o.fecha <='" + panelBuscadorMovimientosVarios.getDcHasta() + "'";
      }

      System.out.println(query);
      cargarDtmBuscador(query);
      if (doReport) {
         doReport(query);
      }
   }

   private void doReport(String query) throws Exception {
      Reportes r = null;
      if (panelBuscadorMovimientosVarios != null) {
         r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_MovimientosVarios.jasper", "Resumen: Movimientos varios");
         r.addParameter("QUERY", query);
         r.addParameter("FECHA_DESDE", panelBuscadorMovimientosVarios.getDcDesde());
         r.addParameter("FECHA_HASTA", panelBuscadorMovimientosVarios.getDcHasta());
      } else if (panelBuscadorCajaToCaja != null) {
         r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_CajaToCaja.jasper", "Resumen: Movimientos entre Cajas");
         r.addParameter("QUERY", query);
      }
      r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
      r.addCurrent_User();
      r.printReport();
   }

   private void cargarDtmBuscador(String query) {
      DefaultTableModel dtm = buscador.getDtm();
      UTIL.limpiarDtm(dtm);
      List<DetalleCajaMovimientos> lista = DAO.getEntityManager().createNativeQuery(query, DetalleCajaMovimientos.class).getResultList();
      for (DetalleCajaMovimientos dcm : lista) {
      //dependiendo del buscador que esté activo..
         if (panelBuscadorCajaToCaja != null) {
            dtm.addRow(new Object[] {
                    dcm.getDescripcion(),
                    UTIL.PRECIO_CON_PUNTO.format(dcm.getMonto()),
                    UTIL.DATE_FORMAT.format(dcm.getFecha()) + "(" + UTIL.TIME_FORMAT.format(dcm.getHora()) + ")",
                    dcm.getUsuario()
                 });
         } else if (panelBuscadorMovimientosVarios != null) {
            dtm.addRow(new Object[] {
                    dcm.getCajaMovimientos(),
                    dcm.getDescripcion(),
                    UTIL.PRECIO_CON_PUNTO.format(dcm.getMonto()),
                    UTIL.DATE_FORMAT.format(dcm.getFecha()) + "(" + UTIL.TIME_FORMAT.format(dcm.getHora()) + ")",
                    dcm.getUsuario()
                 });
         }
      }
   }

   private void cleanPanelMovimientosVarios() {
      panelMovVarios.setTfMonto("");
      panelMovVarios.setTfDescripcion("");
   }

   private void resetBuscadorMovimientosVarios() {
      panelBuscadorMovimientosVarios.getCbCaja().setSelectedIndex(0);
      panelBuscadorMovimientosVarios.getCbEstadoCaja().setSelectedIndex(0);
      panelBuscadorMovimientosVarios.getCbIngresoEgreso().setSelectedIndex(0);
      panelBuscadorMovimientosVarios.setDatesToNull();
      UTIL.limpiarDtm(buscador.getDtm());
   }

   private void initBuscadorCierreCaja(Object jdCierreCaja) {
      panelBuscadorCajasCerradas = new PanelBuscadorCajasCerradas();

      UTIL.loadComboBox(panelBuscadorCajasCerradas.getCbCaja(),
              new CajaJpaController().findCajasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);
      buscador = new JDBuscador(this.jdCierreCaja, true, panelBuscadorCajasCerradas, "Buscador - Cajas cerradas");
      buscador.hideImprimir();
      buscador.hideLimpiar();
      buscador.getjTable1().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseClicked(MouseEvent e) {
            jTableBuscadorCajaCerradas(e);
         }
      });
      try {
         UTIL.getDefaultTableModel(
                 new String[]{"Caja", "F. apertura", "F. Cierre", "Total cierre", " Usuario"},
                 new int[]{70, 50, 50, 30, 60},
                 buscador.getjTable1());
      } catch (Exception ex) {
         Logger.getLogger(CajaMovimientosJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      buscador.setListener(this);
      buscador.setLocationRelativeTo((Component) jdCierreCaja);
      buscador.setVisible(true);
   }

   private void jTableBuscadorCajaCerradas(MouseEvent e) {
      if (e.getClickCount() >= 2) {
         if (buscador != null && panelBuscadorCajasCerradas != null) {
            setInfoCajaMovimientos(getSelectedCajaMovimientos(buscador.getjTable1()));
            buscador.dispose();
         }
      }
   }

   private void armarQueryCierreCajas() {
      String query = "SELECT o.*"
              + " FROM caja_movimientos o, caja"
              + " WHERE o.caja = caja.id AND o.fecha_cierre IS NOT NULL";

      if (panelBuscadorCajasCerradas.getCbCaja().getSelectedIndex() > 0) {
         query += " AND o.caja =" + ((Caja) panelBuscadorCajasCerradas.getCbCaja().getSelectedItem()).getId();
      } else {
         // carga todas las Cajas que tiene permitidas..
         query += " AND (";
         for (int i = 1; i < panelBuscadorCajasCerradas.getCbCaja().getItemCount(); i++) {
            if (i > 1) {
               query += " OR ";
            }
            query += " o.caja =" + ((Caja) panelBuscadorCajasCerradas.getCbCaja().getItemAt(i)).getId();
         }
         query += ")";
      }
      if (panelBuscadorCajasCerradas.getDcDesde() != null) {
         query += " AND o.fecha_cierre >='" + panelBuscadorCajasCerradas.getDcDesde() + "'";
      }

      if (panelBuscadorCajasCerradas.getDcHasta() != null) {
         query += " AND o.fecha_cierre <='" + panelBuscadorCajasCerradas.getDcHasta() + "'";
      }
      query += " ORDER BY o.id";
      System.out.println(query);
      cargarDtmBuscadorCierreCaja(query);
   }

   private void cargarDtmBuscadorCierreCaja(String query) {
      DefaultTableModel dtm = buscador.getDtm();
      UTIL.limpiarDtm(dtm);
      List<CajaMovimientos> cajaMovimientosList = DAO.getEntityManager().createNativeQuery(query, CajaMovimientos.class).getResultList();

      for (CajaMovimientos cajaMovimientos : cajaMovimientosList) {
         dtm.addRow(new Object[]{
                    cajaMovimientos,
                    UTIL.DATE_FORMAT.format(cajaMovimientos.getFechaApertura()),
                    UTIL.DATE_FORMAT.format(cajaMovimientos.getFechaCierre()),
                    UTIL.PRECIO_CON_PUNTO.format(cajaMovimientos.getMontoCierre()),
                    cajaMovimientos.getUsuarioCierre()
                 });
      }
   }

   private void imprimirCierreCaja(CajaMovimientos cajaMovimientos) throws Exception {
      Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_CierreCaja.jasper", "Cierre de caja");
      r.addCurrent_User();
      r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
      try {
         r.addParameter("USUARIO_CIERRE", cajaMovimientos.getUsuarioCierre().toString());
      } catch (NullPointerException ex) {
         r.addParameter("USUARIO_CIERRE", "");
      }
      r.addParameter("CAJA_MOVIMIENTO_ID", cajaMovimientos.getId());
      r.viewReport();
   }

   /**
    * Pone como Descripción del DetalleCajaMovimiento la FacturaVenta
    * @param facturaVenta
    */
   void actualizarDescripcion(FacturaVenta facturaVenta) {
      DetalleCajaMovimientos dcm = new DetalleCajaMovimientosJpaController()
              .findDetalleCajaMovimientosByNumero(facturaVenta.getId(), DetalleCajaMovimientosJpaController.FACTU_VENTA);
      dcm.setDescripcion(getDescripcion(facturaVenta));
      DAO.doMerge(dcm);
   }

   private CajaMovimientos getSelectedCajaMovimientos(javax.swing.JTable table) {
      return (CajaMovimientos) table.getModel().getValueAt(table.getSelectedRow(), 0);
   }

   private void armarQueryMovimientosCajaToCaja(boolean doReport) throws Exception {
      String query =
      "SELECT b.*, u.nick" +
      " FROM (SELECT oo.*"+
		 " FROM detalle_caja_movimientos oo, caja_movimientos cm, caja"+
		 " WHERE oo.ingreso = true AND oo.caja_movimientos = cm.id  AND cm.caja = caja.id AND oo.tipo = " + DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA;

      String cajasQuery = "";
      if (panelBuscadorCajaToCaja.getCbCajaDestino().getSelectedIndex() > 0) {
         cajasQuery += " AND caja.id=" + ((Caja) panelBuscadorCajaToCaja.getCbCajaDestino().getSelectedItem()).getId();
      } else {
         // carga todas las Cajas ORIGEN que tiene permitidas..
         cajasQuery += " AND (";
         for (int i = 1; i < panelBuscadorCajaToCaja.getCbCajaOrigen().getItemCount(); i++) {
            if (i > 1) {
               cajasQuery += " OR ";
            }
            cajasQuery += " caja.id =" + ((Caja) panelBuscadorCajaToCaja.getCbCajaOrigen().getItemAt(i)).getId();
         }
         cajasQuery += ")";
      }
      query += cajasQuery;

      query += ") b," +
		 " (SELECT oo.numero" +
		 " FROM detalle_caja_movimientos oo, caja_movimientos cm, caja " +
		 " WHERE oo.ingreso = false AND oo.caja_movimientos = cm.id  AND cm.caja = caja.id AND oo.tipo = " + DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA;

      cajasQuery = "";
      if (panelBuscadorCajaToCaja.getCbCajaOrigen().getSelectedIndex() > 0) {
         cajasQuery += " AND caja.id=" + ((Caja) panelBuscadorCajaToCaja.getCbCajaOrigen().getSelectedItem()).getId();
      } else {
         // carga todas las Cajas ORIGEN que tiene permitidas..
         cajasQuery += " AND (";
         for (int i = 1; i < panelBuscadorCajaToCaja.getCbCajaOrigen().getItemCount(); i++) {
            if (i > 1) {
               cajasQuery += " OR ";
            }
            cajasQuery += " caja.id =" + ((Caja) panelBuscadorCajaToCaja.getCbCajaOrigen().getItemAt(i)).getId();
         }
         cajasQuery += ")";
      }
      query += cajasQuery;
      query += ") a, usuario u";
      query += " WHERE a.numero = b.numero AND b.usuario = u.id";

      if (panelBuscadorCajaToCaja.getDcDesde() != null) {
         query += " AND b.fecha >='" + panelBuscadorCajaToCaja.getDcDesde() + "'";
      }
      if (panelBuscadorCajaToCaja.getDcHasta() != null) {
         query += " AND b.fecha <='" + panelBuscadorCajaToCaja.getDcHasta() + "'";
      }
      
      query += " ORDER BY b.id";

      System.out.println(query);
      cargarDtmBuscador(query);
      if (doReport) {
         doReport(query);
      }
   }
}
