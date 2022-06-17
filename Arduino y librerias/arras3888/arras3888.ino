#if 0
#include <SPI.h>
#include <PN532_SPI.h>
#include <PN532.h>
#include <NfcAdapter.h>

PN532_SPI pn532spi(SPI, 10);
NfcAdapter nfc = NfcAdapter(pn532spi);
#else

#include <Wire.h>
#include <PN532_I2C.h>
#include <PN532.h>
#include <NfcAdapter.h>

PN532_I2C pn532_i2c(Wire);
NfcAdapter nfc = NfcAdapter(pn532_i2c);
#endif



void setup(void) {
  Serial.begin(9600);
  Serial.println("NDEF Reader.");
  nfc.begin();
  delay(1000);
  Serial.println("Arduino Conectado....");
}

void loop(void) {
  int opt = 0;
  String datos;
  String subDatos;
  while (opt == 0) {
    if (Serial.available()) {
      datos = Serial.readStringUntil('\n');
      if (datos.length() > 1) {
        int limit = datos.length();
        subDatos = datos.substring(2, limit);
        opt = datos.substring(0, 1).toInt();
      } else {
        opt = datos.toInt();
      }

    }
  }

  switch (opt) {
    case 1://utilizamos este metodo para escribir en nuestra tarjeta NFC
      Serial.println("\nColoque una etiqueta Mifare Classic NFC formateada en el lector.");
      delay(5000);
      if (nfc.tagPresent()) {
        NdefMessage message = NdefMessage();
        message.addUriRecord(subDatos);

        bool success = nfc.write(message);
        if (success) {
          Serial.println("Proceso Exitoso Trata de Leer los Datos con un Dispositvo Movil.");
        } else {
          Serial.println("Fallo en escritura.");
        }
      }
      break;

    case 2: //utilizamos el siguiente metodo para dar formato a nuestra tarjeta NFC
      if (nfc.tagPresent()) {

        bool success = nfc.format();
        if (success) {
          Serial.println("\nProceso Exitoso tarjeta formateada como NDEF.");
        } else {
          Serial.println("\nFormato sin exito intente de nuevo.");
        }


      }
      delay(3000);
      break;

    case 3:
      Serial.println("\nColoque una etiqueta en el lector NFC para limpiar.");

      if (nfc.tagPresent()) {

        bool success = nfc.clean();
        if (success) {
          Serial.println("\nProceso Realizado con exito la Etiqueta esta restaurada.");
        } else {
          Serial.println("\nError, no fue posible restaurar la etiqueta.");
        }
  
      }
      
      break;

    case 4://utilizamos este metodo para saber leer los datos de nuestra tarjeta NFC
      Serial.println("\nEscanear un objeto NFC.");
      delay(2000);
      if (nfc.tagPresent())
      {
        NfcTag tag = nfc.read();
        if (tag.hasNdefMessage()) // cada etiqueta no tendrá un mensaje
        {

          NdefMessage message = tag.getNdefMessage();
          //recorrer los registros, imprimiendo algo de información de cada uno
          int recordCount = message.getRecordCount();
          for (int i = 0; i < recordCount; i++)
          {

            NdefRecord record = message.getRecord(i);

            int payloadLength = record.getPayloadLength();
            byte payload[payloadLength];
            record.getPayload(payload);
            String payloadAsString = "";
            for (int c = 0; c < payloadLength; c++) {
              payloadAsString += (char)payload[c];
            }
            //Serial.print("  Payload (as String): ");
            Serial.println(payloadAsString);
            Serial.println(".");
            delay(3000);
            /*
              id is probably blank and will return ""
              String uid = record.getId()if (uid != "") {
              Serial.print("  ID: "); Serial.println(uid);
              }*/
          }
        }

      }

      break;

  }
}
