package exception;

/**
 * EXCEÇÃO PERSONALIZADA — EstoqueInsuficienteException
 *
 * POO aplicado:
 * - Herança: estende RuntimeException
 * - Lançada quando a quantidade solicitada excede o estoque disponível do produto
 * - Regra de negócio: produto com estoque zerado é ocultado do cardápio público
 */
public class EstoqueInsuficienteException extends RuntimeException {

    private final String nomeProduto;
    private final int estoqueDisponivel;
    private final int quantidadeSolicitada;

    public EstoqueInsuficienteException(String nomeProduto, int estoqueDisponivel, int quantidadeSolicitada) {
        super("Estoque insuficiente para '" + nomeProduto + "'. " +
              "Disponível: " + estoqueDisponivel + " | Solicitado: " + quantidadeSolicitada);
        this.nomeProduto = nomeProduto;
        this.estoqueDisponivel = estoqueDisponivel;
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public String getNomeProduto() { return nomeProduto; }
    public int getEstoqueDisponivel() { return estoqueDisponivel; }
    public int getQuantidadeSolicitada() { return quantidadeSolicitada; }
}
