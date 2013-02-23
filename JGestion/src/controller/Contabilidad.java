package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import controller.exceptions.MissingReportException;
import entity.Caja;
import entity.ChequePropio;
import entity.ChequeTerceros;
import entity.Cliente;
import entity.DetalleCajaMovimientos;
import entity.DetalleListaPrecios;
import entity.FacturaCompra;
import entity.FacturaVenta;
import entity.ListaPrecios;
import entity.NotaCredito;
import entity.Producto;
import entity.Proveedor;
import entity.Rubro;
import entity.Sucursal;
import generics.GenericBeanCollection;
import java.text.DecimalFormat;
import utilities.general.UTIL;
import gui.JDBalance;
import gui.JDBuscadorReRe;
import gui.JDInformeUnidadesDeNegocios;
import gui.JDResumenGeneralCtaCte;
import gui.PanelBalanceComprasVentas;
import gui.PanelBalanceGeneral;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.persistence.NoResultException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import jgestion.JGestionUtils;
import jgestion.Main;
import jpa.controller.ClienteJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import utilities.swing.components.ComboBoxWrapper;
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
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
//    private static final Class[] columnClassBalanceCompraVenta = {Object.class, Object.class, String.class, String.class, String.class, String.class};
    private static final Logger LOG = Logger.getLogger(Contabilidad.class.getName());

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setDecimalSeparator('.');
        PU_FORMAT = new DecimalFormat("#0.0000", simbolos);
