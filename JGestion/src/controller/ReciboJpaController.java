package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.Caja;
import entity.CajaMovimientos;
import entity.Cliente;
import entity.CtacteCliente;
import entity.DetalleCajaMovimientos;
import entity.Recibo;
import gui.JFP;
import java.text.ParseException;
import java.util.Iterator;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.DetalleRecibo;
import entity.FacturaVenta;
import entity.Sucursal;
import generics.UTIL;
import gui.JDBuscadorReRe;
import gui.JDReRe;
import gui.PanelModeloRecibo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrador
 */
public class ReciboJpaController implements ActionListener, FocusListener {

   private final String CLASS_NAME = Recibo.class.getSimpleName();
   private final String[] colsName = {"facturaID", "Factura", "Observación", "Entrega"};
   private final int[] colsWidth = {1, 50, 150, 30};
   private JDReRe jdReRe;
   private CtacteCliente selectedCtaCte;
   private java.util.Date selectedFechaReRe = null;
   private JDBuscadorReRe buscador;
   private Recibo rereSelected;

//   public ReciboJpaController() {
//      emf = Persistence.createEntityManagerFactory("JGestionPU");
//   }
//   private EntityManagerFactory emf = null;
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   // <editor-fold defaultstate="collapsed" desc="CRUD..">
   public void create(Recibo recibo) throws PreexistingEntityException, Exception {
      if (recibo.getDetalleReciboList() == null) {
         recibo.setDetalleReciboList(new ArrayList<DetalleRecibo>());
      }
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         List<DetalleRecibo> attachedDetalleReciboList = new ArrayList<DetalleRecibo>();
         for (DetalleRecibo detalleReciboListDetalleReciboToAttach : recibo.getDetalleReciboList()) {
            detalleReciboListDetalleReciboToAttach = em.merge(detalleReciboListDetalleReciboToAttach);
            attachedDetalleReciboList.add(detalleReciboListDetalleReciboToAttach);
         }
         recibo.setDetalleReciboList(attachedDetalleReciboList);
         em.persist(recibo);
//         for (DetalleRecibo detalleReciboListDetalleRecibo : recibo.getDetalleReciboList()) {
//            Recibo oldReciboOfDetalleReciboListDetalleRecibo = detalleReciboListDetalleRecibo.getRecibo();
//            detalleReciboListDetalleRecibo.setRecibo(recibo);
//            detalleReciboListDetalleRecibo = em.merge(detalleReciboListDetalleRecibo);
//            if (oldReciboOfDetalleReciboListDetalleRecibo != null) {
//               oldReciboOfDetalleReciboListDetalleRecibo.getDetalleReciboList().remove(detalleReciboListDetalleRecibo);
//               oldReciboOfDetalleReciboListDetalleRecibo = em.merge(oldReciboOfDetalleReciboListDetalleRecibo);
//            }
//         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         if (findRecibo(recibo.getId()) != null) {
            throw new PreexistingEntityException("Recibo " + recibo + " already exists.", ex);
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(Recibo recibo) throws IllegalOrphanException, NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Recibo persistentRecibo = em.find(Recibo.class, recibo.getId());
         List<DetalleRecibo> detalleReciboListOld = persistentRecibo.getDetalleReciboList();
         List<DetalleRecibo> detalleReciboListNew = recibo.getDetalleReciboList();
         List<String> illegalOrphanMessages = null;
         for (DetalleRecibo detalleReciboListOldDetalleRecibo : detalleReciboListOld) {
            if (!detalleReciboListNew.contains(detalleReciboListOldDetalleRecibo)) {
               if (illegalOrphanMessages == null) {
                  illegalOrphanMessages = new ArrayList<String>();
               }
               illegalOrphanMessages.add("You must retain DetalleRecibo " + detalleReciboListOldDetalleRecibo + " since its recibo field is not nullable.");
            }
         }
         if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
         }
         List<DetalleRecibo> attachedDetalleReciboListNew = new ArrayList<DetalleRecibo>();
         for (DetalleRecibo detalleReciboListNewDetalleReciboToAttach : detalleReciboListNew) {
            detalleReciboListNewDetalleReciboToAttach = em.getReference(detalleReciboListNewDetalleReciboToAttach.getClass(), detalleReciboListNewDetalleReciboToAttach.getId());
            attachedDetalleReciboListNew.add(detalleReciboListNewDetalleReciboToAttach);
         }
         detalleReciboListNew = attachedDetalleReciboListNew;
         recibo.setDetalleReciboList(detalleReciboListNew);
         recibo = em.merge(recibo);
         for (DetalleRecibo detalleReciboListNewDetalleRecibo : detalleReciboListNew) {
            if (!detalleReciboListOld.contains(detalleReciboListNewDetalleRecibo)) {
               Recibo oldReciboOfDetalleReciboListNewDetalleRecibo = detalleReciboListNewDetalleRecibo.getRecibo();
               detalleReciboListNewDetalleRecibo.setRecibo(recibo);
               detalleReciboListNewDetalleRecibo = em.merge(detalleReciboListNewDetalleRecibo);
               if (oldReciboOfDetalleReciboListNewDetalleRecibo != null && !oldReciboOfDetalleReciboListNewDetalleRecibo.equals(recibo)) {
                  oldReciboOfDetalleReciboListNewDetalleRecibo.getDetalleReciboList().remove(detalleReciboListNewDetalleRecibo);
                  oldReciboOfDetalleReciboListNewDetalleRecibo = em.merge(oldReciboOfDetalleReciboListNewDetalleRecibo);
               }
            }
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Long id = recibo.getId();
            if (findRecibo(id) == null) {
               throw new NonexistentEntityException("The recibo with id " + id + " no longer exists.");
            }
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void destroy(Long id) throws IllegalOrphanException, NonexistentEntityException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         Recibo recibo;
         try {
            recibo = em.getReference(Recibo.class, id);
            recibo.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The recibo with id " + id + " no longer exists.", enfe);
         }
         List<String> illegalOrphanMessages = null;
         List<DetalleRecibo> detalleReciboListOrphanCheck = recibo.getDetalleReciboList();
         for (DetalleRecibo detalleReciboListOrphanCheckDetalleRecibo : detalleReciboListOrphanCheck) {
            if (illegalOrphanMessages == null) {
               illegalOrphanMessages = new ArrayList<String>();
            }
            illegalOrphanMessages.add("This Recibo (" + recibo + ") cannot be destroyed since the DetalleRecibo " + detalleReciboListOrphanCheckDetalleRecibo + " in its detalleReciboList field has a non-nullable recibo field.");
         }
         if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
         }
         em.remove(recibo);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<Recibo> findReciboEntities() {
      return findReciboEntities(true, -1, -1);
   }

