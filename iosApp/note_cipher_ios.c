#include "NoteCipherBridge.h"

int noteCipherAesCtrCrypt(
    int encrypt,
    const uint8_t *key,
    size_t keyLength,
    const uint8_t *iv,
    size_t ivLength,
    const uint8_t *input,
    size_t inputLength,
    uint8_t *output
) {
    if (ivLength != 16) {
        return -1;
    }
    CCCryptorRef cryptor = NULL;
    CCCryptorStatus status = CCCryptorCreateWithMode(
        encrypt ? kCCEncrypt : kCCDecrypt,
        kCCModeCTR,
        kCCAlgorithmAES,
        ccNoPadding,
        iv,
        key,
        keyLength,
        NULL,
        0,
        0,
        0,
        &cryptor
    );
    if (status != kCCSuccess) {
        return (int)status;
    }

    size_t bytesProcessed = 0;
    status = CCCryptorUpdate(
        cryptor,
        input,
        inputLength,
        output,
        inputLength,
        &bytesProcessed
    );
    if (status == kCCSuccess) {
        status = CCCryptorFinal(
            cryptor,
            NULL,
            0,
            &bytesProcessed
        );
    }
    CCCryptorRelease(cryptor);
    return (int)status;
}

void noteCipherHmacSha256(
    const uint8_t *key,
    size_t keyLength,
    const uint8_t *data,
    size_t dataLength,
    uint8_t *output
) {
    CCHmac(
        kCCHmacAlgSHA256,
        key,
        keyLength,
        data,
        dataLength,
        output
    );
}
