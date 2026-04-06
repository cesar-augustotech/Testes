package stardew.engine;

import stardew.engine.PersistenciaCSV;
import stardew.enums.Localizacao;
import stardew.exceptions.FazendaException;
import stardew.interfaces.Item;
import stardew.model.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class GameGUI extends JFrame {

    private Fazendeiro fazendeiro;
    private GerenciadorTempo tempo;
    private final PersistenciaCSV persistencia;

    private NPC pierre;
    private NPC willy;

    // Componentes Visuais
    private JLabel lblHud;
    private JTextArea txtLog;
    private JPanel painelCentral;
    private JPanel painelAcoes;

    public GameGUI() {
        this.persistencia = new PersistenciaCSV();
        this.pierre = new NPC("Pierre", "Lojista", Localizacao.CIDADE);
        this.willy = new NPC("Willy", "Pescador", Localizacao.PRAIA);

        configurarJanela();
        iniciarJogo();
    }

    private void configurarJanela() {
        setTitle("Stardew Valley - Swing Edition");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        // HUD (Topo)
        JPanel painelTopo = new JPanel();
        painelTopo.setBackground(new Color(227, 213, 184));
        lblHud = new JLabel();
        lblHud.setFont(new Font("Arial", Font.BOLD, 16));
        painelTopo.add(lblHud);
        add(painelTopo, BorderLayout.NORTH);

        // Log de Eventos (Base)
        txtLog = new JTextArea(6, 50);
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("Diário de Ações"));
        add(scrollLog, BorderLayout.SOUTH);

        // Menu Lateral Esquerdo (Mapa e Sistema)
        JPanel painelMenu = new JPanel(new GridLayout(8, 1, 5, 5));
        painelMenu.setBorder(BorderFactory.createTitledBorder("Mapa & Sistema"));
        painelMenu.setPreferredSize(new Dimension(200, 0));

        painelMenu.add(criarBotaoMapa("Ir p/ Fazenda", Localizacao.FAZENDA));
        painelMenu.add(criarBotaoMapa("Ir p/ Cidade", Localizacao.CIDADE));
        painelMenu.add(criarBotaoMapa("Ir p/ Praia", Localizacao.PRAIA));
        painelMenu.add(criarBotaoMapa("Ir p/ Minas", Localizacao.MINAS));
        painelMenu.add(criarBotaoMapa("Ir p/ Floresta", Localizacao.FLORESTA));

        painelMenu.add(new JSeparator());

        JButton btnInventario = new JButton("Ver Inventário");
        btnInventario.addActionListener(e -> exibirInventario());
        painelMenu.add(btnInventario);

        JButton btnDormir = new JButton("Dormir (Salvar)");
        btnDormir.addActionListener(e -> dormir());
        painelMenu.add(btnDormir);

        add(painelMenu, BorderLayout.WEST);

        // Painel Central Dinâmico (Muda conforme o local)
        painelCentral = new JPanel(new BorderLayout());
        painelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(painelCentral, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════════
    //  INICIALIZAÇÃO
    // ══════════════════════════════════════════════════════════════

    private void iniciarJogo() {
        if (persistencia.saveExiste()) {
            int opcao = JOptionPane.showConfirmDialog(this,
                    "Save encontrado. Deseja continuar o jogo salvo?",
                    "Iniciar Jogo", JOptionPane.YES_NO_OPTION);

            if (opcao == JOptionPane.YES_OPTION) {
                carregarJogo();
            } else {
                novoJogo();
            }
        } else {
            novoJogo();
        }
        log("Bem-vindo(a) de volta, " + fazendeiro.getNome() + "!");
        atualizarInterface();
    }

    private void novoJogo() {
        String nome = JOptionPane.showInputDialog(this, "Qual o seu nome?", "Novo Jogador");
        if (nome == null || nome.isBlank()) nome = "Jogador";

        String fazenda = JOptionPane.showInputDialog(this, "Nome da fazenda?", "Nova Fazenda");
        if (fazenda == null || fazenda.isBlank()) fazenda = "Fazenda do Vale";

        fazendeiro = new Fazendeiro(nome, fazenda);
        tempo = new GerenciadorTempo();
    }

    private void carregarJogo() {
        try {
            tempo = persistencia.carregarTempo();
            Inventario inv = persistencia.carregarInventario();
            Fazenda fazenda = persistencia.carregarFazenda();
            fazendeiro = persistencia.carregarFazendeiro(inv, fazenda);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar save. Iniciando novo jogo.");
            novoJogo();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LÓGICA DE INTERFACE DINÂMICA
    // ══════════════════════════════════════════════════════════════

    private void atualizarInterface() {
        // Verifica se deu a hora de dormir forçado
        if (tempo.isDormindo()) {
            log("Você desmaiou de exaustão! O dia acabou.");
            dormir();
            return;
        }

        // Atualiza HUD
        lblHud.setText(String.format("📍 %s  |  🕒 %s  |  ⚡ Energia: %d/270  |  💰 G%.0f",
                fazendeiro.getLocalizacao(), tempo.toString(), fazendeiro.getEnergia(), fazendeiro.getDinheiro()));

        // Atualiza Centro
        painelCentral.removeAll();
        Localizacao local = fazendeiro.getLocalizacao();

        if (local == Localizacao.FAZENDA) {
            montarPainelFazenda();
        } else {
            montarPainelGenerico(local);
        }

        painelCentral.revalidate();
        painelCentral.repaint();
    }

    private JButton criarBotaoMapa(String texto, Localizacao destino) {
        JButton btn = new JButton(texto);
        btn.addActionListener(e -> {
            if (fazendeiro.getLocalizacao() == destino) {
                log("Você já está aqui!");
                return;
            }
            fazendeiro.moverPara(destino);
            avancarTempo(1);
            log("Você foi para " + destino + ".");
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════════
    //  PAINÉIS DE LOCALIZAÇÃO
    // ══════════════════════════════════════════════════════════════

    private void montarPainelFazenda() {
        JPanel painelGrelha = new JPanel(new GridLayout(3, 4, 10, 10));

        for (Parcela p : fazendeiro.getFazenda().getParcelas()) {
            JButton btnLote = new JButton();
            btnLote.setOpaque(true);

            if (p.estaLivre()) {
                btnLote.setText("Lote #" + p.getId() + " (Livre)");
                btnLote.setBackground(new Color(139, 69, 19));
                btnLote.setForeground(Color.WHITE);
            } else {
                String texto = "<html><center>" + p.getSemente().getCulturaGerada() +
                        "<br>(" + p.getDiasPlantado() + " dias)</center></html>";
                if (p.estaProxima()) {
                    btnLote.setBackground(new Color(50, 205, 50));
                    btnLote.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                } else {
                    btnLote.setBackground(new Color(34, 139, 34));
                    btnLote.setForeground(Color.WHITE);
                }
                btnLote.setText(texto);
            }

            if (p.isIrrigada()) {
                btnLote.setBorder(BorderFactory.createLineBorder(Color.CYAN, 4));
            }

            btnLote.addActionListener(e -> interagirComParcela(p));
            painelGrelha.add(btnLote);
        }

        painelCentral.add(painelGrelha, BorderLayout.CENTER);

        JButton btnAnimais = new JButton("Cuidar dos Animais");
        btnAnimais.addActionListener(e -> executarAcao(() -> fazendeiro.cuidarAnimais()));
        painelCentral.add(btnAnimais, BorderLayout.SOUTH);
    }

    private void montarPainelGenerico(Localizacao local) {
        JPanel painelAcoes = new JPanel(new FlowLayout());
        JLabel lblImagem = new JLabel("Você está em: " + local, SwingConstants.CENTER);
        lblImagem.setFont(new Font("Arial", Font.BOLD, 24));
        painelCentral.add(lblImagem, BorderLayout.CENTER);

        switch (local) {
            case CIDADE -> {
                JButton btnComprar = new JButton("Comprar (Pierre)");
                btnComprar.addActionListener(e -> menuComprar());
                JButton btnVender = new JButton("Vender Itens");
                btnVender.addActionListener(e -> menuVender());
                JButton btnFalar = new JButton("Falar com Pierre");
                btnFalar.addActionListener(e -> log(pierre.agir()));
                painelAcoes.add(btnComprar); painelAcoes.add(btnVender); painelAcoes.add(btnFalar);
            }
            case PRAIA -> {
                JButton btnPescar = new JButton("Pescar");
                btnPescar.addActionListener(e -> executarAcao(() -> fazendeiro.pescar()));
                JButton btnFalar = new JButton("Falar com Willy");
                btnFalar.addActionListener(e -> log(willy.agir()));
                painelAcoes.add(btnPescar); painelAcoes.add(btnFalar);
            }
            case MINAS -> {
                JButton btnMinerar = new JButton("Minerar");
                btnMinerar.addActionListener(e -> executarAcao(() -> fazendeiro.minerar()));
                JButton btnLutar = new JButton("Lutar contra Monstro");
                btnLutar.addActionListener(e -> menuLutar()); // Lógica simplificada de combate
                painelAcoes.add(btnMinerar); painelAcoes.add(btnLutar);
            }
            case FLORESTA -> {
                JButton btnCortar = new JButton("Cortar Árvores");
                btnCortar.addActionListener(e -> executarAcao(() -> fazendeiro.cortarArvore()));
                painelAcoes.add(btnCortar);
            }
        }
        painelCentral.add(painelAcoes, BorderLayout.SOUTH);
    }

    // ══════════════════════════════════════════════════════════════
    //  AÇÕES ESPECÍFICAS
    // ══════════════════════════════════════════════════════════════

    private void interagirComParcela(Parcela p) {
        if (p.estaLivre()) {
            List<Item> sementes = fazendeiro.getInventario().listarPorTipo("Semente");
            if (sementes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Você não tem sementes!");
                return;
            }
            Semente escolhida = (Semente) JOptionPane.showInputDialog(this, "Escolha a semente:",
                    "Plantar", JOptionPane.QUESTION_MESSAGE, null, sementes.toArray(), sementes.get(0));

            if (escolhida != null) {
                executarAcao(() -> fazendeiro.plantar(escolhida, p.getId(), tempo.getEstacao()));
            }
        } else {
            if (p.estaProxima()) {
                executarAcao(() -> fazendeiro.colher(p.getId(), tempo.getEstacao()));
            } else if (!p.isIrrigada()) {
                executarAcao(() -> fazendeiro.irrigar(p.getId()));
            } else {
                log("Esta parcela já está irrigada e crescendo.");
            }
        }
    }

    private void menuComprar() {
        List<Item> estoque = pierre.getEstoque();
        Item escolhido = (Item) JOptionPane.showInputDialog(this, "O que deseja comprar?",
                "Loja do Pierre", JOptionPane.QUESTION_MESSAGE, null, estoque.toArray(), estoque.get(0));

        if (escolhido != null) {
            try {
                fazendeiro.comprar(escolhido);
                if (escolhido instanceof Animal animal) {
                    fazendeiro.getFazenda().adicionarAnimal(animal);
                    fazendeiro.getInventario().removerItem(animal);
                    log("Comprou animal para a fazenda: " + animal.getNome());
                } else {
                    log("Comprou: " + escolhido.getNome() + " por G" + escolhido.getValor());
                }
                atualizarInterface();
            } catch (FazendaException ex) {
                log("Erro: " + ex.getMessage());
            }
        }
    }

    private void menuVender() {
        List<Item> vendaveis = fazendeiro.getInventario().getItens().stream()
                .filter(i -> !i.getTipo().equals("Ferramenta")).toList();

        if (vendaveis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seu inventário não possui itens vendáveis.");
            return;
        }

        Item escolhido = (Item) JOptionPane.showInputDialog(this, "O que deseja vender?",
                "Vender Item", JOptionPane.QUESTION_MESSAGE, null, vendaveis.toArray(), vendaveis.get(0));

        if (escolhido != null) {
            executarAcaoSemTempo(() -> fazendeiro.vender(escolhido.getNome()));
        }
    }

    private void menuLutar() {
        Inimigo inimigo = new Inimigo("Monstro da Caverna", 40, 8, 30);
        log("Um " + inimigo.getNome() + " apareceu!");
        executarAcao(() -> fazendeiro.lutar(inimigo));
    }

    private void dormir() {
        log("Zzz... Dormindo para o próximo dia.");
        fazendeiro.descansar();
        fazendeiro.getFazenda().avancarDia(tempo.getEstacao());
        tempo.avancarDia();

        try {
            persistencia.salvarTudo(fazendeiro, tempo);
            log("Jogo salvo automaticamente!");
        } catch (IOException ex) {
            log("Erro ao salvar o jogo: " + ex.getMessage());
        }

        long prontas = fazendeiro.getFazenda().parcelasProximasDeColher().count();
        if (prontas > 0) log("AVISO: Você tem " + prontas + " parcela(s) pronta(s) para colher!");

        atualizarInterface();
    }

    private void exibirInventario() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ouro: G").append(fazendeiro.getDinheiro()).append("\n\n");
        fazendeiro.getInventario().getItens().forEach(i -> sb.append("- ").append(i.getNome()).append("\n"));
        JOptionPane.showMessageDialog(this, sb.toString(), "Inventário", JOptionPane.INFORMATION_MESSAGE);
    }

    // ══════════════════════════════════════════════════════════════
    //  UTILITÁRIOS DE EXECUÇÃO
    // ══════════════════════════════════════════════════════════════

    @FunctionalInterface
    interface AcaoFazenda { String executar() throws FazendaException; }

    private void executarAcao(AcaoFazenda acao) {
        try {
            String resultado = acao.executar();
            log(resultado);
            avancarTempo(1);
        } catch (FazendaException ex) {
            log("Ops: " + ex.getMessage());
        }
    }

    private void executarAcaoSemTempo(AcaoFazenda acao) {
        try {
            String resultado = acao.executar();
            log(resultado);
            atualizarInterface();
        } catch (FazendaException ex) {
            log("Ops: " + ex.getMessage());
        }
    }

    private void avancarTempo(int horas) {
        tempo.avancarHora(horas);
        atualizarInterface();
    }

    private void log(String msg) {
        txtLog.append(msg + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength()); // Rola para o final
    }
}