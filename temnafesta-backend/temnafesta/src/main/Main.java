package main;

import enums.*;
import exception.*;
import model.*;
import repository.*;
import service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MAIN — Ponto de entrada da aplicação
 *
 * Demonstra todos os conceitos de POO implementados:
 * ✅ Classes e Objetos
 * ✅ Construtores (padrão, principal, reconstrução CSV)
 * ✅ Encapsulamento (atributos privados, métodos de acesso)
 * ✅ Herança (EntidadeBase → todos os models)
 * ✅ Classe Abstrata (EntidadeBase)
 * ✅ Interface (Persistivel, Notificavel, Validavel)
 * ✅ Polimorfismo de classe (Notificavel tratado genericamente)
 * ✅ Polimorfismo de método (sobrecarga e sobrescrita)
 * ✅ Enum (StatusProducao, CanalOrigem, MetodoPagamento, PerfilUsuario)
 * ✅ Relacionamento de classes (composição, agregação, associação)
 * ✅ Exceção Personalizada (ValidacaoException, CampanhaEncerradaException etc.)
 * ✅ Persistência CSV (FileReader + BufferedReader via CsvUtil)
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("=== INICIANDO SISTEMA TEM NA FESTA ===\n");

        // ----------------------------------------------------------------
        // INSTANCIANDO REPOSITORIES (Persistência CSV)
        // ----------------------------------------------------------------
        ClienteRepository clienteRepo     = new ClienteRepository();
        ProdutoRepository produtoRepo     = new ProdutoRepository();
        CampanhaRepository campanhaRepo   = new CampanhaRepository();
        PedidoRepository pedidoRepo       = new PedidoRepository();

        // ----------------------------------------------------------------
        // INSTANCIANDO SERVICES (Regras de negócio)
        // ----------------------------------------------------------------
        ClienteService clienteService     = new ClienteService(clienteRepo);
        ProdutoService produtoService     = new ProdutoService(produtoRepo);
        CampanhaService campanhaService   = new CampanhaService(campanhaRepo);
        PedidoService pedidoService       = new PedidoService(pedidoRepo, clienteRepo,
                                                               produtoRepo, campanhaRepo);
        DashboardService dashboardService = new DashboardService(pedidoRepo, campanhaRepo);

        // ----------------------------------------------------------------
        // 1. DEMONSTRANDO ENUM — PerfilUsuario
        // ----------------------------------------------------------------
        System.out.println("--- [ENUM] Perfis disponíveis ---");
        for (PerfilUsuario perfil : PerfilUsuario.values()) {
            System.out.println("  " + perfil.getDescricao() +
                               " | Gerencia usuários: " + perfil.isPodeGerenciarUsuarios());
        }

        // ----------------------------------------------------------------
        // 2. HERANÇA + CLASSE ABSTRATA — Perfil extends EntidadeBase
        // ----------------------------------------------------------------
        System.out.println("\n--- [HERANÇA] Criando Perfil (subclasse de EntidadeBase) ---");
        Perfil perfilAdmin = new Perfil(1, PerfilUsuario.ADMIN);
        System.out.println(perfilAdmin); // toString() de EntidadeBase + descricaoResumida() de Perfil

        // ----------------------------------------------------------------
        // 3. ENCAPSULAMENTO — Cliente com validação
        // ----------------------------------------------------------------
        System.out.println("\n--- [ENCAPSULAMENTO] Cadastrando Cliente ---");
        Endereco endereco = new Endereco("06220-000", "Rua das Flores", "123",
                                         "Apto 4", "Centro", "Osasco", "SP");
        Cliente cliente = clienteService.cadastrar(
            "Maria Silva", "11999990000", "11999990000",
            "@maria.silva", "Cliente fiel - Páscoa", endereco
        );
        System.out.println(cliente.descricaoResumida());

        // ----------------------------------------------------------------
        // 4. ENUM — StatusProducao com método isPosteriorA()
        // ----------------------------------------------------------------
        System.out.println("\n--- [ENUM] Verificando ordem de status ---");
        StatusProducao s1 = StatusProducao.AGUARDANDO_PAGAMENTO;
        StatusProducao s2 = StatusProducao.EM_PRODUCAO;
        System.out.println(s2 + " é posterior a " + s1 + "? " + s2.isPosteriorA(s1));

        // ----------------------------------------------------------------
        // 5. PERSISTÊNCIA CSV + PRODUTO
        // ----------------------------------------------------------------
        System.out.println("\n--- [CSV] Cadastrando Produtos ---");
        Produto ovo = produtoService.cadastrar(
            "Ovo de Páscoa 150g", "Chocolate ao leite recheado", new BigDecimal("35.00"), 50
        );
        Produto trufa = produtoService.cadastrar(
            "Trufas (caixa 12un)", "Sortidas", new BigDecimal("42.00"), 30
        );
        System.out.println(ovo.descricaoResumida());
        System.out.println(trufa.descricaoResumida());

        // ----------------------------------------------------------------
        // 6. CAMPANHA — Interface Validavel + Notificavel
        // ----------------------------------------------------------------
        System.out.println("\n--- [INTERFACE] Abrindo Campanha Páscoa 2025 ---");
        Campanha campanha = campanhaService.abrirCampanha(
            "Páscoa 2025",
            LocalDate.of(2025, 3, 1),
            LocalDate.of(2025, 4, 20),
            100
        );
        System.out.println(campanha.descricaoResumida());
        System.out.println("Link público: " + campanha.getLinkPublico());

        // ----------------------------------------------------------------
        // 7. POLIMORFISMO DE MÉTODO — sobrecarga em PedidoService
        // ----------------------------------------------------------------
        System.out.println("\n--- [POLIMORFISMO DE MÉTODO] Registrando Pedido ---");
        Map<Integer, Integer> itens = new HashMap<>();
        itens.put(ovo.getId(), 2);   // 2 ovos
        itens.put(trufa.getId(), 1); // 1 caixa de trufas

        Pedido pedido = pedidoService.registrarPedido(
            cliente.getId(),
            campanha.getId(),
            itens,
            LocalDateTime.now().plusDays(5),
            CanalOrigem.WHATSAPP,
            "Presente para a mãe",
            1 // usuarioId
        );
        System.out.println(pedido.descricaoResumida());

        // ----------------------------------------------------------------
        // 8. EXCEÇÃO PERSONALIZADA — Estoque insuficiente
        // ----------------------------------------------------------------
        System.out.println("\n--- [EXCEÇÃO] Tentando pedir mais do que o estoque ---");
        try {
            Map<Integer, Integer> itensDemais = new HashMap<>();
            itensDemais.put(trufa.getId(), 999); // Muito além do estoque
            pedidoService.registrarPedido(
                cliente.getId(), campanha.getId(), itensDemais,
                LocalDateTime.now().plusDays(3), CanalOrigem.INSTAGRAM, "", 1
            );
        } catch (EstoqueInsuficienteException e) {
            System.out.println("✅ Exceção capturada corretamente: " + e.getMessage());
        }

        // ----------------------------------------------------------------
        // 9. CONFIRMAÇÃO DE PAGAMENTO — Notificavel + geração de recibo
        // ----------------------------------------------------------------
        System.out.println("\n--- [NOTIFICAVEL] Confirmando pagamento e gerando recibo ---");
        String recibo = pedidoService.confirmarPagamento(
            pedido.getId(), MetodoPagamento.PIX, 1
        );
        System.out.println(recibo);

        // ----------------------------------------------------------------
        // 10. POLIMORFISMO DE CLASSE — DashboardService usa List<Notificavel>
        // ----------------------------------------------------------------
        System.out.println("\n--- [POLIMORFISMO DE CLASSE] Coletando alertas do sistema ---");
        List<String> alertas = dashboardService.getAlertasSistema();
        System.out.println("Total de alertas: " + alertas.size());

        // ----------------------------------------------------------------
        // 11. EXCEÇÃO — Campanha encerrada
        // ----------------------------------------------------------------
        System.out.println("\n--- [EXCEÇÃO] Tentando abrir segunda campanha ---");
        try {
            campanhaService.abrirCampanha("Halloween 2025",
                LocalDate.of(2025, 10, 1), LocalDate.of(2025, 10, 31), 50);
        } catch (ValidacaoException e) {
            System.out.println("✅ Exceção capturada corretamente: " + e.getMessage());
        }

        // ----------------------------------------------------------------
        // 12. DASHBOARD — KPIs e métricas
        // ----------------------------------------------------------------
        System.out.println();
        dashboardService.exibirResumo();

        // ----------------------------------------------------------------
        // 13. POLIMORFISMO DE MÉTODO — toString() sobrescrito
        // ----------------------------------------------------------------
        System.out.println("\n--- [POLIMORFISMO] toString() polimórfico em EntidadeBase ---");
        EntidadeBase[] entidades = { cliente, ovo, campanha, pedido };
        for (EntidadeBase e : entidades) {
            // Cada entidade exibe seu próprio toString() — polimorfismo em ação
            System.out.println("→ [" + e.getClass().getSimpleName() + "] " + e.descricaoResumida());
        }

        System.out.println("\n=== SISTEMA FINALIZADO ===");
    }
}
