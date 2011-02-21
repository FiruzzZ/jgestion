package controller;

import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.CtacteProveedor;
import entity.DetalleRemesa;
import entity.FacturaCompra;
import entity.Proveedor;
import entity.Remesa;
import generics.UTIL;
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
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import net.sf.jasperreports.engine.JRException;

/**
 *
 * @author Administrador
 */
public class CtacteProveedorJpaController implements ActionListener {

   public static final String CLASS_NAME = CtacteProveedor.class.getSimpleName();
   private JDResumenCtaCtes resumenCtaCtes;
   private Double totalDebe;
   private Double totalHaber;

   // <editor-fold defaultstate="collapsed" desc="CRUD...">
   public EntityManager getEntityManager() {
      return DAO.getEntityManager();
   }

   public void create(CtacteProveedor ctacteProveedor) {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         em.persist(ctacteProveedor);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public void edit(CtacteProveedor ctacteProveedor) throws NonexistentEntityException, Exception {
      EntityManager em = null;
      try {
         em = getEntityManager();
         em.getTransaction().begin();
         ctacteProveedor = em.merge(ctacteProveedor);
         em.getTransaction().commit();
      } catch (Exception ex) {
         String msg = ex.getLocalizedMessage();
         if (msg == null || msg.length() == 0) {
            Integer id = ctacteProveedor.getId();
            if (findCtacteProveedor(id) == null) {
               throw new NonexistentEntityException("The ctacteProveedor with id " + id + " no longer exists.");
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
         CtacteProveedor ctacteProveedor;
         try {
            ctacteProveedor = em.getReference(CtacteProveedor.class, id);
            ctacteProveedor.getId();
         } catch (EntityNotFoundException enfe) {
            throw new NonexistentEntityException("The ctacteProveedor with id " + id + " no longer exists.", enfe);
         }
         em.remove(ctacteProveedor);
         em.getTransaction().commit();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   public List<CtacteProveedor> findCtacteProveedorEntities() {
      return findCtacteProveedorEntities(true, -1, -1);
   }

   public List<CtacteProveedor> findCtacteProveedorEntities(int maxResults, int firstResult) {
      return findCtacteProveedorEntities(false, maxResults, firstResult);
   }

   private List<CtacteProveedor> findCtacteProveedorEntities(boolean all, int maxResults, int firstResult) {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select object(o) from CtacteProveedor as o");
         if (!all) {
            q.setMaxResults(maxResults);
            q.setFirstResult(firstResult);
         }
         return q.getResultList();
      } finally {
         em.close();
      }
   }

   public CtacteProveedor findCtacteProveedor(Integer id) {
      EntityManager em = getEntityManager();
      try {
         return em.find(CtacteProveedor.class, id);
      } finally {
         em.close();
      }
   }

   public int getCtacteProveedorCount() {
      EntityManager em = getEntityManager();
      try {
         Query q = em.createQuery("select count(o) from CtacteProveedor as o");
         return ((Long) q.getSingleResult()).intValue();
      } finally {
         em.close();
      }
   }// </editor-fold>

   void nuevaCtaCte(FacturaCompra facturaCompra) {
      CtacteProveedor ccp = new CtacteProveedor();
      ccp.setDias(facturaCompra.getDiasCtaCte());
      ccp.setEntregado(0.0); //monto $$
      ccp.setEstado(Valores.CtaCteEstado.PENDIENTE.getEstado());
      ccp.setFactura(facturaCompra);
      ccp.setFechaCarga(facturaCompra.getFechaalta());
      ccp.setImporte(facturaCompra.getImporte());
      create(ccp);
   }

   List<CtacteProveedor> findCtacteProveedorByProveedor(Integer idProveedor, int estadoCtaCte) {
      EntityManager em = getEntityManager();
//      em.getTransaction().begin();
      List<CtacteProveedor> listaCtaCteProveedor = null;
      try {
         listaCtaCteProveedor = em.createNativeQuery(
                 "SELECT o.* FROM ctacte_proveedor o, factura_compra f, proveedor p"
                 + " WHERE p.id = f.proveedor AND f.id = o.factura "
                 + " AND o.estado = " + estadoCtaCte + " AND p.id =" + idProveedor,
                 CtacteProveedor.class).getResultList();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      return listaCtaCteProveedor;
   }

   List<CtacteProveedor> findCtacteProveedorByProveedor(Integer idProveedor) {
      EntityManager em = getEntityManager();
      em.getTransaction().begin();
      List<CtacteProveedor> listaCtaCteProveedor = null;
      try {
         listaCtaCteProveedor = em.createNativeQuery(
                 "SELECT o.* FROM ctacte_proveedor o, factura_compra f, proveedor p"
                 + " WHERE p.id = f.proveedor AND f.id = o.factura "
                 + " AND p.id =" + idProveedor,
                 CtacteProveedor.class).getResultList();
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      return listaCtaCteProveedor;
   }

   CtacteProveedor findCtacteProveedorByFactura(Integer idFacturaCompra) throws NoResultException {
      return (CtacteProveedor) DAO.getEntityManager().createNativeQuery("select * from ctacte_proveedor o "
              + "where o.factura = " + idFacturaCompra, CtacteProveedor.class).getSingleResult();

   }

   public void iniResumenCtaCte(JFrame frame, boolean modal) {
      // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.CHECK_PERMISO(PermisosJpaController.PermisoDe.TESORERIA);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(frame, ex.getMessage());
         return;
      }// </editor-fold>

      resumenCtaCtes = new JDResumenCtaCtes(frame, modal, false);
      resumenCtaCtes.getjTableResumen().addMouseListener(new MouseAdapter() {

         @Override
         public void mouseReleased(MouseEvent e) {
            jTableResumenMouseReleased(e);
         }
      });
      UTIL.loadComboBox(resumenCtaCtes.getCbClieProv(), new ProveedorJpaController().findProveedorEntities(), false);
      UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), null, true);
      UTIL.getDefaultTableModel(
              resumenCtaCtes.getjTableResumen(),
              new String[]{"ctacteProveedorID", "Detalle", "Fecha", "Vencimiento", "Debe", "Haber", "Saldo", "Acumulativo"},
              new int[]{1, 60, 50, 50, 30, 30, 30, 50});
      UTIL.hideColumnTable(resumenCtaCtes.getjTableResumen(), 0);
      UTIL.getDefaultTableModel(
              resumenCtaCtes.getjTableDetalle(),
              new String[]{"Nº Factura", "Observación", "Monton"},
              new int[]{60, 100, 50});
      resumenCtaCtes.setListener(this);
      resumenCtaCtes.setLocationByPlatform(true);
      resumenCtaCtes.setVisible(true);
   }

   private void jTableResumenMouseReleased(MouseEvent e) {
      Integer selectedRow = resumenCtaCtes.getjTableResumen().getSelectedRow();
      if (selectedRow > 0) {
         //selecciona una factura CtaCteCliente
         cargarComboBoxRecibosDeCtaCte((CtacteProveedor) DAO.getEntityManager().find(CtacteProveedor.class,
                 Integer.valueOf((resumenCtaCtes.getDtmResumen().getValueAt(selectedRow, 0)).toString())));
      }
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      // <editor-fold defaultstate="collapsed" desc="JButton">
      if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
         javax.swing.JButton btn = (javax.swing.JButton) e.getSource();

         // <editor-fold defaultstate="collapsed" desc="verResumenCCC">
         if (btn.getName().equalsIgnoreCase("verCtactes")) {
            try {
               armarQuery(false);
            } catch (MessageException ex) {
               resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
            } catch (Exception ex) {
               resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
               Logger.getLogger(CtacteClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }// </editor-fold>
         // <editor-fold defaultstate="collapsed" desc="imprimirResumenCCC">
         else if (btn.getName().equalsIgnoreCase("print")) {
            try {
               armarQuery(true);
            } catch (MessageException ex) {
               resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
            } catch (Exception ex) {
               resumenCtaCtes.showMessage(ex.getMessage(), null, 2);
               Logger.getLogger(CtacteClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
            }
         }// </editor-fold>
      }// </editor-fold>
      // <editor-fold defaultstate="collapsed" desc="JComboBox">
      else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
         javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
         if (combo.getName().equalsIgnoreCase("cbReRes")) {
            if (combo.isFocusOwner()) {
               setDatosReciboSelected();
            }
         }
      }// </editor-fold>
   }

   private void armarQuery(boolean imprimirResumen) throws MessageException, Exception {
      totalDebe = 0.0;
      totalHaber = 0.0;

      String query = "SELECT ccc.* "
              + " FROM ctacte_proveedor ccc, proveedor c, factura_compra fv "
              + " WHERE ccc.factura = fv.id "
              + " AND fv.proveedor = c.id ";
      try {
         query += "AND c.id =" + ((Proveedor) resumenCtaCtes.getCbClieProv().getSelectedItem()).getId();
      } catch (ClassCastException ex) {
         throw new MessageException("Proveedor no válido");
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
         imprimirResumenCCC(((Proveedor) resumenCtaCtes.getCbClieProv().getSelectedItem()), resumenCtaCtes.getDcDesde() != null ? " AND ccc.fecha_carga >= '" + UTIL.DATE_FORMAT.format(resumenCtaCtes.getDcDesde()) + "'" : "");
      }
   }

   private void setResumenHistorial(String query) {
      List<CtacteProveedor> lista = DAO.getEntityManager().createNativeQuery(query, CtacteProveedor.class).getResultList();
      for (CtacteProveedor ccc : lista) {
         if (ccc.getEstado() != 3) { // 3 == anulado
            totalDebe += ccc.getImporte();
            totalHaber += ccc.getEntregado();
         }
      }
   }

   private void cargarDtmResumen(String query) {
      javax.swing.table.DefaultTableModel dtm = resumenCtaCtes.getDtmResumen();
      UTIL.limpiarDtm(dtm);
      List<CtacteProveedor> lista = DAO.getEntityManager().createNativeQuery(query, CtacteProveedor.class).getResultList();

      //agregar la 1er fila a la tabla
      double saldoAcumulativo = (totalDebe - totalHaber);
      dtm.addRow(new Object[]{null, "RESUMEN PREVIOS", null, null, UTIL.PRECIO_CON_PUNTO.format(totalDebe), UTIL.PRECIO_CON_PUNTO.format(totalHaber), null, UTIL.PRECIO_CON_PUNTO.format(saldoAcumulativo)});
      for (CtacteProveedor ctaCte : lista) {
         FacturaCompra factura = ctaCte.getFactura();
         //checkea que no esté anulada la ccc
         boolean isAnulada = (ctaCte.getEstado() == 3);
         if (!isAnulada) {
            saldoAcumulativo += ctaCte.getImporte() - ctaCte.getEntregado();
         }

         dtm.addRow(new Object[]{
                    ctaCte.getId(), // <--------- No es visible desde la GUI
                    factura.getTipo() + UTIL.AGREGAR_CEROS(factura.getNumero(), 12),
                    UTIL.DATE_FORMAT.format(factura.getFechaCompra()),
                    UTIL.DATE_FORMAT.format(UTIL.customDateByDays(factura.getFechaCompra(), ctaCte.getDias())),
                    UTIL.PRECIO_CON_PUNTO.format(ctaCte.getImporte()),
                    isAnulada ? "ANULADA" : UTIL.PRECIO_CON_PUNTO.format(ctaCte.getEntregado()),
                    isAnulada ? "ANULADA" : UTIL.PRECIO_CON_PUNTO.format(ctaCte.getImporte() - ctaCte.getEntregado()),
                    isAnulada ? "ANULADA" : UTIL.PRECIO_CON_PUNTO.format(saldoAcumulativo),
                    ctaCte.getEstado()
                 });
      }
   }

   private void imprimirResumenCCC(Proveedor proveedor, String filter_date) throws JRException, Exception {
      Reportes r = new Reportes(Reportes.FOLDER_REPORTES + "JGestion_ResumenCCP.jasper", "Resumen Cta. Cte. Proveedor");
      r.addCurrent_User();
      r.addParameter("PROVEEDOR_ID", proveedor.getId());
      r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
      if (filter_date == null) {
         filter_date = "";
      }
      r.addParameter("FILTER_DATE", filter_date);
      r.printReport(true);
   }

   private void cargarComboBoxRecibosDeCtaCte(CtacteProveedor ctacteProveedor) {
      List<Remesa> recibosList = new RemesaJpaController().findByFactura(ctacteProveedor.getFactura());
      UTIL.loadComboBox(resumenCtaCtes.getCbReRes(), recibosList, false);
      setDatosReciboSelected();
   }

   private void setDatosReciboSelected() {
      try {
         Remesa remesa = (Remesa) resumenCtaCtes.getCbReRes().getSelectedItem();
         resumenCtaCtes.setTfReciboFecha(UTIL.DATE_FORMAT.format(remesa.getFechaRemesa()));
         resumenCtaCtes.setTfReciboMonto(UTIL.DECIMAL_FORMAT.format(remesa.getMonto()));
         cargarDtmDetallesDeCtaCte(remesa);
      } catch (ClassCastException ex) {
         // si el comboBox está vacio
         System.out.println("Remesa NULL!");
         resumenCtaCtes.setTfReciboFecha("");
         resumenCtaCtes.setTfReciboMonto("");
         UTIL.limpiarDtm(resumenCtaCtes.getDtmDetalle());
      }
   }

   private void cargarDtmDetallesDeCtaCte(Remesa remesa) {
      javax.swing.table.DefaultTableModel dtm = resumenCtaCtes.getDtmDetalle();
      UTIL.limpiarDtm(dtm);
      List<DetalleRemesa> detalleReList = remesa.getDetalleRemesaList();
      for (DetalleRemesa detalleRe : detalleReList) {
         dtm.addRow(new Object[]{
                    UTIL.AGREGAR_CEROS(detalleRe.getFacturaCompra().getNumero(), 12),
                    detalleRe.getObservacion(),
                    UTIL.PRECIO_CON_PUNTO.format(detalleRe.getMontoEntrega())
                 });
      }
   }
}
