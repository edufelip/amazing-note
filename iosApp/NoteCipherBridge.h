#include <CommonCrypto/CommonCryptor.h>
#include <CommonCrypto/CommonHMAC.h>
#include <stdint.h>
#include <stddef.h>

int noteCipherAesCtrCrypt(
    int encrypt,
    const uint8_t *key,
    size_t keyLength,
    const uint8_t *iv,
    size_t ivLength,
    const uint8_t *input,
    size_t inputLength,
    uint8_t *output
);

void noteCipherHmacSha256(
    const uint8_t *key,
    size_t keyLength,
    const uint8_t *data,
    size_t dataLength,
    uint8_t *output
);
