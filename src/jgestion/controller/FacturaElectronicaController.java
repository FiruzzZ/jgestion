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
import jgestion.entity.DetalleVenta;
import jgestion.entity.FacturaElectronica;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Sucursal;
import jgestion.jpa.controller.FacturaElectronicaJpaController;
import jgestion.jpa.controller.FacturaVentaJpaController;
import jgestion.jpa.controller.SucursalJpaController;
import net.sf.jasperreports.engine.JRException;
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

    public void doReport(FacturaVenta fv) throws MessageException, MissingReportException, JRException {
        FacturaElectronica fe = findBy(fv);
        HashMap<String, Object> parameters = new HashMap<>(30);
        parameters.put("CBTE_TIPO", fv.getTipo());
        parameters.put("CBTE_TIPO_COD", fe.getCbteTipo());
        parameters.put("CBTE_NOMBRE", "FACTURA");
        parameters.put("CBTE_FECHA_EMISION", fv.getFechaVenta());
        parameters.put("CBTE_PUNTO", UTIL.AGREGAR_CEROS(fe.getPtoVta(), 4));
        parameters.put("CBTE_NUMERO", UTIL.AGREGAR_CEROS(fe.getCbteNumero(), 8));
        parameters.put("CBTE_CAE", fe.getCae());
        parameters.put("CBTE_CAE_VTO", fe.getCaeFechaVto());
        putClienteParameters(parameters, fv.getCliente());
        List<GenericBeanCollection> detalle = new ArrayList<>();
        for (DetalleVenta d : fv.getDetallesVentaList()) {
            BigDecimal alicuota = null;
            BigDecimal subTotal = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
            if (fv.getTipo() == 'A') {
                alicuota = BigDecimal.valueOf(d.getProducto().getIva().getIva());
                subTotal = subTotal
                        .multiply((alicuota.divide(new BigDecimal("100")).add(BigDecimal.ONE)))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            detalle.add(new GenericBeanCollection(d.getProducto().getCodigo(), d.getProducto().getNombre(), d.getCantidad(),
                    "Unitario", d.getPrecioUnitario(), d.getDescuento(), alicuota, subTotal));
        }
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
        parameters.put("CBTE_BARRA", generateBarcode(parameters.get("EMP_CUIT"), parameters.get("CBTE_TIPOCOD"), parameters.get("CBTE_PUNTO"), parameters.get("CBTE_CAE"), (Date) parameters.get("CBTE_CAE_VTO")));
        Reportes r = new Reportes("cbtemembrete", "Comprobante FE " + parameters.get("CBTE_NOMBRE")
                + " " + parameters.get("CBTE_TIPO")
                + " " + parameters.get("CBTE_PUNTO") + "-" + parameters.get("CBTE_NUMERO"));
        parameters.put("DETALLE", r.getBeanCollectionDataSource(detalle));
        for (Map.Entry<String, Object> entrySet : parameters.entrySet()) {
            r.addParameter(entrySet.getKey(), entrySet.getValue());
        }
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
        while (a % 10 != 0) {
            verificador++;
            a += verificador;
        }
        barcode += verificador;
        return barcode;
    }

}
