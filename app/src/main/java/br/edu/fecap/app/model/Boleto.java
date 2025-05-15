package br.edu.fecap.app.model;

import java.util.Date;

public class Boleto {
    private int id;
    private int idUsuario;
    private double valor;
    private Date dataVencimento;
    private Date dataPagamento;
    private String status; // "PENDENTE", "PAGO", "VENCIDO"
    private String codigoBarras;
    private int mes; // Mês referente (1-12)
    private int ano; // Ano referente

    // Construtor
    public Boleto(int id, int idUsuario, double valor, Date dataVencimento,
                  Date dataPagamento, String status, String codigoBarras, int mes, int ano) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.dataPagamento = dataPagamento;
        this.status = status;
        this.codigoBarras = codigoBarras;
        this.mes = mes;
        this.ano = ano;
    }

    // Construtor vazio
    public Boleto() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Date getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(Date dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }
}