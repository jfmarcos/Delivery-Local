# Notas de Versão - Delivery Local 🛵

### Versão 1.1 (Código da Versão: 2)
**Data:** Junho de 2026  
**Status do Build:** Compilado com Sucesso (.APK e .AAB gerados)  
**ID do Pacote:** `com.aistudio.deliverylocal.uwtvcb`

---

## 🌟 O que há de novo nesta versão?

1. **Nova Identidade Visual**
   - **Ícone do Aplicativo Personalizado**: Substituição do ícone padrão do Android por um novo design circular minimalista estilo vetor flat em tom verde sálvia (`ic_delivery_icon.png`), perfeitamente alinhado com a proposta de culinária saudável e entregas rápidas.
   - **Banner Hero de Delivery**: Implementação de um belo banner decorativo no topo da tela inicial destacando: *"Seu Delivery Local Favorito 🛵 - Produtos orgânicos e lanches rápidos fresquinhos!"*.

2. **Segurança e Privacidade do Usuário (LGPD)**
   - **Política de Privacidade Integrada**: Desenvolvimento de um diálogo interativo e acessível de Política de Privacidade (clicando no botão "i" no cabeçalho).
   - Explicação transparente sobre o fluxo de dados: confirmação de que toda a persistência de pedidos e histórico de entregas é **100% local e offline**, armazenada unicamente usando o banco de dados **Room** do próprio dispositivo do usuário.

3. **Correções de Compatibilidade e Publicação**
   - Mudança para um `applicationId` exclusivo (`com.aistudio.deliverylocal.pxmrqv`) para resolver o conflito de pacotes no Google Play Console.
   - Incremento correto para `versionCode = 2` e `versionName = "1.1"`.
   - Compilação limpa do executável **APK** e do pacote de entrega oficial do Google Play **AAB** (Android App Bundle).

---

## 📝 Texto para o Google Play Console (Novidades / Whats New)
*Copie e cole este texto no campo de "Novidades" (whatsnew) na área interna de publicação do Play Console:*

```text
🛵 Novidades no Delivery Local v1.1!
• Novo visual elegante com ícone personalizado de scooter de entrega e banner hero moderno!
• Adicionada a seção de Política de Privacidade de fácil consulta garantindo conformidade com a LGPD.
• Seus dados estão mais protegidos do que nunca: todo o histórico de compras e rotas de entrega é salvo apenas localmente (usando banco de dados Room) no seu próprio aparelho Android.
• Otimizações de desempenho e estabilidade nas listagens e fechamento do pedido.
```
