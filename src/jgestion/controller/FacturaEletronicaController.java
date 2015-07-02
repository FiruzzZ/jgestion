package jgestion.controller;

import afip.ws.exception.WSAFIPErrorResponseException;
import generics.WaitingDialog;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import javax.swing.JOptionPane;
import jgestion.JGestionUtils;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.FacturaElectronica;
import jgestion.entity.FacturaVenta;
import jgestion.entity.Sucursal;
import jgestion.jpa.controller.FacturaElectronicaJpaController;
import jgestion.jpa.controller.FacturaVentaJpaController;
import jgestion.jpa.controller.SucursalJpaController;
import org.apache.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class FacturaEletronicaController {

    private static final Logger LOG = Logger.getLogger(FacturaEletronicaController.class);
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

    public FacturaEletronicaController() {
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
}
