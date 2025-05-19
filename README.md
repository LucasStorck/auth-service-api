# Sistema de Registro e Autenticação de Usuários
Sistema de registro e autenticação de usuários, desenvolvido com Spring Security 6, integrando autenticação baseada em JWT (JSON Web Tokens) e OAUTH 2.0.

## Tecnologias

- Java 21
- Spring
- Spring Security
- Oauth 2.0
- PostgreSQL
- Flyway
- Swagger
- Docker

# Configurando a Chave Pública e Chave Privada
Para configurar as chaves para autenticação via JWT, siga as instruções abaixo.

#### 1. Crie um diretório `jwt` dentro da pasta `resources`

#### 2. Acesse o diretório `jwt` dentro da pasta `resources`:
```
cd \src\main\resources\jwt>
```
#### 3. Gere da chave pública executando o comando:
```
openssl genpkey -algorithm RSA -out app.key -outform PEM
```
#### 4. Em seguida, gere a chave privada com o comando:
```
openssl rsa -pubout -in app.key -out app.pub
```

# Configurando o Docker
Para criar a imagem do Docker, siga os passos:

### 1. Configuração do Dockerfile
#### 1.1 Execute o comando para criar o pacote do aplicativo (sem rodar os testes).
  
```
mvn clean package -DskipTests
```
#### 1.2 Em seguida, execute o comando para criar a imagem Docker:
```
docker build -t authenticator .
```
### 2. Configuração do Docker Compose

#### 2.1 No arquivo `docker-compose.yml`, configure o usuário e a senha de acordo com o que será utilizado no banco de dados.
```
services:
  api:
    image: authenticator
    ports:
      - "8080:8080"
    depends_on:
      - db
  db:
    image: postgres:17
    environment:
      POSTGRES_USER:
      POSTGRES_PASSWORD:
      POSTGRES_DB: users_db
    ports:
      - "5432:5432"
```
#### 2.2 No arquivo `application.properties`, configure as mesmas credenciais de usuário e senha que você definiu no `docker-compose.yml`.
```
spring.application.name=JavaAuthenticator

jwt.public-key=classpath:jwt/app.pub
jwt.private-key=classpath:jwt/app.key

spring.datasource.url=jdbc:postgresql://db:5432/users_db
spring.datasource.username=
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
```
#### 2.3 Depois de realizar essas configurações, execute o seguinte comando para subir os containers:
```
docker-compose up -d
```
# Configurando o Swagger
Após configurar o Docker e subir os containers, você pode acessar a documentação interativa do Swagger no seguinte link
```
http://localhost:8080/swagger-ui/index.html
```
## Considerações
Este projeto foi desenvolvido com o objetivo de consolidar conceitos fundamentais de:

- Programação Orientada a Objetos (POO)
- Desenvolvimento de APIs RESTful
- Boas práticas de desenvolvimento
- Conteinerização via Docker
- Versionamento de banco de dados via Flyway

**_Desenvolvido por Lucas Storck_**
