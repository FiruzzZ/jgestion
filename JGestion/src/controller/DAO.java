package controller;

import entity.Contribuyente;
import entity.DatosEmpresa;
import entity.Iva;
import entity.Permisos;
import entity.PermisosCaja;
import entity.Unidadmedida;
import entity.Usuario;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;

/**
 *
 * @author FiruzzZ
 */
public abstract class DAO {

   private static EntityManager em;
   private static EntityManagerFactory emf;

   static {
      try {
         emf = Persistence.createEntityManagerFactory("JGestionPU");
      } catch (Exception e) {
         System.out.println("EN DAO!!!");
         e.printStackTrace();
      }
   }

   /**
    * Crea todos los datos que el sistema necesita inicialmente:
    * <br>*Contribuyentes
    * <br>*Usuario: admin pws: adminadmin (permisos full)
    * <br>*ya vemos que mas..
    */
   public static void setDefaultData() throws Exception {
      System.out.println("--SETTING DEFAULT DATA--");

      try {
         em = getEntityManager();
         System.out.println("isActive:" + em.getTransaction().isActive());
         em.getTransaction().begin();

         // <editor-fold defaultstate="collapsed" desc="Creación de Contribuyente">
         if (em.createQuery("SELECT count(o) FROM Contribuyente o").getSingleResult().toString().equalsIgnoreCase("0")) {
            System.out.println("CREANDO Contribuyentes..");
            //FACTURAS TIPO     A     B     C     M      X
            em.persist(new Contribuyente(1, "CONSUMIDOR FINAL", false, true, false, false, false));
            em.persist(new Contribuyente(2, "EXENTO", false, true, false, false, false));
            em.persist(new Contribuyente(3, "MONOTRIBUTISTA", false, true, false, false, false));
            em.persist(new Contribuyente(4, "RESP. INSCRIP", true, false, false, false, false));
            em.persist(new Contribuyente(5, "RESP. NO INSCRIP", false, true, false, false, false));
         }
         // </editor-fold>

         // <editor-fold defaultstate="collapsed" desc="Creación de Usuario: admin Pws: adminadmin">
         if (em.createQuery("SELECT count(o) FROM Usuario o").getSingleResult().toString().equalsIgnoreCase("0")) {
            System.out.println("CREANDO Usuario admin..");
            Usuario u = new Usuario();
            u.setId(1);
            u.setEstado(1);
            u.setFechaalta(new java.util.Date());
            u.setNick("admin");
            u.setPass("adminadmin");
            u.setPermisosCajaList(new ArrayList<PermisosCaja>());
            Permisos permisos = new Permisos();
            permisos.setAbmCajas(true);
            permisos.setAbmClientes(true);
            permisos.setAbmListaPrecios(true);
            permisos.setAbmProductos(true);
            permisos.setAbmProveedores(true);
            permisos.setAbmUsuarios(true);
            permisos.setCompra(true);
            permisos.setVenta(true);
            permisos.setDatosGeneral(true);
            permisos.setTesoreria(true);
            em.persist(permisos);
            permisos.setUsuario(u);
            u.setPermisos(permisos);
            em.persist(u);
         }// </editor-fold>

         // <editor-fold defaultstate="collapsed" desc="Creación de Iva's">
         if (em.createQuery("SELECT COUNT(o) FROM Iva o ").getSingleResult().toString().equalsIgnoreCase("0")) {
            System.out.println("CREANDO Iva..");
            em.persist(new Iva(1, 21.0));
            em.persist(new Iva(2, 10.5));
//            em.persist(new Iva(3, 0.0));
         }// </editor-fold>

         // <editor-fold defaultstate="collapsed" desc="Creación DatosEmpresa">
         if (em.createQuery("SELECT COUNT(o) FROM DatosEmpresa o").getSingleResult().toString().equalsIgnoreCase("0")) {
            System.out.println("Creando DatosEmpresa..");
            DatosEmpresa d = new DatosEmpresa(1, "JGestion", 30000000001l, "Dirección", 540000000, new java.util.Date());
            d.setLogo(null);
            em.persist(d);
         }// </editor-fold>

         // <editor-fold defaultstate="collapsed" desc="Creación Unidadmedida -> UNITARIO">
         if (em.createQuery("SELECT COUNT(o) FROM Unidadmedida o").getSingleResult().toString().equalsIgnoreCase("0")) {
            System.out.println("Creando Unidadmedida..");
            em.persist(new Unidadmedida(1, "UNITARIO"));
         }// </editor-fold>

         em.getTransaction().commit();
      } catch (Exception ex) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
         throw ex;
      } finally {
         if (em != null) {
            em.close();
         }
      }
      System.out.println("finished setDefaultData()..");
   }

   public static EntityManager getEntityManager() {
      return emf.createEntityManager();
   }

   public static void closeEntityManagerFactory() {
      if (emf.isOpen()) {
         emf.close();
         System.out.println("EntityManagerFactory cerradO..");
      } else {
         System.out.println("EntityManagerFactory ya estaba CERRADO");
      }
   }

   public static java.sql.Connection getJDBCConnection() {
      em = emf.createEntityManager();
      em.getTransaction().begin();
      UnitOfWorkImpl unitOfWorkImpl = (UnitOfWorkImpl) ((EntityManagerImpl) em.getDelegate()).getActiveSession();
      unitOfWorkImpl.beginEarlyTransaction();
      return unitOfWorkImpl.getAccessor().getConnection();
   }

   static void closeEntityManager() {
      if (em.isOpen()) {
         em.close();
         System.out.println("EntityManager cerrado..");
      } else {
         System.out.println("EntityManager ya eataba cerrado..");
      }
   }

   static void create(Object o) throws Exception {
      em = getEntityManager();
      try {
         em.getTransaction().begin();
         em.persist(o);
         em.getTransaction().commit();
      } catch (Exception e) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         throw e;
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   static void update(Object o) {
      em = getEntityManager();
      try {
         em.getTransaction().begin();
         em.refresh(o);
         em.getTransaction().commit();
      } catch (Exception e) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         e.printStackTrace();
      } finally {
         em.close();
      }
   }

   static void remove(Object o) {
      em = getEntityManager();
      try {
         em.getTransaction().begin();
         em.remove(o);
         em.getTransaction().commit();
      } catch (Exception e) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         e.printStackTrace();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }

   static void doMerge(Object o) {
      em = getEntityManager();
      try {
         em.getTransaction().begin();
         em.merge(o);
         em.getTransaction().commit();
      } catch (Exception e) {
         if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
         }
         e.printStackTrace();
      } finally {
         if (em != null) {
            em.close();
         }
      }
   }
}
