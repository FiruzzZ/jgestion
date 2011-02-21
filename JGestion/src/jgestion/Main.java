package jgestion;

import controller.*;
import controller.exceptions.MessageException;
import entity.*;
import gui.JDSystemMessages;
import gui.JFP;
import java.awt.EventQueue;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.swing.JOptionPane;
import oracle.toplink.essentials.exceptions.DatabaseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Administrador
 */
public class Main {

   private static final Logger log = Logger.getLogger(Main.class);
   private static boolean OCURRIO_ERROR = false;

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      PropertyConfigurator.configure("log4j.properties");
      threadSafe();
      try {
         if (DAO.getEntityManager().isOpen()) {
            DAO.setDefaultData();
            EventQueue.invokeLater(new Runnable() {

               @Override
               public void run() {
                  new JFP().setVisible(true);
               }
            });
         }
      } catch (MessageException ex) {
         log.fatal("MessageException ", ex);
         JOptionPane.showMessageDialog(null, ex.getMessage(), "MessageException on PSVM", 2);
      } catch (DatabaseException ex) {
         JOptionPane.showMessageDialog(null,
                 "No se pudo conectar con la base de datos.\nVerifique el estado del servidor.\nMsg:" + ex.getMessage(),
                 "DatabaseException", 0);
         log.fatal("DataBase Error!!", ex);
         OCURRIO_ERROR = true;
      } catch (PersistenceException ex) {
         JOptionPane.showMessageDialog(null, "PersistenceException..!", "EN MAIN", 0);
         log.fatal("PersistenceException", ex);
         OCURRIO_ERROR = true;
      } catch (Exception ex) {
         JOptionPane.showMessageDialog(null, "ERROR CRÍTICO!\n" + ex.getMessage(), "EN MAIN", 0);
         log.fatal("Error fatal..!", ex);
         OCURRIO_ERROR = true;
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
}
