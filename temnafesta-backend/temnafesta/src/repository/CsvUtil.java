package repository;

import exception.CsvException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASSE UTILITÁRIA — CsvUtil
 *
 * POO aplicado:
 * - Encapsulamento: métodos estáticos utilitários isolados
 * - Reutilização: todos os Repositories usam esta classe
 * - Persistência: usa FileReader, BufferedReader, FileWriter, BufferedWriter
 *
 * Responsável pelas operações de I/O em arquivos .csv.
 * Linha 1 de cada arquivo = cabeçalho (ignorado na leitura de dados).
 */
public class CsvUtil {

    // Separador padrão do CSV
    public static final String SEPARADOR = ";";

    // Diretório onde os CSVs serão armazenados
    public static final String DIRETORIO = "data/";

    // Construtor privado — classe utilitária não deve ser instanciada
    private CsvUtil() {}

    /**
     * Lê todas as linhas de dados de um arquivo CSV.
     * Ignora a linha de cabeçalho (primeira linha).
     * Ignora linhas em branco.
     *
     * @param caminhoArquivo Caminho completo do arquivo CSV
     * @return Lista de strings, uma por linha de dados
     * @throws CsvException se houver erro de leitura
     */
    public static List<String> lerLinhas(String caminhoArquivo) throws CsvException {
        List<String> linhas = new ArrayList<>();
        File arquivo = new File(caminhoArquivo);

        // Se o arquivo não existe, retorna lista vazia (sem dados ainda)
        if (!arquivo.exists()) {
            return linhas;
        }

        // FileReader + BufferedReader — conforme requisito do projeto
        try (FileReader fr = new FileReader(arquivo);
             BufferedReader br = new BufferedReader(fr)) {

            String linha;
            boolean primeiraLinha = true;

            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false; // Pula o cabeçalho
                    continue;
                }
                if (!linha.isBlank()) {
                    linhas.add(linha);
                }
            }

        } catch (IOException e) {
            throw new CsvException(caminhoArquivo, "Erro ao ler arquivo: " + e.getMessage());
        }

        return linhas;
    }

    /**
     * Escreve todas as linhas no arquivo CSV, sobrescrevendo o conteúdo anterior.
     * A primeira linha será sempre o cabeçalho informado.
     *
     * @param caminhoArquivo Caminho completo do arquivo CSV
     * @param cabecalho      Linha de cabeçalho (ex: "id;nome;email")
     * @param linhas         Lista de linhas de dados já formatadas (toCsvLine())
     * @throws CsvException se houver erro de escrita
     */
    public static void escreverLinhas(String caminhoArquivo, String cabecalho,
                                       List<String> linhas) throws CsvException {
        // Garante que o diretório existe
        File dir = new File(DIRETORIO);
        if (!dir.exists()) dir.mkdirs();

        try (FileWriter fw = new FileWriter(caminhoArquivo, false); // false = sobrescreve
             BufferedWriter bw = new BufferedWriter(fw)) {

            bw.write(cabecalho);
            bw.newLine();

            for (String linha : linhas) {
                bw.write(linha);
                bw.newLine();
            }

        } catch (IOException e) {
            throw new CsvException(caminhoArquivo, "Erro ao escrever arquivo: " + e.getMessage());
        }
    }

    /**
     * Gera o próximo ID disponível a partir da lista de linhas existentes.
     * Estratégia: lê o maior id encontrado e retorna id+1.
     * Coluna 0 de cada linha é sempre o id.
     */
    public static int proximoId(List<String> linhas) {
        int maiorId = 0;
        for (String linha : linhas) {
            try {
                int id = Integer.parseInt(linha.split(SEPARADOR)[0]);
                if (id > maiorId) maiorId = id;
            } catch (NumberFormatException ignored) {}
        }
        return maiorId + 1;
    }
}
