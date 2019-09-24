package be.jkin.q2service.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import org.bson.types.ObjectId;

import java.util.Date;

@Table("kudos")
public class Kudos {

    @PrimaryKey
    private ObjectId _id;
    private String fuente;
    private String destino;
    private KudosTema tema;
    private Date fecha;
    private String lugar;
    private String texto;

    public enum KudosTema{
        THANKS,
        WELL_DONE,
        GREAT_JOB,
        CHALA,
        GENERICO
    }

    //Constructors
    public Kudos(){}

    public Kudos(ObjectId _id, String fuente, String destino, KudosTema tema, Date fecha, String lugar, String texto)
    {
        this._id = _id;
        this.fuente = fuente;
        this.destino = destino;
        this.tema = tema;
        this.fecha = fecha;
        this.lugar = lugar;
        this.texto = texto;
    }

    public void setId(ObjectId _id)
    {
        this._id = _id;
    }

    public String getId()
    {
        return this._id.toHexString();
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public KudosTema getTema() {
        return tema;
    }

    public void setTema(KudosTema tema) {
        this.tema = tema;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }


}
