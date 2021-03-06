package jgestion.controller;

import jgestion.entity.Cuenta;
import jgestion.entity.DatosEmpresa;
import jgestion.entity.Contribuyente;
import jgestion.entity.Usuario;
import jgestion.entity.Unidadmedida;
import jgestion.entity.Iva;
import jgestion.entity.Permisos;
import jgestion.entity.Banco;
import jgestion.controller.exceptions.DatabaseErrorException;
import jgestion.entity.enums.ChequeEstado;
import jgestion.gui.JDSystemMessages;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.persistence.*;
import jgestion.entity.Configuracion;
import jgestion.entity.Sistema;
import jgestion.entity.TipoDocumento;
import jgestion.jpa.controller.JGestionJpaImpl;
import jgestion.jpa.controller.TipoDocumentoJpaController;
import org.apache.logging.log4j.LogManager;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.exceptions.DatabaseException;

/**
 *
 * @author FiruzzZ
 */
public abstract class DAO implements Runnable {

    private static EntityManagerFactory emf;
    /**
     * Este EntityManager, es una Transaction iniciada. <code>em.getTransaction().begin())</code> NO
     * SE COMMITEA NUNCA! Es usado exclusivamente para conexiones JDBC, no se limpia (.clear()) ni
     * se cierra (.close())
     */
    private static EntityManager entityManagerForJDBC;
    private static Connection connection;
    private static int instanceOfJDBCCreated = 0;
    private static Properties properties = null;

    private DAO() {
        //singleton..
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //singleton..
        throw new CloneNotSupportedException("This Object can not be cloned, because it's a Singleton Design Class!!! [666]");
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public static void setProperties(final Properties p) {
        if (p == null) {
            throw new IllegalArgumentException("Archivo de configuración de conexión no válido.\nNull Properties");
        }
        if (p.isEmpty()) {
            throw new IllegalArgumentException("Archivo de configuración de conexión no válido.\nEmpty Properties");
        }
        if (p.getProperty("database") == null
                || p.getProperty("port") == null
                || p.getProperty("server") == null) {
            throw new IllegalArgumentException("Archivo de configuración de conexión no válido");
        }
        properties = p;
    }

    public static EntityManager getEntityManager() {
        if (emf == null) {
            String persistenceUnitName = "JGestionPU";
            LogManager.getLogger();//(DAO.class).trace("Initializing EntityManagerFactory= " + persistenceUnitName);
            String server, port, database;
            if (properties != null) {
                server = properties.getProperty("server");
                port = properties.getProperty("port");
                database = properties.getProperty("database");
                if (properties.getProperty("create-tables", "false").equals("true")) {
                    properties.setProperty("eclipselink.ddl-generation", "create-tables");
                }
                properties.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://" + server + ":" + port + "/" + database);
            }
            emf = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
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
            LogManager.getLogger();//(DAO.class).error(ex.getMessage(), ex);
        }
        if (entityManagerForJDBC != null && entityManagerForJDBC.isOpen()) {
            LogManager.getLogger();//(DAO.class).trace("Closing EntityManager for JDBC conn..");
            entityManagerForJDBC.close();

        }
        if (emf != null && emf.isOpen()) {
            LogManager.getLogger();//(DAO.class).trace("Closing EntityManagerFactory..");
            emf.close();
        }
    }

    /**
     * Devuelve un {@link java.sql.Connection} Este método leave a
     * EntityManager.getTransaction.begin() opened! Which must be closed with
     * {@code closeEntityManager()} manually when the returned Connection oebject will no longer be
     * used
     *
     * @return
     */
    public static Connection getJDBCConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
        } catch (SQLException ex) {
            LogManager.getLogger();//(DAO.class).log(Level.ERROR, ex.getMessage(), ex);
        }
        instanceOfJDBCCreated++;
        LogManager.getLogger();//(DAO.class).log(Level.TRACE, "Creating a new JDBC #" + instanceOfJDBCCreated);
        entityManagerForJDBC = emf.createEntityManager();
        entityManagerForJDBC.getTransaction().begin();

