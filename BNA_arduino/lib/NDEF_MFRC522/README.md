# NDEF Library for Arduino

Read and Write NDEF messages on NFC Tags with Arduino.

NFC Data Exchange Format (NDEF) is a common data format that operates across all NFC devices, regardless of the underlying tag or device technology.

This code works with the cheap MFRC522 tag reader.

### Supports 
 - Reading from Mifare Classic Tags with 4 byte UIDs.
 - Writing to Mifare Classic Tags with 4 byte UIDs.
 - Reading from Mifare Ultralight tags.
 - Writing to Mifare Ultralight tags.

### Requires

[MFRC522 Library](https://github.com/miguelbalboa/rfid)

## Getting Started

To use the Ndef library in your code, include the following in your sketch:

    #include <SPI.h>
    #include <MFRC522.h>
    #include <NfcAdapter.h>
    
    MFRC522 mfrc522(CS_PIN, UINT8_MAX);
    NfcAdapter ndef = NfcAdapter(&mfrc522);

    void setup() {
        SPI.begin();        // Init SPI bus
        mfrc522.PCD_Init(); // Init MFRC522
        nfc.begin();
    }

### NfcAdapter

The user interacts with the NfcAdapter to read and write NFC tags using the NFC shield.

Read a message from a tag

    if (nfc.tagPresent()) {
        NfcTag tag = nfc.read();
        tag.print();
    }

Write a message to a tag

    if (nfc.tagPresent()) {
        NdefMessage message = NdefMessage();
        message.addTextRecord("Hello, Arduino!");
        success = nfc.write(message);
    }

Erase a tag. Tags are erased by writing an empty NDEF message. Tags are not zeroed out the old data may still be read off a tag using an application like [NXP's TagInfo](https://play.google.com/store/apps/details?id=com.nxp.taginfolite&hl=en).

    if (nfc.tagPresent()) {
        success = nfc.erase();
    }


Format a Mifare Classic tag as NDEF.

    if (nfc.tagPresent()) {
        success = nfc.format();
    }


Clean a tag. Cleaning resets a tag back to a factory-like state. For Mifare Classic, tag is zeroed and reformatted as Mifare Classic (non-NDEF). For Mifare Ultralight, the tag is zeroed and left empty.

    if (nfc.tagPresent()) {
        success = nfc.clean();
    }


### NfcTag 

Reading a tag with the shield, returns a NfcTag object. The NfcTag object contains meta data about the tag UID, technology, size.  When an NDEF tag is read, the NfcTag object contains a NdefMessage.

### NdefMessage

A NdefMessage consist of one or more NdefRecords.

The NdefMessage object has helper methods for adding records.

    ndefMessage.addTextRecord("hello, world");
    ndefMessage.addUriRecord("http://arduino.cc");

The NdefMessage object is responsible for encoding NdefMessage into bytes so it can be written to a tag. The NdefMessage also decodes bytes read from a tag back into a NdefMessage object.

### NdefRecord

A NdefRecord carries a payload and info about the payload within a NdefMessage.

### Specifications

This code is based on the "NFC Data Exchange Format (NDEF) Technical Specification" and the "Record Type Definition Technical Specifications" that can be downloaded from the [NFC Forum](http://www.nfc-forum.org/specs/spec_license).
    
## Warning

This software is in development. It works for the happy path. Error handling could use improvement. It runs out of memory, especially on the Uno board. Use small messages with the Uno. The Due board can write larger messages. Please submit patches.

## Book
Need more info? Check out my book <a href="http://www.anrdoezrs.net/click-7521423-11260198-1430755877000?url=http%3A%2F%2Fshop.oreilly.com%2Fproduct%2F0636920021193.do%3Fcmp%3Daf-prog-books-videos-product_cj_9781449372064_%2525zp&cjsku=0636920021193" target="_top">
Beginning NFC: Near Field Communication with Arduino, Android, and PhoneGap</a><img src="http://www.lduhtrp.net/image-7521423-11260198-1430755877000" width="1" height="1" border="0"/>.

<a href="http://www.tkqlhce.com/click-7521423-11260198-1430755877000?url=http%3A%2F%2Fshop.oreilly.com%2Fproduct%2F0636920021193.do%3Fcmp%3Daf-prog-books-videos-product_cj_9781449372064_%2525zp&cjsku=0636920021193" target="_top"><img src="http://akamaicovers.oreilly.com/images/0636920021193/cat.gif" border="0" alt="Beginning NFC"/></a><img src="http://www.awltovhc.com/image-7521423-11260198-1430755877000" width="1" height="1" border="0"/>

## License

[BSD License](https://github.com/don/Ndef/blob/master/LICENSE.txt) (c) 2013-2014, Don Coleman