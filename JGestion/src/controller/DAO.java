package controller;

import controller.exceptions.DatabaseErrorException;
import entity.Contribuyente;
import entity.DatosEmpresa;
import entity.Iva;
import entity.MovimientoConcepto;
import entity.Permisos;
import entity.PermisosCaja;
import entity.Unidadmedida;
import entity.Usuario;
import gui.JDSystemMessages;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.exceptions.DatabaseException;

/**
 *
 * @author FiruzzZ
 */
public abstract class DAO implements Runnable {

    private static EntityManagerFactory emf;
    /**
     * Este EntityManager, es una Transaction iniciada. <code>em.getTransaction().begin())</code>
     * NO SE COMMITEA NUNCA!
     * Es usado exclusivamente para conexiones JDBC, no se
     * limpia (.clear()) ni se cierra (.close())
     */
    private static EntityManager entityManagerForJDBC;
    private static Connection connection;
    private static int instanceOfJDBCCreated = 0;

    private DAO() {
        //singleton..
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //singleton..
        throw new CloneNotSupportedException("This Object can not be cloned, because it's a Singleton Design Class!!! [666]");
    }

    public static EntityManager getEntityManager() {
        if (emf == null) {
            Logger.getLogger(DAO.class).debug("EntityManagerFactory == null");
//            emf = Persistence.createEntityManagerFactory("JGestionPU");
//            emf = Persistence.createEntityManagerFactory("JGestionTest");
//         emf = Persistence.createEntityManagerFactory("JGestionPUWorldPrint");
         emf = Persistence.createEntityManagerFactory("JGestionPUProgreso");
            getJDBCConnection();
        }
        EntityManager em = emf.createEntityManager();
        return em;
    }

    static EntityManagerFactory getEntityManagerFactory(String string) {
        return Persistence.createEntityManagerFactory(string);
    }

    public static void closeAllConnections() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class).error(ex.getMessage(), ex);
        }
        if (entityManagerForJDBC != null && entityManagerForJDBC.isOpen()) {
            Logger.getLogger(DAO.class).trace("Closing EntityManager for JDBC conn..");
            entityManagerForJDBC.close();

        }
        if (emf != null && emf.isOpen()) {
            Logger.getLogger(DAO.class).trace("Closing EntityManagerFactory..");
            emf.close();
        }
    }

    /**
     * Devuelve un {@link java.sql.Connection}
     * Este método leave a EntityManager.getTransaction.begin() opened!
     * Which must be closed with {@code closeEntityManager()} manually when the returned Connection oebject will no longer be used
     * @return
     */
    public static Connection getJDBCConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                instanceOfJDBCCreated++;
                Logger.getLogger(DAO.class).log(Level.TRACE, "Creating a new JDBC #" + instanceOfJDBCCreated);
                entityManagerForJDBC = emf.createEntityManager();
                entityManagerForJDBC.getTransaction().begin();

                //JPA 1.0
