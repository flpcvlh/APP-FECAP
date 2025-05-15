package br.edu.fecap.app.model;

public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String matricula;
    private String senha;

    // Construtor
    public Usuario(int id, String nome, String email, String matricula, String senha) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.matricula = matricula;
        this.senha = senha;
    }

    // Construtor vazio para uso com Room e Retrofit
    public Usuario() {
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}