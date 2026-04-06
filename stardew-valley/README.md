# Stardew Farm Simulator — Java 17

Projeto acadêmico de Orientação a Objetos em Java, inspirado em Stardew Valley.

---

## Como executar

### Linux / macOS
```bash
chmod +x compilar.sh
./compilar.sh
```

### Windows
```
compilar.bat
```

### Manual (qualquer SO)
```bash
# Compilar
find src -name "*.java" | xargs javac -d out -source 17 -target 17
# Executar
java -cp out stardew.Main
```

---

## Estrutura de pacotes

```
src/stardew/
├── Main.java                          ← Ponto de entrada
├── enums/
│   ├── Estacao.java                   ← PRIMAVERA, VERAO, OUTONO, INVERNO
│   └── Localizacao.java               ← FAZENDA, CIDADE, PRAIA, MINAS, FLORESTA
├── exceptions/
│   ├── FazendaException.java          ← Exceção base
│   ├── SaldoInsuficienteException.java
│   ├── ColheitaForaDeEstacaoException.java
│   └── AcaoInvalidaException.java
├── interfaces/
│   ├── Item.java                      ← Contrato de item
│   └── Coletavel.java                 ← Contrato de coleta
├── model/
│   ├── ItemBase.java                  ← Classe abstrata de item
│   ├── Semente.java
│   ├── Colheita.java
│   ├── Ferramenta.java
│   ├── RecursoSilvestre.java          ← Implementa Coletavel
│   ├── Habilidades.java               ← Pesca, Colheita, Lenhador, Minerador, Lutador
│   ├── Inventario.java                ← Usa Streams
│   ├── Parcela.java
│   ├── Animal.java
│   ├── Inimigo.java
│   ├── Fazenda.java                   ← Usa Streams
│   ├── GerenciadorTempo.java
│   ├── Personagem.java                ← Classe abstrata
│   ├── Fazendeiro.java                ← Herda Personagem
│   └── NPC.java                       ← Herda Personagem
├── persistence/
│   └── PersistenciaCSV.java           ← FileReader / BufferedReader
└── engine/
    └── GameEngine.java                ← Loop principal + UI ASCII
```

---

## Arquivos de save (gerados em `saves/`)

| Arquivo           | Conteúdo                                    |
|-------------------|---------------------------------------------|
| `fazendeiro.csv`  | Nome, dinheiro, energia, local, habilidades |
| `fazenda.csv`     | Parcelas (semente, dias) + animais          |
| `inventario.csv`  | Todos os itens do inventário                |
| `tempo.csv`       | Hora, dia, estação, ano                     |

---

## Conceitos de POO aplicados

| Conceito               | Onde aparece                                           |
|------------------------|--------------------------------------------------------|
| **Classes abstratas**  | `Personagem`, `ItemBase`                               |
| **Herança**            | `Fazendeiro` e `NPC` ← `Personagem` ; `Semente`, `Colheita`, `Ferramenta`, `RecursoSilvestre` ← `ItemBase` |
| **Polimorfismo**       | `agir()` em `Fazendeiro` vs `NPC`; `calcularPreco()` em `Colheita` |
| **Interfaces**         | `Item` (contrato de valor); `Coletavel` (coleta com local) |
| **Encapsulamento**     | Todos os atributos privados; getters/setters controlados |
| **Enum**               | `Estacao` (com método abstrato), `Localizacao`         |
| **Exceções**           | `FazendaException` ← `SaldoInsuficienteException`, `ColheitaForaDeEstacaoException`, `AcaoInvalidaException` |
| **Streams**            | `Inventario.filtrarPorTipo()`, `contagemPorTipo()`, `valorTotal()`; `Fazenda.parcelasProximasDeColher()`, `relatorio()` |
| **Persistência CSV**   | `PersistenciaCSV` com `FileReader`, `BufferedReader`, `FileWriter`, `BufferedWriter` |
| **Construtores**       | Sobrecarga para novo jogo vs. carregamento de save     |
| **Relacionamento**     | Composição (`Fazenda` → `Parcela`, `Animal`); Agregação (`Fazendeiro` → `Inventario`, `Habilidades`) |

---

## Gameplay

- **Estações**: 28 dias cada. Culturas específicas por estação.
- **Habilidades**: 5 habilidades com XP (0–100 por nível, máx. nível 10). Bônus real no resultado das ações.
- **Localização**: cada local tem ações exclusivas. Tentar uma ação no local errado lança `AcaoInvalidaException`.
- **Tempo**: o dia tem 20 horas úteis (6h–2h). Ao dormir, o dia avança e o jogo é salvo automaticamente.
- **Animais**: felicidade afeta qualidade e quantidade da produção.
- **Combate**: sistema de turnos nas Minas. Nível de Lutador aumenta o dano.
