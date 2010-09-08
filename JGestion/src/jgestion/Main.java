package jgestion;

import controller.*;
import entity.*;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

/**
 *
 * @author Administrador
 */
public class Main {

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      threadSafe();
      try {
         if (DAO.getEntityManager().isOpen()) {
            DAO.setDefaultData();
            java.awt.EventQueue.invokeLater(new Runnable() {
               @Override
               public void run() {
                  new gui.JFP().setVisible(true);
               }
            });
         }
      } catch (Exception ex) {
         javax.swing.JOptionPane.showMessageDialog(null, "ERROR EN MAIN!!!!" + ex.getMessage());
         ex.printStackTrace();
      }
      // Si por ejemplo se produjese una excepción de este tipo
      // se ejecutará el thread antes de que la máquina virtual finalice:
//      throw new RuntimeException();
   }

   private static void threadSafe() {
      Thread shutdownThread = new Thread() {

         @Override
         public void run() {
            System.out.println("DAO: isOpen=" + DAO.getEntityManager().isOpen());
            System.out.println("threadSafe: Cerrando sistema..");
         }
      };
      Runtime.getRuntime().addShutdownHook(shutdownThread);
   }

}
