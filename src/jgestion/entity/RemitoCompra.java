package jgestion.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "remito_compra", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"numero", "proveedor_id"})})
public class RemitoCompra implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "numero", nullable = false, precision = 12)
    private long numero;
    @Basic(optional = false)
    @Column(name = "fecha_remito", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaRemito;
    @Basic(optional = false)
    @Column(name = "anulada", nullable = false)
    private boolean anulada;
    @Basic(optional = false)
    @Column(name = "actualiza_stock", nullable = false)
    private boolean actualizaStock;
    @JoinColumn(name = "proveedor_id", nullable = false)
    @ManyToOne(optional = false)
    private Proveedor proveedor;
    @JoinColumn(name = "sucursal_id", nullable = false)
    @ManyToOne(optional = false)
    private Sucursal sucursal;
    @Column(nullable = false)
    private boolean acuenta;
    @Column(length = 100)
    private String observacion;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "remitoCompra", orphanRemoval = true)
    private List<RemitoCompraDetalle> detalle;

    public RemitoCompra() {
        detalle = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getNumero() {
        return numero;
    }

    public void setNumero(long numero) {
        this.numero = numero;
    }

    public Date getFechaRemito() {
        return fechaRemito;
    }

    public void setFechaRemito(Date fechaRemito) {
        this.fechaRemito = fechaRemito;
    }

    public boolean isActualizaStock() {
        return actualizaStock;
    }

    public void setActualizaStock(boolean actualizaStock) {
        this.actualizaStock = actualizaStock;
    }

    public boolean isAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public List<RemitoCompraDetalle> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<RemitoCompraDetalle> detalle) {
        this.detalle = detalle;
    }

    public boolean isAcuenta() {
        return acuenta;
    }

    public void setAcuenta(boolean acuenta) {
        this.acuenta = acuenta;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
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
        if (!(object instanceof RemitoCompra)) {
            return false;
        }
        RemitoCompra other = (RemitoCompra) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RemitoCompra{" + "id=" + id + ", numero=" + numero + ", fechaRemito=" + fechaRemito + ", anulada=" + anulada + ", actualizaStock=" + actualizaStock
                + ", proveedor=" + proveedor + ", sucursal=" + sucursal + ", acuenta=" + acuenta + ", observacion=" + observacion + ", detalle=" + detalle + '}';
    }

}
