package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.MessageException;
import controller.exceptions.NonexistentEntityException;
import entity.Departamento;
import entity.Municipio;
import entity.Provincia;
import entity.Sucursal;
import gui.JDABM;
import gui.JDContenedor;
import gui.PanelABMSucursal;
import java.awt.event.*;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import jpa.controller.SucursalJpaController;
import utilities.general.UTIL;

/**
 *
 * @author FiruzzZ
 */
public class SucursalController implements ActionListener, MouseListener {

    public final String CLASS_NAME = "Sucursal";
    private JDContenedor contenedor = null;
    private JDABM abm;
    private final String[] colsName = {"Nº", "Nombre", "Punto Venta", "Encargado", "Dirección", "Teléfonos", "Prov.", "Depto.", "Municipio", "Email"};
    private final int[] colsWidth = {15, 100, 20, 80, 100, 100, 40, 40, 40, 50};
    private PanelABMSucursal panel;
    private SucursalJpaController jpaController;
    /**
     * Variable global interna, se una para el alta y modificación de una
     * entidad Sucursal. Uso exclusivo dentro de la class.
     */
    private Sucursal ELOBJECT;

    public SucursalController() {
        jpaController = new SucursalJpaController();
    }

    private void initABM(boolean isEditting, ActionEvent e) throws MessageException {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioJpaController.checkPermiso(PermisosJpaController.PermisoDe.DATOS_GENERAL);
        } catch (MessageException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>
        if (isEditting && ELOBJECT == null) {
            throw new MessageException("Debe elegir una fila");
        }
        panel = new PanelABMSucursal();
        UTIL.loadComboBox(panel.getCbProvincias(), new ProvinciaJpaController().findProvinciaEntities(), true);
        UTIL.loadComboBox(panel.getCbDepartamentos(), null, true);
        UTIL.loadComboBox(panel.getCbMunicipios(), null, true);
        panel.setListener(this);
        if (isEditting) {
            setPanel(ELOBJECT);
        }
        abm = new JDABM(true, contenedor, panel);
        abm.setTitle("ABM " + CLASS_NAME + "es");
        abm.setLocationRelativeTo(contenedor);
        abm.setListener(this);
        abm.setVisible(true);
    }

    /**
     * Arma la query, la cual va filtrar los datos en el JDContenedor
     *
     * @param filtro
     */
    private void armarQuery(String filtro) {
        String query = null;
        if (filtro != null && filtro.length() > 0) {
            query = "SELECT * FROM " + CLASS_NAME + " o WHERE o.nombre LIKE '" + filtro.toUpperCase() + "%'";
        }
        cargarDTM(contenedor.getDTM(), query);
    }

