package controller;

import controller.exceptions.*;
import entity.Municipio;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import entity.Departamento;
import entity.Provincia;
import utilities.general.UTIL;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMDeptos;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author FiruzzZ
 */
public class MunicipioJpaController implements ActionListener, MouseListener, KeyListener {

    public final String CLASS_NAME = Municipio.class.getSimpleName();
    private JDContenedor contenedor = null;
    private JDABM abm;
    private final String[] colsName = {"Nº", "Nombre", "Departamento"};
    private final int[] colsWidth = {20, 120, 100};
    private PanelABMDeptos panel;
    private Municipio municipio;

    // <editor-fold defaultstate="collapsed" desc="CRUD..">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Municipio municipio) throws PreexistingEntityException, Exception {
        DAO.create(municipio);
    }

    public void edit(Municipio municipio) throws NonexistentEntityException, Exception {
        DAO.doMerge(municipio);
    }

    public void destroy(Integer id) throws NonexistentEntityException, IllegalOrphanException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Municipio municipio;
            try {
                municipio = em.getReference(Municipio.class, id);
                municipio.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The municipio with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            //ctrl proveedores orphans
            if (municipio.getProveedorList().size() > 0) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("El " + CLASS_NAME + " está asociado a Proveedores y no puede ser borrado.");
            }
            //ctrl clientes orphans
            if (municipio.getSucursalList().size() > 0) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("El " + CLASS_NAME + " está asociado a Sucursales y no puede ser borrado.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(municipio);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }

    }

    public List<Municipio> findMunicipioEntities() {
        return findMunicipioEntities(true, -1, -1);
    }

    public List<Municipio> findMunicipioEntities(int maxResults, int firstResult) {
        return findMunicipioEntities(false, maxResults, firstResult);
    }

    private List<Municipio> findMunicipioEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Municipio as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Municipio findMunicipio(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Municipio.class, id);
        } finally {
            em.close();
        }
    }

    public int getMunicipioCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Municipio as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }// </editor-fold>

    public void initContenedor(java.awt.Frame frame, boolean modal) {
        contenedor = new JDContenedor(frame, modal, "ABM - " + CLASS_NAME);
        contenedor.hideBtmImprimir();
        UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
        cargarDTM(contenedor.getDTM(), null);
        contenedor.setListener(this);
        contenedor.setVisible(true);
    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        UTIL.limpiarDtm(dtm);
        java.util.List<Municipio> l;
        if (query == null || query.length() < 1) {
            l = DAO.getEntityManager().createNamedQuery(CLASS_NAME + ".findAll").getResultList();
        } else {
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createQuery(query).getResultList();
        }

        for (Municipio o : l) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNombre(),
                        o.getDepartamento().getNombre(),});
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equalsIgnoreCase("new")) {
                try {
                    initABM(false, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(DepartamentoController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("edit")) {
                try {
                    initABM(true, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(DepartamentoController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    if (municipio == null) {
                        throw new MessageException("No hay departamento seleccionado");
                    }
                    destroy(municipio.getId());
                    municipio = null;
                    cargarDTM(contenedor.getDTM(), null);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (IllegalOrphanException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(DepartamentoController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NonexistentEntityException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(DepartamentoController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("Print")) {
            } else if (boton.getName().equalsIgnoreCase("exit")) {
                contenedor.dispose();
                contenedor = null;
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    setEntity();
                    String msj = municipio.getId() == null ? "Registrado.." : "Modificado..";
                    persistEntity(municipio);
                    abm.showMessage(msj, CLASS_NAME, 1);
                    abm.dispose();
                    cargarDTM(contenedor.getDTM(), null);
                    municipio = null;
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    Logger.getLogger(DepartamentoController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                abm.dispose();
                panel = null;
                abm = null;
                municipio = null;
            }
            return;
        } // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="JTextField">
        else if (e.getSource().getClass().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
            if (tf.getName().equalsIgnoreCase("tfFiltro")) {
            }
        } // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="ComboBox">
        else if (e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
            javax.swing.JComboBox combo = (JComboBox) e.getSource();
            if (combo.getName().equalsIgnoreCase("provincias")) {
                if (combo.getSelectedIndex() > 0) {
                    setComboBoxDepartamentos(((Provincia) combo.getSelectedItem()).getId());

                } else {
                    setComboBoxDepartamentos(0);

                }
            }

        }// </editor-fold>

    }

    private void setComboBoxDepartamentos(int idProvincia) {
        if (idProvincia != 0) {
            UTIL.loadComboBox(panel.getCbDepartamentos(),
                    new DepartamentoController().findDeptosFromProvincia(idProvincia), true);
        } else {
            UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
        DefaultTableModel dtm = (DefaultTableModel) ((javax.swing.JTable) e.getSource()).getModel();
        if (selectedRow > -1) {
            municipio = (Municipio) DAO.getEntityManager().find(Municipio.class,
                    Integer.valueOf((dtm.getValueAt(selectedRow, 0)).toString()));
        }
    }

    private void initABM(boolean isEditting, ActionEvent e) throws Exception {
        UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.TESORERIA);
        if (isEditting && municipio == null) {
            throw new MessageException("Debe elegir una fila");
        }
        panel = new PanelABMDeptos();
        panel.getCbProvincias().addActionListener(this);
        panel.hideAbreviacion();
        panel.hideCodigo();
        UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
        if (isEditting) {
            cargarPanelABM(municipio);
        }
        abm = new JDABM(true, contenedor, panel);
        abm.setTitle("ABM - " + CLASS_NAME + "s");
        if (e != null) {
            abm.setLocationRelativeTo((java.awt.Component) e.getSource());
        }
        abm.setListener(this);
        abm.setVisible(true);
    }

    private void cargarPanelABM(Municipio m) {
        boolean encontrado = false;
        int index = 0;
        javax.swing.JComboBox combo = panel.getCbProvincias();

        while (index < combo.getItemCount() && !encontrado) {
            if ((combo.getItemAt(index)).toString().equals(m.getDepartamento().getProvincia().getNombre())) {
                panel.getCbProvincias().setSelectedIndex(index);
                encontrado = true;
            }
            index++;
        }
        index = 0;
        encontrado = false;
        combo = panel.getCbDepartamentos();
        while (index < combo.getItemCount() && !encontrado) {
            if ((combo.getItemAt(index)).toString().equals(m.getDepartamento().getNombre())) {
                panel.getCbDepartamentos().setSelectedIndex(index);
                encontrado = true;
            }
            index++;
        }
        panel.setTfNombre(m.getNombre());
    }

    private void setEntity() throws MessageException {
        if (municipio == null) {
            municipio = new Municipio();
        }
        if (panel.getCbDepartamentos().getSelectedIndex() < 1) {
            throw new MessageException("Debe seleccionar un Departamento");
        }
        if (panel.getTfNombre() == null && panel.getTfNombre().length() < 1) {
            throw new MessageException("Ingrese un nombre para el municipio");
        }

        municipio.setNombre(panel.getTfNombre().toUpperCase());
        municipio.setDepartamento((Departamento) panel.getSelectedCbDepartamento());
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if (e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();

            if (tf.getName().equalsIgnoreCase("tfFiltro")) {
                armarQuery(tf.getText().trim());
            }
        }
    }

    /**
     * Arma la query, la cual va filtrar los datos en el JDContenedor
     *
     * @param filtro
     */
    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT o FROM " + CLASS_NAME + " o WHERE o.nombre LIKE '" + filtro + "%' "
                    + " ORDER BY o.departamento.nombre, o.nombre";
        }
        cargarDTM(contenedor.getDTM(), query);
    }

    private void persistEntity(Municipio municipio) throws MessageException, Exception {
        checkConstraints(municipio);
        if (municipio.getId() == null) {
            create(municipio);
        } else {
            edit(municipio);
        }
    }

    private void checkConstraints(Municipio object) throws MessageException, Exception {
        EntityManager em = getEntityManager();
        String idQuery = "";
        if (object.getId() != null) {
            idQuery = "o.id <> " + object.getId() + " AND ";
        }
        try {
            em.createQuery("SELECT o FROM " + CLASS_NAME + " o "
                    + " WHERE " + idQuery + " o.departamento.nombre= '" + object.getDepartamento().getNombre() + "'"
                    + " AND o.nombre='" + object.getNombre() + "'").getSingleResult();
            throw new MessageException("Ya existe un Municipio con este nombre en este Departamento.");
        } catch (NoResultException ex) {
        }

    }

    public List<Municipio> findMunicipiosFromDepto(int departamentoID) {
        return DAO.getEntityManager().createQuery("SELECT o FROM " + CLASS_NAME + " o WHERE o.departamento.id=" + departamentoID).getResultList();
    }
}
