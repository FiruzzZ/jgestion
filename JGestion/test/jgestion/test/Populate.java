/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jgestion.test;

import controller.DAO;
import entity.Cliente;
import entity.Contribuyente;
import entity.Departamento;
import entity.ListaPrecios;
import entity.Marca;
import entity.Municipio;
import entity.Provincia;
import entity.Rubro;
import entity.Sucursal;
import generics.PropsUtils;
import java.io.File;
import java.math.BigInteger;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

/**
 *
 * @author FiruzzZ
 */
public class Populate {

    @Test
    public void populating() {
        try {
            PropertyConfigurator.configure("log4j.properties");
            Properties properties = PropsUtils.load(new File("cfg.ini"));
            DAO.setProperties(properties);
            EntityManager em = DAO.getEntityManager();
            em.getTransaction().begin();
            em.getTransaction().commit();
            em.close();
        } catch (Exception ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
