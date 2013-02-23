package jgestion;

import controller.*;
import controller.exceptions.MessageException;
import generics.PropsUtils;
import gui.JFP;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.persistence.PersistenceException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.persistence.exceptions.DatabaseException;

/**
 *
 * @author Administrador
 */
public class Main {

    private static final String propertiesFile = "cfg.ini";
    private static final Logger LOG = Logger.getLogger(Main.class);
    private static boolean OCURRIO_ERROR = false;
    public static final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources");
    private JFP principal;
    private ShutDownListener sdl = new ShutDownListener() {
        @Override
        public Connection getConnection() {
            return DAO.getJDBCConnection();
        }

        @Override
        public void shutDownAction() {
            salir();
        }
    };

    static {
        //<editor-fold defaultstate="collapsed" desc="set Nimbus L&F">
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
        }
        //</editor-fold>
    }

    private Main() {
        PropertyConfigurator.configure("log4j.properties");
        threadSafe();
        try {
            Properties properties = PropsUtils.load(new File(propertiesFile));
            DAO.setProperties(properties);
            if (DAO.getEntityManager().isOpen()) {
                DAO.setDefaultData();
                sdl.getFuerzaBrutaShutDown().start();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        principal = new JFP();
                        principal.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                salir();
                            }
                        });
                        new Thread(principal, "checkDBConnection").start();
                        principal.setVisible(true);
                    }
                });
            }
        } catch (IllegalArgumentException ex) {
            LOG.trace(ex.getLocalizedMessage(), ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Ojo!", JOptionPane.WARNING_MESSAGE);
        } catch (FileNotFoundException ex) {
            LOG.fatal("No se encontro el archivo cfg.ini", ex);
            JOptionPane.showMessageDialog(null, "No se encontró el archivo de configuración cfg.ini\n" + ex.getLocalizedMessage(), "Error", 0);
        } catch (IOException ex) {
            LOG.fatal("IOExpection con archivo cfg.ini", ex);
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error intentando acceder al archivo de configuración cfg.ini\n" + ex.getLocalizedMessage(), "Error", 0);
        } catch (MessageException ex) {
            LOG.fatal("MessageException ", ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", 2);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error de conexión con la base de datos.\nMsg:" + ex.getMessage(),
                    "DatabaseException", 0);
            LOG.fatal("DataBase Error!!", ex);
            OCURRIO_ERROR = true;
        } catch (PersistenceException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error -> MAIN -> PersistenceException", 0);
            LOG.fatal("PersistenceException", ex);
            OCURRIO_ERROR = true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "ERROR CRÍTICO!\n" + ex.getMessage(), "EN MAIN", 0);
            LOG.fatal("Error fatal..!", ex);
            OCURRIO_ERROR = true;
        } finally {
            if (OCURRIO_ERROR) {
                System.out.println("Finalizando por las malas!!!");
                System.exit(1);
            }
        }
    }

    private void salir() {
        if (principal != null) {
            principal.dispose();
        }
        try {
            sdl.close();
        } catch (SQLException ex) {
            LOG.fatal(ex, ex);
        }
        if (sdl.isShutDownSystem()) {
            try {
                String shutDownMessage = sdl.getMessage();
                if (new File(System.getProperty("user.dir") + "\\Messenger.jar").exists()) {
                    Process p = Runtime.getRuntime().exec("java -jar Messenger.jar " + shutDownMessage + " Cerrado_por_mantenimiento");
                }
            } catch (Exception ex) {
                LOG.trace("shutting Down Thread!!", ex);
            }
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        Main main = new Main();
    }

    private static void threadSafe() {
        Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                DAO.closeAllConnections();
                System.out.println("threadSafe: Cerrando sistema.. ERROR=" + OCURRIO_ERROR);
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }
}
