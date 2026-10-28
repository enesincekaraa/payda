# 04 — Rezervasyon isteklerinde idempotency

## Çözdüğümüz problem

Bir kullanıcı 40 müsait paketin 20 tanesini ayırıyor.
İşlem veritabanında tamamlanıyor ancak kullanıcı yanıtı alamıyor.

Kullanıcı tekrar denediğinde ikinci bir rezervasyon oluşturmak,
aynı işlem için toplam 40 paket ayırmamıza neden olabilir.

Bu nedenle rezervasyon oluşturma isteklerine bir işlem anahtarı ekledik.

## API kullanımı

Endpoint:

POST /api/v1/donations/{donationId}/reservations

Zorunlu HTTP header:

Idempotency-Key: 3b222126-1675-4cf6-b486-9084f440063c

İstek gövdesi:

{
"packageCount": 20
}

İstemci, her yeni işlem için yeni bir UUID oluşturur.
Aynı işlemi tekrar denerken aynı anahtarı ve içeriği kullanır.

## Uyguladığımız davranışlar

| Durum | Sonuç |
|---|---|
| Yeni anahtar ve geçerli talep | Rezervasyon oluşturulur; 201 döner |
| Aynı anahtar ve aynı talep | Mevcut rezervasyon döner; stok tekrar azalmaz |
| Aynı anahtar ve farklı bağış veya paket sayısı | 409 Conflict |
| Eksik veya UUID'ye dönüştürülemeyen anahtar | 400 Bad Request |
| İptal edilmiş rezervasyonun oluşturma isteğini tekrar gönderme | Aynı rezervasyon CANCELLED durumuyla döner |

Başarılı tekrarda mevcut uygulama 201 ve aynı Location değerini döndürür.
İlk HTTP yanıtının kopyası saklanmaz; rezervasyonun güncel durumu döndürülür.

Yeni bir rezervasyon oluşturmak için yeni anahtar gerekir.

## Servis akışı

1. Anahtarla mevcut rezervasyon aranır.
2. Kayıt varsa bağış kimliği ve paket sayısı karşılaştırılır.
3. Talep aynıysa mevcut rezervasyon döndürülür.
4. Talep farklıysa IdempotencyKeyConflictException fırlatılır.
5. Kayıt yoksa paketler ayrılır ve rezervasyon kaydedilir.

Paket ayırma ve rezervasyon kaydetme aynı transaction içinde gerçekleşir.

## Veritabanı değişikliği

V6 migration'ı ile reservations tablosuna idempotency_key eklendi.

- Mevcut kayıtlara UUID atandı.
- Alan NOT NULL yapıldı.
- uq_reservations_idempotency_key UNIQUE kısıtı eklendi.

Servisteki ön kontrol tek başına eşzamanlı istekleri engellemez.
İki istek de aynı anda anahtarın bulunmadığını görebilir.
UNIQUE kısıtı aynı anahtarla iki kaydın kalıcı olmasını engeller.

## Version ile idempotency arasındaki fark

@Version, eski bir veri sürümü üzerinden yapılan güncellemeyi tespit eder.

Idempotency anahtarı, isteğin önceki bir işlemin tekrarı olup olmadığını
belirlememizi sağlar.

İlk işlem tamamlandıktan sonra gelen tekrar, güncel version değerini
okuyabilir. Bu nedenle @Version tek başına tekrar rezervasyonu önlemez.

## Hata yönetimi

Aynı anahtar farklı taleple kullanılırsa:

IDEMPOTENCY_KEY_CONFLICT

Veritabanındaki idempotency UNIQUE kısıtı ihlal edilirse:

IDEMPOTENCY_CONCURRENT_REQUEST

Her iki durumda da 409 döner; ancak nedenleri farklıdır.

Eşzamanlı çakışma durumunda aynı anahtar ve aynı içerikle tekrar denenebilir.
Farklı içerik hatasında otomatik tekrar yapmak sorunu çözmez.

İsteklerin ilerleme sırasına göre optimistic locking kontrolü de
önce devreye girerek DONATION_CONCURRENT_MODIFICATION döndürebilir.

## Doğrulamalar

- Domain ve repository testleri.
- Serviste aynı talebin tekrarı ve farklı talep kontrolleri.
- Farklı anahtarlarla ayrı rezervasyon oluşturma.
- API'de header doğrulaması ve hata yanıtları.
- Tekrarlanan istekte aynı rezervasyon ve değişmeyen stok.
- İptal sonrası eski isteğin rezervasyonu yeniden aktifleştirmemesi.
- Exception handler'ın ilgili UNIQUE ihlalini diğer hatalardan ayırması.
- Swagger üzerinden aynı anahtarla tekrar gönderme denemesi.

## Henüz tamamlamadığımız doğrulama

İki HTTP isteğini kontrollü biçimde eşzamanlı çalıştıran
ReservationConcurrencyApiTest ertelendi.

Mevcut testlerin geçmesi, bu eşzamanlı HTTP senaryosunun test edildiği
anlamına gelmez.

## Mevcut kapsam

Anahtarlar rezervasyon oluşturma kayıtları genelinde benzersizdir.
Henüz kullanıcı bazında anahtar kapsamı veya anahtar silme politikası yoktur.