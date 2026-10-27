# Payda

Gıda fazlasını ihtiyaç bildiren kuruluşlar ve gönüllülerle
buluşturmayı hedefleyen dayanışma platformu.

Java ve Spring Boot ile adım adım geliştiriliyor.
Her özellikte problemi, tasarım kararını ve testleri açıklıyoruz.

## İlk adım: bağış oluşturma

İlk servisimiz `sharing-service`.

`Donation` sınıfı bağışın başlığını ve toplam paket sayısını taşır.
Başlık boş olamaz; paket sayısı sıfırdan büyük olmalıdır.
Bu kurallar constructor içinde uygulanır.

İlk yerel çalıştırmada proje genelinde 4 test başarıyla geçti.

## Testleri çalıştırma

Gereksinim: Java 25. Proje Spring Boot 4.1.2 kullanır.

```bash
cd sharing-service
./mvnw test
```

## Yol haritası

Rezervasyon, PostgreSQL, güvenlik, Kafka, Elasticsearch,
AI destekli eşleştirme ve teslimat planlama, gözlemlenebilirlik
ve bulut dağıtımı üzerinde ilerleyeceğiz.

## Katkı

Sorular ve öneriler için Issue, kod ve anlatım iyileştirmeleri
için Pull Request açabilirsiniz.