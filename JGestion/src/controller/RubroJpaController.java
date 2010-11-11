package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Rubro;
import generics.UTIL;
import gui.JDMiniABM;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.swing.table.DefaultTableModel;

/**
 * Clase encargada del DAO y CRUD de los Rubro's de los Productos, Clientes
 * y Proveedores.
 * @author FiruzzZ
 */
public class RubroJpaController implements ActionListener, MouseListener {
    public static final int DE_PRODUCTO=1;
//    public static final int DE_CLIENTE=2;
//    public static final int DE_PROVEEDOR=3;

    public final String CLASS_NAME="Rubro";
    private final String[] colsName={"Nº","Nombre","Código"};
    private final int[] colsWidth={20,120,80};
    private JDMiniABM abm;
    /**
     * Con este, separamos los rubros que usan las distintas entidades.
     * 1= productos, 2= clientes, 3 = proveedores
     */
    private final short TIPO;
    private Rubro rubro;

    /**
     * @param tipo 1=Productos, 2=clientes, 3=Proveedores
     */
    public RubroJpaController() {
        TIPO = 1;
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    public void create(Rubro rubro) throws Exception {
        DAO.create(rubro);
    }

    public void edit(Rubro rubro) {
        DAO.doMerge(rubro);
    }

    public void destroy(Integer id) throws NonexistentEntityException, IllegalOrphanException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Rubro rubro;
            //ctrl de Existencia.......
            try {
                rubro = em.getReference(Rubro.class, id);
                rubro.getIdrubro();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The rubro with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            //contrl de clientes orphans

            int cantOrphans = 0;
            cantOrphans += getListDeForeignKeys(id);
            cantOrphans += getListSubRubros(id);

            if (cantOrphans > 0) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new java.util.ArrayList<String>();
                }
                illegalOrphanMessages.add("No puede eliminar este rubro porque hay " + cantOrphans + " " + rubroToString(cantOrphans) + " relacionados a este");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(rubro);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Rubro> findRubros() {
        return DAO.getEntityManager().createNamedQuery("Rubro.findByTipo").setParameter("tipo", TIPO).getResultList();
    }

    public List<Rubro> findRubros(int rubro_de) {
        return DAO.getEntityManager().createNamedQuery("Rubro.findByTipo").setParameter("tipo", rubro_de).getResultList();
    }

    public Rubro findRubro(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Rubro.class, id);
        } finally {
            em.close();
        }
    }// </editor-fold>

    private void checkConstraints(Rubro rubro) throws MessageException, Exception {
        String idQuery="";
        if(rubro.getIdrubro()!=null) idQuery="o.idrubro!="+rubro.getIdrubro()+" AND ";
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM "+CLASS_NAME+" o " +
                    " WHERE "+idQuery+" o.nombre='"+rubro.getNombre()+"' AND o.tipo="+rubro.getTipo() , Rubro.class)
                    .getSingleResult();
            throw new MessageException("Ya existe otro "+CLASS_NAME+" con este nombre.");
        } catch (NoResultException ex) { }
        if(rubro.getCodigo()!=null && rubro.getCodigo().length()>0)
        try {
            DAO.getEntityManager().createNativeQuery("SELECT * FROM "+CLASS_NAME+" o " +
                    " WHERE "+idQuery+" o.codigo='"+rubro.getCodigo()+"' AND o.tipo="+rubro.getTipo() , Rubro.class)
                    .getSingleResult();
            throw new MessageException("Ya existe otro "+CLASS_NAME+" con este código.");
        } catch (NoResultException ex) { }

        //persistiendo......
        if(rubro.getIdrubro()==null)
            create(rubro);
        else
            edit(rubro);
    }

    private void setEntity() throws MessageException {
        if(rubro==null) rubro = new Rubro();
        if(abm.getTfNombre()==null || abm.getTfNombre().trim().length() < 1 )
            throw new MessageException("Debe ingresar un nombre de "+CLASS_NAME.toLowerCase());
        rubro.setNombre(abm.getTfNombre().trim().toUpperCase());
        if(abm.getTfCodigo().trim().length()>0)
            rubro.setCodigo(abm.getTfCodigo().trim().toUpperCase());
        rubro.setTipo(TIPO); // <--- la magia
    }

    public void actionPerformed(ActionEvent e) {
        // <editor-fold defaultstate="collapsed" desc="JButton">
        if (e.getSource().getClass().equals(javax.swing.JButton.class)) {
            javax.swing.JButton boton = (javax.swing.JButton) e.getSource();

            if (boton.getName().equalsIgnoreCase("new")) {
                rubro=null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    eliminarRubro();
                    rubro=null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                    abm.showMessage("Eliminado..", CLASS_NAME, 1);
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (NonexistentEntityException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    ex.printStackTrace();
                } catch (IllegalOrphanException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                rubro=null;
                abm.clearPanelFields();
            } else if (boton.getName().equalsIgnoreCase("guardar")) {
                try {
                    setEntity();
                    checkConstraints(rubro);
                    rubro=null;
                    abm.clearPanelFields();
                    cargarDTM(abm.getDTM(), "");
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch(Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    ex.printStackTrace();
                }
            }

            return;
        }// </editor-fold>
    }

    public void initABM(java.awt.Frame frame, boolean modal) throws Exception {
       // <editor-fold defaultstate="collapsed" desc="checking Permiso">
      try {
         UsuarioJpaController.checkPermisos(PermisosJpaController.PermisoDe.DATOS_GENERAL);
      } catch (MessageException ex) {
         javax.swing.JOptionPane.showMessageDialog(null,ex.getMessage());
         return;
      }// </editor-fold>
        abm = new JDMiniABM(frame, modal);
        abm.setLocationRelativeTo(frame);
        abm.hideFieldExtra();
        abm.hideBtnLock();
        abm.setTitle("ABM - Rubros de "+rubroToString());
        UTIL.getDefaultTableModel(abm.getjTable1(), colsName, colsWidth);
        //oculta columna ID
        UTIL.hideColumnTable(abm.getjTable1(), 0);
        cargarDTM(abm.getDTM(), null);
        abm.setListeners(this);
        abm.setVisible(true);
    }

    public void mouseReleased(MouseEvent e) {
        Integer selectedRow = ((javax.swing.JTable)e.getSource()).getSelectedRow();
        DefaultTableModel dtm = (DefaultTableModel) ((javax.swing.JTable)e.getSource()).getModel();
        if(selectedRow > -1)
            rubro = (Rubro) DAO.getEntityManager().find(Rubro.class,
                Integer.valueOf((dtm.getValueAt(selectedRow, 0)).toString()));
        if(rubro!=null)
            setPanelFields(rubro);
    }

    public void mouseClicked(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        for(int i=dtm.getRowCount(); i>0;i--) {dtm.removeRow(i-1); }
        java.util.List<Rubro> l;
        if(query==null || query.length()<10)
            l = DAO.getEntityManager().createNamedQuery(CLASS_NAME+".findByTipo").setParameter("tipo", TIPO).getResultList();
        else
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(query, Rubro.class).getResultList();
        for (Rubro o : l) {
            dtm.addRow(new Object[] {
                o.getIdrubro(),
                o.getNombre(),
                o.getCodigo()
            });
        }
    }

    private void setPanelFields(Rubro o) {
        abm.setTfNombre(o.getNombre());
        if(o.getCodigo()!=null)
            abm.setTfCodigo(o.getCodigo());
        else abm.setTfCodigo("");
    }

    private void eliminarRubro() throws MessageException, NonexistentEntityException, IllegalOrphanException {
        if(rubro==null)
            throw new MessageException("No hay "+CLASS_NAME+" seleccionado");
        destroy(rubro.getIdrubro());
    }

    private int getListDeForeignKeys(int idRubro) {
        try {

            return DAO.getEntityManager().createNativeQuery("select * from "+rubroToString()+" where rubro="+idRubro).getResultList().size();
        } catch (NoResultException ex) { return 0;
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    private int getListSubRubros(int idRubro) {
        try {
            return DAO.getEntityManager().createNativeQuery("select * from "+rubroToString()+" where subrubro="+idRubro).getResultList().size();
        } catch (NoResultException ex) { return 0;
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }
    private String rubroToString(int rubrosDe) {
        if(rubrosDe<=1)
            switch (TIPO) {
                case 1: return "Producto";
                case 2: return "Cliente";
                case 3: return "Proveedor";
                default: return null;
            }
        else
            switch (TIPO) {
                case 1: return "Productos";
                case 2: return "Clientes";
                case 3: return "Proveedores";
                default: return null;
            }
    }

    private String rubroToString() {
        switch (TIPO) {
            case 1: return "Producto";
            case 2: return "Cliente";
            case 3: return "Proveedor";
            default: return null;
        }
    }
}
