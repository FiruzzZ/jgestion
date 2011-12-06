package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author FiruzzZ
 */
@Entity
@Table(name = "producto", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"nombre"}),
    @UniqueConstraint(columnNames = {"codigo"})
})
@NamedQueries({
    @NamedQuery(name = "Producto.findAll", query = "SELECT p FROM Producto p ORDER BY p.nombre",
    hints = @QueryHint(name = QueryHints.REFRESH, value = "true")),
    @NamedQuery(name = "Producto.findById", query = "SELECT p FROM Producto p WHERE p.id = :id",
    hints = @QueryHint(name = QueryHints.REFRESH, value = "true")),
    @NamedQuery(name = "Producto.findByCodigo", query = "SELECT p FROM Producto p WHERE p.codigo = :codigo",
    hints = @QueryHint(name = QueryHints.REFRESH, value = "true")),
    @NamedQuery(name = "Producto.findByNombre", query = "SELECT p FROM Producto p WHERE p.nombre = :nombre")
})
@SqlResultSetMappings({
    //para los ComboBox
    @SqlResultSetMapping(name = "ProductoToBuscador", entities = {
        @EntityResult(entityClass = Producto.class, fields = {
            @FieldResult(name = "id", column = "id"),
            @FieldResult(name = "codigo", column = "codigo"),
            @FieldResult(name = "nombre", column = "nombre")
        })
    }),
//para la GUI de Contenedor de Productos...
    @SqlResultSetMapping(name = "ProductoToContenedor", entities = {
        @EntityResult(entityClass = Producto.class, fields = {
            @FieldResult(name = "id", column = "id"),
            @FieldResult(name = "codigo", column = "codigo"),
            @FieldResult(name = "nombre", column = "nombre"),
            @FieldResult(name = "marca", column = "marca"),
            @FieldResult(name = "stockactual", column = "stockactual")
        })
    })
})
public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "codigo", length = 50)
    private String codigo;
    @Column(name = "nombre", length = 250)
    private String nombre;
    @Basic(optional = false)
    @Column(name = "stockmaximo", nullable = false)
    private int stockmaximo;
    @Basic(optional = false)
    @Column(name = "stockactual", nullable = false)
    private int stockactual;
    @Basic(optional = false)
    @Column(name = "stockminimo", nullable = false)
    private int stockminimo;
    @Column(name = "deposito")
    private Integer deposito;
    @Column(name = "ubicacion", length = 50)
    private String ubicacion;
    @Column(name = "costo_compra", precision = 2, scale = 2)
    /**
     * Precio del costo, cuando se crea el producto es setteado a 0.
     * Después es modificado por las Factuas de compra.
     */
    private Double costoCompra;
    @Column(name = "descripcion", length = 2000)
    private String descripcion;
    @Column(name = "margen", precision = 2, scale = 2)
    private Double margen;
    @Column(name = "tipomargen")
    private Integer tipomargen;
    @Basic(optional = false)
    @Column(name = "precio_venta", precision = 2, scale = 2, nullable = false)
    private Double precioVenta;
    @Column(name = "foto")
    private byte[] foto;
    @Column(name = "remunerativo", nullable = false)
    private boolean remunerativo;
    @Basic(optional = false)
    @Column(name = "fecha_alta", nullable = false, insertable = false, updatable = false, columnDefinition = "timestamp with time zone NOT NULL DEFAULT now()")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date fechaAlta;
    @Column(name = "ultima_compra")
    @Temporal(value = TemporalType.DATE)
    private Date ultimaCompra;
    @JoinColumn(name = "iva", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Iva iva;
    @JoinColumn(name = "marca", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Marca marca;
    @JoinColumn(name = "rubro", referencedColumnName = "idrubro", nullable = false)
    @ManyToOne(optional = false)
    private Rubro rubro;
    @JoinColumn(name = "subrubro", referencedColumnName = "idrubro")
    @ManyToOne
    private Rubro subrubro;
    @JoinColumn(name = "idunidadmedida", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Unidadmedida idunidadmedida;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "producto")
    private List<Stock> stockList;

    public Producto() {
    }

    public Producto(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getStockmaximo() {
        return stockmaximo;
    }

    public void setStockmaximo(int stockmaximo) {
        this.stockmaximo = stockmaximo;
    }

    public int getStockactual() {
        return stockactual;
    }

    public void setStockactual(int stockactual) {
        this.stockactual = stockactual;
    }

    public int getStockminimo() {
        return stockminimo;
    }

    public void setStockminimo(int stockminimo) {
        this.stockminimo = stockminimo;
    }

    public Integer getDeposito() {
        return deposito;
    }

    public void setDeposito(Integer deposito) {
        this.deposito = deposito;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Double getCostoCompra() {
        return costoCompra;
    }

    public void setCostoCompra(Double costoCompra) {
        this.costoCompra = costoCompra;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getMargen() {
        return margen;
    }

    public void setMargen(Double margen) {
        this.margen = margen;
    }

    public Integer getTipomargen() {
        return tipomargen;
    }

    public void setTipomargen(Integer tipomargen) {
        this.tipomargen = tipomargen;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public boolean getRemunerativo() {
        return remunerativo;
    }

    public void setRemunerativo(boolean remunerativo) {
        this.remunerativo = remunerativo;
    }

    public Date getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(Date fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getUltimaCompra() {
        return ultimaCompra;
    }

    public void setUltimaCompra(Date ultimaCompra) {
        this.ultimaCompra = ultimaCompra;
    }

    public List<Stock> getStockList() {
        return stockList;
    }

    public void setStockList(List<Stock> stockList) {
        this.stockList = stockList;
    }

    public Iva getIva() {
        return iva;
    }

    public void setIva(Iva iva) {
        this.iva = iva;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Rubro getRubro() {
        return rubro;
    }

    public void setRubro(Rubro rubro) {
        this.rubro = rubro;
    }

    public Rubro getSubrubro() {
        return subrubro;
    }

    public void setSubrubro(Rubro subrubro) {
        this.subrubro = subrubro;
    }

    public Unidadmedida getIdunidadmedida() {
        return idunidadmedida;
    }

    public void setIdunidadmedida(Unidadmedida idunidadmedida) {
        this.idunidadmedida = idunidadmedida;
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
        if (!(object instanceof Producto)) {
            return false;
        }
        Producto other = (Producto) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getNombre();
    }

    /**
     * Retorna el mínimo valor (monetario) al cual se podrá vender este.
     * <ol>
     * Por prioridad:
     *  <li> {@link Producto#precioVenta} si está setteado.</li>
     *  <li> {@link Producto#costoCompra} cuando {@link Producto#precioVenta} <code> == null</code>.</li>
     * </ol>
     * @return precio mínimo.
     */
    public Double getMinimoPrecioDeVenta() {
        return this.precioVenta != null ? this.precioVenta : this.costoCompra;
    }

    /**
     * Calcula el margen de ganancia/descuento de ganancia/perdida sobre el monto
     * - monto cantidad monetaria sobre la cual se harán los cálculos
     * - tipoDeMargen Indica como se aplicará el margen al monto. 1 = % (porcentaje), 2 = $ (monto fijo), if (tipo > 2 || 1 > tipo) RETURN null!
     * - margen monto fijo o porcentual
     * @return adiviná!
     */
    public Double getMargenDeGananciaIndividual() {
        if (this.margen == 0) {
            return 0.0;
        }

        double total = 0.0;
        switch (this.tipomargen) {
            case 1: { // margen en %
                total = ((getMinimoPrecioDeVenta() * margen) / 100);
                break;
            }
            case 2: {  // margen en $ (monto fijo).. no hay mucha science...
                total = margen;
                break;
            }
            default:
                return null;
        }
        return total;
    }
}
