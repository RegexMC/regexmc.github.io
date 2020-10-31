powershell -command "Get-ChildItem -Filter '*.java' -Recurse | Get-Content | Measure-Object -line"
pause