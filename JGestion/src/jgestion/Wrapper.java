/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jgestion;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.swing.components.ComboBoxWrapper;

/**
 *
 * @param <T> 
 * @author FiruzzZ
 */
public final class Wrapper<T> {

    public Wrapper() {
    }

    public List<ComboBoxWrapper<T>> getWrapped(List<T> list) {
        List<ComboBoxWrapper<T>> l = new ArrayList<ComboBoxWrapper<T>>(list.size());
        for (T t : list) {
            try {
                Object c = t;
                Integer id = (Integer) c.getClass().getMethod("getId").invoke(c, new Object[]{});
                String nombre = (String) c.getClass().getMethod("getNombre").invoke(c, new Object[]{});
                l.add(new ComboBoxWrapper<T>(t, id, nombre));
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Wrapper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Wrapper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Wrapper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(Wrapper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(Wrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return l;
    }
    
}
