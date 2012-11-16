/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "cuentabancaria_movimientos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CuentabancariaMovimientos.findAll", query = "SELECT c FROM CuentabancariaMovimientos c")})
public class CuentabancariaMovimientos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "fecha_operacion", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaOperacion;
    @Column(length = 200)
    private String descripcion;
    @Column(name = "fecha_credito_debito")
    @Temporal(TemporalType.DATE)
    private Date fechaCreditoDebito;
    @Basic(optional = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal credito;
    @Basic(optional = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal debito;
    @Basic(optional = false)
    @Column(name = "fecha_sistema", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSistema;
    @Basic(optional = false)
    @Column(nullable = false)
    private boolean anulada;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @JoinColumn(nullable = false, name = "operaciones_bancarias_id")
    @ManyToOne(optional = false)
    private OperacionesBancarias operacionesBancarias;
    @JoinColumn(nullable = false)
    @ManyToOne(optional = false)
    private CuentaBancaria cuentaBancaria;
    @ManyToOne
    @JoinColumn(name = "cheque_terceros_id")
    private ChequeTerceros chequeTerceros;
    @ManyToOne
    @JoinColumn(name = "cheque_propio_id")
    private ChequePropio chequePropio;

    public CuentabancariaMovimientos() {
    }

    public CuentabancariaMovimientos(Integer id) {
        this.id = id;
    }

    public CuentabancariaMovimientos(Date fechaOperacion, String descripcion, Date fechaCreditoDebito, BigDecimal credito, BigDecimal debito, boolean anulada, Usuario usuario, OperacionesBancarias movimientoConcepto, CuentaBancaria cuentaBancaria, ChequeTerceros chequeTerceros, ChequePropio chequePropio) {
        this.fechaOperacion = fechaOperacion;
        this.descripcion = descripcion;
        this.fechaCreditoDebito = fechaCreditoDebito;
        this.credito = credito;
        this.debito = debito;
        this.anulada = anulada;
        this.usuario = usuario;
        this.operacionesBancarias = movimientoConcepto;
        this.cuentaBancaria = cuentaBancaria;
        this.chequeTerceros = chequeTerceros;
        this.chequePropio = chequePropio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getFechaOperacion() {
        return fechaOperacion;
    }

    public void setFechaOperacion(Date fechaOperacion) {
        this.fechaOperacion = fechaOperacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFechaCreditoDebito() {
        return fechaCreditoDebito;
    }

    public void setFechaCreditoDebito(Date fechaCreditoDebito) {
        this.fechaCreditoDebito = fechaCreditoDebito;
    }

    public BigDecimal getCredito() {
        return credito;
    }

    public void setCredito(BigDecimal credito) {
        this.credito = credito;
    }

    public BigDecimal getDebito() {
        return debito;
    }

    public void setDebito(BigDecimal debito) {
        this.debito = debito;
    }

    public Date getFechaSistema() {
        return fechaSistema;
    }

    public void setFechaSistema(Date fechaSistema) {
        this.fechaSistema = fechaSistema;
    }

    public boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(boolean anulada) {
        this.anulada = anulada;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public OperacionesBancarias getOperacionesBancarias() {
        return operacionesBancarias;
    }

    public void setOperacionesBancarias(OperacionesBancarias operacionesBancarias) {
        this.operacionesBancarias = operacionesBancarias;
    }

    public CuentaBancaria getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(CuentaBancaria cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }

    public ChequeTerceros getChequeTerceros() {
        return chequeTerceros;
    }

    public void setChequeTerceros(ChequeTerceros chequeTerceros) {
        this.chequeTerceros = chequeTerceros;
    }

    public ChequePropio getChequePropio() {
        return chequePropio;
    }

    public void setChequePropio(ChequePropio chequePropio) {
        this.chequePropio = chequePropio;
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
        if (!(object instanceof CuentabancariaMovimientos)) {
            return false;
        }
        CuentabancariaMovimientos other = (CuentabancariaMovimientos) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CuentabancariaMovimientos{" + "id=" + id + ", fechaOperacion=" + fechaOperacion + ", descripcion=" + descripcion + ", fechaCreditoDebito=" + fechaCreditoDebito + ", credito=" + credito + ", debito=" + debito + ", fechaSistema=" + fechaSistema + ", anulada=" + anulada + ", operacionesBancarias=" + operacionesBancarias + ", cuentaBancaria=" + (cuentaBancaria == null ? null : cuentaBancaria.getId()) + ", chequeTerceros=" + chequeTerceros + ", chequePropio=" + chequePropio + '}';
    }
}
