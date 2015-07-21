package jgestion.controller;

import com.toedter.calendar.JDateChooser;
import generics.GenericBeanCollection;
import generics.gui.GroupLayoutPanelBuilder;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.NoResultException;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import jgestion.JGestion;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.Caja;
import jgestion.entity.ChequePropio;
import jgestion.entity.ChequeTerceros;
import jgestion.entity.Cliente;
import jgestion.entity.ComprobanteRetencion;
import jgestion.entity.DetalleCajaMovimientos;
import jgestion.entity.DetalleCompra;
import jgestion.entity.DetalleListaPrecios;
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaCompra;
import jgestion.entity.FacturaCompra_;
import jgestion.entity.FacturaVenta;
import jgestion.entity.FacturaVenta_;
import jgestion.entity.ListaPrecios;
import jgestion.entity.Marca;
import jgestion.entity.NotaCredito;
import jgestion.entity.Producto;
import jgestion.entity.Producto_;
import jgestion.entity.Proveedor;
import jgestion.entity.Recibo;
import jgestion.entity.Remesa;
import jgestion.entity.Rubro;
import jgestion.entity.Sucursal;
import jgestion.gui.JDABM;
import jgestion.gui.JDBalance;
import jgestion.gui.JDBuscador;
import jgestion.gui.JDBuscadorReRe;
import jgestion.gui.JDInformeResultados;
import jgestion.gui.JDInformeUnidadesDeNegocios;
import jgestion.gui.JDResumenGeneralCtaCte;
import jgestion.gui.PanelBalanceComprasVentas;
import jgestion.gui.PanelBalanceGeneral;
import jgestion.gui.PanelDetalleFacturacion;
import jgestion.gui.PanelInformeFlujoVentas;
import jgestion.gui.PanelProductosCostoVenta;
import jgestion.jpa.controller.ClienteJpaController;
import jgestion.jpa.controller.ComprobanteRetencionJpaController;
import jgestion.jpa.controller.ProveedorJpaController;
import jgestion.jpa.controller.VendedorJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.TableExcelExporter;
import utilities.general.UTIL;
import utilities.general.EntityWrapper;
import utilities.swing.components.FormatRenderer;
import utilities.swing.components.NumberRenderer;

/**
 *
 * @author Administrador
 */
public class Contabilidad {

