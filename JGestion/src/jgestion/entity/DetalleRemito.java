/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jgestion.entity;

import java.io.Serializable;
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

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "detalle_remito")
@NamedQueries({
   @NamedQuery(name = "DetalleRemito.findAll", query = "SELECT d FROM DetalleRemito d"),
   @NamedQuery(name = "DetalleRemito.findById", query = "SELECT d FROM DetalleRemito d WHERE d.id = :id"),
   @NamedQuery(name = "DetalleRemito.findByCantidad", query = "SELECT d FROM DetalleRemito d WHERE d.cantidad = :cantidad")})
public class DetalleRemito implements Serializable {
   private static final long serialVersionUID = 1L;
   @Id
   @Basic(optional = false)
   @Column(name = "id", nullable = false)
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;
   @Basic(optional = false)
   @Column(name = "cantidad", nullable = false)
   private int cantidad;
   @JoinColumn(name = "producto", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Producto producto;
   @JoinColumn(name = "remito", referencedColumnName = "id", nullable = false)
   @ManyToOne(optional = false)
   private Remito remito;

   public DetalleRemito() {
   }

   public DetalleRemito(Integer id) {
      this.id = id;
   }

   public DetalleRemito(Integer id, int cantidad) {
      this.id = id;
      this.cantidad = cantidad;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public int getCantidad() {
      return cantidad;
   }

   public void setCantidad(int cantidad) {
      this.cantidad = cantidad;
   }

   public Producto getProducto() {
      return producto;
   }

   public void setProducto(Producto producto) {
      this.producto = producto;
   }

   public Remito getRemito() {
      return remito;
   }

   public void setRemito(Remito remito) {
      this.remito = remito;
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
      if (!(object instanceof DetalleRemito)) {
         return false;
      }
      DetalleRemito other = (DetalleRemito) object;
      if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }

   @Override
   public String toString() {
      return "entity.DetalleRemito[id=" + id + "]";
   }

}
