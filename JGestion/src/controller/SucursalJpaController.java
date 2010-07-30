package controller;

import controller.exceptions.*;
import entity.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMSucursal;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JButton;

/**
 *
 * @author FiruzzZ
 */

public class SucursalJpaController implements ActionListener, MouseListener, KeyListener {

    public final String CLASS_NAME = "Sucursal";
    private JDContenedor contenedor = null;
    private JDABM abm;
    private final String[] colsName = {"Nº","Nombre","Encargado","Dirección","Teléfonos","Prov.","Depto.","Municipio","Email"};
    private final int[] colsWidth = { 15   ,100     ,80        ,100         ,100        ,40     ,40      ,40      ,50};
    private PanelABMSucursal panel;

    /**
     * Variable global interna, se una para el alta y modificación de una entidad Sucursal.
     * Uso exclusivo dentro de la class.
     */
    private Sucursal sucursal;

    // <editor-fold defaultstate="collapsed" desc="CRUD y List's">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Sucursal sucursal) throws Exception {
        DAO.create(sucursal);
    }

    public void edit(Sucursal sucursal) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            try {
                Sucursal OldSucursal = em.getReference(Sucursal.class, sucursal.getId());
                OldSucursal.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The sucursal with id " + sucursal.getId() + " no existe mas.", enfe);
            }
            em.getTransaction().commit();
            DAO.doMerge(sucursal);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Sucursal sucursal;
            try {
                sucursal = em.getReference(Sucursal.class, id);
                sucursal.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The sucursal with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Cliente> clienteListOrphanCheck = sucursal.getClienteList();
            if (clienteListOrphanCheck.size() > 0) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Sucursal (" + sucursal + ") no puede ser eliminada por que hay Clientes asociados a esta.");
            }

            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }

            em.remove(sucursal);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Sucursal> findSucursalEntities() {
        return findSucursalEntities(true, -1, -1);
    }

    public List<Sucursal> findSucursalEntities(int maxResults, int firstResult) {
        return findSucursalEntities(false, maxResults, firstResult);
    }

    private List<Sucursal> findSucursalEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Sucursal as o order by o.nombre");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Sucursal findSucursal(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Sucursal.class, id);
        } finally {
            em.close();
        }
    }

    public int getSucursalCount() {
        EntityManager em = getEntityManager();
        try {
            return ((Long) em.createQuery("select count(o) from Sucursal as o").getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    // </editor-fold>

    private void initABM(boolean isEditting, ActionEvent e) throws MessageException {
       // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.DATOS_GENERAL);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage());
         return;
      }// </editor-fold>
        if(isEditting && sucursal == null)
            throw new MessageException("Debe elegir una fila");
        panel = new PanelABMSucursal();
        UTIL.loadComboBox(panel.getCbProvincias(),new ProvinciaJpaController().findProvinciaEntities(), true);
        UTIL.loadComboBox(panel.getCbDepartamentos(),null, true);
        UTIL.loadComboBox(panel.getCbMunicipios(),null, true);
        panel.setListener(this);
        if(isEditting)
            setPanel(sucursal);
        abm = new JDABM(contenedor, true, panel);
        abm.setTitle("ABM "+CLASS_NAME+"es");
        abm.setLocationRelativeTo(contenedor);
        abm.setListener(this);
        abm.setVisible(true);
    }

    public void keyReleased(KeyEvent e) {
        if(e.getComponent().getClass().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getComponent();
            if(tf.getName().equalsIgnoreCase("tfFiltro")) {
                armarQuery(tf.getText().trim());
            }
        }
    }

    /**
     * Arma la query, la cual va filtrar los datos en el JDContenedor
     * @param filtro
     */
    private void armarQuery(String filtro) {
        String query = null;
        if(filtro != null && filtro.length() > 0)
            query = "SELECT * FROM "+CLASS_NAME+" o WHERE o.nombre LIKE '"+filtro+"%'";
        cargarDTM(contenedor.getDTM(), query);
    }

    public void initContenedor(java.awt.Frame frame, boolean modal) {
        //init contenedor
        contenedor = new JDContenedor(frame, modal, "ABM - "+ CLASS_NAME);
        contenedor.setSize(contenedor.getWidth() + 200, contenedor.getHeight());
//        contenedor.hideBtmEliminar();
        contenedor.hideBtmImprimir();
        try {
            UTIL.getDefaultTableModel(colsName, colsWidth, contenedor.getjTable1());
        } catch (Exception ex) {
            Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
        }
        cargarDTM(contenedor.getDTM(), null);
        //listener
        contenedor.setListener(this);
        contenedor.setVisible(true);

    }

    private void cargarDTM(javax.swing.table.DefaultTableModel dtm, String query) {
        //limpia el modelo
        for(int i=dtm.getRowCount(); i > 0 ;i--) { dtm.removeRow(i-1); }
        java.util.List<Sucursal> l;
        if(query==null)
            l = DAO.getEntityManager().createNamedQuery(CLASS_NAME+".findAll").getResultList();
        else
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(query, Sucursal.class).getResultList();

        for (Sucursal o : l) {
            dtm.addRow(new Object[] {
                o.getId(),
                o.getNombre(),
                o.getEncargado()!=null ? o.getEncargado() : "",
                o.getDireccion(),
                getTele1ToString(o)+" / " + getTele2ToString(o),
                o.getProvincia().getNombre(),
                o.getDepartamento().getNombre(),
                o.getMunicipio().getNombre(),
                o.getEmail()!=null ? o.getEmail() : "",
            });
        }
    }

   
    public void mouseReleased(MouseEvent e) {
    Integer selectedRow = ((javax.swing.JTable)e.getSource()).getSelectedRow();
        if(selectedRow>-1)
            sucursal = (Sucursal) DAO.getEntityManager().find(Sucursal.class,
                Integer.valueOf((((javax.swing.JTable)e.getSource()).getValueAt(selectedRow, 0)).toString()));
    }
    public void mouseClicked(MouseEvent e) {    }
    public void mousePressed(MouseEvent e) {    }
    public void mouseEntered(MouseEvent e) {    }
    public void mouseExited(MouseEvent e) {    }
    public void keyTyped(KeyEvent e) {    }
    public void keyPressed(KeyEvent e) {    }

    private void setEntity() throws MessageException, Exception {
        if(sucursal==null)
            sucursal = new Sucursal();

        if(panel.getTfNombre()==null || panel.getTfNombre().length() < 1 )
            throw new MessageException("Debe ingresar un nombre");
        if(panel.getTfDireccion()==null)
            throw new MessageException("Debe indicar la dirección de la sucursal");
        if(panel.getCbProvincias().getSelectedIndex()<1)
            throw new MessageException("Debe especificar una Provincia, Departamento, Municipio");
        if(panel.getCbDepartamentos().getSelectedIndex()<1)
            throw new MessageException("Debe especificar un Departamento");
        if(panel.getCbMunicipios().getSelectedIndex()<1)
            throw new MessageException("Debe especificar un Municipio");
        if( (panel.getTfInterno1()!=null && panel.getTfInterno1().length()>0) && panel.getTfTele1()==null)
            throw new MessageException("Especifique un número de teléfono 1 para el interno 1");
        if( (panel.getTfInterno2()!=null && panel.getTfInterno2().length() > 0) &&
                (panel.getTfTele2()==null || panel.getTfTele2().length() < 1 ))
            throw new MessageException("Especifique un número de teléfono 2 para el interno 2");

        // NOT NULLABLE's
        sucursal.setNombre(panel.getTfNombre().toUpperCase());
        sucursal.setDireccion(panel.getTfDireccion());
        sucursal.setProvincia((Provincia) panel.getSelectedProvincia());
        sucursal.setDepartamento((Depto) panel.getSelectedDepartamento());
        sucursal.setMunicipio((Municipio) panel.getSelectedMunicipio());

        //NULLABLE's
        if(panel.getTfEncargado()!=null)
            sucursal.setEncargado(panel.getTfEncargado().toUpperCase());

        if(panel.getTfEmail()!=null)
            sucursal.setEmail(panel.getTfEmail().toUpperCase());
        
        if(panel.getTfTele1()!=null && panel.getTfTele1().length() > 0) {
            sucursal.setTele1(new BigInteger(panel.getTfTele1()));
            if(panel.getTfInterno1().length()>0)
                sucursal.setInterno1(new Integer(panel.getTfInterno1()));
        }
        if(panel.getTfTele2()!=null && panel.getTfTele2().length() > 0) {
            sucursal.setTele2(new BigInteger(panel.getTfTele2()));
            if(panel.getTfInterno2().length()>0)
                sucursal.setInterno2(new Integer(panel.getTfInterno2()));
        }
        System.out.println("->"+sucursal.getNombre()+sucursal.getProvincia()+sucursal.getDepartamento()+sucursal.getMunicipio());
    }

    private void checkConstraints(Sucursal object) throws MessageException, IllegalOrphanException, NonexistentEntityException, Exception {
        String idQuery="";
        if(object.getId()!=null) idQuery="o.id!="+object.getId()+" AND ";
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM "+CLASS_NAME+" o " +
                    " WHERE "+idQuery+" o.nombre='"+object.getNombre()+"' ", Sucursal.class)
                    .getSingleResult();
            throw new MessageException("Ya existe otra "+CLASS_NAME+" con este nombre.");
        } catch (NoResultException ex) { }


        //persistiendo......
        if(object.getId()==null)
            create(object);
        else
            edit(object);
    }

    private void setPanel(Sucursal s) {
        panel.setTfNombre(s.getNombre());
        panel.setTfDireccion(s.getDireccion());
        
        if(s.getEncargado()!=null)
            panel.setTfEncargado(s.getEncargado());

        if(s.getTele1()!=null)
            panel.setTfTele1(s.getTele1().toString());
            if(s.getInterno1()!=null)
                panel.setTfInterno1(s.getInterno1().toString());

        if(s.getTele2()!=null)
            panel.setTfTele2(s.getTele2().toString());
            if(s.getInterno2()!=null)
                panel.setTfInterno2(s.getInterno2().toString());

        if(s.getEmail()!=null)
            panel.setTfEmail(s.getEmail());

        for (int i=0; i < panel.getCbProvincias().getItemCount(); i++) {
            if(panel.getCbProvincias().getItemAt(i).toString().equals(s.getProvincia().getNombre())) {
                panel.getCbProvincias().setSelectedIndex(i);
                i=777;
            }
        }
        for (int i=0; i < panel.getCbDepartamentos().getItemCount(); i++) {
            if(panel.getCbDepartamentos().getItemAt(i).toString().equals(s.getDepartamento().getNombre())) {
                panel.getCbDepartamentos().setSelectedIndex(i);
                i=777;
            }
        }
        for (int i=0; i < panel.getCbMunicipios().getItemCount(); i++) {
            if(panel.getCbMunicipios().getItemAt(i).toString().equals(s.getMunicipio().getNombre())) {
                panel.getCbMunicipios().setSelectedIndex(i);
                i=777;
            }
        }
    }

    private String getTele1ToString(Sucursal o) {
        String tele = null;
        if(o.getTele1()!=null) {
            tele= o.getTele1().toString();
            if(o.getInterno1()!=null)
                tele+="-"+o.getInterno1();
        }
        return tele!=null ? tele : "-";
    }
    private String getTele2ToString(Sucursal o) {
        String tele = null;
        if(o.getTele2()!=null) {
            tele= o.getTele2().toString();
            if(o.getInterno2()!=null)
                tele+="-"+o.getInterno1();
        }
        return tele!=null ? tele : "-";
    }

    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if(boton.getName().equalsIgnoreCase("new")) {
                try {
                    initABM(false, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("edit")) {
                try {
                    initABM(true, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    if(sucursal==null)
                        throw new MessageException("No hay "+CLASS_NAME+" seleccionada");
                    destroy(sucursal.getId());
                    cargarDTM(contenedor.getDTM(), null);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex){
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("Print")) {

            } else if (boton.getName().equalsIgnoreCase("exit")) {
                contenedor.dispose();
                contenedor=null;
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    setEntity();
                    checkConstraints(sucursal);
                    abm.showMessage(sucursal.getId()==null ? "Registrado" : "Modificado", CLASS_NAME, 1);
                    sucursal=null;
                    cargarDTM(contenedor.getDTM(), "");
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    Logger.getLogger(SucursalJpaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                abm.dispose();
                panel=null;
                abm=null;
                sucursal=null;
            }
            return;
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="JTextField">
        else if(e.getSource().getClass().equals(javax.swing.JTextField.class)) {
            javax.swing.JTextField tf = (javax.swing.JTextField) e.getSource();
            if (tf.getName().equalsIgnoreCase("tfFiltro")) {

            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="ComboBox">
        else if(e.getSource().getClass().equals(javax.swing.JComboBox.class)) {
            javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
            if (combo.getName().equalsIgnoreCase("cbProvincias")) {
                if(combo.getSelectedIndex() > 0)
                    UTIL.loadComboBox(panel.getCbDepartamentos(), new DeptoJpaController().findDeptosFromProvincia(((Provincia)combo.getSelectedItem()).getIdprovincia()), true);
                else
                    UTIL.loadComboBox(panel.getCbDepartamentos(), new DeptoJpaController().findDeptosFromProvincia(0), true);
            } else if (combo.getName().equalsIgnoreCase("cbDepartamentos")) {
                if(combo.getSelectedIndex() > 0)
                    UTIL.loadComboBox(panel.getCbMunicipios(), new MunicipioJpaController().findMunicipiosFromDepto(((Depto)combo.getSelectedItem()).getIddepto()), true);
                else
                    UTIL.loadComboBox(panel.getCbMunicipios(), new MunicipioJpaController().findMunicipiosFromDepto(0), true);
            }
        }
        // </editor-fold>
    }

}
