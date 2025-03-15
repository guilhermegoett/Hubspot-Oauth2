# Integra��o com HubSpot - API REST

Este projeto implementa uma API REST em **Java com Spring Boot** para integrar com o HubSpot. Ele inclui autentica��o via OAuth 2.0, cria��o de contatos e recep��o de eventos via Webhook.

---

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot**
- **Spring Security OAuth2**
- **Lombok**
- **SLF4J (Log)**
- **RestTemplate** para chamadas HTTP
- **Jackson** para manipula��o de JSON

---

## Configura��o e Execu��o do Projeto

### 1 Clonar o Reposit�rio

```bash
git clone https://github.com/guilhermegoett/Hubspot-Oauth2
cd seu-repositorio
```

### 2 Configurar as Credenciais do HubSpot

No arquivo `application.properties`, configure as credenciais de autentica��o:

```properties
clientid=SEU_CLIENT_ID
clientsecret=SEU_CLIENT_SECRET
redirecturi=http://localhost:8080/hubspot/callback
```

### 3 Executar o Projeto

```sh
mvn clean install
mvn spring-boot:run
```

Ou, se estiver usando um IDE, rode a classe `DemoApplication.java`.

---

## Endpoints Dispon�veis

### Autoriza��o OAuth2 (Etapa feita pelo navegador)

- **GET** `/hubspot/authorize` ? Redireciona para a tela de login do HubSpot.
- **GET** `/hubspot/callback?code=...` ? Recebe o token de acesso.

### Opera��es com Contatos (Etapa feita no postman)

- **GET** `/hubspot/contatos` ? Lista contatos.
- **POST** `/hubspot/criarcontato` ? Cria um novo contato.

### Webhook

- **POST** `/hubspot/webhook/contact_creation` ? Recebe eventos de cria��o de contato do HubSpot.

---

## Como Funciona o Webhook?

1. No HubSpot, configure um webhook para o evento **contact.creation**.
2. A URL de callback deve ser `http://localhost:8080/hubspot/webhook/contact_creation`.
3. O endpoint recebe o evento e processa os dados do contato.

---

### Melhorias

Testar webhook e continuar seu desenvolvimento.
Dar refesh no token quando acabar o seu tempo corrente.
Melhorar tratamento de erros.
Dividir melhor o projeto e encapsular mais os objetos.

## Licen�a

Este projeto � distribu�do sob a licen�a MIT. Veja `LICENSE` para mais informa��es.

---

## Contato

Caso tenha d�vidas ou sugest�es, entre em contato pelo e-mail **[guilhermegoet@gmail.com](mailto\guilhermegoet@gmail.com)**.

