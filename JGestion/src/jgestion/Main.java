package jgestion;

import controller.*;
import controller.exceptions.MessageException;
import generics.PropsUtils;
import gui.JFP;
import java.awt.EventQueue;
import java.awt.SplashScreen;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.persistence.PersistenceException;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.persistence.exceptions.DatabaseException;

/**
 *
 * @author Administrador
 */
public class Main {
    private static final String propertiesFile = "cfg.ini";
    private static final Logger log = Logger.getLogger(Main.class);
    private static boolean OCURRIO_ERROR = false;
    public static ResourceBundle resourceBundle = ResourceBundle.getBundle("resources");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SplashScreen.getSplashScreen();
        PropertyConfigurator.configure("log4j.properties");
        threadSafe();
        try {
            Properties properties = PropsUtils.load(new File(propertiesFile));
            DAO.setProperties(properties);
            if (DAO.getEntityManager().isOpen()) {
                DAO.setDefaultData();
//                FacturaVenta findFacturaVenta = new FacturaVentaJpaController().findFacturaVenta(436);
//                AFIPWSController af = new AFIPWSController();
//                JDialog invokeFE = af.showSetting(findFacturaVenta);
//                invokeFE.setVisible(true);
//                System.exit(0);
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        JFP jFP = new JFP();
                        new Thread(jFP, "checkDBConnection").start();
                        jFP.setVisible(true);
                    }
                });
            }
        } catch (FileNotFoundException ex) {
            log.fatal("No se encontro el archivo cfg.ini", ex);
            JOptionPane.showMessageDialog(null, "No se encontró el archivo de configuración cfg.ini\n"+ ex.getLocalizedMessage(), "Error", 0);
        } catch (IOException ex) {
            log.fatal("IOExpection con archivo cfg.ini", ex);
            java.util.logging.Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error intentando acceder al archivo de configuración cfg.ini\n"+ ex.getLocalizedMessage(), "Error", 0);
        } catch (MessageException ex) {
            log.fatal("MessageException ", ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", 2);
        } catch (DatabaseException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error de conexión con la base de datos.\nMsg:" + ex.getMessage(),
                    "DatabaseException", 0);
            log.fatal("DataBase Error!!", ex);
            OCURRIO_ERROR = true;
        } catch (PersistenceException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error -> MAIN -> PersistenceException", 0);
            log.fatal("PersistenceException", ex);
            OCURRIO_ERROR = true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "ERROR CRÍTICO!\n" + ex.getMessage(), "EN MAIN", 0);
            log.fatal("Error fatal..!", ex);
            OCURRIO_ERROR = true;
        } finally {
            if (OCURRIO_ERROR) {
                System.out.println("Finalizando por las malas!!!");
                System.exit(1);
            }
        }
        // Si por ejemplo se produciera una excepción de este tipo
        // se ejecutará el thread antes de que la máquina virtual finalice:
        // throw new RuntimeException();
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

    private Main() {
    }
}
