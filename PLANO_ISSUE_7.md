# Issue 7 — Verificação de e-mail para professor e funcionário

## Resumo

Criar o fluxo de verificação por e-mail em etapas separadas, preservando o fluxo atual de alunos.

A implementação assumirá uma conta Gmail dedicada e gratuita como remetente, integrada via Gmail API OAuth2. O uso padrão da API não possui custo adicional dentro das quotas oficiais ([Gmail API](https://developers.google.com/workspace/gmail/api/reference/quota)). A integração ficará abstraída para futura troca por Gmail institucional ou SMTP Relay.

A branch planejada será `feature/issue-7-email-verification`, criada a partir de `origin/main`.

## Alterações principais

- Criar entidade/migration própria para verificações de e-mail, separada de `VALIDACAO` e de `AVALIACAO`.
- Persistir apenas:
  - chave técnica numérica;
  - digest do e-mail para rate limit e vínculo;
  - digest irreversível do PIN;
  - público, campanha/formulário/versão;
  - expiração, tentativas, reenvios, estado e data de consumo.
- Nunca persistir PIN ou e-mail junto às respostas.
- Gerar PIN alfanumérico aleatório de 16 caracteres usando `SecureRandom`.
- Expiração configurável por ambiente, com padrão de 2 horas.
- Reenvio invalida atomicamente a verificação anterior.
- Consumo do PIN protegido por lock/transação para impedir replay e duplicidade.
- Aplicar limites persistidos no PostgreSQL para tentativas e reenvios.
- Usar mensagens e respostas indistinguíveis para evitar enumeração de contas.
- Não incluir PIN, e-mail ou token em logs, URLs, telemetria ou respostas de confirmação.

## API e integração

Criar endpoints públicos separados para:

- solicitar ou reenviar PIN;
- confirmar PIN;
- utilizar uma referência opaca de verificação confirmada no envio do formulário.

A confirmação deverá produzir uma autorização temporária vinculada ao público e ao formulário. O `FormularioService` deverá validar essa autorização antes de persistir qualquer resposta de professor ou funcionário.

Regras de negócio:

- `ALUNO`: mantém o fluxo atual sem verificação por e-mail.
- `PROFESSOR` e `FUNCIONARIO`: exigem verificação concluída.
- Outros públicos não devem receber automaticamente o novo fluxo sem regra explícita.
- E-mail malformado ou externo é rejeitado.
- A validação do domínio `@femass.edu.br` não será tratada como prova de identidade.
- O PIN confirmado será de uso único.

Criar uma interface interna `EmailSender` e uma implementação `GmailApiEmailSender`, usando OAuth2 e credenciais exclusivamente por variáveis/segredos de ambiente. A configuração deverá incluir remetente, client ID, client secret, refresh token e parâmetros de timeout.

Em testes, o sender será mockado; não será necessário enviar e-mails reais.

## Testes e aceite

Cobrir:

- aluno sem exigência de e-mail;
- professor/funcionário sem verificação;
- e-mail inválido ou externo;
- solicitação e reenvio;
- invalidação do PIN anterior;
- PIN com 16 caracteres;
- digest não reversível no banco;
- expiração após 2 horas;
- confirmação correta;
- replay e confirmação duplicada;
- limite de tentativas;
- limite de reenvios;
- concorrência no consumo atômico;
- falha do provedor de e-mail;
- mensagens anti-enumeração;
- ausência de dados sensíveis em logs/respostas;
- separação entre dados de verificação e respostas anônimas;
- integração mockada com o `FormularioService`;
- preservação dos testes existentes de QR Code e alunos.

Executar a suíte Maven e validar as migrations em PostgreSQL antes de considerar a branch pronta.

## Assumptions

- Será usada uma conta Gmail dedicada, não necessariamente `@femass.edu.br`.
- O primeiro fluxo será síncrono: o backend gera o PIN, persiste o digest, envia o e-mail e retorna sucesso genérico.
- O envio ficará encapsulado para permitir migração futura para Gmail institucional ou SMTP Relay, cuja configuração exige autorização administrativa ([SMTP Relay](https://support.google.com/a/answer/2956491)).
- A branch deverá ser criada a partir de `origin/main`.
