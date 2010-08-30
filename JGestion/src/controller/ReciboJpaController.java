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
import java.util.Iterator;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.DetalleRecibo;
import entity.FacturaVenta;
import entity.Sucursal;
import entity.UTIL;
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
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrador
 */
public class ReciboJpaController implements ActionListener, FocusListener {

   private final String CLASS_NAME = "Recibo";
   private final String[] colsName = {"facturaID", "Factura", "Observación", "Entrega"};
   private final int[] colsWidth = {1, 50, 150, 30};
   private JDReRe contenedor;
   private List<FacturaVenta> facturasList;
   private CtacteCliente selectedCtaCte;
   private java.util.Date selectedFechaReRe = null;
   private JDBuscadorReRe buscador;
   private Recibo rereSelected;
   private PanelModeloRecibo panelModeloRecibo;

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

   public void initContenedor(java.awt.Frame frame, boolean modal, boolean setVisible) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.VENTA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      contenedor = new JDReRe(frame, modal);
      contenedor.setLocationRelativeTo(frame);
      //seteos de GUI --->
      contenedor.setTitle(CLASS_NAME + "s");
      contenedor.getLabelReRe().setText("Nº " + CLASS_NAME);
      contenedor.getLabelClienteProveedor().setText("Cliente");
      // <--- seteo de GUI
      try {
         UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
         //escondiendo facturaID
         UTIL.hideColumnTable(contenedor.getjTable1(), 0);
      } catch (Exception ex) {
         Logger.getLogger(FacturaCompraJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
      //set next nº Remesa
      setNextNumeroReRe();
      UTIL.loadComboBox(contenedor.getCbSucursal(),
              new SucursalJpaController().findSucursalEntities(), false);
      UTIL.loadComboBox(contenedor.getCbCaja(),
              new CajaJpaController().findCajasPermitidasByUsuario(UsuarioJpaController.getCurrentUser(), true), false);
      UTIL.loadComboBox(contenedor.getCbClienteProveedor(),
              new ClienteJpaController().findClienteEntities(), true);
      UTIL.loadComboBox(contenedor.getCbCtaCtes(), null, false);

      contenedor.setListener(this);
      contenedor.setVisible(setVisible);
   }

   public void buscadorMouseClicked(MouseEvent e) {
      if (buscador != null) {
         if (e.getClickCount() > 1) {
            setSelectedRecibo();
         }
      }
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         // <editor-fold defaultstate="collapsed" desc="JButton">
         javax.swing.JButton boton = (javax.swing.JButton) e.getSource();
         if (boton.getName().equalsIgnoreCase("aceptar")) {
            try {
               checkConstraints();
               setEntityAndPersist();
               contenedor.showMessage(CLASS_NAME + " cargado..", CLASS_NAME, 1);
               limpiarDetalle();
               resetPanel();

            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         } else if (boton.getName().equalsIgnoreCase("add")) {
            try {
               addEntregaToDetalle();
            } catch (MessageException ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }

         } else if (boton.getName().equalsIgnoreCase("del")) {
            delEntragaFromDetalle();
         } else if (boton.getName().equalsIgnoreCase("Print")) {
            try {
               if (rereSelected != null) {// cuando se imprime un recibo elejido desde el buscador (uno pre existente)
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
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }

         } else if (boton.getName().equalsIgnoreCase("cancelar")) {
            resetPanel();
            limpiarDetalle();
         } else if (boton.getName().equalsIgnoreCase("buscarRERE")) {
            //inicializar buscador de Remesas
            initBuscador(contenedor, true);
            // a ver si eligió algo..
            if (rereSelected != null) {
               setDatosRecibo(rereSelected);
            }

         } else if (boton.getName().equalsIgnoreCase("filtrarReRe")) {
            try {
               armarQuery();
            } catch (MessageException ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
               ex.printStackTrace();
            }
         } else if (boton.getName().equalsIgnoreCase("limpiarBuscadoR")) {
            // está en la GUI limpiarVentana();
         } else if (boton.getName().equalsIgnoreCase("anular")) {
            try {
               anularReRe(rereSelected);
               contenedor.showMessage(CLASS_NAME + " anulada!", CLASS_NAME, 1);
               resetPanel();

            } catch (MessageException ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               buscador.showMessage(ex.getMessage(), CLASS_NAME, 2);
               ex.printStackTrace();
            }
         }
         return;
         // </editor-fold>

      } else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
         // <editor-fold defaultstate="collapsed" desc="JComboBox">
         javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
         if (combo.getName() == null) {
            System.out.println("JComboBox.name = null");
            return;//chau...
         }

         if (combo.getName().equalsIgnoreCase("cbClienteProveedor")) {
            if (combo.getSelectedIndex() > 0) {
               cargarCtaCtes((Cliente) combo.getSelectedItem());
            } else {
               //si no eligió nada.. vacia el combo de cta cte's
               UTIL.loadComboBox(contenedor.getCbCtaCtes(), null, false);
               limpiarDetalle();
            }

         } else if (combo.getName().equalsIgnoreCase("cbCtaCtes")) {
            try {
               setDatosCtaCte();
            } catch (NullPointerException ex) {
               //cuando no eligio una ctacte aún o el cliente/proveedor no tiene ninguna
            }
         }
         // </editor-fold>
      }
   }

   private void checkConstraints() throws MessageException {
      if (contenedor.getDtm().getRowCount() < 1) {
         throw new MessageException("No ha hecho ninguna entrega");
      }

      if (contenedor.getDcFechaReRe() == null) {
         throw new MessageException("Fecha de " + CLASS_NAME + " no válida");
      }

   }

   private void setEntityAndPersist() {
      Recibo recibo = new Recibo();
      recibo.setId(Long.valueOf(contenedor.getTfCuarto() + contenedor.getTfOcteto()));
      recibo.setCaja((Caja) contenedor.getCbCaja().getSelectedItem());
      recibo.setSucursal((Sucursal) contenedor.getCbSucursal().getSelectedItem());
      recibo.setUsuario(UsuarioJpaController.getCurrentUser());
      recibo.setEstado(true);
      recibo.setFechaCarga(new java.util.Date());
      recibo.setFechaRecibo(contenedor.getDcFechaReRe());
      recibo.setHoraCarga(new java.util.Date());
      recibo.setMonto(Double.parseDouble(contenedor.getTfTotalPagado()));
      // 30% faster on ArrayList with initialCapacity :OO
      recibo.setDetalleReciboList(new ArrayList<DetalleRecibo>(contenedor.getDtm().getRowCount()));
      DefaultTableModel dtm = contenedor.getDtm();
      FacturaVentaJpaController fcc = new FacturaVentaJpaController();
      DetalleRecibo dr;
      for (int i = dtm.getRowCount() - 1; i > -1; i--) {
         dr = new DetalleRecibo();
         // ACAAAAAAAAAAAA mov interno no facturiñaaaaaaaa!!!
//            dr.setFacturaVenta(fcc.findFacturaVenta(
//                    Long.parseLong(dtm.getValueAt(i, 0).toString()),
//                    ((Cliente)contenedor.getCbClienteProveedor().getSelectedItem())));
         dr.setFacturaVenta(fcc.findFacturaVenta(Integer.valueOf(dtm.getValueAt(i, 0).toString())));
         dr.setObservacion(dtm.getValueAt(i, 2).toString());
         dr.setMontoEntrega(Double.parseDouble(dtm.getValueAt(i, 3).toString()));
         dr.setRecibo(recibo);
         recibo.getDetalleReciboList().add(dr);

      }
      try {
         create(recibo);
         rereSelected = recibo;
         Iterator<DetalleRecibo> l = recibo.getDetalleReciboList().iterator();
         while (l.hasNext()) {
            dr = l.next();
            //actuliza saldo pagado de cada ctacte
            actualizarMontoEntrega(dr.getFacturaVenta(), dr.getMontoEntrega());
         }
         //registrando pago en CAJA
         new CajaMovimientosJpaController().asentarMovimiento(recibo);

      } catch (NoResultException ex) {
         ex.printStackTrace();
      } catch (PreexistingEntityException ex) {
         Logger.getLogger(RemesaJpaController.class.getName()).log(Level.SEVERE, null, ex);
      } catch (Exception ex) {
         Logger.getLogger(RemesaJpaController.class.getName()).log(Level.SEVERE, null, ex);
      }
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
      UTIL.limpiarDtm(contenedor.getDtm());
      contenedor.setTfImporte("0");
      contenedor.setTfEntrega("");
      contenedor.setTfObservacion("");
      contenedor.setTfSaldo("0");
      contenedor.setTfTotalPagado("0");
      selectedFechaReRe = null;
   }

   private void addEntregaToDetalle() throws MessageException {
      if (contenedor.getDcFechaReRe() == null) {
         throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes");
      }

      if (selectedCtaCte == null) {
         throw new MessageException("No hay Factura seleccionada");
      }

      if (contenedor.getDcFechaReRe().before(selectedCtaCte.getFechaCarga())) {
         throw new MessageException("La fecha de la " + CLASS_NAME + " no puede ser anterior"
                 + "\n a la de la Cta Cte del Proveedor ("
                 + UTIL.DATE_FORMAT.format(selectedCtaCte.getFechaCarga()) + ")");
      }

      // si ya se cargó un detalle de entrega
      // y sigue habiendo al menos UN detalle agregado (dtm no vacia)
      // ctrla que la fecha de ReRe siga siendo la misma
      if ((selectedFechaReRe != null) && (contenedor.getDtm().getRowCount() > 0)
              && (!UTIL.DATE_FORMAT.format(selectedFechaReRe).equals(UTIL.DATE_FORMAT.format(contenedor.getDcFechaReRe())))) {
         throw new MessageException("La fecha de " + CLASS_NAME + " a sido cambiada"
                 + "\nAnterior: " + UTIL.DATE_FORMAT.format(selectedFechaReRe)
                 + "\nActual: " + UTIL.DATE_FORMAT.format(contenedor.getDcFechaReRe()));
      } else {
         selectedFechaReRe = contenedor.getDcFechaReRe();
      }
      FacturaVenta fc = selectedCtaCte.getFactura();
      double entrega;
      String observacion = contenedor.getTfObservacion();
      try {
         entrega = Double.parseDouble(contenedor.getTfEntrega());
         if (entrega <= 0) {
            throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
         }

//         if(entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()) )
         if (entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
            throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante");
         }

      } catch (NumberFormatException e) {
         throw new MessageException("Monto de entrega no válido");
      }
      if (observacion.length() > 200) {
         throw new MessageException("La Observación no puede superar los 200 caracteres");
      }

      DefaultTableModel dtm = contenedor.getDtm();
      dtm.addRow(new Object[]{
                 fc.getId(),
                 //por si es un MovimientiInterno y no un número de FacturaVenta
                 (fc.getNumero() != 0) ? UTIL.AGREGAR_CEROS(String.valueOf(fc.getNumero()), 12) : "I" + String.valueOf(fc.getMovimientoInterno()),
                 observacion,
                 entrega
              });
      double totalEntregado = Double.valueOf(contenedor.getTfTotalPagado());
      contenedor.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(totalEntregado + entrega));

   }

