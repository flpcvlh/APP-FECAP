package br.edu.fecap.app.model;

public class Livro {
    private int id;
    private String titulo;
    private String autor;
    private String descricao;
    private boolean disponivel;
    private String imagemUrl; // Novo campo para a imagem

    // Construtor
    public Livro(int id, String titulo, String autor, String descricao, boolean disponivel, String imagemUrl) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.descricao = descricao;
        this.disponivel = disponivel;
        this.imagemUrl = imagemUrl;
    }

    // Construtor vazio
    public Livro() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    // Novo getter e setter para imagemUrl
    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

}