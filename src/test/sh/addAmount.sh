#!/usr/bin/env bash
##get token
tokens=(`curl --silent -i -G -X POST \
  -H "Authorization: Basic dXNlcl9jcmVkOnN1cGVyc2VjcmV0" \
  -H "Content-Type:application/x-www-form-urlencoded" \
  -d "username={yourLDAPid}" \
  -d "password={yourLDAPpassword}" \
  -d "grant_type=password" \
  localhost:8111/oauth/token \
  | grep -Po "((?<=access_token\":\")[^\"]+)|((?<=refresh_token\":\")[^\"]+)"`)

echo "ACCESS_TOKEN: "${tokens[0]}
echo "REFRESH_TOKEN: "${tokens[1]}

curl --silent -i -G -X POST \
  localhost:8080/v1/coins/add \
  -d "amount=500" \
  -d "account=vdanyliuk" \
  -H "Authorization: Bearer "${tokens[0]}
