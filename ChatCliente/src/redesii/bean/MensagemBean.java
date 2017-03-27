/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package redesii.bean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Plinio
 */
public class MensagemBean implements Serializable{
    
    private String nome;
    private String texto;
    private String nomePrivado;
    private Set<String> usuariosOnline = new HashSet<String>();
    private Acao acao;
    private Status status;
    
    public enum Acao {
        CONECTAR, DESCONECTAR, ENVIAR_PARA_TODOS, ENVIAR_PARA_UM, USUARIOS_ONLINE, RESERVADO, MUDAR_STATUS
    }
    
    public enum Status {
        DISPONIVEL, OCUPADO, AUSENTE
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getNomePrivado() {
        return nomePrivado;
    }

    public void setNomePrivado(String nomePrivado) {
        this.nomePrivado = nomePrivado;
    }

    public Set<String> getUsuariosOnline() {
        return usuariosOnline;
    }

    public void setUsuariosOnline(Set<String> usuariosOnline) {
        this.usuariosOnline = usuariosOnline;
    }

    public Acao getAcao() {
        return acao;
    }

    public void setAcao(Acao acao) {
        this.acao = acao;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
}