   public List<Recibo> findReciboEntities(int maxResults, int firstResult) {
      return findReciboEntities(false, maxResults, firstResult);
   }

   private List<Recibo> findReciboEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from Recibo as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public Recibo findRecibo(Long id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(Recibo.class, id);
      } finally {
         em.close();
      }
   }

   public int getReciboCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from Recibo as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   private Long getNextNumeroRecibo() {
      EntityManager em = getEntityManager();
      Long next_factu = 100000001L;
      try {
         next_factu = 1 + (Long) em.createQuery("SELECT MAX(o.id)"
                 + " FROM " + CLASS_NAME + " o").getSingleResult();
      } catch (NoResultException ex) {
         System.out.println("pintó la 1ra " + CLASS_NAME + "....NoResultEx");
      } catch (NullPointerException ex) {
         System.out.println("pintó la 1ra " + CLASS_NAME + "....NullPointerEx");
      } finally {
         if (em != null) {
            em.close();
         }
      }
      return next_factu;
   }

   /**
    * Crea la ventana para realizar Recibo's
    * @param frame owner/parent
    * @param modal debería ser <code>true</code> siempre, no está implementado para false
    * @param setVisible
    */
   public void initRecibos(JFrame frame, boolean modal, boolean setVisible) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.VENTA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      jdReRe = new JDReRe(frame, modal);
      //seteos de GUI --->
      jdReRe.setTitle(CLASS_NAME + "s");
      jdReRe.getLabelReRe().setText("Nº " + CLASS_NAME);
      jdReRe.getLabelClienteProveedor().setText("Cliente");
      UTIL.getDefaultTableModel(jdReRe.getjTable1(), colsName, colsWidth);
      UTIL.hideColumnTable(jdReRe.getjTable1(), 0);

      setNextNumeroReRe();
      UTIL.loadComboBox(jdReRe.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), false);
      UTIL.loadComboBox(jdReRe.getCbCaja(), new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), false);
      UTIL.loadComboBox(jdReRe.getCbClienteProveedor(), new ClienteJpaController().findClienteEntities(), true);
      UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
      jdReRe.getbAnular().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               anular(rereSelected);
               jdReRe.showMessage(CLASS_NAME + " anulada..", CLASS_NAME, 1);
               resetPanel();
            } catch (MessageException ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
            }
         }
      });
      jdReRe.getbAceptar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               checkConstraints();
               setEntityAndPersist();
               jdReRe.showMessage(CLASS_NAME + " cargado..", CLASS_NAME, 1);
               limpiarDetalle();
               resetPanel();

            } catch (MessageException ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      });
      jdReRe.getBtnADD().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               addEntregaToDetalle();
            } catch (MessageException ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      });
      jdReRe.getBtnDEL().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            delEntragaFromDetalle();
         }
      });
      jdReRe.getbImprimir().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               if (rereSelected != null) {
                  // cuando se imprime un recibo elejido desde el buscador (uno pre existente)
                  imprimirRecibo(rereSelected);
               } else {
                  //cuando se está creando un recibo y se va imprimir al tokesaun!
                  checkConstraints();
                  setEntityAndPersist();
                  imprimirRecibo(rereSelected);
                  limpiarDetalle();
                  resetPanel();
               }
            } catch (MessageException ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      });
      jdReRe.getbBuscar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            initBuscador(jdReRe, true);
            if (rereSelected != null) {
               setDatosRecibo(rereSelected);
            }
         }
      });
      jdReRe.getCbClienteProveedor().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (jdReRe.getCbClienteProveedor().getSelectedIndex() > 0) {
               cargarCtaCtes((Cliente) jdReRe.getCbClienteProveedor().getSelectedItem());
            } else {
               //si no eligió nada.. vacia el combo de cta cte's
               UTIL.loadComboBox(jdReRe.getCbCtaCtes(), null, false);
               limpiarDetalle();
            }
         }
      });
      jdReRe.getCbCtaCtes().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               try {
                  selectedCtaCte = (CtacteCliente) jdReRe.getCbCtaCtes().getSelectedItem();
                  jdReRe.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
                  jdReRe.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
                  jdReRe.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
               } catch (ClassCastException ex) {
                  selectedCtaCte = null;
                  System.out.println("No se pudo caster a CtaCteProveedor -> " + jdReRe.getCbCtaCtes().getSelectedItem());
               }
            } catch (NullPointerException ex) {
               //cuando no eligio una ctacte aún o el cliente/proveedor no tiene ninguna
            }
         }
      });
      jdReRe.getbCancelar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            resetPanel();
            limpiarDetalle();
         }
      });
      jdReRe.setListener(this);
      jdReRe.setLocation(jdReRe.getOwner().getY() + 100, jdReRe.getOwner().getX() + 50);
      jdReRe.setVisible(setVisible);
   }

   public void buscadorMouseClicked(MouseEvent e) {
      if (buscador != null) {
         if (e.getClickCount() > 1) {
            setSelectedRecibo();
         }
      }
   }

   @Override
   public void actionPerformed(ActionEvent e) {
   }

   private void checkConstraints() throws MessageException {
      if (jdReRe.getDtm().getRowCount() < 1) {
         throw new MessageException("No ha hecho ninguna entrega");
      }

      if (jdReRe.getDcFechaReRe() == null) {
         throw new MessageException("Fecha de " + CLASS_NAME + " no válida");
      }

   }

   private void setEntityAndPersist() throws Exception {
      Recibo recibo = new Recibo();
      recibo.setId(Long.valueOf(jdReRe.getTfCuarto() + jdReRe.getTfOcteto()));
      recibo.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
      recibo.setSucursal((Sucursal) jdReRe.getCbSucursal().getSelectedItem());
      recibo.setUsuario(UsuarioJpaController.getCurrentUser());
      recibo.setEstado(true);
      recibo.setFechaRecibo(jdReRe.getDcFechaReRe());
      recibo.setMonto(Double.parseDouble(jdReRe.getTfTotalPagado()));
      // 30% faster on ArrayList with initialCapacity :OO
      recibo.setDetalleReciboList(new ArrayList<DetalleRecibo>(jdReRe.getDtm().getRowCount()));
      DefaultTableModel dtm = jdReRe.getDtm();
      FacturaVentaJpaController fcc = new FacturaVentaJpaController();
      DetalleRecibo dr;
      for (int i = dtm.getRowCount() - 1; i > -1; i--) {
         dr = new DetalleRecibo();
         dr.setFacturaVenta(fcc.findFacturaVenta(Integer.valueOf(dtm.getValueAt(i, 0).toString())));
         dr.setObservacion(dtm.getValueAt(i, 2).toString());
         dr.setMontoEntrega(Double.parseDouble(dtm.getValueAt(i, 3).toString()));
         dr.setRecibo(recibo);
         recibo.getDetalleReciboList().add(dr);

      }
      create(recibo);
      rereSelected = recibo;
      Iterator<DetalleRecibo> iterator = recibo.getDetalleReciboList().iterator();
      while (iterator.hasNext()) {
         dr = iterator.next();
         //actuliza saldo pagado de cada ctacte
         actualizarMontoEntrega(dr.getFacturaVenta(), dr.getMontoEntrega());
      }
      //registrando pago en CAJA
      new CajaMovimientosJpaController().asentarMovimiento(recibo);
   }

   private void actualizarMontoEntrega(FacturaVenta factu, double monto) {
      CtacteCliente ctacte = new CtacteClienteJpaController().findCtacteClienteByFactura(factu.getId());
      System.out.println("updatingMontoEntrega: CtaCte:" + ctacte.getId() + " -> Importe: " + ctacte.getImporte() + " Entregado:" + ctacte.getEntregado() + " + " + monto);

      ctacte.setEntregado(ctacte.getEntregado() + monto);
      if (ctacte.getImporte() == ctacte.getEntregado()) {
         ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getEstado());
         System.out.println("CtaCte Nº:" + ctacte.getId() + " SALDADA");
      }
      DAO.doMerge(ctacte);
   }

   private void limpiarDetalle() {
      UTIL.limpiarDtm(jdReRe.getDtm());
      jdReRe.setTfImporte("0");
      jdReRe.setTfEntrega("");
      jdReRe.setTfObservacion("");
      jdReRe.setTfSaldo("0");
      jdReRe.setTfTotalPagado("0");
      selectedFechaReRe = null;
   }

   private void addEntregaToDetalle() throws MessageException {
      if (jdReRe.getDcFechaReRe() == null) {
         throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes");
      }

      if (selectedCtaCte == null) {
         throw new MessageException("No hay Factura seleccionada");
      }

      try {
         if (UTIL.DATE_FORMAT.parse(UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe())).before(UTIL.DATE_FORMAT.parse(UTIL.DATE_FORMAT.format(selectedCtaCte.getFechaCarga())))) {
            throw new MessageException("La fecha de la " + CLASS_NAME + " no puede ser anterior"
                    + "\n a la de la Cta Cte del Proveedor ("
                    + UTIL.DATE_FORMAT.format(selectedCtaCte.getFechaCarga()) + ")");
         }
      } catch (ParseException ex) {
         //nunca VA PASAR!!!
      }

      // si ya se cargó un detalle de entrega
      // y sigue habiendo al menos UN detalle agregado (dtm no vacia)
      // ctrla que la fecha de ReRe siga siendo la misma
      if ((selectedFechaReRe != null) && (jdReRe.getDtm().getRowCount() > 0)
              && (!UTIL.DATE_FORMAT.format(selectedFechaReRe).equals(UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe())))) {
         throw new MessageException("La fecha de " + CLASS_NAME + " a sido cambiada"
                 + "\nAnterior: " + UTIL.DATE_FORMAT.format(selectedFechaReRe)
                 + "\nActual: " + UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe()));
      } else {
         selectedFechaReRe = jdReRe.getDcFechaReRe();
      }
      FacturaVenta facturaToAddToDetail = selectedCtaCte.getFactura();
      double entrega;
      String observacion = jdReRe.getTfObservacion();
      try {
         entrega = Double.parseDouble(jdReRe.getTfEntrega());
         if (entrega <= 0) {
            throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
         }

//         if(entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()) )
         if (entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
            throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante");
         }

      } catch (NumberFormatException ex) {
         throw new MessageException("Monto de entrega no válido");
      }
      if (observacion.length() > 200) {
         throw new MessageException("La Observación no puede superar los 200 caracteres (no es una novela)");
      }

      for (int i = 0; i < jdReRe.getDtm().getRowCount(); i++) {
         if (facturaToAddToDetail.getId() == (Integer) jdReRe.getDtm().getValueAt(i, 0)) {
            throw new MessageException("El detalle ya contiene una entrega de esta factura");
         }
      }

      jdReRe.getDtm().addRow(new Object[]{
                 facturaToAddToDetail.getId(),
                 //por si es un MovimientiInterno y no un número de FacturaVenta
                 (facturaToAddToDetail.getNumero() != 0) ? UTIL.AGREGAR_CEROS(String.valueOf(facturaToAddToDetail.getNumero()), 12) : "I" + String.valueOf(facturaToAddToDetail.getMovimientoInterno()),
                 observacion,
                 entrega
              });
      double totalEntregado = Double.valueOf(jdReRe.getTfTotalPagado());
      jdReRe.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(totalEntregado + entrega));
   }

   /**
    * Borra la fila seleccionada, del DetalleRecibo
    */
   private void delEntragaFromDetalle() {
      int selectedRow = jdReRe.getjTable1().getSelectedRow();
      if (selectedRow > -1) {
         double entrega = Double.valueOf(jdReRe.getDtm().getValueAt(selectedRow, 3).toString());
         double totalEntregado = Double.valueOf(jdReRe.getTfTotalPagado());
         jdReRe.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(totalEntregado - entrega));
         jdReRe.getDtm().removeRow(selectedRow);
      }
   }

   private void initBuscador(JDialog dialog, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.VENTA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      buscador = new JDBuscadorReRe(dialog, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
      initBuscador();
   }

   public void initBuscador(JFrame frame, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.VENTA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      buscador = new JDBuscadorReRe(frame, "Buscador - " + CLASS_NAME, modal, "Cliente", "Nº " + CLASS_NAME);
      initBuscador();
   }

   private void initBuscador() {
      buscador.setParaRecibos();
      UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteJpaController().findClienteEntities(), true);
      UTIL.loadComboBox(buscador.getCbCaja(), new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), true);
      UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
      UTIL.getDefaultTableModel(
              buscador.getjTable1(),
              new String[]{"Nº Recibo", "Monto", "Fecha", "Sucursal", "Caja", "Usuario", "Fecha/Hora (Sist)"},
              new int[]{50, 30, 40, 50, 50, 50, 70});
      buscador.getjTable1().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseClicked(MouseEvent e) {
            buscadorMouseClicked(e);
         }
      });
      buscador.getbBuscar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               armarQuery();
            } catch (MessageException ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
            }
         }
      });
      buscador.setVisible(true);
   }

   private void cargarCtaCtes(Cliente cliente) {
      limpiarDetalle();
      List<CtacteCliente> ctacteClientePendientesList = new CtacteClienteJpaController().findCtacteClienteByCliente(cliente.getId(), Valores.PENDIENTE);
      UTIL.loadComboBox(jdReRe.getCbCtaCtes(), ctacteClientePendientesList, false);
   }

   /**
    * Resetea la ventana;
    * - pone la fecha actual
    * - clienteProveedor.index(0)
    * - setea el NextNumeroReRe
    * - rereSelected = null;
    */
   private void resetPanel() {
      jdReRe.setDcFechaReRe(new java.util.Date());
      jdReRe.getCbClienteProveedor().setSelectedIndex(0);
      setNextNumeroReRe();
//      bloquearVentana(false);
      rereSelected = null;
   }

   private void armarQuery() throws MessageException {
      String query = "SELECT o.* FROM recibo o, cliente p , caja c, detalle_recibo dr, factura_venta f, usuario u, sucursal s  "
              + " WHERE o.id = dr.recibo "
              + "   AND o.caja = c.id "
              + "   AND o.usuario = u.id "
              + "   AND o.sucursal = s.id "
              + "   AND f.id = dr.factura_venta "
              + "   AND p.id = f.cliente"
              + "   ";

      long numero;
      //filtro por nº de ReRe
      if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
            query += " AND o.id = " + numero;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido");
         }
      }

      //filtro por nº de factura
      if (buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
            query += " AND f.numero = " + numero;
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido");
         }
      }
      if (buscador.getDcDesde() != null) {
         query += " AND o.fecha_recibo >= '" + buscador.getDcDesde() + "'";
      }
      if (buscador.getDcHasta() != null) {
         query += " AND o.fecha_recibo <= '" + buscador.getDcHasta() + "'";
      }
      if (buscador.getCbCaja().getSelectedIndex() > 0) {
         query += " AND o.caja = " + ((Caja) buscador.getCbCaja().getSelectedItem()).getId();
      }
      if (buscador.getCbSucursal().getSelectedIndex() > 0) {
         query += " AND o.sucursal = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId();
      }
      if (buscador.isCheckAnuladaSelected()) {
         query += " AND o.estado = false";
      }
      if (buscador.getCbClieProv().getSelectedIndex() > 0) {
         query += " AND p.id = " + ((Cliente) buscador.getCbClieProv().getSelectedItem()).getId();
      }

      query += " GROUP BY o.id, o.fecha_carga, o.monto, o.usuario, o.caja, o.sucursal, o.fecha_recibo, o.estado"
              + " ORDER BY o.id";
      System.out.println("QUERY: " + query);
      cargarBuscador(query);
   }

   private void cargarBuscador(String query) {
      buscador.dtmRemoveAll();
      DefaultTableModel dtm = buscador.getDtm();
      List<Recibo> l = DAO.getEntityManager().createNativeQuery(query, Recibo.class).getResultList();
      for (Recibo re : l) {
         dtm.addRow(new Object[]{
                    re.toString(),
                    re.getMonto(),
                    UTIL.DATE_FORMAT.format(re.getFechaRecibo()),
                    re.getSucursal(),
                    re.getCaja(),
                    re.getUsuario(),
                    UTIL.DATE_FORMAT.format(re.getFechaCarga()) + " (" + UTIL.TIME_FORMAT.format(re.getFechaCarga() + ")")
                 });
      }
   }

   private void setSelectedRecibo() {
      int rowIndex = buscador.getjTable1().getSelectedRow();
      long id = Long.valueOf(buscador.getjTable1().getValueAt(rowIndex, 0).toString());
      rereSelected = new ReciboJpaController().findRecibo(id);
      if (rereSelected != null) {
         if (jdReRe == null) {
            initRecibos(null, true, false);
         }
         buscador.dispose();
         setDatosRecibo(rereSelected);
         jdReRe.setVisible(true);
      }
   }

   /**
    * Setea la ventana de JDReRe de forma q solo se puedan ver los datos y
    * detalles de la Recibo, imprimir y ANULAR, pero NO MODIFICAR
    * @param recibo
    */
   private void setDatosRecibo(Recibo recibo) {
      bloquearVentana(true);
      String numero = UTIL.AGREGAR_CEROS(String.valueOf(recibo.getId()), 12);
      jdReRe.setTfCuarto(numero.substring(0, 4));
      jdReRe.setTfOcteto(numero.substring(4));

      //por no redundar en DATOOOOOOOOOSS...!!!
      Cliente cliente = new FacturaVentaJpaController().findFacturaVenta(recibo.getDetalleReciboList().get(0).getFacturaVenta().getId()).getCliente();

      jdReRe.setDcFechaReRe(recibo.getFechaRecibo());
      jdReRe.setDcFechaCarga(recibo.getFechaCarga());

      //Uso los toString() para que compare String's..
      //por si el combo está vacio <VACIO> o no eligió ninguno
      //van a tirar error de ClassCastException
      UTIL.setSelectedItem(jdReRe.getCbSucursal(), recibo.getSucursal().toString());
      UTIL.setSelectedItem(jdReRe.getCbCaja(), recibo.getCaja().toString());
      UTIL.setSelectedItem(jdReRe.getCbClienteProveedor(), cliente.toString());
      cargarDetalleReRe(recibo.getDetalleReciboList());
      jdReRe.setTfImporte("");
      jdReRe.setTfPagado("");
      jdReRe.setTfSaldo("");
      jdReRe.setTfTotalPagado(String.valueOf(recibo.getMonto()));
   }

   private void cargarDetalleReRe(List<DetalleRecibo> detalleReciboList) {
      DefaultTableModel dtm = jdReRe.getDtm();
      UTIL.limpiarDtm(dtm);
      for (DetalleRecibo r : detalleReciboList) {
         dtm.addRow(new Object[]{
                    null, // no hace falta cargar facturaID
                    UTIL.AGREGAR_CEROS(String.valueOf(r.getFacturaVenta().getNumero()), 12),
                    r.getObservacion(),
                    r.getMontoEntrega()
                 });
      }
   }

   public void focusGained(FocusEvent e) {
      //..........
   }

   public void focusLost(FocusEvent e) {
      if (buscador != null) {
         if (e.getSource().getClass().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
            if (tf.getName().equalsIgnoreCase("tfocteto")) {
               if (buscador.getTfOcteto().length() > 0) {
                  buscador.setTfOcteto(UTIL.AGREGAR_CEROS(buscador.getTfOcteto(), 8));
               }
            } else if (tf.getName().equalsIgnoreCase("tfFactu8")) {
            }
         }
      }

   }

   private void bloquearVentana(boolean habilitar) {
      jdReRe.getbAnular().setEnabled(habilitar);
//      contenedor.getbImprimir().setEnabled(habilitar);
      // !habilitar
      jdReRe.getBtnADD().setEnabled(!habilitar);
      jdReRe.getBtnDEL().setEnabled(!habilitar);
      jdReRe.getbAceptar().setEnabled(!habilitar);
      jdReRe.getCbCtaCtes().setEnabled(!habilitar);
      jdReRe.getCbCaja().setEnabled(!habilitar);
      jdReRe.getCbSucursal().setEnabled(!habilitar);
      jdReRe.getCbClienteProveedor().setEnabled(!habilitar);
      jdReRe.getDcFechaReRe(!habilitar);
   }

   private void setNextNumeroReRe() {
      Long nextRe = getNextNumeroRecibo();
      String factuString = UTIL.AGREGAR_CEROS(nextRe.toString(), 12);
      jdReRe.setTfCuarto(factuString.substring(0, 4));
      jdReRe.setTfOcteto(factuString.substring(4));
   }

   /**
    * La anulación de una Recibo, resta a <code>CtaCteCliente.entregado</code>
    * los pagos/entregas (parciales/totales) realizados de cada DetalleRecibo y
    * cambia <code>Recibo.estado = false<code>
    * @throws MessageException
    * @throws Exception si Recibo es null, o si ya está anulado
    */
   public void anular(Recibo recibo) throws MessageException, Exception {
      EntityManager em = getEntityManager();
      if (recibo == null) {
         throw new MessageException(CLASS_NAME + " no válido");
      }
      if (!recibo.getEstado()) {
         throw new MessageException("Este " + CLASS_NAME + " ya está anulado");
      }

      List<DetalleRecibo> detalleReciboList = recibo.getDetalleReciboList();
      CtacteCliente ctaCteCliente;
      try {
         em.getTransaction().begin();
         for (DetalleRecibo dr : detalleReciboList) {
            //se resta la entrega ($) que implicaba este detalle con respecto a CADA factura
            ctaCteCliente = new CtacteClienteJpaController().findCtacteClienteByFactura(dr.getFacturaVenta().getId());
            ctaCteCliente.setEntregado(ctaCteCliente.getEntregado() - dr.getMontoEntrega());
            // y si había sido pagada en su totalidad..
            if (ctaCteCliente.getEstado() == Valores.CtaCteEstado.PAGADA.getEstado()) {
               ctaCteCliente.setEstado(Valores.CtaCteEstado.PENDIENTE.getEstado());
            }
            em.merge(ctaCteCliente);
         }
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
      recibo.setEstado(false);
      DAO.doMerge(recibo);
      new CajaMovimientosJpaController().anular(recibo);
   }

   private void imprimirRecibo(Recibo recibo) throws Exception {
      if (recibo == null && recibo.getId() == null) {
         throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
      }

      Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_Recibo_ctacte.jasper", "Recibo");
      r.addParameter("RECIBO_N", recibo.getId());
      r.addCurrent_User();
      r.printReport(true);
   }

   /**
    * Retorna todos los {@link Recibo} que contengan en su {@link DetalleRecibo}
    * a factura.
    * @param factura que deben contenedor los Recibos en su detalle
    * @return una lista de Recibo's
    */
   List<Recibo> findRecibosByFactura(FacturaVenta factura) {
      List<DetalleRecibo> detalleReciboList = new DetalleReciboJpaController().findDetalleReciboEntitiesByFactura(factura);
      List recibosList = new ArrayList(detalleReciboList.size());
      for (DetalleRecibo detalleRecibo : detalleReciboList) {
         recibosList.add(detalleRecibo.getRecibo());
      }
      return recibosList;
   }
}
