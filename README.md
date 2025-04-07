# Integração com HubSpot - API REST

Este projeto implementa uma API REST em **Java com Spring Boot** para integrar com o HubSpot. Ele inclui autenticação via OAuth 2.0, criação de contatos e recepção de eventos via Webhook.

---

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot**
- **Spring Security OAuth2**
- **Lombok**
- **SLF4J (Log)**
- **RestTemplate** para chamadas HTTP
- **Jackson** para manipulação de JSON

---

## Configuração e Execução do Projeto

### 1 Clonar o Repositório

```bash
git clone https://github.com/guilhermegoett/Hubspot-Oauth2
cd seu-repositorio
```

### 2 Configurar as Credenciais do HubSpot

No arquivo `application.properties`, configure as credenciais de autenticação:

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

## Endpoints Disponiveis

### Autorização OAuth2 (Etapa feita pelo navegador)

- **GET** `/hubspot/authorize` ? Redireciona para a tela de login do HubSpot.
- **GET** `/hubspot/callback?code=...` ? Recebe o token de acesso.

### Operações com Contatos (Etapa feita no postman)

- **GET** `/hubspot/contatos` ? Lista contatos.
- **POST** `/hubspot/criarcontato` ? Cria um novo contato.

### Webhook

- **POST** `/hubspot/webhook/contact_creation` ? Recebe eventos de criação de contato do HubSpot.

---

## Como Funciona o Webhook?

1. No HubSpot, configure um webhook para o evento **contact.creation**.
2. A URL de callback deve ser `http://localhost:8080/hubspot/webhook/contact_creation`.
3. O endpoint recebe o evento e processa os dados do contato.

---

### Passo a passo para testar todos os end-points que est?o funcionando

1. Rogar o end-point abaixo no navegador:

http://localhost:8080/hubspot/authorize

2. Escolher a conta chamada **Conta de teste do desenvolvedor 1**

3. Instalar o aplicativo, dando tudo certo na tela ir? aparecer "Access Token armazenado com sucesso!".

4. No navegador rode a url abaixo que ir? listar todos os contatos cadastrados:

 http://localhost:8080/hubspot/contatos
 
5. No postman configure uma chamada POST com a url abaixo:

http://localhost:8080/hubspot/criarcontato

Passando Content-Type:application/json no header.
No corpo da requisicao coloque o seguinte Json de exemplo:

{
    "associations": [
        {
            "types": [
                {
                    "associationCategory": "HUBSPOT_DEFINED",
                    "associationTypeId": 1
                }
            ],
            "to": {
                "id": "31064433528"
            }
        }
    ],
    "objectWriteTraceId": "trace-id-124",
    "properties": {
        "email": "contato.s@empresateste.industries",
        "lastname": "S.",
        "firstname": "Contato"
    }
}

Se obtiver sucesso o status de retorno http ser? 201 Created.
obs: Para enviar mais contatos é só alterar o email que é enviado.

---

### Melhorias

Testar webhook e continuar seu desenvolvimento.
Dar refesh no token quando acabar o seu tempo corrente.
Melhorar tratamento de erros.
Dividir melhor o projeto e encapsular mais os objetos.
Telas de login e para cadastrar contatos.

---

## Licença

Este projeto é distribuído sob a licença MIT. Veja `LICENSE` para mais informações.

---

## Contato

Caso tenha dúvidas ou sugestões, entre em contato pelo e-mail **[guilhermegoet@gmail.com](mailto:guilhermegoet@gmail.com)**.

