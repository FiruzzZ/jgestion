package controller;

import controller.exceptions.*;
import entity.Caja;
import entity.Cliente;
import entity.CtacteCliente;
import entity.NotaCredito;
import entity.Recibo;
import java.text.ParseException;
import java.util.Iterator;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import entity.DetalleRecibo;
import entity.FacturaVenta;
import entity.Sucursal;
import utilities.general.UTIL;
import gui.JDBuscadorReRe;
import gui.JDReRe;
import gui.generics.JDialogTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class ReciboJpaController implements ActionListener, FocusListener {

   private static final String CLASS_NAME = Recibo.class.getSimpleName();
   private static final String[] COLUMN_NAMES = {"facturaID", "Factura", "Observación", "Entrega", "Acredidato"};
   private static final int[] COLUMN_WIDTH = {1, 50, 150, 30, 10};
   private static final Class[] COLUMN_CLASS = {Object.class, Object.class, String.class, Double.class, Boolean.class};
   private JDReRe jdReRe;
   private CtacteCliente selectedCtaCte;
   private Date selectedFechaReRe = null;
   private JDBuscadorReRe buscador;
   private Recibo rereSelected;

   public ReciboJpaController() {
   }

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
         em.persist(recibo);
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
         UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>
      jdReRe = new JDReRe(frame, modal);
      jdReRe.setUIForRecibos();
      UTIL.getDefaultTableModel(jdReRe.getjTable1(), COLUMN_NAMES, COLUMN_WIDTH);
      UTIL.hideColumnsTable(jdReRe.getjTable1(), new int[]{0, 4});
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
               setAndPersist();
               jdReRe.showMessage(CLASS_NAME + " cargado..", CLASS_NAME, 1);
               limpiarDetalle();
               resetPanel();

            } catch (MessageException ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(ReciboJpaController.class).log(Level.ERROR, null, ex);
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
               Logger.getLogger(ReciboJpaController.class).log(org.apache.log4j.Level.ERROR, null, ex);
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
                  setAndPersist();
                  imprimirRecibo(rereSelected);
                  limpiarDetalle();
                  resetPanel();
               }
            } catch (MessageException ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               jdReRe.showMessage(ex.getMessage(), CLASS_NAME, 0);
               Logger.getLogger(ReciboJpaController.class).log(org.apache.log4j.Level.ERROR, null, ex);
            }
         }
      });
      jdReRe.getCbClienteProveedor().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (jdReRe.getCbClienteProveedor().getSelectedIndex() > 0) {
               Cliente cliente = (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem();
               cargarCtaCtes(cliente);
               double credito = new NotaCreditoJpaController().getCreditoDisponible(cliente);
               jdReRe.getTfCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(credito));
               jdReRe.getTfRestanteCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(credito));
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
      jdReRe.getBtnDetalleCreditoDebito().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               displayDetalleCredito((Cliente) jdReRe.getCbClienteProveedor().getSelectedItem());
            } catch (ClassCastException ex) {
               JOptionPane.showMessageDialog(jdReRe, "Debe elegir un Cliente", null, JOptionPane.WARNING_MESSAGE);
            }
         }
      });
      jdReRe.setListener(this);
      jdReRe.setLocationRelativeTo(frame);
      jdReRe.setVisible(setVisible);
   }

   private void displayDetalleCredito(Cliente cliente) {
      JTable tabla = UTIL.getDefaultTableModel(null,
              new String[]{"Nº Nota crédito", "Fecha", "Importe", "Desacreditado", "Total Acum."},
              new int[]{50, 50, 50, 50, 100},
              new Class[]{Object.class, Object.class, Double.class, Double.class, Double.class});
      List<NotaCredito> lista = new NotaCreditoJpaController().findNotaCreditoFrom(cliente, false);
      DefaultTableModel dtm = (DefaultTableModel) tabla.getModel();
      double acumulativo = 0.0;
      for (NotaCredito notaCredito : lista) {
         acumulativo += (notaCredito.getImporte() - notaCredito.getDesacreditado());
         dtm.addRow(new Object[]{
                    UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12),
                    UTIL.DATE_FORMAT.format(notaCredito.getFechaNotaCredito()),
                    notaCredito.getImporte(),
                    notaCredito.getDesacreditado(),
                    acumulativo,});
      }
      JDialogTable jd = new JDialogTable(jdReRe, "Detalle de crédito: " + cliente.getNombre(), true, dtm);
      jd.setSize(600, 400);
      jd.setVisible(true);
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

   private void setAndPersist() throws Exception {
      Recibo recibo = new Recibo();
      recibo.setId(Long.valueOf(jdReRe.getTfCuarto() + jdReRe.getTfOcteto()));
      recibo.setCaja((Caja) jdReRe.getCbCaja().getSelectedItem());
      recibo.setSucursal((Sucursal) jdReRe.getCbSucursal().getSelectedItem());
      recibo.setUsuario(UsuarioJpaController.getCurrentUser());
      recibo.setEstado(true);
      recibo.setFechaRecibo(jdReRe.getDcFechaReRe());
      // 30% faster on ArrayList with initialCapacity :OO
      recibo.setDetalleReciboList(new ArrayList<DetalleRecibo>(jdReRe.getDtm().getRowCount()));
      DefaultTableModel dtm = jdReRe.getDtm();
      FacturaVentaJpaController fcc = new FacturaVentaJpaController();
      DetalleRecibo dr;
      double montoParaDesacreditar = 0;
      for (int i = dtm.getRowCount() - 1; i > -1; i--) {
         dr = new DetalleRecibo();
         dr.setFacturaVenta(fcc.findFacturaVenta(Integer.valueOf(dtm.getValueAt(i, 0).toString())));
         dr.setObservacion(dtm.getValueAt(i, 2) != null ? dtm.getValueAt(i, 2).toString() : null);
         dr.setMontoEntrega(Double.parseDouble(dtm.getValueAt(i, 3).toString()));
         dr.setAcreditado((Boolean) dtm.getValueAt(i, 4));
         dr.setRecibo(recibo);
         recibo.getDetalleReciboList().add(dr);
         if (dr.isAcreditado()) {
            montoParaDesacreditar += dr.getMontoEntrega();
         }
      }
      recibo.setMonto(Double.parseDouble(jdReRe.getTfTotalPagado()));
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
      if (montoParaDesacreditar > 0) {
         new NotaCreditoJpaController().desacreditar(recibo, (Cliente) jdReRe.getCbClienteProveedor().getSelectedItem(), montoParaDesacreditar);
      }
   }

   private void actualizarMontoEntrega(FacturaVenta factu, double monto) {
      CtacteCliente ctacte = new CtacteClienteJpaController().findCtacteClienteByFactura(factu.getId());
      Logger.getLogger(ReciboJpaController.class).debug("updatingMontoEntrega: CtaCte:" + ctacte.getId() + " -> Importe: " + ctacte.getImporte() + " Entregado:" + ctacte.getEntregado() + " + " + monto);

      ctacte.setEntregado(ctacte.getEntregado() + monto);
      if (ctacte.getImporte() == ctacte.getEntregado()) {
         ctacte.setEstado(Valores.CtaCteEstado.PAGADA.getEstado());
         Logger.getLogger(ReciboJpaController.class).debug("CtaCte Nº:" + ctacte.getId() + " PAGADA");
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
         throw new MessageException("Debe especificar una fecha de " + CLASS_NAME + " antes de empezar a cargar Facturas.");
      }
      if (selectedCtaCte == null) {
         throw new MessageException("No hay Factura seleccionada.");
      }
      try {
         //hay que quitar las HH:MM:ss:mmmm de las fechas para hacer las comparaciones
         if (UTIL.DATE_FORMAT.parse(UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe())).before(UTIL.DATE_FORMAT.parse(UTIL.DATE_FORMAT.format(selectedCtaCte.getFactura().getFechaVenta())))) {
            throw new MessageException("La fecha de " + CLASS_NAME + " no puede ser anterior"
                    + "\n a la fecha de Facturación ("
                    + UTIL.DATE_FORMAT.format(selectedCtaCte.getFactura().getFechaVenta()) + ")");
         }
      } catch (ParseException ex) {
         //ignored..
      }

      // si hay cargado al menos un detalle de entrega
      // ctrla que la fecha de ReRe siga siendo la misma
      if ((selectedFechaReRe != null) && (jdReRe.getDtm().getRowCount() > 0)
              && (!UTIL.DATE_FORMAT.format(selectedFechaReRe).equals(UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe())))) {
         throw new MessageException("La fecha de " + CLASS_NAME + " a sido cambiada"
                 + "\nAnterior: " + UTIL.DATE_FORMAT.format(selectedFechaReRe)
                 + "\nActual: " + UTIL.DATE_FORMAT.format(jdReRe.getDcFechaReRe()));
      } else {
         selectedFechaReRe = jdReRe.getDcFechaReRe();
      }
      double entrega;
      String observacion = jdReRe.getTfObservacion().length() > 0 ? jdReRe.getTfObservacion() : null;
      try {
         entrega = Double.parseDouble(jdReRe.getTfEntrega());
         if (entrega <= 0) {
            throw new MessageException("Monto de entrega no válido (Debe ser mayor a 0)");
         }
         if (entrega > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
            throw new MessageException("Monto de entrega no puede ser mayor al Saldo restante");
         }
      } catch (NumberFormatException ex) {
         throw new MessageException("Monto de entrega no válido");
      }
      if (observacion != null && observacion.length() > 200) {
         throw new MessageException("La Observación no puede superar los 200 caracteres (no es una novela)");
      }
      FacturaVenta facturaToAddToDetail = selectedCtaCte.getFactura();
      boolean acreditado = jdReRe.getCheckAcreditarEntrega().isSelected();

      //check que no se inserte una entrega (acreditada o no) de la misma factura
      double entregaParcial = 0;
      for (int i = 0; i < jdReRe.getDtm().getRowCount(); i++) {
         if (facturaToAddToDetail.getId() == (Integer) jdReRe.getDtm().getValueAt(i, 0)) {
            entregaParcial += (Double) jdReRe.getDtm().getValueAt(i, 3);
            if (acreditado == (Boolean) jdReRe.getDtm().getValueAt(i, 4)) {
               throw new MessageException("El detalle ya contiene una entrega "
                       + (acreditado ? " (ACREDITADA)" : "") + " de esta factura.");
            }
         }
      }
      if ((entrega + entregaParcial) > (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado())) {
         throw new MessageException("El monto de esta entrega $" + entrega + " + $" + entregaParcial
                 + "\nsuperan la deuda de la Factura $" + (selectedCtaCte.getImporte() - selectedCtaCte.getEntregado()));
      }
      double restante;
      if (acreditado) {
         restante = Double.parseDouble(jdReRe.getTfRestanteCreditoDebito().getText());
         if (restante < entrega) {
            throw new MessageException("El crédito no es suficiente para cubrir esta entrega");
         }
      }

      jdReRe.getDtm().addRow(new Object[]{
                 facturaToAddToDetail.getId(),
                 //por si es un MovimientiInterno y no un número de FacturaVenta
                 //se concat al final ** (doble asterisco) cuando es acreditada la entrega
                 ((facturaToAddToDetail.getNumero() != 0)
                 ? UTIL.AGREGAR_CEROS(String.valueOf(facturaToAddToDetail.getNumero()), 12)
                 : "I" + String.valueOf(facturaToAddToDetail.getMovimientoInterno())) + (acreditado ? "**" : ""),
                 observacion,
                 entrega,
                 acreditado
              });
      double total;
      if (acreditado) {
         //actualiza total acreditado
         total = Double.valueOf(jdReRe.getTfPorCreditoDebito().getText());
         jdReRe.getTfPorCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(total + entrega));

         restante = Double.parseDouble(jdReRe.getTfCreditoDebito().getText()) - (total + entrega);
         jdReRe.getTfRestanteCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(restante));
      } else {
         //actualiza total "efectivo"
         total = Double.valueOf(jdReRe.getTfTotalPagado());
         jdReRe.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(total + entrega));
      }
      updateTotalReRe();
   }

   /**
    * Borra la fila seleccionada, del DetalleRecibo
    */
   private void delEntragaFromDetalle() {
      int selectedRow = jdReRe.getjTable1().getSelectedRow();
      if (selectedRow > -1) {
         double entrega = (Double) jdReRe.getDtm().getValueAt(selectedRow, 3);
         double entregado;
         //si es acreditado o no
         if ((Boolean) jdReRe.getDtm().getValueAt(selectedRow, 4)) {
            entregado = Double.parseDouble(jdReRe.getTfRestanteCreditoDebito().getText());
            jdReRe.getTfRestanteCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(entregado + entrega));

            //reutilizando variable.. no te asustes!
            entregado = Double.valueOf(jdReRe.getTfPorCreditoDebito().getText());
            jdReRe.getTfPorCreditoDebito().setText(UTIL.PRECIO_CON_PUNTO.format(entregado - entrega));
         } else {
            entregado = Double.valueOf(jdReRe.getTfTotalPagado());
            jdReRe.setTfTotalPagado(UTIL.PRECIO_CON_PUNTO.format(entregado - entrega));
         }
         jdReRe.getDtm().removeRow(selectedRow);
         updateTotalReRe();
      }
   }

   public void initBuscador(JFrame frame, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
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
      StringBuilder query = new StringBuilder("SELECT o.* FROM recibo o, cliente p , caja c, detalle_recibo dr, factura_venta f, usuario u, sucursal s  "
              + " WHERE o.id = dr.recibo "
              + "   AND o.caja = c.id "
              + "   AND o.usuario = u.id "
              + "   AND o.sucursal = s.id "
              + "   AND f.id = dr.factura_venta "
              + "   AND p.id = f.cliente"
              + "   ");

      long numero;
      //filtro por nº de ReRe
      if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfCuarto() + buscador.getTfOcteto());
            query.append(" AND o.id = ").append(numero);
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido");
         }
      }

      //filtro por nº de factura
      if (buscador.getTfFactu4().length() > 0 && buscador.getTfFactu8().length() > 0) {
         try {
            numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
            query.append(" AND f.numero = ").append(numero);
         } catch (NumberFormatException ex) {
            throw new MessageException("Número de " + CLASS_NAME + " no válido");
         }
      }
      if (buscador.getDcDesde() != null) {
         query.append(" AND o.fecha_recibo >= '").append(buscador.getDcDesde()).append("'");
      }
      if (buscador.getDcHasta() != null) {
         query.append(" AND o.fecha_recibo <= '").append(buscador.getDcHasta()).append("'");
      }
      if (buscador.getCbCaja().getSelectedIndex() > 0) {
         query.append(" AND o.caja = ").append(((Caja) buscador.getCbCaja().getSelectedItem()).getId());
      }
      if (buscador.getCbSucursal().getSelectedIndex() > 0) {
         query.append(" AND o.sucursal = ").append(((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId());
      }
      if (buscador.isCheckAnuladaSelected()) {
         query.append(" AND o.estado = false");
      }
      if (buscador.getCbClieProv().getSelectedIndex() > 0) {
         query.append(" AND p.id = ").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
      }

      query.append(" GROUP BY o.id, o.fecha_carga, o.monto, o.usuario, o.caja, o.sucursal, o.fecha_recibo, o.estado"
              + " ORDER BY o.id");
      System.out.println("QUERY: " + query);
      cargarBuscador(query.toString());
   }

   private void cargarBuscador(String query) {
      buscador.dtmRemoveAll();
      DefaultTableModel dtm = buscador.getDtm();
      List<Recibo> l = DAO.getEntityManager().createNativeQuery(query, Recibo.class).setHint("toplink.refresh", true).getResultList();
      for (Recibo o : l) {
         dtm.addRow(new Object[]{
                    o.toString(),
                    o.getMonto(),
                    UTIL.DATE_FORMAT.format(o.getFechaRecibo()),
                    o.getSucursal(),
                    o.getCaja(),
                    o.getUsuario(),
                    UTIL.TIMESTAMP_FORMAT.format(o.getFechaCarga())
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
                    null, //no hace falta cargar facturaID
                    UTIL.AGREGAR_CEROS(String.valueOf(r.getFacturaVenta().getNumero()), 12) + (r.isAcreditado() ? "**" : ""),
                    r.getObservacion(),
                    r.getMontoEntrega(),
                    null //also needless
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
         if (!recibosList.contains(detalleRecibo.getRecibo())) {
            recibosList.add(detalleRecibo.getRecibo());
         }
      }
      return recibosList;
   }

   private void updateTotalReRe() {
      jdReRe.getTfTOTAL().setText(UTIL.PRECIO_CON_PUNTO.format(
              Double.valueOf(jdReRe.getTfPorCreditoDebito().getText())
              + Double.valueOf(jdReRe.getTfTotalPagado())));
   }
}
