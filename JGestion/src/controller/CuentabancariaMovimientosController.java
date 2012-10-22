package controller;

import entity.CuentabancariaMovimientos;
import gui.JDCuentabancariaManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.table.DefaultTableModel;
import jpa.controller.CuentabancariaMovimientosJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaMovimientosController {

    private static final Logger LOG = Logger.getLogger(CuentabancariaMovimientosController.class.getName());
    private CuentabancariaMovimientosJpaController jpaController;
    private JDCuentabancariaManager manager;

    public CuentabancariaMovimientosController() {
        jpaController = new CuentabancariaMovimientosJpaController();
    }

    public JDialog getContenedor(Window owner) {
        manager = new JDCuentabancariaManager(owner);
        manager.getBtnBuscar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String jpql = armarQuery();
                cargarManagerTable(jpql);
            }
        });
        return manager;
    }

    private String armarQuery() {
        StringBuilder query = new StringBuilder("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + "WHERE o.id is not null ");
        if (manager.getCbBancos().getSelectedIndex() > 0) {
            query.append(" AND o.cuentaBancaria.banco.id=").append(((ComboBoxWrapper<?>) manager.getCbBancos().getSelectedItem()).getId());
        } else {
            for (int i = 1; i < manager.getCbBancos().getItemCount(); i++) {
                query.append(" AND o.cuentaBancaria.banco.id=").append(((ComboBoxWrapper<?>) manager.getCbBancos().getItemAt(i)).getId());

            }
        }
        if (manager.getCbCuentabancaria().getSelectedIndex() > 0) {
            query.append(" AND o.cuentaBancaria.id=").append(((ComboBoxWrapper<?>) manager.getCbCuentabancaria().getSelectedItem()).getId());
        } else {
            //if (no seleccionó banco) => cuentas bancarias está vacía
            if (manager.getCbBancos().getSelectedIndex() > 0) {
                for (int i = 1; i < manager.getCbBancos().getItemCount(); i++) {
                    query.append(" AND o.cuentaBancaria.id=").append(((ComboBoxWrapper<?>) manager.getCbCuentabancaria().getItemAt(i)).getId());

                }
            }
        }
        if (manager.getCbOperacionesBancariasFiltro().getSelectedIndex() > 0) {
            query.append(" AND o.operacionesBancarias.id=").append(((ComboBoxWrapper<?>) manager.getCbOperacionesBancariasFiltro().getSelectedItem()).getId());
        } 
        if (manager.getDcDesde() != null || manager.getDcHasta() != null) {
            if (manager.getRbOperacion()) {
                if (manager.getDcDesde() != null) {
                    query.append(" AND o.fechaOperacion >='").append(UTIL.DATE_FORMAT.format(manager.getDcDesde())).append("'");
                }
                if (manager.getDcHasta() != null) {
                    query.append(" AND o.fechaOperacion <='").append(UTIL.DATE_FORMAT.format(manager.getDcHasta())).append("'");
                }
            } else {
                if (manager.getDcDesde() != null) {
                    query.append(" AND o.fechaCreditoDebito >='").append(UTIL.DATE_FORMAT.format(manager.getDcDesde())).append("'");
                }
                if (manager.getDcHasta() != null) {
                    query.append(" AND o.fechaCreditoDebito <='").append(UTIL.DATE_FORMAT.format(manager.getDcHasta())).append("'");
                }
            }
        }
        LOG.debug(query.toString());
        return query.toString();
    }

    private void cargarManagerTable(String jpql) {
        DefaultTableModel dtm = (DefaultTableModel) manager.getjTable1().getModel();
        dtm.setRowCount(0);
        List<CuentabancariaMovimientos> l = jpaController.findByQuery(jpql);
        for (CuentabancariaMovimientos o : l) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getDescripcion(),
                        o.getCredito(),
                        o.getDebito(),
                        o.getFechaCreditoDebito(),
                        o.getOperacionesBancarias().getNombre(),
                        o.getFechaOperacion(),
                        o.getUsuario(),
                        o.getFechaSistema()
                    });
        }
    }
}
