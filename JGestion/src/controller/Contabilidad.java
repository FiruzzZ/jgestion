package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import controller.exceptions.MissingReportException;
import entity.DetalleCajaMovimientos;
import entity.FacturaCompra;
import entity.FacturaVenta;
import generics.UTIL;
import gui.JDBalance;
import gui.PanelBalanceComprasVentas;
import gui.PanelBalanceGeneral;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Administrador
 */
public class Contabilidad {

   private JDBalance jdBalanceUI;
   private PanelBalanceGeneral panelBalanceGeneral;
   private static final String columnNamesBalanceGeneral[] = {"FECHA", "DESCRIPCIÓN", "INGRESOS", "EGRESOS", "TOTAL ACUM."};
   private static final int columnWidthsBalanceGeneral[] = {40, 440, 55, 55, 60};
   private static final Class[] columnClassBalanceGeneral = {Object.class, Object.class, String.class, String.class, String.class};
   private PanelBalanceComprasVentas panelBalanceComprasVentas;
   private static final String columnNamesBalanceCompraVenta[] = {"FECHA", "DESCRIPCIÓN", "INGRESOS/EGRESOS", "EFECTIVO", "CTA. CTE.", "TOTAL ACUM."};
   private static final int columnWidthsBalanceCompraVenta[] = {60, 300, 55, 55, 60, 60};
   private static final Class[] columnClassBalanceCompraVenta = {Object.class, Object.class, String.class, String.class, String.class, String.class};

