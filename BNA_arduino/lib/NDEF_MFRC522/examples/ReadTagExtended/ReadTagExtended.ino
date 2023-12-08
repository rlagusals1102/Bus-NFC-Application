// Shows the content of a Mifare Classic tag formatted as an NDEF tag
// It serves as an example of how to parse the records one at a time
// This example requires #define NDEF_USE_SERIAL to be uncommented in Ndef.h

#include <SPI.h>
#include <MFRC522.h>
#include "NfcAdapter.h"

#define CS_PIN 10

MFRC522 mfrc522(CS_PIN, UINT8_MAX); // Create MFRC522 instance

NfcAdapter nfc = NfcAdapter(&mfrc522);

void setup(void) {
    Serial.begin(9600);
    Serial.println("Extended NDEF Reader\nPlace an unformatted Mifare Classic tag on the reader to show contents.");
    SPI.begin();        // Init SPI bus
    mfrc522.PCD_Init(); // Init MFRC522
    nfc.begin();
}

void loop(void) {
    if (nfc.tagPresent())
    {
        Serial.println("Reading NFC tag");
        NfcTag tag = nfc.read();
        Serial.println(tag.getTagType());
        Serial.print("UID: ");Serial.println(tag.getUidString());
    
        if (tag.hasNdefMessage()) // every tag won't have a message
        {
    
              NdefMessage message = tag.getNdefMessage();
              Serial.print("\nThis NFC Tag contains an NDEF Message with ");
              Serial.print(message.getRecordCount());
              Serial.print(" NDEF Record");
              if (message.getRecordCount() != 1) {
                    Serial.print("s");
              }
              Serial.println(".");
        
              // cycle through the records, printing some info from each
              int recordCount = message.getRecordCount();
              for (int i = 0; i < recordCount; i++)
              {
                    Serial.print("\nNDEF Record ");Serial.println(i+1);
                    NdefRecord record = message.getRecord(i);
                    // NdefRecord record = message[i]; // alternate syntax
            
                    Serial.print("  TNF: ");Serial.println(record.getTnf());
                    Serial.print("  Type: ");PrintHexChar(record.getType(), record.getTypeLength()); // will be "" for TNF_EMPTY
            
                    // The TNF and Type should be used to determine how your application processes the payload
                    // There's no generic processing for the payload, it's returned as a byte[]
                    int payloadLength = record.getPayloadLength();
                    const byte *payload = record.getPayload();
            
                    // Print the Hex and Printable Characters
                    Serial.print("  Payload (HEX): ");
                    PrintHexChar(payload, payloadLength);
            
                    // Force the data into a String (might work depending on the content)
                    // Real code should use smarter processing
                    String payloadAsString = "";
                    for (int c = 0; c < payloadLength; c++) {
                          payloadAsString += (char)payload[c];
                    }
                    Serial.print("  Payload (as String): ");
                    Serial.println(payloadAsString);
            
                    // id is probably blank and will return ""
                    if (record.getIdLength() > 0) {
                          Serial.print("  ID: ");PrintHexChar(record.getId(), record.getIdLength());
                    }
              }
        }
  }
  delay(5000);
}
