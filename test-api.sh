#!/bin/bash

# test api
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"EMAIL","recipient":"test@test.com","subject":"test","content":"test content"}'
echo ""

curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"SMS","recipient":"+123456789","content":"sms test"}'  
echo ""

curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"EMAIL","recipient":"admin@test.com","subject":"alert","content":"system alert"}'
echo ""

# get by id
curl -X GET http://localhost:8080/notifications/1
echo ""

# get recent
curl -X GET http://localhost:8080/notifications/recent  
echo ""

# update
curl -X PUT http://localhost:8080/notifications/1 -H "Content-Type: application/json" -d '{"subject":"updated subject","content":"updated content"}'
echo ""

# update partial
curl -X PUT http://localhost:8080/notifications/2 -H "Content-Type: application/json" -d '{"subject":"new subject"}'
echo ""

curl -X PUT http://localhost:8080/notifications/3 -H "Content-Type: application/json" -d '{"content":"new content"}'
echo ""

# check updates
curl -X GET http://localhost:8080/notifications/recent
echo ""

# delete
curl -X DELETE http://localhost:8080/notifications/2
echo ""

# check delete (should 404)  
curl -X GET http://localhost:8080/notifications/2
echo ""

# remaining
curl -X GET http://localhost:8080/notifications/recent
echo ""

# error tests
echo "error tests"

# validation error - missing fields
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"EMAIL","subject":"test"}'
echo ""

# validation error - empty fields  
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"EMAIL","recipient":"","content":""}'
echo ""

# invalid notification type
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"WRONG_TYPE","recipient":"test@test.com","content":"test"}'
echo ""

# not found - get
curl -X GET http://localhost:8080/notifications/999
echo ""

# not found - update
curl -X PUT http://localhost:8080/notifications/999 -H "Content-Type: application/json" -d '{"subject":"test"}'
echo ""

# not found - delete
curl -X DELETE http://localhost:8080/notifications/999
echo ""

# invalid json format
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"EMAIL","recipient":"test@test.com"'
echo ""

# large content test
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"EMAIL","recipient":"test@test.com","subject":"Large Content","content":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"}'
echo ""

# bulk create - multiple requests
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"SMS","recipient":"+111","content":"bulk msg 1"}' &
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"SMS","recipient":"+222","content":"bulk msg 2"}' &  
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"SMS","recipient":"+333","content":"bulk msg 3"}' &
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"EMAIL","recipient":"bulk1@test.com","subject":"bulk test","content":"bulk email 1"}' &
curl -X POST http://localhost:8080/notifications -H "Content-Type: application/json" -d '{"type":"EMAIL","recipient":"bulk2@test.com","subject":"bulk test","content":"bulk email 2"}' &
wait
echo ""

# check all notifications after bulk
curl -X GET http://localhost:8080/notifications/recent
echo ""

# final status check
curl -X GET http://localhost:8080/notifications/recent
echo ""