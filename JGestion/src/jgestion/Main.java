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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.swing.JOptionPane;
import oracle.toplink.essentials.exceptions.DatabaseException;

/**
 *
 * @author Administrador
 */
public class Main {

   private static boolean OCURRIO_ERROR = false;

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
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
         System.out.println("MessageException agarrado en PSVM");
         JOptionPane.showMessageDialog(null, ex.getMessage(), "MessageException on PSVM", 2);
      } catch (DatabaseException ex) {
         JOptionPane.showMessageDialog(null,
                 "No se pudo conectar con la base de datos.\nVerifique el estado del servidor.\nMsg:" + ex.getMessage(),
                 "DatabaseException", 0);
         OCURRIO_ERROR = true;
      } catch (Exception ex) {
         JOptionPane.showMessageDialog(null, "ERROR CRÍTICO!\n" + ex.getMessage(), "EN MAIN", 0);
         OCURRIO_ERROR = true;
         ex.printStackTrace();
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
