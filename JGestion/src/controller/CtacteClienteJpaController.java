package controller;

import controller.exceptions.*;
import entity.Cliente;
import entity.CtacteCliente;
import entity.DetalleRecibo;
import entity.FacturaVenta;
import entity.Recibo;
import entity.UTIL;
import gui.JDResumenCtaCtes;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrador
 */
public class CtacteClienteJpaController implements ActionListener {

   public static final String CLASS_NAME = CtacteCliente.class.getSimpleName();
   private JDResumenCtaCtes resumenCtaCtes;
   private double totalDebe;  //<----
   private double totalHaber; //<----

   // <editor-fold defaultstate="collapsed" desc="CRUD...">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(CtacteCliente ctacteCliente) throws PreexistingEntityException, Exception {

      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.persist(ctacteCliente);
         em.getTransaction().commit();
      } catch (Exception ex) {
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(CtacteCliente ctacteCliente) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         ctacteCliente = em.merge(ctacteCliente);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = ctacteCliente.getId();
            if (findCtacteCliente(id) == null) {
               throw new NonexistentEntityException("The ctacteCliente with id " + id + " no longer exists.");
            }
         }
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void destroy(Integer id) throws NonexistentEntityException {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         CtacteCliente ctacteCliente;
         try {
            ctacteCliente = em.getReference(CtacteCliente.class, id);
            ctacteCliente.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The ctacteCliente with id " + id + " no longer exists.", enfe);
         }
         em.remove(ctacteCliente);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<CtacteCliente> findCtacteClienteEntities() {
      return findCtacteClienteEntities(true, -1, -1);
   }

   public List<CtacteCliente> findCtacteClienteEntities(int maxResults, int firstResult) {
      return findCtacteClienteEntities(false, maxResults, firstResult);
   }

   private List<CtacteCliente> findCtacteClienteEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from CtacteCliente as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public CtacteCliente findCtacteCliente(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(CtacteCliente.class, id);
      } finally {
         em.close();
      }
   }

   public int getCtacteClienteCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from CtacteCliente as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   void nuevaCtaCte(FacturaVenta facturaVenta) throws Exception {
      System.out.println("Nueva CtaCte Cliente " + facturaVenta);
      CtacteCliente ccp = new CtacteCliente();
      ccp.setDias((short) facturaVenta.getDiasCtaCte());
      ccp.setEntregado(0.0); //monto $$
      ccp.setEstado((Valores.CtaCteEstado.PENDIENTE.getEstado()));
      ccp.setFactura(facturaVenta);
      ccp.setFechaCarga(facturaVenta.getFechaalta());
      ccp.setHoraCarga(new java.util.Date());
      ccp.setImporte(facturaVenta.getImporte());
      create(ccp);
   }

   List<CtacteCliente> findCtacteClienteFromCliente(Integer idCliente, int estadoCtaCte) {
      EntityManager em = getEntityManager();
      em.getTransaction().begin();
      List<CtacteCliente> listaCtaCteCliente = null;
      try {
         listaCtaCteCliente = em.createNativeQuery(
                 "SELECT o.* FROM ctacte_cliente o, factura_venta f, cliente p"
                 + " WHERE p.id = f.cliente AND f.id = o.factura "
                 + " AND o.estado = " + estadoCtaCte + " AND p.id =" + idCliente,
                 CtacteCliente.class).getResultList();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      return listaCtaCteCliente;
   }

   List<CtacteCliente> findCtacteClienteFromCliente(Integer idCliente) {
      EntityManager em = getEntityManager();
      em.getTransaction().begin();
      List<CtacteCliente> listaCtaCteCliente = null;
      try {
         listaCtaCteCliente = em.createNativeQuery(
                 "SELECT o.* FROM ctacte_cliente o, factura_venta f, cliente p"
                 + " WHERE p.id = f.cliente AND f.id = o.factura "
                 + " AND p.id =" + idCliente,
                 CtacteCliente.class).getResultList();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      return listaCtaCteCliente;
   }

   CtacteCliente findCtacteClienteByFactura(Integer id) {
      return (CtacteCliente) DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o "
              + " where o.factura.id = " + id).getSingleResult();
   }

   List<CtacteCliente> findCtacteClienteByCliente(Integer clienteID, short estadoCtaCte) {
      System.out.println("findCtaCteClienteByCliente (" + clienteID + ", " + estadoCtaCte + ")");
      EntityManager em = getEntityManager();
      em.getTransaction().begin();
      List<CtacteCliente> listaCtaCteCliente = null;
      try {
         listaCtaCteCliente = em.createNativeQuery(
                 "SELECT o.* FROM ctacte_cliente o, factura_venta f, cliente c"
                 + " WHERE c.id = f.cliente AND f.id = o.factura "
                 + " AND o.estado = " + estadoCtaCte + " AND c.id =" + clienteID
                 + " ORDER BY o.id",
                 CtacteCliente.class).getResultList();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      return listaCtaCteCliente;
   }

   private void jTableResumenMouseReleased(MouseEvent e) {
   }

   public void initResumenCtaCte(javax.swing.JFrame frame, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.TESORERIA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
         return;
      }// </editor-fold>

      resumenCtaCtes = new JDResumenCtaCtes(frame, modal, true);
      resumenCtaCtes.getjTableResumen().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseReleased(MouseEvent e) {
            Integer selectedRow = resumenCtaCtes.getjTableResumen().getSelectedRow();
            if (selectedRow > 0) {
               //selecciona una factura (CtaCteCliente)
               cargarComboBoxRecibosDeCtaCte((CtacteCliente) DAO.getEntityManager()
                       .find(CtacteCliente.class,Integer.valueOf((resumenCtaCtes.getDtmResumen().getValueAt(selectedRow, 0)).toString())));
            }
         }
      });
      resumenCtaCtes.getbBuscar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               armarQuery(false);
            } catch (MessageException ex) {
               resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
            } catch (Exception ex) {
               resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
               Logger.getLogger(CtacteClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      });
      resumenCtaCtes.getbImprimir().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               armarQuery(true);
            } catch (MessageException ex) {
               resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
            } catch (Exception ex) {
               resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
               Logger.getLogger(CtacteClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      });
      resumenCtaCtes.getCbReRes().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (resumenCtaCtes.getCbReRes().isFocusOwner()) {
               setDatosReciboSelected();
            }
         }
      });
      UTIL.loadComboBox(resumenCtaCtes.getCbClieProv(), new ClienteJpaController().findClienteEntities(), false);
      UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), null, true);
      UTIL.getDefaultTableModel(
              resumenCtaCtes.getjTableResumen(),
              new String[]{"ctacteClienteID", "Detalle", "Fecha", "Vencimiento", "Debe", "Haber", "Saldo", "Acumulativo", "estadoCCC"},
              new int[]{1, 60, 50, 50, 30, 30, 30, 50, 1});
      UTIL.hideColumnsTable(resumenCtaCtes.getjTableResumen(), new int[]{0, 8});
      UTIL.getDefaultTableModel(
              resumenCtaCtes.getjTableDetalle(),
              new String[]{"Nº Factura", "Observación", "Monton"},
              new int[]{60, 100, 50});
      resumenCtaCtes.setListener(this);
