#!/usr/bin/env sh
set -eu
MVN_VERSION=3.9.12
CACHE="$HOME/.m2/wrapper/dists/apache-maven-$MVN_VERSION"
MVN_HOME="$CACHE/apache-maven-$MVN_VERSION"
if [ ! -x "$MVN_HOME/bin/mvn" ]; then
  echo "[CASA O NADA] Descargando Maven $MVN_VERSION..."
  mkdir -p "$CACHE"
  URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MVN_VERSION/apache-maven-$MVN_VERSION-bin.tar.gz"
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$URL" -o "$CACHE/maven.tar.gz"; else wget -q "$URL" -O "$CACHE/maven.tar.gz"; fi
  tar -xzf "$CACHE/maven.tar.gz" -C "$CACHE"
  rm -f "$CACHE/maven.tar.gz"
fi
exec "$MVN_HOME/bin/mvn" "$@"
