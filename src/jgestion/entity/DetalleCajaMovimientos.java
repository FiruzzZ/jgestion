package jgestion.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Administrador
 */
@Entity
@Table(name = "detalle_caja_movimientos")
@NamedQueries({
    @NamedQuery(name = "DetalleCajaMovimientos.findAll", query = "SELECT d FROM DetalleCajaMovimientos d"),
    @NamedQuery(name = "DetalleCajaMovimientos.findById", query = "SELECT d FROM DetalleCajaMovimientos d WHERE d.id = :id")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(name = "DetalleCajaMovimientos.BalanceGeneral", entities = {
        @EntityResult(entityClass = DetalleCajaMovimientos.class, fields = {
            @FieldResult(name = "id", column = "id"),
            @FieldResult(name = "ingreso", column = "ingreso"),
            @FieldResult(name = "tipo", column = "tipo"),
            @FieldResult(name = "monto", column = "monto"),
            @FieldResult(name = "fecha", column = "fecha"),
            @FieldResult(name = "descripcion", column = "descripcion")
        })
    })
})
public class DetalleCajaMovimientos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "ingreso", nullable = false)
    private boolean ingreso;
    /**
     * 1 factu_compra, 2 factu_venta, 3 remesa, 4 recibo, 5 movimiento caja, 6
     * devolucion (anulacion), 7 apertura caja, 8 mov. varios, 9 mov interno
     * (MVI)
     */
    @Basic(optional = false)
    @Column(name = "tipo", nullable = false)
    private short tipo;
    @Basic(optional = false)
    @Column(name = "numero", nullable = false)
    private long numero;
    @Basic(optional = false)
    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;
    @Basic(optional = false)
    @Column(name = "fecha", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    @Column(name = "fechamovimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaMovimiento;
    @Basic(optional = false)
    @Column(name = "descripcion", nullable = false, length = 250)
    private String descripcion;
    @JoinColumn(name = "caja_movimientos", nullable = false)
    @ManyToOne(optional = false)
    private CajaMovimientos cajaMovimientos;
    @JoinColumn(name = "usuario", nullable = false)
    @ManyToOne(optional = false)
    private Usuario usuario;
    @JoinColumn(name = "movimiento_concepto", nullable = false) //REFACTOR column name pendiente!!..
    @ManyToOne(optional = false)
    private Cuenta cuenta;
    @JoinColumn(name = "subcuenta_id")
    @ManyToOne
    private SubCuenta subCuenta;
    @JoinColumn(name = "unidad_de_negocio_id")
    @ManyToOne
    private UnidadDeNegocio unidadDeNegocio;

    public DetalleCajaMovimientos() {
    }

    public DetalleCajaMovimientos(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean getIngreso() {
        return ingreso;
    }

    public void setIngreso(boolean ingreso) {
        this.ingreso = ingreso;
    }

    public short getTipo() {
        return tipo;
    }

    /**
     * Set el tipo (este campo está parametrizado).
     *
     * @param tipo
     * @see DetalleCajaMovimientos#tipo
     */
    public void setTipo(short tipo) {
        this.tipo = tipo;
    }

    public long getNumero() {
        return numero;
    }

    public void setNumero(long numero) {
        this.numero = numero;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public CajaMovimientos getCajaMovimientos() {
        return cajaMovimientos;
    }

    public void setCajaMovimientos(CajaMovimientos cajaMovimientos) {
        this.cajaMovimientos = cajaMovimientos;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public SubCuenta getSubCuenta() {
        return subCuenta;
    }

    public void setSubCuenta(SubCuenta subCuenta) {
        this.subCuenta = subCuenta;
    }

    public UnidadDeNegocio getUnidadDeNegocio() {
        return unidadDeNegocio;
    }

    public void setUnidadDeNegocio(UnidadDeNegocio unidadDeNegocio) {
        this.unidadDeNegocio = unidadDeNegocio;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Date fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
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
        if (!(object instanceof DetalleCajaMovimientos)) {
            return false;
        }
        DetalleCajaMovimientos other = (DetalleCajaMovimientos) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getDescripcion();
    }
}
