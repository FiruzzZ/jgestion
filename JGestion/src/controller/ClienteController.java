package controller;

import controller.exceptions.*;
import entity.Cliente;
import java.awt.event.KeyEvent;
import java.util.List;
import entity.Contribuyente;
import entity.Departamento;
import entity.Municipio;
import entity.Proveedor;
import entity.Provincia;
import utilities.general.UTIL;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMProveedores;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.RollbackException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jpa.controller.ClienteJpaController;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.postgresql.util.PSQLException;

/**
 *
 * @author Administrador
 */
public class ClienteController implements ActionListener {

    public final String CLASS_NAME = Cliente.class.getSimpleName();
    private final String[] columnNames = {"ID", "Código", "Razón social", "Tipo", "Nº Doc.", "Teléfonos"};
    private final int[] columnWidths = {10, 20, 100, 10, 40, 80};
    private Cliente EL_OBJECT;
    private JDContenedor contenedor = null;
    private JDABM abm;
    private PanelABMProveedores panelABM;
    private final ClienteJpaController jpaController;

    public ClienteController() {
        jpaController = new ClienteJpaController();
    }

    public void destroy(Integer id) throws NonexistentEntityException, MessageException {
        try {
            Cliente cliente;
            cliente = jpaController.find(id);
            if (cliente == null) {
                throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.");
            }
            jpaController.remove(cliente);
        } catch (RollbackException ex) {
            if (ex.getCause() instanceof DatabaseException) {
                PSQLException ps = (PSQLException) ex.getCause().getCause();
                if (ps.getMessage().contains("viola la llave foránea") || ps.getMessage().contains("violates foreign key constraint")) {
                    throw new MessageException("No se puede eliminar porque existen otros registros que están relacionados a este");
                }
            }
            throw ex;
        } finally {
            jpaController.closeEntityManager();
        }
    }

    public List<Cliente> findAll() {
        return jpaController.findAll();
    }

