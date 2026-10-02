@echo off
setlocal EnableExtensions
set "MVN_VERSION=3.9.12"
set "CACHE=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MVN_VERSION%"
set "MVN_HOME=%CACHE%\apache-maven-%MVN_VERSION%"
if not exist "%MVN_HOME%\bin\mvn.cmd" (
  echo [CASA O NADA] Descargando Maven %MVN_VERSION%...
  if not exist "%CACHE%" mkdir "%CACHE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $u='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MVN_VERSION%/apache-maven-%MVN_VERSION%-bin.zip'; $z='%CACHE%\\maven.zip'; Invoke-WebRequest -Uri $u -OutFile $z; Expand-Archive -Path $z -DestinationPath '%CACHE%' -Force; Remove-Item $z -Force"
  if errorlevel 1 exit /b 1
)
call "%MVN_HOME%\bin\mvn.cmd" %*
