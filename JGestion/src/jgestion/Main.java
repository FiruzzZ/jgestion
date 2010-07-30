package jgestion;

import controller.*;
import entity.*;
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
//            EntityManager em = null;
//            try {
//               em = DAO.getEntityManager();
//               em.getTransaction().begin();
//               em.getTransaction().commit();
//               em.close();
//            }catch (Exception ex) {
//               em.getTransaction().rollback();
//               ex.printStackTrace();
//            }
            java.awt.EventQueue.invokeLater(new Runnable() {
               public void run() {
                  new gui.JFP().setVisible(true);
               }
            });
         }
      } catch (Exception ex) {
         javax.swing.JOptionPane.showMessageDialog(null, "MAIN ERROR:" + ex + "\nClass:" + ex.getClass() + "\nME: " + ex.getMessage());
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
