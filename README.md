# SJ Coin server

## Start up documentation

### Step 1: Create databases structure

#### Enter as root user and create user for these databases using commands:

```sql
CREATE USER 'user'@'localhost' IDENTIFIED BY 'somePassword';

GRANT ALL PRIVILEGES ON *.* TO 'user'@'localhost';
```

#### Enter as this new user and create databases:

```sql
CREATE DATABASE sj_coins CHARACTER SET utf8;
```

#### NOTE: All the tables will be created during the first service start.

### Step 2: Install and create application with the help of Monax Platform:

Please read the "Getting started" tutorial [here](https://monax.io/docs/getting-started/)


### Step 3: Add sensitive properties:

```bash
mkdir $HOME/.coins
mkdir $HOME/.coins/images
touch application.properties
```

Add this properties to the previously created file

```properties
#DATABASE
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/sj_coins?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false
spring.datasource.username=user
spring.datasource.password=somePassword

#ERIS
eris.token.contract.address=someAddress
eris.token.contract.file=contractFileName

eris.offline.contract.address=someAddress
eris.offline.contract.file=contractFileName

eris.treasury.account.address=someAddress
eris.treasury.account.key.public=somePublicKey
eris.treasury.account.key.private=somePrivateKey

eris.chain.url=http://hostname:1337
eris.compiler.url=http://hostname/compile
eris.chain.version=0.11

#AUTH
authPublicKeyFile=/home/username/.coins/auth.pub
auth.server.url=https://hostname
auth.client.client-id=clientId
auth.client.client-secret=clientSecret

```

### Step 4: Add logback configuration

```bash
cd $HOME/.coins
touch logback.xml
```

Add basic configuration to the file

```xml
<configuration debug="true" scan="true" scanPeriod="30">

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%-35(%d{dd-MM-yyyy} %magenta(%d{HH:mm:ss}) [%5.10(%thread)]) %highlight(%-5level) %cyan(%logger{16}) - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="info">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

### Step 5: Run project