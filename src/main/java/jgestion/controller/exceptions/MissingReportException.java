/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jgestion.controller.exceptions;

/**
 * Usada para indicar que no se pudo hallar el archivo del reporte
 * @author FiruzzZ
 */
public class MissingReportException extends Exception {

   public MissingReportException(String message) {
      super(message);
   }

}
