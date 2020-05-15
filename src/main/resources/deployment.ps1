# *** BEGIN *** Customizations
$sitename='confdataapi' # if you site is site1.azurewebsites.net
$username='$confdataapi' # Example: $username='$site1' (NOT 'site1\$site1', NOT 'site1\site1'. Also, note the single quotes.)
$password='vJez8xLRxutXy7hWQj9qeecig5ppktMhQf1bRs0ytPwjEwsFjG5t0qsNqKEs'
$warFilePath='C:\Users\idoga\Documents\Dev\confdata\target\confdata.war'
# *** END *** Customizations

$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(("{0}:{1}" -f $username, $password)))
$apiUrl = "https://$sitename.scm.azurewebsites.net/api/wardeploy"
Invoke-RestMethod -Uri $apiUrl -Headers @{Authorization=("Basic {0}" -f $base64AuthInfo)} -Method POST -InFile $warFilePath -ContentType "multipart/form-data"