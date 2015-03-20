#include <SPI.h>
#include <dht.h>
#include <BtMqttSn.h>

//-----

#define ADDRESS 1

//-----

#define xstr(s) str(s)
#define str(s) #s

#define CLIENT_NODE_ID (ADDRESS)
#define CLIENT_ID "Node" xstr(ADDRESS)
#define PUBLISH_BASE "Sensors/"

#define PUBLISH_TEMPERATURE_TOPIC PUBLISH_BASE "Temperature"
#define PUBLISH_HUMIDITY_TOPIC PUBLISH_BASE "Humidity"
#define PUBLISH_UPTIME_TOPIC PUBLISH_BASE "Uptime"
#define PUBLISH_CONNECT_UPTIME_TOPIC PUBLISH_BASE "ConnectUptime"
#define PUBLISH_RECONNECT_COUNTER_TOPIC PUBLISH_BASE "ReconectCounter"

#define CLIENT_NODE_ID ADDRESS
#define GATEWAY_NODE_ID 0

#define DHT22_READ_INTERVAL 5000
#define RETRY_CONNECT_DELAY 20000

#define CHIP_ENABLE 9
#define CHIP_SELECT 10
#define RF_CHANNEL 50

#define DHT22_PIN 7

//-----

MqttSnClient client;
dht DHT;
unsigned long nextReadTime;
unsigned long lastConnect;
unsigned long reconnectCounter;

void setup() {
   Serial.begin(9600);
   Serial << endl << endl << endl << "*** MQTT-SN publish temperature example ***" << endl;
   Serial << endl;
   Serial << " - Node                   = " << CLIENT_NODE_ID << endl;
   Serial << " - Pup-Topic Temperature  = " << PUBLISH_TEMPERATURE_TOPIC << endl;
   Serial << " - Pup-Topic Humidity     = " << PUBLISH_HUMIDITY_TOPIC << endl;

   client.begin(CHIP_ENABLE, CHIP_SELECT, CLIENT_NODE_ID, GATEWAY_NODE_ID, CLIENT_ID, RF_CHANNEL);

   Serial << "try connect ..." << endl;

   connect();

   unsigned long now = millis();
   lastConnect = now;
   nextReadTime = now;

}

void loop() {
   if(client.loop()) {
     if(nextReadTime < millis()) {
       publishTemperature();
       nextReadTime = millis() + DHT22_READ_INTERVAL;
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
      client.begin(CHIP_ENABLE, CHIP_SELECT, CLIENT_NODE_ID, GATEWAY_NODE_ID, CLIENT_ID, RF_CHANNEL);
      delay(RETRY_CONNECT_DELAY);
      Serial << "... retry connect ..." << endl;
    }
    Serial << "... connected" << endl;
}

void publishTemperature() {
  int answer = DHT.read22(DHT22_PIN);
  if(answer != DHTLIB_OK) {
    switch (answer)
    {
      case DHTLIB_ERROR_CHECKSUM: Serial << "DHT Checksum error" << endl; return;
      case DHTLIB_ERROR_TIMEOUT : Serial << "DHT Time out error" << endl; return;
    }
  }
  char buffer[20] = {0};
  unsigned long uptime = millis();
  unsigned long connectUptime = uptime - lastConnect;
  Serial << "publish T = " << DHT.temperature << ", H = " << DHT.humidity << " Up " << uptime << " ConUp " << connectUptime << " ReCon " << reconnectCounter << endl;
  client.publish(PUBLISH_UPTIME_TOPIC, ultoa(uptime,buffer,10));
  client.publish(PUBLISH_CONNECT_UPTIME_TOPIC, ultoa(connectUptime,buffer,10));
  client.publish(PUBLISH_RECONNECT_COUNTER_TOPIC, ultoa(reconnectCounter,buffer,10));
  client.publish(PUBLISH_TEMPERATURE_TOPIC, dtostrf(DHT.temperature,4,2,buffer));
  client.publish(PUBLISH_HUMIDITY_TOPIC, dtostrf(DHT.humidity,4,2,buffer));
}
