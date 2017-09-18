package jgestion.controller;

import jgestion.jpa.controller.ProvinciaJpaController;
import java.awt.Window;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Cliente;
import jgestion.entity.Proveedor;
import java.awt.event.KeyEvent;
import java.util.List;
import jgestion.entity.Contribuyente;
import jgestion.entity.Departamento;
import jgestion.entity.Municipio;
import jgestion.entity.Provincia;
import utilities.general.UTIL;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMProveedores;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.math.BigDecimal;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.jpa.controller.ProveedorJpaController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author FiruzzZ
 */
public class ProveedorController implements ActionListener {

    private static final Logger LOG = LogManager.getLogger();
    public final String CLASS_NAME = Proveedor.class.getSimpleName();
    private final String[] colsName = {"ID", "Código", "Razón social", "CUIT", "Teléfonos"};
    private final int[] colsWidth = {10, 20, 120, 40, 90};
    private JDContenedor contenedor;
    private JDABM abm;
    private Proveedor EL_OBJECT;
    private PanelABMProveedores panelABM;
    private final ProveedorJpaController jpaController;

    public ProveedorController() {
        jpaController = new ProveedorJpaController();
    }

    public List<Proveedor> findAll() {
        return jpaController.findAllLite();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try { //global error catcher
            // <editor-fold defaultstate="collapsed" desc="JButton">
            if (e.getSource().getClass().equals(JButton.class)) {
                JButton boton = (JButton) e.getSource();
                if (boton.getName().equalsIgnoreCase("new")) {
                    try {
                        EL_OBJECT = null;
                        initABM(false);
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                } else if (boton.getName().equalsIgnoreCase("edit")) {
                    try {
                        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
                        if (selectedRow > -1) {
                            EL_OBJECT = jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                            initABM(true);
                        }
                    } catch (MessageException ex) {
                        contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                } else if (boton.equals(contenedor.getbBorrar())) {
                    try {
                        Integer selectedRow = contenedor.getjTable1().getSelectedRow();
                        if (selectedRow > -1) {
                            jpaController.remove(jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString())));
                            cargarContenedorTabla(contenedor.getDTM());
                        } else {
                            throw new MessageException("No hay " + CLASS_NAME + " seleccionada");
                        }
                    } catch (MessageException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), null, JOptionPane.WARNING_MESSAGE);
                    }
                } else if (boton.getName().equalsIgnoreCase("Print")) {
                } else if (boton.getName().equalsIgnoreCase("exit")) {
                    contenedor.dispose();
                    contenedor = null;
                } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                    try {
                        setEntity();
                        String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                        checkConstraints(EL_OBJECT);
                        if (EL_OBJECT.getId() == null) {
                            jpaController.persist(EL_OBJECT);
                        } else {
                            jpaController.merge(EL_OBJECT);
                        }
                        abm.showMessage(msg, CLASS_NAME, 1);
                        cargarContenedorTabla(contenedor.getDTM());
                        abm.dispose();
                    } catch (MessageException ex) {
                        abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    }
                } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                    abm.dispose();
                    panelABM = null;
                    abm = null;
                    EL_OBJECT = null;
                } else if (boton.getName().equalsIgnoreCase("bDepartamentoS")) {
                    new DepartamentoController().initContenedor(null, true);
                    //cuando cierra el abm
                    if (panelABM.getCbProvincias().getSelectedIndex() > 0) {
                        UTIL.loadComboBox(panelABM.getCbDepartamentos(),
                                new DepartamentoController().findDeptosFromProvincia(
                                        ((Provincia) panelABM.getCbProvincias().getSelectedItem()).getId()), true);
                    } else {
                        UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
                    }

                } else if (boton.getName().equalsIgnoreCase("bmunicipios")) {
                    new MunicipioController().initContenedor(null, true);
                    if (panelABM.getCbDepartamentos().getSelectedIndex() > 0) {
                        UTIL.loadComboBox(panelABM.getCbMunicipios(),
                                new MunicipioController().findMunicipiosFromDepto(
                                        ((Departamento) panelABM.getCbDepartamentos().getSelectedItem()).getId()), true);
                    } else {
                        UTIL.loadComboBox(panelABM.getCbMunicipios(), null, true);
                    }
                }
            } // </editor-fold>
            // <editor-fold defaultstate="collapsed" desc="ComboBox">
            else if (e.getSource().getClass().equals(JComboBox.class)) {
                JComboBox combo = (JComboBox) e.getSource();
                if (combo.equals(panelABM.getCbProvincias())) {
                    if (combo.getSelectedIndex() > 0) {
                        UTIL.loadComboBox(panelABM.getCbDepartamentos(), new DepartamentoController().findDeptosFromProvincia(((Provincia) combo.getSelectedItem()).getId()), true);
                    } else {
                        UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
                    }

                } else if (combo.equals(panelABM.getCbDepartamentos())) {
                    if (combo.getSelectedIndex() > 0) {
                        UTIL.loadComboBox(panelABM.getCbMunicipios(), new MunicipioController().findMunicipiosFromDepto(((Departamento) combo.getSelectedItem()).getId()), true);
                    } else {
                        UTIL.loadComboBox(panelABM.getCbMunicipios(), null, true);
                    }
                }
            }
            // </editor-fold>
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Error inesperado", JOptionPane.ERROR_MESSAGE);
            LOG.error("actionPerformed", ex);
        }
    }

    public JDialog initContenedor(Window owner, boolean modal) {
        contenedor = new JDContenedor(owner, modal, "ABM - " + CLASS_NAME);
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                armarQuery(contenedor.getTfFiltro().getText().trim());
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        cargarContenedorTabla(contenedor.getDTM());
        //listener
        contenedor.setListener(this);
        return contenedor;
    }

    private void cargarContenedorTabla(DefaultTableModel dtm) {
        cargarContenedorTabla(dtm, null);
    }

    private void cargarContenedorTabla(DefaultTableModel dtm, String query) {
        UTIL.limpiarDtm(dtm);
        List<Proveedor> l;
        if (query == null) {
            l = jpaController.findAll();
        } else {
            l = jpaController.findByNativeQuery(query);
        }

        for (Proveedor o : l) {
            dtm.addRow(new Object[]{
                o.getId(),
                o.getCodigo(),
                o.getNombre(),
                o.getCuit(),
                ((o.getTele1() != null) ? o.getTele1().toString() : "-")
                + ((o.getTele2() != null) ? o.getTele2().toString() : "-")
            });
        }
    }

    private void initABM(boolean isEditing) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_PROVEEDORES);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }
        panelABM = new PanelABMProveedores();
        UTIL.loadComboBox(panelABM.getCbCondicIVA(), new ContribuyenteController().findAll(), false);
        UTIL.loadComboBox(panelABM.getCbProvincias(), new ProvinciaJpaController().findAll(), true);
        UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
        UTIL.loadComboBox(panelABM.getCbMunicipios(), null, true);
        panelABM.setListener(this);
        if (isEditing) {
            setPanel(EL_OBJECT);
        }
        abm = new JDABM(contenedor, "ABM " + CLASS_NAME + "es", true, panelABM);
        abm.setListener(this);
        abm.setLocationRelativeTo(contenedor);
        abm.setVisible(true);
    }

    private void setPanel(Proveedor o) {
        panelABM.setTfCodigo(o.getCodigo());
        panelABM.setTfNombre(o.getNombre());
        panelABM.setTfDireccion(o.getDireccion());
        panelABM.setTfNumDocumento(String.valueOf(o.getCuit()));
        panelABM.getTfLimiteCtaCte().setText(o.getLimiteCtaCte().intValue() + "");
        UTIL.setSelectedItem(panelABM.getCbCondicIVA(), o.getContribuyente().getNombre());
        UTIL.setSelectedItem(panelABM.getCbProvincias(), o.getProvincia().getNombre());
        UTIL.setSelectedItem(panelABM.getCbDepartamentos(), o.getDepartamento().getNombre());
        UTIL.setSelectedItem(panelABM.getCbMunicipios(), o.getMunicipio().getNombre());

        if (o.getCodigopostal() != null) {
            panelABM.setTfCP(o.getCodigopostal().toString());
        }

        if (o.getTele1() != null) {
            panelABM.setTfTele1(o.getTele1().toString());
            if (o.getInterno1() != null) {
                panelABM.setTfInterno1(o.getInterno1().toString());
            }
        }

        if (o.getTele2() != null) {
            panelABM.setTfTele2(o.getTele2().toString());
            if (o.getInterno2() != null) {
                panelABM.setTfInterno2(o.getInterno2().toString());
            }
        }

        if (o.getContacto() != null) {
            panelABM.setTfContacto(o.getContacto());
        }

        if (o.getEmail() != null) {
            panelABM.setTfEmail(o.getEmail());
        }

        if (o.getWebpage() != null) {
            panelABM.setTfWEB(o.getWebpage());
        }

    }

    private Proveedor setEntity() throws MessageException {
        if (EL_OBJECT == null) {
            EL_OBJECT = new Proveedor();
        }
        //<editor-fold defaultstate="collapsed" desc="CTRL">
        if (panelABM.getTfCodigo().length() < 1) {
            throw new MessageException("Ingresar un código");
        }

        if (panelABM.getTfNombre() == null || panelABM.getTfNombre().length() < 1) {
            throw new MessageException("Debe ingresar un nombre");
        }
        try {
            Long.valueOf(panelABM.getTfNumDocumento());
            UTIL.VALIDAR_CUIL(panelABM.getTfNumDocumento());
            panelABM.setIconoValidadorCUIT(true, "CUIT válido");
        } catch (NumberFormatException ex) {
            throw new MessageException("La CUIT/CUIL no es válida (ingrese solo números)");
        } catch (IllegalArgumentException ex) {
            panelABM.setIconoValidadorCUIT(false, ex.getMessage());
        }
        if (panelABM.getTfDireccion() == null) {
            throw new MessageException("Debe indicar la dirección de la proveedor");
        }
        if (panelABM.getCbProvincias().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar una Provincia y Departamento");
        }
        if (panelABM.getCbDepartamentos().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Departamento");
        }
        if (panelABM.getCbMunicipios().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Municipio");
        }
        if (panelABM.getTfInterno1() != null && panelABM.getTfTele1() == null) {
            throw new MessageException("Especifique un número de teléfono 1 para el interno 1");
        }
        if (panelABM.getTfInterno2() != null && panelABM.getTfTele2() == null) {
            throw new MessageException("Especifique un número de teléfono 2 para el interno 2");
        }
        try {
            if (panelABM.getTfLimiteCtaCte().getText().length() > 0) {
                Long.valueOf(panelABM.getTfLimiteCtaCte().getText());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Límite Cta. Cte. no válido (ingrese solo números enteros, hasta 12 dígitos)");
        }
        //</editor-fold>
        Provincia provincia = (Provincia) panelABM.getSelectedProvincia();
        Departamento departamento = (Departamento) panelABM.getSelectedDepartamento();
        Municipio municipio = (Municipio) panelABM.getSelectedMunicipio();

        // NOT NULLABLE's
        EL_OBJECT.setCodigo(panelABM.getTfCodigo());
        EL_OBJECT.setNombre(panelABM.getTfNombre().toUpperCase());
        EL_OBJECT.setDireccion(panelABM.getTfDireccion());
        EL_OBJECT.setProvincia(provincia);
        EL_OBJECT.setDepartamento(departamento);
        EL_OBJECT.setMunicipio(municipio);
        EL_OBJECT.setContribuyente((Contribuyente) panelABM.getSelectedCondicIVA());
        EL_OBJECT.setCuit(new Long(panelABM.getTfNumDocumento()));
        EL_OBJECT.setLimiteCtaCte(new BigDecimal(panelABM.getTfLimiteCtaCte().getText()));
        //NULLABLE's
        if (panelABM.getTfCP().length() > 0) {
            EL_OBJECT.setCodigopostal(new Integer(panelABM.getTfCP()));
        }

        if (panelABM.getTfEmail().length() > 0) {
            EL_OBJECT.setEmail(panelABM.getTfEmail());
        }

        if (panelABM.getTfWEB().length() > 0) {
            EL_OBJECT.setWebpage(panelABM.getTfWEB());
        }
        if (panelABM.getTfTele1().length() > 0 && panelABM.getTfTele1().length() > 0) {
            EL_OBJECT.setTele1(new Long(panelABM.getTfTele1()));
            if (panelABM.getTfInterno1().length() > 0) {
                EL_OBJECT.setInterno1(new Integer(panelABM.getTfInterno1()));
            }
        }
        if (panelABM.getTfTele2().length() > 0 && panelABM.getTfTele2().length() > 0) {
            EL_OBJECT.setTele2(new Long(panelABM.getTfTele2()));
            if (panelABM.getTfInterno2().length() > 0) {
                EL_OBJECT.setInterno2(new Integer(panelABM.getTfInterno2()));
            }
        }
        return EL_OBJECT;
    }

    /**
     * Check the constraints related to the Entity like UNIQUE's codigo, nombre...
     *
     * @param o
     * @throws MessageException end-user explanation message.
     */
    private void checkConstraints(Proveedor o) throws MessageException {
        String idQuery = "";
        if (o.getId() != null) {
            idQuery = "o.id <>" + o.getId() + " AND ";
        }
        try {
            jpaController.findByQuery(jpaController.getSelectFrom()
                    + " WHERE " + idQuery + " UPPER(o.nombre)='" + o.getNombre().toUpperCase() + "' ");
            throw new MessageException("Ya existe un " + CLASS_NAME + " con este nombre.");
        } catch (NoResultException ex) {
        }
        try {
            jpaController.findByQuery(jpaController.getSelectFrom()
                    + " WHERE " + idQuery + " o.cuit=" + o.getCuit());
            throw new MessageException("Ya existe un " + CLASS_NAME + " con esta CUIT/CUIL/DNI.");
        } catch (NoResultException ex) {
        }
        try {
            jpaController.findByQuery(jpaController.getSelectFrom()
                    + " WHERE " + idQuery + " o.codigo='" + o.getCodigo() + "'");
            throw new MessageException("Ya existe un " + CLASS_NAME + " con este código.");
        } catch (NoResultException ex) {
        }
    }

    /**
     * Arma la query, para filtrar filas en la tabla del JDContenedor
     *
     * @param filtro atributo "nombre" del objeto; ej.: o.nombre LIKE 'filtro%'
     */
    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.nombre ILIKE '%" + filtro + "%' ORDER BY o.nombre";
        }
        cargarContenedorTabla(contenedor.getDTM(), query);
    }

    Proveedor createProveedorFromCliente(Cliente cliente) throws MessageException {
        Proveedor p = new Proveedor();
        p.setNombre(cliente.getNombre());
        p.setCodigo(cliente.getCodigo());
        p.setContribuyente(cliente.getContribuyente());
        p.setCuit(cliente.getNumDoc());
        p.setProvincia(cliente.getProvincia());
        p.setDepartamento(cliente.getDepartamento());
        p.setMunicipio(cliente.getMunicipio());
        p.setDireccion(cliente.getDireccion());
        p.setEmail(cliente.getEmail());
        p.setTele1(cliente.getTele1());
        p.setInterno1(cliente.getInterno1());
        p.setTele2(cliente.getTele2());
        p.setInterno2(cliente.getInterno2());
        p.setCodigopostal(cliente.getCodigopostal());
        p.setEmail(cliente.getEmail());
        p.setObservacion(cliente.getObservacion());
        p.setWebpage(cliente.getWebpage());
        p.setLimiteCtaCte(BigDecimal.ZERO);
        checkConstraints(p);
        jpaController.persist(p);
        return p;
    }

    public JDialog initProveedorToCliente(Window window) {
        initContenedor(window, true);
        contenedor.setButtonsVisible(false);
        contenedor.getLabelMensaje().setText("<html>Esta opción le permite crear un Cliente de los datos de un Proveedor</html>");
        contenedor.getbNuevo().setVisible(true);
        contenedor.getbNuevo().setText("Convertir");
        contenedor.getbNuevo().removeActionListener(this);
        contenedor.getbNuevo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer proveedorID = (Integer) UTIL.getSelectedValue(contenedor.getjTable1(), 0);
                Proveedor proveedor = jpaController.find(proveedorID);
                try {
                    if (proveedor == null) {
                        throw new MessageException("Seleccione el " + CLASS_NAME + " que desea convertir");
                    }
                    createClienteFromProveedor(proveedor);
                    JOptionPane.showMessageDialog(contenedor, "Cliente creado");
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(contenedor, ex.getMessage());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(contenedor, ex.getMessage());
                    LogManager.getLogger().error(ex.getMessage(), ex);
                }
            }
        });
        return contenedor;
    }

    private Cliente createClienteFromProveedor(Proveedor proveedor) throws MessageException {
        Cliente cliente;
        cliente = new ClienteController().createFromProveedor(proveedor);
        return cliente;
    }

    public void displayPAP(Window owner) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
