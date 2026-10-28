# Payda — 3. gün: Rezervasyon yaşam döngüsü ve CI

Bu adımda paket ayırmayı, kendi kimliği ve durumu olan
bir rezervasyon kaydına dönüştürdük.

## Rezervasyon oluşturma

POST /api/v1/donations/{donationId}/reservations

```json
{
  "packageCount": 15
}
```

Başarılı işlem 201 döndürür. Location başlığı, oluşturulan
rezervasyonun okunabileceği adresi taşır.

ReservationService aynı transaction içinde:
1. Bağışı bulur.
2. Donation.reserve() ile paketleri ayırır.
3. Rezervasyon kaydını oluşturur.

Bir sürüm çatışması oluşursa transaction geri alınır.
Başarısız işlem fazladan rezervasyon bırakmaz.

## Rezervasyon okuma ve iptal

GET /api/v1/reservations/{id}

POST /api/v1/reservations/{id}/cancel

İptal sırasında rezervasyon CANCELLED durumuna geçer;
rezervasyonun paket miktarı bağışta yeniden kullanılabilir olur.

Rezervasyon silinmez. Geçmiş bilgisi korunur.

Aynı rezervasyonu tekrar iptal etme girişimi:
409 — RESERVATION_ALREADY_CANCELLED.

## Kullanılabilir bağışları listeleme

GET /api/v1/donations/available?page=0&size=10

Yalnızca kullanılabilir paketi kalan bağışlar döner.

- page sıfırdan başlar.
- size, sayfa başına en fazla kayıt sayısıdır.
- content, istenen sayfanın kayıtlarını taşır.
- totalElements, filtreye uyan toplam kayıt sayısıdır.
- totalPages, toplam sayfa sayısıdır.

Örnek: 7 uygun bağışta page=2 ve size=3 istenirse,
content içinde 1 kayıt bulunur. Toplam sayfa sayısı 3'tür.

Listeleme, paket garantisi vermez. Rezervasyon oluşturulurken
miktar yeniden kontrol edilir.

## Veri bütünlüğü

- Foreign key, rezervasyonun var olan bir bağışa bağlanmasını sağlar.
- CHECK kısıtları, geçersiz miktar ve durumları engeller.
- @Version, eski sürümle yapılan güncellemeleri tespit eder.
- Transaction, ilgili değişikliklerin birlikte tamamlanmasını sağlar.

## Testleri çalıştırma

Docker çalışırken sharing-service klasöründe:

```bash
./mvnw test
```

Testcontainers, test PostgreSQL container'ını başlatır.
Flyway gerekli tabloları oluşturur.

Bu adım sonunda yerelde doğrulanan sonuç: 52 başarılı test.

## Continuous Integration

Workflow:
.github/workflows/sharing-service-ci.yml

GitHub Actions, push ve pull request olaylarında:
1. Kodu indirir.
2. Java 25 ortamını hazırlar.
3. Maven Wrapper ile verify çalıştırır.

İlk CI çalıştırması başarıyla tamamlandı.

## Sıradaki problem

Aynı rezervasyon oluşturma isteği tekrar gönderildiğinde
yeni bir rezervasyon oluşabilir.

Sonraki güvenilirlik adımımız, bu durumu idempotency ile ele almak.