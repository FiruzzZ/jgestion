package jgestion.controller;

import jgestion.entity.CuentaBancaria;
import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Banco;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMBancoSucursales;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import jgestion.jpa.controller.BancoJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.general.UTIL;

/**
 *
 * @author Administrador
 */
public class BancoController {

    public final String CLASS_NAME = Banco.class.getSimpleName();
    private Banco EL_OBJECT;
    private final String[] colsName = {"id", "Nombre", "Página Web"};
    private final int[] colsWidth = {20, 120, 100};
    private JDContenedor contenedor;
    private JDABM abm;
    private PanelABMBancoSucursales panelABM;
    private boolean permitirFiltroVacio;
    private static final Logger LOG = LogManager.getLogger();
    private BancoJpaController jpaController = new BancoJpaController();

    public BancoController() {
    }

    public JDialog initContenedor(JFrame owner, boolean modal, boolean modoBuscador) throws DatabaseErrorException {
        contenedor = new JDContenedor(owner, modal, "ABM - " + CLASS_NAME);
        contenedor.hideBtmImprimir();
        contenedor.getTfFiltro().setToolTipText("Filtra por nombre del " + CLASS_NAME);
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
                    cargarContenedorTabla(null);
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
                    cargarContenedorTabla(null);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    LOG.error(ex);
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                }
            }
        });
        contenedor.getbBorrar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jpaController.remove(jpaController.find(Integer.valueOf(UTIL.getSelectedValue(contenedor.getjTable1(), 0).toString())));
                    cargarContenedorTabla(null);
                    contenedor.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (Exception ex) {
                    LOG.error(ex, ex);
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                }
            }
        });
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
            List<Banco> l;
            if (query == null) {
                l = jpaController.findAll();
            } else {
                l = jpaController.findByNativeQuery(query);
            }
            for (Banco o : l) {
                dtm.addRow(new Object[]{
                    o.getId(),
                    o.getNombre(),
                    o.getWebpage()
                });
            }
        }
    }

    /**
     * @see BancoJpaController#initABM(javax.swing.JDialog, boolean)
     * @param parent
     * @return
     * @throws MessageException
     */
    public JDialog initABM(Window parent) throws MessageException {
        return initABM(parent, false);
    }

    /**
     * Crea una instancia del ABM
     *
     * @param parent
     * @param isEditing
     * @return una ventana para la creación de Bancos
     * @throws MessageException
     */
    private JDialog initABM(Window parent, boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
        if (isEditing) {
            EL_OBJECT = getSelectedFromContenedor();
            if (EL_OBJECT == null) {
                throw new MessageException("Debe elegir una fila");
            }
        }
        return settingABM(parent, isEditing);
    }

    private JDialog settingABM(Window parent, boolean isEditing) {
        panelABM = new PanelABMBancoSucursales();
        panelABM.hideFieldsSucursal();
        abm = new JDABM(parent, "ABM - " + CLASS_NAME, true, panelABM);
        if (isEditing) {
            setPanelABM(EL_OBJECT);
        }
        abm.getbAceptar().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (EL_OBJECT == null) {
                        EL_OBJECT = new Banco();
                    }
                    setEntity(EL_OBJECT);
                    checkConstraints(EL_OBJECT);
                    String msg;
                    if (EL_OBJECT.getId() == null) {
                        jpaController.persist(EL_OBJECT);
                        msg = "Creado..";
                    } else {
                        jpaController.merge(EL_OBJECT);
                        msg = "Modificado..";
                    }
                    EL_OBJECT = null;
                    abm.showMessage(msg, CLASS_NAME, 1);
                    panelABM.clearFields();
                    cargarContenedorTabla(null);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
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

    private void setPanelABM(Banco o) {
        panelABM.getTfNombre().setText(o.getNombre());
        panelABM.getTfPaginaWeb().setText(o.getWebpage());
    }

    private Banco getSelectedFromContenedor() {
        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
        if (selectedRow > -1) {
            return jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
        } else {
            return null;
        }
    }

    private void checkConstraints(Banco o) throws MessageException {
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id!=" + o.getId() + " AND ";
        }
        Integer old = (Integer) jpaController.findAttribute("SELECT o.id FROM " + jpaController.getAlias()
                + " WHERE " + idQuery + " o.nombre='" + o.getNombre() + "' ");
        if (old != null) {
            throw new MessageException("Ya existe un banco con este nombre.");
        }
    }

    private void setEntity(Banco o) throws MessageException {
        String nombre;
        String webpage;
        if (panelABM.getTfNombre().getText() == null || panelABM.getTfNombre().getText().trim().length() < 1) {
            throw new MessageException("Debe ingresar un nombre");
        }
        webpage = panelABM.getTfPaginaWeb().getText().trim();
        if (webpage.isEmpty()) {
            webpage = null;
        }
        nombre = panelABM.getTfNombre().getText();
        o.setNombre(nombre);
        o.setWebpage(webpage);
    }

    public List<Banco> findWithCuentasBancarias(boolean activa) {
        return jpaController.findAll("SELECT o.banco FROM " + CuentaBancaria.class.getSimpleName()
                + " o WHERE o.activa=" + activa + " GROUP BY o.banco");
    }

    /**
     * Same result using
     * {@link #findWithCuentasBancarias(Boolean.FALSE)} &&  {@link #findWithCuentasBancarias(Boolean.TRUE)}
     *
     * @return
     */
    public List<Banco> findAllWithCuentasBancarias() {
        return jpaController.findAll("SELECT o.banco FROM " + CuentaBancaria.class.getSimpleName()
                + " o GROUP BY o.banco");
    }
}
