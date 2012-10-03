package controller;

import controller.exceptions.DatabaseErrorException;
import controller.exceptions.MessageException;
import controller.exceptions.MissingReportException;
import entity.Caja;
import entity.CajaMovimientos;
import entity.ChequePropio;
import entity.ChequeTerceros;
import entity.Cliente;
import entity.CtacteCliente;
import entity.DetalleCajaMovimientos;
import entity.DetalleListaPrecios;
import entity.FacturaCompra;
import entity.FacturaVenta;
import entity.ListaPrecios;
import entity.Producto;
import entity.Proveedor;
import entity.Rubro;
import entity.Sucursal;
import generics.GenericBeanCollection;
import java.text.DecimalFormat;
import utilities.general.UTIL;
import gui.JDBalance;
import gui.JDBuscadorReRe;
import gui.JFP;
import gui.PanelBalanceComprasVentas;
import gui.PanelBalanceGeneral;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import jgestion.JGestionUtils;
import jpa.controller.CajaMovimientosJpaController;
import jpa.controller.FacturaVentaJpaController;
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

    private JDBalance jdBalanceUI;
    private PanelBalanceGeneral panelBalanceGeneral;
    private static final String columnNamesBalanceGeneral[] = {"FECHA", "DESCRIPCIÓN", "INGRESOS", "EGRESOS", "TOTAL ACUM."};
    private static final int columnWidthsBalanceGeneral[] = {40, 440, 55, 55, 60};
    private static final Class[] columnClassBalanceGeneral = {Object.class, Object.class, String.class, String.class, String.class};
    private PanelBalanceComprasVentas panelBalanceComprasVentas;
    private static final String columnNamesBalanceCompraVenta[] = {"FECHA", "DESCRIPCIÓN", "INGRESOS/EGRESOS", "EFECTIVO", "CTA. CTE.", "TOTAL ACUM."};
    private static final int columnWidthsBalanceCompraVenta[] = {60, 190, 60, 60, 60, 60};
    private static final Class[] columnClassBalanceCompraVenta = {Object.class, Object.class, String.class, String.class, String.class, String.class};
    private static final Logger LOG = Logger.getLogger(Contabilidad.class.getName());

    static {
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
    public void initBalanceGeneralUI(JFrame parent) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        panelBalanceGeneral = new PanelBalanceGeneral();
        UTIL.loadComboBox(panelBalanceGeneral.getCbCajas(), new UsuarioHelper().getWrappedCajas(null), false);
        jdBalanceUI = new JDBalance(parent, false, panelBalanceGeneral);
        jdBalanceUI.setTitle("Balance");
        jdBalanceUI.getLabelTotalAux().setVisible(false);
        jdBalanceUI.getTfTotalAux().setVisible(false);
        UTIL.getDefaultTableModel(jdBalanceUI.getjTable1(),
                columnNamesBalanceGeneral,
                columnWidthsBalanceGeneral,
                columnClassBalanceGeneral);
        UTIL.setHorizonalAlignment(jdBalanceUI.getjTable1(), String.class, JLabel.RIGHT);
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
    private String armarQueryBalance() {
        StringBuilder query = new StringBuilder("SELECT o.* FROM detalle_caja_movimientos o JOIN caja_movimientos cm ON (o.caja_movimientos = cm.id)"
                + " WHERE o.tipo <> " + DetalleCajaMovimientosJpaController.APERTURA_CAJA);
        if (panelBalanceGeneral.getCbCajas().getSelectedIndex() > -1) {
            query.append(" AND cm.caja=").append(((ComboBoxWrapper<?>) panelBalanceGeneral.getCbCajas().getSelectedItem()).getId());
        } else {
            query.append(" AND (");
            for (int i = 0; i < panelBalanceGeneral.getCbCajas().getItemCount(); i++) {
                ComboBoxWrapper<?> caja = (ComboBoxWrapper<?>) panelBalanceGeneral.getCbCajas().getItemAt(i);
                query.append(" cm.caja=").append(caja.getId());
                if ((i + 1) < panelBalanceGeneral.getCbCajas().getItemCount()) {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        if (!panelBalanceGeneral.getCheckMovEntreCajas().isSelected()) {
            query.append(" AND o.tipo <> ").append(DetalleCajaMovimientosJpaController.MOVIMIENTO_CAJA);
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
     * UI para ver los registros de venta (Facturas [Contado, Cta. Cte.]), Mov.
     * internos, etc..
     *
     * @param parent papi frame
     * @throws MessageException end user message information
     */
    public void initBalanceCompraVentaUI(JFrame parent) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        panelBalanceComprasVentas = new PanelBalanceComprasVentas();
        jdBalanceUI = new JDBalance(parent, false, panelBalanceComprasVentas);
        jdBalanceUI.getLabelTotalAux().setText("INGR/EGRE");
        jdBalanceUI.getLabelTotalIngresos().setText("EFECTIVO");
        jdBalanceUI.getLabelTotalEgresos().setText("CTA. CTE.");
        jdBalanceUI.getLabelTotalTotal().setText(null);
        jdBalanceUI.getTfTotal().setText("---------");
        jdBalanceUI.setSize(700, 500);
        UTIL.getDefaultTableModel(jdBalanceUI.getjTable1(),
                columnNamesBalanceCompraVenta,
                columnWidthsBalanceCompraVenta,
                columnClassBalanceCompraVenta);
        UTIL.setHorizonalAlignment(jdBalanceUI.getjTable1(), String.class, JLabel.RIGHT);
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
        Logger.getLogger(Contabilidad.class).debug(sb.toString());
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
                        JGestionUtils.getNumeracion(factura) + (factura.getAnulada() ? "[ANULADA]" : ""),
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
        Double efectivo = null, cccpc = null;
        Double totalEfectivo = 0.0;
        Double totalIngresos = 0.0;
        Double totalCCCPC = 0.0;
        Double entregado = null;
        SimpleDateFormat dateFormat = UTIL.instanceOfDATE_FORMAT();
        for (FacturaVenta factura : l) {
            if (!factura.getAnulada()) {
                totalIngresos += factura.getImporte();
                if (Valores.FormaPago.CONTADO.getId() == factura.getFormaPago()) {
                    cccpc = null;
                    efectivo = factura.getImporte();
                    totalEfectivo += efectivo;
                } else if (Valores.FormaPago.CTA_CTE.getId() == factura.getFormaPago()) {
                    entregado = new CtacteClienteJpaController().findCtacteClienteByFactura(factura.getId()).getEntregado();
                    double importe = factura.getImporte();
                    cccpc = (importe - entregado);
                    totalCCCPC += cccpc;
                    efectivo = entregado > 0 ? entregado : null;
                } else {
                    Logger.getLogger(Contabilidad.class).warn("FormaPago DESCONOCIDA = " + factura.getFormaPago() + ", Nº" + factura.getNumero());
                }
            } else {
                efectivo = null;
                cccpc = null;
            }
            dtm.addRow(new Object[]{
                        dateFormat.format(factura.getFechaVenta()),
                        JGestionUtils.getNumeracion(factura) + (factura.getAnulada() ? "[ANULADA]" : ""),
                        UTIL.DECIMAL_FORMAT.format(factura.getImporte()),
                        efectivo != null ? efectivo : "------",
                        cccpc != null ? UTIL.DECIMAL_FORMAT.format(cccpc) : "------",
                        UTIL.DECIMAL_FORMAT.format(totalIngresos)
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
        Double precioFinal = (producto.getPrecioVenta()
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
        Double montoDefinitivo = (monto == null ? producto.getPrecioVenta() : monto);
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

    public void displayInformeComprobantesCompra(JFrame owner) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.VENTA);
        buscador = new JDBuscadorReRe(owner, "Buscador - Comprobantes Compra", true, "Proveedor", "Nº Factura");
        buscador.hideCaja();
        buscador.hideFormaPago();
        buscador.hideFactura();
        buscador.setFechaSistemaFieldsVisible(false);
        buscador.getbImprimir().setVisible(true);
        UTIL.loadComboBox(buscador.getCbClieProv(), new ProveedorController().findEntities(), true);
        UTIL.loadComboBox(buscador.getCbCaja(), new CajaController().findCajasPermitidasByUsuario(UsuarioController.getCurrentUser(), true), true);
        UTIL.loadComboBox(buscador.getCbSucursal(), new UsuarioHelper().getWrappedSucursales(), true);
        UTIL.loadComboBox(buscador.getCbFormasDePago(), Valores.FormaPago.getFormasDePago(), true);
        UTIL.getDefaultTableModel(
                buscador.getjTable1(),
                new String[]{"Nº y Tipo", "Fecha", "Proveedor", "CUIT", "Gravado", "IVA105", "IVA21", "Perc. IIBB", "Otros Imp.", "No Recup", "No Gravado", "Descuento", "Importe"},
                new int[]{90, 50, 50, 60, 50, 50, 50, 50, 70, 60, 60, 60, 60},
                new Class<?>[]{null, null, null, Long.class, null, null, null, null, null, null, null, null, null});
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
                            dtm.getValueAt(row, 7),
                            dtm.getValueAt(row, 8),
                            dtm.getValueAt(row, 10),
                            dtm.getValueAt(row, 12),
                            null
                            ));
                }
                Reportes r = new Reportes("JGestion_ComprobantesCompras.jasper", "Listado Comprobantes");
                r.setDataSource(data);
                r.addEmpresaReport();
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
        StringBuilder queryFactuCompra = new StringBuilder();
        StringBuilder queryNotaCredito = new StringBuilder();

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
//        if (buscador.getDcDesdeSistema() != null) {
//            query.append(" AND o.fechaalta >= '").append(buscador.getDcDesdeSistema()).append("'");
//        }
//        if (buscador.getDcHastaSistema() != null) {
//            query.append(" AND o.fechaalta <= '").append(buscador.getDcHastaSistema()).append("'");
//        }
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
                + " SELECT "
                + "'F' || o.tipo || to_char(o.numero, '0000-00000000') as comprobante, "
                + " o.fecha_compra as fecha, proveedor.nombre, proveedor.cuit, "
                //la mentirita del GRAVADO :O jejejej
                + " case when o.gravado <=0 then (o.importe-o.iva10-o.iva21-o.perc_iva-o.impuestos_recuperables) else o.gravado end,"
                + " o.iva10, o.iva21, o.perc_iva, o.impuestos_recuperables, o.impuestos_norecuperables, o.no_gravado, o.descuento, o.importe"
                + " FROM public.factura_compra o, public.proveedor"
                + " WHERE "
                + " o.proveedor = proveedor.id"
                + queryFactuCompra.toString()
                + " ORDER BY"
                + " o.fecha_compra ASC) com"
                + " UNION ("
                + " SELECT "
                + " 'NC' || to_char(sucursal.puntoventa, '0000') || to_char(o.numero,'-00000000'),"
                + " o.fecha_nota_credito as fecha, cliente.nombre, cliente.num_doc,"
                + " o.gravado, o.iva10, o.iva21, 0, o.impuestos_recuperables, 0, o.no_gravado, 0 as descuento, o.importe"
                + " FROM public.nota_credito o, public.cliente, public.sucursal"
                + " WHERE "
                + " o.cliente = cliente.id AND"
                + " o.sucursal = sucursal.id"
                + queryNotaCredito.toString()
                + " ORDER BY"
                + " o.fecha_nota_credito ASC)"
                + " ORDER BY fecha";
        System.out.println("QUERY: " + sql);
        List<?> l = DAO.getNativeQueryResultList(sql, (String) null);
        return l;
    }
}
