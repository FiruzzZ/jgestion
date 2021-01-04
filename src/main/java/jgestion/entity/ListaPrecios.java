package jgestion.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "lista_precios")
public class ListaPrecios implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "nombre", nullable = false, length = 100, unique = true)
    private String nombre;
    @Basic(optional = false)
    @Column(name = "margen", nullable = false, precision = 5, scale = 2)
    private Double margen;
    @Basic(optional = false)
    @Column(name = "margen_general", nullable = false)
    private boolean margenGeneral;
    @Basic(optional = false)
    @Column(name = "para_catalogo_web", nullable = false)
    private boolean paraCatalogoWeb;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "listaPrecio")
    private List<DetalleListaPrecios> detalleListaPreciosList;

    public ListaPrecios() {
        detalleListaPreciosList = new ArrayList<>();
    }

    public ListaPrecios(Integer id) {
        this();
        this.id = id;
    }

    public ListaPrecios(Integer id, String nombre, Double margen, boolean margenGeneral, boolean paraCatalogoWeb) {
        this();
        this.id = id;
        this.nombre = nombre;
        this.margen = margen;
        this.margenGeneral = margenGeneral;
        this.paraCatalogoWeb = paraCatalogoWeb;
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

    public Double getMargen() {
        return margen;
    }

    public void setMargen(Double margen) {
        this.margen = margen;
    }

    public boolean getMargenGeneral() {
        return margenGeneral;
    }

    public void setMargenGeneral(boolean margenGeneral) {
        this.margenGeneral = margenGeneral;
    }

    public boolean getParaCatalogoWeb() {
        return paraCatalogoWeb;
    }

    public void setParaCatalogoWeb(boolean paraCatalogoWeb) {
        this.paraCatalogoWeb = paraCatalogoWeb;
    }

    public List<DetalleListaPrecios> getDetalleListaPreciosList() {
        return detalleListaPreciosList;
    }

    public void setDetalleListaPreciosList(List<DetalleListaPrecios> detalleListaPreciosList) {
        this.detalleListaPreciosList = detalleListaPreciosList;
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
        if (!(object instanceof ListaPrecios)) {
            return false;
        }
        ListaPrecios other = (ListaPrecios) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getNombre() + (this.getParaCatalogoWeb() ? " [CW]" : "");
    }
}