   /**
    * GUI para visualización de los movimientos INGRESOS/EGRESOS.
    * @param parent
    */
   public void initBalanceGeneralUI(JFrame parent) {
      panelBalanceGeneral = new PanelBalanceGeneral();
      jdBalanceUI = new JDBalance(parent, false, panelBalanceGeneral);
      jdBalanceUI.getLabelTotalAux().setVisible(false);
      jdBalanceUI.getTfTotalAux().setVisible(false);
      UTIL.getDefaultTableModel(jdBalanceUI.getjTable1(),
              columnNamesBalanceGeneral,
              columnWidthsBalanceGeneral,
              columnClassBalanceGeneral);
      DefaultTableCellRenderer defaultTableCellRender = new DefaultTableCellRenderer();
      defaultTableCellRender.setHorizontalAlignment(JLabel.RIGHT);
      jdBalanceUI.getjTable1().setDefaultRenderer(String.class, defaultTableCellRender);
      jdBalanceUI.setTitle("Balance");
      jdBalanceUI.getbBuscar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               List<DetalleCajaMovimientos> l = (List<DetalleCajaMovimientos>) DAO.getNativeQueryResultList(armarQueryBalance(), DetalleCajaMovimientos.class.getSimpleName() + ".BalanceGeneral");
               cargarTablaBalanceGeneral(l);
            } catch (DatabaseErrorException ex) {
               Logger.getLogger(Contabilidad.class.getName()).log(Level.FATAL, null, ex);
            }
         }
      });
      jdBalanceUI.getbImprimir().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            List<DetalleCajaMovimientos> l;
            try {
               String query = armarQueryBalance();
               l = (List<DetalleCajaMovimientos>) DAO.getNativeQueryResultList(query, DetalleCajaMovimientos.class.getSimpleName() + ".BalanceGeneral");
               cargarTablaBalanceGeneral(l);
               doReportBalance(query,
                       panelBalanceGeneral.getDcDesde().getDate(),
                       panelBalanceGeneral.getDcHasta().getDate(),
                       panelBalanceGeneral.getCheckMovEntreCajas().isSelected());
            } catch (JRException ex) {
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
            } catch (MissingReportException ex) {
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
            } catch (DatabaseErrorException ex) {
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
            }
         }
      });
      jdBalanceUI.setLocationRelativeTo(parent);
      jdBalanceUI.setVisible(true);
   }

   /**
    * Adiviná que hace......
    * @param QUERY String SQL statement
    * @param FECHA_DESDE opcional
    * @param FECHA_HASTA opcional
    * @throws MissingReportException
    * @throws JRException
    */
   private void doReportBalance(String QUERY, Date FECHA_DESDE, Date FECHA_HASTA, boolean CON_MOV_CAJAS)
           throws MissingReportException, JRException {
      Reportes r = new Reportes("JGestion_Balance.jasper", "Balance");
      r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
      r.addParameter("CURRENT_USER", UsuarioJpaController.getCurrentUser().getNick());
      r.addParameter("QUERY", QUERY);
      r.addParameter("FECHA_DESDE", FECHA_DESDE);
      r.addParameter("FECHA_HASTA", FECHA_HASTA);
      r.addParameter("CON_MOV_CAJAS", CON_MOV_CAJAS);
      r.viewReport();
   }

   /**
    * Create SQL Statement to retrieve entities {@link DetalleCajaMovimientos#tipo} != 7 (aperturas de caja)
    * @return a String with SQL
    */
   private String armarQueryBalance() {
      //tipo == 7 son las aperturas de caja
      StringBuilder sb = new StringBuilder("SELECT o.* FROM detalle_caja_movimientos o WHERE tipo <> 7");
      if (!panelBalanceGeneral.getCheckMovEntreCajas().isSelected()) {
         sb.append(" AND tipo <> 5");
      }
      Date fecha = panelBalanceGeneral.getDcDesde().getDate();
      if (fecha != null) {
         sb.append(" AND fecha >= '").append(fecha).append("'");
      }
      fecha = panelBalanceGeneral.getDcHasta().getDate();
      if (fecha != null) {
         sb.append(" AND fecha <= '").append(fecha).append("'");
      }
      if (panelBalanceGeneral.getCbIngresosEgresos().getSelectedIndex() > 0) {
         sb.append(" AND o.ingreso=").append(panelBalanceGeneral.getCbIngresosEgresos().getSelectedIndex() == 1);
      }
      sb.append(" ORDER BY o.fecha");
      return sb.toString();
   }

   private void cargarTablaBalanceGeneral(List<DetalleCajaMovimientos> lista) {
      UTIL.limpiarDtm(jdBalanceUI.getjTable1());
      Double subTotal, ingresos, egresos;
      subTotal = 0.0;
      ingresos = 0.0;
      egresos = 0.0;
      SimpleDateFormat dateFormat = UTIL.instanceOfDATE_FORMAT();
      for (DetalleCajaMovimientos detalleCajaMovimientos : lista) {
         //los movimientos entre caja no representan un ingreso/egreso real
         if (detalleCajaMovimientos.getTipo() != DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA) {
            if (detalleCajaMovimientos.getIngreso()) {
               ingresos += detalleCajaMovimientos.getMonto();
            } else {
               egresos += detalleCajaMovimientos.getMonto();
            }
            subTotal += detalleCajaMovimientos.getMonto();
         }
         UTIL.getDtm(jdBalanceUI.getjTable1()).addRow(new Object[]{
                    dateFormat.format(detalleCajaMovimientos.getFecha()),
                    detalleCajaMovimientos.getDescripcion(),
                    detalleCajaMovimientos.getIngreso() ? detalleCajaMovimientos.getMonto() : null,
                    detalleCajaMovimientos.getIngreso() ? null : detalleCajaMovimientos.getMonto(),
                    UTIL.PRECIO_CON_PUNTO.format(subTotal)
                 });
      }
      jdBalanceUI.getTfIngresos().setText(UTIL.PRECIO_CON_PUNTO.format(ingresos));
      jdBalanceUI.getTfEgresos().setText(UTIL.PRECIO_CON_PUNTO.format(egresos));
      jdBalanceUI.getTfTotal().setText(UTIL.PRECIO_CON_PUNTO.format(subTotal));
   }

   /**
    * Ventana para ver los registros de venta (Facturas [Contado, Cta. Cte.]),
    * Mov. internos, etc..
    * @param parent papi frame
    */
   public void initBalanceCompraVentaUI(JFrame parent) {
      panelBalanceComprasVentas = new PanelBalanceComprasVentas();
      jdBalanceUI = new JDBalance(parent, false, panelBalanceComprasVentas);
      jdBalanceUI.getLabelTotalAux().setText("INGRESOS/EGRESOS");
      jdBalanceUI.getLabelTotalIngresos().setText("EFECTIVO");
      jdBalanceUI.getLabelTotalEgresos().setText("Cta. Cte.");
      jdBalanceUI.getLabelTotalTotal().setText(null);
      jdBalanceUI.getTfTotal().setText("---------");

      jdBalanceUI.setSize(700, 500);
      UTIL.getDefaultTableModel(jdBalanceUI.getjTable1(),
              columnNamesBalanceCompraVenta,
              columnWidthsBalanceCompraVenta,
              columnClassBalanceCompraVenta);
      DefaultTableCellRenderer defaultTableCellRender = new DefaultTableCellRenderer();
      defaultTableCellRender.setHorizontalAlignment(JLabel.RIGHT);
      jdBalanceUI.getjTable1().setDefaultRenderer(String.class, defaultTableCellRender);
      jdBalanceUI.setTitle("Balance");
      jdBalanceUI.getbBuscar().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               if (!panelBalanceComprasVentas.getCheckContado().isSelected()
                       && !panelBalanceComprasVentas.getCheckCtaCte().isSelected()) {
                  throw new MessageException("Debe elegir al menos una forma de facturación (CONTADO, CTA. CTE.)");
               }
               String query = armarQueryBalanceComprasVentas();
               if (panelBalanceComprasVentas.getCbComprasVentas().getSelectedIndex() == 0) {
                  cargarTablaBalanceCompra((List<FacturaCompra>) DAO.getNativeQueryResultList(query, FacturaCompra.class));
               } else {
                  cargarTablaBalanceVenta((List<FacturaVenta>) DAO.getNativeQueryResultList(query, FacturaVenta.class));
               }
            } catch (DatabaseErrorException ex) {
               Logger.getLogger(Contabilidad.class.getName()).log(Level.FATAL, null, ex);
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
            } catch (MessageException ex) {
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
            }
         }
      });
      jdBalanceUI.getbImprimir().addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               if (!panelBalanceComprasVentas.getCheckContado().isSelected()
                       && !panelBalanceComprasVentas.getCheckCtaCte().isSelected()) {
                  throw new MessageException("Debe elegir al menos una forma de facturación (CONTADO, CTA. CTE.)");
               }
               String query = armarQueryBalanceComprasVentas();
               if (panelBalanceComprasVentas.getCbComprasVentas().getSelectedIndex() == 0) {
                  cargarTablaBalanceCompra((List<FacturaCompra>) DAO.getNativeQueryResultList(query, FacturaCompra.class));
               } else {
                  cargarTablaBalanceVenta((List<FacturaVenta>) DAO.getNativeQueryResultList(query, FacturaVenta.class));
               }
               doReportBalanceCompraVenta(query,
                       panelBalanceComprasVentas.getDcDesde().getDate(),
                       panelBalanceComprasVentas.getDcHasta().getDate(),
                       panelBalanceComprasVentas.getCbComprasVentas().getSelectedItem().toString(), //title
                       panelBalanceComprasVentas.getCheckContado().isSelected(),
                       panelBalanceComprasVentas.getCheckCtaCte().isSelected(),
                       panelBalanceComprasVentas.getCheckAnuladas().isSelected());
            } catch (JRException ex) {
               Logger.getLogger(Contabilidad.class.getName()).log(Level.FATAL, null, ex);
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
            } catch (MissingReportException ex) {
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
            } catch (DatabaseErrorException ex) {
               Logger.getLogger(Contabilidad.class.getName()).log(Level.FATAL, null, ex);
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
            } catch (MessageException ex) {
               JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
            }
         }
      });
      jdBalanceUI.setLocationRelativeTo(parent);
      jdBalanceUI.setVisible(true);
   }

   private String armarQueryBalanceComprasVentas() {
      String tabla = panelBalanceComprasVentas.getCbComprasVentas().getSelectedIndex() == 0 ? "compra" : "venta";
      String entidad = panelBalanceComprasVentas.getCbComprasVentas().getSelectedIndex() == 0 ? "proveedor" : "cliente";
      StringBuilder sb = new StringBuilder("SELECT o.*, o.fecha_" + tabla + " as fecha, ccc.entregado"
              + " FROM factura_" + tabla + " o LEFT JOIN ctacte_" + entidad + " ccc ON o.id = ccc.factura"
              + " WHERE o.id IS NOT NULL");
      if (panelBalanceComprasVentas.getCheckContado().isSelected() && panelBalanceComprasVentas.getCheckCtaCte().isSelected()) {
         //no hace falta ningún filtro.. va traer ambas
      } else {
         if (panelBalanceComprasVentas.getCheckContado().isSelected()) {
            sb.append(" AND o.forma_pago = ").append(Valores.FormaPago.CONTADO.getId());
         }
         if (panelBalanceComprasVentas.getCheckCtaCte().isSelected()) {
            sb.append(" AND o.forma_pago = ").append(Valores.FormaPago.CTA_CTE.getId());
         }
      }
      if (!panelBalanceComprasVentas.getCheckAnuladas().isSelected()) {
         sb.append(" AND o.anulada = false");
      }
      Date fecha = panelBalanceComprasVentas.getDcDesde().getDate();
      if (fecha != null) {
         sb.append(" AND o.fecha_").append(tabla).append(" >= '").append(fecha).append("'");
      }
      fecha = panelBalanceComprasVentas.getDcHasta().getDate();
      if (fecha != null) {
         sb.append(" AND o.fecha_").append(tabla).append(" <= '").append(fecha).append("'");
      }

      sb.append(" ORDER BY o.fecha_").append(tabla);
      System.out.println(sb.toString());
      return sb.toString();
   }

   private void cargarTablaBalanceCompra(List<FacturaCompra> l) {
      DefaultTableModel dtm = UTIL.getDtm(jdBalanceUI.getjTable1());
      UTIL.limpiarDtm(dtm);
      Double totalEfectivo, totalIngresos, efectivo = null, cccpc = null;
      totalEfectivo = 0.0;
      totalIngresos = 0.0;
      Double totalCCPPC = 0.0;
      Double entregado;
      SimpleDateFormat dateFormat = UTIL.instanceOfDATE_FORMAT();
      for (FacturaCompra factura : l) {
         if (!factura.getAnulada()) {
            totalIngresos += factura.getImporte();
            if (Valores.FormaPago.CONTADO.getId() == factura.getFormaPago()) {
               cccpc = null;
               efectivo = factura.getImporte();
               totalEfectivo += efectivo;
            } else if (Valores.FormaPago.CTA_CTE.getId() == factura.getFormaPago()) {
               efectivo = null;
               entregado = new CtacteProveedorJpaController().findCtacteProveedorByFactura(factura.getId()).getEntregado();
               cccpc = (factura.getImporte() - entregado);
               totalCCPPC += cccpc;
               efectivo = entregado > 0 ? entregado : null;
            } else {
               Logger.getLogger(Contabilidad.class).info("y botella?");
            }
         } else {
            efectivo = null;
            cccpc = null;
         }
         dtm.addRow(new Object[]{
                    dateFormat.format(factura.getFechaCompra()),
                    factura.getTipo() + factura.toString() + (factura.getAnulada() ? "(ANULADA)" : ""),
                    UTIL.PRECIO_CON_PUNTO.format(factura.getImporte()),
                    efectivo != null ? efectivo : "------",
                    cccpc != null ? cccpc : "------",
                    UTIL.PRECIO_CON_PUNTO.format(totalIngresos)
                 });
      }
      jdBalanceUI.getTfTotalAux().setText(UTIL.PRECIO_CON_PUNTO.format(totalIngresos));
      jdBalanceUI.getTfIngresos().setText(UTIL.PRECIO_CON_PUNTO.format(totalEfectivo));
      jdBalanceUI.getTfEgresos().setText(UTIL.PRECIO_CON_PUNTO.format(totalCCPPC));
      jdBalanceUI.getTfTotal().setText(null);
   }

   private void cargarTablaBalanceVenta(List<FacturaVenta> l) {
      DefaultTableModel dtm = UTIL.getDtm(jdBalanceUI.getjTable1());
      UTIL.limpiarDtm(dtm);
      Double totalEfectivo, totalIngresos, efectivo = null, cccpc = null;
      totalEfectivo = 0.0;
      totalIngresos = 0.0;
      Double totalCCCPC = 0.0;
      Double entregado;
      SimpleDateFormat dateFormat = UTIL.instanceOfDATE_FORMAT();
      for (FacturaVenta factura : l) {
         if (!factura.getAnulada()) {
            totalIngresos += factura.getImporte();
            if (Valores.FormaPago.CONTADO.getId() == factura.getFormaPago()) {
               cccpc = null;
               efectivo = factura.getImporte();
               totalEfectivo += efectivo;
            } else if (Valores.FormaPago.CTA_CTE.getId() == factura.getFormaPago()) {
               efectivo = null;
               entregado = new CtacteClienteJpaController().findCtacteClienteByFactura(factura.getId()).getEntregado();
               cccpc = (factura.getImporte() - entregado);
               totalCCCPC += cccpc;
               efectivo = entregado > 0 ? entregado : null;
            } else {
               Logger.getLogger(Contabilidad.class).info("y botella?");
            }
         } else {
            efectivo = null;
            cccpc = null;
         }
         dtm.addRow(new Object[]{
                    dateFormat.format(factura.getFechaVenta()),
                    factura.getTipo() + factura.toString() + (factura.getAnulada() ? "(ANULADA)" : ""),
                    UTIL.PRECIO_CON_PUNTO.format(factura.getImporte()),
                    efectivo != null ? efectivo : "------",
                    cccpc != null ? cccpc : "------",
                    UTIL.PRECIO_CON_PUNTO.format(totalIngresos)
                 });
      }
      jdBalanceUI.getTfTotalAux().setText(UTIL.PRECIO_CON_PUNTO.format(totalIngresos));
      jdBalanceUI.getTfIngresos().setText(UTIL.PRECIO_CON_PUNTO.format(totalEfectivo));
      jdBalanceUI.getTfEgresos().setText(UTIL.PRECIO_CON_PUNTO.format(totalCCCPC));
      jdBalanceUI.getTfTotal().setText(null);
   }

   private void doReportBalanceCompraVenta(String query, Date desde, Date hasta,
           String title, boolean contado, boolean ctacte, boolean anuladas)
           throws JRException, MissingReportException {
      Reportes r = new Reportes("Jgestion_balance_factucv.jasper", "Balance de " + title);
      r.addParameter("SUBREPORT_DIR", Reportes.FOLDER_REPORTES);
      r.addParameter("QUERY", query);
      r.addParameter("FECHA_DESDE", desde);
      r.addParameter("FECHA_HASTA", hasta);
      r.addParameter("TITLE", "BALANCE DE FACTURAS " + title);
      r.addParameter("FACT_CONTADO", contado);
      r.addParameter("FACT_CTACTE", ctacte);
      r.addParameter("FACT_ANULADAS", anuladas);
      r.viewReport();
   }
}
