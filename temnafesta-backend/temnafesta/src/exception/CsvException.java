package exception;

/**
 * EXCEÇÃO PERSONALIZADA — CsvException
 *
 * POO aplicado:
 * - Herança: estende Exception (EXCEÇÃO VERIFICADA — obriga o tratamento com try/catch)
 * - Lançada pelos Repositories quando há falha de leitura/escrita no CSV
 * - Diferente das demais (RuntimeException), esta é verificada pois falhas de I/O
 *   devem ser explicitamente tratadas pelo chamador
 */
public class CsvException extends Exception {

    private final String caminhoArquivo;
    private final int linhaProblematica;

    public CsvException(String caminhoArquivo, String mensagem) {
        super("Erro no CSV [" + caminhoArquivo + "]: " + mensagem);
        this.caminhoArquivo = caminhoArquivo;
        this.linhaProblematica = -1;
    }

    public CsvException(String caminhoArquivo, int linha, String mensagem) {
        super("Erro no CSV [" + caminhoArquivo + "] na linha " + linha + ": " + mensagem);
        this.caminhoArquivo = caminhoArquivo;
        this.linhaProblematica = linha;
    }

    public String getCaminhoArquivo() { return caminhoArquivo; }
    public int getLinhaProblematica() { return linhaProblematica; }
}