    /**
     * #0.0000
     */
    public static final DecimalFormat PU_FORMAT;
    private JDBalance jdBalanceUI;
    private PanelBalanceGeneral panelBalanceGeneral;
    private static final String columnNamesBalanceGeneral[] = {"FECHA", "DESCRIPCIÓN", "INGRESOS", "EGRESOS", "TOTAL ACUM."};
    private static final int columnWidthsBalanceGeneral[] = {40, 440, 55, 55, 60};
    private static final Class[] columnClassBalanceGeneral = {Object.class, Object.class, String.class, String.class, String.class};
    private PanelBalanceComprasVentas panelBalanceComprasVentas;
//    private static final Class[] columnClassBalanceCompraVenta = {Object.class, Object.class, String.class, String.class, String.class, String.class};
    private static final Logger LOG = LogManager.getLogger();

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        PU_FORMAT = new DecimalFormat("#0.0000", simbolos);
    }
    private JDBuscadorReRe buscadorReRe;
    private JDBuscador buscador;
    private PanelProductosCostoVenta panelito;

    /**
     * GUI para ver de los movimientos INGRESOS/EGRESOS.
     *
     * @param parent
     * @throws MessageException
     */
    public void initMovimientosCajasUI(Window parent) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        panelBalanceGeneral = new PanelBalanceGeneral();
        List<EntityWrapper<Caja>> cajas = JGestionUtils.getWrappedCajas(new UsuarioHelper().getCajas(null));
        if (cajas.isEmpty()) {
            throw new MessageException(jgestion.JGestion.resourceBundle.getString("unassigned.caja"));
        }
        UTIL.loadComboBox(panelBalanceGeneral.getCbCajas(), cajas, true);
        jdBalanceUI = new JDBalance(parent, false, panelBalanceGeneral);
        jdBalanceUI.setTitle("Flujo de fondos - Movimientos de Cajas");
        jdBalanceUI.getLabelEgresos().setVisible(false);
        jdBalanceUI.getTfEgresos().setVisible(false);
        jdBalanceUI.getjTable1().setAutoCreateRowSorter(true);
        UTIL.getDefaultTableModel(jdBalanceUI.getjTable1(),
                columnNamesBalanceGeneral,
                columnWidthsBalanceGeneral,
                columnClassBalanceGeneral);
        UTIL.setHorizonalAlignment(jdBalanceUI.getjTable1(), String.class, JLabel.RIGHT);
        jdBalanceUI.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<DetalleCajaMovimientos> l = (List<DetalleCajaMovimientos>) DAO.getNativeQueryResultList(armarQueryBalanceGeneral(), DetalleCajaMovimientos.class.getSimpleName() + ".BalanceGeneral");
                    cargarTablaBalanceGeneral(l);
                } catch (DatabaseErrorException ex) {
                    LogManager.getLogger();//(Contabilidad.class.getName()).log(Level.FATAL, null, ex);
                }
            }
        });
        jdBalanceUI.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<DetalleCajaMovimientos> l;
                try {
                    String query = armarQueryBalanceGeneral();
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
     *
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
        r.addParameter("CURRENT_USER", UsuarioController.getCurrentUser().getNick());
        r.addParameter("QUERY", QUERY);
        r.addParameter("FECHA_DESDE", FECHA_DESDE);
        r.addParameter("FECHA_HASTA", FECHA_HASTA);
        r.addParameter("CON_MOV_CAJAS", CON_MOV_CAJAS);
        r.viewReport();
    }

    /**
     * Create Native SQL Statement to retrieve entities {@link DetalleCajaMovimientos#tipo} != 7
     * (aperturas de caja)
     *
     * @return a String with SQL
     */
    private String armarQueryBalanceGeneral() {
        StringBuilder query = new StringBuilder("SELECT o.* FROM detalle_caja_movimientos o JOIN caja_movimientos cm ON (o.caja_movimientos = cm.id)"
                + " WHERE o.tipo <> " + DetalleCajaMovimientosController.APERTURA_CAJA);
        if (panelBalanceGeneral.getCbCajas().getSelectedIndex() > 0) {
            query.append(" AND cm.caja=").append(((EntityWrapper<?>) panelBalanceGeneral.getCbCajas().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < panelBalanceGeneral.getCbCajas().getItemCount(); i++) {
                EntityWrapper<?> caja = (EntityWrapper<?>) panelBalanceGeneral.getCbCajas().getItemAt(i);
                query.append(" cm.caja=").append(caja.getId());
                if ((i + 1) < panelBalanceGeneral.getCbCajas().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (!panelBalanceGeneral.getCheckMovEntreCajas().isSelected()) {
            query.append(" AND o.tipo <> ").append(DetalleCajaMovimientosController.MOVIMIENTO_CAJA);
        }
        Date fecha = panelBalanceGeneral.getDcDesde().getDate();
        if (fecha != null) {
            query.append(" AND o.fecha >= '").append(fecha).append("'");
        }
        fecha = panelBalanceGeneral.getDcHasta().getDate();
        if (fecha != null) {
            query.append(" AND o.fecha <= '").append(fecha).append("'");
        }
        if (panelBalanceGeneral.getCbIngresosEgresos().getSelectedIndex() > 0) {
            query.append(" AND o.ingreso=").append(panelBalanceGeneral.getCbIngresosEgresos().getSelectedIndex() == 1);
        }
        query.append(" ORDER BY o.fecha");
        return query.toString();
    }

    private void cargarTablaBalanceGeneral(List<DetalleCajaMovimientos> lista) {
        UTIL.limpiarDtm(jdBalanceUI.getjTable1());
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal ingresos = BigDecimal.ZERO;
        BigDecimal egresos = BigDecimal.ZERO;
        SimpleDateFormat dateFormat = UTIL.instanceOfDATE_FORMAT();
        for (DetalleCajaMovimientos detalleCajaMovimientos : lista) {
            //los movimientos entre caja no representan un ingreso/egreso real
            if (detalleCajaMovimientos.getTipo() != DetalleCajaMovimientosController.MOVIMIENTO_CAJA) {
                if (detalleCajaMovimientos.getIngreso()) {
                    ingresos = ingresos.add(detalleCajaMovimientos.getMonto());
                } else {
                    egresos = egresos.add(detalleCajaMovimientos.getMonto());
                }
                subTotal = subTotal.add(detalleCajaMovimientos.getMonto());
            }
            UTIL.getDtm(jdBalanceUI.getjTable1()).addRow(new Object[]{
                dateFormat.format(detalleCajaMovimientos.getFecha()),
                detalleCajaMovimientos.getDescripcion(),
                detalleCajaMovimientos.getIngreso() ? detalleCajaMovimientos.getMonto() : null,
                detalleCajaMovimientos.getIngreso() ? null : detalleCajaMovimientos.getMonto(),
                UTIL.PRECIO_CON_PUNTO.format(subTotal)
            });
        }
        jdBalanceUI.getTfEfectivo().setText(UTIL.PRECIO_CON_PUNTO.format(ingresos));
        jdBalanceUI.getTfCtaCte().setText(UTIL.PRECIO_CON_PUNTO.format(egresos));
        jdBalanceUI.getTfTotal().setText(UTIL.PRECIO_CON_PUNTO.format(subTotal));
    }

    /**
     * UI para ver los registros de venta (Facturas [Contado, Cta. Cte.]), Mov. internos, etc..
     *
     * @param parent papi frame
     * @throws MessageException end user message information
     */
    public void initBalanceCompraVentaUI(JFrame parent) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        panelBalanceComprasVentas = new PanelBalanceComprasVentas();
        List<EntityWrapper<Sucursal>> s = JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales());
        if (s.isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        UTIL.loadComboBox(panelBalanceComprasVentas.getCbSucursal(), s, true);
        jdBalanceUI = new JDBalance(parent, false, panelBalanceComprasVentas);
        jdBalanceUI.setSize(700, 500);
        UTIL.getDefaultTableModel(jdBalanceUI.getjTable1(),
                new String[]{"FECHA", "DESCRIPCIÓN", "INGRESOS", "EGRESOS", "EFECTIVO", "CTA. CTE.", "TOTAL ACUM."},
                new int[]{60, 190, 60, 60, 60, 60, 60});
        jdBalanceUI.getjTable1().getColumnModel().getColumn(0).setCellRenderer(FormatRenderer.getDateRenderer());
        jdBalanceUI.getjTable1().getColumnModel().getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        jdBalanceUI.getjTable1().getColumnModel().getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        jdBalanceUI.getjTable1().getColumnModel().getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        jdBalanceUI.getjTable1().getColumnModel().getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        jdBalanceUI.getjTable1().getColumnModel().getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        //<editor-fold defaultstate="collapsed" desc="totalesCalculator">
        jdBalanceUI.getjTable1().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                BigDecimal in = BigDecimal.ZERO;
                BigDecimal eg = BigDecimal.ZERO;
                BigDecimal ef = BigDecimal.ZERO;
                BigDecimal cc = BigDecimal.ZERO;
                BigDecimal ac = BigDecimal.ZERO;
                DefaultTableModel dtm = (DefaultTableModel) jdBalanceUI.getjTable1().getModel();
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    if (dtm.getValueAt(row, 2) != null) {
                        in = in.add(BigDecimal.valueOf((Double) dtm.getValueAt(row, 2)));
                    }
                    if (dtm.getValueAt(row, 3) != null) {
                        eg = eg.add(BigDecimal.valueOf((Double) dtm.getValueAt(row, 3)));
                    }
                    if (dtm.getValueAt(row, 4) != null) {
                        ef = ef.add(BigDecimal.valueOf((Double) dtm.getValueAt(row, 4)));
                    }
                    if (dtm.getValueAt(row, 5) != null) {
                        cc = cc.add(BigDecimal.valueOf((Double) dtm.getValueAt(row, 5)));
                    }
                }
                jdBalanceUI.getTfIngresos().setText(UTIL.DECIMAL_FORMAT.format(in));
                jdBalanceUI.getTfEgresos().setText(UTIL.DECIMAL_FORMAT.format(eg));
                jdBalanceUI.getTfEfectivo().setText(UTIL.DECIMAL_FORMAT.format(ef));
                jdBalanceUI.getTfCtaCte().setText(UTIL.DECIMAL_FORMAT.format(cc));
            }
        });
        //</editor-fold>
        jdBalanceUI.setTitle("Balance");
        jdBalanceUI.getbBuscar().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!panelBalanceComprasVentas.getCheckContado().isSelected()
                            && !panelBalanceComprasVentas.getCheckCtaCte().isSelected()) {
                        throw new MessageException("Debe elegir al menos una forma de facturación (CONTADO, CTA. CTE.)");
                    }
                    cargarTablaBalanceCompraVenta();
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    LOG.error("error buscando balance compraventa", ex);
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
                }
            }
        });
        jdBalanceUI.getbImprimir().addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!panelBalanceComprasVentas.getCheckContado().isSelected()
                            && !panelBalanceComprasVentas.getCheckCtaCte().isSelected()) {
                        throw new MessageException("Debe elegir al menos una forma de facturación (CONTADO, CTA. CTE.)");
                    }
                    cargarTablaBalanceCompraVenta();
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    LOG.error("error buscando balance compraventa", ex);
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
                }
            }
        });
        jdBalanceUI.getBtnToExcel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (jdBalanceUI.getjTable1().getRowCount() < 1) {
                        throw new MessageException(JGestion.resourceBundle.getString("warn.emptytable"));
                    }
                    File file = JGestionUtils.showSaveDialogFileChooser(jdBalanceUI, "Archivo Excel (.xls)", new File("balance.xls"), "xls");
                    TableExcelExporter tee = new TableExcelExporter(file, jdBalanceUI.getjTable1());
                    tee.export();
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(buscador, "¿Abrir archivo generado?", null, JOptionPane.YES_NO_OPTION)) {
                        Desktop.getDesktop().open(file);
                    }
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    LOG.error("", ex);
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
                }
            }
        });
        jdBalanceUI.setLocationRelativeTo(parent);
        jdBalanceUI.setSize(jdBalanceUI.getWidth() + 100, jdBalanceUI.getHeight());
        jdBalanceUI.setVisible(true);
    }

    private void cargarTablaBalanceCompraVenta() throws DatabaseErrorException, MessageException {
        int index = panelBalanceComprasVentas.getCbComprasVentas().getSelectedIndex();
        List<Object[]> dataCompra = new ArrayList<>(0);
        List<Object[]> dataVenta = new ArrayList<>(0);
        if (index == 0 || index == 1) {
            String query = armarQueryBalanceComprasVentas("compra", "proveedor");
            dataCompra = getFacturaCompraList((List<FacturaCompra>) DAO.getNativeQueryResultList(query, FacturaCompra.class));
        }
        if (index == 0 || index == 2) {
            String query = armarQueryBalanceComprasVentas("venta", "cliente");
            dataVenta = getFacturaVentaList((List<FacturaVenta>) DAO.getNativeQueryResultList(query, FacturaVenta.class));
        }
        Comparator<Object[]> comparator = new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Date) o1[0]).compareTo(((Date) o2[0]));
            }
        };
        for (Object[] objects : dataVenta) {
            dataCompra.add(objects);
        }
        dataVenta.clear();
        Collections.sort(dataCompra, comparator);
        DefaultTableModel dtm = (DefaultTableModel) jdBalanceUI.getjTable1().getModel();
        dtm.setRowCount(0);
        for (Object[] objects : dataCompra) {
            dtm.addRow(objects);
        }
    }

    private String armarQueryBalanceComprasVentas(String tabla, String entidad) {

        StringBuilder query = new StringBuilder("SELECT o.*, o.fecha_" + tabla + " as fecha, ccc.entregado"
                + " FROM factura_" + tabla + " o LEFT JOIN ctacte_" + entidad + " ccc ON o.id = ccc.factura"
                + " WHERE o.id IS NOT NULL");
        if (panelBalanceComprasVentas.getCheckContado().isSelected() && panelBalanceComprasVentas.getCheckCtaCte().isSelected()) {
            //no hace falta ningún filtro.. va traer ambas
        } else {
            if (panelBalanceComprasVentas.getCheckContado().isSelected()) {
                query.append(" AND o.forma_pago = ").append(Valores.FormaPago.CONTADO.getId());
            }
            if (panelBalanceComprasVentas.getCheckCtaCte().isSelected()) {
                query.append(" AND o.forma_pago = ").append(Valores.FormaPago.CTA_CTE.getId());
            }
        }
        if (!panelBalanceComprasVentas.getCheckAnuladas().isSelected()) {
            query.append(" AND o.anulada = false");
        }
        if (panelBalanceComprasVentas.getCbSucursal().getSelectedIndex() > 0) {
            query.append(" AND o.sucursal=").append(((EntityWrapper<?>) panelBalanceComprasVentas.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < panelBalanceComprasVentas.getCbSucursal().getItemCount(); i++) {
                EntityWrapper<?> sucursal = (EntityWrapper<?>) panelBalanceComprasVentas.getCbSucursal().getItemAt(i);
                query.append(" o.sucursal=").append(sucursal.getId());
                if ((i + 1) < panelBalanceComprasVentas.getCbSucursal().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        Date fecha = panelBalanceComprasVentas.getDcDesde().getDate();
        if (fecha != null) {
            query.append(" AND o.fecha_").append(tabla).append(" >= '").append(fecha).append("'");
        }
        fecha = panelBalanceComprasVentas.getDcHasta().getDate();
        if (fecha != null) {
            query.append(" AND o.fecha_").append(tabla).append(" <= '").append(fecha).append("'");
        }

        query.append(" ORDER BY o.fecha_").append(tabla);
        LogManager.getLogger();//(Contabilidad.class).debug(query.toString());
        return query.toString();
    }

    private List<Object[]> getFacturaCompraList(List<FacturaCompra> l) throws MessageException {
        String errores = "";
        List<Object[]> data = new ArrayList<>(l.size());
        Double totalIngresos, efectivo = null, cccpc = null;
        totalIngresos = 0.0;
        Double entregado;
        for (FacturaCompra factura : l) {
            if (!factura.getAnulada()) {
                totalIngresos += factura.getImporte();
                if (Valores.FormaPago.CONTADO.getId() == factura.getFormaPago()) {
                    cccpc = null;
                    efectivo = factura.getImporte();
                } else if (Valores.FormaPago.CTA_CTE.getId() == factura.getFormaPago()) {
                    try {
                        entregado = new CtacteProveedorController().findCtacteProveedorByFactura(factura.getId()).getEntregado().doubleValue();
                        cccpc = (factura.getImporte() - entregado);
                        efectivo = entregado > 0 ? entregado : null;
                    } catch (NoResultException ex) {
                        errores += "\nFactura (id " + factura.getId() + ") N° " + JGestionUtils.getNumeracion(factura) + ", no se encontró la Cta Cte.";
                    }
                } else {
                    LOG.info("Factura=" + factura.getId() + ", FormaPago.id=" + factura.getFormaPago());
                }
            } else {
                efectivo = null;
                cccpc = null;
            }
            data.add(new Object[]{
                factura.getFechaCompra(),
                JGestionUtils.getNumeracion(factura) + (factura.getAnulada() ? "[ANULADA]" : ""),
                null,
                factura.getImporte(),
                efectivo,// != null ? efectivo : "------",
                cccpc,// != null ? cccpc : "------",
                totalIngresos
            });
        }
        if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(null, errores, "Errores de información", JOptionPane.ERROR_MESSAGE);
        }
        return data;
    }

    private List<Object[]> getFacturaVentaList(List<FacturaVenta> l) {
        List<Object[]> data = new ArrayList<Object[]>(l.size());
        Double efectivo = null, cccpc = null;
        Double totalIngresos = 0.0;
        Double entregado;
        for (FacturaVenta factura : l) {
            if (!factura.getAnulada()) {
                totalIngresos += factura.getImporte().doubleValue();
                if (Valores.FormaPago.CONTADO.getId() == factura.getFormaPago()) {
                    cccpc = null;
                    efectivo = factura.getImporte().doubleValue();
                } else if (Valores.FormaPago.CTA_CTE.getId() == factura.getFormaPago()) {
                    entregado = new CtacteClienteController().findBy(factura).getEntregado();
                    double importe = factura.getImporte().doubleValue();
                    cccpc = (importe - entregado);
                    efectivo = entregado > 0 ? entregado : null;
                } else {
                    LogManager.getLogger();//(Contabilidad.class).warn("FormaPago DESCONOCIDA = " + factura.getFormaPago() + ", FacturaVenta.id=" + factura.getId());
                }
            } else {
                efectivo = null;
                cccpc = null;
            }
            data.add(new Object[]{
                factura.getFechaVenta(),
                JGestionUtils.getNumeracion(factura) + (factura.getAnulada() ? "[ANULADA]" : ""),
                factura.getImporte(),
                null,
                efectivo,// != null ? efectivo : "------",
                cccpc,// != null ? cccpc : "------",
                totalIngresos
            });
        }
        return data;
    }

    private void doReportBalanceCompraVenta(String query, Date desde, Date hasta,
            String title, boolean contado, boolean ctacte, boolean anuladas)
            throws JRException, MissingReportException {
        Reportes r = new Reportes("Jgestion_balance_factucv.jasper", "Balance de " + title);
        r.addParameter("CURRENT_USER", UsuarioController.getCurrentUser().getNick());
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

    /**
     * Calcula el precio final del producto con o sin IVA, teniendo en cuenta el margen segun la
     * lista de precios.
     *
     * @param producto Del cual se obtendrá el {@code producto.getPrecioVenta()} y
     * {@linkplain Producto#getIva()}
     * @param listaPrecios
     * @param incluirIVA
     * @return precioFinal incluido IVA
     */
    public static Double getPrecioFinal(Producto producto, ListaPrecios listaPrecios, boolean incluirIVA) {
        //      LogManager.getLogger();//(this.getClass()).debug("Producto:" + producto.getNombre() + ", $venta:"+producto.getPrecioVenta() + ", ListaPrecio:" + listaPrecios.getNombre());
        Double precioFinal = (producto.getPrecioVenta().doubleValue()
                + GET_MARGEN_SEGUN_LISTAPRECIOS(listaPrecios, producto, null));
        Double iva = 0.0;
        if (incluirIVA) {
            iva = UTIL.getPorcentaje(precioFinal, producto.getIva().getIva());
        }
        return precioFinal + iva;
    }

    /**
     *
     * @param monto
     * @param tipoDeMargen 1 = %, 2 = $
     * @param margen
     * @return
     */
    public static BigDecimal GET_MARGEN(BigDecimal monto, int tipoDeMargen, BigDecimal margen) {
        if (margen.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal total;
        switch (tipoDeMargen) {
            case 1: { // margen en %
                total = monto.multiply(margen).divide(new BigDecimal("100"));
                break;
            }
            case 2: { // margen en $ (monto fijo).. no hay mucha science...
                total = margen;
                break;
            }
            default:
                return null;
        }
        return total;
    }

    /**
     * Calcula el margen monetario ganancia/perdida sobre el monto.
     *
     * @param monto cantidad monetaria sobre la cual se hará el cálculo
     * @param tipoDeMargen Indica como se aplicará el margen al monto. If
     * <code>(tipo > 2 || tipo &lt; 1)</code> will return <code>null</code>.
     * <lu> <li>1 = % (porcentaje) <li>2 = $ (monto fijo) <lu>
     * @param margen monto fijo o porcentual.
     * @return El margen de ganancia correspondiente al monto.
     */
    public static Double GET_MARGEN(double monto, int tipoDeMargen, double margen) {
        if (margen == 0) {
            return 0.0;
        }
        double total;
        switch (tipoDeMargen) {
            case 1: { // margen en %
                total = ((monto * margen) / 100);
                break;
            }
            case 2: { // margen en $ (monto fijo).. no hay mucha science...
                total = margen;
                break;
            }
            default:
                return null;
        }
        return total;
    }

    /**
     * Calcula el margen de ganancia sobre el monto según la {@link ListaPrecios} seleccionada.
     *
     * @param listaPrecios
     * @param producto {@link Producto} del cual se tomarán los {@link Rubro} y Sub para determinar
     * el margen de ganancia if {@link ListaPrecios#margenGeneral} == FALSE.
     * @param monto sobre el cual se va calcular el margen (suele incluir el margen individual de
     * ganancia de cada producto). Si es == null, se utilizará {@link Producto#precioVenta}
     * @return margen de ganancia (NO INCLUYE EL MONTO).
     */
    public static Double GET_MARGEN_SEGUN_LISTAPRECIOS(ListaPrecios listaPrecios, Producto producto, Double monto) {
        double margenFinal = 0.0;
        if (listaPrecios.getMargenGeneral()) {
            margenFinal = listaPrecios.getMargen();
        } else {
            boolean encontro = false;
            Rubro rubro = producto.getRubro();
            Rubro subRubro = producto.getSubrubro();
            List<DetalleListaPrecios> detalleListaPreciosList = listaPrecios.getDetalleListaPreciosList();
            //si no se encuentra el Rubro en el DetalleListaPrecio, margen permanecerá en 0
            for (DetalleListaPrecios dlp : detalleListaPreciosList) {
                double margenEncontrado = 0.0;
                //si el Rubro del Producto coincide con un Rubro de la ListaPrecios
                if (dlp.getRubro().equals(rubro)) {
                    encontro = true;
                    margenEncontrado = dlp.getMargen();
                } else {
                    //si el subRubro coincide con algún Rubro definido en la ListaPrecios
                    if (!encontro && subRubro != null) {
                        if (dlp.getRubro().equals(subRubro)) {
                            margenEncontrado = dlp.getMargen();
                        }
                    }
                }
                if (margenEncontrado > margenFinal) {
                    /*
                     * Puede que la listaPrecios tenga determinado margenes de
                     * ganancia tanto para el Rubro como SubRubro de un
                     * Producto, entonces solo tomamos el mayor de ellos para
                     * aplicarlo.
                     */
                    margenFinal = margenEncontrado;
                }
            }
        }
        Double montoDefinitivo = (monto == null ? producto.getPrecioVenta().doubleValue() : monto);
        return ((montoDefinitivo * margenFinal) / 100);
    }

    /**
     * Retorna el estado del cheque como String. <br>1 = ENTREGADO (estado exclusivo de
     * {@link ChequePropio}). <br>2 = CARTERA (estado exclusivo de {@link ChequeTerceros}). <br>3 =
     * DEPOSTADO. <br>4 = CAJA (cheque que se convirtió en efectivo y se asentó en alguna caja)
     * <br>5 = RECHAZADO.
     *
     * @param estadoID
     * @return String que representa el estado.
     * @throws IllegalArgumentException si no existe el estadoID
     */
    public static String getChequeEstadoToString(int estadoID) {
        String estado;
        switch (estadoID) {
            case 1: {
                estado = "ENTREGADO";
                break;
            }
            case 2: {
                estado = "CARTERA";
                break;
            }
            case 3: {
                estado = "DEPOSITADO";
                break;
            }
            case 4: {
                estado = "CAJA";
                break;
            }
            case 5: {
                estado = "RECHAZADO";
                break;
            }

            default: {
                throw new IllegalArgumentException("Estado de CHEQUE no definido");
            }
        }
        return estado;
    }

    public static BigDecimal parse(double d) {
        BigDecimal big = BigDecimal.valueOf(d);
        big = big.setScale(2);
        return big;
    }

    public void displayInformeComprobantesVenta(Window owner, final boolean soloMovimientosInternos) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        buscadorReRe = new JDBuscadorReRe(owner, "Informe - Comprobantes Ventas", false, "Cliente", "Nº Factura");
        buscadorReRe.getjTable1().setAutoCreateRowSorter(true);
        buscadorReRe.hideFactura();
        buscadorReRe.hideUDNCuentaSubCuenta();
        buscadorReRe.hideVendedor();
        buscadorReRe.getbImprimir().setVisible(true);
        UTIL.loadComboBox(buscadorReRe.getCbClieProv(), JGestionUtils.getWrappedClientes(new ClienteController().findAll()), true);
        UTIL.loadComboBox(buscadorReRe.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        List<EntityWrapper<Sucursal>> sucus = JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales());
        if (sucus.isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        UTIL.loadComboBox(buscadorReRe.getCbSucursal(), sucus, true);
        UTIL.loadComboBox(buscadorReRe.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        UTIL.getDefaultTableModel(
                buscadorReRe.getjTable1(),
                new String[]{"Nº y Tipo", "Fecha", "Cliente", "CUIT", "Gravado", "IVA105", "IVA21", "IVA27", "Otros IVA's", "No Gravado", "Descuento", "Importe"},
                new int[]{90, 50, 50, 60, 50, 50, 50, 50, 50, 70, 60, 60},
                new Class<?>[]{null, null, null, Long.class, null, null, null, null, null, null, null, null});
        TableColumnModel tc = buscadorReRe.getjTable1().getColumnModel();
        tc.getColumn(1).setCellRenderer(FormatRenderer.getDateRenderer());
        tc.getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(8).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(9).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(10).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(11).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscadorReRe.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buscadorReRe.getjTable1().getRowCount() > 0) {
                    try {
                        doComprobantesVentaReport();
                    } catch (MissingReportException | JRException ex) {
                        buscadorReRe.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    buscadorReRe.showMessage("No hay comprobantes filtrados, utilice diferentes filtros para obtener resultados", "Nada que imprimir", JOptionPane.WARNING_MESSAGE);
                }
            }

            private void doComprobantesVentaReport() throws MissingReportException, JRException {
                List<GenericBeanCollection> data = new ArrayList<GenericBeanCollection>(buscadorReRe.getjTable1().getRowCount());
                DefaultTableModel dtm = buscadorReRe.getDtm();
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    data.add(new GenericBeanCollection(
                            dtm.getValueAt(row, 0), dtm.getValueAt(row, 1), dtm.getValueAt(row, 2), dtm.getValueAt(row, 3),
                            dtm.getValueAt(row, 4), dtm.getValueAt(row, 5), dtm.getValueAt(row, 6), dtm.getValueAt(row, 7),
                            dtm.getValueAt(row, 8), dtm.getValueAt(row, 9), dtm.getValueAt(row, 10), dtm.getValueAt(row, 11)));
                }
                Reportes r = new Reportes("JGestion_ComprobantesVentas.jasper", "Listado Comprobantes");
                r.setDataSource(data);
                r.addMembreteParameter();
                r.addConnection();
                r.viewReport();
            }
        });
        buscadorReRe.getBtnToExcel().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        buscadorReRe.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<FacturaVenta> data = getComprobantesVenta(soloMovimientosInternos);
                    DefaultTableModel dtm = (DefaultTableModel) buscadorReRe.getjTable1().getModel();
                    dtm.setRowCount(0);
                    for (FacturaVenta facturaVenta : data) {
                        dtm.addRow(new Object[]{
                            JGestionUtils.getNumeracion(facturaVenta),
                            facturaVenta.getFechaVenta(),
                            facturaVenta.getCliente().getNombre(),
                            facturaVenta.getCliente().getNumDoc(),
                            facturaVenta.getGravado(),
                            new BigDecimal(facturaVenta.getIva10()),
                            new BigDecimal(facturaVenta.getIva21()),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            facturaVenta.getNoGravado(),
                            new BigDecimal(facturaVenta.getDescuento()),
                            facturaVenta.getImporte()
                        });
                    }
                } catch (MessageException ex) {
                    ex.displayMessage(buscadorReRe);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscadorReRe, ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    LOG.error(ex, ex);
                }
            }
        });
        buscadorReRe.setLocationRelativeTo(owner);
        buscadorReRe.setVisible(true);
    }

    public void displayInformeComprobantesCompra(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        buscadorReRe = new JDBuscadorReRe(owner, "Buscador - Comprobantes Compra", false, "Proveedor", "Nº Factura");
        buscadorReRe.getjTable1().setAutoCreateRowSorter(true);
        buscadorReRe.hideCaja();
        buscadorReRe.hideFactura();
        buscadorReRe.hideCheckAnulado();
        buscadorReRe.getbImprimir().setVisible(true);
        UTIL.loadComboBox(buscadorReRe.getCbClieProv(), JGestionUtils.getWrappedProveedores(new ProveedorJpaController().findAllLite()), true);
        UTIL.loadComboBox(buscadorReRe.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        List<EntityWrapper<Sucursal>> sucus = JGestionUtils.getWrappedSucursales(new UsuarioHelper().getSucursales());
        if (sucus.isEmpty()) {
            throw new MessageException(JGestion.resourceBundle.getString("unassigned.sucursal"));
        }
        UTIL.loadComboBox(buscadorReRe.getCbSucursal(), sucus, true);
        buscadorReRe.getLabelFormasDePago().setText("Tipo");
        UTIL.loadComboBox(buscadorReRe.getCbFormasDePago(), FacturaCompraController.TIPOS_FACTURA, true);
        UTIL.getDefaultTableModel(
                buscadorReRe.getjTable1(),
                new String[]{"Nº y Tipo", "Fecha", "Proveedor", "CUIT", "Gravado", "IVA105", "IVA21", "Otros IVA's", "Perc. IIBB", "Perc. IVA", "Otros Imp.", "No Recup", "No Gravado", "Descuento", "Importe"},
                new int[]{90, 50, 50, 60, 50, 50, 50, 50, 50, 50, 70, 60, 60, 60, 60},
                new Class<?>[]{null, null, null, Long.class, null, null, null, null, null, null, null, null, null, null, null});
        TableColumnModel tc = buscadorReRe.getjTable1().getColumnModel();
        tc.getColumn(1).setCellRenderer(FormatRenderer.getDateRenderer());
        tc.getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(8).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(9).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(10).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(11).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(12).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(13).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(14).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscadorReRe.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buscadorReRe.getjTable1().getRowCount() > 0) {
                    try {
                        doComprobantesCompraReport();
                    } catch (MissingReportException ex) {
                        buscadorReRe.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (JRException ex) {
                        buscadorReRe.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    buscadorReRe.showMessage("No hay comprobantes filtrados, utilice diferentes filtros para obtener resultados", "Nada que imprimir", JOptionPane.WARNING_MESSAGE);
                }
            }

            private void doComprobantesCompraReport() throws MissingReportException, JRException {
                List<GenericBeanCollection> data = new ArrayList<GenericBeanCollection>(buscadorReRe.getjTable1().getRowCount());
                DefaultTableModel dtm = buscadorReRe.getDtm();
                for (int row = 0; row < dtm.getRowCount(); row++) {
                    data.add(new GenericBeanCollection(
                            dtm.getValueAt(row, 0),
                            dtm.getValueAt(row, 1),
                            dtm.getValueAt(row, 2),
                            dtm.getValueAt(row, 3),
                            dtm.getValueAt(row, 4),
                            dtm.getValueAt(row, 5),
                            dtm.getValueAt(row, 6),
                            dtm.getValueAt(row, 8),
                            dtm.getValueAt(row, 9),
                            dtm.getValueAt(row, 10),
                            dtm.getValueAt(row, 11),
                            dtm.getValueAt(row, 13),
                            dtm.getValueAt(row, 14),
                            null));
                }
                Reportes r = new Reportes("JGestion_ComprobantesCompras.jasper", "Listado Comprobantes");
                r.setDataSource(data);
                r.addMembreteParameter();
                r.addConnection();
                r.viewReport();
            }
        });
        buscadorReRe.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<?> data = getComprobantesCompra();
                    DefaultTableModel dtm = buscadorReRe.getDtm();
                    dtm.setRowCount(0);
                    for (Object o : data) {
                        dtm.addRow((Object[]) o);

                    }
                } catch (MessageException ex) {
                    ex.displayMessage(buscadorReRe);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscadorReRe, ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    LOG.error(ex, ex);
                }
            }
        });
        buscadorReRe.setLocationRelativeTo(owner);
        buscadorReRe.setVisible(true);
    }

    @SuppressWarnings("unchecked")
    private List<?> getComprobantesCompra() throws MessageException, DatabaseErrorException {
        StringBuilder queryFactuCompra = new StringBuilder(300);
        StringBuilder queryNotaCredito = new StringBuilder(300);

        long numero;
        //filtro por nº de ReRe
        if (buscadorReRe.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscadorReRe.getTfOcteto());
                queryFactuCompra.append(" AND o.numero = ").append(numero);
                queryNotaCredito.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de comprobante no válido");
            }
        }

        //filtro por nº de factura
        if (buscadorReRe.getTfCuarto().length() > 0 && buscadorReRe.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscadorReRe.getTfFactu4() + buscadorReRe.getTfFactu8());
                queryFactuCompra.append(" AND o.numero = ").append(numero);
                queryNotaCredito.append(" AND sucursal.puntoventa = ").append(buscadorReRe.getTfFactu4());
                queryNotaCredito.append(" AND o.numero = ").append(buscadorReRe.getTfFactu8());
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de comprobante no válido");
            }
        }
        if (buscadorReRe.getDcDesde() != null) {
            queryFactuCompra.append(" AND o.fecha_compra >= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesde())).append("'");
            queryNotaCredito.append(" AND o.fecha_nota_credito >= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesde())).append("'");
        }
        if (buscadorReRe.getDcHasta() != null) {
            queryFactuCompra.append(" AND o.fecha_compra <= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcHasta())).append("'");
            queryNotaCredito.append(" AND o.fecha_nota_credito <= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcHasta())).append("'");
        }
        if (buscadorReRe.getDcDesdeSistema() != null) {
            queryFactuCompra.append(" AND o.fechaalta >= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesdeSistema())).append("'");
            queryNotaCredito.append(" AND o.fecha_Carga >= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesdeSistema())).append("'");
        }
        if (buscadorReRe.getDcHastaSistema() != null) {
            queryFactuCompra.append(" AND o.fechaalta <= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcHastaSistema())).append("'");
            queryNotaCredito.append(" AND o.fecha_Carga <= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcHastaSistema())).append("'");
        }
        if (buscadorReRe.getCbFormasDePago().getSelectedIndex() > 0) {
            queryFactuCompra.append(" AND o.tipo = '").append(buscadorReRe.getCbFormasDePago().getSelectedItem()).append("'");
        }
        UsuarioHelper usuarioHelper = new UsuarioHelper();
        if (buscadorReRe.getCbCaja().getSelectedIndex() > 0) {
            queryFactuCompra.append(" AND o.caja = ").append(((Caja) buscadorReRe.getCbCaja().getSelectedItem()).getId());
        } else {
            queryFactuCompra.append(" AND (");
            Iterator<Caja> iterator = usuarioHelper.getCajas(Boolean.TRUE).iterator();
            while (iterator.hasNext()) {
                Caja caja = iterator.next();
                queryFactuCompra.append("o.caja=").append(caja.getId());
                if (iterator.hasNext()) {
                    queryFactuCompra.append(" OR ");
                }
            }
            queryFactuCompra.append(")");
        }
        if (buscadorReRe.getCbSucursal().getSelectedIndex() > 0) {
            queryFactuCompra.append(" AND o.sucursal = ").append(((EntityWrapper<?>) buscadorReRe.getCbSucursal().getSelectedItem()).getId());
        } else {
            queryFactuCompra.append(" AND (");
            for (int i = 1; i < buscadorReRe.getCbSucursal().getItemCount(); i++) {
                EntityWrapper<Sucursal> cbw = (EntityWrapper<Sucursal>) buscadorReRe.getCbSucursal().getItemAt(i);
                queryFactuCompra.append(" o.sucursal=").append(cbw.getId());
                if ((i + 1) < buscadorReRe.getCbSucursal().getItemCount()) {
                    queryFactuCompra.append(" OR ");
                }
            }
            queryFactuCompra.append(")");
        }

        if (buscadorReRe.getCbClieProv().getSelectedIndex() > 0) {
            queryFactuCompra.append(" AND o.proveedor = ").append(((EntityWrapper<Proveedor>) buscadorReRe.getCbClieProv().getSelectedItem()).getId());
        }

        String sql
                = "SELECT com.* FROM ("
                + " SELECT 'F' || o.tipo || to_char(o.numero, '0000-00000000') as comprobante,	o.fecha_compra as fecha, proveedor.nombre, proveedor.cuit,"
                + " cast(case when o.gravado <=0 then (o.importe-o.iva10-o.iva21-o.perc_iva-o.impuestos_recuperables) else o.gravado end as numeric(12,2)),	cast(o.iva10 as numeric(12,2)),	cast(o.iva21 as numeric(12,2)), o.otros_ivas, "
                + "	cast(o.perc_dgr as numeric(12,2)), cast(o.perc_iva as numeric(12,2)), cast( o.impuestos_recuperables as numeric(12,2)), cast( o.impuestos_norecuperables as numeric(12,2)), cast( o.no_gravado as numeric(12,2)), cast( o.descuento as numeric(12,2)), cast( o.importe as numeric(12,2))"
                + " FROM factura_compra o, proveedor"
                + " WHERE o.anulada = false AND o.proveedor = proveedor.id "
                + queryFactuCompra.toString()
                + " ORDER BY"
                + " o.fecha_compra ASC) com"
                + " UNION ("
                + " SELECT 'NC' || to_char(sucursal.puntoventa, '0000') || to_char(o.numero,'-00000000'), o.fecha_nota_credito as fecha, cliente.nombre, cliente.num_doc,"
                + " cast(o.gravado as numeric(12,2)), cast(o.iva10 as numeric(12,2)), cast(o.iva21 as numeric(12,2)), cast(o.impuestos_recuperables as numeric(12,2)),"
                + " cast(0 as numeric(12,2)), cast(0 as numeric(12,2)), cast(0 as numeric(12,2)), cast(0 as numeric(12,2)), cast(o.no_gravado as numeric(12,2)), cast(0 as numeric(12,2)) as descuento, cast(o.importe as numeric(12,2))"
                + " FROM nota_credito o, cliente, sucursal"
                + " WHERE o.anulada = false AND o.cliente = cliente.id AND o.sucursal = sucursal.id"
                + queryNotaCredito.toString()
                + " ORDER BY"
                + " o.fecha_nota_credito ASC)"
                + " ORDER BY fecha";
        System.out.println("QUERY: " + sql);
        List<?> l = DAO.getNativeQueryResultList(sql, (String) null);
        return l;
    }

    private List<FacturaVenta> getComprobantesVenta(boolean soloMovimientosInternos) throws MessageException, DatabaseErrorException {
        StringBuilder queryWhereFactuVenta = new StringBuilder(300).append(" o.");
        if (soloMovimientosInternos) {
            queryWhereFactuVenta.append(FacturaVenta_.tipo.getName()).append(" = 'I'");
        } else {
            queryWhereFactuVenta.append(FacturaVenta_.tipo.getName()).append(" <> 'I'");
        }
        StringBuilder queryWhereNotaCredito = new StringBuilder(300).append(" o.id is not null");

        long numero;
        //filtro por nº de comprobante
        if (buscadorReRe.getTfCuarto().length() > 0) {
            try {
                numero = Long.parseLong(buscadorReRe.getTfCuarto());
                queryWhereFactuVenta.append(" AND o.sucursal.puntoVenta = ").append(numero);
                queryWhereNotaCredito.append(" AND o.sucursal.puntoVenta = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de Punto de Venta de comprobante no válido");
            }
        }
        if (buscadorReRe.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscadorReRe.getTfOcteto());
                if (soloMovimientosInternos) {
                    queryWhereFactuVenta.append(" AND o.").append(FacturaVenta_.movimientoInterno.getName()).append(" = ").append(numero);
                } else {
                    queryWhereFactuVenta.append(" AND o.").append(FacturaVenta_.numero.getName()).append(" = ").append(numero);
                }
                queryWhereNotaCredito.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de comprobante no válido");
            }
        }

        if (buscadorReRe.getDcDesde() != null) {
            queryWhereFactuVenta.append(" AND o.fechaVenta >= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesde())).append("'");
            queryWhereNotaCredito.append(" AND o.fechaNotaCredito >= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesde())).append("'");
        }
        if (buscadorReRe.getDcHasta() != null) {
            queryWhereFactuVenta.append(" AND o.fechaVenta <= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcHasta())).append("'");
            queryWhereNotaCredito.append(" AND o.fechaNotaCredito <= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesde())).append("'");
        }
        if (buscadorReRe.getDcDesdeSistema() != null) {
            queryWhereFactuVenta.append(" AND o.fechaalta >= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesdeSistema())).append("'");
            queryWhereNotaCredito.append(" AND o.fechaCarga >= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcDesdeSistema())).append("'");
        }
        if (buscadorReRe.getDcHastaSistema() != null) {
            queryWhereFactuVenta.append(" AND o.fechaalta <= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcHastaSistema())).append("'");
            queryWhereNotaCredito.append(" AND o.fechaCarga <= '").append(UTIL.yyyy_MM_dd.format(buscadorReRe.getDcHastaSistema())).append("'");
        }
        UsuarioHelper usuarioHelper = new UsuarioHelper();
        if (buscadorReRe.getCbCaja().getSelectedIndex() > 0) {
            queryWhereFactuVenta.append(" AND o.caja.id = ").append(((Caja) buscadorReRe.getCbCaja().getSelectedItem()).getId());
        } else {
            queryWhereFactuVenta.append(" AND (");
            Iterator<Caja> iterator = usuarioHelper.getCajas(Boolean.TRUE).iterator();
            while (iterator.hasNext()) {
                Caja caja = iterator.next();
                queryWhereFactuVenta.append("o.caja.id=").append(caja.getId());
                if (iterator.hasNext()) {
                    queryWhereFactuVenta.append(" OR ");
                }
            }
            queryWhereFactuVenta.append(")");
        }
        if (buscadorReRe.getCbSucursal().getSelectedIndex() > 0) {
            queryWhereFactuVenta.append(" AND o.sucursal.id = ").append(((EntityWrapper<?>) buscadorReRe.getCbSucursal().getSelectedItem()).getId());
        } else {
            queryWhereFactuVenta.append(" AND (");
            for (int i = 1; i < buscadorReRe.getCbSucursal().getItemCount(); i++) {
                EntityWrapper<Sucursal> cbw = (EntityWrapper<Sucursal>) buscadorReRe.getCbSucursal().getItemAt(i);
                queryWhereFactuVenta.append(" o.sucursal.id=").append(cbw.getId());
                if ((i + 1) < buscadorReRe.getCbSucursal().getItemCount()) {
                    queryWhereFactuVenta.append(" OR ");
                }
            }
            queryWhereFactuVenta.append(")");
        }

        if (buscadorReRe.getCbClieProv().getSelectedIndex() > 0) {
            queryWhereFactuVenta.append(" AND o.cliente.id = ").append(((EntityWrapper<Cliente>) buscadorReRe.getCbClieProv().getSelectedItem()).getId());
        }

        queryWhereFactuVenta.append(" AND o.anulada = ").append(buscadorReRe.getCheckAnulada().isSelected());
        queryWhereNotaCredito.append(" AND o.anulada = ").append(buscadorReRe.getCheckAnulada().isSelected());

        System.out.println("QUERY: " + queryWhereFactuVenta.toString());
        @SuppressWarnings("unchecked")
        List<FacturaVenta> l = (List<FacturaVenta>) DAO.findEntities(FacturaVenta.class, queryWhereFactuVenta.toString());
        List<NotaCredito> ln = (List<NotaCredito>) DAO.findEntities(NotaCredito.class, queryWhereNotaCredito.toString());
        return l;
    }

    public void displayMovimientosGenerales(Window owner) {
    }

    public void displayCtaCteGeneralResumen(Window window) {
        final JDResumenGeneralCtaCte resumenGeneralCtaCte = new JDResumenGeneralCtaCte(window, false);
        UTIL.hideColumnTable(resumenGeneralCtaCte.getjXTable1(), 0);
        resumenGeneralCtaCte.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Date desde = resumenGeneralCtaCte.getDcDesde().getDate();
                Date hasta = resumenGeneralCtaCte.getDcHasta().getDate();
                List<Object[]> data;
                if (resumenGeneralCtaCte.getCbClieProv().getSelectedIndex() == 0) {
                    data = new CtacteClienteController().findSaldos(desde, hasta);
                    cargarTablaResumenGeneralCtaCte(data);
                } else {
                    data = new CtacteProveedorController().findSaldos(desde, hasta);
                    cargarTablaResumenGeneralCtaCte(data);
                }
            }

            private void cargarTablaResumenGeneralCtaCte(List<Object[]> data) {
                DefaultTableModel dtm = (DefaultTableModel) resumenGeneralCtaCte.getjXTable1().getModel();
                dtm.setRowCount(0);
                BigDecimal t = BigDecimal.ZERO;
                for (Object[] o : data) {
                    dtm.addRow(o);
                    t = t.add((BigDecimal) o[2]);
                }
                resumenGeneralCtaCte.getTfTotal().setText(UTIL.DECIMAL_FORMAT.format(t));
            }
        });
        resumenGeneralCtaCte.getjXTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    try {
                        int selectedRow = resumenGeneralCtaCte.getjXTable1().getSelectedRow();
                        if (resumenGeneralCtaCte.getCbClieProv().getSelectedIndex() == 0) {
                            Cliente o = new ClienteJpaController().find((Integer) resumenGeneralCtaCte.getjXTable1().getModel().getValueAt(selectedRow, 0));
                            new CtacteClienteController().getResumenCtaCte(resumenGeneralCtaCte, true, o).setVisible(true);
                        } else {
                            Proveedor o = new ProveedorController().findProveedor((Integer) resumenGeneralCtaCte.getjXTable1().getModel().getValueAt(selectedRow, 0));
                            new CtacteProveedorController().getResumenCtaCte(resumenGeneralCtaCte, true, o).setVisible(true);
                        }
                    } catch (MessageException | MissingReportException | JRException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "", JOptionPane.WARNING_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "", JOptionPane.ERROR_MESSAGE);
                        LOG.error("double click en resumenGeneralCtaCte", ex);
                    }
                }
            }
        });
        resumenGeneralCtaCte.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<GenericBeanCollection> data = new ArrayList<>(resumenGeneralCtaCte.getjXTable1().getRowCount());
                    DefaultTableModel dtm = (DefaultTableModel) resumenGeneralCtaCte.getjXTable1().getModel();
                    for (int row = 0; row < dtm.getRowCount(); row++) {
                        data.add(new GenericBeanCollection(
                                dtm.getValueAt(row, 1),
                                dtm.getValueAt(row, 2)
                        ));
                    }
                    Reportes r = new Reportes("ResumenCtaCteGeneral.jasper", "Resumen Cta Cte General");
                    r.addParameter("CLI_O_PRO", resumenGeneralCtaCte.getCbClieProv().getSelectedItem().toString());
                    r.setDataSource(data);
                    r.addMembreteParameter();
                    r.addConnection();
                    r.viewReport();
                } catch (MissingReportException ex) {
                    JOptionPane.showMessageDialog(resumenGeneralCtaCte, ex.getMessage());
                } catch (JRException ex) {
                    LOG.error("error en reporte: ResumenCtaCteGeneral", ex);
                    JOptionPane.showMessageDialog(resumenGeneralCtaCte, ex.getMessage());
                }
            }
        });
        resumenGeneralCtaCte.setVisible(true);
    }

    public void showInformePorUnidadesDeNegocios(Window owner) {
        new JDInformeUnidadesDeNegocios(owner).setVisible(true);
    }

    public void displayProductosCostoVenta(Window owner) {
        panelito = new PanelProductosCostoVenta();
        UTIL.loadComboBox(panelito.getCbMarcas(), JGestionUtils.getWrappedMarcas(new MarcaController().findAll()), true);
        UTIL.loadComboBox(panelito.getCbRubros(), JGestionUtils.getWrappedRubros(new RubroController().findRubros()), true);
        UTIL.loadComboBox(panelito.getCbSubRubros(), JGestionUtils.getWrappedRubros(new RubroController().findRubros()), true);
        UTIL.loadComboBox(panelito.getCbVendedores(), JGestionUtils.getWrappedVendedor(new VendedorJpaController().findAll()), true);
        UTIL.loadComboBox(panelito.getCbClientes(), JGestionUtils.getWrappedClientes(new ClienteController().findAll()), true);
        UTIL.loadComboBox(panelito.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        buscador = new JDBuscador(owner, "Productos: Costo / Venta", false, panelito);
        buscador.getBtnImprimir().setEnabled(false);
        buscador.getBtnToExcel().setEnabled(false);
        buscador.getPanelInferior().setVisible(true);
        buscador.addResumeItem("Costo", new JTextField(8));
        buscador.addResumeItem("Venta", new JTextField(8));
        buscador.addResumeItem("Ve - Co", new JTextField(8));
//        buscador.agrandar(200, 0);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Factura.id", "Cliente", "Factura", "Fecha", "Vendedor", "Producto", "Rubro", "SubRubro", "Marca", "Costo Compra", "P. Venta", "Venta-Costo", "Cantidad", "Rentabilidad"},
                new int[]{1, 100, 100, 60, 100, 80, 80, 60, 80, 80, 80, 80, 20, 80},
                new Class<?>[]{Integer.class, null, null, Date.class, null, null, null, null, null, BigDecimal.class, BigDecimal.class, BigDecimal.class, Integer.class, BigDecimal.class});
        TableColumnModel tm = buscador.getjTable1().getColumnModel();
        tm.getColumn(3).setCellRenderer(FormatRenderer.getDateRenderer());
        tm.getColumn(9).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        tm.getColumn(10).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        tm.getColumn(11).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        tm.getColumn(12).setCellRenderer(NumberRenderer.getIntegerRenderer());
        tm.getColumn(13).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().setAutoCreateRowSorter(true);
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String query = armarQueryProductosCostoVenta();
                    cargarTablaProductosCostoVenta(query);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage());
                    LOG.error("Error en cargarTablaMovimientosProductos()", ex);
                }

            }
        });
        buscador.setVisible(true);
    }

    @SuppressWarnings({"unchecked", "unchecked"})
    private String armarQueryProductosCostoVenta() {
        StringBuilder sb = new StringBuilder(
                "SELECT fv.id, fv.cliente.nombre,"
                + " CONCAT(fv.tipo, SUBSTRING(CONCAT(10000+fv.sucursal.puntoVenta,''), 2), '-', SUBSTRING(CONCAT(100000000+fv.numero,''),2)), "
                + "fv.fechaVenta, CONCAT(v.apellido,' ', v.nombre), p.nombre, p.rubro.nombre, sr.nombre,"
                + " p.marca.nombre, dv.costoCompra, dv.precioUnitario, dv.precioUnitario - dv.costoCompra,"
                + " dv.cantidad, (dv.cantidad * (dv.precioUnitario - dv.costoCompra))"
                + " FROM " + FacturaVenta.class.getSimpleName() + " fv"
                + " JOIN fv.detallesVentaList dv"
                + " JOIN dv.producto p"
                + " LEFT JOIN p.subrubro sr"
                + " LEFT JOIN fv.vendedor v"
                + " WHERE fv.id IS NOT NULL");
        if (panelito.getCbMarcas().getSelectedIndex() > 0) {
            sb.append(" AND p.marca.id = ").append(((EntityWrapper<Marca>) panelito.getCbMarcas().getSelectedItem()).getId());
        }
        if (panelito.getCbRubros().getSelectedIndex() > 0) {
            sb.append(" AND p.").append(Producto_.rubro.getName()).append(".id = ").append(((EntityWrapper<Rubro>) panelito.getCbRubros().getSelectedItem()).getId());
        }
        if (panelito.getCbSubRubros().getSelectedIndex() > 0) {
            sb.append(" AND p.").append(Producto_.subrubro.getName()).append(".id = ").append(((EntityWrapper<Rubro>) panelito.getCbSubRubros().getSelectedItem()).getId());
        }
        if (panelito.getCbClientes().getSelectedIndex() > 0) {
            sb.append(" AND fv.cliente.id = ").append(((EntityWrapper<Sucursal>) panelito.getCbClientes().getSelectedItem()).getId());
        }
        if (panelito.getCbVendedores().getSelectedIndex() > 0) {
            sb.append(" AND fv.vendedor.id = ").append(((EntityWrapper<?>) panelito.getCbVendedores().getSelectedItem()).getId());
        }
        if (panelito.getCbFormasDePago().getSelectedIndex() > 0) {
            sb.append(" AND fv.formaPago = ").append(((Valores.FormaPago) panelito.getCbFormasDePago().getSelectedItem()).getId());
        }
        if (panelito.getDcDesde() != null) {
            sb.append(" AND fv.fechaVenta >= '").append(UTIL.yyyy_MM_dd.format(panelito.getDcDesde())).append("'");
        }
        if (panelito.getDcHasta() != null) {
            sb.append(" AND fv.fechaVenta <= '").append(UTIL.yyyy_MM_dd.format(panelito.getDcHasta())).append("'");
        }
        return sb.toString();
    }

    private void cargarTablaProductosCostoVenta(String query) {
        DefaultTableModel dtm = buscador.getDtm();
        dtm.setRowCount(0);
        BigDecimal costo = BigDecimal.ZERO;
        BigDecimal venta = BigDecimal.ZERO;
        BigDecimal rentabilidad = BigDecimal.ZERO;
        @SuppressWarnings("unchecked")
        List<Object[]> l = DAO.getEntityManager().createQuery(query).getResultList();
        for (Object[] o : l) {
            dtm.addRow(o);
            costo = costo.add((BigDecimal) dtm.getValueAt(dtm.getRowCount() - 1, dtm.getColumnCount() - 4));
            venta = venta.add((BigDecimal) dtm.getValueAt(dtm.getRowCount() - 1, dtm.getColumnCount() - 3));
            rentabilidad = rentabilidad.add((BigDecimal) dtm.getValueAt(dtm.getRowCount() - 1, dtm.getColumnCount() - 1));
        }
        buscador.getResumeItems().get("Costo").setText(UTIL.DECIMAL_FORMAT.format(costo));
        buscador.getResumeItems().get("Venta").setText(UTIL.DECIMAL_FORMAT.format(venta));
        buscador.getResumeItems().get("Ve - Co").setText(UTIL.DECIMAL_FORMAT.format(rentabilidad));
    }

    public void showInformeResultados(Window owner) {
        new JDInformeResultados(owner).setVisible(true);
    }

    public void displayInformeDetalleFacturacion(Window window) {
        final PanelDetalleFacturacion panelDetalleFacturacion = new PanelDetalleFacturacion();
        buscador = new JDBuscador(window, "Informe: Detalle de Facturación", false, panelDetalleFacturacion);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"DetalleVenta.id", "I-E", "Egreso", "Ingreso", "Cond. Vta", "Fecha", "Fecha Sistema", "UDN", "Cuenta", "Sub cuenta", "Sujeto", "Productos", "F. Tipo", "F. Sucursal", "F Nro", "Sucursal", "Caja", "Año", "Mes"},
                new int[]{1, 10, 90, 90, 10, 50, 80, 100, 100, 100, 200, 50, 20, 40, 60, 100, 100, 20, 20},
                new Class<?>[]{null, null, null, null, null, null, null, null, null, null, null, null, null, Integer.class, Integer.class, null, null, Integer.class, Integer.class});
        TableColumnModel tm = buscador.getjTable1().getColumnModel();
        tm.getColumn(2).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        tm.getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer(4));
        tm.getColumn(5).setCellRenderer(FormatRenderer.getDateRenderer());
        tm.getColumn(6).setCellRenderer(FormatRenderer.getDateTimeRenderer());
        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().setAutoCreateRowSorter(true);
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<Object[]> data = armarQueryDetalleFacturacion(panelDetalleFacturacion);
                    DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
                    dtm.setRowCount(0);
                    for (Object[] objects : data) {
                        dtm.addRow(objects);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getMessage(), null, JOptionPane.ERROR_MESSAGE);
                    LOG.error("Error query detalleFacturacion", ex);
                }

            }
        });
        buscador.getBtnToExcel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (buscador.getjTable1().getRowCount() < 1) {
                        throw new MessageException("La tabla no tiene ningún dato a exportar");
                    }
                    File file = JGestionUtils.showSaveDialogFileChooser(buscador, "Exportar Detalle de Facturacion (.xls)", null, "xls");
                    if (file != null) {
                        TableExcelExporter tee = new TableExcelExporter(file, buscador.getjTable1());
                        tee.setCellStyle(5, "dd/MM/yyyy HH:mm:ss");
                        tee.export();
                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(buscador, "¿Abrir archivo generado?", null, JOptionPane.YES_NO_OPTION)) {
                            Desktop.getDesktop().open(file);
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(buscador, "Algo salió mal exportando detalle de facturación\n" + ex.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                    LOG.error("exportando detallefacturacion", ex);
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, "Algo salió mal exportando detalle de facturación\n" + ex.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                    LOG.error("exportando detallefacturacion", ex);
                }
            }
        });
        buscador.getBtnImprimir().setEnabled(false);
        buscador.setVisible(true);
    }

    public List<Object[]> armarQueryDetalleFacturacion(PanelDetalleFacturacion p) {
        StringBuilder queryIngresos = new StringBuilder(""
                + "SELECT dv.id, 'I', 0, dv.cantidad*dv.precioUnitario, fv.formaPago, fv.fechaVenta, fv.fechaalta, udn.nombre"
                + ", cuenta.nombre, subCuenta.nombre, fv.cliente.nombre, dv.producto.nombre, fv.tipo, fv.sucursal.puntoVenta, fv.numero"
                + ", fv.sucursal.nombre, caja.nombre"
                + ", fv.fechaVenta, fv.fechaVenta "
                + " FROM " + DetalleVenta.class.getSimpleName() + " dv"
                + " JOIN dv.factura fv"
                + " JOIN fv.caja caja"
                + " LEFT JOIN fv.unidadDeNegocio udn"
                + " LEFT JOIN fv.cuenta cuenta"
                + " LEFT JOIN fv.subCuenta subCuenta");
        StringBuilder queryEgresos = new StringBuilder(""
                + "SELECT dv.id, 'E', dv.cantidad*dv.precioUnitario, 0, fv.formaPago, fv.fechaCompra, fv.fechaalta, udn.nombre"
                + ", cuenta.nombre, subCuenta.nombre, fv.proveedor.nombre, dv.producto.nombre, fv.tipo"
                + ", CAST(SUBSTRING(CAST(1000000000000+fv.numero as text), 2,4) AS INTEGER)"
                + ", CAST(SUBSTRING(CAST(1000000000000+fv.numero AS text), 6) AS INTEGER)"
                + ", fv.sucursal.nombre, caja.nombre"
                + ", fv.fechaCompra, fv.fechaCompra "
                + " FROM " + DetalleCompra.class.getSimpleName() + " dv"
                + " JOIN dv.factura fv"
                + " JOIN fv.caja caja"
                + " LEFT JOIN fv.unidadDeNegocio udn"
                + " LEFT JOIN fv.cuenta cuenta"
                + " LEFT JOIN fv.subCuenta subCuenta");

        queryIngresos.append(" WHERE fv.formaPago IN (");
        queryEgresos.append(" WHERE fv.formaPago IN (");
        if (p.isCheckContado() && p.isCheckCtaCte()) {
            queryIngresos.append(Valores.FormaPago.CONTADO.getId()).append(", ").append(Valores.FormaPago.CTA_CTE.getId());
            queryEgresos.append(Valores.FormaPago.CONTADO.getId()).append(", ").append(Valores.FormaPago.CTA_CTE.getId());
        } else {
            queryIngresos.append(p.isCheckContado() ? Valores.FormaPago.CONTADO.getId() : Valores.FormaPago.CTA_CTE.getId());
            queryEgresos.append(p.isCheckContado() ? Valores.FormaPago.CONTADO.getId() : Valores.FormaPago.CTA_CTE.getId());
        }
        queryIngresos.append(")");
        queryEgresos.append(")");

        queryIngresos.append(" AND fv.anulada=").append(p.isCheckAnuladas());
        queryEgresos.append(" AND fv.anulada=").append(p.isCheckAnuladas());

        if (p.getCbClieProv().getSelectedIndex() > 0) {
            queryIngresos.append(" AND fv.cliente.id=").append(((EntityWrapper<?>) p.getCbClieProv().getSelectedItem()).getId());
            queryEgresos.append(" AND fv.proveedor.id=").append(((EntityWrapper<?>) p.getCbClieProv().getSelectedItem()).getId());
        }
        if (p.getCbSucursal().getSelectedIndex() > 0) {
            queryIngresos.append(" AND fv.sucursal.id=").append(((EntityWrapper<?>) p.getCbSucursal().getSelectedItem()).getId());
            queryEgresos.append(" AND fv.sucursal.id=").append(((EntityWrapper<?>) p.getCbSucursal().getSelectedItem()).getId());
        }
        if (p.getCbCuenta().getSelectedIndex() > 0) {
            queryIngresos.append(" AND fv.cuenta.id=").append(((EntityWrapper<?>) p.getCbCuenta().getSelectedItem()).getId());
            queryEgresos.append(" AND fv.cuenta.id=").append(((EntityWrapper<?>) p.getCbCuenta().getSelectedItem()).getId());
        }
        if (p.getCbSubCuenta().getSelectedIndex() > 0) {
            queryIngresos.append(" AND fv.subCuenta.id=").append(((EntityWrapper<?>) p.getCbSubCuenta().getSelectedItem()).getId());
            queryEgresos.append(" AND fv.subCuenta.id=").append(((EntityWrapper<?>) p.getCbSubCuenta().getSelectedItem()).getId());
        }
        if (p.getDcDesde() != null) {
            queryIngresos.append(" AND fv.fechaVenta >='").append(UTIL.yyyy_MM_dd.format(p.getDcDesde())).append("'");
            queryEgresos.append(" AND fv.fechaCompra >='").append(UTIL.yyyy_MM_dd.format(p.getDcDesde())).append("'");
        }
        if (p.getDcHasta() != null) {
            queryIngresos.append(" AND fv.fechaVenta <='").append(UTIL.yyyy_MM_dd.format(p.getDcHasta())).append("'");
            queryEgresos.append(" AND fv.fechaCompra <='").append(UTIL.yyyy_MM_dd.format(p.getDcHasta())).append("'");
        }
        if (p.getDcDesdeSistema() != null) {
            queryIngresos.append(" AND fv.").append(FacturaVenta_.fechaalta.getName()).append(" >='").append(UTIL.yyyy_MM_dd.format(p.getDcDesdeSistema())).append("'");
            queryEgresos.append(" AND fv.").append(FacturaCompra_.fechaalta.getName()).append(" >='").append(UTIL.yyyy_MM_dd.format(p.getDcDesdeSistema())).append("'");
        }
        if (p.getDcHastaSistema() != null) {
            queryIngresos.append(" AND fv.").append(FacturaVenta_.fechaalta.getName()).append(" <='").append(UTIL.yyyy_MM_dd.format(p.getDcHastaSistema())).append("'");
            queryEgresos.append(" AND fv.").append(FacturaCompra_.fechaalta.getName()).append(" <='").append(UTIL.yyyy_MM_dd.format(p.getDcHastaSistema())).append("'");
        }
        String q;
        if (p.getCbComprasVentas().getSelectedIndex() == 0) {
            q = queryIngresos.toString() + " UNION " + queryEgresos.toString();
        } else if (p.getCbComprasVentas().getSelectedIndex() == 1) {
            q = queryEgresos.toString();
        } else {
            q = queryIngresos.toString();
        }
        @SuppressWarnings("unchecked")
        List<Object[]> l = DAO.createQuery(q, false).getResultList();
        Calendar c = Calendar.getInstance();
        for (Object[] o : l) {
            c.setTime((Date) o[o.length - 2]);
            o[o.length - 2] = c.get(Calendar.YEAR);
            c.setTime((Date) o[o.length - 1]);
            o[o.length - 1] = c.get(Calendar.MONTH) + 1;
        }

        //como no se puede aplicar un order by fv.fechaalta sobre una UNION en JPA
        Collections.sort(l, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Date) o1[6]).compareTo((Date) o2[6]);
            }
        });
        return l;

    }

    public void displayFlujoPorVentas(Window window) {
        final PanelInformeFlujoVentas panelFlujoVentas = new PanelInformeFlujoVentas();
        buscador = new JDBuscador(window, "Informe: Flujo de Ventas", false, panelFlujoVentas);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Factura", "Concepto", "Fecha", "Importe"}, new int[]{100, 500, 80, 80}, new Class<?>[]{null, null, Date.class, BigDecimal.class});
        TableColumnModel tm = buscador.getjTable1().getColumnModel();
        tm.getColumn(2).setCellRenderer(FormatRenderer.getDateRenderer());
        tm.getColumn(3).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getPanelInferior().setVisible(true);
        buscador.addResumeItem("Total", new JTextField(8));
//        UTIL.hideColumnTable(buscador.getjTable1(), 0);
        buscador.getjTable1().setAutoCreateRowSorter(true);
        buscador.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<Object[]> data = armarQueryFlujoVentas(panelFlujoVentas.getData());
                    DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
                    dtm.setRowCount(0);
                    BigDecimal total = BigDecimal.ZERO;
                    for (Object[] o : data) {
                        String[] xx = o[0].toString().substring(2).split("-");
                        o[0] = o[0].toString().substring(0, 2) + UTIL.AGREGAR_CEROS(xx[0], 4) + "-" + UTIL.AGREGAR_CEROS(xx[1], 8);
                        dtm.addRow(o);
                        total = total.add((BigDecimal) o[3]);
                    }
                    buscador.getResumeItems().get("Total").setText(UTIL.DECIMAL_FORMAT.format(total));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), null, JOptionPane.ERROR_MESSAGE);
                    LOG.error("Error informes: flujo ventas", ex);
                }

            }
        });
        buscador.getBtnToExcel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (buscador.getjTable1().getRowCount() < 1) {
                        throw new MessageException("La tabla no tiene ningún dato a exportar");
                    }
                    File file = JGestionUtils.showSaveDialogFileChooser(buscador, "Exportar Flujo de Ventas (.xls)", null, "xls");
                    if (file != null) {
                        TableExcelExporter tee = new TableExcelExporter(file, buscador.getjTable1());
                        tee.setCellStyle(5, "dd/MM/yyyy HH:mm:ss");
                        tee.export();
                        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(buscador, "¿Abrir archivo generado?", null, JOptionPane.YES_NO_OPTION)) {
                            Desktop.getDesktop().open(file);
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(buscador, "Algo salió mal exportando detalle de facturación\n" + ex.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                    LOG.error("exportando detallefacturacion", ex);
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, "Algo salió mal exportando detalle de facturación\n" + ex.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                    LOG.error("exportando detallefacturacion", ex);
                }
            }
        });
        buscador.getBtnImprimir().setEnabled(false);
        buscador.setVisible(true);
    }

    private List<Object[]> armarQueryFlujoVentas(Map<String, Object> data) {
        Date flujoDesde = (Date) data.get("flujoDesde");
        String fd = flujoDesde != null ? ">= '" + UTIL.yyyy_MM_dd.format(flujoDesde) + "'" : "";
        Date flujoHasta = (Date) data.get("flujoHasta");
        String fh = flujoHasta != null ? "<= '" + UTIL.yyyy_MM_dd.format(flujoHasta) + "'" : "";

        StringBuilder facturasVentaContado = new StringBuilder("SELECT concat('F', o.tipo, o.sucursal.puntoVenta, '-', o.numero), 'Contado', o.fechaVenta, CAST(o.importe as NUMERIC(12,2)) "
                + " from " + FacturaVenta.class.getSimpleName() + " o WHERE o.formaPago=" + Valores.FormaPago.CONTADO.getId())
                .append(fd.isEmpty() ? "" : " AND o.fechaVenta" + fd).append(fh.isEmpty() ? "" : " AND o.fechaVenta" + fh);

//        0=Efectivo ({@link DetalleCajaMovimientos}), 1=Cheque Propio, 2=Cheque
//     * Tercero, 3=Nota de Crédito, 4=Retención, 5=Cuenta Bancaria Movimientos
//     * (trasnferencia), 6={@link Especie}
        StringBuilder pagos = new StringBuilder(1000);
        if ((Boolean) data.get("efectivo")) {
            pagos.append(" UNION SELECT CONCAT('RE', o.sucursal.puntoVenta, '-', o.numero), CONCAT('EF ', dcm.cajaMovimientos.caja.nombre, ' (', dcm.cajaMovimientos.id, ')'), o.fechaRecibo, dcm.monto "
                    + " FROM Recibo o JOIN o.pagos p JOIN DetalleCajaMovimientos dcm ON p.comprobanteId = dcm.id WHERE p.formaPago = 0 ")
                    .append(fd.isEmpty() ? "" : " AND o.fechaRecibo" + fd).append(fh.isEmpty() ? "" : " AND o.fechaRecibo" + fh);
        }
        if ((Boolean) data.get("propio")) {
            pagos.append(" UNION SELECT CONCAT('RE', o.sucursal.puntoVenta, '-', o.numero), CONCAT('CHP ', dcm.numero), dcm.fechaCobro, dcm.importe")
                    .append(" FROM Recibo o JOIN o.pagos p JOIN ChequePropio dcm ON p.comprobanteId = dcm.id ")
                    .append(" WHERE p.formaPago = 1").append(fd.isEmpty() ? "" : " AND dcm.fechaCobro" + fd).append(fh.isEmpty() ? "" : " AND dcm.fechaCobro" + fh);
        }
        if ((Boolean) data.get("terceros")) {
            pagos.append(" UNION SELECT CONCAT('RE', o.sucursal.puntoVenta, '-', o.numero), CONCAT('CH ', dcm.numero), dcm.fechaCobro, dcm.importe")
                    .append(" FROM Recibo o JOIN o.pagos p JOIN ChequeTerceros dcm ON p.comprobanteId = dcm.id ")
                    .append(" WHERE p.formaPago = 2").append(fd.isEmpty() ? "" : " AND dcm.fechaCobro" + fd).append(fh.isEmpty() ? "" : " AND dcm.fechaCobro" + fh);
        }
        if ((Boolean) data.get("nota")) {
            pagos.append(" UNION SELECT CONCAT('RE', o.sucursal.puntoVenta, '-', o.numero), CONCAT('NC ', dcm.sucursal.puntoVenta, '-', dcm.numero), dcm.fechaNotaCredito, dcm.importe")
                    .append(" FROM Recibo o JOIN o.pagos p JOIN NotaCredito dcm ON p.comprobanteId = dcm.id ")
                    .append(" WHERE p.formaPago = 3").append(fd.isEmpty() ? "" : " AND dcm.fechaNotaCredito" + fd).append(fh.isEmpty() ? "" : " AND dcm.fechaNotaCredito" + fh);
        }
        if ((Boolean) data.get("retencion")) {
            pagos.append(" UNION SELECT CONCAT('RE', o.sucursal.puntoVenta, '-', o.numero), CONCAT('RE ', dcm.numero), dcm.fecha, dcm.importe")
                    .append(" FROM Recibo o JOIN o.pagos p JOIN ComprobanteRetencion dcm ON p.comprobanteId = dcm.id ")
                    .append(" WHERE p.formaPago = 4").append(fd.isEmpty() ? "" : " AND dcm.fecha" + fd).append(fh.isEmpty() ? "" : " AND dcm.fecha" + fh);
        }
        if ((Boolean) data.get("transferencia")) {
            pagos.append(" UNION SELECT CONCAT('RE', o.sucursal.puntoVenta, '-', o.numero), CONCAT('TR ', dcm.descripcion), dcm.fechaCreditoDebito, dcm.credito")
                    .append(" FROM Recibo o JOIN o.pagos p JOIN CuentabancariaMovimientos dcm ON p.comprobanteId = dcm.id ")
                    .append(" WHERE p.formaPago = 5").append(fd.isEmpty() ? "" : " AND dcm.fechaCreditoDebito" + fd).append(fh.isEmpty() ? "" : " AND dcm.fechaCreditoDebito" + fh);
        }
        if ((Boolean) data.get("especie")) {
            pagos.append(" UNION SELECT CONCAT('RE', o.sucursal.puntoVenta, '-', o.numero), CONCAT('ES ', dcm.descripcion), o.fechaRecibo, dcm.importe")
                    .append(" FROM Recibo o JOIN o.pagos p JOIN Especie dcm ON p.comprobanteId = dcm.id ")
                    .append(" WHERE p.formaPago = 6").append(fd.isEmpty() ? "" : " AND o.fechaRecibo" + fd).append(fh.isEmpty() ? "" : " AND o.fechaRecibo" + fh);
        }
        String q;
        if ((Boolean) data.get("efectivo")) {
            q = facturasVentaContado.toString() + pagos.toString();
        } else {
            q = pagos.toString().substring(6); // descarta " UNION"
        }
        @SuppressWarnings("unchecked")
        List<Object[]> l = DAO.createQuery(q, false).getResultList();
        //como no se puede aplicar un order by fv.fechaalta sobre una UNION en JPA
        Collections.sort(l, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                if (o1[2] == null) {
                    return -1;
                }
                if (o2[2] == null) {
                    return 1;
                }

                return ((Date) o1[2]).compareTo((Date) o2[2]);
            }
        });
        return l;
    }

    public void displayInformeComprobantesRetencion(Window owner) {
        final JDateChooser dcDesde = new JDateChooser();
        final JDateChooser dcHasta = new JDateChooser();
        final JComboBox cbDominios = new JComboBox(new Object[]{"Recibos", "Remesas"});
        final GroupLayoutPanelBuilder glpb = new GroupLayoutPanelBuilder();
//        glpb.getInfoLabel().setText("Todos los campos son necesarios");
        glpb.getInfoLabel().setForeground(Color.BLUE);
        glpb.addFormItem(new JLabel("Fecha Desde"), dcDesde);
        glpb.addFormItem(new JLabel("Fecha Hasta"), dcHasta);
        glpb.addFormItem(new JLabel("Origen"), cbDominios);
        JPanel panel = glpb.build();
        final JDABM jdabm = new JDABM(null, "Informe de Facturas Compra por Dominio", true, panel);
        jdabm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String entityName = (cbDominios.getSelectedIndex() == 0 ? Recibo.class.getSimpleName() : Remesa.class.getSimpleName());
                    //<editor-fold defaultstate="collapsed" desc="query">
                    StringBuilder query = new StringBuilder("SELECT o.sucursal.puntoVenta, o.numero,  cr.numero, cr.fecha, cr.importe"
                            + " FROM " + entityName + " o"
                            + " JOIN o.pagos d " //+ (cbDominios.getSelectedIndex() == 0 ? ReciboPagos.class.getSimpleName() : RemesaPagos.class.getSimpleName()) + " d"
                            + " JOIN " + ComprobanteRetencion.class.getSimpleName() + " cr ON d.comprobanteId = cr.id"
                            + " WHERE d.formaPago=5 ");
                    SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
                    if (dcDesde.getDate() != null) {
                        query.append(" AND o.fecha").append(entityName).append(" >= '").append(yyyyMMdd.format(dcDesde.getDate())).append("'");
                    } else {
                        throw new MessageException("Fecha Desde no especificada");
                    }
                    if (dcHasta.getDate() != null) {
                        query.append(" AND o.fecha").append(entityName).append(" <= '").append(yyyyMMdd.format(dcHasta.getDate())).append("'");
                    } else {
                        throw new MessageException("Fecha Hasta no especificada");
                    }
                    query.append(" ORDER BY o.id");
                    @SuppressWarnings("unchecked")
                    List<Object[]> l = new ComprobanteRetencionJpaController().findAttributes(query.toString());
                    DefaultTableModel dtm = new DefaultTableModel(new String[]{"Sucu", "Nº Comprobante Origen...!!!", "N° retencion", "Fecha", "Importe"}, 0);
                    for (Object[] o : l) {
                        dtm.addRow(o);
                    }
                    List<GenericBeanCollection> data = new ArrayList<>(dtm.getRowCount());
                    DecimalFormat sucu = new DecimalFormat("0000");
                    DecimalFormat num = new DecimalFormat("00000000");
                    for (int row = 0; row < dtm.getRowCount(); row++) {
                        data.add(new GenericBeanCollection(
                                sucu.format(dtm.getValueAt(row, 0)) + "-" + num.format(dtm.getValueAt(row, 1)),
                                dtm.getValueAt(row, 2),
                                dtm.getValueAt(row, 3),
                                dtm.getValueAt(row, 4),
                                null, null, null, null, null, null, null, null));
                    }
                    Reportes r = new Reportes("JGestion_ComprobantesRetencion.jasper", "Informe - Comprobantes de retención");
                    r.setDataSource(data);
                    r.addParameter("TITLE_PAGE_HEADER", cbDominios.getSelectedItem());
                    r.addParameter("FECHA_DESDE", dcDesde.getDate());
                    r.addParameter("FECHA_HASTA", dcHasta.getDate());
                    r.addConnection();
                    r.viewReport();
                } catch (JRException | MissingReportException | MessageException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } catch (Exception ex) {
                    LOG.error(ex, ex);
                }
            }

        }
        );
        jdabm.setVisible(true);
    }
}
