# Library Events Producer
[Apache Kafka for Developers using Spring Boot](https://www.udemy.com/course/apache-kafka-for-developers-using-springboot/)

## Curl command for service REST endpoint
```text
curl -i \
-d '{"libraryEventId":null,"book":{"bookId":456,"bookName":"Kafka Using Spring Boot","bookAuthor":"Dilip"}}' \
-H "Content-Type: application/json" \
-X POST http://localhost:8080/v1/library/event
```

PUT WITH ID
---------------------
```text
curl -i \
-d '{"libraryEventId":1,"book":{"bookId":456,"bookName":"Kafka Using Spring Boot.X","bookAuthor":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/library/event
```

```text
curl -i \
-d '{"libraryEventId":999,"book":{"bookId":456,"bookName":"Kafka Using Spring Boot.X","bookAuthor":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/library/event
```

PUT WITHOUT ID
---------------------
```text
curl -i \
-d '{"libraryEventId":null,"book":{"bookId":456,"bookName":"Kafka Using Spring Boot","bookAuthor":"Dilip"}}' \
-H "Content-Type: application/json" \
-X PUT http://localhost:8080/v1/library/event
```

## Setting up keystore

### Generate certificate
```text
$: keytool -keystore server.keystore.jks -alias localhost -validity 365 -genkey -keyalg RSA

Enter keystore password:
Re-enter new password:
What is your first and last name?
  [Unknown]:  localhost
What is the name of your organizational unit?
  [Unknown]:  localhost
What is the name of your organization?
  [Unknown]:  localhost
What is the name of your City or Locality?
  [Unknown]:  Kremenchuk
What is the name of your State or Province?
  [Unknown]:  PT
What is the two-letter country code for this unit?
  [Unknown]:  UA
Is CN=localhost, OU=localhost, O=localhost, L=Kremenchuk, ST=PT, C=UA correct?
  [no]:  yes

Generating 2,048 bit RSA key pair and self-signed certificate (SHA256withRSA) with a validity of 365 days
        for: CN=localhost, OU=localhost, O=localhost, L=Kremenchuk, ST=PT, C=UA
```
### Inspect certificate
```text
keytool -list -v -keystore server.keystore.jks
```
### Generate certificate authority CA
```text
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -subj "/CN=local-security-CA"
```
### Create server cert-file
```text
keytool -keystore server.keystore.jks -alias localhost -certreq -file cert-file
```
### Sign certificate
```text
openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:password
```
### Adding the Signed Cert in to the KeyStore file
```text
keytool -keystore server.keystore.jks -alias CARoot -import -file ca-cert
keytool -keystore server.keystore.jks -alias localhost -import -file cert-signed
```
### Create client cert-file
```text
keytool -keystore client.truststore.jks -alias CARoot -import -file ca-cert
```
### Crete empty properties file `client-ssl.properties` in `C:\Kafka\kafka_2.13-3.2.3\bin` 
```text
cd . > client-ssl.properties
```
### Generate server trust-store file
```text
keytool -keystore server.truststore.jks -alias CARoot -import -file ca-cert
```
### Create client keystore file
```text
cp server.keystore.jks client.keystore.jks
```