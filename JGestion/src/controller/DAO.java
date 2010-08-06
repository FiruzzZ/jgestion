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
            permisos.setCerrarCajas(true);
            em.persist(permisos);
            permisos.setUsuario(u);
            u.setPermisos(permisos);
            em.persist(u);
         }// </editor-fold>

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

         // <editor-fold defaultstate="collapsed" desc="Creanción Provincias, Departamentos (de Mnes.) y Municipios (de Mnes.)">
         if (em.createQuery("SELECT COUNT(o) FROM Provincia o").getSingleResult().toString().equalsIgnoreCase("0")) {
            System.out.println("Creando Provincias, Departamentos, Municipios");
            DAO.getJDBCConnection().createStatement().execute(
             " INSERT INTO provincia (idprovincia,nombre) VALUES "
               +"(1,'Buenos Aires'), (2,'Catamarca'), (3,'Chaco'), (4,'Chubut'),"
               +"(5,'Corrientes'), (6,'Córdoba'), (7,'Entre Ríos'), (8,'Formosa'),"
               +"(9,'Jujuy'), (10,'La Pampa'), (11,'La Rioja'), (12,'Mendoza'),"
               +"(13,'Misiones'),(14,'Neuquén'),(15,'Río Negro'),(16,'Salta'),"
               +"(17,'San Juan'),(18,'San Luis'),(19,'Santa Cruz'),(20,'Santa Fe'),"
               +"(21,'Sgo del Estero'),(22,'T. del Fuego'),(23,'Tucumán');"
            +" INSERT INTO depto (iddepto, idprovincia, nombre) VALUES "
               +"(1,5,'Corrientes'),(2,5,'Concepción'),(3,5,'Santo Tomé'),"
               +"(4,13,'CAPITAL'), (5,13,'Concepción'), (6,13,'Eldorado'),"
               +"(7,13,'General Manuel Belgrano'),(8,13,'Guaraní'),(9,13,'Iguazú'),"
               +"(10,13,'Leandro N. Alem'),(11,13,'Libertador General San Martín'),"
               +"(12,13,'Montecarlo'),(13,13,'OBERÁ'),(14,13,'San Ignacio'),"
               +"(15,13,'San Javier'),(16,13,'San Pedro'),(17,13,'Veinticinco de Mayo'),"
               +"(18,13,'APÓSTOLES'),(19,13,'Cainguás'),(20,13,'Candelaria');"
            +" INSERT INTO municipio (iddepto, nombre) VALUES "
+"(4,'GARUPÁ'),(4,'POSADAS'),(4,'FACHINAL'),(5,'CONCEPCIÓN DE LA SIERRA'),(5,'SANTA MARIA'),(6,'COLONIA DELICIA'),(6,'9 DE JULIO'),"
+"(6,'EL DORADO'),(6,'COLONIA VICTORIA'),(7,'BERNARDO DE IRIGOYEN'),(7,'CMDTE ANDRESITO'),(7,'SAN ANTONIO'),(8,'SAN VICENTE'),"
+"(8,'EL SOBERBIO'),(9,'WANDA'),(9,'PUERTO LIBERTAD'),(9,'PUERTO ESPERANZA'),(9,'PUERTO IGUAZU'),(10,'ARROYO DEL MEDIO'),"
+"(10,'L.N ALEM'),(10,'DOS ARROYOS'),(10,'CAÁ-YARÍ'),(10,'OLEGARIO V. ANDRADE'),(10,'CERRO AZUL'),(10,'ALMAFUERTE'),"
+"(11,'PUERTO LEONI'),(11,'CAPIOVI'),(11,'PUERTO RICO'),(11,'RUIZ DE MONTOYA'),(12,'CARAGUATAY'),(13,'SAN MARTIN '),"
+"(13,'CAMPO VIERA'),(13,'COLONIA ALBERDI'),(13,'GRAL ALVEAR'),(13,'PANAMBI'),(13,'CAMPO RAMON'),(13,'GUARANI'),"
+"(14,'GRAL URQUIZA'),(14,'SANTO PIPO'),(14,'COLONIA POLANA'),(14,'SAN IGNACIO'),(14,'CORPUS'),(14,'JARDIN AMERICA'),"
+"(14,'HIPOLITO YRIGOYEN'),(15,'MOJON GRANDE'),(15,'SAN JAVIER'),(15,'Florentino Ameghino'),(16,'SAN PEDRO'),(17,'ALBA POSSE'),"
+"(17,'COLONIA AURORA'),(17,'25 DE MAYO'),(18,'AZARA'),(18,'APÓSTOLES'),(18,'SAN JOSE'),(18,'TRES CAPONES'),(19,'DOS DE MAYO'),"
+"(19,'CAMPO GRANDE'),(20,'MARTIRES'),(20,'BOMPLAN'),(20,'CERRO CORA'),(20,'CANDELARIA'),(20,'LORETO'),(20,'PROFUNDIDAD'),"
+"(20,'SANTA ANA');");
//            DAO.getEntityManager().getTransaction().commit();
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
      if(em != null && em.isOpen()) {
         System.out.print("old..");
         return em;
      } else {
         System.out.print("new..");
         return emf.createEntityManager();
      }
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
