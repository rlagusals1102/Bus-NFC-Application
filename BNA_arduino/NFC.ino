#include <Arduino.h>
#include "Adafruit_PN532.h"
#include <LiquidCrystal_I2C.h>

#define PN532_SCK  (2)
#define PN532_MOSI (3)
#define PN532_SS   (4)
#define PN532_MISO (5)

#define PN532_IRQ   (2)
#define PN532_RESET (3)

// ic2
Adafruit_PN532 nfc(PN532_IRQ, PN532_RESET);
LiquidCrystal_I2C lcd(0x27, 16, 2); 



void setup() {
  Serial.begin(115200);
  delay(50);

  lcd.clear();
  lcd.setCursor(0, 1);
  lcd.print("7719");
  lcd.setCursor(0, 1);
  lcd.print("Not tagged");

  Serial.println("Start NFC");

  nfc.begin();
  uint32_t versiondata = nfc.getFirmwareVersion();
  if (! versiondata) {
    Serial.print("Didn't find PN53x board");
    while (1);
  }

  Serial.print("Found chip PN5"); Serial.println((versiondata >> 24) & 0xFF, HEX);
  Serial.print("Firmware ver. "); Serial.print((versiondata >> 16) & 0xFF, DEC);
  Serial.print('.'); Serial.println((versiondata >> 8) & 0xFF, DEC);


  nfc.SAMConfig();
  Serial.println("Waiting for an ISO14443A Card ...");

  delay(2);
}

void setData(uint8_t *data, char *stationNumber) {
    char *CHARS = "01234567890ABCDEF";
    data[0] = 0x00;
    data[1] = 0x01;
    for (int i = 0; i < 5; i++) {
       data[i + 2] = (strchr(CHARS, stationNumber[i * 2]) - CHARS) << 4;
        if (i != 4) {
            data[i + 2] |= (strchr(CHARS, stationNumber[i * 2 + 1]) - CHARS);
        }
       
    }
}


void loop() {
  uint8_t success;
  uint8_t responseLength = 64;
  success = nfc.inListPassiveTarget();
  if (success) {
    Serial.println("Found something!");

    uint8_t cardInfo[responseLength];
    uint8_t cardNumSize = 0;
    uint8_t selectApdu[] = { 0x00, 0xA4, 0x04, 0x00, 0x07, 0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x00 };

    success = nfc.inDataExchange(selectApdu, sizeof(selectApdu), cardInfo, &responseLength);

    if (success) {
        Serial.print("responseLength: ");
        Serial.println(responseLength);
        nfc.PrintHexChar(cardInfo, responseLength);
        if (responseLength != 2 || cardInfo[0] != 0x90 || cardInfo[1] != 0x00) {
            uint8_t data[7];
            setData(data, "124000008");
            success = nfc.inDataExchange(data, sizeof(data), cardInfo, &responseLength);
            
            if (success && responseLength == 2 && (cardInfo[0] == 0x90 && cardInfo[1] == 0x00)) {
                Serial.print("Success!");
                lcd.clear();
                lcd.setCursor(0, 1);
                lcd.print("Success");
                delay(2000)
                lcd.clear();
                lcd.setCursor(0, 1);
                lcd.print("Success");
            }
        }
    }
  }
  delay(1);
}