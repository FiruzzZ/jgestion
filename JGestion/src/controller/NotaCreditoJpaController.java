package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.MissingReportException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import entity.Caja;
import entity.Cliente;
import entity.NotaCredito;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.DetalleNotaCredito;
import entity.ListaPrecios;
import entity.Sucursal;
import generics.UTIL;
import gui.JDBuscadorReRe;
import gui.JDFacturaVenta;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 *
 * @author Administrador
 */
public class NotaCreditoJpaController {

   private static final String CLASS_NAME = NotaCredito.class.getSimpleName();
   private JDFacturaVenta jdFacturaVenta;
   private FacturaVentaJpaController facturaVentaController;
   private JDBuscadorReRe buscador;
   private NotaCredito EL_OBJECT;
   private boolean MODO_VISTA;
   private static int OBSERVACION_PROPERTY_LIMIT_LENGHT = 200;

   public NotaCreditoJpaController() {
   }

   // <editor-fold defaultstate="collapsed" desc="CRUD">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(NotaCredito notaCredito) throws PreexistingEntityException, Exception {
      Collection<DetalleNotaCredito> toAttach = notaCredito.getDetalleNotaCreditoCollection();
      notaCredito.setDetalleNotaCreditoCollection(new ArrayList<DetalleNotaCredito>());
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.persist(notaCredito);
         em.getTransaction().commit();
         for (DetalleNotaCredito detalleNotaCredito : toAttach) {
            detalleNotaCredito.setNotaCredito(notaCredito);
            DAO.create(detalleNotaCredito);
         }
      } catch (Exception ex) {
         if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<NotaCredito> findNotaCreditoEntities() {
      return findNotaCreditoEntities(true, -1, -1);
   }

   public List<NotaCredito> findNotaCreditoEntities(int maxResults, int firstResult) {
      return findNotaCreditoEntities(false, maxResults, firstResult);
   }

   private List<NotaCredito> findNotaCreditoEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from NotaCredito as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public NotaCredito findNotaCredito(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(NotaCredito.class, id);
      } finally {
         em.close();
      }
   }

   public int getNotaCreditoCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from NotaCredito as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   public void initABMNotaCredito(JFrame owner, boolean modal, boolean setVisible, boolean loadDefaultData) throws MessageException {
      facturaVentaController = new FacturaVentaJpaController();
      facturaVentaController.initFacturaVenta(owner, modal, null, 2, false, loadDefaultData);
      facturaVentaController.getContenedor().getBtnAceptar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               //deshabilita botón para no darle fruta
               facturaVentaController.getContenedor().getBtnAceptar().setEnabled(false);
               if (!MODO_VISTA) {
                  NotaCredito notaCreditoToPersist = setAndPersist();
                  create(notaCreditoToPersist);
                  reporte(notaCreditoToPersist);
                  facturaVentaController.setNumeroFactura(getNextNumeroNotaCredito());
                  facturaVentaController.borrarDetalles();
               } else {
                  if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(jdFacturaVenta, "¿Desea reimprimir la Nota de crédito?", "Re imprimir", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null)) {
                     reporte(EL_OBJECT);
                  }
               }
            } catch (MessageException ex) {
               facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
            } catch (Exception ex) {
               facturaVentaController.getContenedor().showMessage(ex.getMessage(), CLASS_NAME, 2);
               Logger.getLogger(NotaCreditoJpaController.class).log(Level.ERROR, ex, ex);
            } finally {
               facturaVentaController.getContenedor().getBtnAceptar().setEnabled(true);
            }
         }
      });
      if (loadDefaultData) {
         facturaVentaController.setNumeroFactura(getNextNumeroNotaCredito());
      }
      facturaVentaController.getContenedor().setUIToNotaCredito();
      jdFacturaVenta = facturaVentaController.getContenedor();
      jdFacturaVenta.setVisible(setVisible);
   }

   private Long getNextNumeroNotaCredito() {
      EntityManager em = getEntityManager();
      Long nextRemitoNumero = 100000001L;
      try {
         nextRemitoNumero = 1 + (Long) em.createQuery("SELECT MAX(o.numero) FROM " + CLASS_NAME + " o").getSingleResult();
      } catch (NullPointerException ex) {
         // primero!! 0001-00000001
      } finally {
         em.close();
      }
      return nextRemitoNumero;
   }

   private NotaCredito setAndPersist() throws MessageException, Exception {
      if (jdFacturaVenta.getDcFechaFactura() == null) {
         throw new MessageException("Fecha de factura no válida");
      }
      Date fechaNotaCredito = jdFacturaVenta.getDcFechaFactura();
      Sucursal sucursal;
      Cliente cliente = (Cliente) jdFacturaVenta.getCbCliente().getSelectedItem();
      String observacion = null;
      try {
         sucursal = (Sucursal) jdFacturaVenta.getCbSucursal().getSelectedItem();
      } catch (ClassCastException e) {
         throw new MessageException("Debe crear una Sucursal."
                 + "\nMenú: Datos Generales -> Sucursales");
      }
      if (jdFacturaVenta.getTfObservacion().getText().trim().length() > 0) {
         observacion = jdFacturaVenta.getTfObservacion().getText().trim();
         if (observacion.length() >= OBSERVACION_PROPERTY_LIMIT_LENGHT) {
            throw new MessageException("La obsevación no puede tener mas de 250 caracteres (tamaño actual=" + observacion.length() + ").");
         }
      }
      DefaultTableModel dtm = jdFacturaVenta.getDTM();
      if (dtm.getRowCount() < 1) {
         throw new MessageException("No ha agregado ningún item al detalle.");
      } else if (dtm.getRowCount() > FacturaVentaJpaController.LIMITE_DE_ITEMS) {
         throw new MessageException("El límite del detalle son " + FacturaVentaJpaController.LIMITE_DE_ITEMS + " items.");
      }

      NotaCredito newNotaCredito = new NotaCredito();
      newNotaCredito.setNumero(Long.valueOf(jdFacturaVenta.getTfFacturaCuarto() + jdFacturaVenta.getTfFacturaOcteto()));
      newNotaCredito.setFechaNotaCredito(fechaNotaCredito);
      newNotaCredito.setImporte(Double.valueOf(jdFacturaVenta.getTfTotal()));
      newNotaCredito.setGravado(Double.valueOf(jdFacturaVenta.getTfGravado()));
      newNotaCredito.setIva10(Double.valueOf(jdFacturaVenta.getTfTotalIVA105()));
      newNotaCredito.setIva21(Double.valueOf(jdFacturaVenta.getTfTotalIVA21()));
      newNotaCredito.setCliente(cliente);
      newNotaCredito.setSucursal(sucursal);
      newNotaCredito.setUsuario(UsuarioJpaController.getCurrentUser());
      newNotaCredito.setObservacion(observacion);
      newNotaCredito.setDetalleNotaCreditoCollection(new ArrayList<DetalleNotaCredito>(dtm.getRowCount()));
      DetalleNotaCredito detalleVenta;
      ProductoJpaController productoController = new ProductoJpaController();
      for (int i = 0; i < dtm.getRowCount(); i++) {
         detalleVenta = new DetalleNotaCredito();
         detalleVenta.setCantidad(Integer.valueOf(dtm.getValueAt(i, 3).toString()));
         detalleVenta.setPrecioUnitario(Double.valueOf(dtm.getValueAt(i, 4).toString()));
         detalleVenta.setProducto(productoController.findProducto((Integer) dtm.getValueAt(i, 9)));
         newNotaCredito.getDetalleNotaCreditoCollection().add(detalleVenta);
      }
      return newNotaCredito;
   }

   public void initBuscador(JFrame owner, final boolean paraAnular) {
      buscador = new JDBuscadorReRe(owner, "Buscador - Notas de crédito", true, "Cliente", "Nº");
      buscador.hideCaja();
      buscador.hideFactura();
      buscador.hideFormaPago();
      buscador.getbBuscar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               armarQuery();
            } catch (MessageException ex) {
               Logger.getLogger(NotaCreditoJpaController.class.getName()).log(Level.WARN, null, ex);
            }
         }
      });
      UTIL.loadComboBox(buscador.getCbClieProv(), new ClienteJpaController().findClienteEntities(), true);
      UTIL.loadComboBox(buscador.getCbSucursal(), new SucursalJpaController().findSucursalEntities(), true);
      UTIL.getDefaultTableModel(
              buscador.getjTable1(),
              new String[]{"NotaCreditoID", "Nº " + CLASS_NAME, "Cliente", "Importe", "Fecha", "Sucursal", "Usuario", "Fecha (Sistema)"},
              new int[]{1, 60, 150, 50, 50, 80, 50, 70});
      //escondiendo facturaID
      UTIL.hideColumnTable(buscador.getjTable1(), 0);
      buscador.getjTable1().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
               if (buscador.getjTable1().getSelectedRow() > -1) {
                  EL_OBJECT = findNotaCredito(Integer.valueOf(buscador.getDtm().getValueAt(buscador.getjTable1().getSelectedRow(), 0).toString()));
                  try {
                     setDatosEnUI(EL_OBJECT, paraAnular);
                  } catch (MessageException ex) {
                     Logger.getLogger(NotaCreditoJpaController.class.getName()).log(Level.WARN, null, ex);
                  }
               }
            }
         }
      });
      if (paraAnular) {
         buscador.getCheckAnulada().setEnabled(false);
      }
      MODO_VISTA = true;
      buscador.setLocationRelativeTo(owner);
      buscador.setVisible(true);
   }

   private void armarQuery() throws MessageException {
      String query = "SELECT o.* FROM nota_credito o"
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
      if (buscador.getDcDesde() != null) {
         query += " AND o.fecha_nota_credito >= '" + buscador.getDcDesde() + "'";
      }
      if (buscador.getDcHasta() != null) {
         query += " AND o.fecha_nota_credito <= '" + buscador.getDcHasta() + "'";
      }
      if (buscador.getCbSucursal().getSelectedIndex() > 0) {
         query += " AND o.sucursal = " + ((Sucursal) buscador.getCbSucursal().getSelectedItem()).getId();
      }
      if (buscador.getCbClieProv().getSelectedIndex() > 0) {
         query += " AND o.cliente = " + ((Cliente) buscador.getCbClieProv().getSelectedItem()).getId();
      }
      query += " ORDER BY o.id";
      Logger.getLogger(FacturaVentaJpaController.class).log(Level.TRACE, "queryBuscador=" + query);
      cargarDtmBuscador(query);
   }

   private void cargarDtmBuscador(String query) {
      DefaultTableModel dtm = buscador.getDtm();
      UTIL.limpiarDtm(dtm);
      List<NotaCredito> l = DAO.getEntityManager().createNativeQuery(query, NotaCredito.class).getResultList();
      for (NotaCredito o : l) {
         dtm.addRow(new Object[]{
                    o.getId(), // <--- no es visible
                    UTIL.AGREGAR_CEROS(o.getNumero(), 12),
                    o.getCliente(),
                    o.getImporte(),
                    UTIL.DATE_FORMAT.format(o.getFechaNotaCredito()),
                    o.getSucursal(),
                    o.getUsuario(),
                    UTIL.DATE_FORMAT.format(o.getFechaCarga()) + " (" + UTIL.TIME_FORMAT.format(o.getFechaCarga()) + ")"
                 });
      }
   }

   private void setDatosEnUI(final NotaCredito notaCredito, boolean paraAnular) throws MessageException {
      initABMNotaCredito(null, true, false, false);
      // setting info on UI
      String numFactura = UTIL.AGREGAR_CEROS(notaCredito.getNumero(), 12);
      jdFacturaVenta.setTfFacturaCuarto(numFactura.substring(0, 4));
      jdFacturaVenta.setTfFacturaOcteto(numFactura.substring(4));
      jdFacturaVenta.getCbCliente().addItem(notaCredito.getCliente());
      jdFacturaVenta.getCbSucursal().addItem(notaCredito.getSucursal());
      jdFacturaVenta.setDcFechaFactura(notaCredito.getFechaNotaCredito());
      jdFacturaVenta.getTfObservacion().setText(notaCredito.getObservacion());
      Collection<DetalleNotaCredito> lista = notaCredito.getDetalleNotaCreditoCollection();
      DefaultTableModel dtm = jdFacturaVenta.getDTM();
      for (DetalleNotaCredito detalle : lista) {
         System.out.println(detalle.toString());
         double productoConIVA = detalle.getPrecioUnitario() + UTIL.getPorcentaje(detalle.getPrecioUnitario(), detalle.getProducto().getIva().getIva());
         //"IVA","Cód. Producto","Producto","Cantidad","P. Unitario","P. final","Desc","Sub total"
         dtm.addRow(new Object[]{
                    null,
                    detalle.getProducto().getCodigo(),
                    detalle.getProducto(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    UTIL.PRECIO_CON_PUNTO.format(productoConIVA),
                    0.0,
                    UTIL.PRECIO_CON_PUNTO.format((detalle.getCantidad() * productoConIVA))
                 });
      }
      //totales
      jdFacturaVenta.setTfGravado(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getImporte() - (notaCredito.getIva10() + notaCredito.getIva21())));
      jdFacturaVenta.setTfTotalIVA105(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getIva10()));
      jdFacturaVenta.setTfTotalIVA21(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getIva21()));
      jdFacturaVenta.setTfTotal(UTIL.PRECIO_CON_PUNTO.format(notaCredito.getImporte()));
      jdFacturaVenta.modoVista();
      jdFacturaVenta.getCbListaPrecio().setVisible(false); // EXCLU NotaCredito
      if (paraAnular) {
         jdFacturaVenta.hidePanelCambio();
         jdFacturaVenta.getBtnCancelar().setVisible(false);
         jdFacturaVenta.getBtnAceptar().setVisible(false);
         jdFacturaVenta.getBtnFacturar().setVisible(false);
         jdFacturaVenta.getBtnAnular().setVisible(paraAnular);
         jdFacturaVenta.getBtnAnular().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
               anular(notaCredito);
            }
         });
      }
      jdFacturaVenta.setLocationRelativeTo(buscador);
      jdFacturaVenta.setLocationByPlatform(true);
      jdFacturaVenta.setVisible(true);
   }

   private void anular(NotaCredito notaCredito) {
      notaCredito.setAnulada(true);
      NotaCreditoJpaController merge = getEntityManager().merge(this);

   }

   private void reporte(NotaCredito notaCredito) throws MissingReportException, JRException {
      Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_NotaCredito.jasper", "Nota de Crédito");
      r.addCurrent_User();
      r.addParameter("NOTA_CREDITO_ID", notaCredito.getId());
      r.printReport(true);
   }

   double getCreditoDisponible(Cliente cliente) {
      Object[] o = (Object[]) getEntityManager().createQuery(""
              + "SELECT sum(o.importe), sum (o.desacreditado) "
              + "FROM " + CLASS_NAME + " o "
              + "WHERE o.anulada = FALSE AND o.cliente.id =" + cliente.getId()).getSingleResult();
      double creditoDisponible;
      try {
         creditoDisponible = ((Double) o[0]) - ((Double) o[1]);
      } catch (NullPointerException e) {
         //Cliente hasn't got registers on table NotaCredito.. so o[0] and o[1] are null
         creditoDisponible = 0;
      }
      return creditoDisponible;
   }

   /**
    * Find entities of {@link NotaCredito#cliente} = cliente
    * @param cliente
    * @return
    * @throws DatabaseErrorException
    */
   List<NotaCredito> findNotaCreditoFrom(Cliente cliente) {
      return findNotaCreditoFrom(cliente, null);
   }

   List<NotaCredito> findNotaCreditoFrom(Cliente cliente, boolean anulada) {
      return findNotaCreditoFrom(cliente, Boolean.valueOf(anulada));
   }

   private List<NotaCredito> findNotaCreditoFrom(Cliente cliente, Boolean anulada) {
      String filtro = (anulada == null) ? "" : " AND o.anulada= " + anulada;
      return DAO.getEntityManager().createQuery(
              "SELECT o FROM " + CLASS_NAME + " o "
              + "WHERE o.cliente.id= " + cliente.getId()
              + filtro).getResultList();
   }

   /**
    * Resta credito de las {@link NotaCredito} del {@link Cliente}
    * @param cliente Al cual se le van desacreditar
    * @param montoDesacreditar monto que se le va a desacreditar. if is &lt=0 this method will do nothing.
    * @return la diferencia entre (montoDesacreditar - lo que se pudo desacreditar)
    */
   double desacreditar(Cliente cliente, double montoDesacreditar) {
      List<NotaCredito> l = findNotaCreditoAcreditables(cliente);
      double totalPorDesacreditar = montoDesacreditar;
      EntityManager em = getEntityManager();
      try {
         double creditoRestante;
         em.getTransaction().begin();
         for (NotaCredito notaCredito : l) {
            //cuando no hay mas para desacreditar..chau
            if (totalPorDesacreditar <= 0) {
               //también puede entrar directo acá si montoDesacreditar es <= 0
               break;
            }
            creditoRestante = notaCredito.getImporte() - notaCredito.getDesacreditado();
            //si el tope de crédito restante de la Nota es..
            if (creditoRestante <= totalPorDesacreditar) {
               notaCredito.setDesacreditado(notaCredito.getImporte());
               totalPorDesacreditar -= creditoRestante;
            } else {
               notaCredito.setDesacreditado(notaCredito.getDesacreditado() + totalPorDesacreditar);
               totalPorDesacreditar -= totalPorDesacreditar;
            }
            em.merge(notaCredito);
         }
         em.getTransaction().commit();
      } catch (Exception ex) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         Logger.getLogger(NotaCreditoJpaController.class).log(Level.DEBUG, "Error desacreditar()", ex);
      } finally {
         if (em != null) {
            em.close();
         }
      }
      return totalPorDesacreditar;
   }

   List<NotaCredito> findNotaCreditoAcreditables(Cliente cliente) {
      return getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o where o.cliente.id=" + cliente.getId()).getResultList();
   }
}
