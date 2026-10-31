# Ishchi bazaga o'tkazish — MobilOstatka 2.3.0

Fayl: `D:\data\MobilOstatka_ISHCHI_2.3.0.cfe` (ichida `МинВерсия = "1.6"` — hozirgi telefonlar bloklanmaydi).
Unda 2.0.2 ning hammasi + xodim bo'limlari, amallar/kirishlar tarixi, majburiy token.
2.3.0 da yangi: telefonni xodimga biriktirish (boshqa telefondan kirish — faqat administrator ruxsati bilan),
xodim kartasida telefon raqami, SIM-kartadagi raqam (aniqlansa) kirishlar tarixida.

**Muhim (2.3.0):** yuklagandan keyin har bir xodim KEYINGI kirgan telefoni unga avtomatik biriktiriladi.
O'zingiz (administrator) bir nechta telefon/kompyuterdan kirsangiz — «Xodimlar»da o'zingizni ochib
**«Faqat biriktirilgan telefondan»** ni o'chiring. Xodim telefonini almashtirsa — «Xodimlar»da «Yangi telefon»
belgisi chiqadi → oching → **«Ruxsat berish»**.

## 0. Zaxira (oldin)
- [ ] Bazaning `.dt` nusxasi olindi.
- [ ] Hozirgi kengaytma saqlandi: Конфигурация → Расширения конфигурации → MobilOstatka →
      Конфигурация → **Сохранить в файл…** → `MobilOstatka_eski.cfe`.

## 1. Kengaytmani yuklash (ish vaqtidan tashqarida tavsiya etiladi)
- [ ] Расширения конфигурации → MobilOstatka → Конфигурация → **Загрузить из файла…** →
      `MobilOstatka_ISHCHI_2.3.0.cfe`.
- [ ] **F7** (Обновить конфигурацию базы данных).
- [ ] Kengaytmalar ro'yxatida MobilOstatka: «Активно» — belgili, «Безопасный режим» — avvalgidek.

## 2. Tekshirish (eski ilova bilan, hech narsa buzilmagan bo'lishi kerak)
- [ ] Telefonda hozirgi ilova bilan kirish, Kunlik sanoq, Ichki sanoq ishlaydi.
- [ ] Brauzerda: `http://87.192.241.38:8090/doms2021/hs/ostatka/versiya` → `"min": "1.6"`.

## 3. Yangi APK 2.3.0
- [ ] Android Studio → `D:\claude\1c_app\android` → Sync Project with Gradle Files → Build APK(s).
      (Java o'zgargan — `GeoGuard.info()`, shuning uchun APK albatta qayta yig'ilsin.)
- [ ] `app-debug.apk` → serverda `E:\DomStroyOstatka\domstroy.apk` (eskisining o'rniga).
- [ ] Bitta telefonga o'rnatib sinash: kirish, bosh sahifa, admin uchun «Amallar tarixi» / «Kirishlar tarixi».

## 4. Hammani yangilash
- [ ] 1C'da `НастройкиВерсии` → `МинВерсия = "2.3.0"` → saqlash → F7.
      (Avval APK serverda bo'lishi SHART, keyin МинВерсия.)

## 5. Xodimlar
- [ ] Administrator logini bilan ilovaga kiring → «Xodimlar» → **avval O'ZINGIZNI** qo'shing.
- [ ] Qolgan xodimlarni qo'shing: ism, PIN, 1C login, «Joylashuvni tekshirish», **ochiq bo'limlar**.
- Eski xodimlarda bo'limlar belgilanmagan bo'lsa — hammasi ochiq.

## Xodim ishdan ketsa
1. «Xodimlar» → xodimni oching → **«Faol»ni o'chiring** (yoki «Xodimni o'chirish»). Sessiyasi darhol tugaydi.
2. U ishlagan **1C loginining parolini 1C'da almashtiring** va shu loginga biriktirilgan qolgan xodimlarda
   «1C paroli» maydoniga yangisini yozing.
3. «Kirishlar tarixi»da uning keyingi urinishlari ko'rinadi («O'chirilgan xodim kirishga urindi»).

## Qaytarish (muammo bo'lsa)
- Расширения → MobilOstatka → Загрузить из файла… → `MobilOstatka_eski.cfe` → F7.
