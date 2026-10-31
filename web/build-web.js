// Web versiya (iPhone va boshqa brauzerlar uchun) — www/index.html dan yig'iladi. Asl fayl o'zgarmaydi.
//   node build-web.js          -> dist\       (ishchi baza: <sayt>/doms2021/hs/ostatka/)
//   node build-web.js test     -> dist-test\  (test baza:   <sayt>/TestClaude/hs/ostatka/)
// Sahifa 1C publikatsiyasi bilan BIR saytda (bir xil domen va port) turishi kerak — shunda brauzer
// so'rovlarni to'smaydi (CORS). iPhone joylashuvni faqat HTTPS'da beradi.
const fs = require("fs");
const path = require("path");

const test = process.argv[2] === "test";
const SRC = path.join(__dirname, "..", "www", "index.html");
const OUT = path.join(__dirname, test ? "dist-test" : "dist");
const ICON = path.join(__dirname, "..", "android", "app", "src", "main", "res", "mipmap-xxxhdpi", "ic_launcher.png");

let s = fs.readFileSync(SRC, "utf8");
const rootOld = 'ROOT="http://87.192.241.38:8090/doms2021/hs/ostatka/"';
if (!s.includes(rootOld)) throw new Error("ROOT topilmadi: " + rootOld);
s = s.replace(rootOld, `ROOT=location.origin+"/${test ? "TestClaude" : "doms2021"}/hs/ostatka/"`);

const head =
  '<link rel="manifest" href="manifest.webmanifest">\n' +
  '<meta name="apple-mobile-web-app-capable" content="yes">\n' +
  '<meta name="mobile-web-app-capable" content="yes">\n' +
  '<meta name="apple-mobile-web-app-status-bar-style" content="default">\n' +
  '<meta name="apple-mobile-web-app-title" content="Dom Stroy">\n' +
  '<link rel="apple-touch-icon" href="icon-192.png">\n' +
  '<link rel="icon" type="image/png" href="icon-192.png">\n';
if (!s.includes("<title>Dom Stroy</title>")) throw new Error("<title> topilmadi");
s = s.replace("<title>Dom Stroy</title>", (test ? "<title>Dom Stroy TEST</title>\n" : "<title>Dom Stroy</title>\n") + head);

fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, "index.html"), s);
fs.copyFileSync(ICON, path.join(OUT, "icon-192.png"));
fs.writeFileSync(
  path.join(OUT, "manifest.webmanifest"),
  JSON.stringify(
    {
      name: test ? "Dom Stroy TEST" : "Dom Stroy",
      short_name: test ? "DS TEST" : "Dom Stroy",
      start_url: "./",
      scope: "./",
      display: "standalone",
      background_color: "#FFFFFF",
      theme_color: "#FFFFFF",
      icons: [{ src: "icon-192.png", sizes: "192x192", type: "image/png" }],
    },
    null,
    1
  )
);
// IIS: .webmanifest turini va keshsiz index.html ni bildiradi (har safar yangi versiya)
fs.writeFileSync(
  path.join(OUT, "web.config"),
  `<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <system.webServer>
    <staticContent>
      <remove fileExtension=".webmanifest" />
      <mimeMap fileExtension=".webmanifest" mimeType="application/manifest+json" />
    </staticContent>
    <httpProtocol>
      <customHeaders>
        <add name="Cache-Control" value="no-cache" />
      </customHeaders>
    </httpProtocol>
  </system.webServer>
</configuration>
`
);
const ver = (s.match(/const APP_VER="([^"]+)"/) || [])[1];
console.log("Tayyor:", OUT, "versiya", ver);
