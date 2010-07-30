/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "iva")
@NamedQueries({@NamedQuery(name = "Iva.findAll", query = "SELECT i FROM Iva i"), @NamedQuery(name = "Iva.findById", query = "SELECT i FROM Iva i WHERE i.id = :id"), @NamedQuery(name = "Iva.findByIva", query = "SELECT i FROM Iva i WHERE i.iva = :iva")})
public class Iva implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "iva", nullable = false, precision = 4, scale = 1)
    private Double iva;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "iva")
    private List<Producto> productoList;

    public Iva() {
    }

    public Iva(Integer id) {
        this.id = id;
    }

    public Iva(Integer id, Double iva) {
        this.id = id;
        this.iva = iva;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getIva() {
        return iva;
    }

    public void setIva(Double iva) {
        this.iva = iva;
    }

    public List<Producto> getProductoList() {
        return productoList;
    }

    public void setProductoList(List<Producto> productoList) {
        this.productoList = productoList;
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
        if (!(object instanceof Iva)) {
            return false;
        }
        Iva other = (Iva) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getIva().toString();
    }

}
