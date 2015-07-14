package jgestion.controller;

import afip.ws.exception.WSAFIPErrorResponseException;
import generics.GenericBeanCollection;
import generics.WaitingDialog;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
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
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaElectronica;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Sucursal;
import jgestion.jpa.controller.FacturaElectronicaJpaController;
import jgestion.jpa.controller.FacturaVentaJpaController;
import jgestion.jpa.controller.SucursalJpaController;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class FacturaElectronicaController {

    private static final Logger LOG = Logger.getLogger(FacturaElectronicaController.class);
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
                        for (FacturaElectronica fe : l) {
                            if (fe.getCbteTipo() == 1 || fe.getCbteTipo() == 6 || fe.getCbteTipo() == 11) {
                                Sucursal s = new SucursalJpaController().findByPuntoVenta(fe.getPtoVta());
                                char tipo = fe.getCbteTipo() == 1 ? 'A'
                                        : fe.getCbteTipo() == 6 ? 'B' : 'C';
                                FacturaVenta fv = new FacturaVentaJpaController().findBy(s, tipo, (int) fe.getCbteNumero());
                                waiting.appendMessage("consultando CAE de " + JGestionUtils.getNumeracion(fv), true, true);
                                FacturaElectronica fee = null;
                                try {
                                    /**
                                     * Si en el sistema figura como CAE pendiente, pero la consulta
                                     * del comprobante retorna algo puede que haya sucedido un error
                                     * y no se guardó el CAE retornado
                                     */
                                    fee = afipwsController.getFEComprobante(fv.getSucursal().getPuntoVenta(), tipo, (int) fv.getNumero());
                                } catch (WSAFIPErrorResponseException ex) {
                                    waiting.appendMessage(ex.getMessage(), true, true);
                                }
                                try {
                                    if (fee == null) {
                                        waiting.appendMessage("solicitando CAE de " + JGestionUtils.getNumeracion(fv), true, true);
                                        fee = afipwsController.requestCAE(fv);
                                    }
                                    fe.setCae(fee.getCae());
                                    fe.setCaeFechaVto(fee.getCaeFechaVto());
                                    fe.setObservaciones(fee.getObservaciones());
                                    fe.setResultado(fee.getResultado());
                                    fe.setFechaProceso(fee.getFechaProceso());
                                    fe.setObservaciones(fee.getObservaciones());
                                    LOG.info(fe.toString());
                                    feJpaController.merge(fe);
                                    waiting.appendMessage("guardando comprobante..", true, true);
                                } catch (WSAFIPErrorResponseException | MessageException ex) {
                                    waiting.appendMessage(ex.getMessage(), true, true);
                                }
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

    /**
     *
     * @param fv
     * @return
     */
    public FacturaElectronica findBy(FacturaVenta fv) {
        return jpaController.findBy(AFIPWSController.getTipoComprobanteID(fv), fv.getSucursal().getPuntoVenta(), fv.getNumero());
    }

    public void doReport(FacturaVenta fv) throws MissingReportException, JRException, MessageException {
        FacturaElectronica fe = findBy(fv);
        if(fe.getCae() == null) {
            throw new MessageException("el comprobante " + JGestionUtils.getNumeracion(fv) + " aún no posee CAE");
        }
        HashMap<String, Object> parameters = new HashMap<>(30);
        parameters.put("CBTE_TIPO", fv.getTipo() + "");
        //tiene que tener 2 dígitos el cod tipo cbte
        parameters.put("CBTE_TIPO_COD", fe.getCbteTipo() > 9 ? fe.getCbteTipo() : "0" + fe.getCbteTipo());
        parameters.put("CBTE_NOMBRE", "FACTURA");
        parameters.put("CBTE_FECHA_EMISION", fv.getFechaVenta());
        parameters.put("CBTE_PUNTO", UTIL.AGREGAR_CEROS(fe.getPtoVta(), 4));
        parameters.put("CBTE_NUMERO", UTIL.AGREGAR_CEROS(fe.getCbteNumero(), 8));
        parameters.put("CBTE_CAE", fe.getCae());
        parameters.put("CBTE_CAE_VTO", fe.getCaeFechaVto());
        putClienteParameters(parameters, fv.getCliente());
        List<GenericBeanCollection> detalle = new ArrayList<>();
        parameters.put("CBTE_TOTAL", fv.getImporte());
        parameters.put("CBTE_GRAVADO", fv.getGravado());
        if (parameters.get("CBTE_TIPO").equals("A")) {
            parameters.put("CBTE_NOGRAVADO", fv.getNoGravado());
        } else {
            //se muestra como SubTotal
            parameters.put("CBTE_NOGRAVADO", fv.getImporte());
        }
        parameters.put("CBTE_OTROSTRIB", BigDecimal.ZERO);
        parameters.put("CBTE_BONIF", BigDecimal.ZERO);
        BigDecimal iva27 = BigDecimal.ZERO;
        BigDecimal iva21 = BigDecimal.ZERO;
        BigDecimal iva105 = BigDecimal.ZERO;
        BigDecimal iva5 = BigDecimal.ZERO;
        BigDecimal iva205 = BigDecimal.ZERO;
        for (DetalleVenta d : fv.getDetallesVentaList()) {
            BigDecimal alicuota = BigDecimal.valueOf(d.getProducto().getIva().getIva()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal subTotal = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())).setScale(4, RoundingMode.HALF_UP);
            if (fv.getTipo() == 'A') {
                if (alicuota.doubleValue() > 0) {
                    BigDecimal iva = subTotal.divide(alicuota, 2, RoundingMode.HALF_UP);
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
            }
            detalle.add(new GenericBeanCollection(d.getProducto().getCodigo(), d.getProducto().getNombre(), d.getCantidad(),
                    "Unitario", d.getPrecioUnitario(), d.getDescuento(), alicuota, subTotal));
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
