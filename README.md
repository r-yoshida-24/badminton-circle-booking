# バドミントンサークル出欠管理Webアプリ

LINE公式アカウントからLIFFアプリを起動し、LINE LoginのID TokenをSpring Boot側で検証してから出欠管理画面へ遷移する、バドミントンサークル向け出欠管理Webアプリです。

## システム概要
- LINE公式アカウントのメニューやメッセージから LIFF アプリを起動
- LIFF SDK で LINE Login 状態を確認し、未ログインなら LINE Login へ遷移
- `liff.getIDToken()` で取得した ID Token を Spring Boot `/auth/line` へ送信
- サーバー側で ID Token の署名・issuer・audience・expiration を検証
- 検証済み `sub` を LINE user ID として `members` テーブルと照合
- 登録済みかつ有効な会員のみ Spring Security セッションを作成
- 認証済みユーザーは `/attendance/**` と `/mypage/**` を利用可能
- `ADMIN` 権限は `/admin/**` へアクセス可能

## 使用技術
- Java 21
- Spring Boot 3.5.x
- Spring Security 6.5.x
- Spring MVC
- Thymeleaf
- Spring Data JPA
- Flyway
- MySQL 8
- Maven
- LIFF SDK
- Docker / Docker Compose

## 必要な環境
- JDK 21
- Maven 3.9+（または Maven Wrapper `./mvnw`）
- Docker / Docker Compose
- MySQL 8 を利用できる環境
- LINE Developers アカウント

## LINE Developersで必要な設定
1. LINE Login チャネルを作成
2. チャネル ID とチャネルシークレットを控える
3. LIFF アプリを作成し、LIFF ID を控える
4. 公式LINEアカウントをチャネルへ連携する
5. 必要に応じて Friendship API を有効化する

## LINE Loginチャネル設定
- Callback URL に本アプリの公開URLを設定してください
- OpenID Connect を有効にしてください
- ID Token が発行される設定を維持してください
- 署名検証のため、サーバーから `https://api.line.me/oauth2/v2.1/certs` へ到達できる必要があります

## LIFF設定
- Endpoint URL: `https://<公開URL>/liff-login`
- Scope: `profile` と `openid`
- Size: Full 推奨
- Scan QR / Multiple tabs は必要に応じて設定してください

## LINE公式アカウントとの連携方法
1. LINE公式アカウントを作成
2. LINE Developers コンソールで LINE Login チャネルと連携
3. リッチメニューやあいさつメッセージから LIFF URL を案内
4. Friendship API を使う場合は、友だち追加済みであることを確認できるようにしてください

## Callback / Endpoint URL設定
- LINE Login Callback URL: `https://<公開URL>/liff-login`
- LIFF Endpoint URL: `https://<公開URL>/liff-login`
- アプリ利用画面:
  - `/liff-login`
  - `/attendance`
  - `/mypage`
  - `/admin/events`

## 必要な環境変数
`.env.example` を `.env` にコピーして値を設定してください。

| 変数名 | 説明 |
| --- | --- |
| `DB_NAME` | MySQLデータベース名 |
| `DB_USERNAME` | MySQLユーザー |
| `DB_PASSWORD` | MySQLパスワード |
| `DB_ROOT_PASSWORD` | MySQL root パスワード |
| `DB_PORT` | ローカル公開ポート |
| `LINE_LIFF_ID` | LIFF ID |
| `LINE_CHANNEL_ID` | LINE Login チャネル ID |
| `LINE_CHANNEL_SECRET` | LINE Login チャネルシークレット |
| `LINE_FRIENDSHIP_CHECK_ENABLED` | Friendship API チェック有無 |

## 環境変数ファイルの準備
どちらの起動方法でも、まず `.env` を用意してください。

```bash
cp .env.example .env
# .env を編集（DBパスワードやLINE関連の値を設定）
```

## Docker Compose起動方法（DBとアプリを一括起動）
MySQLコンテナの起動から Spring Boot アプリの起動までを一度に行う場合は、リポジトリのルートで以下を実行してください。

```bash
docker compose up --build
```

