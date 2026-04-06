package stardew.exceptions;

/**
 * Lançada quando o jogador não tem ouro suficiente para uma compra.
 */
public class SaldoInsuficienteException extends FazendaException {

    private final double saldoAtual;
    private final double valorNecessario;

    public SaldoInsuficienteException(double saldoAtual, double valorNecessario) {
        super(String.format(
            "Saldo insuficiente! Você tem G%.0f mas precisa de G%.0f.",
            saldoAtual, valorNecessario
        ));
        this.saldoAtual = saldoAtual;
        this.valorNecessario = valorNecessario;
    }

    public double getSaldoAtual()      { return saldoAtual; }
    public double getValorNecessario() { return valorNecessario; }
}