//      resumenCtaCtes.setLocationByPlatform(true);
      resumenCtaCtes.setVisible(true);
   }

   private double getSaldoAcumulado() {
      double saldo = 0.0;
      javax.swing.table.DefaultTableModel dtm = resumenCtaCtes.getDtmResumen();
      for (int i = dtm.getRowCount() - 1; i > -1; i--) {
         saldo += Double.parseDouble(dtm.getValueAt(i, 6).toString());
      }
      return saldo;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      //evoluciónnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
   }

   private void armarQuery(boolean imprimirResumen) throws MessageException, Exception {
      totalDebe = 0.0;
      totalHaber = 0.0;

      String query = "SELECT ccc.* "
              + " FROM ctacte_cliente ccc, cliente c, factura_venta fv "
              + " WHERE ccc.factura = fv.id "
              + " AND fv.cliente = c.id ";
      try {
         query += "AND c.id ="
                 + ((Cliente) resumenCtaCtes.getCbClieProv().getSelectedItem()).getId();
      } catch (ClassCastException ex) {
         throw new MessageException("Cliente no válido");
      }

      if (resumenCtaCtes.getDcDesde() != null) {
         //calcula los totales del DEBE / HABER / SALDO ACUMULATIVO de la CtaCte
         // anterior a la fecha desde la cual se eligió en el buscador
         setResumenHistorial(query + "AND ccc.fecha_carga < '" + resumenCtaCtes.getDcDesde() + "'");

         query += "AND ccc.fecha_carga >= '" + resumenCtaCtes.getDcDesde() + "'";
      }

      query += " ORDER BY ccc.id";
      cargarDtmResumen(query);
      if (imprimirResumen) {
         imprimirResumenCCC(((Cliente) resumenCtaCtes.getCbClieProv().getSelectedItem()), resumenCtaCtes.getDcDesde() != null ? " AND ccc.fecha_carga >= '" + UTIL.DATE_FORMAT.format(resumenCtaCtes.getDcDesde()) + "'" : "");
      }
   }

   private void cargarDtmResumen(String query) {
      DefaultTableModel dtm = resumenCtaCtes.getDtmResumen();
      UTIL.limpiarDtm(dtm);
      List<CtacteCliente> cccList = DAO.getEntityManager().createNativeQuery(query, CtacteCliente.class).getResultList();

      //agregar la 1er fila a la tabla
      double saldoAcumulativo = (totalDebe - totalHaber);
      dtm.addRow(new Object[]{null, "RESUMEN PREVIO", null, null, UTIL.PRECIO_CON_PUNTO.format(totalDebe), UTIL.PRECIO_CON_PUNTO.format(totalHaber), null, UTIL.PRECIO_CON_PUNTO.format(saldoAcumulativo)});
      for (CtacteCliente ctaCte : cccList) {
         FacturaVenta factura = ctaCte.getFactura();
         //checkea que no esté anulada la ccc
         boolean isAnulada = (ctaCte.getEstado() == 3);
         if (!isAnulada) {
            saldoAcumulativo += ctaCte.getImporte() - ctaCte.getEntregado();
         }

         dtm.addRow(new Object[]{
                    ctaCte.getId(), // <--------- No es visible desde la GUI
                    factura.getTipo() + UTIL.AGREGAR_CEROS(factura.getNumero(), 12),
                    UTIL.DATE_FORMAT.format(factura.getFechaVenta()),
                    UTIL.DATE_FORMAT.format(UTIL.customDateByDays(factura.getFechaVenta(), ctaCte.getDias())),
                    UTIL.PRECIO_CON_PUNTO.format(ctaCte.getImporte()),
                    isAnulada ? "ANULADA" : UTIL.PRECIO_CON_PUNTO.format(ctaCte.getEntregado()),
                    isAnulada ? "ANULADA" : UTIL.PRECIO_CON_PUNTO.format(ctaCte.getImporte() - ctaCte.getEntregado()),
                    isAnulada ? "ANULADA" : UTIL.PRECIO_CON_PUNTO.format(saldoAcumulativo),
                    ctaCte.getEstado()
                 });
      }
   }

   private void imprimirResumenCCC(Cliente cliente, String filter_date) throws Exception {
      Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_ResumenCCC.jasper", "Resumen CCC");
      r.addCurrent_User();
      r.addParameter("CLIENTE_ID", cliente.getId());
      r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
      if (filter_date == null) {
         filter_date = "";
      }
      r.addParameter("FILTER_DATE", filter_date);
      r.printReport();
   }

   /**
    * Carga el combo con los Recibo's que tenga esta CtaCteCliente.
    * @param ctacteCliente
    */
   private void cargarComboBoxRecibosDeCtaCte(CtacteCliente ctacteCliente) {
      List<Recibo> recibosList = new ReciboJpaController().findRecibosByFactura(ctacteCliente.getFactura());
      UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), recibosList, false);
      setDatosReciboSelected();
   }

   private void setDatosReciboSelected() {
      try {
         Recibo recibo = (Recibo) resumenCtaCtes.getCbReRes().getSelectedItem();
         resumenCtaCtes.setTfReciboFecha(UTIL.DATE_FORMAT.format(recibo.getFechaRecibo()));
         resumenCtaCtes.setTfReciboMonto(UTIL.DECIMAL_FORMAT.format(recibo.getMonto()));
         resumenCtaCtes.getLabelReciboAnulado().setVisible(!recibo.getEstado());
         cargarDtmDetallesDeCtaCte(recibo);
      } catch (ClassCastException ex) {
         // si el comboBox está vacio
         System.out.println("Recibo NULL!");
         resumenCtaCtes.setTfReciboFecha("");
         resumenCtaCtes.setTfReciboMonto("");
         resumenCtaCtes.getLabelReciboAnulado().setVisible(false);
         UTIL.limpiarDtm(resumenCtaCtes.getDtmDetalle());
      }
   }

   private void cargarDtmDetallesDeCtaCte(Recibo recibo) {
      javax.swing.table.DefaultTableModel dtm = resumenCtaCtes.getDtmDetalle();
      UTIL.limpiarDtm(dtm);
      List<DetalleRecibo> detalleReciboList = recibo.getDetalleReciboList();
      for (DetalleRecibo detalleRecibo : detalleReciboList) {
         dtm.addRow(new Object[]{
                    UTIL.AGREGAR_CEROS(detalleRecibo.getFacturaVenta().getNumero(), 12),
                    detalleRecibo.getObservacion(),
                    UTIL.PRECIO_CON_PUNTO.format(detalleRecibo.getMontoEntrega())
                 });
      }
   }

   /**
    * Calcula el total del DEBE, HABER y SALDO ACUMULATIVO de la Cta. cte. del
    * Cliente anterior a la fecha desde especificada en el Buscador.
    * @param query
    */
   private void setResumenHistorial(String query) {
      List<CtacteCliente> lista = DAO.getEntityManager().createNativeQuery(query, CtacteCliente.class).getResultList();
      for (CtacteCliente ccc : lista) {
         if (ccc.getEstado() != 3) { // 3 == anulada
            totalDebe += ccc.getImporte();
            totalHaber += ccc.getEntregado();
         }
      }
   }
}