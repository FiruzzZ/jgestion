package jgestion.controller;

import java.awt.Window;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Banco;
import jgestion.entity.BancoSucursal;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMBancoSucursales;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.controller.exceptions.ConstraintViolationJpaException;
import jgestion.jpa.controller.BancoJpaController;
import jgestion.jpa.controller.BancoSucursalJpaController;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class BancoSucursalController implements Serializable {

    public final String CLASS_NAME = BancoSucursal.class.getSimpleName();
    private BancoSucursal EL_OBJECT;
    private final String[] colsName = {"SucursalBanco.id", "Código", "Nombre", "Banco", "Dirección", "Teléfono"};
    private final int[] colsWidth = {20, 40, 100, 100, 100, 50};
    private JDABM abm;
    private PanelABMBancoSucursales panelABM;
    private JDContenedor contenedor;
    private boolean permitirFiltroVacio;
    private static final Logger LOG = LogManager.getLogger();
    private BancoSucursalJpaController dao = new BancoSucursalJpaController();

    public BancoSucursalController() {
    }

    public JDialog initContenedor(Window owner, boolean modal, boolean modoBuscador) {
        contenedor = new JDContenedor(owner, modal, "ABM - Sucursales de Banco");
        contenedor.getTfFiltro().setToolTipText("Filtra por nombre de la Sucursal de Banco");
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (contenedor.getTfFiltro().getText().trim().length() > 0) {
                    permitirFiltroVacio = true;
                    armarQuery(contenedor.getTfFiltro().getText().trim());
                } else {
                    if (permitirFiltroVacio) {
                        permitirFiltroVacio = false;
                        armarQuery(contenedor.getTfFiltro().getText().trim());
                    }
                }
            }
        });
        contenedor.setModoBuscador(modoBuscador);
        contenedor.getbNuevo().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    EL_OBJECT = null;
                    initABM(contenedor, false);
                    abm.setLocationRelativeTo(contenedor);
                    abm.setVisible(true);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
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
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LOG.error(ex.getMessage(), ex);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(evt -> {
            try {
                BancoSucursal toRemove = dao.find(Integer.valueOf(UTIL.getSelectedValueFromModel(contenedor.getjTable1(), 0).toString()));
                dao.remove(toRemove);
                contenedor.showMessage("Eliminado..", CLASS_NAME, 1);
            } catch (ConstraintViolationJpaException ex) {
                contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
            }
        });
        contenedor.getbImprimir().setVisible(false);
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        //no permite filtro de vacio en el inicio
        permitirFiltroVacio = false;
        cargarContenedorTabla(null);
        return contenedor;
    }

    /**
     * Arma la query, la cual va filtrar los datos en el JDContenedor
     *
     * @param filtro
     */
    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.nombre ILIKE '" + filtro + "%' ORDER BY o.nombre";
        }
        cargarContenedorTabla(query);
    }

    private void cargarContenedorTabla(String query) {
        if (contenedor != null) {
            DefaultTableModel dtm = contenedor.getDTM();
            UTIL.limpiarDtm(dtm);
            List<BancoSucursal> l;
            if (query == null) {
                l = dao.findAll();
            } else {
                l = dao.findByNativeQuery(query);
            }
            l.forEach(o -> {
                dtm.addRow(new Object[]{
                    o.getId(),
                    o.getCodigo(),
                    o.getNombre(),
                    o.getBanco().getNombre(),
                    o.getDireccion(),
                    o.getTelefono()
                });
            });
        }
    }

    /**
     * @see BancoJpaController#initABM(javax.swing.JDialog, boolean)
     * @param parent
     * @return
     * @throws MessageException
     */
    public JDialog initABM(JDialog parent) throws MessageException {
        return initABM(parent, false);
    }

    /**
     * Crea una instancia modal del ABM
     *
     * @param parent
     * @param isEditing
     * @return una ventana ABM
     * @throws MessageException
     */
    private JDialog initABM(JDialog parent, boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
        if (isEditing) {
            if (contenedor.getjTable1().getSelectedRow() == -1) {
                throw new MessageException("Debe elegir una fila");

            }
            EL_OBJECT = dao.find((Integer) UTIL.getSelectedValueFromModel(contenedor.getjTable1(), 0));
        }
        return getJDialogABM(parent, isEditing);
    }

    private JDialog getJDialogABM(JDialog parent, boolean isEditing) {
        panelABM = new PanelABMBancoSucursales();
        UTIL.loadComboBox(panelABM.getCbBancos(), new BancoJpaController().findAll(), false);
        abm = new JDABM(parent, "ABM - " + CLASS_NAME, true, panelABM);
        if (isEditing) {
            setPanelABM(EL_OBJECT);
        }
        abm.getbAceptar().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (EL_OBJECT == null) {
                        EL_OBJECT = new BancoSucursal();
                    }
                    setEntity(EL_OBJECT);
                    checkConstraints(EL_OBJECT);
                    String msg;
                    if (EL_OBJECT.getId() == null) {
                        dao.persist(EL_OBJECT);
                        msg = "Creado..";
                    } else {
                        dao.merge(EL_OBJECT);
                        msg = "Modificado..";
                    }
                    EL_OBJECT = null;
                    abm.showMessage(msg, CLASS_NAME, 1);
                    panelABM.clearFields();
                    cargarContenedorTabla(null);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    LOG.error(ex.getMessage(), ex);
                    abm.showMessage(ex.getMessage(), "Algo salió mal", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        abm.getbCancelar().addActionListener(evt -> {
            EL_OBJECT = null;
            abm.dispose();
        });
        return abm;
    }

    /**
     * Chequea que:
     * <ul>
     * <li>El nombre de la sucursal sea único para ese banco</li>
     * <li>El código sea único</li>
     * </ul>
     *
     * @param o
     * @throws MessageException
     */
    private void checkConstraints(BancoSucursal o) throws MessageException {
        BancoSucursal old = dao.findByNombre(o.getBanco(), o.getNombre());
        if (old != null && !Objects.equals(o, old)) {
            throw new MessageException("Ya existe otra Sucursal de Banco con este nombre.");
        }
        old = dao.findByCodigo(o.getCodigo());
        if (old != null && !Objects.equals(o, old)) {
            throw new MessageException("Ya existe otra Sucursal de Banco con este código.");
        }
    }

    private void setPanelABM(BancoSucursal bancoSucursal) {
        UTIL.setSelectedItem(panelABM.getCbBancos(), bancoSucursal.getBanco());
        panelABM.getTfCodigo().setText(bancoSucursal.getCodigo());
        panelABM.getTfNombre().setText(bancoSucursal.getNombre());
        panelABM.getTfDireccion().setText(bancoSucursal.getDireccion());
        if (bancoSucursal.getTelefono() != null) {
            panelABM.getTfTelefono().setText(bancoSucursal.getTelefono().toString());
        }
    }

    private void setEntity(BancoSucursal o) throws MessageException {
        String nombre, codigo, direccion;
        Long telefono = null;
        Banco banco;

        if (panelABM.getTfCodigo().getText().trim().isEmpty()) {
            throw new MessageException("Debe ingresar un código");
        }
        if (panelABM.getTfNombre().getText().trim().isEmpty()) {
            throw new MessageException("Debe ingresar un nombre");
        }

        if (!panelABM.getTfTelefono().getText().isEmpty()) {
            try {
                if (panelABM.getTfTelefono().getText().trim().length() > 12) {
                    throw new MessageException("Número de teléfono no puede tener mas de 12 dígitos.");
                }
                telefono = Long.valueOf(panelABM.getTfTelefono().getText());
            } catch (NumberFormatException ex) {
                throw new MessageException("Número de teléfono no válido, debe ingresar solo números.");
            }
        }
        nombre = StringUtils.trimToNull(panelABM.getTfNombre().getText());
        codigo = StringUtils.trimToNull(panelABM.getTfCodigo().getText());
        direccion = StringUtils.trimToNull(panelABM.getTfDireccion().getText());
        banco = (Banco) panelABM.getCbBancos().getSelectedItem();
        o.setNombre(nombre);
        o.setCodigo(codigo);
        o.setDireccion(direccion);
        o.setTelefono(telefono);
        o.setBanco(banco);
    }
}
