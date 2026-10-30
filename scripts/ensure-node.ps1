function Get-CasaNode([string]$Root) {
 $system=Get-Command node -ErrorAction SilentlyContinue
 if($system){$v=(& $system.Source -p 'process.versions.node');if($v -match '^24\.' -and [version]$v -ge [version]'24.15.0'){
  $npm=Get-Command npm.cmd -ErrorAction SilentlyContinue
  if($npm){return @{Node=$system.Source;Npm=Join-Path (Split-Path $npm.Source) 'node_modules/npm/bin/npm-cli.js'}}
 }}
 $version='24.22.2'
 $directory=Join-Path $Root ".tools/node-v$version-win-x64"
 if(-not (Test-Path "$directory/node.exe")){
  New-Item -ItemType Directory -Force (Join-Path $Root '.tools')|Out-Null
  $file="node-v$version-win-x64.zip"
  $archive=Join-Path $Root ".tools/$file"
  $oldProgress=$ProgressPreference
  $ProgressPreference='SilentlyContinue'
  try{
   Write-Host 'Preparando Node.js portable oficial para Angular; no cambia la instalacion del sistema.'
   Invoke-WebRequest -UseBasicParsing "https://nodejs.org/dist/v$version/$file" -OutFile $archive
   $sums=(Invoke-WebRequest -UseBasicParsing "https://nodejs.org/dist/v$version/SHASUMS256.txt").Content
   $expected=($sums -split "`n"|Where-Object {$_ -match ('\s'+[regex]::Escape($file)+'\s*$')}|Select-Object -First 1).Trim().Split(' ')[0]
   if(-not $expected -or (Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expected.ToLowerInvariant()){throw 'La descarga Node no coincide con su SHA256 oficial.'}
   Expand-Archive -LiteralPath $archive -DestinationPath (Join-Path $Root '.tools') -Force
  }finally{$ProgressPreference=$oldProgress}
 }
 return @{Node="$directory/node.exe";Npm="$directory/node_modules/npm/bin/npm-cli.js"}
}
