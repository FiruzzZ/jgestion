package controller;

import controller.exceptions.*;
import entity.Cliente;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Contribuyente;
import entity.Departamento;
import entity.Municipio;
import entity.Proveedor;
import entity.Provincia;
import utilities.general.UTIL;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMProveedores;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Administrador
 */
public class ClienteJpaController implements ActionListener {

    public final String CLASS_NAME = Cliente.class.getSimpleName();
    private final String[] columnNames = {"ID", "Código", "Razón social", "Tipo", "Nº Doc.", "Teléfonos"};
    private final int[] columnWidths = {10, 20, 100, 10, 40, 80};
    private Cliente EL_OBJECT;
    private JDContenedor contenedor = null;
    private JDABM abm;
    private PanelABMProveedores panelABM;

    // <editor-fold defaultstate="collapsed" desc="CRUD y List's">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Cliente cliente) throws Exception {
        DAO.create(cliente);
    }

    public void edit(Cliente cliente) throws NonexistentEntityException, MessageException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            if (em.find(Cliente.class, cliente.getId()) == null) {
                throw new MessageException("No existe mas ningún registro de este Cliente");

            }
            cliente = em.merge(cliente);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = cliente.getId();
                if (findCliente(id) == null) {
                    throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Cliente cliente;
            try {
                cliente = em.getReference(Cliente.class, id);
                cliente.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cliente with id " + id + " no longer exists.", enfe);
            }
            //CTRL DE VENTAS al clie...........

            em.remove(cliente);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Cliente> findEntities() {
        return findClienteEntities(true, -1, -1);
    }

    public List<Cliente> findClienteEntities(int maxResults, int firstResult) {
        return findClienteEntities(false, maxResults, firstResult);
    }

    private List<Cliente> findClienteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Cliente as o ORDER BY o.nombre");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Cliente findCliente(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Cliente.class, id);
        } finally {
            em.close();
        }
    }

    public int getClienteCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Cliente as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    // </editor-fold>

    public JDialog initContenedor(JFrame frame, boolean modal) {
        contenedor = new JDContenedor(frame, modal, "ABM - " + CLASS_NAME + "s");
        contenedor.hideBtmEliminar();
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
            l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").getResultList();
        } else {
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(query, Cliente.class).getResultList();
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
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.ABM_CLIENTES);
        if (isEditing && EL_OBJECT == null) {
            throw new MessageException("Debe elegir una fila de la tabla");
        }

        panelABM = new PanelABMProveedores();
        panelABM.getCheckRetencionDGR().setVisible(false);
        panelABM.getCheckRetencionIVA().setVisible(false);
        UTIL.loadComboBox(panelABM.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
        UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
        UTIL.loadComboBox(panelABM.getCbMunicipios(), null, true);
        UTIL.loadComboBox(panelABM.getCbCondicIVA(), new ContribuyenteJpaController().findContribuyenteEntities(), false);
        panelABM.setListener(this);
        if (isEditing) {
            setPanel(EL_OBJECT);
        } else {
            // si es nuevo, agrega un código sugerido
            panelABM.setTfCodigo(String.valueOf(getClienteCount() + 1));
        }

        abm = new JDABM(true, contenedor, panelABM);
        abm.setTitle("ABM " + CLASS_NAME + "s");
        if (e != null) {
            abm.setLocation(((java.awt.Component) e.getSource()).getLocation());
        }

        abm.setListener(this);
        abm.setVisible(true);
    }

    private void setPanel(Cliente object) {
        panelABM.setTfCodigo(object.getCodigo());
        panelABM.setTfNombre(object.getNombre());
        panelABM.setTfDireccion(object.getDireccion());
        panelABM.getCbTipoDocumento().setSelectedIndex(object.getTipodoc() - 1);
        panelABM.setTfNumDocumento(String.valueOf(object.getNumDoc()));

        if (object.getCodigopostal() != null) {
            panelABM.setTfCP(object.getCodigopostal().toString());
        }
        if (object.getTele1() != null) {
            panelABM.setTfTele1(object.getTele1().toString());
            if (object.getInterno1() != null) {
                panelABM.setTfInterno1(object.getInterno1().toString());
            }
        }
        if (object.getTele2() != null) {
            panelABM.setTfTele2(object.getTele2().toString());
            if (object.getInterno2() != null) {
                panelABM.setTfInterno2(object.getInterno2().toString());
            }
        }

        if (object.getEmail() != null) {
            panelABM.setTfEmail(object.getEmail());
        }

        if (object.getWebpage() != null) {
            panelABM.setTfWEB(object.getWebpage());
        }

        if (object.getContacto() != null) {
            panelABM.setTfContacto(object.getContacto());
        }

        for (int i = 0; i < panelABM.getCbCondicIVA().getItemCount(); i++) {
            if (panelABM.getCbCondicIVA().getItemAt(i).toString().equals(object.getContribuyente().getNombre())) {
                panelABM.getCbCondicIVA().setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < panelABM.getCbProvincias().getItemCount(); i++) {
            if (panelABM.getCbProvincias().getItemAt(i).toString().equals(object.getProvincia().getNombre())) {
                panelABM.getCbProvincias().setSelectedIndex(i);
                break;
            }
        }

        if (object.getDepartamento() != null) {
            for (int i = 0; i < panelABM.getCbDepartamentos().getItemCount(); i++) {
                if (panelABM.getCbDepartamentos().getItemAt(i).toString().equals(object.getDepartamento().getNombre())) {
                    panelABM.getCbDepartamentos().setSelectedIndex(i);
                    break;
                }
            }
        }

        if (object.getMunicipio() != null) {
            for (int i = 0; i < panelABM.getCbMunicipios().getItemCount(); i++) {
                if (panelABM.getCbMunicipios().getItemAt(i).toString().equals(object.getMunicipio().getNombre())) {
                    panelABM.getCbMunicipios().setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void setEntity(Cliente o) throws MessageException {
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
        }// </editor-fold>

        Provincia provincia = (Provincia) panelABM.getSelectedProvincia();
        Departamento departamento = (Departamento) panelABM.getSelectedDepartamento();
        Municipio municipio = (Municipio) panelABM.getSelectedMunicipio();
        // NOT NULLABLE's
        o.setCodigo(panelABM.getTfCodigo());
        o.setNombre(panelABM.getTfNombre().toUpperCase());
        o.setDireccion(panelABM.getTfDireccion());
        o.setProvincia(provincia);
        o.setDepartamento(departamento);
        o.setMunicipio(municipio);
        o.setContribuyente((Contribuyente) panelABM.getSelectedCondicIVA());
        o.setTipodoc(panelABM.getCbTipoDocumento().getSelectedIndex() + 1);// DNI = 1 , CUIT = 2
        o.setNumDoc(new Long(panelABM.getTfNumDocumento()));

        // estado activo
        o.setEstado(1);

        //NULLABLE's
        if (panelABM.getTfContacto().length() > 0) {
            o.setContacto(panelABM.getTfContacto());
        }

        if (panelABM.getTfCP().length() > 0) {
            o.setCodigopostal(new Integer(panelABM.getTfCP()));
        }

        if (panelABM.getTfEmail().length() > 0) {
            o.setEmail(panelABM.getTfEmail());
        }

        if (panelABM.getTfWEB().length() > 0) {
            o.setWebpage(panelABM.getTfWEB());
        }

        // <editor-fold defaultstate="collapsed" desc="setter Tele1">
        if (panelABM.getTfTele1().length() > 0) {
            o.setTele1(new Long(panelABM.getTfTele1()));
            if (panelABM.getTfInterno1().length() > 0) {
                o.setInterno1(new Integer(panelABM.getTfInterno1()));
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="setter Tele2">
        if (panelABM.getTfTele2().length() > 0) {
            o.setTele2(new Long(panelABM.getTfTele2()));
            if (panelABM.getTfInterno2().length() > 0) {
                o.setInterno2(new Integer(panelABM.getTfInterno2()));
            }
        }
        // </editor-fold>
    }

    /**
     * Check the constraints related to the Entity like UNIQUE's codigo,
     * nombre...
     *
     * @param object
     * @throws MessageException end-user explanation message.
     * @throws Exception
     */
    private void checkConstraints(Cliente object) throws MessageException {
        String idQuery = "";

        if (object.getId() != null) {
            idQuery = "o.id!=" + object.getId() + " AND ";
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.codigo='" + object.getCodigo() + "'", Cliente.class).getSingleResult();
            throw new MessageException("Ya existe otro " + CLASS_NAME + " con este Código.");
        } catch (NoResultException ex) {
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.nombre='" + object.getNombre() + "' ", Cliente.class).getSingleResult();
            throw new MessageException("Ya existe otro " + CLASS_NAME + " con este nombre.");
        } catch (NoResultException ex) {
        }
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.num_doc=" + object.getNumDoc(), Cliente.class).getSingleResult();
            throw new MessageException("Ya existe otro " + CLASS_NAME + " con este DNI/CUIT.");
        } catch (NoResultException ex) {
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("new")) {
                try {
                    EL_OBJECT = null;
                    initABM(false, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("edit")) {
                try {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        EL_OBJECT = DAO.getEntityManager().find(Cliente.class,
                                Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                        initABM(true, e);
                    }
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    int selectedRow = contenedor.getjTable1().getSelectedRow();
                    if (selectedRow > -1) {
                        EL_OBJECT = DAO.getEntityManager().find(Cliente.class,
                                Integer.valueOf((contenedor.getDTM().getValueAt(selectedRow, 0)).toString()));
                    }
                    if (EL_OBJECT == null) {
                        throw new MessageException("No hay " + CLASS_NAME + " seleccionado");
                    }
                    destroy(EL_OBJECT.getId());
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (NonexistentEntityException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(ClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("Print")) {
                //no implementado aun...
            } else if (boton.getName().equalsIgnoreCase("exit")) {
                contenedor.dispose();
                contenedor = null;
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    if (EL_OBJECT == null) {
                        EL_OBJECT = new Cliente();
                    }
                    setEntity(EL_OBJECT);
                    checkConstraints(EL_OBJECT);
                    String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                    //persistiendo......
                    if (EL_OBJECT.getId() == null) {
                        create(EL_OBJECT);
                    } else {
                        edit(EL_OBJECT);
                    }
                    abm.showMessage(msg, CLASS_NAME, 1);
                    cargarContenedorTabla(contenedor.getDTM(), "");
                    abm.dispose();
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    Logger.getLogger(ClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                abm.dispose();
                panelABM = null;
                abm = null;
                EL_OBJECT = null;
            } else if (boton.equals(panelABM.getbDepartamentos())) {
                new DepartamentoJpaController().initContenedor(null, true);
                //cuando cierra el abm
                if (panelABM.getCbProvincias().getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panelABM.getCbDepartamentos(),
                            new DepartamentoJpaController().findDeptosFromProvincia(
                            ((Provincia) panelABM.getCbProvincias().getSelectedItem()).getId()), true);
                } else {
                    UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
                }

            } else if (boton.equals(panelABM.getbMunicipios())) {
                new MunicipioJpaController().initContenedor(null, true);
                if (panelABM.getCbDepartamentos().getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panelABM.getCbMunicipios(),
                            new MunicipioJpaController().findMunicipiosFromDepto(
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
            if (combo.getName().equalsIgnoreCase("cbProvincias")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panelABM.getCbDepartamentos(), ((Provincia) combo.getSelectedItem()).getDeptoList(), true);
                } else {
                    UTIL.loadComboBox(panelABM.getCbDepartamentos(), null, true);
                }
            } else if (combo.getName().equalsIgnoreCase("cbDepartamentos")) {
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

    public JDialog initClienteToProveedor(JFrame jFrame) {
        initContenedor(jFrame, true);
        contenedor.setButtonsVisible(false);
        contenedor.getLabelMensaje().setText("<html>Esta opción le permite crear un Proveedor de los datos de un Cliente</html>");
        contenedor.getbNuevo().setVisible(true);
        contenedor.getbNuevo().setText("Convertir");
        contenedor.getbNuevo().removeActionListener(this);
        contenedor.getbNuevo().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Integer clienteID = (Integer) UTIL.getSelectedValue(contenedor.getjTable1(), 0);
                Cliente cliente = findCliente(clienteID);
                try {
                    if (cliente == null) {
                        throw new MessageException("Seleccione el " + CLASS_NAME + " que desea convertir");
                    }
                    createProveedorFromCliente(cliente);
                    JOptionPane.showMessageDialog(contenedor, "Proveedor creado");
                } catch (MessageException ex) {
                    JOptionPane.showMessageDialog(contenedor, ex.getMessage());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(contenedor, ex.getMessage());
                    Logger.getLogger(ClienteJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return contenedor;
    }

    private void createProveedorFromCliente(Cliente cliente) throws MessageException, Exception {
        new ProveedorJpaController().createProveedorFromCliente(cliente);
    }

    Cliente createFromProveedor(Proveedor proveedor) throws MessageException, Exception {
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
        return p;
    }
}
