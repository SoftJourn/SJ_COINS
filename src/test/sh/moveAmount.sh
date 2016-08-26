#!/usr/bin/env bash
##get token
tokens=(`curl --silent -i -k -G -X POST \
  -H "Authorization: Basic dXNlcl9jcmVkOnN1cGVyc2VjcmV0" \
  -H "Content-Type:application/x-www-form-urlencoded" \
  -d "username={user}" \
  -d "password={password}" \
  -d "grant_type=password" \
  https://localhost:8111/oauth/token \
  | grep -Po "((?<=access_token\":\")[^\"]+)|((?<=refresh_token\":\")[^\"]+)"`)

echo "ACCESS_TOKEN: "${tokens[0]}
echo "REFRESH_TOKEN: "${tokens[1]}

#curl --silent -i -k -X POST \
#  https://localhost:8080/api/v1/add/vromanchuk \
#  -d '{"comment": "Pass some money to Account1", "amount": 500 }' \
#  -H "Authorization: Bearer "${tokens[0]} \
#  -H "Content-Type: application/json"
#
curl --silent -i -k -X POST \
  https://localhost:8080/api/v1/move/vdanyliuk \
  -d '{"comment": "Pass some money to Account1", "amount": 1 }' \
  -H "Authorization: Bearer "${tokens[0]} \
  -H "Content-Type: application/json"
