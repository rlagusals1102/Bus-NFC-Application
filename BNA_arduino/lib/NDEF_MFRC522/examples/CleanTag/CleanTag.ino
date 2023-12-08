// Clean resets a tag back to factory-like state
// For Mifare Classic, tag is zero'd and reformatted as Mifare Classic
// For Mifare Ultralight, tags is zero'd and left empty

#include <SPI.h>
#include <MFRC522.h>
#include "NfcAdapter.h"

#define CS_PIN 10

MFRC522 mfrc522(CS_PIN, UINT8_MAX); // Create MFRC522 instance

NfcAdapter nfc = NfcAdapter(&mfrc522);

void setup(void) {
    Serial.begin(9600);
    Serial.println("NFC Tag Cleaner\nPlace a tag on the NFC reader to clean back to factory state.");
    SPI.begin();        // Init SPI bus
    mfrc522.PCD_Init(); // Init MFRC522
    nfc.begin();
}

void loop(void) {
    if (nfc.tagPresent()) {
        Serial.println("Cleaning tag");
        bool success = nfc.clean();
        if (success) {
            Serial.println("\tSuccess, tag restored to factory state.");
            delay(10000);
        } else {
            Serial.println("\tError, unable to clean tag.");
        }
    }
    delay(5000);
}
