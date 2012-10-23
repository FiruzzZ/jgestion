package controller;

import controller.exceptions.MessageException;
import entity.ComprobanteRetencion;
import gui.JDABM;
import gui.PanelABMComprobanteRetencion;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.logging.Level;
import jpa.controller.ComprobanteRetencionJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class ComprobanteRetencionController {

    private static final Logger LOG = Logger.getLogger(ComprobanteRetencionController.class.getName());
    /**
     * mutables
     */
    private ComprobanteRetencion entity;
    private PanelABMComprobanteRetencion panelABM;
    private final ComprobanteRetencionJpaController jpaController;

    public ComprobanteRetencionController() {
        jpaController = new ComprobanteRetencionJpaController();
    }

    public ComprobanteRetencion displayComprobanteRetencion(Window owner, ComprobanteRetencion toEdit) {
        panelABM = new PanelABMComprobanteRetencion();
        if (toEdit != null) {
            entity = toEdit;
            panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(toEdit.getImporte()));
            panelABM.getTfNumero().setText(toEdit.getNumero().toString());
            panelABM.getDcFecha().setDate(toEdit.getFecha());
        }
        final JDABM jd = new JDABM(owner, null, true, panelABM);
        jd.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    validate();
                    setEntity();
                    checkConstraints(entity);
                    jd.dispose();
                } catch (MessageException ex) {
                    ex.displayMessage(null);
                }
            }

            private void checkConstraints(ComprobanteRetencion o) throws MessageException {
                ComprobanteRetencion old = jpaController.findByNumero(o.getNumero());
                if ((o.getId() == null && old != null) || o.equals(old)) {
                    throw new MessageException("Ya existe un comprobante de retención con el N° " + old.getNumero());
                }
            }

            private void validate() throws MessageException {
                try {
                    Long.parseLong(panelABM.getTfNumero().getText());
                } catch (Exception exception) {
                    throw new MessageException("Número de comprobante no válido");
                }
                BigDecimal importe;
                try {
                    importe = new BigDecimal(panelABM.getTfImporte().getText().trim());
                } catch (Exception ex) {
                    throw new MessageException("Importe no válido");
                }
                if (importe.compareTo(BigDecimal.ZERO) != 1) {
                    throw new MessageException("Importe debe ser mayor a cero.");
                }
                if (panelABM.getDcFecha().getDate() == null) {
                    throw new MessageException("Fecha de comprobante no válida");
                }
            }
        });
        jd.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                entity = null;
                jd.dispose();
            }
        });
        
        jd.setLocationRelativeTo(owner);
        jd.setVisible(true);
        return entity;
    }

    private void setEntity() {
        if (entity == null) {
            entity = new ComprobanteRetencion();
        }
        entity.setImporte(new BigDecimal(panelABM.getTfImporte().getText().trim()));
        entity.setNumero(Long.parseLong(panelABM.getTfNumero().getText()));
        entity.setFecha(panelABM.getDcFecha().getDate());
    }
}
