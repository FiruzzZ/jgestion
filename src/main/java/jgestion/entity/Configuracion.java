
package jgestion.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "configuracion")
@NamedQueries({
   @NamedQuery(name = "Configuracion.findAll", query = "SELECT c FROM Configuracion c")
})
public class Configuracion implements Serializable {
   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   private Integer id;
   @Column(name = "initial_position_mainscreen_x", precision = 17, scale = 17)
   private Double initialPositionMainscreenX;
   @Column(name = "initial_position_mainscreen_y", precision = 17, scale = 17)
   private Double initialPositionMainscreenY;
   @Basic(optional = false)
   @Column(name = "width_mainscreen", nullable = false)
   private int widthMainscreen;
   @Basic(optional = false)
   @Column(name = "height_mainscreen", nullable = false)
   private int heightMainscreen;
   @Basic(optional = false)
   @Column(name = "decimal_digits_count", nullable = false)
   private int decimalDigitsCount;

   public Configuracion() {
   }

   public Configuracion(Integer id) {
      this.id = id;
   }

   public Configuracion(Integer id, int widthMainscreen, int heightMainscreen, int decimalDigitsCount) {
      this.id = id;
      this.widthMainscreen = widthMainscreen;
      this.heightMainscreen = heightMainscreen;
      this.decimalDigitsCount = decimalDigitsCount;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Double getInitialPositionMainscreenX() {
      return initialPositionMainscreenX;
   }

   public void setInitialPositionMainscreenX(Double initialPositionMainscreenX) {
      this.initialPositionMainscreenX = initialPositionMainscreenX;
   }

   public Double getInitialPositionMainscreenY() {
      return initialPositionMainscreenY;
   }

   public void setInitialPositionMainscreenY(Double initialPositionMainscreenY) {
      this.initialPositionMainscreenY = initialPositionMainscreenY;
   }

   public int getWidthMainscreen() {
      return widthMainscreen;
   }

   public void setWidthMainscreen(int widthMainscreen) {
      this.widthMainscreen = widthMainscreen;
   }

   public int getHeightMainscreen() {
      return heightMainscreen;
   }

   public void setHeightMainscreen(int heightMainscreen) {
      this.heightMainscreen = heightMainscreen;
   }

   public int getDecimalDigitsCount() {
      return decimalDigitsCount;
   }

   public void setDecimalDigitsCount(int decimalDigitsCount) {
      this.decimalDigitsCount = decimalDigitsCount;
   }

   @Override
   public int hashCode() {
      int hash = 0;
      hash += (id != null ? id.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object object) {
      // TODO: Warning - this method won't work in the case the id fields are not set
      if (!(object instanceof Configuracion)) {
         return false;
      }
      Configuracion other = (Configuracion) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.Configuracion[id=" + id + 
              ", init_x=" + initialPositionMainscreenX +
              ", init_y=" + initialPositionMainscreenY +
              ", width_s=" + widthMainscreen +
              ", height_s=" + heightMainscreen +
              ", decimal=" + decimalDigitsCount + "]";
   }

}
