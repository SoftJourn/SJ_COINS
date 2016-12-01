#!/usr/bin/env bash

source ./init.sh

#### ADDITIONAL INFO ######
### Store username asd password in init.sh file and do NOT include this file in commit
### Create that file with the following commands
### declare -r yourLDAPid=username
### declare -r yourLDAPpassword=password

##get token
tokens=(`curl --silent -i -k -G -X POST \
  -H "Authorization: Basic dXNlcl9jcmVkOnN1cGVyc2VjcmV0" \
  -H "Content-Type:application/x-www-form-urlencoded" \
  -d "username="${yourLDAPid} \
  -d "password="${yourLDAPpassword} \
  -d "grant_type=password" \
  https://sjcoins-testing.softjourn.if.ua/auth/oauth/token \
  | grep -Po "((?<=access_token\":\")[^\"]+)|((?<=refresh_token\":\")[^\"]+)"`)

echo "ACCESS_TOKEN: "${tokens[0]}
echo "REFRESH_TOKEN: "${tokens[1]}

declare data='{"tokenContractAddress":"6B33A557FC0B928868482E98979AA99EB665B41B","offlineContractAddress":"01C29AF9C80CEE3A3119DD4F7AEBB7DFA9A177E1","chequeHash":"3e0e7d543b2719f45b80e4a9650793718043e63ad3cae3e36ad64f0636f02b67","amount":100}'

curl -i --silent -k -X  POST \
  http://localhost:8080/api/v1/deposit \
  -d "${data}" \
  -H "Authorization: Bearer "${tokens[0]} \
  -H "Accept: application/json" \
  -H "Content-Type: application/json"

