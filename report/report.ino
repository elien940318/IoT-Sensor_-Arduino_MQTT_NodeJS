#include <Wire.h>
#include <BH1750.h>
#include <ESP8266WiFi.h>
#include "PubSubClient.h"
#include "DHT.h"

void msgReceived(char*, byte*, unsigned int);

char ssid[] = "305";
char password[] = "18741199";
byte server1[] = {192,168,10,5};  
int port = 1883;
BH1750 lightMeter;
DHT dht(4, DHT11);

WiFiClient client;
float tmpData, humData;

PubSubClient mqttClient(server1, port, msgReceived, client);

void setup() {
  
  Serial.begin(115200);
  Wire.begin();
  lightMeter.begin();
  dht.begin();
  // put your setup code here, to run once:
  pinMode(14, OUTPUT);  // led
  pinMode(12, OUTPUT);  // 초음파 trig
  pinMode(13, INPUT);   // 초음파 echo
  
  delay(10);
  Serial.println();
  Serial.println();
  Serial.println("Connecting to~");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while(WiFi.status()!= WL_CONNECTED){
    delay(500);
    Serial.print(",");
  }
  Serial.println();
  Serial.println("Wi-Fi AP Connected!");

  Serial.println(WiFi.localIP());

  if(mqttClient.connect("Arduino")){
       Serial.println("MQTT Broker Connected!");
       mqttClient.subscribe("led"); //led 구독자를 등록(데이터를 읽어갈 구독자 등록)
  }
}

void loop() {
  mqttClient.loop();
  
  char message[64] = "", pTmpBuf[50], pHumBuf[50], pDisBuf[50], pLuxBuf[50];
    
  getHumidityTemperatureSensorData();
  dtostrf(tmpData, 5, 2, pTmpBuf);
  dtostrf(humData, 5, 2, pHumBuf);

  dtostrf(getUltraSonicSensorData(), 6, 2, pDisBuf);

  uint16_t tmpLux = lightMeter.readLightLevel();
  dtostrf(tmpLux, 5, 0, pLuxBuf);

  sprintf(message, "{\"tmp\": %s, \"hum\": %s, \"dis\": %s, \"lux\": %s}", 
          pTmpBuf, pHumBuf, pDisBuf, pLuxBuf);
  Serial.println(message);
  mqttClient.publish("sensorlog", message); 

  delay(10000);
}

void msgReceived(char* topic, byte* payload, unsigned int uLen)
{
  char pBuffer[uLen + 1];

  int i;
  for(i=0; i<uLen; i++)
    pBuffer[i] = payload[i];
  
  pBuffer[i]='\0';
  Serial.println(pBuffer);
  if(pBuffer[0]=='1')
    digitalWrite(14, HIGH);
  else
    digitalWrite(14, LOW);
}

void getHumidityTemperatureSensorData()
{
  //dht11.read(humData, tmpData);
  humData = dht.readHumidity();
  tmpData = dht.computeHeatIndex(dht.readTemperature(), humData, false);
}

float getUltraSonicSensorData() {
  digitalWrite(12, HIGH);
  delay(2);
  digitalWrite(12, LOW);
  float interval = pulseIn(13, HIGH);
  float distance = interval * 340 / 1000000 * 100 / 2;

  return distance;
}
