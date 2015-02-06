package entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author FiruzzZ
 */
@Entity
public class VentaSimpleConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Cliente cliente;
    @JoinColumn(name = "lista_precios_id", nullable = false)
    @ManyToOne(optional = false)
    private ListaPrecios listaPrecios;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Caja caja;
    @JoinColumn(name = "unidad_de_negocio_id")
    @ManyToOne
    private UnidadDeNegocio unidadDeNegocio;
    @ManyToOne
    private Cuenta cuenta;
    @ManyToOne
    private SubCuenta subCuenta;
    @JoinTable(joinColumns = {
        @JoinColumn(name = "ventasimpleconfig_id")},
            inverseJoinColumns = @JoinColumn(name = "rubro_id"))
    @OneToMany(fetch = FetchType.EAGER)
    private List<Rubro> rubros;

    public VentaSimpleConfig() {
    }

    public VentaSimpleConfig(Integer id, Sucursal sucursal, Cliente cliente, ListaPrecios listaPrecios, Caja caja, UnidadDeNegocio unidadDeNegocio, Cuenta cuenta, SubCuenta subCuenta, List<Rubro> rubros) {
        this.id = id;
        this.sucursal = sucursal;
        this.cliente = cliente;
        this.listaPrecios = listaPrecios;
        this.caja = caja;
        this.unidadDeNegocio = unidadDeNegocio;
        this.cuenta = cuenta;
        this.subCuenta = subCuenta;
        this.rubros = rubros;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public ListaPrecios getListaPrecios() {
        return listaPrecios;
    }

    public Caja getCaja() {
        return caja;
    }

    public UnidadDeNegocio getUnidadDeNegocio() {
        return unidadDeNegocio;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public SubCuenta getSubCuenta() {
        return subCuenta;
    }

    public List<Rubro> getRubros() {
        return rubros;
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
        if (!(object instanceof VentaSimpleConfig)) {
            return false;
        }
        VentaSimpleConfig other = (VentaSimpleConfig) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VentaSimpleConfig{" + "id=" + id + ", sucursal=" + sucursal + ", cliente=" + cliente + ", listaPrecios=" + listaPrecios + ", caja=" + caja + ", unidadDeNegocio=" + unidadDeNegocio + ", cuenta=" + cuenta + ", subCuenta=" + subCuenta + ", rubros=" + rubros + '}';
    }

}