//                UnitOfWorkImpl unitOfWorkImpl = (UnitOfWorkImpl) ((EntityManagerImpl) entityManagerForJDBC.getDelegate()).getActiveSession();
//                unitOfWorkImpl.beginEarlyTransaction();
//                connection = unitOfWorkImpl.getAccessor().getConnection();

                //JPA 2.0
                connection = entityManagerForJDBC.unwrap(java.sql.Connection.class);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class).log(Level.ERROR, ex.getMessage(), ex);
        }
        return connection;
    }

    static void create(Object o) throws Exception {
        EntityManager em = null;
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

    /**
     * Como no se puede borrar una detached entity,
     * @param classType
     * @param id can't be nul!!
     */
    static void remove(Class classType, Integer id) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("id can not be NULL!!");
        }
        EntityManager em = null;
        em = getEntityManager();
        try {
            em.getTransaction().begin();
            Object o = em.find(classType, id);
            em.remove(o);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Merge the state of the given entity into the current persistence context.
     * If any exception occurs, a rollback action on the current transaction is
     * launched.
     * @param o entity instance
     * @return the managed instance that the state was merged to or
     * <code>null</code> if a exception occurs
     */
    static <T extends Object> T doMerge(T o) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T merge = em.merge(o);
            em.getTransaction().commit();
            return merge;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            Logger.getLogger(DAO.class).log(Level.ERROR, e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return null;
    }

    static Object getNativeQuerySingleResult(String sqlString, Class<?> resultClass) throws DatabaseErrorException {
        EntityManager em = getEntityManager();
        try {
            return em.createNativeQuery(sqlString, resultClass).getSingleResult();
        } catch (DatabaseException e) {
            Logger.getLogger(DAO.class).log(Level.FATAL, e);
            throw new DatabaseErrorException();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene una collection de objetos
     * @param sqlString a native SQL statement.
     * @param resultClass the class of the returning List. If is NULL, will be a untyped List.
     * @return a list...
     * @throws DatabaseErrorException
     */
    static List<?> getNativeQueryResultList(String sqlString, Class<?> resultClass) throws DatabaseErrorException {
        EntityManager em = getEntityManager();
        try {
            List l;
            if (resultClass != null) {
                l = em.createNativeQuery(sqlString, resultClass).getResultList();
            } else {
                l = em.createNativeQuery(sqlString).getResultList();
            }
            return l;
        } catch (DatabaseException e) {
            throw new DatabaseErrorException();
        } finally {
            em.close();
        }
    }

    static List<?> getNativeQueryResultList(String sqlString, String resultSetMapping) throws DatabaseErrorException {
        EntityManager em = getEntityManager();
        try {
            return em.createNativeQuery(sqlString, resultSetMapping).setHint(QueryHints.REFRESH, true).getResultList();
        } catch (DatabaseException e) {
            throw new DatabaseErrorException(e);
        } finally {
            em.close();
        }
    }

    /**
     * Busca el estado mas actualizado del objeto en la base de datos.
     * El objeto debe tener un campo llamado id y ser único (UNIQUE CONSTRAINT)
     * (<code>object.id</code>)
     * @param object La clase de la cual se buscará la instancia
     * @param id valor único identificador
     * @return Una instancia del tipo <code>object</code>
     */
    public static Object findEntity(Class object, Integer id) {
        if (object == null) {
            throw new IllegalArgumentException("El parámetro object can not be NULL");
        }
        if (id == null) {
            throw new IllegalArgumentException("El parámetro id can not be NULL");
        }
        try {
            return getEntityManager().createQuery("SELECT o FROM " + object.getSimpleName() + " o WHERE o.id=" + id).setHint(QueryHints.REFRESH, true).getSingleResult();
        } catch (NoResultException ex) {
            return null;
        } catch (NonUniqueResultException ex) {
            throw new RuntimeException("More than one entity " + object.getSimpleName() + " was found!!, id is not a UNIQUE identifier field");
        }
    }

    /**
     * Retorna una collections de objetos
     * object is renamed to o (<code> SELECT o FROM object o</code>).
     * @param object Class type of the object to find and return.
     * @param conditions a String with filters to obtain the collections entities.
     * Example <code>conditions = "o.id > 777 AND o.aField != null"</code>, and this will be
     * contated to <code>"WHERE " + conditions</code>.
     * Must be null is there is no conditions.
     * @return a List of object
     */
    static List<?> findEntities(Class object, String conditions) {
        if (object == null) {
            throw new IllegalArgumentException("El parámetro object can not be NULL");
        }
        conditions = conditions == null ? "" : "WHERE " + conditions;
        return getEntityManager().createQuery("SELECT o FROM " + object.getSimpleName() + " o " + conditions).setHint(QueryHints.REFRESH, true).getResultList();
    }

    /**
     * Crea todos los datos que el sistema necesita inicialmente:
     * <br>*Contribuyentes
     * <br>*Usuario: admin pws: adminadmin (permisos full)
     * <br>*ya vemos que mas..
     */
    public static void setDefaultData() throws Exception {
        System.out.println("--SETTING DEFAULT DATA--");
        EntityManager em = null;
        JDSystemMessages ventanaSystemMessage = new JDSystemMessages(null, true);

        try {
            em = getEntityManager();
            em.getTransaction().begin();
//         new Thread(ventanaSystemMessage).start();
            // <editor-fold defaultstate="collapsed" desc="Creación de Usuario: admin Pws: adminadmin">
            if (em.createQuery("SELECT count(o) FROM Usuario o").getSingleResult().toString().equalsIgnoreCase("0")) {
                ventanaSystemMessage.agregar("Creando base de datos.. (Iniciando datos necesarios)");
                ventanaSystemMessage.agregar("Ceando usuario por defecto: admin contraseña: adminadmin");
                Usuario u = new Usuario();
                u.setId(1);
                u.setEstado(1);
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
                ventanaSystemMessage.agregar("Creando contribuyentes..");
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
                ventanaSystemMessage.agregar("Creando DatosEmpresa..");
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

            // <editor-fold defaultstate="collapsed" desc="MovimientoConcepto.EFECTIVO">
            if (em.createQuery("SELECT COUNT(o) FROM " + MovimientoConceptoJpaController.CLASS_NAME + " o").getSingleResult().toString().equalsIgnoreCase("0")) {
                System.out.println("Creando Unidadmedida..");
                MovimientoConcepto o = new MovimientoConcepto();
                o.setId(1);
                o.setNombre("EFECTIVO");
                em.persist(o);
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Creanción Provincias, Departamentos (de Mnes.) y Municipios (de Mnes.)">
            if (em.createQuery("SELECT COUNT(o) FROM Provincia o").getSingleResult().toString().equalsIgnoreCase("0")) {
                ventanaSystemMessage.agregar("Creando Provincias..");
                ventanaSystemMessage.agregar("Creando Departamentos.. (de Misiones)");
                ventanaSystemMessage.agregar("Creando Municipios..");
                System.out.println("Creando Provincias, Departamentos, Municipios");
                getJDBCConnection().createStatement().execute(
                        " INSERT INTO provincia (idprovincia, nombre) VALUES "
                        + "(1,'Buenos Aires'), (2,'Catamarca'), (3,'Chaco'), (4,'Chubut'),"
                        + "(5,'Corrientes'), (6,'Córdoba'), (7,'Entre Ríos'), (8,'Formosa'),"
                        + "(9,'Jujuy'), (10,'La Pampa'), (11,'La Rioja'), (12,'Mendoza'),"
                        + "(13,'Misiones'),(14,'Neuquén'),(15,'Río Negro'),(16,'Salta'),"
                        + "(17,'San Juan'),(18,'San Luis'),(19,'Santa Cruz'),(20,'Santa Fe'),"
                        + "(21,'Sgo del Estero'),(22,'T. del Fuego'),(23,'Tucumán');"
                        + " INSERT INTO depto (iddepto, idprovincia, nombre) VALUES "
                        + "(1,5,'Corrientes'),(2,5,'Concepción'),(3,5,'Santo Tomé'),"
                        + "(4,13,'Posadas'), (5,13,'Concepción'), (6,13,'Eldorado'),"
                        + "(7,13,'General Manuel Belgrano'),(8,13,'Guaraní'),(9,13,'Iguazú'),"
                        + "(10,13,'Leandro N. Alem'),(11,13,'Libertador General San Martín'),"
                        + "(12,13,'Montecarlo'),(13,13,'OBERÁ'),(14,13,'San Ignacio'),"
                        + "(15,13,'San Javier'),(16,13,'San Pedro'),(17,13,'Veinticinco de Mayo'),"
                        + "(18,13,'APÓSTOLES'),(19,13,'Cainguás'),(20,13,'Candelaria');"
                        + " INSERT INTO municipio (iddepto, nombre) VALUES "
                        + "(4,'GARUPÁ'),(4,'POSADAS'),(4,'FACHINAL'),(5,'CONCEPCIÓN DE LA SIERRA'),(5,'SANTA MARIA'),(6,'COLONIA DELICIA'),(6,'9 DE JULIO'),"
                        + "(6,'EL DORADO'),(6,'COLONIA VICTORIA'),(7,'BERNARDO DE IRIGOYEN'),(7,'CMDTE ANDRESITO'),(7,'SAN ANTONIO'),(8,'SAN VICENTE'),"
                        + "(8,'EL SOBERBIO'),(9,'WANDA'),(9,'PUERTO LIBERTAD'),(9,'PUERTO ESPERANZA'),(9,'PUERTO IGUAZU'),(10,'ARROYO DEL MEDIO'),"
                        + "(10,'L.N ALEM'),(10,'DOS ARROYOS'),(10,'CAÁ-YARÍ'),(10,'OLEGARIO V. ANDRADE'),(10,'CERRO AZUL'),(10,'ALMAFUERTE'),"
                        + "(11,'PUERTO LEONI'),(11,'CAPIOVI'),(11,'PUERTO RICO'),(11,'RUIZ DE MONTOYA'),(12,'CARAGUATAY'),(13,'SAN MARTIN '),"
                        + "(13,'CAMPO VIERA'),(13,'COLONIA ALBERDI'),(13,'GRAL ALVEAR'),(13,'PANAMBI'),(13,'CAMPO RAMON'),(13,'GUARANI'),"
                        + "(14,'GRAL URQUIZA'),(14,'SANTO PIPO'),(14,'COLONIA POLANA'),(14,'SAN IGNACIO'),(14,'CORPUS'),(14,'JARDIN AMERICA'),"
                        + "(14,'HIPOLITO YRIGOYEN'),(15,'MOJON GRANDE'),(15,'SAN JAVIER'),(15,'Florentino Ameghino'),(16,'SAN PEDRO'),(17,'ALBA POSSE'),"
                        + "(17,'COLONIA AURORA'),(17,'25 DE MAYO'),(18,'AZARA'),(18,'APÓSTOLES'),(18,'SAN JOSE'),(18,'TRES CAPONES'),(19,'DOS DE MAYO'),"
                        + "(19,'CAMPO GRANDE'),(20,'MARTIRES'),(20,'BOMPLAN'),(20,'CERRO CORA'),(20,'CANDELARIA'),(20,'LORETO'),(20,'PROFUNDIDAD'),"
                        + "(20,'SANTA ANA');");
                getJDBCConnection().commit();
                getJDBCConnection().close();
//            DAO.getEntityManager().getTransaction().commit();
            }// </editor-fold>

            new ChequesController();
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            Logger.getLogger(DAO.class.getName()).log(Level.ERROR, null, ex);
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        ventanaSystemMessage.agregar("Proceso de inicialización finalizado..");
        System.out.println("finished setDefaultData()..");
    }

    /**
     * Ver {@link EntityManager#createQuery(java.lang.String)}
     * @param query a Java Persistence query string
     * @param withRefreshQueryHint
     * @return 
     */
    static Query createQuery(String query, boolean withRefreshQueryHint) {
        EntityManager em = getEntityManager();
        Query q = em.createQuery(query);
        if (withRefreshQueryHint) {
            q.setHint(QueryHints.REFRESH, true);
        }
        return q;
    }
    
    static Query createNativeQuery(String query, Class aClass, boolean withRefreshQueryHint) {
        EntityManager em = getEntityManager();
        Query q = em.createNativeQuery(query, aClass);
        if (withRefreshQueryHint) {
            q.setHint(QueryHints.REFRESH, true);
        }
        return q;
    }
    
    public <T extends Object> List<T> get(T object, String condicion) {
        List<T> l = new ArrayList<T>();
        return l;
        
    }
}
