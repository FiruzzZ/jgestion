package jgestion;

import controller.*;
import controller.exceptions.MessageException;
import gui.JFP;
import java.awt.EventQueue;
import java.awt.SplashScreen;
import java.util.ResourceBundle;
import javax.persistence.PersistenceException;
import javax.swing.JOptionPane;
//import oracle.toplink.essentials.exceptions.DatabaseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.persistence.exceptions.DatabaseException;

/**
 *
 * @author Administrador
 */
public class Main {

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
        } catch (MessageException ex) {
            log.fatal("MessageException ", ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "MessageException on PSVM", 2);
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
