package jgestion.controller;

import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Especie;
import jgestion.gui.JDABM;
import jgestion.gui.PanelABMEspecie;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import jgestion.jpa.controller.EspecieJpaController;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class EspecieController {

    private final EspecieJpaController jpaController = new EspecieJpaController();
    private Especie entity;
    private PanelABMEspecie panelABM;

    public EspecieController() {
    }

    public Especie displayEspecie(Window owner, Especie toEdit) {
        panelABM = new PanelABMEspecie();
        if (toEdit != null) {
            entity = toEdit;
            panelABM.getTfImporte().setText(UTIL.PRECIO_CON_PUNTO.format(toEdit.getImporte()));
            panelABM.getTaDescripcion().setText(toEdit.getDescripcion());
        }
        final JDABM jd = new JDABM(owner, "ABM - Especie", true, panelABM);
        jd.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    validate();
                    setEntity();
//                    checkConstraints(entity);
                    jd.dispose();
                } catch (MessageException ex) {
                    ex.displayMessage(null);
                }
            }

            private void validate() throws MessageException {
                BigDecimal importe;
                try {
                    importe = new BigDecimal(panelABM.getTfImporte().getText().trim());
                } catch (Exception ex) {
                    throw new MessageException("Importe no válido");
                }
                if (importe.compareTo(BigDecimal.ZERO) != 1) {
                    throw new MessageException("Importe debe ser mayor a cero.");
                }
                if (panelABM.getTaDescripcion().getText().trim().isEmpty()) {
                    throw new MessageException("Descripción no válida");
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
            entity = new Especie();
        }
        entity.setImporte(new BigDecimal(panelABM.getTfImporte().getText().trim()));
        entity.setDescripcion(panelABM.getTaDescripcion().getText().trim());
    }
}
