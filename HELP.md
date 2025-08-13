# Notification Service Setup Guide

### 1. 啟動服務
```bash
# 啟動所有依賴服務 (MySQL, Redis, RocketMQ)
docker compose up -d

# 等待服務完全啟動 (約30秒)
sleep 30

# 啟動 Spring Boot 應用
mvn spring-boot:run
```

### 2. 驗證服務
```bash
# 測試 API
curl -X POST http://localhost:8080/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EMAIL",
    "recipient": "test@example.com",
    "subject": "測試通知",
    "content": "Hello World"
  }'
```

## RocketMQ Topic 管理

```bash
# 修改 broker.conf
autoCreateTopicEnable = false

# 手動創建 notification-topic
docker exec rocketmq-broker sh -c "
cd /home/rocketmq/rocketmq-5.1.4/bin && 
sh mqadmin updateTopic \
  -n rocketmq-namesrv:9876 \
  -t notification-topic \
  -c DefaultCluster \
  -r 4 \
  -w 4 \
  -p 6
"
```

### 驗證 Topic
```bash
# 檢查 topic 列表
docker exec rocketmq-broker sh -c "
cd /home/rocketmq/rocketmq-5.1.4/bin && 
sh mqadmin topicList -n rocketmq-namesrv:9876
"

# 檢查 topic 狀態
docker exec rocketmq-broker sh -c "
cd /home/rocketmq/rocketmq-5.1.4/bin && 
sh mqadmin topicStatus -n rocketmq-namesrv:9876 -t notification-topic
"
```

## API 端點

### 創建通知
```http
POST /notifications
Content-Type: application/json

{
  "type": "EMAIL|SMS|PUSH",
  "recipient": "user@example.com",
  "subject": "通知主題",
  "content": "通知內容"
}
```

### 查詢通知
```http
GET /notifications/{id}
GET /notifications/recent
```

### 更新通知
```http
PUT /notifications/{id}
Content-Type: application/json

{
  "subject": "更新的主題",
  "content": "更新的內容"
}
```

### 刪除通知
```http
DELETE /notifications/{id}
```


### Dev (broker.conf)
```conf
brokerIP1 = 127.0.0.1
autoCreateTopicEnable = true    # 方便開發測試
```

### Prod (broker.conf)
```conf
brokerIP1 = <實際內網IP>
autoCreateTopicEnable = false   # 手動管理 topic
aclEnable = true               # 啟用權限控制
```
