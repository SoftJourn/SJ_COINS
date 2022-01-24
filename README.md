# SJ Coin server

## Prepare stage
#### Setup environment variables.
```
SJ_COINS_SERVER_DATASOURCE_URL=jdbc:mysql://localhost:3306/sj_coins?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false
SJ_COINS_SERVER_DATASOURCE_USERNAME=sj
SJ_COINS_SERVER_DATASOURCE_PASSWORD=password
SJ_COINS_SERVER_LOGGING_CONFIG_FILE=/path/to/coins/logback.xml
SJ_COINS_SERVER_IMAGE_PATH=/path/to/coins/images
SJ_COINS_SERVER_AUTH_SERVER_URL=http://127.0.0.1:8081
SJ_COINS_SERVER_AUTH_CLIENT_ID=coins_server
SJ_COINS_SERVER_AUTH_CLIENT_SECRET=x5VLts24E63Q5TXU23dMrSeU
SJ_COINS_SERVER_FABRIC_CLIENT_URL=http://localhost:4000/
SJ_COINS_SERVER_AUTH_PUBKEY_PATH=/path/to/coins/auth.pub
```
Or with export:
```
export SJ_COINS_SERVER_DATASOURCE_URL='jdbc:mysql://localhost:3306/sj_coins?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false'
export SJ_COINS_SERVER_DATASOURCE_USERNAME='sj'
export SJ_COINS_SERVER_DATASOURCE_PASSWORD='password'
export SJ_COINS_SERVER_LOGGING_CONFIG_FILE='/path/to/coins/logback.xml'
export SJ_COINS_SERVER_IMAGE_PATH='/path/to/coins/images'
export SJ_COINS_SERVER_AUTH_SERVER_URL='http://127.0.0.1:8081'
export SJ_COINS_SERVER_AUTH_CLIENT_ID='coins_server'
export SJ_COINS_SERVER_AUTH_CLIENT_SECRET='x5VLts24E63Q5TXU23dMrSeU'
export SJ_COINS_SERVER_FABRIC_CLIENT_URL='http://localhost:4000/'
export SJ_COINS_SERVER_AUTH_PUBKEY_PATH='/path/to/coins/auth.pub'
```

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

#AUTH
authPublicKeyFile=/home/username/.coins/auth.pub
auth.server.url=https://hostname
auth.client.client-id=clientId
auth.client.client-secret=clientSecret

#FABRIC
node.fabric.client=https://hostname
org.name=organazionName
fabric.peers=peerHostName:port
treasury.account=someAccount

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