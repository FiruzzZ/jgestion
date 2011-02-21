package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "historial_ofertas")
@NamedQueries({
   @NamedQuery(name = "HistorialOfertas.findAll", query = "SELECT h FROM HistorialOfertas h"),
   @NamedQuery(name = "HistorialOfertas.findById", query = "SELECT h FROM HistorialOfertas h WHERE h.id = :id")
})
public class HistorialOfertas implements Serializable {

   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "precio", nullable = false)
   private double precio;
   @Basic(optional = false)
   @Column(name = "destacado", nullable = false)
   private boolean destacado;
   @Basic(optional = false)
   @Column(name = "inicio_oferta", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date inicioOferta;
   @Basic(optional = false)
   @Column(name = "fin_oferta", nullable = false)
   @Temporal(TemporalType.DATE)
   private Date finOferta;
   @Basic(optional = false)
   @Column(name = "vigente", nullable = false)
   private boolean vigente;
   @Column(name = "alta", nullable = false, insertable = false, updatable = false, columnDefinition = " timestamp with time zone NOT NULL DEFAULT now()")
   @Temporal(TemporalType.DATE)
   private Date alta;
   @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false, updatable = false)
   @ManyToOne(optional = false)
   private Producto producto;

   public HistorialOfertas() {
   }

   public HistorialOfertas(Integer id) {
      this.id = id;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public double getPrecio() {
      return precio;
   }

   public void setPrecio(double precio) {
      this.precio = precio;
   }

   public boolean getDestacado() {
      return destacado;
   }

   /**
    * Es destacado u oferta.. no hay mucha historia
    * @param destacado
    */
   public void setDestacado(boolean destacado) {
      this.destacado = destacado;
   }

   public Date getInicioOferta() {
      return inicioOferta;
   }

   public void setInicioOferta(Date inicioOferta) {
      this.inicioOferta = inicioOferta;
   }

   public Date getFinOferta() {
      return finOferta;
   }

   public void setFinOferta(Date finOferta) {
      this.finOferta = finOferta;
   }

   public boolean getVigente() {
      return vigente;
   }

   public void setVigente(boolean vigente) {
      this.vigente = vigente;
   }

   public Date getAlta() {
      return alta;
   }

   public void setAlta(Date alta) {
      this.alta = alta;
   }

   public Producto getProducto() {
      return producto;
   }

   public void setProducto(Producto producto) {
      this.producto = producto;
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
      if (!(object instanceof HistorialOfertas)) {
         return false;
      }
      HistorialOfertas other = (HistorialOfertas) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.HistorialOfertas[id=" + id + "]";
   }
}