        //JPA 1.0
//                UnitOfWorkImpl unitOfWorkImpl = (UnitOfWorkImpl) ((EntityManagerImpl) entityManagerForJDBC.getDelegate()).getActiveSession();
//                unitOfWorkImpl.beginEarlyTransaction();
//                connection = unitOfWorkImpl.getAccessor().getConnection();
        //JPA 2.0
        connection = entityManagerForJDBC.unwrap(java.sql.Connection.class);
        return connection;
    }

    static void create(Object o) throws Exception {
        EntityManager em = getEntityManager();
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
     *
     * @param classType
     * @param id can't be nul!!
     */
    static void remove(Class<?> classType, Integer id) throws Exception {
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
     * Merge the state of the given entity into the current persistence context. If any exception
     * occurs, a rollback action on the current transaction is launched.
     *
     * @param o entity instance
     * @return the managed instance that the state was merged to or <code>null</code> if a exception
     * occurs
     */
    static <T extends Object> T merge(T o) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            T merge = em.merge(o);
            em.getTransaction().commit();
            return merge;
        } catch (Exception ex) {
            LogManager.getLogger();//(DAO.class).log(Level.ERROR, o.toString() + " > " + ex.getLocalizedMessage(), ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
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
            LogManager.getLogger();//(DAO.class).log(Level.FATAL, e);
            throw new DatabaseErrorException();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene una collection de objetos
     *
     * @param sqlString a native SQL statement.
     * @param resultClass the class of the returning List. If is NULL, will be a untyped List.
     * @return a list...
     * @throws DatabaseErrorException
     */
    static List<?> getNativeQueryResultList(String sqlString, Class<?> resultClass) throws DatabaseErrorException {
        EntityManager em = getEntityManager();
        try {
            List<?> l;
            if (resultClass != null) {
                l = em.createNativeQuery(sqlString, resultClass).setHint(QueryHints.REFRESH, true).getResultList();
            } else {
                l = getNativeQueryResultList(sqlString);
            }
            return l;
        } catch (DatabaseException e) {
            throw new DatabaseErrorException();
        } finally {
            em.close();
        }
    }

    static List<?> getNativeQueryResultList(String sqlString) throws DatabaseErrorException {
        EntityManager em = getEntityManager();
        try {
            return em.createNativeQuery(sqlString).getResultList();
        } catch (DatabaseException e) {
            throw new DatabaseErrorException(e);
        } finally {
            em.close();
        }
    }

    static List<?> getNativeQueryResultList(String sqlString, String resultSetMapping) throws DatabaseErrorException {
        EntityManager em = getEntityManager();
        try {
            if (resultSetMapping != null) {
                return em.createNativeQuery(sqlString, resultSetMapping).setHint(QueryHints.REFRESH, true).getResultList();
            } else {
                return getNativeQueryResultList(sqlString);
            }
        } catch (DatabaseException e) {
            throw new DatabaseErrorException(e);
        } finally {
            em.close();
        }
    }

    /**
     * Find by primary key. Search for an entity of the specified class and primary key.
     * <br>The entity instance is <b>always retrieved from the database<b>
     * ensured by the use of {@link QueryHints#REFRESH}.
     * <br>El objeto debe tener un campo llamado id y ser único (PRIMARY KEY or UNIQUE CONSTRAINT) (
     * <code>object.id</code>)
     *
     * @param object
     * @param id
     * @return the found entity instance or null if the entity does not exist
     */
    public static Object findEntity(Class<?> object, Integer id) {
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
     * Retorna una collections de objetos object is renamed to o (
     * <code> SELECT o FROM object o</code>).
     *
     * @param object Class type of the object to find and return.
     * @param conditions a String with filters to obtain the collections entities. Example
     * <code>conditions = "o.id > 777 AND o.aField != null"</code>, and this will be contated to
     * <code>"WHERE " + conditions</code>. Must be null is there is no conditions.
     * @return a List of object
     */
    static List<?> findEntities(Class<?> object, String conditions) {
        if (object == null) {
            throw new IllegalArgumentException("El parámetro object can not be NULL");
        }
        conditions = conditions == null ? "" : "WHERE " + conditions;
        return getEntityManager().createQuery("SELECT o FROM " + object.getSimpleName() + " o " + conditions).setHint(QueryHints.REFRESH, true).getResultList();
    }

    /**
     * Crea todos los datos que el sistema necesita inicialmente:
     * <br>*Contribuyentes <br>*Usuario: admin pws: adminadmin (permisos full)
     * <br>*ya vemos que mas..
     *
     * @throws Exception
     */
    public static void setDefaultData() throws Exception {
        if (properties.getProperty("populate", "false").equals("false")) {
            return;
        }
        LogManager.getLogger();
        EntityManager em = null;
        JDSystemMessages ventanaSystemMessage = new JDSystemMessages(null, true);
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            //<editor-fold defaultstate="collapsed" desc="Creación tablas (que no son entities!): cheque_estado">
            if (em.createNativeQuery("SELECT to_regclass('cheque_estado')").getSingleResult() == null) {
                LogManager.getLogger().trace("creando tabla: cheque_estado");
                em.createNativeQuery("CREATE TABLE cheque_estado ("
                        + "  id integer NOT NULL,"
                        + "  nombre character varying(20) NOT NULL,"
                        + "  CONSTRAINT cheque_estado_pkey PRIMARY KEY (id),"
                        + "  CONSTRAINT cheque_estado_nombre_key UNIQUE (nombre)"
                        + ")").executeUpdate();
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Creación de Estados de Cheques">
            if (em.createNativeQuery("SELECT * FROM cheque_estado").getResultList().isEmpty()) {
                LogManager.getLogger().trace("Creando cheque_estado's..");
                StringBuilder sb = null;
                //fill with Enum's
                for (ChequeEstado chequeEstado : ChequeEstado.values()) {
                    sb = new StringBuilder("INSERT INTO cheque_estado VALUES(");
                    sb.append(chequeEstado.getId()).append(",'").append(chequeEstado.toString()).append("');");
                }
                em.createNativeQuery(sb.toString()).executeUpdate();
            }
            //</editor-fold>
            JGestionJpaImpl<Sistema, Integer> sistemaDao = new JGestionJpaImpl<Sistema, Integer>() {
            };
            if (sistemaDao.findAll().isEmpty()) {
                Sistema o = new Sistema(1, false, "Sistema en mantenimiento");
                sistemaDao.persist(o);
            }
            if (em.createQuery("SELECT count(o) FROM Configuracion o").getSingleResult().toString().equalsIgnoreCase("0")) {
                em.persist(new Configuracion("afip_fe_pkcs12", "sin_definir", "Clave PK12"));
                em.persist(new Configuracion("afip_fe_ticket", "", "AFIP WS (XML TicketAccess)"));
                em.persist(new Configuracion("afip_ws_production", "false", "AFIP WS Production enviroment true/false"));
                em.persist(new Configuracion("cantidad_decimales", "2", "Cantidad de decimales para todo los cálculos"));
                em.persist(new Configuracion("permitir_stock_negativo", "false", "Permite realizar ventas y que el stock resultante sea menor a cero"));
            }
            // <editor-fold defaultstate="collapsed" desc="Creación de Usuario: admin Pws: adminadmin">
            if (em.createQuery("SELECT count(o) FROM Usuario o").getSingleResult().toString().equalsIgnoreCase("0")) {
                LogManager.getLogger().trace("Creando usuario por defecto: admin contraseña: adminadmin");
                Usuario u = new Usuario();
                u.setId(1);
                u.setActivo(true);
                u.setNick("admin");
                u.setPass("adminadmin");
                u.setPermisosCajaList(new ArrayList<>());
                Permisos permisos = new Permisos();
                permisos.setAbmUsuarios(true);
                em.persist(permisos);
                permisos.setUsuario(u);
                u.setPermisos(permisos);
                em.persist(u);
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Creación de Contribuyente">
            if (em.createQuery("SELECT count(o) FROM Contribuyente o").getSingleResult().toString().equalsIgnoreCase("0")) {
                ventanaSystemMessage.agregar("Creando contribuyentes..");
                LogManager.getLogger().trace("CREANDO Contribuyentes..");
                //FACTURAS TIPO     A     B     C     M      X
                em.persist(new Contribuyente(1, "CONSUMIDOR FINAL", false, true, false, false, false));
                em.persist(new Contribuyente(2, "EXENTO", false, true, false, false, false));
                em.persist(new Contribuyente(3, "MONOTRIBUTISTA", false, false, true, false, false));
                em.persist(new Contribuyente(4, "RESP. INSCRIP", true, true, false, false, false));
                em.persist(new Contribuyente(5, "RESP. NO INSCRIP", false, true, false, false, false));
            }
            // </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Creación de Iva's">
            if (em.createQuery("SELECT COUNT(o) FROM Iva o ").getSingleResult().toString().equalsIgnoreCase("0")) {
                ventanaSystemMessage.agregar("Creando IVA's..");
                LogManager.getLogger().trace("CREANDO IVA's..");
                em.persist(new Iva(1, 21.0f));
                em.persist(new Iva(2, 10.5f));
                em.persist(new Iva(3, 0f));
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Creación Unidadmedida -> UNITARIO">
            if (em.createQuery("SELECT COUNT(o) FROM Unidadmedida o").getSingleResult().toString().equalsIgnoreCase("0")) {
                LogManager.getLogger().trace("Creando Unidadmedida..");
                em.persist(new Unidadmedida(1, "UNITARIO"));
            }// </editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Creación de Bancos">
            if (em.createQuery("SELECT COUNT(o) FROM " + Banco.class.getSimpleName() + " o ").getSingleResult().toString().equalsIgnoreCase("0")) {
                LogManager.getLogger().trace("Creando Bancos..");
                em.createNativeQuery("insert into banco (nombre, webpage) values "
                        + "('ABN AMRO','www.abnamro.com.ar'),('American Express Bank','www.americanexpress.com.ar'),('BACS','www.bacs.com.ar'),('Banco B.I. Creditanstalt','www.bicreditanstalt.com.ar'),('Banco Bradesco','www.bradesco.com.br'),"
                        + "('Banco Cetelem','www.cetelem.com.ar'),('Banco Ciudad','www.bancociudad.com.ar'),('Banco CMF','www.cmfb.com.ar'),('Banco Cofidis','www.cofidis.com'),('Banco Columbia','www.bancocolumbia.com.ar'),"
                        + "('Banco Comafi','www.bancocomafi.com.ar'),('Banco Credicoop','www.bancocredicoop.coop'),('Banco de Córdoba','www.bancor.com.ar'),('Banco de Corrientes','www.bancodecorrientes.com.ar'),('Banco de Formosa','www.bancodeformosa.com'),"
                        + "('Banco de La Pampa','www.blp.com.ar'),('Banco de San Juan','www.bancosanjuan.com'),('Banco de Santiago del Estero','www.bse.com.ar'),('Banco de Servicios Financieros', null),('Banco de Servicios y Transacciones','www.bancost.com.ar'),"
                        + "('Banco de Tierra del Fuego','www.bancotdf.com.ar'),('Banco de Valores','www.bancodevalores.com'),('Banco del Chubut','www.bancochubut.com.ar'),('Banco del Sol','www.bancodelsol.com'),('Banco del Tucumán','www.bancodeltucuman.com.ar'),"
                        + "('Banco del Uruguay','www.brounet.com.uy'),('Banco do Brasil','www.bancodobrasil.com.br'),('Banco Finansur','www.bancofinansur.com.ar'),('Banco Galicia','www.bancogalicia.com'),('Banco Hipotecario','www.hipotecario.com.ar'),"
                        + "('Banco Industrial','www.bancoindustrial.com.ar'),('Banco Itaú','www.itau.com.ar'),('Banco Julio','www.bancojulio.com.ar'),('Banco Macro','www.macro.com.ar'),('Banco Mariva','www.mariva.com.ar'),('Banco Masventas','www.bancomasventas.com.ar'),"
                        + "('Banco Meridian','www.bancomeridian.com'),('Banco Municipal de Rosario','www.bmros.com.ar'),('Banco Nación','www.bna.com.ar'),('Banco Patagonia','www.bancopatagonia.com.ar'),('Banco Piano','www.bancopiano.com.ar'),"
                        + "('Banco Provincia','www.bapro.com.ar'),('Banco Provincia del Neuquén','www.bpn.com.ar'),('Banco Regional de Cuyo','www.bancoregional.com.ar'),('Banco Roela','www.bancoroela.com.ar'),('Banco Saenz','www.bancosaenz.com.ar'),"
                        + "('Banco Santa Cruz','www.bancosantacruz.com.ar'),('Banco Santander Río','www.santanderrio.com.ar'),('Banco Supervielle','www.supervielle.com.ar'),('Bank of America','www.bankofamerica.com'),('Bank of Tokyo-Mitsubishi UFJ','www.bk.mufg.jp/english/'),"
                        + "('BBVA Banco Francés','www.bancofrances.com.ar'),('BICE','www.bice.com.ar'),('BNP Paribas','www.bnpparibas.com.ar'),('Citibank','www.citibank.com/argentina'),('Deutsche Bank','www.db.com'),('HSBC Bank','www.hsbc.com.ar'),"
                        + "('JPMorgan','www.jpmorgan.com'),('MBA Lazard Banco De Inversiones','www.mba-lazard.com'),('Nuevo Banco de Entre Ríos','www.nuevobersa.com.ar'),('Nuevo Banco de La Rioja','www.nblr.com.ar'),('Nuevo Banco de Santa Fe','www.bancobsf.com.ar'),"
                        + "('Nuevo Banco del Chaco','www.nbch.com.ar'),('RCI Banque','www.rcibanque.com'),('Standard Bank','www.standardbank.com.ar'), ('ICBC', 'www.icbc.com.ar');")
                        .executeUpdate();
            }
            //</editor-fold>

            // <editor-fold defaultstate="collapsed" desc="MovimientoConcepto.EFECTIVO">
            if (em.createQuery("SELECT COUNT(o) FROM " + Cuenta.class.getSimpleName() + " o").getSingleResult().toString().equalsIgnoreCase("0")) {
                LogManager.getLogger().trace("Creando " + Cuenta.class.getSimpleName());
                Cuenta o = new Cuenta();
                o.setId(1);
                o.setNombre("EFECTIVO");
                em.persist(o);
            }// </editor-fold>

            // <editor-fold defaultstate="collapsed" desc="Creación Provincias, Departamentos (de Mnes.) y Municipios (de Mnes.)">
            if (em.createQuery("SELECT COUNT(o) FROM Provincia o").getSingleResult().toString().equalsIgnoreCase("0")) {
                LogManager.getLogger().trace("Creando Provincias..");
                LogManager.getLogger().trace("Creando Departamentos.. (de Misiones)");
                LogManager.getLogger().trace("Creando Municipios..");
                getJDBCConnection().createStatement().execute(
                        " INSERT INTO provincia (idprovincia, nombre) VALUES "
                        + "(1,'Buenos Aires'), (2,'Catamarca'), (3,'Chaco'), (4,'Chubut'),"
                        + "(5,'Corrientes'), (6,'Córdoba'), (7,'Entre Ríos'), (8,'Formosa'),"
                        + "(9,'Jujuy'), (10,'La Pampa'), (11,'La Rioja'), (12,'Mendoza'),"
                        + "(13,'Misiones'),(14,'Neuquén'),(15,'Río Negro'),(16,'Salta'),"
                        + "(17,'San Juan'),(18,'San Luis'),(19,'Santa Cruz'),(20,'Santa Fe'),"
                        + "(21,'Santiago del Estero'),(22,'Tierra del Fuego'),(23,'Tucumán');"
                        + " INSERT INTO depto (iddepto, idprovincia, nombre) VALUES "
                        + "(1,5,'Corrientes'),(2,5,'Concepción'),(3,5,'Santo Tomé'),"
                        + "(4,13,'Posadas'), (5,13,'Concepción'), (6,13,'Eldorado'),"
                        + "(7,13,'General Manuel Belgrano'),(8,13,'Guaraní'),(9,13,'Iguazú'),"
                        + "(10,13,'Leandro N. Alem'),(11,13,'Libertador General San Martín'),"
                        + "(12,13,'Montecarlo'),(13,13,'OBERÁ'),(14,13,'San Ignacio'),"
                        + "(15,13,'San Javier'),(16,13,'San Pedro'),(17,13,'Veinticinco de Mayo'),"
                        + "(18,13,'APÓSTOLES'),(19,13,'Cainguás'),(20,13,'Candelaria');"
                        + " INSERT INTO municipio (iddepto, nombre) VALUES "
                        + "(4,'POSADAS'), (4,'GARUPÁ'),(4,'FACHINAL'),(5,'CONCEPCIÓN DE LA SIERRA'),(5,'SANTA MARIA'),(6,'COLONIA DELICIA'),(6,'9 DE JULIO'),"
                        + "(6,'EL DORADO'),(6,'COLONIA VICTORIA'),(7,'BERNARDO DE IRIGOYEN'),(7,'CMDTE ANDRESITO'),(7,'SAN ANTONIO'),(8,'SAN VICENTE'),"
                        + "(8,'EL SOBERBIO'),(9,'WANDA'),(9,'PUERTO LIBERTAD'),(9,'PUERTO ESPERANZA'),(9,'PUERTO IGUAZU'),(10,'ARROYO DEL MEDIO'),"
                        + "(10,'L.N ALEM'),(10,'DOS ARROYOS'),(10,'CAÁ-YARÍ'),(10,'OLEGARIO V. ANDRADE'),(10,'CERRO AZUL'),(10,'ALMAFUERTE'),"
                        + "(11,'PUERTO LEONI'),(11,'CAPIOVI'),(11,'PUERTO RICO'),(11,'RUIZ DE MONTOYA'),(12,'CARAGUATAY'),(13,'SAN MARTIN '),"
                        + "(13,'CAMPO VIERA'),(13,'COLONIA ALBERDI'),(13,'GRAL ALVEAR'),(13,'PANAMBI'),(13,'CAMPO RAMON'),(13,'GUARANI'),"
                        + "(14,'GRAL URQUIZA'),(14,'SANTO PIPO'),(14,'COLONIA POLANA'),(14,'SAN IGNACIO'),(14,'CORPUS'),(14,'JARDIN AMERICA'),"
                        + "(14,'HIPOLITO YRIGOYEN'),(15,'MOJON GRANDE'),(15,'SAN JAVIER'),(15,'Florentino Ameghino'),(16,'SAN PEDRO'),(17,'ALBA POSSE'),"
                        + "(17,'COLONIA AURORA'),(17,'25 DE MAYO'),(18,'AZARA'),(18,'APÓSTOLES'),(18,'SAN JOSE'),(18,'TRES CAPONES'),(19,'DOS DE MAYO'),"
                        + "(19,'CAMPO GRANDE'),(20,'MARTIRES'),(20,'BOMPLAN'),(20,'CERRO CORA'),(20,'CANDELARIA'),(20,'LORETO'),(20,'PROFUNDIDAD'),"
                        + "(20,'SANTA ANA');"
                        + "SELECT setval('public.depto_iddepto_seq', (SELECT count(1) FROM depto), true);");
                getJDBCConnection().commit();
                getJDBCConnection().close();
            }// </editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Creación de Operaciones bancarias">
            if (em.createNativeQuery("SELECT * FROM operaciones_bancarias").getResultList().isEmpty()) {
                LogManager.getLogger().trace("Creando Operaciones Bancarias..");
                StringBuilder sb = new StringBuilder("INSERT INTO operaciones_bancarias VALUES ");
                sb.append("(").append(OperacionesBancariasController.DEPOSITO).append(",'DEPÓSITO'),");
                sb.append("(").append(OperacionesBancariasController.EXTRACCION).append(",'EXTRACCIÓN'),");
                sb.append("(").append(OperacionesBancariasController.TRANSFERENCIA).append(",'TRANSFERENCIA');");
                em.createNativeQuery(sb.toString()).executeUpdate();
                em.createNativeQuery("SELECT SETVAL('operaciones_bancarias_id_seq'::regclass, 3);").getResultList();
            }
            //</editor-fold>

            if (new TipoDocumentoJpaController().findAll().isEmpty()) {
                LogManager.getLogger().trace("Creando Tipos de documentos..");
                em.persist(new TipoDocumento(1, "DNI", 96));
                em.persist(new TipoDocumento(2, "CUIT", 80));
                em.persist(new TipoDocumento(3, "CUIL", 86));
            }
            em.getTransaction().commit();
            // <editor-fold defaultstate="collapsed" desc="Creación DatosEmpresa">
            if (new DatosEmpresaJpaController().findDatosEmpresa() == null) {
                ventanaSystemMessage.agregar("Creando DatosEmpresa..");
                LogManager.getLogger().trace("Creando DatosEmpresa..");
                DatosEmpresa d = new DatosEmpresa(1, "JGestion", 30000000007L, "Dirección 777", 540000000, new Date());
                d.setContribuyente(new ContribuyenteController().findContribuyente(4));
                d.setLogo(null);
                new DatosEmpresaJpaController().create(d);
            }// </editor-fold>
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LogManager.getLogger().fatal(ex, ex);
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
     *
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

    static Query createNativeQuery(String query, Class<?> aClass, boolean withRefreshQueryHint) {
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

    public static Date getDateFromDB() {
        return (Date) getEntityManager().createNativeQuery("SELECT CURRENT_TIMESTAMP").getSingleResult();
    }
}