   /**
    * Borra la fila seleccionada, del DetalleRecibo
    */
   private void delEntragaFromDetalle() {
      int selectedRow = contenedor.getjTable1().getSelectedRow();
      if (selectedRow > -1) {
         double entrega = Double.valueOf(contenedor.getDtm().getValueAt(selectedRow, 3).toString());
         double totalEntregado = Double.valueOf(contenedor.getTfTotalPagado());
         contenedor.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(totalEntregado - entrega));
         contenedor.getDtm().removeRow(selectedRow);
      }
   }

   private void initBuscador(javax.swing.JDialog dialog, boolean modal) {
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

   public void initBuscador(javax.swing.JFrame frame, boolean modal) {
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
              new int[]{50, 30, 40, 50, 50, 50, 70}
              );
      buscador.getjTable1().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseClicked(MouseEvent e) {
            buscadorMouseClicked(e);
         }
      });
      buscador.setListeners(this);
      buscador.setVisible(true);
   }

   private void cargarCtaCtes(Cliente cliente) {
      limpiarDetalle();
      List<CtacteCliente> ctacteClientePendientesList = new CtacteClienteJpaController().findCtacteClienteByCliente(cliente.getId(), Valores.PENDIENTE);
      UTIL.loadComboBox(contenedor.getCbCtaCtes(), ctacteClientePendientesList, false);

   }

   /**
    * Carga los datos de la CtaCteCliente (FacturaVenta) elegida, sobre el cual se va crear el Recibo.
    */
   private void setDatosCtaCte() {
      try {
         selectedCtaCte = (CtacteCliente) contenedor.getCbCtaCtes().getSelectedItem();
         contenedor.setTfImporte(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte()));
         contenedor.setTfPagado(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getEntregado()));
         contenedor.setTfSaldo(UTIL.PRECIO_CON_PUNTO.format(selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
      } catch (ClassCastException ex) {
         selectedCtaCte = null;
         System.out.println("No se pudo caster a CtaCteProveedor -> " + contenedor.getCbCtaCtes().getSelectedItem());
      }

   }

   /**
    * Resetea la ventana;
    * - pone la fecha actual
    * - clienteProveedor.index(0)
    * - setea el NextNumeroReRe
    * - rereSelected = null;
    */
   private void resetPanel() {
      contenedor.setDcFechaReRe(new java.util.Date());
      contenedor.getCbClienteProveedor().setSelectedIndex(0);
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

      query += " GROUP BY o.id, o.fecha_carga, o.hora_carga, o.monto, o.usuario, o.caja, o.sucursal, o.fecha_recibo, o.estado"
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
                    UTIL.DATE_FORMAT.format(re.getFechaCarga()) + " - " + UTIL.TIME_FORMAT.format(re.getHoraCarga())
                 });
      }
   }

   private void setSelectedRecibo() {
      int rowIndex = buscador.getjTable1().getSelectedRow();
      long remesaID = Long.valueOf(buscador.getjTable1().getValueAt(rowIndex, 0).toString());
      rereSelected = new ReciboJpaController().findRecibo(remesaID);
      if (rereSelected != null) {
         if (contenedor == null) {
            initContenedor(null, true, false);
         } 
         buscador.dispose();
         setDatosRecibo(rereSelected);
         contenedor.setVisible(true);
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
      contenedor.setTfCuarto(numero.substring(0, 4));
      contenedor.setTfOcteto(numero.substring(4));

      //por no redundar en DATOOOOOOOOOSS...!!!
      Cliente cliente = new FacturaVentaJpaController().findFacturaVenta(recibo.getDetalleReciboList().get(0).getFacturaVenta().getId()).getCliente();

      contenedor.setDcFechaReRe(recibo.getFechaRecibo());
      contenedor.setDcFechaCarga(recibo.getFechaCarga());

      //Uso los toString() para que compare String's..
      //por si el combo está vacio <VACIO> o no eligió ninguno
      //van a tirar error de ClassCastException
      UTIL.setSelectedItem(contenedor.getCbSucursal(), recibo.getSucursal().toString());
      UTIL.setSelectedItem(contenedor.getCbCaja(), recibo.getCaja().toString());
      UTIL.setSelectedItem(contenedor.getCbClienteProveedor(), cliente.toString());
      cargarDetalleReRe(recibo.getDetalleReciboList());
      contenedor.setTfImporte("");
      contenedor.setTfPagado("");
      contenedor.setTfSaldo("");
      contenedor.setTfTotalPagado(String.valueOf(recibo.getMonto()));
   }

   private void cargarDetalleReRe(List<DetalleRecibo> detalleReciboList) {
      DefaultTableModel dtm = contenedor.getDtm();
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
      contenedor.getbAnular().setEnabled(habilitar);
//      contenedor.getbImprimir().setEnabled(habilitar);
      // !habilitar
      contenedor.getBtnADD().setEnabled(!habilitar);
      contenedor.getBtnDEL().setEnabled(!habilitar);
      contenedor.getbAceptar().setEnabled(!habilitar);
      contenedor.getCbCtaCtes().setEnabled(!habilitar);
      contenedor.getCbCaja().setEnabled(!habilitar);
      contenedor.getCbSucursal().setEnabled(!habilitar);
      contenedor.getCbClienteProveedor().setEnabled(!habilitar);
      contenedor.getDcFechaReRe(!habilitar);
   }

   private void setNextNumeroReRe() {
      Long nextRe = getNextNumeroRecibo();
      String factuString = UTIL.AGREGAR_CEROS(nextRe.toString(), 12);
      contenedor.setTfCuarto(factuString.substring(0, 4));
      contenedor.setTfOcteto(factuString.substring(4));
   }

   /**
    * La anulación de una Recibo, resta a <code>CtaCteCliente.entregado</code>
    * los pagos/entregas (parciales/totales) realizados de cada DetalleRecibo y
    * cambia <code>Recibo.estado = false<code>
    * @throws MessageException
    * @throws Exception si Recibo es null, o si ya está anulado
    */
   public void anularReRe(Recibo recibo) throws MessageException, Exception {
      EntityManager em = getEntityManager();
      if (recibo == null) {
         throw new MessageException(CLASS_NAME + " is NULL");
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
      r.printReport();
   }

   List<Recibo> findRecibosByFactura(FacturaVenta factura) {
      List<DetalleRecibo> detalleReciboList = new DetalleReciboJpaController().findDetalleReciboEntitiesByFactura(factura);
      List recibosList = new ArrayList(detalleReciboList.size());
      for (DetalleRecibo detalleRecibo : detalleReciboList) {
         recibosList.add(detalleRecibo.getRecibo());
      }
      return recibosList;
   }
}
