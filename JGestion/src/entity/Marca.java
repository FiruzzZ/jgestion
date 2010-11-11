package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 * Clase Marca, de la cual depende Producto para ser persistido
 * @author FiruzzZ
 */
@Entity
@Table(name = "marca", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"nombre"})})
@NamedQueries({
   @NamedQuery(name = "Marca.findAll", query = "SELECT m FROM Marca m ORDER BY m.nombre"),
   @NamedQuery(name = "Marca.findById", query = "SELECT m FROM Marca m WHERE m.id = :id"),
   @NamedQuery(name = "Marca.findByCodigo", query = "SELECT m FROM Marca m WHERE m.codigo = :codigo"),
   @NamedQuery(name = "Marca.findByNombre", query = "SELECT m FROM Marca m WHERE m.nombre = :nombre")
})
public class Marca implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Column(name = "codigo", length = 50)
   private String codigo;
   @Basic(optional = false)
   @Column(name = "nombre", nullable = false, length = 50)
   private String nombre;

   public Marca() {
   }

   public Marca(Integer id) {
      this.id = id;
   }

   public Marca(Integer id, String nombre) {
      this.id = id;
      this.nombre = nombre;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getCodigo() {
      return codigo;
   }

   public void setCodigo(String codigo) {
      this.codigo = codigo;
   }

   public String getNombre() {
      return nombre;
   }

   public void setNombre(String nombre) {
      this.nombre = nombre;
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
      if (!(object instanceof Marca)) {
         return false;
      }
      Marca other = (Marca) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return this.getNombre();
   }
}
