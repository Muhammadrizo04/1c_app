# Web versiyaning TEST nusxasini shu kompyuterdagi IIS'ga qo'yadi (TestClaude publikatsiyasi yoniga).
# Administrator sifatida ishga tushiring:
#   PowerShell (Admin) ->  powershell -ExecutionPolicy Bypass -File D:\claude\1c_app\web\test-iis-joylash.ps1
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
node build-web.js test
$dst = "C:\inetpub\wwwroot\ilova\web"
New-Item -ItemType Directory -Force $dst | Out-Null
Copy-Item "$PSScriptRoot\dist-test\*" $dst -Force
Write-Host "Ko'chirildi: $dst"
Get-ChildItem $dst | Select-Object Name, Length | Format-Table
try {
  $r = Invoke-WebRequest "http://127.0.0.1/ilova/web/" -UseBasicParsing
  Write-Host "IIS javobi: $($r.StatusCode) (sahifa ochildi)"
} catch { Write-Host "IIS javobi: XATO - $($_.Exception.Message)" }
Write-Host ""
Write-Host "HTTPS bog'lanishlari (443):"
Get-WebBinding -Protocol https -ErrorAction SilentlyContinue | Format-Table protocol, bindingInformation
