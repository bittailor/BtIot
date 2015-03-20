#include <SPI.h>
#include <dht.h>
#include <BtMqttSn.h>

//-----

#define ADDRESS 2

//-----

#define xstr(s) str(s)
#define str(s) #s

#define CLIENT_NODE_ID (ADDRESS)
#define CLIENT_ID "Node" xstr(ADDRESS)
#define TOPIC_BASE "Actor/"

#define SWITH_TOPIC TOPIC_BASE "Switch"
#define UPTIME_TOPIC TOPIC_BASE "Uptime"
#define CONNECT_UPTIME_TOPIC TOPIC_BASE "ConnectUptime"
#define RECONNECT_COUNTER_TOPIC TOPIC_BASE "ReconectCounter"

#define CLIENT_NODE_ID ADDRESS
#define GATEWAY_NODE_ID 0
#define RETRY_CONNECT_DELAY 20000
#define PUBLISH_INTERVAL 5000

#define CHIP_ENABLE 9
#define CHIP_SELECT 10
#define RF_CHANNEL 50

#define SWITCH_PIN 7

//-----

MqttSnClient client;
dht DHT;
unsigned long nextReadTime;
unsigned long lastConnect;
unsigned long reconnectCounter;

void setup() {
   Serial.begin(9600);
   Serial << endl << endl << endl << "*** MQTT-SN actor example ***" << endl;
   Serial << endl;
   Serial << " - Node Address  = " << CLIENT_NODE_ID << endl;
   Serial << " - Topic Switch  = " << SWITH_TOPIC << endl;

   client.begin(CHIP_ENABLE, CHIP_SELECT, CLIENT_NODE_ID, GATEWAY_NODE_ID, CLIENT_ID, RF_CHANNEL, &callback);
   pinMode(SWITCH_PIN, OUTPUT);

   Serial << "try connect ..." << endl;

   connect();

   unsigned long now = millis();
   lastConnect = now;
   nextReadTime = now;

}

void loop() {
   if(client.loop()) {
     if(nextReadTime < millis()) {
       publish();
       nextReadTime = millis() + PUBLISH_INTERVAL;
     }
   } else {
     Serial << "Connection lost try reconnect ..." << endl;
     connect();
     reconnectCounter++;
     lastConnect = millis();
   }
}

void connect() {
    while (!client.connect()) {
      Serial << "... connect failed, reset client ... " << endl;
      client.end();
      delay(1000);
      Serial << "... and retry connect after delay @ " <<  (millis() + RETRY_CONNECT_DELAY) << " ..." << endl;
      client.begin(CHIP_ENABLE, CHIP_SELECT, CLIENT_NODE_ID, GATEWAY_NODE_ID, CLIENT_ID, RF_CHANNEL, &callback);
      delay(RETRY_CONNECT_DELAY);
      Serial << "... retry connect ..." << endl;
    }
    client.subscribe(SWITH_TOPIC);
    Serial << "... connected" << endl;
}

void publish() {
  char buffer[20] = {0};
  unsigned long uptime = millis();
  unsigned long connectUptime = uptime - lastConnect;
  Serial << "publish Up " << uptime << " ConUp " << connectUptime << " ReCon " << reconnectCounter << endl;
  client.publish(UPTIME_TOPIC, ultoa(uptime,buffer,10));
  client.publish(CONNECT_UPTIME_TOPIC, ultoa(connectUptime,buffer,10));
  client.publish(RECONNECT_COUNTER_TOPIC, ultoa(reconnectCounter,buffer,10));
}

void callback(const char* iTopic, const char* iData) {
    if(strcmp(iTopic, SWITH_TOPIC)==0) {
        handleBuzzer(iData);
    }
}

void handleBuzzer(const char* iData) {
    int state = atoi(iData) > 0 ? HIGH : LOW;
    Serial << "Switch to = " << state << endl;
    digitalWrite(SWITCH_PIN, state);
}
