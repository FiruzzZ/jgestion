package jgestion.controller;

import afip.ws.exception.WSAFIPErrorResponseException;
import generics.GenericBeanCollection;
import generics.WaitingDialog;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.MissingReportException;
import jgestion.entity.Cliente;
import jgestion.entity.DatosEmpresa;
import jgestion.entity.DetalleNotaCredito;
import jgestion.entity.DetalleNotaDebito;
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaElectronica;
import jgestion.entity.FacturaVenta;
import jgestion.entity.NotaCredito;
import jgestion.entity.NotaDebito;
import jgestion.entity.Sucursal;
import jgestion.jpa.controller.FacturaElectronicaJpaController;
import jgestion.jpa.controller.FacturaVentaJpaController;
import jgestion.jpa.controller.NotaCreditoJpaController;
import jgestion.jpa.controller.NotaDebitoJpaController;
import jgestion.jpa.controller.SucursalJpaController;
import net.sf.jasperreports.engine.JRException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class FacturaElectronicaController {

    private static final Logger LOG = LogManager.getLogger();
    private static Thread caeRequestThread;

    public static void initSolicitudCAEs() {
        if (caeRequestThread != null && caeRequestThread.isAlive()) {
            JOptionPane.showMessageDialog(null, "En proceso...");
            return;
        }
        final WaitingDialog waiting = new WaitingDialog(null, "Solicitando CAE..", false, "");
        waiting.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                caeRequestThread = new Thread(() -> {
                    try {
                        FacturaElectronicaJpaController feJpaController = new FacturaElectronicaJpaController();
                        List<FacturaElectronica> l = feJpaController.findAllPendientes();
                        if (l.isEmpty()) {
                            waiting.appendMessage("No hay comprobantes pendientes de CAE.." + JGestionUtils.getRandomMotivation(), true, true);
                            return;
                        }
                        waiting.appendMessage("CAE pendientes: " + l.size(), true, true);
                        waiting.appendMessage("obteniendo ticket de acceso..", true, true);
                        AFIPWSController afipwsController = new AFIPWSController(null);
                        Object comprobante = null;
                        for (FacturaElectronica fe : l) {
                            String cbteNumero = null;
                            Sucursal s = new SucursalJpaController().findByPuntoVenta(fe.getPtoVta());
                            char tipo = 0;
                            if (fe.getCbteTipo() == 1 || fe.getCbteTipo() == 6 || fe.getCbteTipo() == 11 || fe.getCbteTipo() == 51) {
                                tipo = fe.getCbteTipo() == 1 ? 'A'
                                        : fe.getCbteTipo() == 6 ? 'B' : 'C';
                                FacturaVenta fv = new FacturaVentaJpaController().findBy(s, tipo, (int) fe.getCbteNumero());
                                comprobante = fv;
                                cbteNumero = JGestionUtils.getNumeracion(fv);
                            } else if (fe.getCbteTipo() == 2 || fe.getCbteTipo() == 7 || fe.getCbteTipo() == 12 || fe.getCbteTipo() == 52) {
                                tipo = fe.getCbteTipo() == 2 ? 'A'
                                        : fe.getCbteTipo() == 7 ? 'B'
                                                : fe.getCbteTipo() == 12 ? 'C' : 'M';
                                NotaDebito nc = new NotaDebitoJpaController().findBy(s, tipo, (int) fe.getCbteNumero());
                                comprobante = nc;
                                cbteNumero = JGestionUtils.getNumeracion(nc);
                            } else if (fe.getCbteTipo() == 3 || fe.getCbteTipo() == 8 || fe.getCbteTipo() == 13 || fe.getCbteTipo() == 53) {
                                tipo = fe.getCbteTipo() == 3 ? 'A'
                                        : fe.getCbteTipo() == 8 ? 'B'
                                                : fe.getCbteTipo() == 13 ? 'C' : 'M';
                                NotaCredito nc = new NotaCreditoJpaController().findBy(s, tipo, (int) fe.getCbteNumero());
                                comprobante = nc;
                                cbteNumero = JGestionUtils.getNumeracion(nc, true);

                            }
                            waiting.appendMessage("consultando CAE de " + cbteNumero, true, true);
                            FacturaElectronica fee = null;
                            try {
                                /**
                                 * Si en el sistema figura como CAE pendiente, pero la consulta del
                                 * comprobante retorna algo puede que haya sucedido un error y no se
                                 * guardó el CAE retornado
                                 */
                                fee = afipwsController.getFEComprobante(s.getPuntoVenta(), tipo, (int) fe.getCbteNumero());
                            } catch (WSAFIPErrorResponseException ex) {
                                if (!ex.getMessage().contains("code:602")) {
                                    waiting.appendMessage(ex.getMessage(), true, true);
                                }
                            }
                            try {
                                if (fee == null) {
                                    waiting.appendMessage("solicitando CAE de " + cbteNumero, true, true);
                                    if (comprobante instanceof FacturaVenta) {
                                        fee = afipwsController.requestCAE(fe, (FacturaVenta) comprobante);
                                    }
                                    if (comprobante instanceof NotaCredito) {
                                        fee = afipwsController.requestCAE(fe, (NotaCredito) comprobante);
                                    }
                                    if (comprobante instanceof NotaDebito) {
                                        fee = afipwsController.requestCAE(fe, (NotaDebito) comprobante);
                                    }
                                }

                                LOG.info(fee.toString());
                                feJpaController.merge(fee);
                                waiting.appendMessage("guardando comprobante..", true, true);
                            } catch (WSAFIPErrorResponseException | MessageException ex) {
                                waiting.appendMessage(ex.getMessage(), true, true);
                            }
                        }
                    } catch (Exception ex) {
                        LOG.error(ex, ex);
                        waiting.appendMessage(ex.getMessage(), true, true);
                    } finally {
                        waiting.dispose();
                        JOptionPane.showMessageDialog(null, waiting.getMessageToKeep());
                    }
                });
                caeRequestThread.start();
            }
        });
        waiting.setVisible(true);
    }

    private final FacturaElectronicaJpaController jpaController = new FacturaElectronicaJpaController();

    public FacturaElectronicaController() {

    }

    /**
     * Crea una instancia de {@link FacturaElectronica}
     *
     * @param f
     * @return
     */
    static FacturaElectronica createFrom(FacturaVenta f) {
        FacturaElectronica fe = new FacturaElectronica(null, AFIPWSController.getTipoComprobanteID(f),
                f.getSucursal().getPuntoVenta(), Long.valueOf(f.getNumero()).intValue(),
                null, null, 3, null, null, null);
        return fe;
    }

    static FacturaElectronica createFrom(NotaDebito f) {
        FacturaElectronica fe = new FacturaElectronica(null, AFIPWSController.getTipoComprobanteID(f),
                f.getSucursal().getPuntoVenta(), Long.valueOf(f.getNumero()).intValue(),
                null, null, 3, null, null, null);
        return fe;
    }

    static FacturaElectronica createFrom(NotaCredito f) {
        FacturaElectronica fe = new FacturaElectronica(null, AFIPWSController.getTipoComprobanteID(f),
                f.getSucursal().getPuntoVenta(), Long.valueOf(f.getNumero()).intValue(),
                null, null, 3, null, null, null);
        return fe;
    }

    public FacturaElectronica findBy(FacturaVenta cbte) {
        return jpaController.findBy(AFIPWSController.getTipoComprobanteID(cbte), cbte.getSucursal().getPuntoVenta(), cbte.getNumero());
    }

    public FacturaElectronica findBy(NotaCredito cbte) {
        return jpaController.findBy(AFIPWSController.getTipoComprobanteID(cbte), cbte.getSucursal().getPuntoVenta(), cbte.getNumero());
    }

    public FacturaElectronica findBy(NotaDebito cbte) {
        return jpaController.findBy(AFIPWSController.getTipoComprobanteID(cbte), cbte.getSucursal().getPuntoVenta(), cbte.getNumero());
    }

    void doReport(FacturaVenta cbte) throws MissingReportException, JRException, MessageException {
        FacturaElectronica fe = findBy(cbte);
        if (fe.getCae() == null) {
            throw new MessageException("el comprobante " + JGestionUtils.getNumeracion(cbte) + " aún no posee CAE");
        }
        HashMap<String, Object> parameters = new HashMap<>(30);
        parameters.put("CBTE_TIPO", cbte.getTipo() + "");
        //tiene que tener 2 dígitos el cod tipo cbte
        parameters.put("CBTE_TIPO_COD", fe.getCbteTipo() > 9 ? fe.getCbteTipo() : "0" + fe.getCbteTipo());
        parameters.put("CBTE_NOMBRE", "FACTURA");
        parameters.put("CBTE_FECHA_EMISION", cbte.getFechaVenta());
        parameters.put("CBTE_PUNTO", UTIL.AGREGAR_CEROS(fe.getPtoVta(), 4));
        parameters.put("CBTE_NUMERO", UTIL.AGREGAR_CEROS(fe.getCbteNumero(), 8));
        parameters.put("CBTE_CONDICION_VENTA", cbte.getFormaPago() == 1 ? "Contado" : "Cta. Cte.");
        parameters.put("CBTE_CAE", fe.getCae());
        parameters.put("CBTE_CAE_VTO", fe.getCaeFechaVto());
        putClienteParameters(parameters, cbte.getCliente());
        parameters.put("CBTE_TOTAL", cbte.getImporte());
        parameters.put("CBTE_GRAVADO", cbte.getGravado());
        if (parameters.get("CBTE_TIPO").equals("A")) {
            parameters.put("CBTE_NOGRAVADO", cbte.getNoGravado());
        } else {
            //se muestra como SubTotal
            parameters.put("CBTE_NOGRAVADO", cbte.getImporte());
        }
        parameters.put("CBTE_OTROSTRIB", BigDecimal.ZERO);
        parameters.put("CBTE_BONIF", BigDecimal.ZERO);
        BigDecimal iva27 = BigDecimal.ZERO;
        BigDecimal iva21 = BigDecimal.ZERO;
        BigDecimal iva105 = BigDecimal.ZERO;
        BigDecimal iva5 = BigDecimal.ZERO;
        BigDecimal iva205 = BigDecimal.ZERO;
        List<GenericBeanCollection> detalle = new ArrayList<>();
        for (DetalleVenta d : cbte.getDetallesVentaList()) {
            BigDecimal alicuota = BigDecimal.valueOf(d.getProducto().getIva().getIva()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal precioUnitario = d.getPrecioUnitario();
            BigDecimal subTotal = precioUnitario.multiply(BigDecimal.valueOf(d.getCantidad())).setScale(4, RoundingMode.HALF_UP);
            if (cbte.getTipo() == 'A') {
                if (alicuota.doubleValue() > 0) {
                    BigDecimal iva = UTIL.getPorcentaje(subTotal, alicuota);
                    if (alicuota.toString().equals("10.5")) {
                        iva105 = iva105.add(iva);
                    } else if (alicuota.toString().equals("2.5")) {
                        iva205 = iva205.add(iva);
                    } else if (alicuota.intValue() == 5) {
                        iva5 = iva5.add(iva);
                    } else if (alicuota.intValue() == 21) {
                        iva21 = iva21.add(iva);
                    } else if (alicuota.intValue() == 27) {
                        iva27 = iva27.add(iva);
                    }
                }
            } else {
                subTotal = subTotal
                        .multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE)))
                        .setScale(2, RoundingMode.HALF_UP);
                precioUnitario = precioUnitario.multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE)))
                        .setScale(4, RoundingMode.HALF_UP);
                alicuota = null;
            }
            detalle.add(new GenericBeanCollection(d.getProducto().getCodigo(), d.getProducto().getNombre(), d.getCantidad(),
                    "Unitario", precioUnitario, d.getDescuento(), alicuota, subTotal));
        }
        parameters.put("CBTE_IVA27", iva27);
        parameters.put("CBTE_IVA21", iva21);
        parameters.put("CBTE_IVA105", iva105);
        parameters.put("CBTE_IVA5", iva5);
        parameters.put("CBTE_IVA205", iva205);
        doReport(parameters, detalle);
    }

    void doReport(NotaDebito cbte) throws MessageException, MissingReportException, JRException {
        FacturaElectronica fe = findBy(cbte);
        if (fe.getCae() == null) {
            throw new MessageException("el comprobante " + JGestionUtils.getNumeracion(cbte) + " aún no posee CAE");
        }
        HashMap<String, Object> parameters = new HashMap<>(30);
        parameters.put("CBTE_TIPO", cbte.getTipo() + "");
        //tiene que tener 2 dígitos el cod tipo cbte
        parameters.put("CBTE_TIPO_COD", fe.getCbteTipo() > 9 ? fe.getCbteTipo() : "0" + fe.getCbteTipo());
        parameters.put("CBTE_NOMBRE", "NOTA DÉBITO");
        parameters.put("CBTE_FECHA_EMISION", cbte.getFechaNotaDebito());
        parameters.put("CBTE_PUNTO", UTIL.AGREGAR_CEROS(fe.getPtoVta(), 4));
        parameters.put("CBTE_NUMERO", UTIL.AGREGAR_CEROS(fe.getCbteNumero(), 8));
//        parameters.put("CBTE_CONDICION_VENTA", cbte.getFormaPago() == 1 ? "Contado" : "Cta. Cte.");
        parameters.put("CBTE_CAE", fe.getCae());
        parameters.put("CBTE_CAE_VTO", fe.getCaeFechaVto());
        putClienteParameters(parameters, cbte.getCliente());
        parameters.put("CBTE_OBSERVACION", cbte.getObservacion());
        parameters.put("CBTE_TOTAL", cbte.getImporte());
        parameters.put("CBTE_GRAVADO", cbte.getGravado());
        if (parameters.get("CBTE_TIPO").equals("A")) {
            parameters.put("CBTE_NOGRAVADO", cbte.getNoGravado());
        } else {
            //se muestra como SubTotal
            parameters.put("CBTE_NOGRAVADO", cbte.getImporte());
        }
        parameters.put("CBTE_OTROSTRIB", BigDecimal.ZERO);
        parameters.put("CBTE_BONIF", BigDecimal.ZERO);
        BigDecimal iva27 = BigDecimal.ZERO;
        BigDecimal iva21 = BigDecimal.ZERO;
        BigDecimal iva105 = BigDecimal.ZERO;
        BigDecimal iva5 = BigDecimal.ZERO;
        BigDecimal iva205 = BigDecimal.ZERO;
        List<GenericBeanCollection> detalle = new ArrayList<>();
        for (DetalleNotaDebito d : cbte.getDetalle()) {
            BigDecimal alicuota = BigDecimal.valueOf(d.getIva().getIva()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal precioUnitario = d.getImporte();
            BigDecimal subTotal = precioUnitario.multiply(BigDecimal.valueOf(d.getCantidad())).setScale(4, RoundingMode.HALF_UP);
            if (cbte.getTipo() == 'A') {
                if (alicuota.doubleValue() > 0) {
                    BigDecimal iva = UTIL.getPorcentaje(subTotal, alicuota);
                    if (alicuota.toString().equals("10.5")) {
                        iva105 = iva105.add(iva);
                    } else if (alicuota.toString().equals("2.5")) {
                        iva205 = iva205.add(iva);
                    } else if (alicuota.intValue() == 5) {
                        iva5 = iva5.add(iva);
                    } else if (alicuota.intValue() == 21) {
                        iva21 = iva21.add(iva);
                    } else if (alicuota.intValue() == 27) {
                        iva27 = iva27.add(iva);
                    }
                }
            } else {
                subTotal = subTotal
                        .multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE)))
                        .setScale(2, RoundingMode.HALF_UP);
                precioUnitario = precioUnitario.multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE)))
                        .setScale(4, RoundingMode.HALF_UP);
                alicuota = null;
            }
            detalle.add(new GenericBeanCollection(null, d.getConcepto(), d.getCantidad(),
                    "Unitario", precioUnitario, BigDecimal.ZERO, alicuota, subTotal));
        }
        parameters.put("CBTE_IVA27", iva27);
        parameters.put("CBTE_IVA21", iva21);
        parameters.put("CBTE_IVA105", iva105);
        parameters.put("CBTE_IVA5", iva5);
        parameters.put("CBTE_IVA205", iva205);
        doReport(parameters, detalle);
    }

    void doReport(NotaCredito cbte) throws MessageException, MissingReportException, JRException {
        FacturaElectronica fe = findBy(cbte);
        if (fe.getCae() == null) {
            throw new MessageException("el comprobante " + JGestionUtils.getNumeracion(cbte, true) + " aún no posee CAE");
        }
        HashMap<String, Object> parameters = new HashMap<>(30);
        parameters.put("CBTE_TIPO", cbte.getTipo() + "");
        //tiene que tener 2 dígitos el cod tipo cbte
        parameters.put("CBTE_TIPO_COD", fe.getCbteTipo() > 9 ? fe.getCbteTipo() : "0" + fe.getCbteTipo());
        parameters.put("CBTE_NOMBRE", "NOTA CRÉDITO");
        parameters.put("CBTE_FECHA_EMISION", cbte.getFechaNotaCredito());
        parameters.put("CBTE_PUNTO", UTIL.AGREGAR_CEROS(fe.getPtoVta(), 4));
        parameters.put("CBTE_NUMERO", UTIL.AGREGAR_CEROS(fe.getCbteNumero(), 8));
//        parameters.put("CBTE_CONDICION_VENTA", cbte.getFormaPago() == 1 ? "Contado" : "Cta. Cte.");
        parameters.put("CBTE_CAE", fe.getCae());
        parameters.put("CBTE_CAE_VTO", fe.getCaeFechaVto());
        putClienteParameters(parameters, cbte.getCliente());
        parameters.put("CBTE_OBSERVACION", cbte.getObservacion());
        parameters.put("CBTE_TOTAL", cbte.getImporte());
        parameters.put("CBTE_GRAVADO", cbte.getGravado());
        if (parameters.get("CBTE_TIPO").equals("A")) {
            parameters.put("CBTE_NOGRAVADO", cbte.getNoGravado());
        } else {
            //se muestra como SubTotal
            parameters.put("CBTE_NOGRAVADO", cbte.getImporte());
        }
        parameters.put("CBTE_OTROSTRIB", BigDecimal.ZERO);
        parameters.put("CBTE_BONIF", BigDecimal.ZERO);
        BigDecimal iva27 = BigDecimal.ZERO;
        BigDecimal iva21 = BigDecimal.ZERO;
        BigDecimal iva105 = BigDecimal.ZERO;
        BigDecimal iva5 = BigDecimal.ZERO;
        BigDecimal iva205 = BigDecimal.ZERO;
        List<GenericBeanCollection> detalle = new ArrayList<>();
        for (DetalleNotaCredito d : cbte.getDetalle()) {
            BigDecimal alicuota = BigDecimal.valueOf(d.getProducto().getIva().getIva()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal precioUnitario = d.getPrecioUnitario();
            BigDecimal subTotal = precioUnitario.multiply(BigDecimal.valueOf(d.getCantidad())).setScale(4, RoundingMode.HALF_UP);
            if (cbte.getTipo() == 'A') {
                if (alicuota.doubleValue() > 0) {
                    BigDecimal iva = UTIL.getPorcentaje(subTotal, alicuota);
                    if (alicuota.toString().equals("10.5")) {
                        iva105 = iva105.add(iva);
                    } else if (alicuota.toString().equals("2.5")) {
                        iva205 = iva205.add(iva);
                    } else if (alicuota.intValue() == 5) {
                        iva5 = iva5.add(iva);
                    } else if (alicuota.intValue() == 21) {
                        iva21 = iva21.add(iva);
                    } else if (alicuota.intValue() == 27) {
                        iva27 = iva27.add(iva);
                    }
                }
            } else {
                subTotal = subTotal
                        .multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE)))
                        .setScale(2, RoundingMode.HALF_UP);
                precioUnitario = precioUnitario.multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE)))
                        .setScale(4, RoundingMode.HALF_UP);
                alicuota = null;
            }
            detalle.add(new GenericBeanCollection(d.getProducto().getCodigo(), d.getProducto().getNombre(), d.getCantidad(),
                    "Unitario", precioUnitario, BigDecimal.ZERO, alicuota, subTotal));
        }
        parameters.put("CBTE_IVA27", iva27);
        parameters.put("CBTE_IVA21", iva21);
        parameters.put("CBTE_IVA105", iva105);
        parameters.put("CBTE_IVA5", iva5);
        parameters.put("CBTE_IVA205", iva205);
        doReport(parameters, detalle);
    }

    /**
     *
     * @param parameters
     * @param detalle columnas: Código, Producto, Cantidad, Unidad, Precio Unit., Bonif., Alic IVA,
     * SubTotal
     * @throws MissingReportException
     */
    private void doReport(HashMap<String, Object> parameters, List<GenericBeanCollection> detalle) throws MissingReportException, JRException {
        DatosEmpresa de = new DatosEmpresaJpaController().findDatosEmpresa(1);
        parameters.put("EMP_LOGO", de.getLogo());
        parameters.put("EMP_RAZON_SOCIAL", de.getNombre());
        parameters.put("EMP_DOMICILIO", de.getDireccion());
        parameters.put("EMP_IVA", de.getContribuyente().getNombre());
        parameters.put("EMP_CUIT", de.getCuit());
        parameters.put("EMP_IIBB", de.getCuit());
        parameters.put("EMP_FECHA_INICIO", de.getFechaInicioActividad());
        parameters.put("CBTE_BARRA", generateBarcode(parameters.get("EMP_CUIT"), parameters.get("CBTE_TIPO_COD"), parameters.get("CBTE_PUNTO"), parameters.get("CBTE_CAE"), (Date) parameters.get("CBTE_CAE_VTO")));
        Reportes r = new Reportes("cbtemembrete", "Comprobante FE " + parameters.get("CBTE_NOMBRE")
                + " " + parameters.get("CBTE_TIPO")
                + " " + parameters.get("CBTE_PUNTO") + "-" + parameters.get("CBTE_NUMERO"));
        parameters.put("DETALLE", r.getBeanCollectionDataSource(detalle));
        for (Map.Entry<String, Object> entrySet : parameters.entrySet()) {
            r.addParameter(entrySet.getKey(), entrySet.getValue());
        }
        r.setjPrint(r.getJasperPrinter());
        //generación de reporte "duplicado"
        HashMap<String, Object> clone = (HashMap) parameters.clone();
        clone.put("DETALLE", r.getBeanCollectionDataSource(detalle));
        Reportes r2 = new Reportes("cbtemembrete", "Comprobante FE " + clone.get("CBTE_NOMBRE")
                + " " + clone.get("CBTE_TIPO")
                + " " + clone.get("CBTE_PUNTO") + "-" + clone.get("CBTE_NUMERO"));
        r2.setParameterMap(clone);
        r2.addParameter("CBTE_COPIA", "DUPLICADO");
        r.append(r2.getJasperPrinter());
        r.viewReport();
    }

    private void putClienteParameters(HashMap<String, Object> parameters, Cliente cliente) {
        parameters.put("CLIE_TIPODOC", cliente.getTipodoc().getNombre());
        parameters.put("CLIE_DOC", cliente.getNumDoc());
        parameters.put("CLIE_NOMBRE", cliente.getNombre());
        parameters.put("CLIE_DOMICILIO", cliente.getDireccion());
        parameters.put("CLIE_IVA", cliente.getContribuyente().getNombre());
    }

    /**
     * {@link http://www.afip.gov.ar/afip/resol170204.html}
     *
     * @return
     */
    public static String generateBarcode(Object cuit, Object tipoCod, Object punto, Object cae, Date caeVto) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String barcode = "" + cuit + tipoCod + punto + cae + sdf.format(caeVto);
        int a = 0, b = 0;
        for (char toCharArray : barcode.toCharArray()) {
            a += toCharArray;
        }
        a *= 3;
        for (int i = 0; i < barcode.length(); i++) {
            if ((i + 1) % 2 == 0) {
                b += barcode.charAt(i);
            }
        }
        a += b;
        int verificador = 0;
        while ((a + verificador) % 10 != 0) {
            verificador++;
        }
        LOG.trace("a=" + a + ", verificador=" + verificador);
        barcode += verificador;
        LOG.info("barcode (" + barcode.length() + ")=" + barcode);
        return barcode;
    }

}
