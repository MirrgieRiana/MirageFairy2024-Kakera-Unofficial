#!/usr/bin/env bash

usage() {
  echo "USAGE: $0 <version>" >&2
  exit 1
}

(($# > 0)) || usage

version=$1
shift

(($# == 0)) || usage


cd "$(dirname "$0")"


echo "Version: $version"

[[ `git status --porcelain` ]] && {
  echo "未コミットの内容があります" >&2
  git status
  exit 1
}

echo "バージョン更新中..."
content=$(cat gradle.properties)
echo "$content" | version="$version" perl -lpE 's/^(mod_version=)(\d+\.\d+\.\d+)$/"$1$ENV{version}"/ge' -- - > gradle.properties || exit

echo "コミット中..."
git add gradle.properties || exit
git commit -m "v$version" || exit

echo "タグを追加中..."
git tag "v$version" || exit

echo "リポジトリをpush中..."
git push || exit
git push --tag || exit

echo "完了"
