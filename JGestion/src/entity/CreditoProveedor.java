/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "credito_proveedor")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CreditoProveedor.findAll", query = "SELECT c FROM CreditoProveedor c")})
public class CreditoProveedor implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean debe;
    @Basic(optional = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;
    @Basic(optional = false)
    @Column(nullable = false, length = 200)
    private String concepto;
    @Basic(optional = false)
    @Column(name = "fecha_carga", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCarga;
    @JoinColumn(name = "proveedor_id", nullable = false)
    @ManyToOne(optional = false)
    private Proveedor proveedor;

    public CreditoProveedor() {
    }

    public CreditoProveedor(Integer id) {
        this.id = id;
    }

    public CreditoProveedor(Integer id, boolean debe, BigDecimal importe, String concepto, Proveedor proveedor) {
        this.id = id;
        this.debe = debe;
        this.importe = importe;
        this.concepto = concepto;
        this.proveedor = proveedor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean getDebe() {
        return debe;
    }

    public void setDebe(boolean debe) {
        this.debe = debe;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Date getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Date fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
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
        if (!(object instanceof CreditoProveedor)) {
            return false;
        }
        CreditoProveedor other = (CreditoProveedor) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CreditoProveedor{" + "id=" + id + ", debe=" + debe + ", importe=" + importe + ", concepto=" + concepto + ", fechaCarga=" + fechaCarga + ", proveedor.id=" + (proveedor != null ? proveedor.getId() : null) + '}';
    }
}
