# コントリビューター行動規範

本プロジェクトにはまだコントリビューターの言動に具体的に言及する条項は整備されておりませんが、2022年10月10日以降のMirageFairyプロジェクトにおいて、形を変えながら受け継がれている以下のポエムがあります。

> MirageFairyプロジェクトは、一人の主要な貢献者および、有志の貢献者のボランティアにより、無償で提供されております。
>
> 主要な貢献者は、マイナス100%の営利と200%の愛によってMirageFairyプロジェクトでの活動を行っています。
>
> 貢献者およびプレイヤーは、他の貢献者やプレイヤーのお客様でも上司でも召使いでも神様でもありません。
>
> MirageFairyプロジェクトは、自己だけでなく、すべての関係者がともに楽しく過ごせることを心から願う人のために存在します。
>
> ぜひ、愛を持ってご利用ください。
>
> —— 『MirageFairyプロジェクトへの理解 2023年11月版』

# Issues

以下のようなIssueはいつでも歓迎しております。

- 不具合・バグの報告
- ゲームバランス上の致命的な問題
- 他MODとの連携上の問題

不具合等に関しては、Issueのスレッド上でそれが発生する具体的なメカニズムの解明のためのやり取りが必要な場合があります。

---

以下のようなIssueも歓迎しておりますが、こちらについてはスレッド内で採用の可否を検討する必要がある場合があります。

- ゲームバランスに影響する仕様の変更や提案
- 機能の追加の提案
- 致命的というほどではないゲームバランス上の問題
- 他MODとの連携の新たなサポート

# Pull Request

Pull Requestはいつでも歓迎しております。

いくつかのルールがあります。

## コミットメッセージ規約について

[コミットメッセージ規約](#コミットメッセージ規約)

これは本プロジェクトのドキュメントを記述する際に使われ、コミットメッセージがこれに則っていない場合、ドキュメントの記述コストが少し上がります。

## コントリビューションの著作権およびライセンスに関して

コントリビューションは、以下のドキュメントによる記載により、プロジェクトと同じライセンスとして取り込まれます。

- [GitHub のサービス使用条件 - D - 6](https://docs.github.com/ja/site-policy/github-terms/github-terms-of-service#6-contributions-under-repository-license)
- [Apache License 2.0 - 5](https://github.com/MirageFairy/MirageFairy2024/blob/main/LICENSE#L130)

著作権が発生する程度の規模のコントリビューションでは、コントリビューターがその部分の著作権を保持します。

そうでない些細な変更については、著作権はプロジェクトの主要な貢献者に譲渡されます。

# コミットメッセージ規約

本プロジェクトでは、ゲーム内容に影響のある変更を手動でリストアップするために、伝統的に次のような規則が使われています。

```
[<スコープ>: ]<変更の種類>:[ <自由な変更内容の記述>]

# 例

ADD: アイテムAを追加
VIEW: change: foo_block: texture
CREATIVE: add: FooDebugItem
doc: ItemStack.getIdentifier
format:
refactor: name
```

## スコープ

ゲーム性に影響しない部分について、その部分を以下の例に基づいて（雰囲気で）分類します。

| スコープ     | 説明                   |
|----------|----------------------|
| test     | テストコードの変更            |
| build    | GradleやIDEに関する変更     |
| doc      | KDocの変更              |
| ci       | GitHub Actionsに関する変更 |
| view     | テクスチャやテキストなど見た目のみの変更 |
| creative | クリエイティブ専用アイテムの変更     |

## 変更の種類

変更の種類を以下の例に基づいて（雰囲気で）分類します。

| 変更の種類    | 説明                  |
|----------|---------------------|
| add      | ゲームコンテンツに対する追加・機能追加 |
| remove   | ゲームコンテンツに対する削除・機能削除 |
| change   | ゲームコンテンツに対する破壊的変更   |
| internal | ソースコード上のみの変更        |
| refactor | ソースコード構造の等価的な変更     |
| format   | ソースコードの整形           |
| cleanup  | ソースコードの余分な部分の削除     |
| comment  | ソースコー上のコメントの編集      |
| fix      | 意図しない挙動や記述等の修正      |
| ignore   | .gitignoreへの追加      |
| use      | 依存ライブラリの追加          |

## ゲーム内容に影響がある際の大文字化

**ゲーム内容に影響がある変更は、 `ADD: ミラージュの花` のように左端の節をすべて大文字で書きます。**

これには見た目の変更およびクリエイティブ専用の変更も含みます。

## 従属コミット

何らかの理由でコミットし損ねた変更を後から追加するだけのコミットは、タイトルを `_` 1個だけで構成することができます。

ただし、未pushのコミットは、squashするなどして手元で最大限に綺麗にしなければなりません。

## メタ的なファイルのコミット

`.gitignore`, `README.md`, GitHub Actionsに関するファイルなど、MODプログラムおよびGradleに関わらないファイルへの変更は、 `Update .gitignore` のようにデフォルトのコミットタイトルを付けることができます。