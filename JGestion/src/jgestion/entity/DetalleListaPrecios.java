
package jgestion.entity;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "detalle_lista_precios")
@NamedQueries({@NamedQuery(name = "DetalleListaPrecios.findAll", query = "SELECT d FROM DetalleListaPrecios d"), @NamedQuery(name = "DetalleListaPrecios.findById", query = "SELECT d FROM DetalleListaPrecios d WHERE d.id = :id"), @NamedQuery(name = "DetalleListaPrecios.findByMargen", query = "SELECT d FROM DetalleListaPrecios d WHERE d.margen = :margen")})
public class DetalleListaPrecios implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "margen", nullable = false, precision = 5, scale = 2)
    private Double margen;
    @JoinColumn(name = "lista_precio", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private ListaPrecios listaPrecio;
    @JoinColumn(name = "rubro", referencedColumnName = "idrubro", nullable = false)
    @ManyToOne(optional = false)
    private Rubro rubro;

    public DetalleListaPrecios() {
    }

    public DetalleListaPrecios(Integer id) {
        this.id = id;
    }

    public DetalleListaPrecios(Integer id, Double margen) {
        this.id = id;
        this.margen = margen;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getMargen() {
        return margen;
    }

    public void setMargen(Double margen) {
        this.margen = margen;
    }

    public ListaPrecios getListaPrecio() {
        return listaPrecio;
    }

    public void setListaPrecio(ListaPrecios listaPrecio) {
        this.listaPrecio = listaPrecio;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
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
        if (!(object instanceof DetalleListaPrecios)) {
            return false;
        }
        DetalleListaPrecios other = (DetalleListaPrecios) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.DetalleListaPrecios[id=" + id + "]";
    }

}
