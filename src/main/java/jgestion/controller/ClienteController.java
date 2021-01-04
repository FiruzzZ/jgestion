package jgestion.controller;

import java.awt.Window;
import jgestion.controller.exceptions.MessageException;
import jgestion.entity.Cliente;
import java.awt.event.KeyEvent;
import java.util.List;
import jgestion.entity.Contribuyente;
import jgestion.entity.Departamento;
import jgestion.entity.Municipio;
import jgestion.entity.Proveedor;
import jgestion.entity.Provincia;
import utilities.general.UTIL;
import jgestion.gui.JDABM;
import jgestion.gui.JDContenedor;
import jgestion.gui.PanelABMProveedores;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import jgestion.JGestionUtils;
import jgestion.entity.TipoDocumento;
import jgestion.jpa.controller.ClienteJpaController;
import jgestion.jpa.controller.TipoDocumentoJpaController;
import org.apache.logging.log4j.LogManager;

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

    public List<Cliente> findAll() {
        return jpaController.findAll();
    }

    public JDialog initContenedor(Window owner, boolean modal) {
        contenedor = new JDContenedor(owner, modal, "ABM - " + CLASS_NAME + "s");
        contenedor.getbImprimir().setVisible(false);
        contenedor.setSize(contenedor.getWidth() + 200, contenedor.getHeight());
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
                o.getTipodoc().getNombre(),
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
        JGestionUtils.setProvinciaLocalidadesComboListener(panelABM.getCbProvincias(), true, panelABM.getCbDepartamentos(), true, panelABM.getCbMunicipios(), true);
        UTIL.loadComboBox(panelABM.getCbTipoDocumento(), JGestionUtils.getWrappedTipoDocumentos(new TipoDocumentoJpaController().findAll()), false);
        UTIL.loadComboBox(panelABM.getCbMunicipios(), null, true);
        UTIL.loadComboBox(panelABM.getCbCondicIVA(), JGestionUtils.getWrappedContribuyentes(new ContribuyenteController().findAll()), false);
        panelABM.setListener(this);
        if (isEditing) {
            setUI(EL_OBJECT);
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

    private void setUI(Cliente o) {
        panelABM.setTfCodigo(o.getCodigo());
        panelABM.setTfNombre(o.getNombre());
        panelABM.setTfDireccion(o.getDireccion());
        UTIL.setSelectedItem(panelABM.getCbTipoDocumento(), o.getTipodoc());
        UTIL.setSelectedItem(panelABM.getCbCondicIVA(), o.getContribuyente(), false);
        UTIL.setSelectedItem(panelABM.getCbProvincias(), o.getProvincia(), true);
        UTIL.setSelectedItem(panelABM.getCbDepartamentos(), o.getDepartamento(), true);
        UTIL.setSelectedItem(panelABM.getCbMunicipios(), o.getMunicipio(), true);
        panelABM.setTfNumDocumento(String.valueOf(o.getNumDoc()));
        panelABM.getTfLimiteCtaCte().setText(o.getLimiteCtaCte().intValue() + "");
        panelABM.setTfCP(Objects.toString(o.getCodigopostal(), null));
        panelABM.setTfTele1(Objects.toString(o.getTele1(), null));
        panelABM.setTfInterno1(Objects.toString(o.getInterno1(), null));
        panelABM.setTfTele2(Objects.toString(o.getTele2(), null));
        panelABM.setTfInterno2(Objects.toString(o.getInterno2(), null));
        panelABM.setTfContacto(o.getContacto());
        panelABM.setTfEmail(o.getEmail());
        panelABM.setTfWEB(o.getWebpage());
    }

    private void setFields() throws MessageException {
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

        Long telefono1 = null;
        try {
            if (panelABM.getTfTele1().length() > 0) {
                telefono1 = Long.valueOf(panelABM.getTfTele1());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 1 no válido");
        }

        Long telefono2 = null;
        try {
            if (panelABM.getTfTele2().length() > 0) {
                telefono2 = Long.valueOf(panelABM.getTfTele2());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 2 no válido");
        }
        Integer interno1 = null;
        if (panelABM.getTfInterno1().length() > 0) {
            if (panelABM.getTfTele1() == null) {
                throw new MessageException("Especifique un número de teléfono 1 para el interno 1");
            } else {
                try {
                    interno1 = Integer.valueOf(panelABM.getTfInterno1());
                } catch (Exception e) {
                    throw new MessageException("Número de interno 1 no válido (Solo números enteros)");
                }
            }
        }

        Integer interno2 = null;
        if (panelABM.getTfInterno2().length() > 0) {
            if (panelABM.getTfTele2() == null) {
                throw new MessageException("Especifique un número de teléfono 2 para el interno 2");
            } else {
                try {
                    interno2 = Integer.valueOf(panelABM.getTfInterno2());
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
        Provincia provincia = null;
        Departamento departamento = null;
        Municipio municipio = null;
        if (panelABM.getCbProvincias().getSelectedIndex() > 0) {
            provincia = (Provincia) UTIL.getEntityWrapped(panelABM.getCbProvincias()).getEntity();
        }
        if (panelABM.getCbDepartamentos().getSelectedIndex() > 0) {
            departamento = (Departamento) UTIL.getEntityWrapped(panelABM.getCbDepartamentos()).getEntity();
        }
        if (panelABM.getCbMunicipios().getSelectedIndex() > 0) {
            municipio = (Municipio) UTIL.getEntityWrapped(panelABM.getCbMunicipios()).getEntity();
        }

        // NOT NULLABLE's
        EL_OBJECT.setCodigo(panelABM.getTfCodigo());
        EL_OBJECT.setNombre(panelABM.getTfNombre().toUpperCase());
        EL_OBJECT.setDireccion(panelABM.getTfDireccion());
        EL_OBJECT.setProvincia(provincia);
        EL_OBJECT.setDepartamento(departamento);
        EL_OBJECT.setMunicipio(municipio);
        EL_OBJECT.setContribuyente((Contribuyente) UTIL.getEntityWrapped(panelABM.getCbCondicIVA()).getEntity());
        EL_OBJECT.setTipodoc((TipoDocumento) UTIL.getEntityWrapped(panelABM.getCbTipoDocumento()).getEntity());
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

        EL_OBJECT.setTele1(telefono1);
        EL_OBJECT.setInterno1(interno1);
        EL_OBJECT.setTele2(telefono2);
        EL_OBJECT.setInterno2(interno2);
    }

    /**
     * Check the constraints related to the Entity like UNIQUE's codigo, nombre...
     *
     * @param object
     * @throws MessageException end-user explanation message.
     */
    private void checkConstraints(Cliente object) throws MessageException {
        String idQuery = "";

        if (object.getId() != null) {
            idQuery = "o.id<>" + object.getId() + " AND ";
        }
        String l = (String) jpaController.findAttribute("SELECT o.nombre"
                + " FROM " + jpaController.getAlias()
                + " WHERE " + idQuery + " o.codigo='" + object.getCodigo() + "'");
        if (l != null) {
            throw new MessageException("Ya existe el cliente: " + l + " con este Código.");
        }
        l = (String) jpaController.findAttribute("SELECT o.nombre"
                + " FROM " + jpaController.getAlias()
                + " WHERE " + idQuery + " o.nombre='" + object.getNombre() + "' ");
        if (l != null) {
            throw new MessageException("Ya existe un " + CLASS_NAME + " con este nombre.");
        }
        l = (String) jpaController.findAttribute("SELECT o.nombre"
                + " FROM " + jpaController.getAlias()
                + " WHERE " + idQuery + " o.numDoc=" + object.getNumDoc());
        if (l != null) {
            throw new MessageException("Ya existe el cliente: " + l + " con este DNI/CUIT.");
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
                    LogManager.getLogger().error(ex.getMessage(), ex);
                }
            } else if (boton.equals(contenedor.getbModificar())) {
                try {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        EL_OBJECT = jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                    } else {
                        EL_OBJECT = null;
                    }
                    initABM(true, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LogManager.getLogger().error(ex.getMessage(), ex);
                }

            } else if (boton.equals(contenedor.getbBorrar())) {
                try {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        jpaController.remove(jpaController.find(Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString())));
                        cargarContenedorTabla(contenedor.getDTM(), null);
                    } else {
                        throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
                    }
                    JOptionPane.showMessageDialog(contenedor, "Registro eliminado");
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    LogManager.getLogger().error(ex.getMessage(), ex);
                }
            } else if (boton.getName().equalsIgnoreCase("Print")) {
                //no implementado aun...
            } else if (boton.getName().equalsIgnoreCase("exit")) {
                contenedor.dispose();
                contenedor = null;
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    setFields();
                    checkConstraints(EL_OBJECT);
                    String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                    //persistiendo......
                    if (EL_OBJECT.getId() == null) {
                        jpaController.persist(EL_OBJECT);
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
                    LogManager.getLogger().error(ex.getMessage(), ex);
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
        }// </editor-fold>
    }

    /**
     * Arma la query, para filtrar filas en la tabla del JDContenedor
     *
     * @param filtro atributo "nombre" del objeto; ej.: o.nombre ILIKE 'filtro%'
     */
    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE o.nombre ILIKE '%" + filtro + "%'"
                    + " or o.num_doc::varchar ilike '" + filtro + "%' "
                    + " order by o.nombre";
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
        contenedor.getLabelMensaje().setVisible(true);
        contenedor.getLabelMensaje().setText("<html>Esta opción le permite crear un Proveedor a partir de los datos de un Cliente</html>");
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
                    LogManager.getLogger().error(ex.getMessage(), ex);
                }
            }
        });
        return contenedor;
    }

    private void createProveedorFromCliente(Cliente cliente) throws MessageException {
        new ProveedorController().createProveedorFromCliente(cliente);
    }

    Cliente createFromProveedor(Proveedor proveedor) throws MessageException {
        Cliente p = new Cliente();
        p.setNombre(proveedor.getNombre());
        p.setCodigo(proveedor.getCodigo());
        p.setContribuyente(proveedor.getContribuyente());
        p.setTipodoc(new TipoDocumentoJpaController().find(2));
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
        jpaController.persist(p);
        return p;
    }

    public void displaySelector(Consumer<Cliente> selected) {
        initContenedor(null, true);
        contenedor.setTitle("Selector de Cliente");
        contenedor.setButtonsVisible(false);
        final Runnable onSelection = () -> {
            Integer id = (Integer) UTIL.getSelectedValueFromModel(contenedor.getjTable1(), 0);
            if (id != null) {
                contenedor.dispose();
                selected.accept(jpaController.find(id));
            }
        };
        contenedor.getjTable1().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    evt.consume();
                    onSelection.run();
                }
            }

        });
        contenedor.getjTable1().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onSelection.run();
                }
            }

        });
        contenedor.setVisible(true);
    }
}
