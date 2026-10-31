# Dom Stroy — ostatka ilovasi (telefon / TSD) + 1C

Bu loyiha avval claude.ai chatida boshlangan, 2026-10-15 da Code bo‘limiga ko‘chirildi.
Foydalanuvchi o‘zbekcha yoki ruscha yozadi — o‘sha tilda javob ber.

## Ilova
- Capacitor 8 + Android, appId `uz.ostatka.app`, nomi «Dom Stroy».
- Ishchi fayl: `www/index.html` (bitta HTML ichida hammasi). `www/index N.html` — chatdagi eski versiyalar, tegma.
- 1C HTTP-servisi `.../hs/ostatka/` ga murojaat qiladi (ishchi server `87.192.241.38:8090/doms2021`, ngrok orqali `/mobil/hs/ostatka/`).
- Bor funksiyalar: foydalanuvchi + PIN bilan kirish, barmoq izi (biometric), qoldiqlarni qidirish, «Kunlik sanoq» / «Ichki sanoq» (ОстатканиТугирлаш hujjati), sozlamalar.

## 1C
- Konfiguratsiya «КСБ» (KSB SOFT) v1.0.24.02, platforma 8.3.13.1690 (x86):
  `C:\Program Files (x86)\1cv8\8.3.13.1690\bin\1cv8.exe`
- Test baza: `Srvr="localhost";Ref="TestClaude"`, foydalanuvchi `Claude`, parolsiz. Konfiguratorga ham kirish mumkin.
- Ishchi baza `Srvr="server";Ref="doms2021"` — HECH QACHON tegma. SQL ga to‘g‘ridan-to‘g‘ri yozma.
- Konfiguratsiya dampi: `D:\data\config_dump`, kengaytmalar: `D:\data\ext_dump\<nomi>`.
  Damp: `1cv8.exe DESIGNER /S localhost\TestClaude /N Claude /DumpConfigToFiles <dir> [-Extension <nomi>]`
- `/hs/ostatka/` servisi — asosiy bazadagi kengaytma **Расш1_Ostatka** (test bazada yo‘q). Ish ASOSIY bazada davom etadi,
  hammasi u yerda sinalgan. Modul kodi nusxasi: `1c/Rassh1_Ostatka_modul.bsl`. Men 1C kodini yozaman,
  foydalanuvchi o‘zi Konfiguratorda asosiy bazaga qo‘yadi — asosiy bazaga o‘zim ulanmayman.
- Majburiy yangilash: 1C `НастройкиВерсии()` (МинВерсия, APK havolasi `http://87.192.241.38:8090/ilova/domstroy.apk`,
  БезВерсииБлок). Ilova `APP_VER` ni `X-App-Version` sarlavhasida yuboradi; eski bo‘lsa 1C **200** + `"update":true`
  qaytaradi va ilova to‘liq ekran «Yangilash» oynasini ochadi. 426 ISHLATMA: ishchi veb-server uni
  400 «Некорректные символы в заголовке HTTP» ga aylantiradi (2026-10-15).
  APK serverda IIS orqali `E:\DomStroyOstatka` papkasidan beriladi (server kompyuterida, bu kompyuterda emas).
- Yangi versiya chiqarish: `APP_VER` (index.html) + `versionName`/`versionCode` (android/app/build.gradle) ni oshirish →
  `npx cap sync android` → Android Studio’da APK → `E:\DomStroyOstatka\domstroy.apk` → 1C’da МинВерсия ni oshirish.
- Holat 2026-10-15: telefonlarda 1.2, asosiy bazada МинВерсия = "1.2" — 1.1→1.2 yangilanish telefonda muvaffaqiyatli sinaldi.
  Ilova ichidan yuklab olish (ApkUpdater) hali haqiqiy yangilanishda sinalmagan — 1.3 chiqqanda tekshirish.
- 1.2 dan boshlab APK’ni ilovaning o‘zi yuklaydi va o‘rnatishni ochadi: native plagin `ApkUpdater`
  (android/app/src/main/java/uz/ostatka/app/ApkUpdater.java, MainActivity’da registerPlugin) — brauzer
  ishlatilmaydi, server IP ko‘rinmaydi. Manifestda REQUEST_INSTALL_PACKAGES.
