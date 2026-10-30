function Import-CasaEnvironment([string]$Root){
 if(Test-Path "$Root/.env"){Get-Content "$Root/.env" | ForEach-Object {if($_ -match '^([A-Z_][A-Z0-9_]*)=(.*)$'){[Environment]::SetEnvironmentVariable($Matches[1],$Matches[2],'Process')}}}
}
function Get-CasaJava {
 $javaExe=if($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME/bin/java.exe")){"$env:JAVA_HOME/bin/java.exe"}else{(Get-Command java -ErrorAction Stop).Source}
 $version=(& $javaExe --version 2>&1 | Out-String)
 if($version -notmatch '(?:openjdk|java)\s+(\d+)' -or [int]$Matches[1] -lt 21){throw 'Configure JAVA_HOME y PATH con Java 21.'}
 return $javaExe
}
function Save-CasaProcesses([string]$Root,$Entries){ConvertTo-Json -InputObject @($Entries) -Depth 5 | Set-Content "$Root/.run/processes.json" -Encoding UTF8}
function Wait-CasaHttp([string]$Url,[int]$Seconds=180,$Process=$null){
 $deadline=(Get-Date).AddSeconds($Seconds)
 do{
  if($Process){$Process.Refresh();if($Process.HasExited){throw "El proceso termino con codigo $($Process.ExitCode) antes de estar listo."}}
  try{$null=Invoke-RestMethod -Uri $Url -TimeoutSec 3;return}catch{Start-Sleep -Seconds 2}
 }while((Get-Date)-lt $deadline)
 throw "No respondio $Url dentro del plazo."
}
function Start-CasaProcess([string]$Root,[string]$Module,[int]$Port,[string]$JavaExe){
 $jar=Get-ChildItem "$Root/$Module/target/*.jar" | Where-Object {$_.Name -notlike '*.original'} | Select-Object -First 1
 if(-not $jar){throw "Falta el JAR de $Module. Ejecute la compilacion."}
 $heap=if($env:CASA_JAVA_MAX_HEAP){$env:CASA_JAVA_MAX_HEAP}else{'256m'}
 if($heap -notmatch '^[1-9][0-9]*[mMgG]$'){throw 'CASA_JAVA_MAX_HEAP debe indicar memoria como 256m o 512m.'}
 $name=Split-Path -Leaf $Module
 $p=Start-Process -FilePath $JavaExe -ArgumentList @('-Xms64m',"-Xmx$heap",'-XX:+ExitOnOutOfMemoryError','-jar',('"'+$jar.FullName+'"')) -WorkingDirectory $Root -WindowStyle Hidden -RedirectStandardOutput "$Root/.run/$name.out.log" -RedirectStandardError "$Root/.run/$name.err.log" -PassThru
 return $p
}
