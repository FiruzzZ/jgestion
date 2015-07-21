/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jgestion.test;

import jgestion.controller.DAO;
import generics.PropsUtils;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

/**
 *
 * @author FiruzzZ
 */
public class Populate {

    public void populating() {
        try {
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