    public JDialog initContenedor(JFrame frame, boolean modal) {
        contenedor = new JDContenedor(frame, modal, "ABM - " + CLASS_NAME + "s");
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                armarQuery(contenedor.getTfFiltro().getText().trim());
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), columnNames, columnWidths);
        UTIL.hideColumnTable(contenedor.getjTable1(), 0);
        cargarContenedorTabla(contenedor.getDTM(), null);
        contenedor.setListener(this);
        return contenedor;
    }

    private void cargarContenedorTabla(DefaultTableModel dtm, String query) {
        UTIL.limpiarDtm(dtm);
        List<Cliente> l;
        if (query == null || query.length() < 1) {
            l = jpaController.findAll();

        } else {
            // para cuando se usa el Buscador del ABM
            l = jpaController.findByNativeQuery(query);
        }

        for (Cliente o : l) {
            dtm.addRow(new Object[]{
                o.getId(),
                o.getCodigo(),
                o.getNombre(),
                o.getTipodoc() == 1 ? "DNI" : "CUIT",
                o.getNumDoc(),
                telefonosToString(o)
            });
        }
    }

    /**
     *
     * @param isEditing
     * @param e se posicionará a la ventana en relación a este, can be null.
     * @throws MessageException
     */
    private void initABM(boolean isEditing, ActionEvent e) throws MessageException {
        UsuarioController.checkPermiso(PermisosController.PermisoDe.ABM_CLIENTES);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila");

        }

        panelABM = new PanelABMProveedores();
        panelABM.getCheckRetencionDGR().setVisible(false);
        panelABM.getCheckRetencionIVA().setVisible(false);
        UTIL.loadComboBox(panelABM.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
        UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
        UTIL.loadComboBox(panelABM.getCbMunicipios(), null, true);
        UTIL.loadComboBox(panelABM.getCbCondicIVA(), new ContribuyenteController().findContribuyenteEntities(), false);
        panelABM.setListener(this);
        if (isEditing) {
            setPanel(EL_OBJECT);
        } else {
            // si es nuevo, agrega un código sugerido
            panelABM.setTfCodigo(String.valueOf(jpaController.count() + 1));
        }

        abm = new JDABM(contenedor, "ABM " + CLASS_NAME + "s", true, panelABM);
        if (e != null) {
            abm.setLocation(((java.awt.Component) e.getSource()).getLocation());
        }

        abm.setListener(this);
        abm.setVisible(true);
    }

    private void setPanel(Cliente o) {
        panelABM.setTfCodigo(o.getCodigo());
        panelABM.setTfNombre(o.getNombre());
        panelABM.setTfDireccion(o.getDireccion());
        panelABM.getCbTipoDocumento().setSelectedIndex(o.getTipodoc() - 1);
        panelABM.setTfNumDocumento(String.valueOf(o.getNumDoc()));
        panelABM.getTfLimiteCtaCte().setText(o.getLimiteCtaCte().intValue() + "");
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

        if (o.getEmail() != null) {
            panelABM.setTfEmail(o.getEmail());
        }

        if (o.getWebpage() != null) {
            panelABM.setTfWEB(o.getWebpage());
        }

        if (o.getContacto() != null) {
            panelABM.setTfContacto(o.getContacto());
        }

        for (int i = 0; i < panelABM.getCbCondicIVA().getItemCount(); i++) {
            if (panelABM.getCbCondicIVA().getItemAt(i).toString().equals(o.getContribuyente().getNombre())) {
                panelABM.getCbCondicIVA().setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < panelABM.getCbProvincias().getItemCount(); i++) {
            if (panelABM.getCbProvincias().getItemAt(i).toString().equals(o.getProvincia().getNombre())) {
                panelABM.getCbProvincias().setSelectedIndex(i);
                break;
            }
        }

        if (o.getDepartamento() != null) {
            for (int i = 0; i < panelABM.getCbDepartamentos().getItemCount(); i++) {
                if (panelABM.getCbDepartamentos().getItemAt(i).toString().equals(o.getDepartamento().getNombre())) {
                    panelABM.getCbDepartamentos().setSelectedIndex(i);
                    break;
                }
            }
        }

        if (o.getMunicipio() != null) {
            for (int i = 0; i < panelABM.getCbMunicipios().getItemCount(); i++) {
                if (panelABM.getCbMunicipios().getItemAt(i).toString().equals(o.getMunicipio().getNombre())) {
                    panelABM.getCbMunicipios().setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void setEntity() throws MessageException {
        if (EL_OBJECT == null) {
            EL_OBJECT = new Cliente();
        }
        // <editor-fold defaultstate="collapsed" desc="CTRL">
        if (panelABM.getTfCodigo() == null || panelABM.getTfCodigo().length() < 1) {
            throw new MessageException("Debe ingresar un código");
        }

        if (panelABM.getTfNombre() == null || panelABM.getTfNombre().length() < 1) {
            throw new MessageException("Debe ingresar un nombre");
        }

        if (panelABM.getTfDireccion() == null) {
            throw new MessageException("Dirección no válida");
        }

        if (panelABM.getCbProvincias().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar una Provincia, Departamento y Municipio");
        }

        if (panelABM.getCbDepartamentos().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Departamento");
        }
        if (panelABM.getCbMunicipios().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Municipio");
        }

        try {
            if (panelABM.getTfTele1().length() > 0) {
                Long.valueOf(panelABM.getTfTele1());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 1 no válido");
        }

        try {
            if (panelABM.getTfTele2().length() > 0) {
                Long.valueOf(panelABM.getTfTele2());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 2 no válido");
        }
        if (panelABM.getTfInterno1().length() > 0) {
            if (panelABM.getTfTele1() == null) {
                throw new MessageException("Especifique un número de teléfono 1 para el interno 1");
            } else {
                try {
                    Integer.valueOf(panelABM.getTfInterno1());
                } catch (Exception e) {
                    throw new MessageException("Número de interno 1 no válido (Solo números enteros)");
                }
            }
        }

        if (panelABM.getTfInterno2().length() > 0) {
            if (panelABM.getTfTele2() == null) {
                throw new MessageException("Especifique un número de teléfono 2 para el interno 2");
            } else {
                try {
                    Integer.valueOf(panelABM.getTfInterno1());
                } catch (Exception e) {
                    throw new MessageException("Número de interno 2 no válido (Solo números enteros)");
                }
            }
        }

        if (panelABM.getCbTipoDocumento().getSelectedIndex() == 0) {
            if (panelABM.getTfNumDocumento().length() < 1) {
                throw new MessageException("Número de DNI no válido");
            }
        } else {
            try {
                Long.valueOf(panelABM.getTfNumDocumento());
                UTIL.VALIDAR_CUIL(panelABM.getTfNumDocumento());
                panelABM.setIconoValidadorCUIT(true, "CUIT válido");
            } catch (NumberFormatException ex) {
                throw new MessageException("La CUIT/CUIL no es válida (ingrese solo números)");
            } catch (IllegalArgumentException ex) {
                panelABM.setIconoValidadorCUIT(false, ex.getMessage());
            }
        }
        try {
            if (panelABM.getTfLimiteCtaCte().getText().length() > 0) {
                Long.valueOf(panelABM.getTfLimiteCtaCte().getText());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Límite Cta. Cte. no válido (ingrese solo números enteros, hasta 12 dígitos)");
        }
        // </editor-fold>
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
        EL_OBJECT.setTipodoc(panelABM.getCbTipoDocumento().getSelectedIndex() + 1);// DNI = 1 , CUIT = 2
        EL_OBJECT.setNumDoc(new Long(panelABM.getTfNumDocumento()));
        EL_OBJECT.setLimiteCtaCte(new BigDecimal(panelABM.getTfLimiteCtaCte().getText()));
        // estado activo
        EL_OBJECT.setEstado(1);

        //NULLABLE's
        if (panelABM.getTfContacto().length() > 0) {
            EL_OBJECT.setContacto(panelABM.getTfContacto());
        }

        if (panelABM.getTfCP().length() > 0) {
            EL_OBJECT.setCodigopostal(new Integer(panelABM.getTfCP()));
        }

        if (panelABM.getTfEmail().length() > 0) {
            EL_OBJECT.setEmail(panelABM.getTfEmail());
        }

        if (panelABM.getTfWEB().length() > 0) {
            EL_OBJECT.setWebpage(panelABM.getTfWEB());
        }

        if (panelABM.getTfTele1().length() > 0) {
            EL_OBJECT.setTele1(new Long(panelABM.getTfTele1()));
            if (panelABM.getTfInterno1().length() > 0) {
                EL_OBJECT.setInterno1(new Integer(panelABM.getTfInterno1()));
            }
        }
        if (panelABM.getTfTele2().length() > 0) {
            EL_OBJECT.setTele2(new Long(panelABM.getTfTele2()));
            if (panelABM.getTfInterno2().length() > 0) {
                EL_OBJECT.setInterno2(new Integer(panelABM.getTfInterno2()));
            }
        }
    }

    /**
     * Check the constraints related to the Entity like UNIQUE's codigo, nombre...
     *
     * @param object
     * @throws MessageException end-user explanation message.
     * @throws Exception
     */
    private void checkConstraints(Cliente object) throws MessageException {
        String idQuery = "";

        if (object.getId() != null) {
            idQuery = "o.id<>" + object.getId() + " AND ";

        }
        String l = (String) jpaController.findAttribute("SELECT o.nombre"
                + " FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + " WHERE " + idQuery + " o.codigo='" + object.getCodigo() + "'");
        if (l != null) {
            throw new MessageException(
                    "Ya existe el cliente: " + l + " con este Código.");
        }
        l = (String) jpaController.findAttribute("SELECT o.nombre"
                + " FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + " WHERE " + idQuery + " o.nombre='" + object.getNombre() + "' ");
        if (l != null) {
            throw new MessageException(
                    "Ya existe un " + CLASS_NAME + " con este nombre.");
        }
        l = (String) jpaController.findAttribute("SELECT o.nombre"
                + " FROM " + jpaController.getEntityClass().getSimpleName() + " o "
                + " WHERE " + idQuery + " o.numDoc=" + object.getNumDoc());
        if (l != null) {
            throw new MessageException(
                    "Ya existe el cliente: " + l + " con este DNI/CUIT.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();

            if (boton.equals(contenedor.getbNuevo())) {
                try {
                    EL_OBJECT = null;
                    initABM(false, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.equals(contenedor.getbModificar())) {
                try {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        EL_OBJECT = DAO.getEntityManager().find(Cliente.class,
                                Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                    } else {
                        EL_OBJECT = null;
                    }
                    initABM(true, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (boton.equals(contenedor.getbBorrar())) {
                try {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        destroy(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                        cargarContenedorTabla(contenedor.getDTM(), null);
                    } else {
                        throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
                    }
                    JOptionPane.showMessageDialog(contenedor, "Registro eliminado");
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (NonexistentEntityException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("Print")) {
                //no implementado aun...
            } else if (boton.getName().equalsIgnoreCase("exit")) {
                contenedor.dispose();
                contenedor = null;
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    setEntity();
                    checkConstraints(EL_OBJECT);
                    String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                    //persistiendo......
                    if (EL_OBJECT.getId() == null) {
                        jpaController.create(EL_OBJECT);
                    } else {
                        jpaController.merge(EL_OBJECT);
                    }
                    abm.showMessage(msg, CLASS_NAME, 1);
                    cargarContenedorTabla(contenedor.getDTM(), "");
                    abm.dispose();
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                abm.dispose();
                panelABM = null;
                abm = null;
                EL_OBJECT = null;
            } else if (boton.equals(panelABM.getbDepartamentos())) {
                new DepartamentoController().initContenedor(null, true);
                //cuando cierra el abm
                if (panelABM.getCbProvincias().getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panelABM.getCbDepartamentos(),
                            new DepartamentoController().findDeptosFromProvincia(
                                    ((Provincia) panelABM.getCbProvincias().getSelectedItem()).getId()), true);
                } else {
                    UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
                }

            } else if (boton.equals(panelABM.getbMunicipios())) {
                new MunicipioController().initContenedor(null, true);
                if (panelABM.getCbDepartamentos().getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panelABM.getCbMunicipios(),
                            new MunicipioController().findMunicipiosFromDepto(
                                    ((Departamento) panelABM.getCbDepartamentos().getSelectedItem()).getId()), true);
                } else {
                    UTIL.loadComboBox(panelABM.getCbMunicipios(), null, true);
                }
            }

            return;
        }// </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="ComboBox">
        else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
            javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();

            if (combo.getName()
                    .equalsIgnoreCase("cbProvincias")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panelABM.getCbDepartamentos(), ((Provincia) combo.getSelectedItem()).getDeptoList(), true);
                } else {
                    UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
                }
            } else if (combo.getName()
                    .equalsIgnoreCase("cbDepartamentos")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panelABM.getCbMunicipios(), ((Departamento) combo.getSelectedItem()).getMunicipioList(), true);
                } else {
                    UTIL.loadComboBox(panelABM.getCbMunicipios(), null, true);
                }
            }
        }
// </editor-fold>
    }

    /**
     * Arma la query, para filtrar filas en la tabla del JDContenedor
     *
     * @param filtro atributo "nombre" del objeto; ej.: o.nombre ILIKE 'filtro%'
     */
    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.nombre ILIKE '" + filtro + "%'";
        }
        cargarContenedorTabla(contenedor.getDTM(), query);
    }

    private String telefonosToString(Cliente o) {
        String t = "";
        if (o.getTele1() != null) {
            t += o.getTele1().toString();
            if (o.getInterno1() != null) {
                t += "-" + o.getInterno1().toString();
            }
        }
        if (o.getTele2() != null) {
            t += "/";
            t += o.getTele2().toString();
            if (o.getInterno2() != null) {
                t += "-" + o.getInterno2().toString();
            }
        }

        return t;
    }

    public JDialog initClienteToProveedor(JFrame owner) {
        initContenedor(owner, true);
        contenedor.setButtonsVisible(false);
        contenedor.getLabelMensaje().setText("<html>Esta opción le permite crear un Proveedor de los datos de un Cliente</html>");
        contenedor.getbNuevo().setVisible(true);
        contenedor.getbNuevo().setText("Convertir");
        contenedor.getbNuevo().removeActionListener(this);
        contenedor.getbNuevo().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer clienteID = (Integer) UTIL.getSelectedValue(contenedor.getjTable1(), 0);
                try {
                    if (clienteID == null) {
                        throw new MessageException("Seleccione el " + CLASS_NAME + " que desea convertir");
                    }
                    Cliente cliente = jpaController.find(clienteID);
                    if (cliente == null) {
                        cargarContenedorTabla(contenedor.getDTM(), null);
                        throw new MessageException("El Cliente que intenta convertir no existe mas.");
                    }
                    createProveedorFromCliente(cliente);
                    JOptionPane.showMessageDialog(contenedor, "Proveedor creado");
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(contenedor, ex.getMessage());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(contenedor, ex.getMessage());
                    Logger
                            .getLogger(ClienteController.class
                                    .getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return contenedor;
    }

    private void createProveedorFromCliente(Cliente cliente) throws MessageException, Exception {
        new ProveedorController().createProveedorFromCliente(cliente);
    }

    Cliente createFromProveedor(Proveedor proveedor) throws MessageException {
        Cliente p = new Cliente();
        p.setNombre(proveedor.getNombre());
        p.setCodigo(proveedor.getCodigo());
        p.setContribuyente(proveedor.getContribuyente());
        p.setTipodoc(2);
        p.setNumDoc(proveedor.getCuit());
        p.setProvincia(proveedor.getProvincia());
        p.setDepartamento(proveedor.getDepartamento());
        p.setMunicipio(proveedor.getMunicipio());
        p.setDireccion(proveedor.getDireccion());
        p.setEmail(proveedor.getEmail());
        p.setTele1(proveedor.getTele1());
        p.setInterno1(proveedor.getInterno1());
        p.setTele2(proveedor.getTele2());
        p.setInterno2(proveedor.getInterno2());
        p.setCodigopostal(proveedor.getCodigopostal());
        p.setEmail(proveedor.getEmail());
        p.setObservacion(proveedor.getObservacion());
        p.setWebpage(proveedor.getWebpage());
        p.setEstado(1);
        checkConstraints(p);
        jpaController.create(p);
        return p;
    }
}
