package entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "tipos_documentos", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"nombre"})
})
@NamedQueries({
   @NamedQuery(name = "TiposDocumentos.findAll", query = "SELECT t FROM TiposDocumentos t"),
   @NamedQuery(name = "TiposDocumentos.findById", query = "SELECT t FROM TiposDocumentos t WHERE t.id = :id"),
   @NamedQuery(name = "TiposDocumentos.findByNombre", query = "SELECT t FROM TiposDocumentos t WHERE t.nombre = :nombre")
})
public class TiposDocumentos implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "nombre", nullable = false, length = 50)
   private String nombre;
   @Basic(optional = false)
   @Column(name = "longitud_max", nullable = false)
   private int longitudMax;

   public TiposDocumentos() {
   }

   public TiposDocumentos(Integer id) {
      this.id = id;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getNombre() {
      return nombre;
   }

   public void setNombre(String nombre) {
      this.nombre = nombre;
   }

   public int getLongitudMax() {
      return longitudMax;
   }

   public void setLongitudMax(int longitudMax) {
      this.longitudMax = longitudMax;
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
      if (!(object instanceof TiposDocumentos)) {
         return false;
      }
      TiposDocumentos other = (TiposDocumentos) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.TiposDocumentos[id=" + id + "]";
   }
}
