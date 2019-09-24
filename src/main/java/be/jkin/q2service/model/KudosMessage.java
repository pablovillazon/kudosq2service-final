package be.jkin.q2service.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public final class KudosMessage implements Serializable {
    private final String IdKudos;
    private final String Fuente;
    private final String Destino;
    private final MessageType Accion;

    public KudosMessage(@JsonProperty("id") String idKudos,
                        @JsonProperty("fuente") String fuente,
                        @JsonProperty("destino") String destino,
                        @JsonProperty("accion") MessageType accion)
    {
        this.IdKudos = idKudos;
        this.Fuente = fuente;
        this.Destino = destino;
        this.Accion = accion;
    }

    public String getIdKudos() {
        return IdKudos;
    }

    public String getFuente() {
        return Fuente;
    }

    public String getDestino() {
        return Destino;
    }

    public MessageType getAccion() {
        return Accion;
    }

    @Override
    public String toString()
    {
        return "KudosMessage{"+
                "id= '" + IdKudos +"'"+
                "fuente = '"+ Fuente  +"'"+
                "destino = '"+ Destino +"'"+
                "accion = '"+ Accion+"'";
    }
}