- `db` サービスは `mysqladmin ping` によるヘルスチェックに成功するまで起動中とみなされ、`app` サービスは `depends_on` の `condition: service_healthy` により **db が healthy になってから起動を開始**します。
- コンテナ内の Spring Boot アプリは `DB_HOST=db` を使用し、Compose が作成する内部ネットワーク上のサービス名 `db` で MySQL に接続します（`localhost` ではありません）。
- 初回はイメージのビルドが走るため起動まで時間がかかります。

バックグラウンドで起動する場合は次のようにします。

```bash
docker compose up -d --build
```

アプリのログを確認する場合:

```bash
docker compose logs -f app
```

停止する場合:

```bash
docker compose down
```

DBのデータ（ボリューム）ごと削除したい場合は以下を実行してください。**MySQLのデータが完全に削除されるため注意してください。**

```bash
docker compose down -v
```

## ローカルでDBだけ起動してアプリをMavenで起動する方法
Compose でアプリまで一括起動せず、DBだけコンテナで起動してアプリをローカルの Maven（ホットリロードやデバッグがしやすい）で動かす場合は以下のようにします。

```bash
docker compose up -d db
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
# または
mvn spring-boot:run
```

この方法では、アプリはコンテナ内ではなくホストマシン上で動作するため、`application.yml` のデフォルト値どおり `DB_HOST` は `localhost`（未設定時のデフォルト）として MySQL に接続します。一方、`docker compose up --build` でアプリもコンテナ化して起動する場合は、`docker-compose.yml` の `app` サービスに設定された環境変数 `DB_HOST=db` が使われ、Compose サービス名 `db` で接続します。

## 初期ADMINユーザーの登録方法
会員は自動作成されません。MySQLへ明示的に登録してください。

```sql
INSERT INTO members (line_user_id, display_name, role, enabled, version, created_at, updated_at)
VALUES ('Uxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', '管理者', 'ADMIN', true, 0, NOW(6), NOW(6));
```

一般会員の例:

```sql
INSERT INTO members (line_user_id, display_name, role, enabled, version, created_at, updated_at)
VALUES ('Uyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy', '一般会員', 'USER', true, 0, NOW(6), NOW(6));
```

## 動作確認方法
1. `.env` を設定
2. `docker compose up -d db`
3. `./mvnw test`
4. `./mvnw spring-boot:run`
5. 管理者で `/admin/events` からイベントを登録
6. LINE Developers で設定した LIFF URL を公式LINEから開く
7. 出欠画面で「参加 / 不参加 / 未定」を更新
8. 管理画面で参加者一覧を確認

## セキュリティ実装の要点
- クライアント送信の LINE user ID は信用せず、ID Token 検証後の `sub` を利用
- `NimbusJwtDecoder` により JWK 署名検証を実施
- `issuer` / `audience` / `expiration` を検証
- `member` 未登録ユーザーは拒否し、自動入会させない
- `enabled=false` 会員は拒否
- Spring Security の `SecurityContext` を明示的に HTTP セッションへ保存
- セッション固定攻撃対策としてセッションID変更を有効化
- CSRF は無効化せず、LIFF ページから CSRF トークン付きで `/auth/line` を呼び出す
- `/admin/**` は `ADMIN` のみ許可
- 出欠更新はログイン中の会員のみ対象で、クライアントから `memberId` を受け取らない

## DBマイグレーション
Flyway を利用しています。
- `V1__create_member.sql`
- `V2__create_event.sql`
- `V3__create_attendance.sql`

## テスト
以下を自動テストしています。
- LINE認証正常系/異常系
- member未登録・無効化
- 出欠登録と更新
- 同一イベント重複レコード防止
- 未認証アクセス拒否
- `ADMIN` のみ管理画面許可

## 補足
- `LINE_CHANNEL_SECRET` は将来チャネルアクセストークン検証や拡張用途で利用できますが、本実装では ID Token 検証の主要要素として JWK / issuer / audience / expiration を利用しています
- 参加申請機能は未実装ですが、未登録ユーザーを自動作成しない構成にしており、今後 `membership_requests` のようなテーブルを追加しやすい構成です
