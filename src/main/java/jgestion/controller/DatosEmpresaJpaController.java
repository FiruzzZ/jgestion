package jgestion.controller;

import java.awt.Window;
import jgestion.controller.exceptions.MessageException;
import jgestion.controller.exceptions.NonexistentEntityException;
import jgestion.entity.DatosEmpresa;
import utilities.general.UTIL;
import jgestion.gui.JDDatosEmpresa;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.swing.JButton;
import jgestion.entity.Contribuyente;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author FiruzzZ
 */
public class DatosEmpresaJpaController implements ActionListener {

    private JDDatosEmpresa jd;
    private File logoFile = null;
    private DatosEmpresa EL_OBJECT;
    private boolean quitarImagen = false;

    public EntityManager getEntityManager() {
        return DAO.getEntityManager();
    }

    // <editor-fold defaultstate="collapsed" desc="CRUD...">
    public void create(DatosEmpresa datosEmpresa) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(datosEmpresa);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(DatosEmpresa datosEmpresa) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.merge(datosEmpresa);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public DatosEmpresa findDatosEmpresa() {
        EntityManager em = getEntityManager();
        try {
            return em.find(DatosEmpresa.class, 1);
        } finally {
            em.close();
        }
    }
    // </editor-fold>

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().getClass().equals(JButton.class)) {
            JButton boton = (JButton) e.getSource();
            if (boton.getName().equals("bGuardar")) {
                try {
                    checkAndSet();
                    jd.showMessage("Datos actualizados", null, 1);
                } catch (MessageException ex) {
                    jd.showMessage(ex.getMessage(), "", 2);
                } catch (Exception ex) {
                    LogManager.getLogger().error(ex.getMessage(), ex);
                    jd.showMessage(ex.getMessage(), "Error", 0);
                }
            } else if (boton.getName().equals("bSalir")) {
                jd.dispose();
            } else if (boton.getName().equals("bBuscarFoto")) {
                try {
                    cargarImagen();
                } catch (Exception ex) {
                    LogManager.getLogger().error(ex.getMessage(), ex);
                }
            } else if (boton.getName().equals("bQuitarFoto")) {
                jd.getLabelLogo().setIcon(null);
                jd.getLabelLogo().setText("[ Logo empresa ]");
                logoFile = null;
                quitarImagen = true;
                EL_OBJECT.setLogo(null);
            }
        }
    }

    public void initJD(Window owner, boolean modal) throws Exception {
        // <editor-fold defaultstate="collapsed" desc="checking Permiso">
        try {
            UsuarioController.checkPermiso(PermisosController.PermisoDe.DATOS_GENERAL);
        } catch (MessageException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, ex.getMessage());
            return;
        }// </editor-fold>
        jd = new JDDatosEmpresa(owner, modal);
        jd.setListener(this);
        EL_OBJECT = findDatosEmpresa();
        if (EL_OBJECT != null) {
            setUI();
        }
        jd.setLocationRelativeTo(owner);
        jd.setVisible(true);
    }

    private void checkAndSet() throws MessageException, NonexistentEntityException, Exception {
        if (jd.getTfNombre().length() < 1) {
            throw new MessageException("Nombre no válido");
        }
        try {
            UTIL.VALIDAR_CUIL(jd.getTfCUIT().trim());
        } catch (Exception ex) {
            throw new MessageException(ex.getMessage());
        }

        if (jd.getDcInicioActividad() == null) {
            throw new MessageException("Fecha de inicio de actividad no válida");
        }
        if (jd.getTfDireccion().trim().length() < 1) {
            throw new MessageException("Dirección no válido");
        }

        if (jd.getTfTele1().length() < 1) {
            throw new MessageException("Teléfono1 no válido");
        }

        if (EL_OBJECT == null) {
            EL_OBJECT = new DatosEmpresa();
        }

        EL_OBJECT.setNombre(jd.getTfNombre());
        EL_OBJECT.setCuit(Long.valueOf(jd.getTfCUIT()));
        EL_OBJECT.setFechaInicioActividad(jd.getDcInicioActividad());
        EL_OBJECT.setDireccion(jd.getTfDireccion());
        EL_OBJECT.setContribuyente((Contribuyente) UTIL.getEntityWrapped(jd.getCbContribuyente()).getEntity());

        try {
            if (jd.getTfTele1().length() > 0) {
                EL_OBJECT.setTele1(Long.valueOf(jd.getTfTele1()));
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 1 no válido (Solo números)");
        }
        try {
            if (jd.getTfTele2().length() > 0) {
                EL_OBJECT.setTele2(Long.valueOf(jd.getTfTele2()));
            } else {
                EL_OBJECT.setTele2(null);
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Teléfono 2 no válido (Solo números");
        }
        try {
            if (jd.getTfCtaCte().length() > 0) {
                EL_OBJECT.setCtaCte(Long.valueOf(jd.getTfCtaCte()));
            }
        } catch (NumberFormatException e) {
            throw new MessageException("Número Cta. Cte. no válido (solo ingresar números)");
        }

        if (jd.getTfEmail().length() > 0) {
            EL_OBJECT.setEmail(jd.getTfEmail());
        }

        if (jd.getTfWeb().length() > 0) {
            EL_OBJECT.setWebPage(jd.getTfWeb());
        }

        if (logoFile != null && !quitarImagen) {
            try {
                EL_OBJECT.setLogo(UTIL.getBytesFromFile(logoFile));
            } catch (Exception ex) {
                LogManager.getLogger().error(ex.getMessage(), ex);
                jd.showMessage(ex.getMessage(), "Error seteando logo", 0);
            }
        } else {
            // si logoFile == null, es porque no se eligió ninguna foto 
            // y tampoco había una cargada
        }
        if (EL_OBJECT.getId() == null) {
            EL_OBJECT.setId(1);
            create(EL_OBJECT);
        } else {
            edit(EL_OBJECT);
        }

    }

    private void setUI() {
        jd.setTfNombre(EL_OBJECT.getNombre());
        if (EL_OBJECT.getContribuyente() != null) {
            UTIL.setSelectedItem(jd.getCbContribuyente(), EL_OBJECT.getContribuyente());
        }
        jd.setTfCUIT(String.valueOf(EL_OBJECT.getCuit()));
        jd.setDcInicioActividad(EL_OBJECT.getFechaInicioActividad());
        jd.setTfDireccion(EL_OBJECT.getDireccion());
        try {
            jd.setTfTele1(EL_OBJECT.getTele1().toString());
        } catch (NullPointerException e) {
            System.out.println("tele1 null....");
        }
        try {
            jd.setTfTele2(EL_OBJECT.getTele2().toString());
        } catch (NullPointerException e) {
            System.out.println("tele2 null....");
        }
        try {
            jd.setTfCtaCte(EL_OBJECT.getCtaCte().toString());
        } catch (NullPointerException e) {
            System.out.println("cta cte null....");
        }
        try {
            jd.setTfEmail(EL_OBJECT.getEmail());
        } catch (NullPointerException e) {
            System.out.println("email null....");
        }
        try {
            jd.setTfWeb(EL_OBJECT.getWebPage());
        } catch (NullPointerException e) {
            System.out.println("web null....");
        }

        if (EL_OBJECT.getLogo() != null && EL_OBJECT.getLogo().length > 0) {
            jd.getLabelLogo().setText(null);
            try {
                logoFile = UTIL.imageToFile(EL_OBJECT.getLogo(), "png");
                jd.getLabelLogo(UTIL.setImageAsIconLabel(jd.getLabelLogo(), logoFile));
            } catch (NullPointerException ex) {
                System.out.println("cargando logo null....???");
            } catch (IOException ex) {
                jd.showMessage(ex.getMessage(), "Error carga imagen", 0);
            } catch (Exception ex) {
                jd.showMessage(ex.getMessage(), "Error carga imagen", 0);
                ex.printStackTrace();
            }
        }
    }

    private void cargarImagen() throws Exception {
        javax.swing.JFileChooser filec = new javax.swing.JFileChooser();
        javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Imagenes", "jpg", "bmp", "jpeg", "png");
        filec.setFileFilter(filter);
        filec.addChoosableFileFilter(filter);
        int val = filec.showOpenDialog(null);
        if (val == javax.swing.JFileChooser.APPROVE_OPTION) {
            logoFile = filec.getSelectedFile();
            if (UTIL.isImagenExtension(logoFile)) {
                try {
                    jd.getLabelLogo().setText(null);
                    jd.setLabelLogo(UTIL.setImageAsIconLabel(jd.getLabelLogo(), logoFile));
                    quitarImagen = false;
                } catch (java.io.IOException ex) {
                    jd.showMessage(ex.getMessage(), "Error", 0);
                    ex.printStackTrace();
                }
            } else {
                logoFile = null;
                jd.showMessage("El archivo debe ser una imagen", "Extensión de archivo", 0);
            }
        }
    }
}
