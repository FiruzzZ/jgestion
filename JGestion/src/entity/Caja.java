package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "caja", uniqueConstraints = {
   @UniqueConstraint(columnNames = {"nombre"})})
@NamedQueries({
   @NamedQuery(name = "Caja.findAll", query = "SELECT c FROM Caja c"),
   @NamedQuery(name = "Caja.findById", query = "SELECT c FROM Caja c WHERE c.id = :id"),
   @NamedQuery(name = "Caja.findByBaja", query = "SELECT c FROM Caja c WHERE c.baja = :baja")
})
public class Caja implements Serializable {

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
   @Column(name = "estado", nullable = false)
   private boolean estado;
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "caja")
   private List<CajaMovimientos> cajaMovimientosList;
   @Basic(optional = false)
   @Column(name = "baja", nullable = false)
   private boolean baja;

   public Caja() {
   }

   public Caja(Integer id) {
      this.id = id;
   }

   public Caja(Integer id, String nombre, boolean estado, boolean baja) {
      this.id = id;
      this.nombre = nombre;
      this.estado = estado;
      this.baja = baja;
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

   public boolean getEstado() {
      return estado;
   }

   public void setEstado(boolean estado) {
      this.estado = estado;
   }

   public List<CajaMovimientos> getCajaMovimientosList() {
      return cajaMovimientosList;
   }

   public void setCajaMovimientosList(List<CajaMovimientos> cajaMovimientosList) {
      this.cajaMovimientosList = cajaMovimientosList;
   }

   public boolean isBaja() {
      return this.baja;
   }

   public void setBaja(boolean baja) {
      this.baja = baja;
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
      if (!(object instanceof Caja)) {
         return false;
      }
      Caja other = (Caja) object;
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
