# Payda: Bağış API’si ve paket ayırma

Bu adımda bağışları PostgreSQL’e kaydeden, HTTP üzerinden
okuyan ve paket ayırmayı sağlayan akışı geliştirdik.

## Çalıştırma

Gereksinimler: JDK 25 ve çalışan Docker.

Proje kökünde geliştirme veritabanını başlat:

```bash
docker compose up -d postgres
```

Servis klasörüne geçip uygulamayı çalıştır:

```bash
cd sharing-service
./mvnw spring-boot:run
```

Swagger UI: http://localhost:8080/swagger-ui/index.html

## API

| İşlem | Endpoint | Başarılı cevap |
|---|---|---|
| Bağış oluştur | POST /api/v1/donations | 201 |
| Bağış oku | GET /api/v1/donations/{id} | 200 |
| Paket ayır | POST /api/v1/donations/{id}/reserve | 200 |

Bağış oluşturma gövdesi:

```json
{
  "title": "Paketli ekmek",
  "totalPackageCount": 40
}
```

Paket ayırma gövdesi:

```json
{
  "packageCount": 15
}
```

Bu işlemden sonra toplam 40, ayrılmış 15, kullanılabilir 25 pakettir.

## Kodun görevleri

- api: HTTP istekleri, doğrulama, cevaplar ve hata eşleştirmeleri.
- application: İş akışı ve transaction sınırları.
- domain: Paket ayırma kuralları ve miktar hesaplamaları.
- persistence: JPA repository üzerinden veritabanı erişimi.

Paket ayırma miktarı mevcut miktara eklenir.
Bu nedenle endpoint bir işlem olarak POST kullanır.

## Hata davranışları

- Sıfır veya negatif miktar: 400.
- Bulunamayan bağış: 404.
- Yetersiz kullanılabilir paket: 409, INSUFFICIENT_PACKAGES.
- Sürüm çatışması: 409, DONATION_CONCURRENT_MODIFICATION.

## Eşzamanlı güncelleme

Donation üzerindeki @Version alanını Hibernate yönetir.

Başka bir transaction bağışı değiştirmişse eski sürümle yapılan
güncelleme reddedilir. @Transactional tek başına bu sürüm
kontrolünü sağlamaz.

Testte iki ayrı transaction'ın çalışma sırası kontrol edilerek
eski sürümle güncelleme senaryosu oluşturulur.
Bu test bir yük testi değildir.

## Testler

Docker çalışırken sharing-service klasöründe:

```bash
./mvnw test
```

Testcontainers, geliştirme veritabanından ayrı PostgreSQL
container'ı başlatır. Test için uygulamayı ayrıca başlatmak gerekmez.

Bu adım sonunda doğrulanan sonuç: 19 test, 0 hata.

## Migration yaklaşımı

Şema değişiklikleri Flyway migration dosyalarıyla uygulanır.
Uygulanmış migration dosyaları değiştirilmez; yeni migration eklenir.

## Mevcut kapsam

Paket ayırma işlemi bağış üzerindeki miktarları günceller.
Henüz rezervasyon sahibi, iptal veya teslim takibi bulunmaz.