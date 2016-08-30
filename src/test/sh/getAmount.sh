#!/usr/bin/env bash

source ./init.sh

#### ADDITIONAL INFO ######
### Store username asd password in init.sh file and do NOT include this file in commit

##get token
tokens=(`curl --silent -i -k -G -X POST \
  -H "Authorization: Basic dXNlcl9jcmVkOnN1cGVyc2VjcmV0" \
  -H "Content-Type:application/x-www-form-urlencoded" \
  -d "username="${yourLDAPid} \
  -d "password="${yourLDAPpassword} \
  -d "grant_type=password" \
  https://localhost:8111/oauth/token \
  | grep -Po "((?<=access_token\":\")[^\"]+)|((?<=refresh_token\":\")[^\"]+)"`)

echo "ACCESS_TOKEN: "${tokens[0]}
echo "REFRESH_TOKEN: "${tokens[1]}

curl --silent -i -k -X  GET \
  https://localhost:8080/api/v1/amount \
  -H "Authorization: Bearer "${tokens[0]} \