//        PRECIO_CON_PUNTO = UTIL.PRECIO_CON_PUNTO;
//        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
//        simbolos.setDecimalSeparator('.');
//        PRECIO_CON_PUNTO = new DecimalFormat("#0.00", simbolos);
//        PRECIO_CON_PUNTO.setRoundingMode(RoundingMode.HALF_DOWN);
//        UTIL.setPRECIO_CON_PUNTO("#0.00", RoundingMode.HALF_DOWN);
    }
    private JDBuscadorReRe buscador;

    /**
     * GUI para ver de los movimientos INGRESOS/EGRESOS.
     *
     * @param parent
     * @throws MessageException
     */
    public void initMovimientosCajasUI(JFrame parent) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        panelBalanceGeneral = new PanelBalanceGeneral();
        List<ComboBoxWrapper<Caja>> cajas = new UsuarioHelper().getWrappedCajas(null);
        if (cajas.isEmpty()) {
            throw new MessageException(jgestion.Main.resourceBundle.getString("unassigned.caja"));
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
                    Logger.getLogger(Contabilidad.class.getName()).log(Level.FATAL, null, ex);
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
     * Create Native SQL Statement to retrieve entities
     * {@link DetalleCajaMovimientos#tipo} != 7 (aperturas de caja)
     *
     * @return a String with SQL
     */
    private String armarQueryBalanceGeneral() {
        StringBuilder query = new StringBuilder("SELECT o.* FROM detalle_caja_movimientos o JOIN caja_movimientos cm ON (o.caja_movimientos = cm.id)"
                + " WHERE o.tipo <> " + DetalleCajaMovimientosController.APERTURA_CAJA);
        if (panelBalanceGeneral.getCbCajas().getSelectedIndex() > 0) {
            query.append(" AND cm.caja=").append(((ComboBoxWrapper<?>) panelBalanceGeneral.getCbCajas().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < panelBalanceGeneral.getCbCajas().getItemCount(); i++) {
                ComboBoxWrapper<?> caja = (ComboBoxWrapper<?>) panelBalanceGeneral.getCbCajas().getItemAt(i);
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
        Double subTotal, ingresos, egresos;
        subTotal = 0.0;
        ingresos = 0.0;
        egresos = 0.0;
        SimpleDateFormat dateFormat = UTIL.instanceOfDATE_FORMAT();
        for (DetalleCajaMovimientos detalleCajaMovimientos : lista) {
            //los movimientos entre caja no representan un ingreso/egreso real
            if (detalleCajaMovimientos.getTipo() != DetalleCajaMovimientosController.MOVIMIENTO_CAJA) {
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
        jdBalanceUI.getTfEfectivo().setText(UTIL.PRECIO_CON_PUNTO.format(ingresos));
        jdBalanceUI.getTfCtaCte().setText(UTIL.PRECIO_CON_PUNTO.format(egresos));
        jdBalanceUI.getTfTotal().setText(UTIL.PRECIO_CON_PUNTO.format(subTotal));
    }

    /**
     * UI para ver los registros de venta (Facturas [Contado, Cta. Cte.]), Mov.
     * internos, etc..
     *
     * @param parent papi frame
     * @throws MessageException end user message information
     */
    public void initBalanceCompraVentaUI(JFrame parent) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.TESORERIA);
        panelBalanceComprasVentas = new PanelBalanceComprasVentas();
        List<ComboBoxWrapper<Sucursal>> s = new UsuarioHelper().getWrappedSucursales();
        if (s.isEmpty()) {
            throw new MessageException(Main.resourceBundle.getString("unassigned.sucursal"));
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
                    doIt();
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    LOG.error(null, ex);
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
                    doIt();
                    //                    doReportBalanceCompraVenta(query,
                    //                            panelBalanceComprasVentas.getDcDesde().getDate(),
                    //                            panelBalanceComprasVentas.getDcHasta().getDate(),
                    //                            panelBalanceComprasVentas.getCbComprasVentas().getSelectedItem().toString(), //title
                    //                            panelBalanceComprasVentas.getCheckContado().isSelected(),
                    //                            panelBalanceComprasVentas.getCheckCtaCte().isSelected(),
                    //                            panelBalanceComprasVentas.getCheckAnuladas().isSelected());
                    //                } catch (JRException ex) {
                    //                    Logger.getLogger(Contabilidad.class.getName()).log(Level.FATAL, null, ex);
                    //                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
                    //                } catch (MissingReportException ex) {
                    //                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), null, 2);
                } catch (Exception ex) {
                    LOG.error(null, ex);
                    JOptionPane.showMessageDialog(jdBalanceUI, ex.getMessage(), "ERROR", 0);
                }
            }
        });
        jdBalanceUI.setLocationRelativeTo(parent);
        jdBalanceUI.setVisible(true);
    }

    private void doIt() throws DatabaseErrorException, MessageException {
        int index = panelBalanceComprasVentas.getCbComprasVentas().getSelectedIndex();
        List<Object[]> dataCompra = new ArrayList<Object[]>(0);
        List<Object[]> dataVenta = new ArrayList<Object[]>(0);
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
            query.append(" AND o.sucursal=").append(((ComboBoxWrapper<?>) panelBalanceComprasVentas.getCbSucursal().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 1; i < panelBalanceComprasVentas.getCbSucursal().getItemCount(); i++) {
                ComboBoxWrapper<?> sucursal = (ComboBoxWrapper<?>) panelBalanceComprasVentas.getCbSucursal().getItemAt(i);
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
        Logger.getLogger(Contabilidad.class).debug(query.toString());
        return query.toString();
    }

    private List<Object[]> getFacturaCompraList(List<FacturaCompra> l) throws MessageException {
        String errores = "";
        List<Object[]> data = new ArrayList<Object[]>(l.size());
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
                totalIngresos += factura.getImporte();
                if (Valores.FormaPago.CONTADO.getId() == factura.getFormaPago()) {
                    cccpc = null;
                    efectivo = factura.getImporte();
                } else if (Valores.FormaPago.CTA_CTE.getId() == factura.getFormaPago()) {
                    entregado = new CtacteClienteController().findByFactura(factura.getId()).getEntregado();
                    double importe = factura.getImporte();
                    cccpc = (importe - entregado);
                    efectivo = entregado > 0 ? entregado : null;
                } else {
                    Logger.getLogger(Contabilidad.class).warn("FormaPago DESCONOCIDA = " + factura.getFormaPago() + ", FacturaVenta.id=" + factura.getId());
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
     * Calcula el precio final del producto con o sin IVA, teniendo en cuenta el
     * margen segun la lista de precios.
     *
     * @param producto Del cual se obtendrá el {@code producto.getPrecioVenta()}
     * y {@linkplain Producto#getIva()}
     * @param listaPrecios
     * @param incluirIVA
     * @return precioFinal incluido IVA
     */
    public static Double getPrecioFinal(Producto producto, ListaPrecios listaPrecios, boolean incluirIVA) {
        //      Logger.getLogger(this.getClass()).debug("Producto:" + producto.getNombre() + ", $venta:"+producto.getPrecioVenta() + ", ListaPrecio:" + listaPrecios.getNombre());
        Double precioFinal = (producto.getPrecioVenta().doubleValue()
                + GET_MARGEN_SEGUN_LISTAPRECIOS(listaPrecios, producto, null));
        Double iva = 0.0;
        if (incluirIVA) {
            iva = UTIL.getPorcentaje(precioFinal, producto.getIva().getIva());
        }
        return precioFinal + iva;
    }

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
     * Calcula el margen de ganancia sobre el monto según la
     * {@link ListaPrecios} seleccionada.
     *
     * @param listaPrecios
     * @param producto {@link Producto} del cual se tomarán los {@link Rubro} y
     * Sub para determinar el margen de ganancia if
     * {@link ListaPrecios#margenGeneral} == FALSE.
     * @param monto sobre el cual se va calcular el margen (suele incluir el
     * margen individual de ganancia de cada producto). Si es == null, se
     * utilizará {@link Producto#precioVenta}
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
     * Retorna el estado del cheque como String. <br>1 = ENTREGADO (estado
     * exclusivo de {@link ChequePropio}). <br>2 = CARTERA (estado exclusivo de
     * {@link ChequeTerceros}). <br>3 = DEPOSTADO. <br>4 = CAJA (cheque que se
     * convirtió en efectivo y se asentó en alguna caja) <br>5 = RECHAZADO.
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

    public void displayInformeComprobantesVenta(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(owner, "Informe - Comprobantes Ventas", false, "Cliente", "Nº Factura");
        buscador.getjTable1().setAutoCreateRowSorter(true);
        buscador.hideFactura();
        buscador.setFechaSistemaFieldsVisible(false);
        buscador.getbImprimir().setVisible(true);
        UTIL.loadComboBox(buscador.getCbClieProv(), JGestionUtils.getWrappedClientes(new ClienteController().findAll()), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Nº y Tipo", "Fecha", "Cliente", "CUIT", "Gravado", "IVA105", "IVA21", "IVA27", "Otros IVA's", "No Gravado", "Descuento", "Importe"},
                new int[]{90, 50, 50, 60, 50, 50, 50, 50, 50, 70, 60, 60},
                new Class<?>[]{null, null, null, Long.class, null, null, null, null, null, null, null, null});
        TableColumnModel tc = buscador.getjTable1().getColumnModel();
        tc.getColumn(1).setCellRenderer(FormatRenderer.getDateRenderer());
        tc.getColumn(4).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(5).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(6).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(7).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(8).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(9).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(10).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        tc.getColumn(11).setCellRenderer(NumberRenderer.getCurrencyRenderer());
        buscador.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buscador.getjTable1().getRowCount() > 0) {
                    try {
                        doComprobantesVentaReport();
                    } catch (MissingReportException ex) {
                        buscador.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (JRException ex) {
                        buscador.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    buscador.showMessage("No hay comprobantes filtrados, utilice diferentes filtros para obtener resultados", "Nada que imprimir", JOptionPane.WARNING_MESSAGE);
                }
            }

            private void doComprobantesVentaReport() throws MissingReportException, JRException {
                List<GenericBeanCollection> data = new ArrayList<GenericBeanCollection>(buscador.getjTable1().getRowCount());
                DefaultTableModel dtm = buscador.getDtm();
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
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<FacturaVenta> data = getComprobantesVenta();
                    DefaultTableModel dtm = (DefaultTableModel) buscador.getjTable1().getModel();
                    dtm.setRowCount(0);
                    for (FacturaVenta facturaVenta : data) {
                        dtm.addRow(new Object[]{
                                    JGestionUtils.getNumeracion(facturaVenta),
                                    facturaVenta.getFechaVenta(),
                                    facturaVenta.getCliente().getNombre(),
                                    facturaVenta.getCliente().getNumDoc(),
                                    new BigDecimal(facturaVenta.getGravado()),
                                    new BigDecimal(facturaVenta.getIva10()),
                                    new BigDecimal(facturaVenta.getIva21()),
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    facturaVenta.getNoGravado(),
                                    new BigDecimal(facturaVenta.getDescuento()),
                                    new BigDecimal(facturaVenta.getImporte())
                                });
                    }
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    LOG.error(ex, ex);
                }
            }
        });
        buscador.setLocationRelativeTo(owner);
        buscador.setVisible(true);
    }

    public void displayInformeComprobantesCompra(Window owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.COMPRA);
        buscador = new JDBuscadorReRe(owner, "Buscador - Comprobantes Compra", false, "Proveedor", "Nº Factura");
        buscador.getjTable1().setAutoCreateRowSorter(true);
        buscador.hideCaja();
        buscador.hideFactura();
        buscador.hideCheckAnulado();
        buscador.setFechaSistemaFieldsVisible(false);
        buscador.getbImprimir().setVisible(true);
        UTIL.loadComboBox(buscador.getCbClieProv(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        buscador.getLabelFormasDePago().setText("Tipo");
        UTIL.loadComboBox(buscador.getCbFormasDePago(), FacturaCompraController.TIPOS_FACTURA, true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Nº y Tipo", "Fecha", "Proveedor", "CUIT", "Gravado", "IVA105", "IVA21", "Otros IVA's", "Perc. IIBB", "Otros Imp.", "No Recup", "No Gravado", "Descuento", "Importe"},
                new int[]{90, 50, 50, 60, 50, 50, 50, 50, 50, 70, 60, 60, 60, 60},
                new Class<?>[]{null, null, null, Long.class, null, null, null, null, null, null, null, null, null, null});
        TableColumnModel tc = buscador.getjTable1().getColumnModel();
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
        buscador.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (buscador.getjTable1().getRowCount() > 0) {
                    try {
                        doComprobantesCompraReport();
                    } catch (MissingReportException ex) {
                        buscador.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (JRException ex) {
                        buscador.showMessage(ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    buscador.showMessage("No hay comprobantes filtrados, utilice diferentes filtros para obtener resultados", "Nada que imprimir", JOptionPane.WARNING_MESSAGE);
                }
            }

            private void doComprobantesCompraReport() throws MissingReportException, JRException {
                List<GenericBeanCollection> data = new ArrayList<GenericBeanCollection>(buscador.getjTable1().getRowCount());
                DefaultTableModel dtm = buscador.getDtm();
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
                            dtm.getValueAt(row, 13)));
                }
                Reportes r = new Reportes("JGestion_ComprobantesCompras.jasper", "Listado Comprobantes");
                r.setDataSource(data);
                r.addMembreteParameter();
                r.addConnection();
                r.viewReport();
            }
        });
        buscador.getbBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    List<?> data = getComprobantesCompra();
                    cargarTablaBuscador(data);
                } catch (MessageException ex) {
                    ex.displayMessage(buscador);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(buscador, ex.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    LOG.error(ex, ex);
                }
            }
        });
        buscador.setLocationRelativeTo(owner);
        buscador.setVisible(true);
    }

    private void cargarTablaBuscador(List<?> query) {
        DefaultTableModel dtm = buscador.getDtm();
        dtm.setRowCount(0);
        for (Object facturaVenta : query) {
            dtm.addRow((Object[]) facturaVenta);

        }
    }

    private List<?> getComprobantesCompra() throws MessageException, DatabaseErrorException {
        StringBuilder queryFactuCompra = new StringBuilder(300);
        StringBuilder queryNotaCredito = new StringBuilder(300);

        long numero;
        //filtro por nº de ReRe
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                queryFactuCompra.append(" AND o.numero = ").append(numero);
                queryNotaCredito.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de comprobante no válido");
            }
        }

        //filtro por nº de factura
        if (buscador.getTfCuarto().length() > 0 && buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfFactu4() + buscador.getTfFactu8());
                queryFactuCompra.append(" AND o.numero = ").append(numero);
                queryNotaCredito.append(" AND sucursal.puntoventa = ").append(buscador.getTfFactu4());
                queryNotaCredito.append(" AND o.numero = ").append(buscador.getTfFactu8());
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de comprobante no válido");
            }
        }
        if (buscador.getDcDesde() != null) {
            queryFactuCompra.append(" AND o.fecha_compra >= '").append(buscador.getDcDesde()).append("'");
            queryNotaCredito.append(" AND o.fecha_nota_credito >= '").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getDcHasta() != null) {
            queryFactuCompra.append(" AND o.fecha_compra <= '").append(buscador.getDcHasta()).append("'");
            queryNotaCredito.append(" AND o.fecha_nota_credito <= '").append(buscador.getDcDesde()).append("'");
        }
        if (buscador.getCbFormasDePago().getSelectedIndex() > 0) {
            queryFactuCompra.append(" AND o.tipo = '").append(buscador.getCbFormasDePago().getSelectedItem()).append("'");
        }
        UsuarioHelper usuarioHelper = new UsuarioHelper();
        if (buscador.getCbCaja().getSelectedIndex() > 0) {
            queryFactuCompra.append(" AND o.caja = ").append(((Caja) buscador.getCbCaja().getSelectedItem()).getId());
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
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            queryFactuCompra.append(" AND o.sucursal = ").append(((ComboBoxWrapper<?>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            queryFactuCompra.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i);
                queryFactuCompra.append(" o.sucursal=").append(cbw.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    queryFactuCompra.append(" OR ");
                }
            }
            queryFactuCompra.append(")");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            queryFactuCompra.append(" AND o.proveedor = ").append(((Proveedor) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        String sql =
                "SELECT com.* FROM ("
                //                + " SELECT "
                //                + "'F' || o.tipo || to_char(o.numero, '0000-00000000') as comprobante, "
                //                + " o.fecha_compra as fecha, proveedor.nombre, proveedor.cuit, "
                //                //la mentirita del GRAVADO :O jejejej
                //                + " case when o.gravado <=0 then (o.importe-o.iva10-o.iva21-o.perc_iva-o.impuestos_recuperables) else o.gravado end,"
                //                + " o.iva10, o.iva21, o.perc_iva, o.impuestos_recuperables, o.impuestos_norecuperables, o.no_gravado, o.descuento, o.importe"
                + " SELECT 'F' || o.tipo || to_char(o.numero, '0000-00000000') as comprobante,	o.fecha_compra as fecha, proveedor.nombre, proveedor.cuit,"
                + " cast(case when o.gravado <=0 then (o.importe-o.iva10-o.iva21-o.perc_iva-o.impuestos_recuperables) else o.gravado end as numeric(12,2)),	cast(o.iva10 as numeric(12,2)),	cast(o.iva21 as numeric(12,2)), o.otros_ivas, "
                + "	cast(o.perc_iva as numeric(12,2)),	cast( o.impuestos_recuperables as numeric(12,2)),    cast( o.impuestos_norecuperables as numeric(12,2)), cast( o.no_gravado as numeric(12,2)), cast( o.descuento as numeric(12,2)), cast( o.importe as numeric(12,2))"
                + " FROM factura_compra o, proveedor"
                + " WHERE o.anulada = false AND o.proveedor = proveedor.id "
                + queryFactuCompra.toString()
                + " ORDER BY"
                + " o.fecha_compra ASC) com"
                + " UNION ("
                //                + " SELECT "
                //                + " 'NC' || to_char(sucursal.puntoventa, '0000') || to_char(o.numero,'-00000000'),"
                //                + " o.fecha_nota_credito as fecha, cliente.nombre, cliente.num_doc,"
                //                + " o.gravado, o.iva10, o.iva21, 0, o.impuestos_recuperables, 0, o.no_gravado, 0 as descuento, o.importe"
                + " SELECT 'NC' || to_char(sucursal.puntoventa, '0000') || to_char(o.numero,'-00000000'), o.fecha_nota_credito as fecha, cliente.nombre, cliente.num_doc, cast(o.gravado as numeric(12,2)), cast(o.iva10 as numeric(12,2)), cast(o.iva21 as numeric(12,2)), cast(o.impuestos_recuperables as numeric(12,2)), cast(0 as numeric(12,2)), cast(0 as numeric(12,2)), cast(0 as numeric(12,2)),	cast(o.no_gravado as numeric(12,2)), cast(0 as numeric(12,2)) as descuento, cast(o.importe as numeric(12,2))"
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

    private List<FacturaVenta> getComprobantesVenta() throws MessageException, DatabaseErrorException {
        StringBuilder queryWhereFactuVenta = new StringBuilder(300).append(" o.tipo <> 'I'");
        StringBuilder queryWhereNotaCredito = new StringBuilder(300).append(" o.id is not null");

        long numero;
        //filtro por nº de comprobante
        if (buscador.getTfCuarto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfCuarto());
                queryWhereFactuVenta.append(" AND o.sucursal.puntoVenta = ").append(numero);
                queryWhereNotaCredito.append(" AND o.sucursal.puntoVenta = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de Punto de Venta de comprobante no válido");
            }
        }
        if (buscador.getTfOcteto().length() > 0) {
            try {
                numero = Long.parseLong(buscador.getTfOcteto());
                queryWhereFactuVenta.append(" AND o.numero = ").append(numero);
                queryWhereNotaCredito.append(" AND o.numero = ").append(numero);
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de comprobante no válido");
            }
        }

        if (buscador.getDcDesde() != null) {
            queryWhereFactuVenta.append(" AND o.fechaVenta >= '").append(yyyyMMdd.format(buscador.getDcDesde())).append("'");
            queryWhereNotaCredito.append(" AND o.fechaNotaCredito >= '").append(yyyyMMdd.format(buscador.getDcDesde())).append("'");
        }
        if (buscador.getDcHasta() != null) {
            queryWhereFactuVenta.append(" AND o.fechaVenta <= '").append(yyyyMMdd.format(buscador.getDcHasta())).append("'");
            queryWhereNotaCredito.append(" AND o.fechaNotaCredito <= '").append(yyyyMMdd.format(buscador.getDcDesde())).append("'");
        }
        UsuarioHelper usuarioHelper = new UsuarioHelper();
        if (buscador.getCbCaja().getSelectedIndex() > 0) {
            queryWhereFactuVenta.append(" AND o.caja.id = ").append(((Caja) buscador.getCbCaja().getSelectedItem()).getId());
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
        if (buscador.getCbSucursal().getSelectedIndex() > 0) {
            queryWhereFactuVenta.append(" AND o.sucursal.id = ").append(((ComboBoxWrapper<?>) buscador.getCbSucursal().getSelectedItem()).getId());
        } else {
            queryWhereFactuVenta.append(" AND (");
            for (int i = 1; i < buscador.getCbSucursal().getItemCount(); i++) {
                ComboBoxWrapper<Sucursal> cbw = (ComboBoxWrapper<Sucursal>) buscador.getCbSucursal().getItemAt(i);
                queryWhereFactuVenta.append(" o.sucursal.id=").append(cbw.getId());
                if ((i + 1) < buscador.getCbSucursal().getItemCount()) {
                    queryWhereFactuVenta.append(" OR ");
                }
            }
            queryWhereFactuVenta.append(")");
        }

        if (buscador.getCbClieProv().getSelectedIndex() > 0) {
            queryWhereFactuVenta.append(" AND o.cliente.id = ").append(((Cliente) buscador.getCbClieProv().getSelectedItem()).getId());
        }

        queryWhereFactuVenta.append(" AND o.anulada = ").append(buscador.getCheckAnulada().isSelected());
        queryWhereNotaCredito.append(" AND o.anulada = ").append(buscador.getCheckAnulada().isSelected());

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
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "", JOptionPane.WARNING_MESSAGE);
                    } catch (MissingReportException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "", JOptionPane.WARNING_MESSAGE);
                    } catch (JRException ex) {
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
            }
        });
        resumenGeneralCtaCte.setVisible(true);
    }

    public void showInformePorUnidadesDeNegocios(Window owner) {
        new JDInformeUnidadesDeNegocios(owner).setVisible(true);
    }
}
