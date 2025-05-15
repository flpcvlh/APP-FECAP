package br.edu.fecap.app.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import br.edu.fecap.app.model.Boleto;
import br.edu.fecap.app.model.Emprestimo;
import br.edu.fecap.app.model.Livro;

/**
 * Classe para simular respostas da API enquanto o backend não está pronto
 * Isso permite desenvolver e testar o aplicativo sem depender da API real
 */
public class ApiMock {

    private static final Random random = new Random();

    // Simular lista de livros disponíveis
    public static List<Livro> getLivrosMock() {
        List<Livro> livros = new ArrayList<>();

        Livro livro1 = new Livro();
        livro1.setId(1);
        livro1.setTitulo("Entendendo Algoritmos");
        livro1.setAutor("Aditya Y. Bhargava");
        livro1.setDescricao("Um guia ilustrado para programadores e outros curiosos");
        livro1.setDisponivel(true);
        livro1.setImagemUrl("livro_algoritmos"); // Nome do recurso drawable

        Livro livro2 = new Livro();
        livro2.setId(2);
        livro2.setTitulo("O Mítico Homem-Mês");
        livro2.setAutor("Frederick P. Brooks Jr.");
        livro2.setDescricao("Ensaios sobre engenharia de software");
        livro2.setDisponivel(true);
        livro2.setImagemUrl("livro_mitico");

        Livro livro3 = new Livro();
        livro3.setId(3);
        livro3.setTitulo("Código Limpo");
        livro3.setAutor("Robert C. Martin");
        livro3.setDescricao("Habilidades práticas do Agile Software");
        livro3.setDisponivel(false);
        livro3.setImagemUrl("livro_clean_code");

        Livro livro4 = new Livro();
        livro4.setId(4);
        livro4.setTitulo("O Programador Pragmático");
        livro4.setAutor("Andrew Hunt e David Thomas");
        livro4.setDescricao("De Aprendiz a Mestre");
        livro4.setDisponivel(true);
        livro4.setImagemUrl("livro_pragmatic");

        Livro livro5 = new Livro();
        livro5.setId(5);
        livro5.setTitulo("Padrões de Projeto");
        livro5.setAutor("Erich Gamma et al.");
        livro5.setDescricao("Soluções reutilizáveis de software orientado a objetos");
        livro5.setDisponivel(true);
        livro5.setImagemUrl("livro_design_patterns");

        livros.add(livro1);
        livros.add(livro2);
        livros.add(livro3);
        livros.add(livro4);
        livros.add(livro5);

        return livros;

    }

    // Simular histórico de empréstimos
    public static List<Emprestimo> getEmprestimosMock(int idUsuario) {
        List<Emprestimo> emprestimos = new ArrayList<>();

        // Datas para os empréstimos
        Calendar cal = Calendar.getInstance();

        // Primeiro empréstimo (ativo)
        Emprestimo emp1 = new Emprestimo();
        emp1.setId(1);
        emp1.setIdUsuario(idUsuario);
        emp1.setIdLivro(1);

        cal.add(Calendar.DAY_OF_MONTH, -15); // 15 dias atrás
        emp1.setDataEmprestimo(cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, 30); // 15 dias a partir de hoje
        emp1.setDataDevolucaoPrevista(cal.getTime());

        emp1.setStatus("ATIVO");

        // Segundo empréstimo (devolvido)
        Emprestimo emp2 = new Emprestimo();
        emp2.setId(2);
        emp2.setIdUsuario(idUsuario);
        emp2.setIdLivro(3);

        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2); // 2 meses atrás
        emp2.setDataEmprestimo(cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, 15); // 15 dias após o empréstimo
        emp2.setDataDevolucaoPrevista(cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, -2); // Devolvido 2 dias antes do prazo
        emp2.setDataDevolucaoEfetiva(cal.getTime());

        emp2.setStatus("DEVOLVIDO");

        emprestimos.add(emp1);
        emprestimos.add(emp2);

        return emprestimos;
    }

    // Simular boletos mensais
    public static List<Boleto> getBoletosMock(int idUsuario) {
        List<Boleto> boletos = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        int anoAtual = cal.get(Calendar.YEAR);
        int mesAtual = cal.get(Calendar.MONTH) + 1; // Janeiro é 0

        // Gerar boletos para os últimos 6 meses
        for (int i = 0; i < 6; i++) {
            int mes = mesAtual - i;
            int ano = anoAtual;

            // Ajustar para meses anteriores ao ano
            if (mes <= 0) {
                mes += 12;
                ano--;
            }

            Boleto boleto = new Boleto();
            boleto.setId(i + 1);
            boleto.setIdUsuario(idUsuario);
            boleto.setValor(1250.0); // Valor fixo para exemplo

            // Definir data de vencimento (dia 10 de cada mês)
            cal = Calendar.getInstance();
            cal.set(ano, mes - 1, 10);
            boleto.setDataVencimento(cal.getTime());

            // Definir código de barras aleatório
            boleto.setCodigoBarras("34191" + random.nextInt(100000000) + "12345" + random.nextInt(100000000));

            boleto.setMes(mes);
            boleto.setAno(ano);

            // Boletos de meses anteriores já pagos, mês atual pendente
            if (i > 0) {
                boleto.setStatus("PAGO");

                // Data de pagamento (2 dias antes do vencimento)
                cal.add(Calendar.DAY_OF_MONTH, -2);
                boleto.setDataPagamento(cal.getTime());
            } else {
                boleto.setStatus("PENDENTE");
                boleto.setDataPagamento(null);
            }

            boletos.add(boleto);
        }

        return boletos;
    }

    // Simular pagamento de boleto
    public static Boleto pagarBoletoMock(int idBoleto) {
        // Encontrar boleto na lista mock
        List<Boleto> boletos = getBoletosMock(1); // Simulação com id fixo

        for (Boleto boleto : boletos) {
            if (boleto.getId() == idBoleto) {
                // Atualizar status do boleto
                boleto.setStatus("PAGO");
                boleto.setDataPagamento(new Date());
                return boleto;
            }
        }

        return null;
    }
}