- 1.4: do'kon hududi (GPS). Native plagin `GeoGuard` (joylashuv + soxta GPS aniqlash), ilova har so'rovga
  `X-Geo: lat,lon,acc,mock` yoki `err:KOD` qo'shadi. Qarorni 1C `ПроверкаЛокации` qiladi (`НастройкиЛокации`:
  do'kon 40.527056, 70.953573, radius 300 m, МаксТочность 150, Исключения — loginlar). Uzoqda bo'lsa
  200 + `{"geo":true,code,error}` → ilovada to'liq ekran oynasi. 1.4 dan eski ilovalar yangilashga yuboriladi.
- 1.7: «Ko'chirish» — omborlar o'rtasida (`СкладларУртасидаУтказиш`). `/omborlar` (filialning barcha faol omborlari; tranzit omborlarni YASHIRMA — VITRINA/Brak ishchi omborlar),
  `/kochirish` POST → hujjat DARHOL o'tkaziladi; `ТоварКабулКилинди` HECH QACHON qo'yilmaydi (foydalanuvchi talabi,
  2026-10-15; tranzitli omborga tovar tranzitga tushadi, qabul 1C'da); faqat o'z filiali.
  Изох = "Mobil ilova: o'tkazma — <Xodim>" (uid izohda YO'Q). Takror yuborishdan himoya: uid → hujjat
  `ХранилищеОбщихНастроек("MobilOstatka.Otkazma", uid, ..., "MobilOstatka")` (`ОтказмаПоUID`). Mantiq `Кочирить(Данные)`.
- Versiya 2.0.3 (versionCode 16): 2.0.2 + Kunlik sanoqda «Hammasini tozalash». Ishchi .cfe: D:\data\MobilOstatka_ISHCHI_2.0.2.cfe (МинВерсия 1.6; 1C kodi 2.0.3 uchun o'zgarmagan).
- Versiya 2.1.0 (versionCode 17, 2026-10-16). Ishchi .cfe: D:\data\MobilOstatka_ISHCHI_2.1.0.cfe (МинВерсия 1.6). TestClaude'da sinalgan.
  - Xodim rejimida token HAR QANDAY versiyada shart (1.11 istisnosi olib tashlandi).
  - Xodimga bo'limlar: `Булимлар` (satr "kunlik,ichki,otkazma,qoldiq"; yo'q = hammasi). 1C har endpointda tekshiradi
    (`ПроверкаБулима`, yopiq bo'lsa 403 `{"ruxsat":false}`); `/kirish` va `/versiya` (token bilan) `bolimlar` qaytaradi;
    ilova plitkalarni yashiradi va plitka bosilganda `/versiya` bilan qayta tekshiradi (`gate`). skladlar — hammaga ochiq.
  - Tarix: `ХранилищеОбщихНастроек("MobilOstatka.Tarix", "A|yyyyMMdd|xodim" yoki "K|...")` (kun+xodim bo'yicha massiv),
    `ПолучитьСписок` bilan o'qiladi, 180 kundan eskisi o'chiriladi. O'qish: `/xodimlar?tarix=amal|kirish&kun=&xodim=` (admin).
    Yoziladi: kunlik (InvPost), ichki (saqlash/o'tkazish), o'tkazma, xodimlar o'zgarishi, har bir kirish urinishi.
  - Qurilma: ilova `/kirish` ga `X-Qurilma` (URL-encode, "Model · Android N") va `X-Qurilma-Id` (ANDROID_ID —
    `GeoGuard.info()`; brauzerda LS "devId") yuboradi; xodimda `Курилма/КурилмаИд`, boshqa ID bo'lsa «YANGI QURILMA».
  - Ilovada admin plitkalari «Amallar tarixi» (bh), «Kirishlar tarixi» (bkr), ekran `th`. Xodim oynasida bo'lim kalitlari.
- 2.1.1 (versionCode 18, 2026-10-17): Ichki sanoq filtrlari. 1C'da «sanalmagan» va «fakt 0» farqlanmaydi (ikkalasi Факт=0,
  Сони=−qoldiq), shuning uchun ilovadan yozilgan qatorlar `ХранилищеОбщихНастроек("MobilOstatka.Sanaldi", <hujjat UUID>)`
  da (Соответствие "товар|серия") saqlanadi (HujjatPost, InvPost; o'tkazilganda o'chiriladi). SANALGAN = Факт≠0 yoki ro'yxatda;
  FARQLI = sanalgan va Факт≠Учет; Сони ustuniga TAYANMA. Hisob HujjatGet/HujjatlarGet kodida. Ilovada sanalmagan qatorda farq «—».
  Ishchi .cfe: D:\data\MobilOstatka_ISHCHI_2.1.1.cfe.
- 2.1.2 (versionCode 19, 2026-10-17): Qoldiqlar filtri — omborlar emas, FILIALLAR (`/omborlar?filiallar=1` →
  [{id,name,meniki}], o'z filiali birinchi). `/qoldiq/{q}?filial=` va `/qoldiqlar/{id}?filial=` (`ФилиалПоИд`); eski `sklad`
  parametri ham ishlaydi. Ilova LS: qsF/qsFn. Ishchi .cfe: D:\data\MobilOstatka_ISHCHI_2.1.2.cfe.
  2026-10-17: foydalanuvchi ishchi bazaga 2.1.2 ni o'tkazdi va «Hammasi yaxshi» dedi.
- 2.1.3 (versionCode 20): Xodimlar ro'yxatida «Bloklangan» belgisi klassi `blk` edi — u to'liq ekran oynalar (#geo/#upd)
  klassi bilan to'qnashib, butun ekranni yopardi. Endi `.xs .st.lock`. Faqat ilova o'zgardi (1C emas).
  CSS klass nomlarida `.blk` ni boshqa joyda ISHLATMA.
- 2.2.0 (versionCode 21, 2026-10-19): telefonni biriktirish. Xodim strukturasi: Телефон, SIM (oxirgi), Богла (yo'q=Истина),
  БогИд/БогНом/БогСана (biriktirilgan), КутИд/КутНом/КутСана/КутSIM (ruxsat kutayotgan). KirishPost: Богла va ID yo'q → 409
  «Ilovani yangilang»; БогИд bo'sh → birinchi telefon biriktiriladi; boshqa ID → Кут* yoziladi, 409. XodimlarPost: telefon,
  boglash, qurilma = ruxsat|rad|tozalash. Maydonlar `ПолеХодима(Х, Имя, Умолч)` bilan o'qiladi (eski yozuvlar).
  Ilova: `GeoGuard.sim()` (READ_PHONE_STATE + READ_PHONE_NUMBERS, ko'p SIM'da raqam bo'sh) → `X-Sim`.
  Tarixda bir soniyadagi yozuvlar tartibi saqlanadi. Ishchi .cfe: D:\data\MobilOstatka_ISHCHI_2.2.0.cfe.
  TestClaude: Aziz Karimov — «Faqat biriktirilgan telefondan» o'chiq (brauzer sinovlari uchun).
- 2.3.0 (versionCode 22, 2026-10-29): Радиус 450 m. Joylashuv har so'rovda EMAS: `НастройкиЛокации().Интервал` = 20 daq.
  1C oxirgi muvaffaqiyatli tekshiruv vaqtini `ХранилищеОбщихНастроек("MobilOstatka.Geo", <xodim yoki "1c:login">)` da
  saqlaydi (kirishda va uzoqda bo'lsa tozalanadi). X-Geo bor → tekshiradi; yo'q va muddat o'tgan → `{"geo":true,"code":"need"}`.
  Ilova `geoDue` (19.5 daq) bo'yicha GPS oladi, "need" kelsa GPS bilan bir marta qayta yuboradi. Eski ilovalar har so'rovda yuboradi.
- WEB versiya (iPhone): `web/build-web.js` → `web/dist` (ROOT = location.origin + "/doms2021/hs/ostatka/"), manifest, ikonka,
  web.config. 1C publikatsiyasi bilan bir saytda + HTTPS shart (GPS). Yo'riqnoma: `web/WEB_VERSIYA.md`. `WEB` belgisi —
  yangilash = location.reload(). Kompyuterda sinash: capture/server.js `/web/` (dist-test).
  - Ilovani kompyuterda TestClaude bilan sinash: `D:\claude\video\capture` (server.js — proksi :8800, launch.js + step.js — puppeteer).
- 1.8: «Qoldiqlar» (faqat ko'rish). `/qoldiq/{q}?sklad=` (nom yoki aniq artikul; sklad yo'q — filial bo'yicha jami),
  `/qoldiqlar/{id}?seria=` (tovar omborlar kesimida). Test bazada sinalgan.
- 1.9: Qoldiqlar BARCHA filiallar bo'yicha (foydalanuvchi so'rovi): har tovarda `filiallar:[{name,soni,meniki}]`,
  omborlar kesimida `filial` nomi. Ombor tanlansa — faqat shu ombor (filial cheklovisiz).
- 1.10: «Ko'chirish» UI'da «O'tkazma» deb ataladi (ichki nomlar kc/kochirish o'zgarmagan). Kunlik sanoq: 1C'ga faqat
  farqli tovarlar ketadi; InvPost «baza»ni ilovadan (`uchet`) oladi; yuborishdan oldin hisobot (✅/❌) Telegram'ga
  yuborilishi yoki nusxalanishi SHART (`repH` izi). Native plagin `TextShare` (copy/share, Telegram paketlari).
- 1.11: XODIMLAR (ilovaning o'z foydalanuvchilari). Kengaytmaga registr qo'shib BO'LMAYDI: asosiy konfiguratsiya
  moslik rejimi 8.3.9, kengaytma undan yuqori bo'lolmaydi, 8.3.9 da kengaytma ma'lumot obyektlari taqiqlangan
  (asosiy konfiguratsiyani o'zgartirish taqiqlangan). Shuning uchun ro'yxat `ХранилищеОбщихНастроек`
  ("MobilOstatka.Xodimlar"/"list", foydalanuvchi "MobilOstatka") da — massiv strukturalar. PIN sha256(nom:pin) —
  umumiy modul `Расш1_МобилХодимлар`. `/kirish` (svc login bilan) → {login, password, token}; ilova `X-Xodim-Token`
  yuboradi; 1.11+ va faol xodim bo'lsa token SHART (aks holda {"relogin":true}). `/xodimlar` GET/POST — faqat
  ПолныеПрава/Администратор. Faol xodim yo'q bo'lsa — eski 1C-login rejimi. Изох: "... — <Xodim ismi>".
  Xodimga `GPS` (bool, yo'q bo'lsa = true): false bo'lsa do'kon hududi tekshirilmaydi (ПроверкаДоступа). 1C login
  ro'yxatdan tanlanadi (`/xodimlar?users=1`), aynan 1C'dagidek saqlanadi (ba'zi loginlar bo'sh joy bilan boshlanadi).
  Xodim rejimi o'chiq bo'lsa GPS istisnosi — `НастройкиЛокации().Исключения` (1C loginlar).
  TestClaude'da sinov xodimi: «Aziz Karimov» (1C login Claude, PIN 1234). Ishchi baza uchun: D:\data\MobilOstatka_1.11.cfe.
- Kirish tartibi (foydalanuvchi talabi): 1) `/versiya` (svc login) → eski bo'lsa yangilash oynasi, ro'yxat ham
  yuklanmaydi; 2) login/PIN (`/kirish` yoki 1C login uchun `/versiya` bilan GPS'siz tekshiruv); 3) GPS va `/skladlar`
  (`finishLogin`). GPS to'sasa — «Qayta tekshirish» `pendLogin` bilan davom etadi. Ilova ochilganda GPS so'ralmaydi.
- Foydalanuvchi ruxsati: TestClaude’ga shablon/modulni o‘zim yozaveraman (Konfigurator yopiq bo‘lsa).
- Test ilovaga o'tkazish: `powershell -ExecutionPolicy Bypass -File D:\claude\1c_test_app\sync-from-main.ps1`.
- TestClaude’dagi `MobilOstatka` = ishchi bazadan olingan `D:\data\MobilOstatka.cfe` (2026-10-15) + Omborlar/Kochirish
  shablonlari + `1c/Rassh1_Ostatka_modul.bsl`. O‘zim noldan yasagan kengaytma 404 bergan edi — doim ishchi .cfe dan boshla.
  Yangilash: dump → modulni almashtir → `/LoadConfigFromFiles <dir> -Extension MobilOstatka` + `/UpdateDBCfg -Extension MobilOstatka`.
  Konfigurator ochiq bo‘lsa yuklanmaydi — foydalanuvchidan yopishni so‘ra.
- Test bazada HTTP orqali sinash ishlaydi: `curl -u "Claude:" -H "X-App-Version: 1.7" -H "X-Geo: 40.527056,70.953573,10,0"
  http://127.0.0.1/TestClaude/hs/ostatka/...` (IIS, publikatsiya C:\inetpub\wwwroot\TestClaude, kengaytma servislari yoqilgan).
  2026-10-15: /kochirish haqiqiy sinaldi — hujjat D10002240 o‘tkazildi, qoldiq 29→28 / 0→1, uid takrori, qoldiqdan ko‘p, bir xil ombor.
- Test ilova: `D:\claude\1c_test_app` (1c_app nusxasi). appId `uz.ostatka.test`, nomi «Dom Stroy TEST», pastda «TEST BAZA»
  belgisi, server `http://87.192.241.38:9999/TestClaude/hs/ostatka/` (IIS, foydalanuvchi kompyuteri 192.168.100.37).
  Asosiy ilova bilan yonma-yon o‘rnatiladi. 1c_app’dagi o‘zgarishlarni unga ham ko‘chirish kerak (ROOT/appId/nom/belgidan tashqari).
- Test bazada kod sinash: COM (V83.COMConnector) Connect "Library not registered" beradi, kengaytma ilova moduli
  ham chaqirilmadi — runtime sinov ishlamadi; faqat `/CheckModules -Extension` bilan sintaksis tekshiriladi.
- `capacitor.config.json` da `androidScheme` QO‘YMA: telefonlarda https://localhost, o‘zgarsa localStorage (sanoqlar) yo‘qoladi.
- APK debug kalit bilan imzolanadi (foydalanuvchi qarori, 2026-10-15). Kalit `C:\Users\kerio_old\.android\debug.keystore`,
  zaxira: `D:\data\keys\debug.keystore`. Kalit yo‘qolsa, telefonlarda ilovani o‘chirib qayta o‘rnatish kerak bo‘ladi
  (2026-10-15 da shunday bo‘lgan). Yangi kompyuter/qayta o‘rnatishdan keyin zaxiradan qaytaring.
- Bu kompyuterda gradlew terminaldan ishlamaydi (loopback connection to‘silgan) — APK ni Android Studio’da yig‘ish.
- Ma’lumot manbalari (tovarlar, shtrixkodlar, qoldiqlar, narxlar, savdo, Telegram) to‘liq o‘rganilgan:
  **`D:\data\ИЗУЧЕНИЕ_БАЗЫ.md` — ishni boshlashdan oldin o‘qi.**

## Keyinga qoldirilgan: iPhone (2026-10-15)
- Tavsiya: PWA (ilova serverdan, Safari → «Ekranga qo'shish»). HTTPS shart (iPhone GPS faqat https da).
- Domen `dsyuksak.uz` → 62.171.137.170 (boshqa server, DNS ahost.uz). Do'kon routeri 87.192.241.38 (TP-Link):
  80/443 → 192.168.100.69 (HikCentral — TEGMA), 8090 → 192.168.100.30:80 (1C), 9999 → 192.168.100.37:80 (test).
- Reja: A `ilova` → 87.192.241.38; router `8443 → 192.168.100.30:443`; IIS'da sayt + sertifikat (DNS-01 tekshiruv,
  port 80 band). Foydalanuvchi tanlashi kerak: pullik 1 yillik sertifikat (tavsiya) yoki Let's Encrypt (har 2–3 oyda
  qo'lda TXT). Yoki 62.171.137.170 sizniki bo'lsa — teskari proksi. HTTPS'dan keyin Android ham https ga o'tkaziladi.

## Rejalar
1. TSD (shtrixkod terminali) uchun ilova — hozirgi ilovaga shtrixkod bo‘yicha qidirish va har skanda +1.
2. Telefon ilovasi (shu HTML asosida).
3. Hisobotlar uchun Telegram-bot (bazadagi mavjud Telegram mexanizmidan foydalanish).

Ochiq masalalar: PIN kodda ochiq yozilgan, ngrok, 1C login/parol telefonda saqlanadi, «Yakunlash» sanalmagan tovarlarni 0 qiladi.
Test baza uchun veb-server (IIS/Apache) hali o‘rnatilmagan.
