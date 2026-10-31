# Web versiya (iPhone uchun) — joylashtirish

Web versiya — telefondagi ilovaning o'zi, brauzerda ochiladi. iPhone'da Safari → «Bosh ekranga qo'shish»
qilinsa, oddiy ilovadek belgisi bilan ochiladi.

## Fayllar
- Yig'ish: `node D:\claude\1c_app\web\build-web.js` → `D:\claude\1c_app\web\dist\`
  (test baza uchun: `node build-web.js test` → `dist-test\`).
- `dist\` ichida: `index.html`, `manifest.webmanifest`, `icon-192.png`, `web.config`.
- Har safar `www\index.html` o'zgarsa (yangi versiya), qayta yig'ib serverga ko'chirish kerak.

## Shart: sahifa 1C publikatsiyasi bilan BIR saytda bo'lishi
Sahifa `/doms2021/hs/ostatka/` ga murojaat qiladi va manzilni sahifa ochilgan saytdan oladi.
Shuning uchun `dist\` ni server kompyuteridagi (192.168.100.30) IIS'ga, 1C publikatsiyasi turgan saytga qo'yiladi.
Masalan APK turgan papka yoniga: `E:\DomStroyOstatka\web\` → manzil `…/ilova/web/`.

## Shart: HTTPS
iPhone (va Android Chrome ham) joylashuvni (GPS) faqat **https://** sahifaga beradi. http'da ilova
«joylashuvga ruxsat yo'q» deydi va ishlamaydi.
Test kompyuterda (192.168.100.37) 30.10.2026 da shu tartibda qilindi va ishladi:
`https://ilovatest.dsyuksak.uz:9998/ilova/web/` (router qoidasi 16: 9998-9999 → .37, ichki port bo'sh;
IIS: http 9999 + https 9998). Ishchi server uchun xuddi shu, faqat nom/portlar boshqa:

1. **DNS** (ahost.uz paneli, «Название» maydoniga faqat qisqa nom): A-yozuv `ilova` → `87.192.241.38`.
2. **Router** (TP-Link, jadval to'la — 16 ta): qoida 15 ni `8090-8091` → `192.168.100.30`, ichki port bo'sh.
   80/443 va HikCentral qoidalariga (1–14) TEGMANG.
   Avval IIS'da (.30) **http 8090** bog'lanishi bo'lishi kerak, aks holda hozirgi telefonlar uziladi.
3. **IIS komponentlari**: «Статическое содержимое» va «Документ по умолчанию» o'rnatilgan bo'lsin
   (yo'q bo'lsa fayllar 0 bayt bo'lib keladi):
   `Enable-WindowsOptionalFeature -Online -FeatureName IIS-StaticContent,IIS-DefaultDocument`
4. **Firewall** (admin): `New-NetFirewallRule -DisplayName "Ilova HTTPS 8091" -Direction Inbound -Protocol TCP -LocalPort 8091 -Action Allow`
5. **Sertifikat** (Let's Encrypt, win-acme, admin PowerShell):
   - BIRINCHI marta faqat interaktiv: `wacs.exe` → `M` → Manual input → `ilova.dsyuksak.uz` → acme-dns →
     `https://auth.acme-dns.io` → chiqqan CNAME'ni ahost'ga qo'shish (`_acme-challenge.ilova` → `…auth.acme-dns.io`)
     → DNS'da ko'ringach Enter → RSA → Windows Certificate Store (WebHosting) → No installation steps.
   - Keyin: `wacs.exe --source manual --host ilova.dsyuksak.uz --validation acme-dns --acmednsserver https://auth.acme-dns.io --store certificatestore --installation iis --installationsiteid 1 --sslport 8091`
     (`installationsiteid` — 1C publikatsiyasi turgan sayt ID si). Yangilanish avtomatik (Task Scheduler, 09:00).
6. Tekshirish: `https://ilova.dsyuksak.uz:8091/ilova/web/` ochiladi, kirish ishlaydi.

## Xodimga ko'rsatma (iPhone)
1. Safari'da manzilni oching → pastdagi «Ulashish» → **«На экран Домой / Add to Home Screen»**.
2. Bosh ekrandagi «Dom Stroy» belgisidan oching, ism va PIN bilan kiring.
3. «Joylashuvdan foydalanish»ga **ruxsat bering**.

## Web versiyada farqlar
- Barmoq izi bilan kirish va SIM raqamini aniqlash yo'q (iPhone ruxsat bermaydi).
- Yangilash: «Yangilash» oynasida «Sahifani yangilash» — serverdagi yangi fayl yuklanadi (APK kerak emas).
- Telefonni biriktirish brauzer xotirasidagi ID bo'yicha ishlaydi. Safari ma'lumotlari tozalansa yoki boshqa
  brauzerdan ochilsa — yangi qurilma bo'lib ko'rinadi, administrator «Ruxsat berish»ni bosadi.
- Soxta GPS aniqlanmaydi (iPhone buni bermaydi).