    public void initContenedor(java.awt.Frame frame, boolean modal) {
        //init contenedor
        contenedor = new JDContenedor(frame, modal, "ABM - " + CLASS_NAME);
        contenedor.setSize(contenedor.getWidth() + 200, contenedor.getHeight());
//        contenedor.hideBtmEliminar();
        contenedor.hideBtmImprimir();
        try {
            UTIL.getDefaultTableModel(contenedor.getjTable1(), colsName, colsWidth);
        } catch (Exception ex) {
            Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
        }
        cargarDTM(contenedor.getDTM(), null);
        contenedor.getTfFiltro().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                armarQuery(contenedor.getTfFiltro().getText().trim());
            }
        });
        contenedor.setListener(this);
        contenedor.setVisible(true);

    }

    private void cargarDTM(DefaultTableModel dtm, String query) {
        dtm.setRowCount(0);
        List<Sucursal> l;
        if (query == null) {
            l = jpaController.findAll();
        } else {
            // para cuando se usa el Buscador del ABM
            l = DAO.getEntityManager().createNativeQuery(query, Sucursal.class).getResultList();
        }

        for (Sucursal o : l) {
            dtm.addRow(new Object[]{
                        o.getId(),
                        o.getNombre(),
                        UTIL.AGREGAR_CEROS(o.getPuntoVenta(), 4),
                        o.getEncargado() != null ? o.getEncargado() : "",
                        o.getDireccion(),
                        getTele1ToString(o) + " / " + getTele2ToString(o),
                        o.getProvincia().getNombre(),
                        o.getDepartamento().getNombre(),
                        o.getMunicipio().getNombre(),
                        o.getEmail() != null ? o.getEmail() : ""});
        }
    }

    public void mouseReleased(MouseEvent e) {
        Integer selectedRow = ((javax.swing.JTable) e.getSource()).getSelectedRow();
        if (selectedRow > -1) {
            ELOBJECT = (Sucursal) DAO.getEntityManager().find(Sucursal.class,
                    Integer.valueOf((((javax.swing.JTable) e.getSource()).getValueAt(selectedRow, 0)).toString()));
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private void setEntity() throws MessageException, Exception {
        if (ELOBJECT == null) {
            ELOBJECT = new Sucursal();
        }
        long puntoVenta;
        // <editor-fold defaultstate="collapsed" desc="CTRLS">
        if (panel.getTfNombre() == null || panel.getTfNombre().length() < 1) {
            throw new MessageException("Debe ingresar un nombre");
        }
        try {
            puntoVenta = Integer.valueOf(panel.getTfPuntoVenta().getText());
            if (puntoVenta < 1 || puntoVenta > 9999) {
                throw new MessageException("Punto de Venta no válido (debe estar compuesto solo por números, entre 0001 y 9999)");
            }
        } catch (NumberFormatException ex) {
            throw new MessageException("Punto de Venta no válido (debe estar compuesto solo por números, entre 0001 y 9999)");
        }
        if (panel.getTfDireccion() == null) {
            throw new MessageException("Debe indicar la dirección de la sucursal");
        }
        if (panel.getCbProvincias().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar una Provincia, Departamento, Municipio");
        }
        if (panel.getCbDepartamentos().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Departamento");
        }
        if (panel.getCbMunicipios().getSelectedIndex() < 1) {
            throw new MessageException("Debe especificar un Municipio");
        }
        if ((panel.getTfInterno1() != null && panel.getTfInterno1().length() > 0) && panel.getTfTele1() == null) {
            throw new MessageException("Especifique un número de teléfono 1 para el interno 1");
        }
        if ((panel.getTfInterno2() != null && panel.getTfInterno2().length() > 0)
                && (panel.getTfTele2() == null || panel.getTfTele2().length() < 1)) {
            throw new MessageException("Especifique un número de teléfono 2 para el interno 2");
        }// </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="NULLable's">
        if (panel.getTfEncargado() != null) {
            ELOBJECT.setEncargado(panel.getTfEncargado().toUpperCase());
        }

        if (panel.getTfEmail() != null) {
            ELOBJECT.setEmail(panel.getTfEmail().toUpperCase());
        }

        if (panel.getTfTele1() != null && panel.getTfTele1().length() > 0) {
            ELOBJECT.setTele1(new BigInteger(panel.getTfTele1()));
            if (panel.getTfInterno1().length() > 0) {
                ELOBJECT.setInterno1(new Integer(panel.getTfInterno1()));
            }
        }
        if (panel.getTfTele2() != null && panel.getTfTele2().length() > 0) {
            ELOBJECT.setTele2(new BigInteger(panel.getTfTele2()));
            if (panel.getTfInterno2().length() > 0) {
                ELOBJECT.setInterno2(new Integer(panel.getTfInterno2()));
            }
        }// </editor-fold>
        // NOT NULLABLE's
        ELOBJECT.setNombre(panel.getTfNombre().toUpperCase());
        ELOBJECT.setDireccion(panel.getTfDireccion());
        ELOBJECT.setProvincia((Provincia) panel.getSelectedProvincia());
        ELOBJECT.setDepartamento((Departamento) panel.getSelectedDepartamento());
        ELOBJECT.setMunicipio((Municipio) panel.getSelectedMunicipio());
        ELOBJECT.setPuntoVenta(puntoVenta);
    }

    private void checkConstraints(Sucursal object) throws MessageException, IllegalOrphanException, NonexistentEntityException {
        String idQuery = "";
        if (object.getId() != null) {
            idQuery = "o.id !=" + object.getId() + " AND ";
        }
        if (!jpaController.findByNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                + " WHERE " + idQuery + " o.nombre='" + object.getNombre() + "' ").isEmpty()) {
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este nombre.");
        }
        if (!jpaController.findByNativeQuery("SELECT * FROM " + CLASS_NAME + " o "
                + " WHERE " + idQuery + " o.puntoventa=" + object.getPuntoVenta()).isEmpty()) {
            throw new MessageException("Ya existe otra " + CLASS_NAME + " con este punto de venta.");
        }
        if (object.getId() == null) {
            jpaController.create(object);
        } else {
            jpaController.merge(object);
        }
    }

    private void setPanel(Sucursal s) {
        panel.setTfNombre(s.getNombre());
        panel.getTfPuntoVenta().setText(UTIL.AGREGAR_CEROS(s.getPuntoVenta(), 4));
        panel.setTfDireccion(s.getDireccion());

        if (s.getEncargado() != null) {
            panel.setTfEncargado(s.getEncargado());
        }

        if (s.getTele1() != null) {
            panel.setTfTele1(s.getTele1().toString());
        }
        if (s.getInterno1() != null) {
            panel.setTfInterno1(s.getInterno1().toString());
        }

        if (s.getTele2() != null) {
            panel.setTfTele2(s.getTele2().toString());
        }
        if (s.getInterno2() != null) {
            panel.setTfInterno2(s.getInterno2().toString());
        }

        if (s.getEmail() != null) {
            panel.setTfEmail(s.getEmail());
        }

        for (int i = 0; i < panel.getCbProvincias().getItemCount(); i++) {
            if (panel.getCbProvincias().getItemAt(i).toString().equals(s.getProvincia().getNombre())) {
                panel.getCbProvincias().setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < panel.getCbDepartamentos().getItemCount(); i++) {
            if (panel.getCbDepartamentos().getItemAt(i).toString().equals(s.getDepartamento().getNombre())) {
                panel.getCbDepartamentos().setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < panel.getCbMunicipios().getItemCount(); i++) {
            if (panel.getCbMunicipios().getItemAt(i).toString().equals(s.getMunicipio().getNombre())) {
                panel.getCbMunicipios().setSelectedIndex(i);
                break;
            }
        }
    }

    private String getTele1ToString(Sucursal o) {
        String tele = null;
        if (o.getTele1() != null) {
            tele = o.getTele1().toString();
            if (o.getInterno1() != null) {
                tele += "-" + o.getInterno1();
            }
        }
        return tele != null ? tele : "-";
    }

    private String getTele2ToString(Sucursal o) {
        String tele = null;
        if (o.getTele2() != null) {
            tele = o.getTele2().toString();
            if (o.getInterno2() != null) {
                tele += "-" + o.getInterno1();
            }
        }
        return tele != null ? tele : "-";
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
                    Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("edit")) {
                try {
                    initABM(true, e);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (boton.getName().equalsIgnoreCase("del")) {
                try {
                    if (ELOBJECT == null) {
                        throw new MessageException("No hay " + CLASS_NAME + " seleccionada");
                    }
                    jpaController.remove(ELOBJECT);
                    cargarDTM(contenedor.getDTM(), null);
                } catch (MessageException ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    contenedor.showMessage(ex.getMessage(), CLASS_NAME, 0);
                    Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("Print")) {
            } else if (boton.getName().equalsIgnoreCase("exit")) {
                contenedor.dispose();
                contenedor = null;
            } else if (boton.getName().equalsIgnoreCase("aceptar")) {
                try {
                    setEntity();
                    checkConstraints(ELOBJECT);
                    String msg = ELOBJECT.getId() == null ? "Registrado" : "Modificado";
                    abm.showMessage(msg, CLASS_NAME, 1);
                    ELOBJECT = null;
                    cargarDTM(contenedor.getDTM(), "");
                    abm.dispose();
                } catch (MessageException ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                } catch (Exception ex) {
                    abm.showMessage(ex.getMessage(), CLASS_NAME, 2);
                    Logger.getLogger(SucursalController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (boton.getName().equalsIgnoreCase("cancelar")) {
                abm.dispose();
                panel = null;
                abm = null;
                ELOBJECT = null;
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
            javax.swing.JComboBox combo = (javax.swing.JComboBox) e.getSource();
            if (combo.getName().equalsIgnoreCase("cbProvincias")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panel.getCbDepartamentos(), new DepartamentoController().findDeptosFromProvincia(((Provincia) combo.getSelectedItem()).getId()), true);
                } else {
                    UTIL.loadComboBox(panel.getCbDepartamentos(), new DepartamentoController().findDeptosFromProvincia(0), true);
                }
            } else if (combo.getName().equalsIgnoreCase("cbDepartamentos")) {
                if (combo.getSelectedIndex() > 0) {
                    UTIL.loadComboBox(panel.getCbMunicipios(), new MunicipioJpaController().findMunicipiosFromDepto(((Departamento) combo.getSelectedItem()).getId()), true);
                } else {
                    UTIL.loadComboBox(panel.getCbMunicipios(), new MunicipioJpaController().findMunicipiosFromDepto(0), true);
                }
            }
        }
        // </editor-fold>
    }
}
