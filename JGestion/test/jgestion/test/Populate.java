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
import java.io.IOException;
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
            Provincia misiones = em.find(Provincia.class, 13);
            Departamento capital = em.find(Departamento.class, 4);
            Municipio posadas = em.find(Municipio.class, 1);
            Rubro rubro = new Rubro(null, "Un Rubro", (short) 1);
            Marca marca = new Marca(null, "una Marca");
            ListaPrecios lp = new ListaPrecios(null, "VENTAS", 15.0, true, true);
            Contribuyente responsableInscripto = em.find(Contribuyente.class, 4);
            Sucursal s = new Sucursal("Una Sucursal", "ninguna", BigInteger.ONE, BigInteger.ZERO, 1, Integer.SIZE, Integer.SIZE, "encargado", "email@email.com", capital, posadas, misiones, 1L, 1, 1, 1, 1, 1);
            em.persist(rubro);
            em.persist(marca);
            em.persist(lp);
            em.persist(s);
            Cliente c = new Cliente();
            c.setCodigo("clie1");
            c.setCodigopostal(3300);
            c.setContacto("nadie");
            c.setContribuyente(responsableInscripto);
            c.setDepartamento(capital);
            c.setMunicipio(posadas);
            c.setProvincia(misiones);
            c.setDireccion("ninguna 777");
            c.setEstado(1);
            c.setNombre("Una Empresa Grande");
            c.setRubro(rubro);
            c.setTipodoc(2);
            c.setNumDoc(23306877569L);
            c.setSucursal(s);
            c.setTele1(3764777777L);

            em.getTransaction().commit();
            em.close();
        } catch (Exception ex) {
            Logger.getLogger(Populate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
