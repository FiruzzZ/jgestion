package controller;

import controller.exceptions.MessageException;
import entity.Banco;
import entity.CuentaBancaria;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMCuentabancaria;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jpa.controller.CuentabancariaJpaController;
import org.apache.log4j.Logger;
import utilities.general.UTIL;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @author FiruzzZ
 */
public class CuentabancariaController {

    private CuentabancariaJpaController jpaController;
    private JDABM abm;
    private JDContenedor contenedor;
    private boolean permitirFiltroVacio;
    private CuentaBancaria EL_OBJECT;
    private static final Logger LOG = Logger.getLogger(CuentabancariaController.class.getName());
    private PanelABMCuentabancaria panelABM;

    public CuentabancariaController() {
        jpaController = new CuentabancariaJpaController();
    }

    public JDialog initContenedor(JFrame owner) throws MessageException {
        contenedor = new JDContenedor(owner, true, "Administrar Cuentas Bancarias");
//        contenedor.getTfFiltro().setToolTipText("Filtra por nombre de la Sucursal de Banco");
//        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyReleased(KeyEvent e) {
//                if (contenedor.getTfFiltro().getText().trim().length() > 0) {
//                    permitirFiltroVacio = true;
//                    armarQuery(contenedor.getTfFiltro().getText().trim());
//                } else {
//                    if (permitirFiltroVacio) {
//                        permitirFiltroVacio = false;
//                        armarQuery(contenedor.getTfFiltro().getText().trim());
//                    }
//                }
//            }
//        });
        contenedor.getbNuevo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EL_OBJECT = null;
                    initABM(contenedor, false);
                    abm.setLocationRelativeTo(contenedor);
                    abm.setVisible(true);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                }
            }
        });
        contenedor.getbModificar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    initABM(contenedor, true);
                    abm.setLocationRelativeTo(contenedor);
                    abm.setVisible(true);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    LOG.error(ex.getMessage(), ex);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    remove(jpaController.find(Integer.valueOf(UTIL.getSelectedValue(contenedor.getjTable1(), 0).toString())));
                    contenedor.showMessage("Eliminado..", jpaController.getEntityClass().getSimpleName(), 1);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 0);
                    LOG.error(ex.getMessage(), ex);
                }
            }
        });
        contenedor.getbImprimir().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), new String[]{"id", "Banco", "N° Cuenta", "Activa"}, new int[]{1, 200, 80, 30});
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        //no permite filtro de vacio en el inicio
        permitirFiltroVacio = false;
        cargarContenedorTabla(null);
        return contenedor;
    }

    /**
     * Crea una instancia del ABM
     *
     * @param parent
     * @param isEditing
     * @return una ventana para la creación de Bancos
     * @throws MessageException
     */
    private JDialog initABM(JDialog parent, boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        if (isEditing) {
            EL_OBJECT = getSelectedFromContenedor();
            if (EL_OBJECT == null) {
                throw new MessageException("Debe elegir una fila");
            }
        }
        return settingABM(parent, isEditing);
    }

    private CuentaBancaria getSelectedFromContenedor() {
        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            return jpaController.find(Integer.valueOf(contenedor.getDTM().getValueAt(selectedRow, 0).toString()));
        } else {
            return null;
        }
    }

    private JDialog settingABM(JDialog parent, boolean isEditing) {
        panelABM = new PanelABMCuentabancaria();
        UTIL.loadComboBox(panelABM.getCbBancos(), JGestionUtils.getWrappedBancos(new BancoController().findEntities()), false);
        if (isEditing) {
            setPanelABM(EL_OBJECT);
        }
        abm = new JDABM(parent, "ABM - " + jpaController.getEntityClass().getSimpleName(), true, panelABM);
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (EL_OBJECT == null) {
                        EL_OBJECT = new CuentaBancaria();
                        EL_OBJECT.setSaldo(BigDecimal.ZERO);
                    }
                    setEntity(EL_OBJECT);
                    checkConstraints(EL_OBJECT);
                    String msg;
                    if (EL_OBJECT.getId() == null) {
                        jpaController.create(EL_OBJECT);
                        msg = "Creado..";
                    } else {
                        jpaController.merge(EL_OBJECT);
                        msg = "Modificado..";
                    }
                    EL_OBJECT = null;
                    cargarContenedorTabla(null);
                    abm.showMessage(msg, jpaController.getEntityClass().getSimpleName(), 1);
                    abm.dispose();
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), jpaController.getEntityClass().getSimpleName(), 2);
                } catch (Exception ex) {
                    LOG.error(ex);
                }
            }
        });
        abm.getbCancelar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EL_OBJECT = null;
                abm.dispose();
            }
        });
        return abm;
    }

    private void setEntity(CuentaBancaria o) throws MessageException {
        o.setBanco(((ComboBoxWrapper<Banco>) panelABM.getCbBancos().getSelectedItem()).getEntity());
        String numeroCuenta = panelABM.getTfNumero().getText();
        if (numeroCuenta == null || numeroCuenta.trim().length() < 1) {
            throw new MessageException("Debe ingresar el número de cuenta");
        }

        try {
            o.setNumero(Long.valueOf(numeroCuenta));
        } catch (NumberFormatException ex) {
            throw new MessageException("Número de cuenta no válido, ingrese solo números enteros");
        } catch (Exception ex) {
            throw new MessageException("Número de cuenta no válido, ingrese solo números enteros");
        }
        o.setActiva(panelABM.getjCheckBox1().isSelected());
    }

    private void checkConstraints(CuentaBancaria o) throws MessageException {
        String idquery = o.getId() != null ? "o.id <>" + o.getId() + " AND " : "";
        if (!jpaController.findByQuery("SELECT o FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + "WHERE " + idquery + " o.banco.id=" + o.getBanco().getId() + " AND o.numero=" + o.getNumero()).isEmpty()) {
            throw new MessageException("Ya existe un registro del Banco " + o.getBanco().getNombre() + " con el N° " + o.getNumero());
        }
    }

    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + jpaController.getEntityClass().getSimpleName() + " o WHERE o.numero >= " + filtro;
        }
        cargarContenedorTabla(query);
    }

    private void cargarContenedorTabla(String query) {
        if (contenedor != null) {
            DefaultTableModel dtm = contenedor.getDTM();
            dtm.setRowCount(0);
            List<CuentaBancaria> l;
            if (query == null) {
                l = jpaController.findAll();
            } else {
                l = jpaController.findByNativeQuery(query);
            }
            for (CuentaBancaria o : l) {
                dtm.addRow(new Object[]{o.getId(), o.getBanco().getNombre(), o.getNumero(), o.getActiva()});
            }
        }
    }

    private void remove(CuentaBancaria find) throws MessageException {
        try {
            jpaController.remove(find);
        } catch (Exception e) {
            throw new MessageException(e.getLocalizedMessage());
        }
    }

    private void setPanelABM(CuentaBancaria o) {
        UTIL.setSelectedItem(panelABM.getCbBancos(), o.getBanco().getNombre());
        panelABM.getTfNumero().setText(o.getNumero().toString());
        panelABM.getjCheckBox1().setSelected(o.getActiva());
    }
}
