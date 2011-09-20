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
    private final String[] colsName = {"ID", "Código", "Razón social", "Tipo", "Nº Doc.", "Teléfonos"};
    private final int[] colsWidth = {10, 20, 100, 10, 40, 80};
    private Cliente EL_OBJECT;
    private JDContenedor contenedor = null;
    private JDABM abm;
    private PanelABMProveedores panel;

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

    public List<Cliente> findClienteEntities() {
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

    public Container initContenedor(JFrame frame, boolean modal) {
        contenedor = new JDContenedor(frame, modal, "ABM - " + CLASS_NAME + "s");
        contenedor.hideBtmEliminar();
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                armarQuery(contenedor.getTfFiltro().getText().trim());
            }
        });
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
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

        panel = new PanelABMProveedores();
        panel.getCheckRetencionDGR().setVisible(false);
        panel.getCheckRetencionIVA().setVisible(false);
        UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
        UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
        UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
        UTIL.loadComboBox(panel.getCbCondicIVA(), new ContribuyenteJpaController().findContribuyenteEntities(), false);
        panel.setListener(this);
        if (isEditing) {
            setPanel(EL_OBJECT);
        } else {
            // si es nuevo, agrega un código sugerido
            panel.setTfCodigo(String.valueOf(getClienteCount() + 1));
        }

        abm = new JDABM(true, contenedor, panel);
        abm.setTitle("ABM " + CLASS_NAME + "s");
        if (e != null) {
            abm.setLocation(((java.awt.Component) e.getSource()).getLocation());
        }

        abm.setListener(this);
        abm.setVisible(true);
    }

    private void setPanel(Cliente object) {
        panel.setTfCodigo(object.getCodigo());
        panel.setTfNombre(object.getNombre());
        panel.setTfDireccion(object.getDireccion());
        panel.getCbTipoDocumento().setSelectedIndex(object.getTipodoc() - 1);
        panel.setTfNumDocumento(String.valueOf(object.getNumDoc()));

        if (object.getCodigopostal() != null) {
            panel.setTfCP(object.getCodigopostal().toString());
        }
        if (object.getTele1() != null) {
            panel.setTfTele1(object.getTele1().toString());
            if (object.getInterno1() != null) {
                panel.setTfInterno1(object.getInterno1().toString());
            }
        }
        if (object.getTele2() != null) {
            panel.setTfTele2(object.getTele2().toString());
            if (object.getInterno2() != null) {
                panel.setTfInterno2(object.getInterno2().toString());
            }
        }

        if (object.getEmail() != null) {
            panel.setTfEmail(object.getEmail());
        }

        if (object.getWebpage() != null) {
            panel.setTfWEB(object.getWebpage());
        }

        if (object.getContacto() != null) {
            panel.setTfContacto(object.getContacto());
        }

        for (int i = 0; i < panel.getCbCondicIVA().getItemCount(); i++) {
            if (panel.getCbCondicIVA().getItemAt(i).toString().equals(object.getContribuyente().getNombre())) {
                panel.getCbCondicIVA().setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < panel.getCbProvincias().getItemCount(); i++) {
            if (panel.getCbProvincias().getItemAt(i).toString().equals(object.getProvincia().getNombre())) {
                panel.getCbProvincias().setSelectedIndex(i);
                break;
            }
        }

        if (object.getDepartamento() != null) {
            for (int i = 0; i < panel.getCbDepartamentos().getItemCount(); i++) {
                if (panel.getCbDepartamentos().getItemAt(i).toString().equals(object.getDepartamento().getNombre())) {
                    panel.getCbDepartamentos().setSelectedIndex(i);
                    break;
                }
            }
        }

        if (object.getMunicipio() != null) {
            for (int i = 0; i < panel.getCbMunicipios().getItemCount(); i++) {
                if (panel.getCbMunicipios().getItemAt(i).toString().equals(object.getMunicipio().getNombre())) {
                    panel.getCbMunicipios().setSelectedIndex(i);
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
        if (panel.getTfCodigo() == null || panel.getTfCodigo().length() < 1) {
            throw new MessageException("Debe ingresar un código");
        }

        if (panel.getTfNombre() == null || panel.getTfNombre().length() < 1) {
            throw new MessageException("Debe ingresar un nombre");
        }

        if (panel.getTfDireccion() == null) {
            throw new MessageException("Dirección no válida");
        }

        if (panel.getCbProvincias().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar una Provincia y Departamento");
        }

        if (panel.getCbDepartamentos().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Departamento");
        }

        try {
            if (panel.getTfTele1().length() > 0) {
                Long.valueOf(panel.getTfTele1());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 1 no válido");
        }

        try {
            if (panel.getTfTele2().length() > 0) {
                Long.valueOf(panel.getTfTele2());
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 2 no válido");
        }
        if (panel.getTfInterno1().length() > 0) {
            if (panel.getTfTele1() == null) {
                throw new MessageException("Especifique un número de teléfono 1 para el interno 1");
            } else {
                try {
                    Integer.valueOf(panel.getTfInterno1());
                } catch (Exception e) {
                    throw new MessageException("Número de interno 1 no válido (Solo números enteros)");
                }
            }
        }

        if (panel.getTfInterno2().length() > 0) {
            if (panel.getTfTele2() == null) {
                throw new MessageException("Especifique un número de teléfono 2 para el interno 2");
            } else {
                try {
                    Integer.valueOf(panel.getTfInterno1());
                } catch (Exception e) {
                    throw new MessageException("Número de interno 2 no válido (Solo números enteros)");
                }
            }
        }

        if (panel.getCbTipoDocumento().getSelectedIndex() == 0) {
            if (panel.getTfNumDocumento().length() < 1) {
                throw new MessageException("Número de DNI no válido, debe completar el campo");
            }
        } else {
            try {
                UTIL.VALIDAR_CUIL(panel.getTfNumDocumento());
            } catch (Exception ex) {
                throw new MessageException(ex.getMessage());
            }
        }// </editor-fold>

        // NOT NULLABLE's
        EL_OBJECT.setCodigo(panel.getTfCodigo());
        EL_OBJECT.setNombre(panel.getTfNombre().toUpperCase());
        EL_OBJECT.setDireccion(panel.getTfDireccion());
        EL_OBJECT.setProvincia((Provincia) panel.getSelectedProvincia());
        EL_OBJECT.setDepartamento((Departamento) panel.getSelectedDepartamento());
        EL_OBJECT.setContribuyente((Contribuyente) panel.getSelectedCondicIVA());
        EL_OBJECT.setTipodoc(panel.getCbTipoDocumento().getSelectedIndex() + 1);// DNI = 1 , CUIT = 2
        EL_OBJECT.setNumDoc(new Long(panel.getTfNumDocumento()));

        // estado activo
        EL_OBJECT.setEstado(1);

        //NULLABLE's
        if (panel.getCbMunicipios().getSelectedIndex() > 0) {
            EL_OBJECT.setMunicipio((Municipio) panel.getSelectedMunicipio());
        }

        if (panel.getTfContacto().length() > 0) {
            EL_OBJECT.setContacto(panel.getTfContacto());
        }

        if (panel.getTfCP().length() > 0) {
            EL_OBJECT.setCodigopostal(new Integer(panel.getTfCP()));
        }

        if (panel.getTfEmail().length() > 0) {
            EL_OBJECT.setEmail(panel.getTfEmail());
        }

        if (panel.getTfWEB().length() > 0) {
            EL_OBJECT.setWebpage(panel.getTfWEB());
        }

        // <editor-fold defaultstate="collapsed" desc="setter Tele1">
        if (panel.getTfTele1().length() > 0) {
            EL_OBJECT.setTele1(new Long(panel.getTfTele1()));
            if (panel.getTfInterno1().length() > 0) {
                EL_OBJECT.setInterno1(new Integer(panel.getTfInterno1()));
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="setter Tele2">
        if (panel.getTfTele2().length() > 0) {
            EL_OBJECT.setTele2(new Long(panel.getTfTele2()));
            if (panel.getTfInterno2().length() > 0) {
                EL_OBJECT.setInterno2(new Integer(panel.getTfInterno2()));
            }
        }
        // </editor-fold>
    }

    /**
     * Check the constraints related to the Entity like UNIQUE's codigo, nombre...
     * @param object
     * @throws MessageException end-user explanation message.
     * @throws Exception 
     */
    private void checkConstraints(Cliente object) throws MessageException, Exception {
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


        //persistiendo......
        if (object.getId() == null) {
            create(object);
        } else {
            edit(object);
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
                    setEntity();
                    String msg = EL_OBJECT.getId() == null ? "Registrado" : "Modificado";
                    checkConstraints(EL_OBJECT);
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
                panel = null;
                abm = null;
                EL_OBJECT = null;
            } else if (boton.getName().equalsIgnoreCase("municipio")) {
                new MunicipioJpaController().initContenedor(null, true);
                UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
                UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
                UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
            } else if (boton.getName().equalsIgnoreCase("departamento")) {
                new DepartamentoJpaController().initContenedor(null, true);
                UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
                UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
                UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
            }
            return;
        }// </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="ComboBox">
        else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
            javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
            if (combo.getName().equalsIgnoreCase("cbProvincias")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panel.getCbDepartamentos(), ((Provincia) combo.getSelectedItem()).getDeptoList(), true);
                } else {
                    UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
                }
            } else if (combo.getName().equalsIgnoreCase("cbDepartamentos")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panel.getCbMunicipios(), ((Departamento) combo.getSelectedItem()).getMunicipioList(), true);
                } else {
                    UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
                }
            }
        }
        // </editor-fold>
    }

    /**
     * Arma la query, para filtrar filas en la tabla del JDContenedor
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

    public void initClienteToProveedor(JFrame jFrame) {
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
        contenedor.setVisible(true);
    }

    private Proveedor createProveedorFromCliente(Cliente cliente) throws MessageException, Exception {
        Proveedor proveedor;
        proveedor = new ProveedorJpaController().createFromCliente(cliente);
        return proveedor;
    }

    Cliente createFromProveedor(Proveedor cliente) throws MessageException, Exception {
        Cliente p = new Cliente();
        p.setNombre(cliente.getNombre());
        p.setCodigo(cliente.getCodigo());
        p.setContribuyente(cliente.getContribuyente());
        p.setTipodoc(2);
        p.setNumDoc(cliente.getCuit());
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
        p.setEstado(1);
        checkConstraints(p);
        return p;
    }
}
