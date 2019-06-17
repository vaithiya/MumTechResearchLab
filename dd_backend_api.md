# API I/F 仕様書

<!-- toc -->

## Overview
- 本ドキュメントでは、API インターフェースの仕様書に関して記載する。

## Rererring link
- [API設計書_iemarco_v2](https://docs.google.com/spreadsheets/d/1oHwuLPUIlTUzFQV754iQ588KSHpdSMQ5qmv6E404quM/edit?usp=sharing)

## URL
- 現状は、`https://{FQDN}/api/` だが、`https://{FQDN}/api/v1/` がAPI のURLになるようにする。

## Header

### JSON pattern

| key             | description                        | mandatory |
|:----------------|:-----------------------------------|:----------|
| Accept          | application/json                   | yes       |
| Content-Type    | application/json;charset=UTF-8     | yes       |
| X-Authorization | Bearer JWT access token by cognito | no        |

**POST Example:**  

```sh
chcp 65001
curl -v -X POST \
    -H "Accept: application/json"  \
    -H "Content-Type: application/json;charset=UTF-8"  \
    -H "X-Authorization: Bearer {access token}" \
    -d '{
          "xxxx": "xxx"
    }' \
    https://{fqdn}/api/v1/clientProperty/
```

### Multi part pattern

| key             | description                        | mandatory |
|:----------------|:-----------------------------------|:----------|
| Content-Type    | multipart/form-data                | yes       |
| X-Authorization | Bearer JWT access token by cognito | no        |

----

## stationHierarchy
- **都道府県／路線／駅情報管理** API の I/F に関してを記載する。

### POST /stationHierarchy

----

## address
- **住所情報管理** API の I/F に関してを記載する。

### GET /address

| Name             | Method | Token    | Content-Type                   |
|:-----------------|:-------|:---------|:-------------------------------|
| 郵便番号住所取得 | GET    | required | application/json;charset=UTF-8 |

**Process:**  
1. 郵便番号に紐づく、都道府県+市区町村情報を取得し返却する。

**Path parameters:**  
- N/A

**Querystring:**  
- ページネーションのロジックを考慮していないため、クエリなしの検索は現在許容されない。

| QueryString Key | 型     | サイズ | 必須 | 検索条件 | 値の説明       | フォーマット                            |
|:----------------|:-------|:-------|:-----|:---------|:---------------|:----------------------------------------|
| postal_code     | string | N/A    | no   | 完全一致 | 郵便番号       | 半角数字のハイフン区切り<br>ex.103-0025 |
| pref_cd         | string | N/A    | no   | 完全一致 | 都道府県コード | 半角数字<br>ex. 1                       |
| city_cd         | string | N/A    | no   | 完全一致 | 市区町村コード | 半角数字<br>ex. 01101                   |
| town_cd         | string | N/A    | no   | 完全一致 | 町域コード     | 半角数字<br>ex. 11010002                |

**Response success:**

- サンプルオブジェクト

```JSON
{
    "pref_cd" : 1,
    "pref_name" : "北海道",
    "city" : [
        {
            "city_cd" : 1101,
            "city_name" : "札幌市中央区"
        }
    ],
    "town" : [
        {
            "town_cd" : 11010002,
            "town_name" : "大通西"
        }
    ],
    "jichome" : [
        {"jichome_name" : "　"},
        {"jichome_name" : "１丁目"},
        {"jichome_name" : "２丁目"},
        {"jichome_name" : "３丁目"},
        {"jichome_name" : "４丁目"},
        {"jichome_name" : "５丁目"}
    ]
}
```  

- 説明

| JSON Key               | Type   | Size | Mandatory | Search | Note                         | Format       |
|:-----------------------|:-------|:-----|:----------|:-------|:-----------------------------|:-------------|
| pref_cd                | number | 2    | yes       | N/A    | 都道府県コード               | 1 - 47       |
| pref_name              | string | 100  | yes       | N/A    | 都道府県                     | 任意の文字列 |
| city[]                 | array  | N/A  | yes       | N/A    | 市区町村のオブジェクトを格納 | N/A          |
| city[].city_cd         | number | 10   | yes       | N/A    | 市区町村コード               | 半角数字     |
| city[].city_name       | string | 100  | yes       | N/A    | 市区町村                     | 任意の文字列 |
| town[]                 | array  | N/A  | yes       | N/A    | 市区町村のオブジェクトを格納 | N/A          |
| town[].town_cd         | number | 10   | yes       | N/A    | 町域コード                   | 半角数字     |
| town[].town_name       | string | 100  | yes       | N/A    | 町域                         | 任意の文字列 |
| jichome[]              | array  | N/A  | no        | N/A    | 字丁目のオブジェクトを格納   | N/A          |
| jichome[].jichome_name | string | 100  | no        | N/A    | 字丁目                       | 任意の文字列 |

**Response failure:**

| HTTP Status | Title                        |
|:------------|:-----------------------------|
| 400         | BadRequestException          |
| 401         | UnauthorizedException        |
| 403         | ForbiddenException           |
| 500         | InternalServerErrorException |

----

## property
- **物件情報管理** API の I/F に関してを記載する。

### GET /property/:id

### POST /property/findByDtlCond

----

## clientProperty
- **物件情報（加盟店）管理** API の I/F に関してを記載する。

### POST /clientProperty

| Name                   | Method | Token    | Content-Type                   |
|:-----------------------|:-------|:---------|:-------------------------------|
| 物件情報（加盟店）登録 | POST   | required | application/json;charset=UTF-8 |

**Process:**  
1. 入力された情報を引数として、加盟店物件情報テーブルに情報を登録する。  
1. 登録時に新しく加盟店物件IDを自動発番する。発番ルールとしては、物件IDの頭に「cl_」を使用する。  
1. 楽天コールインテリジェンスを使用し、物件に紐づく独自の電話番号を取得し、格納する。  

**Entity:**  
- T_CLIENT_PROPERTY_BASE, T_CLIENT_PROPERTY, T_CLIENT_PROPERTY_LAND  **<-- TODO:ソースみて、気づいたら直す**

**Path parameters:**  
- N/A

**Querystring:**  
- N/A

**Request body:**  

- サンプルオブジェクト  

```JSON
{
    "type":1,
    "sale_status":2,
    "ad_comment":"※価格・販売戸数は未定です。\r\n※販売開始まで契約または予約の申込および申込順位の確保につながる行為は一切できません。\r\n※★月販売予定。\r\n※第★期の販売住戸が未確定のため、物件データは第★期以降の全販売対象住戸のものを表記しています。確定情報は新規分譲広告にて明示します。",
    "name":"野村不動産　プラウドシーズン西落合",
    "sale_housiki":0,
    "sale_start":2,
    "sale_start_year":2019,
    "sale_start_month":1,
    "sale_start_timing":3,
    "sentyaku_year_month_day":"2019-04-15",
    "sale_schedule_comment":"来月金額公開予定！",
    "postal_code_first":"141",
    "postal_code_last":"0033",
    "prefectures_list":1,
    "city_list":1101,
    "town_list":11010000,
    "jityou_list":"1丁目",
    "tiban":"1番地",
    "gouban":"7号",
    "address_last":"青山外苑ビル9F",
    "latitude":123.456,
    "longitude":456.123,
    "main_access":1,
    "ensen_name":1002,
    "station_name":100201,
    "access_type":1,
    "walk_time":10,
    "bus_time":20,
    "getoff_busstop":"六本木駅前",
    "getoff_busstop_walk_time":15,
    "running_distance_1":10,
    "running_distance_2":2,
    "bus_company":"東急バス",
    "busstop":"新宿駅前",
    "others_access_1":1,
    "ensen_name_1":1002,
    "station_name_1":100201,
    "access_type_1":1,
    "walk_time_1":10,
    "bus_time_1":20,
    "getoff_busstop_1":"六本木駅前",
    "getoff_busstop_walk_time_1":15,
    "running_distance_1_1":10,
    "running_distance_1_2":2,
    "bus_company_1":"東急バス",
    "busstop_1":"新宿駅前",
    "others_access_2":1,
    "ensen_name_2":1002,
    "station_name_2":100201,
    "access_type_2":1,
    "walk_time_2":10,
    "bus_time_2":20,
    "getoff_busstop_2":"六本木駅前",
    "getoff_busstop_walk_time_2":15,
    "running_distance_2_1":10,
    "running_distance_2_2":2,
    "bus_company_2":"東急バス",
    "busstop_2":"新宿駅前",
    "price_setting":1,
    "price_1":2000,
    "price_handle":2,
    "price_2":3000,
    "site_right_type":1,
    "land_area_type":1,
    "land_area_1":100,
    "land_area_under_decimal_1":15,
    "land_area_handle":1,
    "land_area_2":200,
    "land_area_under_decimal_2":30,
    "shidoufutan_type":0,
    "shidoumenseki":100,
    "shidoumenseki_decimal":11,
    "share_ratio":51,
    "share_ratio_under_decimal":2,
    "overall_ratio":90,
    "overall_ratio_under_decimal":21,
    "building_area_type":1,
    "building_area_1":200,
    "building_area_under_decimal_1":30,
    "building_area_handle":2,
    "building_area_2":300,
    "building_area_under_decimal_2":20,
    "sale_house_num_type":1,
    "sale_house_num":3,
    "total_house_num":10,
    "room_num_1":2,
    "layout_type_1":12,
    "service_rule_type_1":1,
    "layout_handle":1,
    "room_num_2":3,
    "layout_type_2":5,
    "service_rule_type_2":1,
    "complete_time_type":1,
    "complete_time_select":2,
    "complete_year":2019,
    "complete_month":4,
    "complete_decade":4,
    "comlete_year_month_day":"2019-04-15",
    "complete_after_contract":2,
    "building_situation":2,
    "parking_type":2,
    "advertiser_company_trade_aspect_type":17,
    "trade_aspect_1":"",
    "advertiser_company_trade_aspect_type_2":6,
    "company_postalcode_2":"141",
    "company_area_num_2":"0033",
    "company_address_2":"",
    "position_group_name_2":"経団連",
    "license_num_2":"第HPA-17-10449-1号",
    "company_name_2":"",
    "advertiser_company_trade_aspect_type_3":1,
    "company_postalcode_3":"141",
    "company_area_num_3":"0033",
    "company_address_3":"東京都品川区西品川1-2-3-444",
    "position_group_name_3":"経団連",
    "license_num_3":"第HPA-17-10449-1号",
    "company_name_3":"",
    "station_conveniene_selecct":[1,3],
    "dwelling_unit_floors_num_select":[1],
    "lighting_ventication_select":[1,6,10],
    "character_madori_selecet":[6,7],
    "kitchen_concerned_facilities_select":[1,2,3],
    "parking_select":[1,6],
    "reform_renovation_select":[1,5,6],
    "main_inner_image_1":"",
    "main_inner_image_category_1":10,
    "main_inner_image_caption_1":"",
    "sub_inner_image_2":"",
    "sub_inner_image_category_2":19,
    "sub_inner_image_caption_2":"",
    "main_outer_image_1":"",
    "main_outer_image_category_1":11,
    "main_outer_image_caption_1":"",
    "sub_outer_image_2":"",
    "sub_outer_image_category_2":1,
    "sub_outer_image_caption_2":"",
    "internal_memo":"社内のメモです",
    "store_id":"IM00001",
    "posting_priority_value":10,
    "jimukyoku_memo":"事務局のメモです"
}
```

- 説明

| JSON Key                               | Type   | Size | Mandatory | Search | Note                              | Format                                                                                                                                                    |
|:---------------------------------------|:-------|:-----|:----------|:-------|:----------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------|
| type                                   | number | 1    | yes       | N/A    | 物件種別                          | 1=一戸建て、2=二戸建て以上                                                                                                                                |
| sale_status                            | number | 1    | yes       | N/A    | 物件ステータス                    | 0=下書き、1=販売予告中、2=販売中、3=成約済、4=掲載止                                                                                                      |
| ad_comment                             | string | 1000 | no        | N/A    | 予告広告補足コメント              | 任意の文字列                                                                                                                                              |
| name                                   | string | 100  | yes       | N/A    | 物件名                            | 任意の文字列                                                                                                                                              |
| sale_housiki                           | number | 1    | no        | N/A    | 販売方式                          | 最低限機能verの場合は **0** 固定<br>（0=未定、1=先着順、2=登録抽選）                                                                                      |
| sale_start                             | number | 1    | no        | N/A    | 販売開始方式区分                  | 1=年月、2=年月日                                                                                                                                          |
| sale_start_year                        | number | 4    | no        | N/A    | 販売開始年                        | 4桁の数値（ex.2019）                                                                                                                                      |
| sale_start_month                       | number | 2    | no        | N/A    | 販売開始月                        | 1 - 12                                                                                                                                                    |
| sale_start_timing                      | number | 1    | no        | N/A    | 販売開始旬                        | 1=初旬、2=上旬、3=中旬、4=下旬、5=末、0=未選択                                                                                                            |
| sentyaku_year_month_day                | string | 10   | no        | N/A    | 販売開始年月日参照表示欄          | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| sale_schedule_comment                  | string | 1000 | no        | N/A    | 販売スケジュールコメント入力欄    | 任意の文字列                                                                                                                                              |
| postal_code_first                      | string | 5    | no        | N/A    | 郵便区分番号                      | 半角数字（0始まり含む）                                                                                                                                   |
| postal_code_last                       | string | 4    | no        | N/A    | 町域番号                          | 半角数字（0始まり含む）                                                                                                                                   |
| prefectures_list                       | number | 2    | yes       | N/A    | 都道府県選択リスト                | 半角数字                                                                                                                                                  |
| city_list                              | number | 11   | yes       | N/A    | 市区郡選択リスト                  | 数値                                                                                                                                                      |
| town_list                              | number | N/A  | yes       | N/A    | 町村郡選択リスト                  | 数値                                                                                                                                                      |
| jityou_list                            | string | 50   | no        | N/A    | 字丁選択リスト                    | 任意の文字列（ex.1丁目）                                                                                                                                  |
| tiban                                  | string | 10   | no        | N/A    | 地番入力欄                        | 任意の文字列（ex.1番地）                                                                                                                                  |
| gouban                                 | string | 10   | no        | N/A    | 号番入力欄                        | 任意の文字列（ex.7号）                                                                                                                                    |
| address_last                           | string | 50   | no        | N/A    | 住所末尾入力欄                    | 任意の文字列（ex.青山外苑ビル9F）                                                                                                                         |
| latitude                               | number | 11   | yes       | N/A    | 緯度                              | 浮動小数点を含む数値                                                                                                                                      |
| longitude                              | number | 12   | yes       | N/A    | 経度                              | 浮動小数点を含む数値                                                                                                                                      |
| main_access                            | number | 1    | yes       | N/A    | 主要交通区分                      | 1=電車、2=バス                                                                                                                                            |
| ensen_name                             | number | 10   | no        | N/A    | 路線参照表示欄                    | 半角数字                                                                                                                                                  |
| station_name                           | number | 10   | no        | N/A    | 駅参照表示欄                      | 半角数字                                                                                                                                                  |
| access_type                            | number | 1    | no        | N/A    | 交通手段区分                      | 1=徒歩、2=バス、3=車                                                                                                                                      |
| walk_time                              | number | 2    | no        | N/A    | 徒歩時間入力欄                    | 半角数字（単位：分）                                                                                                                                      |
| bus_time                               | number | 2    | no        | N/A    | バス乗車時間入力欄                | 半角数字（単位：分）                                                                                                                                      |
| getoff_busstop                         | string | 25   | no        | N/A    | 下車バス停入力欄                  | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| getoff_busstop_walk_time               | number | 2    | no        | N/A    | 下車バス停_徒歩時間入力           | 半角数字（単位：分）                                                                                                                                      |
| running_distance_1                     | number | 5    | no        | N/A    | 走行距離1                         | 半角数字（単位：km）                                                                                                                                      |
| running_distance_2                     | number | 2    | no        | N/A    | 走行距離2                         | 半角数字                                                                                                                                                  |
| bus_company                            | string | 50   | no        | N/A    | バス会社入力欄                    | 任意の文字列（ex.東急バス）                                                                                                                               |
| busstop                                | string | 50   | no        | N/A    | バス停入力欄                      | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| others_access_1                        | number | 1    | no        | N/A    | その他 交通1区分                  | 1=電車、2=バス                                                                                                                                            |
| ensen_name_1                           | number | 10   | no        | N/A    | 路線参照表示欄1                   | 半角数字                                                                                                                                                  |
| station_name_1                         | number | 10   | no        | N/A    | 駅参照表示欄1                     | 半角数字                                                                                                                                                  |
| access_type_1                          | number | 1    | no        | N/A    | 交通手段区分1                     | 1=徒歩、2=バス、3=車                                                                                                                                      |
| walk_time_1                            | number | 2    | no        | N/A    | 徒歩時間入力欄1                   | 半角数字（単位：分）                                                                                                                                      |
| bus_time_1                             | number | 2    | no        | N/A    | バス乗車時間入力欄1               | 半角数字（単位：分）                                                                                                                                      |
| getoff_busstop_1                       | string | 25   | no        | N/A    | 下車バス停入力欄1                 | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| getoff_busstop_walk_time_1             | number | 2    | no        | N/A    | 下車バス停_徒歩時間入力1          | 半角数字（単位：分）                                                                                                                                      |
| running_distance_1_1                   | number | 5    | no        | N/A    | 走行距離1                         | 半角数字（単位：km）                                                                                                                                      |
| running_distance_1_2                   | number | 2    | no        | N/A    | 走行距離2                         | 半角数字                                                                                                                                                  |
| bus_company_1                          | string | 25   | no        | N/A    | バス会社入力欄1                   | 任意の文字列（ex.東急バス）                                                                                                                               |
| busstop_1                              | string | 25   | no        | N/A    | バス停入力欄1                     | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| others_access_2                        | number | 1    | no        | N/A    | その他 交通2区分                  | 1=電車、2=バス                                                                                                                                            |
| ensen_name_2                           | number | 10   | no        | N/A    | 路線参照表示欄2                   | 半角数字                                                                                                                                                  |
| station_name_2                         | number | 10   | no        | N/A    | 駅参照表示欄2                     | 半角数字                                                                                                                                                  |
| access_type_2                          | number | 1    | no        | N/A    | 交通手段区分2                     | 1=徒歩、2=バス、3=車                                                                                                                                      |
| walk_time_2                            | number | 2    | no        | N/A    | 徒歩時間入力欄2                   | 半角数字（単位：分）                                                                                                                                      |
| bus_time_2                             | number | 2    | no        | N/A    | バス乗車時間入力欄2               | 半角数字（単位：分）                                                                                                                                      |
| getoff_busstop_2                       | string | 25   | no        | N/A    | 下車バス停入力欄2                 | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| getoff_busstop_walk_time_2             | number | 2    | no        | N/A    | 下車バス停_徒歩時間入力2          | 半角数字（単位：分）                                                                                                                                      |
| running_distance_2_1                   | number | 5    | no        | N/A    | 走行距離2_1                       | 半角数字（単位：km）                                                                                                                                      |
| running_distance_2_2                   | number | 2    | no        | N/A    | 走行距離2_2                       | 半角数字                                                                                                                                                  |
| bus_company_2                          | string | 25   | no        | N/A    | バス会社入力欄2                   | 任意の文字列（ex.東急バス）                                                                                                                               |
| busstop_2                              | string | 25   | no        | N/A    | バス停入力欄2                     | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| price_setting                          | number | 1    | yes       | N/A    | 価格設定                          | 0=未定、1=予定・確定                                                                                                                                      |
| price_1                                | number | 6    | yes       | N/A    | 価格入力欄1                       | 半角数字（単位：JPY）                                                                                                                                     |
| price_handle                           | number | 1    | yes       | N/A    | 価格入力欄の取り扱い              | 1="～"、2="・"（中点）                                                                                                                                    |
| price_2                                | number | 6    | yes       | N/A    | 価格入力欄2                       | 半角数字（単位：JPY）                                                                                                                                     |
| site_right_type                        | number | 1    | yes       | N/A    | 敷地権利区分                      | 1=所有権のみ、2=借地権のみ、3=所有権・借地権混在                                                                                                          |
| land_area_type                         | number | 1    | yes       | N/A    | 土地面積区分                      | 1=登記、2=実測、0=未選択                                                                                                                                  |
| land_area_1                            | number | 6    | yes       | N/A    | 土地面積数1入力欄                 | 半角数字                                                                                                                                                  |
| land_area_under_decimal_1              | number | 2    | yes       | N/A    | 土地面積数（小数点以下）1入力欄   | 半角数字                                                                                                                                                  |
| land_area_handle                       | number | 1    | yes       | N/A    | 土地面積数欄の取り扱い            | 1="～"、2="."（小数点）                                                                                                                                   |
| land_area_2                            | number | 6    | yes       | N/A    | 土地面積数2入力欄                 | 半角数字                                                                                                                                                  |
| land_area_under_decimal_2              | number | 2    | yes       | N/A    | 土地面積数（小数点以下）2入力欄   | 半角数字                                                                                                                                                  |
| shidoufutan_type                       | number | 1    | no        | N/A    | 私道負担区分                      | 0=無、1=有、2=共有                                                                                                                                        |
| shidoumenseki                          | number | 4    | no        | N/A    | 私道面積数入力欄                  | 半角数字                                                                                                                                                  |
| shidoumenseki_decimal                  | number | 2    | no        | N/A    | 私道面積数（小数点以下）入力欄    | 半角数字                                                                                                                                                  |
| share_ratio                            | number | 5    | no        | N/A    | 持分比率入力欄                    | 半角数字（単位：％）                                                                                                                                      |
| share_ratio_under_decimal              | number | 2    | no        | N/A    | 持分比率（小数点以下）入力欄      | 半角数字                                                                                                                                                  |
| overall_ratio                          | number | 9    | no        | N/A    | 全体比率入力欄                    | 半角数字                                                                                                                                                  |
| overall_ratio_under_decimal            | number | 2    | no        | N/A    | 全体比率（小数点以下）入力欄      | 半角数字                                                                                                                                                  |
| building_area_type                     | number | 1    | yes       | N/A    | 建物面積区分                      | 1=登記、2=実測、0=未選択                                                                                                                                  |
| building_area_1                        | number | 4    | yes       | N/A    | 建物面積数1入力欄                 | 半角数字（単位：㎡）                                                                                                                                      |
| building_area_under_decimal_1          | number | 2    | no        | N/A    | 建物面積数（小数点以下）1入力欄   | 半角数字                                                                                                                                                  |
| building_area_handle                   | number | 1    | yes       | N/A    | 建物面積数欄の取り扱い            | 1="～"、2="."（小数点）                                                                                                                                   |
| building_area_2                        | number | 4    | yes       | N/A    | 建物面積数2入力欄                 | 半角数字（単位：㎡）                                                                                                                                      |
| building_area_under_decimal_2          | number | 2    | no        | N/A    | 建物面積数（小数点以下）2入力欄   | 半角数字                                                                                                                                                  |
| sale_house_num_type                    | number | 1    | yes       | N/A    | 販売戸数設定                      | 0= 未定、1= 予定・確定                                                                                                                                    |
| sale_house_num                         | number | 5    | yes       | N/A    | 販売戸数入力欄                    | 半角数字（単位：戸）                                                                                                                                      |
| total_house_num                        | number | 5    | no        | N/A    | 総戸数入力欄                      | 半角数字（単位：戸）                                                                                                                                      |
| room_num_1                             | number | 2    | yes       | N/A    | 部屋数入力欄1                     | 半角数字：（単位：部屋）                                                                                                                                  |
| layout_type_1                          | number | 2    | yes       | N/A    | 間取りタイプ設定1                 | 0=選択してください、1=ワンルーム、2=K<br>3=DK、4=LDK、5=LK、6=KK、7=DKK、8=LKK<br>9=DDKK、10=LLKK、11=LDKK、12=LDDKK、13=LLDKK                            |
| service_rule_type_1                    | number | 1    | no        | N/A    | サービスルール設定1               | 0=選択してください、1=+S、2=+2S、3=+3S                                                                                                                    |
| layout_handle                          | number | 1    | no        | N/A    | 間取りの取り扱い                  | 1="～"、2="."（小数点）                                                                                                                                   |
| room_num_2                             | number | 2    | no        | N/A    | 部屋数入力欄2                     | 半角数字：（単位：部屋）                                                                                                                                  |
| layout_type_2                          | number | 2    | yes       | N/A    | 間取りタイプ設定2                 | 0=選択してください、1=ワンルーム、2=K<br>3=DK、4=LDK、5=LK、6=KK、7=DKK、8=LKK<br>9=DDKK、10=LLKK、11=LDKK、12=LDDKK、13=LLDKK                            |
| service_rule_type_2                    | number | 1    | no        | N/A    | サービスルール設定2               | 0=選択してください、1=+S、2=+2S、3=+3S                                                                                                                    |
| complete_time_type                     | number | 1    | yes       | N/A    | 完成時期区分                      | 0= 未選択、1= 完成予定、2= 完成済、3= 契約後                                                                                                              |
| complete_time_select                   | number | 1    | yes       | N/A    | 完成時期方式                      | 1= 年月、2= 年月日                                                                                                                                        |
| complete_year                          | number | 4    | no        | N/A    | 完成年                            | 半角数字：（単位：年）                                                                                                                                    |
| complete_month                         | number | 2    | no        | N/A    | 完成月                            | 半角数字：（単位：月）                                                                                                                                    |
| complete_decade                        | number | 1    | no        | N/A    | 完成旬                            | 0= 未選択、1= 初旬、2= 上旬、3= 中旬、4= 下旬、5= 末                                                                                                      |
| comlete_year_month_day                 | string | 10   | no        | N/A    | 完成年月日参照表示欄              | YYYY-MM-DD                                                                                                                                                |
| complete_after_contract                | number | 2    | no        | N/A    | 完成時期契約後入力欄              | 半角数字                                                                                                                                                  |
| parking_type                           | number | 1    | no        | N/A    | 駐車場区分                        | 1= 車庫、2= 地下車庫、3= カースペース<br>4= カーポート、5= 無、0= 未選択                                                                                  |
| advertiser_company_trade_aspect_type   | number | 2    | yes       | N/A    | 広告主（貴社）会社取引態様区分    | **See &lowast;1**                                                                                                                                         |
| trade_aspect_1                         | string | 20   | no        | N/A    | 取引態様1入力欄                   | 任意の文字列                                                                                                                                              |
| advertiser_company_trade_aspect_type_2 | number | 2    | no        | N/A    | 会社2取引態様区分                 | **See &lowast;2**                                                                                                                                         |
| company_postalcode_2                   | string | 5    | no        | N/A    | 会社2郵便区分番号入力欄           | 半角数字                                                                                                                                                  |
| company_area_num_2                     | string | 4    | no        | N/A    | 会社2町域番号入力欄               | 半角数字                                                                                                                                                  |
| company_address_2                      | string | 150  | no        | N/A    | 住所2入力欄                       | 任意の文字列                                                                                                                                              |
| position_group_name_2                  | string | 200  | no        | N/A    | 所属団体名2入力欄                 | 任意の文字列                                                                                                                                              |
| license_num_2                          | string | 30   | no        | N/A    | 免許番号2入力欄                   | 任意の文字列                                                                                                                                              |
| company_name_2                         | string | 150  | no        | N/A    | 社名2入力欄                       | 任意の文字列                                                                                                                                              |
| advertiser_company_trade_aspect_type_3 | number | 2    | no        | N/A    | 会社3取引態様区分                 | **See &lowast;2**                                                                                                                                         |
| company_postalcode_3                   | string | 5    | no        | N/A    | 会社3郵便区分番号入力欄           | 半角数字                                                                                                                                                  |
| company_area_num_3                     | string | 4    | no        | N/A    | 会社3町域番号入力欄               | 半角数字                                                                                                                                                  |
| company_address_3                      | string | 150  | no        | N/A    | 住所3入力欄                       | 任意の文字列                                                                                                                                              |
| position_group_name_3                  | string | 200  | no        | N/A    | 所属団体名3入力欄                 | 任意の文字列                                                                                                                                              |
| license_num_3                          | string | 30   | no        | N/A    | 免許番号3入力欄                   | 任意の文字列                                                                                                                                              |
| company_name_3                         | string | 150  | no        | N/A    | 社名3入力欄                       | 任意の文字列                                                                                                                                              |
| station_conveniene_selecct             | array  | N/A  | no        | N/A    | 駅利便性設定                      | number in array（複数可能）<br>1= 2沿線以上利用可、2= 検索駅まで平坦、3= 始発駅                                                                           |
| dwelling_unit_floors_num_select        | array  | N/A  | no        | N/A    | 住戸・階数設定                    | number in array（複数可能）<br>1= 平屋、2= 2階建て、3= 3階建て以上（複数可能）                                                                            |
| lighting_ventication_select            | array  | N/A  | no        | N/A    | 陽当り・採光・通風設定            | number in array（複数可能）<br>**See &lowast;3**                                                                                                          |
| character_madori_selecet               | array  | N/A  | no        | N/A    | 間取り設定                        | number in array（複数可能）<br>**See &lowast;4**                                                                                                          |
| kitchen_concerned_facilities_select    | array  | N/A  | no        | N/A    | キッチン・関連設備設定            | number in array（複数可能）<br>**See &lowast;5**                                                                                                          |
| parking_select                         | array  | N/A  | no        | N/A    | 駐車・駐輪設定                    | number in array（複数可能）<br>1= 駐車2台可、2= 駐車3台以上可、3= ハイルーフ駐車場<br>4= EV車充電設備、5= シャッター車庫、6= ビルトインガレージ           |
| reform_renovation_select               | array  | N/A  | no        | N/A    | ﾘﾌｫｰﾑ・ﾘﾉﾍﾞｰｼｮﾝ設定               | number in array（複数可能）<br>1= 適合リノベーション、2= 内外装リフォーム、3= 内装リフォーム<br>4= 外装リフォーム、5= フローリング張替、6= リノベーション |
| main_inner_image_1                     | string | 255  | no        | N/A    | 内観・その他メイン画像1           | パス（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg）                                                                          |
| main_inner_image_category_1            | number | 2    | no        | N/A    | 内観・その他メイン画像1カテゴリー | **See &lowast;6**                                                                                                                                         |
| main_inner_image_caption_1             | string | 100  | no        | N/A    | メイン画像1キャプション           | 任意の文字列                                                                                                                                              |
| sub_inner_image_2                      | string | 255  | no        | N/A    | 画像2                             | パス（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg）                                                                          |
| sub_inner_image_category_2             | number | 2    | no        | N/A    | 画像2カテゴリー                   | **See &lowast;6 **                                                                                                                                        |
| sub_inner_image_caption_2              | string | 100  | no        | N/A    | 画像2キャプション                 | 任意の文字列                                                                                                                                              |
| main_outer_image_1                     | string | 255  | no        | N/A    | メイン画像1                       | パス（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg）                                                                          |
| main_outer_image_category_1            | number | 2    | no        | N/A    | メイン画像1カテゴリー             | **See &lowast;6**                                                                                                                                         |
| main_outer_image_caption_1             | string | 100  | no        | N/A    | メイン画像1キャプション           | 任意の文字列                                                                                                                                              |
| sub_outer_image_2                      | string | 255  | no        | N/A    | 画像2                             | パス（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg）                                                                          |
| sub_outer_image_category_2             | number | 2    | no        | N/A    | 画像2カテゴリー                   | **See &lowast;6**                                                                                                                                         |
| sub_outer_image_caption_2              | string | 100  | no        | N/A    | 画像2キャプション                 | 任意の文字列                                                                                                                                              |
| internal_memo                          | string | 2000 | no        | N/A    | 社内メモ入力欄                    | 任意の文字列                                                                                                                                              |
| store_id                               | string | 7    | yes       | N/A    | 店舗ID                            | 半角英数記号                                                                                                                                              |
| posting_priority_value                 | number | 8    | no        | N/A    | 物件掲載優先値欄                  | 半角数字                                                                                                                                                  |
| jimukyoku_memo                         | string | 2000 | no        | N/A    | いえまるこ事務局メモ入力欄        | 任意の文字列                                                                                                                                              |

- **&lowast;1**：advertiser_company_trade_aspect_type

| Value | Meaning                  |
|:------|:-------------------------|
| 0     | 選択してください         |
| 1     | 売主                     |
| 2     | 建物売主                 |
| 3     | 土地売主                 |
| 4     | 土地貸主                 |
| 5     | 土地転貸主               |
| 6     | 販売提携(代理)           |
| 7     | 販売提携(媒介)           |
| 8     | 販売提携(復代理)         |
| 9     | 仲介(一般媒介)           |
| 10    | 仲介(専任媒介)           |
| 11    | 仲介(専属専任)           |
| 12    | 先物                     |
| 13    | 事業主・売主             |
| 14    | 事業主・建物売主         |
| 15    | 事業主・土地売主         |
| 16    | 事業主・販売提携（代理） |
| 17    | 事業主・販売提携（媒介） |

- **&lowast;2**：advertiser_company_trade_aspect_type_2 / advertiser_company_trade_aspect_type_3

| Value | Meaning          |
|:------|:-----------------|
| 0     | 選択してください |
| 1     | 売主             |
| 2     | 建物売主         |
| 3     | 土地売主         |
| 4     | 土地貸主         |
| 5     | 土地転貸主       |
| 6     | 販売提携(代理)   |
| 7     | 販売提携(復代理) |
| 8     | 販売提携(媒介)   |
| 9     | 仲介             |

- **&lowast;3**：lighting_ventication_select

| Value | Meaning      |
|:------|:-------------|
| 1     | 南向き       |
| 2     | 陽当り良好   |
| 3     | 3面採光      |
| 4     | 東南向き     |
| 5     | 全室南向き   |
| 6     | 通風良好     |
| 7     | 南西向き     |
| 8     | 全室2面採光  |
| 9     | 全室南西向き |
| 10    | 全室東南向き |

- **&lowast;4**：character_madori_selecet

| Value | Meaning       |
|:------|:--------------|
| 0     | LDK20畳以上   |
| 1     | LDK18畳以上   |
| 2     | LDK15畳以上   |
| 3     | 和室          |
| 4     | ロフト        |
| 5     | 吹抜け        |
| 6     | 全居室6畳以上 |
| 7     | 2世帯住宅     |
| 8     | 可動間仕切り  |

- **&lowast;**5：kitchen_concerned_facilities_select

| Value | Meaning                            |
|:------|:-----------------------------------|
| 1     | システムキッチン                   |
| 2     | 対面式キッチン                     |
| 3     | アイランドキッチン                 |
| 4     | パントリー（食器・食品の収納庫）   |
| 5     | IHクッキングヒーター               |
| 6     | 食器洗乾燥機                       |
| 7     | ディスポーザー（生ごみ粉砕処理器） |
| 8     | 浄水器                             |

- **&lowast;**6：main_inner_image_category_1

| Value | Meaning                |
|:------|:-----------------------|
| 0     | 選択してください       |
| 1     | リビング               |
| 2     | リビング以外の居室     |
| 3     | 洗面台・洗面所         |
| 4     | キッチン               |
| 5     | 収納                   |
| 6     | 浴室                   |
| 7     | トイレ                 |
| 8     | バルコニー             |
| 9     | 庭                     |
| 10    | 玄関                   |
| 11    | その他内観             |
| 12    | 同仕様写真(リビング）  |
| 13    | 同仕様写真(キッチン）  |
| 14    | 同仕様写真(浴室）      |
| 15    | 同仕様写真(その他内観) |
| 16    | 完成予想図(内観)       |
| 17    | モデルハウス写真       |
| 18    | 展示場/ショウルーム    |
| 19    | その他                 |

**Response success:**

- サンプルオブジェクト

```JSON
{
    "id" : "cl_0000000134"
}
```

**Response failure:**

| HTTP Status | Title                        |
|:------------|:-----------------------------|
| 400         | BadRequestException          |
| 401         | UnauthorizedException        |
| 403         | ForbiddenException           |
| 500         | InternalServerErrorException |

### PUT /clientProperty

| Name                   | Method | Token    | Content-Type                   |
|:-----------------------|:-------|:---------|:-------------------------------|
| 物件情報（加盟店）登録 | PUT    | required | application/json;charset=UTF-8 |

**Process:**  
1. 加盟店物件IDと入力された情報を引数として、一致する加盟店物件情報を加盟店物件情報テーブルにて更新する。
1. ステータスに関してアカウントロックされた店舗に紐づく物件は、一律掲載を止める。
    1. ステータスを掲載止に変更する。
    1. 変更日時と同時に変更した担当者名が物件一覧に表示されるが、システムが変更した旨が分かる名称を表示する（お名前はご検討お願いします）。
1. アカウントロックを解除した場合にも、ステータスは掲載止のまま変更しない。
    1. 要は自動で一括「販売中」にすることはない。
    1. 加盟店に再度手でステータスを変更してもらう運用とする。

**Entity:**  
- T_CLIENT_PROPERTY_BASE, T_CLIENT_PROPERTY, T_CLIENT_PROPERTY_LAND  **<-- TODO:ソースみて、気づいたら直す**

**Path parameters:**  
- N/A

**Querystring:**  
- N/A

**Request body:**  

- サンプルオブジェクト  

```JSON
{
    "id" : "cl_0000000134",
    "type":1,
    "sale_status":2,
    "ad_comment":"※価格・販売戸数は未定です。\r\n※販売開始まで契約または予約の申込および申込順位の確保につながる行為は一切できません。\r\n※★月販売予定。\r\n※第★期の販売住戸が未確定のため、物件データは第★期以降の全販売対象住戸のものを表記しています。確定情報は新規分譲広告にて明示します。",
    "name":"野村不動産　プラウドシーズン西落合",
    "sale_housiki":0,
    "sale_start":2,
    "sale_start_year":2019,
    "sale_start_month":1,
    "sale_start_timing":3,
    "sentyaku_year_month_day":"2019-04-15",
    "sale_schedule_comment":"来月金額公開予定！",
    "postal_code_first":"141",
    "postal_code_last":"0033",
    "prefectures_list":1,
    "city_list":1101,
    "town_list":11010000,
    "jityou_list":"1丁目",
    "tiban":"1番地",
    "gouban":"7号",
    "address_last":"青山外苑ビル9F",
    "latitude":123.456,
    "longitude":456.123,
    "main_access":1,
    "ensen_name":1002,
    "station_name":100201,
    "access_type":1,
    "walk_time":10,
    "bus_time":20,
    "getoff_busstop":"六本木駅前",
    "getoff_busstop_walk_time":15,
    "running_distance_1":10,
    "running_distance_2":2,
    "bus_company":"東急バス",
    "busstop":"新宿駅前",
    "others_access_1":1,
    "ensen_name_1":1002,
    "station_name_1":100201,
    "access_type_1":1,
    "walk_time_1":10,
    "bus_time_1":20,
    "getoff_busstop_1":"六本木駅前",
    "getoff_busstop_walk_time_1":15,
    "running_distance_1_1":10,
    "running_distance_1_2":2,
    "bus_company_1":"東急バス",
    "busstop_1":"新宿駅前",
    "others_access_2":1,
    "ensen_name_2":1002,
    "station_name_2":100201,
    "access_type_2":1,
    "walk_time_2":10,
    "bus_time_2":20,
    "getoff_busstop_2":"六本木駅前",
    "getoff_busstop_walk_time_2":15,
    "running_distance_2_1":10,
    "running_distance_2_2":2,
    "bus_company_2":"東急バス",
    "busstop_2":"新宿駅前",
    "price_setting":1,
    "price_1":2000,
    "price_handle":2,
    "price_2":3000,
    "site_right_type":1,
    "land_area_type":1,
    "land_area_1":100,
    "land_area_under_decimal_1":15,
    "land_area_handle":1,
    "land_area_2":200,
    "land_area_under_decimal_2":30,
    "shidoufutan_type":0,
    "shidoumenseki":100,
    "shidoumenseki_decimal":11,
    "share_ratio":51,
    "share_ratio_under_decimal":2,
    "overall_ratio":90,
    "overall_ratio_under_decimal":21,
    "building_area_type":1,
    "building_area_1":200,
    "building_area_under_decimal_1":30,
    "building_area_handle":2,
    "building_area_2":300,
    "building_area_under_decimal_2":20,
    "sale_house_num_type":1,
    "sale_house_num":3,
    "total_house_num":10,
    "room_num_1":2,
    "layout_type_1":12,
    "service_rule_type_1":1,
    "layout_handle":1,
    "room_num_2":3,
    "layout_type_2":5,
    "service_rule_type_2":1,
    "complete_time_type":1,
    "complete_time_select":2,
    "complete_year":2019,
    "complete_month":4,
    "complete_decade":4,
    "comlete_year_month_day":"2019-04-15",
    "complete_after_contract":2,
    "building_situation":2,
    "parking_type":2,
    "advertiser_company_trade_aspect_type":17,
    "trade_aspect_1":"",
    "advertiser_company_trade_aspect_type_2":6,
    "company_postalcode_2":"141",
    "company_area_num_2":"0033",
    "company_address_2":"",
    "position_group_name_2":"経団連",
    "license_num_2":"第HPA-17-10449-1号",
    "company_name_2":"",
    "advertiser_company_trade_aspect_type_3":1,
    "company_postalcode_3":"141",
    "company_area_num_3":"0033",
    "company_address_3":"東京都品川区西品川1-2-3-444",
    "position_group_name_3":"経団連",
    "license_num_3":"第HPA-17-10449-1号",
    "company_name_3":"",
    "station_conveniene_selecct":[1,3],
    "dwelling_unit_floors_num_select":[1],
    "lighting_ventication_select":[1,6,10],
    "character_madori_selecet":[6,7],
    "kitchen_concerned_facilities_select":[1,2,3],
    "parking_select":[1,6],
    "reform_renovation_select":[1,5,6],
    "main_inner_image_1":"",
    "main_inner_image_category_1":10,
    "main_inner_image_caption_1":"",
    "sub_inner_image_2":"",
    "sub_inner_image_category_2":19,
    "sub_inner_image_caption_2":"",
    "main_outer_image_1":"",
    "main_outer_image_category_1":11,
    "main_outer_image_caption_1":"",
    "sub_outer_image_2":"",
    "sub_outer_image_category_2":1,
    "sub_outer_image_caption_2":"",
    "internal_memo":"社内のメモです",
    "store_id":"IM00001",
    "posting_priority_value":10,
    "jimukyoku_memo":"事務局のメモです"
}
```

- 説明

| JSON Key                               | Type   | Size | Mandatory | Search   | Note                              | Format                                                                                                                                                    |
|:---------------------------------------|:-------|:-----|:----------|:---------|:----------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------|
| id                                     | string | 13   | yes       | 完全一致 | 物件ID                            | `cl_` + 半角数字10桁                                                                                                                                      |
| type                                   | number | 1    | yes       | N/A      | 物件種別                          | 1=一戸建て、2=二戸建て以上                                                                                                                                |
| sale_status                            | number | 1    | yes       | N/A      | 物件ステータス                    | 0=下書き、1=販売予告中、2=販売中、3=成約済、4=掲載止                                                                                                      |
| ad_comment                             | string | 1000 | no        | N/A      | 予告広告補足コメント              | 任意の文字列                                                                                                                                              |
| name                                   | string | 100  | yes       | N/A      | 物件名                            | 任意の文字列                                                                                                                                              |
| sale_housiki                           | number | 1    | no        | N/A      | 販売方式                          | 最低限機能verの場合は **0** 固定<br>（0=未定、1=先着順、2=登録抽選）                                                                                      |
| sale_start                             | number | 1    | no        | N/A      | 販売開始方式区分                  | 1=年月、2=年月日                                                                                                                                          |
| sale_start_year                        | number | 4    | no        | N/A      | 販売開始年                        | 4桁の数値（ex.2019）                                                                                                                                      |
| sale_start_month                       | number | 2    | no        | N/A      | 販売開始月                        | 1 - 12                                                                                                                                                    |
| sale_start_timing                      | number | 1    | no        | N/A      | 販売開始旬                        | 1=初旬、2=上旬、3=中旬、4=下旬、5=末、0=未選択                                                                                                            |
| sentyaku_year_month_day                | string | 10   | no        | N/A      | 販売開始年月日参照表示欄          | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| sale_schedule_comment                  | string | 1000 | no        | N/A      | 販売スケジュールコメント入力欄    | 任意の文字列                                                                                                                                              |
| postal_code_first                      | string | 5    | no        | N/A      | 郵便区分番号                      | 半角数字（0始まり含む）                                                                                                                                   |
| postal_code_last                       | string | 4    | no        | N/A      | 町域番号                          | 半角数字（0始まり含む）                                                                                                                                   |
| prefectures_list                       | number | 2    | yes       | N/A      | 都道府県選択リスト                | 半角数字                                                                                                                                                  |
| city_list                              | number | 11   | yes       | N/A      | 市区郡選択リスト                  | 数値                                                                                                                                                      |
| town_list                              | number | N/A  | yes       | N/A      | 町村郡選択リスト                  | 数値                                                                                                                                                      |
| jityou_list                            | string | 50   | no        | N/A      | 字丁選択リスト                    | 任意の文字列（ex.1丁目）                                                                                                                                  |
| tiban                                  | string | 10   | no        | N/A      | 地番入力欄                        | 任意の文字列（ex.1番地）                                                                                                                                  |
| gouban                                 | string | 10   | no        | N/A      | 号番入力欄                        | 任意の文字列（ex.7号）                                                                                                                                    |
| address_last                           | string | 50   | no        | N/A      | 住所末尾入力欄                    | 任意の文字列（ex.青山外苑ビル9F）                                                                                                                         |
| latitude                               | number | 11   | yes       | N/A      | 緯度                              | 浮動小数点を含む数値                                                                                                                                      |
| longitude                              | number | 12   | yes       | N/A      | 経度                              | 浮動小数点を含む数値                                                                                                                                      |
| main_access                            | number | 1    | yes       | N/A      | 主要交通区分                      | 1=電車、2=バス                                                                                                                                            |
| ensen_name                             | number | 10   | no        | N/A      | 路線参照表示欄                    | 半角数字                                                                                                                                                  |
| station_name                           | number | 10   | no        | N/A      | 駅参照表示欄                      | 半角数字                                                                                                                                                  |
| access_type                            | number | 1    | no        | N/A      | 交通手段区分                      | 1=徒歩、2=バス、3=車                                                                                                                                      |
| walk_time                              | number | 2    | no        | N/A      | 徒歩時間入力欄                    | 半角数字（単位：分）                                                                                                                                      |
| bus_time                               | number | 2    | no        | N/A      | バス乗車時間入力欄                | 半角数字（単位：分）                                                                                                                                      |
| getoff_busstop                         | string | 25   | no        | N/A      | 下車バス停入力欄                  | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| getoff_busstop_walk_time               | number | 2    | no        | N/A      | 下車バス停_徒歩時間入力           | 半角数字（単位：分）                                                                                                                                      |
| running_distance_1                     | number | 5    | no        | N/A      | 走行距離1                         | 半角数字（単位：km）                                                                                                                                      |
| running_distance_2                     | number | 2    | no        | N/A      | 走行距離2                         | 半角数字                                                                                                                                                  |
| bus_company                            | string | 50   | no        | N/A      | バス会社入力欄                    | 任意の文字列（ex.東急バス）                                                                                                                               |
| busstop                                | string | 50   | no        | N/A      | バス停入力欄                      | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| others_access_1                        | number | 1    | no        | N/A      | その他 交通1区分                  | 1=電車、2=バス                                                                                                                                            |
| ensen_name_1                           | number | 10   | no        | N/A      | 路線参照表示欄1                   | 半角数字                                                                                                                                                  |
| station_name_1                         | number | 10   | no        | N/A      | 駅参照表示欄1                     | 半角数字                                                                                                                                                  |
| access_type_1                          | number | 1    | no        | N/A      | 交通手段区分1                     | 1=徒歩、2=バス、3=車                                                                                                                                      |
| walk_time_1                            | number | 2    | no        | N/A      | 徒歩時間入力欄1                   | 半角数字（単位：分）                                                                                                                                      |
| bus_time_1                             | number | 2    | no        | N/A      | バス乗車時間入力欄1               | 半角数字（単位：分）                                                                                                                                      |
| getoff_busstop_1                       | string | 25   | no        | N/A      | 下車バス停入力欄1                 | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| getoff_busstop_walk_time_1             | number | 2    | no        | N/A      | 下車バス停_徒歩時間入力1          | 半角数字（単位：分）                                                                                                                                      |
| running_distance_1_1                   | number | 5    | no        | N/A      | 走行距離1                         | 半角数字（単位：km）                                                                                                                                      |
| running_distance_1_2                   | number | 2    | no        | N/A      | 走行距離2                         | 半角数字                                                                                                                                                  |
| bus_company_1                          | string | 25   | no        | N/A      | バス会社入力欄1                   | 任意の文字列（ex.東急バス）                                                                                                                               |
| busstop_1                              | string | 25   | no        | N/A      | バス停入力欄1                     | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| others_access_2                        | number | 1    | no        | N/A      | その他 交通2区分                  | 1=電車、2=バス                                                                                                                                            |
| ensen_name_2                           | number | 10   | no        | N/A      | 路線参照表示欄2                   | 半角数字                                                                                                                                                  |
| station_name_2                         | number | 10   | no        | N/A      | 駅参照表示欄2                     | 半角数字                                                                                                                                                  |
| access_type_2                          | number | 1    | no        | N/A      | 交通手段区分2                     | 1=徒歩、2=バス、3=車                                                                                                                                      |
| walk_time_2                            | number | 2    | no        | N/A      | 徒歩時間入力欄2                   | 半角数字（単位：分）                                                                                                                                      |
| bus_time_2                             | number | 2    | no        | N/A      | バス乗車時間入力欄2               | 半角数字（単位：分）                                                                                                                                      |
| getoff_busstop_2                       | string | 25   | no        | N/A      | 下車バス停入力欄2                 | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| getoff_busstop_walk_time_2             | number | 2    | no        | N/A      | 下車バス停_徒歩時間入力2          | 半角数字（単位：分）                                                                                                                                      |
| running_distance_2_1                   | number | 5    | no        | N/A      | 走行距離2_1                       | 半角数字（単位：km）                                                                                                                                      |
| running_distance_2_2                   | number | 2    | no        | N/A      | 走行距離2_2                       | 半角数字                                                                                                                                                  |
| bus_company_2                          | string | 25   | no        | N/A      | バス会社入力欄2                   | 任意の文字列（ex.東急バス）                                                                                                                               |
| busstop_2                              | string | 25   | no        | N/A      | バス停入力欄2                     | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| price_setting                          | number | 1    | yes       | N/A      | 価格設定                          | 0=未定、1=予定・確定                                                                                                                                      |
| price_1                                | number | 6    | yes       | N/A      | 価格入力欄1                       | 半角数字（単位：JPY）                                                                                                                                     |
| price_handle                           | number | 1    | yes       | N/A      | 価格入力欄の取り扱い              | 1="～"、2="・"（中点）                                                                                                                                    |
| price_2                                | number | 6    | yes       | N/A      | 価格入力欄2                       | 半角数字（単位：JPY）                                                                                                                                     |
| site_right_type                        | number | 1    | yes       | N/A      | 敷地権利区分                      | 1=所有権のみ、2=借地権のみ、3=所有権・借地権混在                                                                                                          |
| land_area_type                         | number | 1    | yes       | N/A      | 土地面積区分                      | 1=登記、2=実測、0=未選択                                                                                                                                  |
| land_area_1                            | number | 6    | yes       | N/A      | 土地面積数1入力欄                 | 半角数字                                                                                                                                                  |
| land_area_under_decimal_1              | number | 2    | yes       | N/A      | 土地面積数（小数点以下）1入力欄   | 半角数字                                                                                                                                                  |
| land_area_handle                       | number | 1    | yes       | N/A      | 土地面積数欄の取り扱い            | 1="～"、2="."（小数点）                                                                                                                                   |
| land_area_2                            | number | 6    | yes       | N/A      | 土地面積数2入力欄                 | 半角数字                                                                                                                                                  |
| land_area_under_decimal_2              | number | 2    | yes       | N/A      | 土地面積数（小数点以下）2入力欄   | 半角数字                                                                                                                                                  |
| shidoufutan_type                       | number | 1    | no        | N/A      | 私道負担区分                      | 0=無、1=有、2=共有                                                                                                                                        |
| shidoumenseki                          | number | 4    | no        | N/A      | 私道面積数入力欄                  | 半角数字                                                                                                                                                  |
| shidoumenseki_decimal                  | number | 2    | no        | N/A      | 私道面積数（小数点以下）入力欄    | 半角数字                                                                                                                                                  |
| share_ratio                            | number | 5    | no        | N/A      | 持分比率入力欄                    | 半角数字（単位：％）                                                                                                                                      |
| share_ratio_under_decimal              | number | 2    | no        | N/A      | 持分比率（小数点以下）入力欄      | 半角数字                                                                                                                                                  |
| overall_ratio                          | number | 9    | no        | N/A      | 全体比率入力欄                    | 半角数字                                                                                                                                                  |
| overall_ratio_under_decimal            | number | 2    | no        | N/A      | 全体比率（小数点以下）入力欄      | 半角数字                                                                                                                                                  |
| building_area_type                     | number | 1    | yes       | N/A      | 建物面積区分                      | 1=登記、2=実測、0=未選択                                                                                                                                  |
| building_area_1                        | number | 4    | yes       | N/A      | 建物面積数1入力欄                 | 半角数字（単位：㎡）                                                                                                                                      |
| building_area_under_decimal_1          | number | 2    | no        | N/A      | 建物面積数（小数点以下）1入力欄   | 半角数字                                                                                                                                                  |
| building_area_handle                   | number | 1    | yes       | N/A      | 建物面積数欄の取り扱い            | 1="～"、2="."（小数点）                                                                                                                                   |
| building_area_2                        | number | 4    | yes       | N/A      | 建物面積数2入力欄                 | 半角数字（単位：㎡）                                                                                                                                      |
| building_area_under_decimal_2          | number | 2    | no        | N/A      | 建物面積数（小数点以下）2入力欄   | 半角数字                                                                                                                                                  |
| sale_house_num_type                    | number | 1    | yes       | N/A      | 販売戸数設定                      | 0= 未定、1= 予定・確定                                                                                                                                    |
| sale_house_num                         | number | 5    | yes       | N/A      | 販売戸数入力欄                    | 半角数字（単位：戸）                                                                                                                                      |
| total_house_num                        | number | 5    | no        | N/A      | 総戸数入力欄                      | 半角数字（単位：戸）                                                                                                                                      |
| room_num_1                             | number | 2    | yes       | N/A      | 部屋数入力欄1                     | 半角数字：（単位：部屋）                                                                                                                                  |
| layout_type_1                          | number | 2    | yes       | N/A      | 間取りタイプ設定1                 | 0=選択してください、1=ワンルーム、2=K<br>3=DK、4=LDK、5=LK、6=KK、7=DKK、8=LKK<br>9=DDKK、10=LLKK、11=LDKK、12=LDDKK、13=LLDKK                            |
| service_rule_type_1                    | number | 1    | no        | N/A      | サービスルール設定1               | 0=選択してください、1=+S、2=+2S、3=+3S                                                                                                                    |
| layout_handle                          | number | 1    | no        | N/A      | 間取りの取り扱い                  | 1="～"、2="."（小数点）                                                                                                                                   |
| room_num_2                             | number | 2    | no        | N/A      | 部屋数入力欄2                     | 半角数字：（単位：部屋）                                                                                                                                  |
| layout_type_2                          | number | 2    | yes       | N/A      | 間取りタイプ設定2                 | 0=選択してください、1=ワンルーム、2=K<br>3=DK、4=LDK、5=LK、6=KK、7=DKK、8=LKK<br>9=DDKK、10=LLKK、11=LDKK、12=LDDKK、13=LLDKK                            |
| service_rule_type_2                    | number | 1    | no        | N/A      | サービスルール設定2               | 0=選択してください、1=+S、2=+2S、3=+3S                                                                                                                    |
| complete_time_type                     | number | 1    | yes       | N/A      | 完成時期区分                      | 0= 未選択、1= 完成予定、2= 完成済、3= 契約後                                                                                                              |
| complete_time_select                   | number | 1    | yes       | N/A      | 完成時期方式                      | 1= 年月、2= 年月日                                                                                                                                        |
| complete_year                          | number | 4    | no        | N/A      | 完成年                            | 半角数字：（単位：年）                                                                                                                                    |
| complete_month                         | number | 2    | no        | N/A      | 完成月                            | 半角数字：（単位：月）                                                                                                                                    |
| complete_decade                        | number | 1    | no        | N/A      | 完成旬                            | 0= 未選択、1= 初旬、2= 上旬、3= 中旬、4= 下旬、5= 末                                                                                                      |
| comlete_year_month_day                 | string | 10   | no        | N/A      | 完成年月日参照表示欄              | YYYY-MM-DD                                                                                                                                                |
| complete_after_contract                | number | 2    | no        | N/A      | 完成時期契約後入力欄              | 半角数字                                                                                                                                                  |
| building_situation                     | number | 1    | yes       | N/A      | 建物状況区分                      | 1= 新築、2= 未入居、3= 中古                                                                                                                               |
| parking_type                           | number | 1    | no        | N/A      | 駐車場区分                        | 1= 車庫、2= 地下車庫、3= カースペース<br>4= カーポート、5= 無、0= 未選択                                                                                  |
| advertiser_company_trade_aspect_type   | number | 2    | yes       | N/A      | 広告主（貴社）会社取引態様区分    | **See &lowast;1**                                                                                                                                         |
| trade_aspect_1                         | string | 20   | no        | N/A      | 取引態様1入力欄                   | 任意の文字列                                                                                                                                              |
| advertiser_company_trade_aspect_type_2 | number | 2    | no        | N/A      | 会社2取引態様区分                 | **See &lowast;2**                                                                                                                                         |
| company_postalcode_2                   | string | 5    | no        | N/A      | 会社2郵便区分番号入力欄           | 半角数字                                                                                                                                                  |
| company_area_num_2                     | string | 4    | no        | N/A      | 会社2町域番号入力欄               | 半角数字                                                                                                                                                  |
| company_address_2                      | string | 150  | no        | N/A      | 住所2入力欄                       | 任意の文字列                                                                                                                                              |
| position_group_name_2                  | string | 200  | no        | N/A      | 所属団体名2入力欄                 | 任意の文字列                                                                                                                                              |
| license_num_2                          | string | 30   | no        | N/A      | 免許番号2入力欄                   | 任意の文字列                                                                                                                                              |
| company_name_2                         | string | 150  | no        | N/A      | 社名2入力欄                       | 任意の文字列                                                                                                                                              |
| advertiser_company_trade_aspect_type_3 | number | 2    | no        | N/A      | 会社3取引態様区分                 | **See &lowast;2**                                                                                                                                         |
| company_postalcode_3                   | string | 5    | no        | N/A      | 会社3郵便区分番号入力欄           | 半角数字                                                                                                                                                  |
| company_area_num_3                     | string | 4    | no        | N/A      | 会社3町域番号入力欄               | 半角数字                                                                                                                                                  |
| company_address_3                      | string | 150  | no        | N/A      | 住所3入力欄                       | 任意の文字列                                                                                                                                              |
| position_group_name_3                  | string | 200  | no        | N/A      | 所属団体名3入力欄                 | 任意の文字列                                                                                                                                              |
| license_num_3                          | string | 30   | no        | N/A      | 免許番号3入力欄                   | 任意の文字列                                                                                                                                              |
| company_name_3                         | string | 150  | no        | N/A      | 社名3入力欄                       | 任意の文字列                                                                                                                                              |
| station_conveniene_selecct             | array  | N/A  | no        | N/A      | 駅利便性設定                      | number in array（複数可能）<br>1= 2沿線以上利用可、2= 検索駅まで平坦、3= 始発駅                                                                           |
| dwelling_unit_floors_num_select        | array  | N/A  | no        | N/A      | 住戸・階数設定                    | number in array（複数可能）<br>1= 平屋、2= 2階建て、3= 3階建て以上（複数可能）                                                                            |
| lighting_ventication_select            | array  | N/A  | no        | N/A      | 陽当り・採光・通風設定            | number in array（複数可能）<br>**See &lowast;3**                                                                                                          |
| character_madori_selecet               | array  | N/A  | no        | N/A      | 間取り設定                        | number in array（複数可能）<br>**See &lowast;4**                                                                                                          |
| kitchen_concerned_facilities_select    | array  | N/A  | no        | N/A      | キッチン・関連設備設定            | number in array（複数可能）<br>**See &lowast;5**                                                                                                          |
| parking_select                         | array  | N/A  | no        | N/A      | 駐車・駐輪設定                    | number in array（複数可能）<br>1= 駐車2台可、2= 駐車3台以上可、3= ハイルーフ駐車場<br>4= EV車充電設備、5= シャッター車庫、6= ビルトインガレージ           |
| reform_renovation_select               | array  | N/A  | no        | N/A      | ﾘﾌｫｰﾑ・ﾘﾉﾍﾞｰｼｮﾝ設定               | number in array（複数可能）<br>1= 適合リノベーション、2= 内外装リフォーム、3= 内装リフォーム<br>4= 外装リフォーム、5= フローリング張替、6= リノベーション |
| main_inner_image_1                     | string | 255  | no        | N/A      | 内観・その他メイン画像1           | パス（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg）                                                                          |
| main_inner_image_category_1            | number | 2    | no        | N/A      | 内観・その他メイン画像1カテゴリー | **See &lowast;6**                                                                                                                                         |
| main_inner_image_caption_1             | string | 100  | no        | N/A      | メイン画像1キャプション           | 任意の文字列                                                                                                                                              |
| sub_inner_image_2                      | string | 255  | no        | N/A      | 画像2                             | パス（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg）                                                                          |
| sub_inner_image_category_2             | number | 2    | no        | N/A      | 画像2カテゴリー                   | **See &lowast;6 **                                                                                                                                        |
| sub_inner_image_caption_2              | string | 100  | no        | N/A      | 画像2キャプション                 | 任意の文字列                                                                                                                                              |
| main_outer_image_1                     | string | 255  | no        | N/A      | メイン画像1                       | パス（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg）                                                                          |
| main_outer_image_category_1            | number | 2    | no        | N/A      | メイン画像1カテゴリー             | **See &lowast;6**                                                                                                                                         |
| main_outer_image_caption_1             | string | 100  | no        | N/A      | メイン画像1キャプション           | 任意の文字列                                                                                                                                              |
| sub_outer_image_2                      | string | 255  | no        | N/A      | 画像2                             | パス（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg）                                                                          |
| sub_outer_image_category_2             | number | 2    | no        | N/A      | 画像2カテゴリー                   | **See &lowast;6**                                                                                                                                         |
| sub_outer_image_caption_2              | string | 100  | no        | N/A      | 画像2キャプション                 | 任意の文字列                                                                                                                                              |
| internal_memo                          | string | 2000 | no        | N/A      | 社内メモ入力欄                    | 任意の文字列                                                                                                                                              |
| store_id                               | string | 7    | yes       | N/A      | 店舗ID                            | 半角英数記号                                                                                                                                              |
| posting_priority_value                 | number | 8    | no        | N/A      | 物件掲載優先値欄                  | 半角数字                                                                                                                                                  |
| jimukyoku_memo                         | string | 2000 | no        | N/A      | いえまるこ事務局メモ入力欄        | 任意の文字列                                                                                                                                              |

- **&lowast;1**：advertiser_company_trade_aspect_type

| Value | Meaning                  |
|:------|:-------------------------|
| 0     | 選択してください         |
| 1     | 売主                     |
| 2     | 建物売主                 |
| 3     | 土地売主                 |
| 4     | 土地貸主                 |
| 5     | 土地転貸主               |
| 6     | 販売提携(代理)           |
| 7     | 販売提携(媒介)           |
| 8     | 販売提携(復代理)         |
| 9     | 仲介(一般媒介)           |
| 10    | 仲介(専任媒介)           |
| 11    | 仲介(専属専任)           |
| 12    | 先物                     |
| 13    | 事業主・売主             |
| 14    | 事業主・建物売主         |
| 15    | 事業主・土地売主         |
| 16    | 事業主・販売提携（代理） |
| 17    | 事業主・販売提携（媒介） |

- **&lowast;2**：advertiser_company_trade_aspect_type_2 / advertiser_company_trade_aspect_type_3

| Value | Meaning          |
|:------|:-----------------|
| 0     | 選択してください |
| 1     | 売主             |
| 2     | 建物売主         |
| 3     | 土地売主         |
| 4     | 土地貸主         |
| 5     | 土地転貸主       |
| 6     | 販売提携(代理)   |
| 7     | 販売提携(復代理) |
| 8     | 販売提携(媒介)   |
| 9     | 仲介             |

- **&lowast;3**：lighting_ventication_select

| Value | Meaning      |
|:------|:-------------|
| 1     | 南向き       |
| 2     | 陽当り良好   |
| 3     | 3面採光      |
| 4     | 東南向き     |
| 5     | 全室南向き   |
| 6     | 通風良好     |
| 7     | 南西向き     |
| 8     | 全室2面採光  |
| 9     | 全室南西向き |
| 10    | 全室東南向き |

- **&lowast;4**：character_madori_selecet

| Value | Meaning       |
|:------|:--------------|
| 0     | LDK20畳以上   |
| 1     | LDK18畳以上   |
| 2     | LDK15畳以上   |
| 3     | 和室          |
| 4     | ロフト        |
| 5     | 吹抜け        |
| 6     | 全居室6畳以上 |
| 7     | 2世帯住宅     |
| 8     | 可動間仕切り  |

- **&lowast;**5：kitchen_concerned_facilities_select

| Value | Meaning                            |
|:------|:-----------------------------------|
| 1     | システムキッチン                   |
| 2     | 対面式キッチン                     |
| 3     | アイランドキッチン                 |
| 4     | パントリー（食器・食品の収納庫）   |
| 5     | IHクッキングヒーター               |
| 6     | 食器洗乾燥機                       |
| 7     | ディスポーザー（生ごみ粉砕処理器） |
| 8     | 浄水器                             |

- **&lowast;**6：main_inner_image_category_1

| Value | Meaning                |
|:------|:-----------------------|
| 0     | 選択してください       |
| 1     | リビング               |
| 2     | リビング以外の居室     |
| 3     | 洗面台・洗面所         |
| 4     | キッチン               |
| 5     | 収納                   |
| 6     | 浴室                   |
| 7     | トイレ                 |
| 8     | バルコニー             |
| 9     | 庭                     |
| 10    | 玄関                   |
| 11    | その他内観             |
| 12    | 同仕様写真(リビング）  |
| 13    | 同仕様写真(キッチン）  |
| 14    | 同仕様写真(浴室）      |
| 15    | 同仕様写真(その他内観) |
| 16    | 完成予想図(内観)       |
| 17    | モデルハウス写真       |
| 18    | 展示場/ショウルーム    |
| 19    | その他                 |

**Response success:**

- サンプルオブジェクト

```JSON
{
    "id" : "cl_0000000134"
}
```

**Response failure:**

| HTTP Status | Title                        |
|:------------|:-----------------------------|
| 400         | BadRequestException          |
| 401         | UnauthorizedException        |
| 403         | ForbiddenException           |
| 500         | InternalServerErrorException |

### POST /clientProperty/list

| Name                       | Method | Token    | Content-Type                   |
|:---------------------------|:-------|:---------|:-------------------------------|
| 物件情報（加盟店）一覧表示 | POST   | required | application/json;charset=UTF-8 |

- 本来 GET にするべきだが、現状、POST で設計されているので、POST のままとする。  

**Process:**  
1. 加盟店物件一覧情報を取得する。

**Path parameters:**  
- N/A

**Querystring:**  
- N/A

**Request body:**  
- サンプルオブジェクト

```json
{  
    "size" : 100,
    "page_no" : 1,
    "store_id" : "IM00001",
    "sale_status": 0,
    "property_name" : "野村不動産",
    "postal_code_first" : "108",
    "postal_code_last" : "0075",
    "address" : "東京都",
}
```

- 説明

| JSON Key          | 型     | サイズ | 必須 | 検索条件   | 値の説明               | フォーマット                                     |
|:------------------|:-------|:-------|:-----|:-----------|:-----------------------|:-------------------------------------------------|
| size              | number | 3      | no   | 返却件数   | 返却結果件数の最大件数 | 1 - 100（default = 30）                          |
| page_no           | number | N/A    | no   | ページ番号 | 返却ページ番号         | 半角数字（default = 30）                         |
| store_id          | string | 7      | no   | 完全一致   | 店舗ID                 | "IM" + 半角数字5桁                               |
| sale_status       | number | 1      | no   | 完全一致   | 物件ステータス         | 0=下書き,1=販売予告中,2=販売中,3=成約済,4=掲載止 |
| property_name     | string | 100    | no   | 部分一致   | 物件名                 | 任意の文字列                                     |
| postal_code_first | string | 3      | no   | 完全一致   | 郵便区分番号           | 半角数字                                         |
| postal_code_last  | string | 4      | no   | 完全一致   | 町域番号               | 半角数字                                         |
| address           | string | 4      | no   | 部分一致   | 所在地                 | 任意の文字列                                     |

**Response body:**  

- サンプルオブジェクト  

```JSON
{
    "found" : 2,
    "start": 1,
    "hit": [
        {
            "id" : "cl_0000000134",
            "status_label" : "新築一戸建て",
            "name" : "野村不動産　プラウドシーズン西落合",
            "sale_house_num" : 2,
            "total_house_num" : 1,
            "price_text" : "5280万円〜5500万円",
            "postal_code" : "141-0033",
            "address" : "東京都新宿区",
            "sale_status" : 1,
            "sale_status_text" : "下書き",
            "store_name" : "ケイアイネットリアリティ",
            "score" : 100,
            "release_at" : "2019-03-01T06:25:26.056Z",
            "release_user" : "system",
            "created_at" : "2019-03-01T06:25:26.056Z",
            "create_user" : "system",
            "updated_at" : "2019-03-01T06:25:26.056Z",
            "update_user" : "system"
        }
    ]
}
```

- 説明

| JSON Key               | 型     | サイズ | 必須 | 検索条件 | 値の説明                   | フォーマット                                         |
|:-----------------------|:-------|:-------|:-----|:---------|:---------------------------|:-----------------------------------------------------|
| found                  | number | N/A    | yes  | N/A      | 総件数                     | 半角数値                                             |
| start                  | number | N/A    | yes  | N/A      | 返却レコード開始位置       | 半角数値                                             |
| hit[].id               | string | N/A    | no   | N/A      | 物件ID                     |                                                      |
| hit[].status_label     | string | ???    | no   | N/A      | 物件状況ラベル             |                                                      |
| hit[].name             | string | ???    | no   | N/A      | 物件名                     |                                                      |
| hit[].sale_house_num   | number | 11     | no   | N/A      | 物件販売戸数               | 半角数値                                             |
| hit[].total_house_num  | number | 11     | no   | N/A      | 総戸数                     | 半角数値                                             |
| hit[].price_text       | string | ???    | no   | N/A      | 物件価格（テキスト）       | 語尾に万円をつけた値                                 |
| hit[].postal_code      | string | 8      | no   | N/A      | 郵便番号                   | 半角数字 + ハイフン                                  |
| hit[].address          | string | 200    | no   | N/A      | 物件所在地                 |                                                      |
| hit[].sale_status      | number | 1      | no   | N/A      | 物件ステータス             | 0=下書き、1=販売予告中、2=販売中、3=成約済、4=掲載止 |
| hit[].sale_status_text | string | 5      | no   | N/A      | 物件ステータス（テキスト） | sales_status のテキスト                              |
| hit[].store_name       | string | ???    | no   | N/A      | 店舗名                     |                                                      |
| hit[].score            | number | ???    | no   | N/A      | 店舗優先値                 |                                                      |
| hit[].release_at       | string | ???    | no   | N/A      | 掲載開始日時               | UTC                                                  |
| hit[].release_user     | string | ???    | no   | N/A      | 掲載者名                   | 任意の文字列                                         |
| hit[].created_at       | string | ???    | no   | N/A      | 登録日時                   | UTC                                                  |
| hit[].create_user      | string | ???    | no   | N/A      | 登録者名                   | 任意の文字列                                         |
| hit[].updated_at       | string | ???    | no   | N/A      | 更新日時                   | UTC                                                  |
| hit[].update_user      | string | ???    | no   | N/A      | 更新者名                   | 任意の文字列                                         |

**Response failure:**  

| HTTP Status | Title                        |
|:------------|:-----------------------------|
| 400         | BadRequestException          |
| 401         | UnauthorizedException        |
| 403         | ForbiddenException           |
| 500         | InternalServerErrorException |

### POST /clientProperty/photo

| Name                   | Method | Token    | Content-Type        |
|:-----------------------|:-------|:---------|:--------------------|
| 物件（加盟店）写真登録 | POST   | required | multipart/form-data |

**Process:**  
1. 加盟店物件の画像の登録を行う。

**Path parameters:**  
- N/A

**Querystring:**  
- N/A

**Request form:**  

| Key      | Type   | Size | Mandatory | Search | Note       | Format             |
|:---------|:-------|:-----|:----------|:-------|:-----------|:-------------------|
| image    | file   | N/A  | yes       | N/A    | 画像データ | ファイル指定       |
| store_id | string | 7    | yes       | N/A    | 店舗ID     | IM + 5桁の半角数字 |

**Response success:**

- サンプルオブジェクト

```json
{
    "image_key": "tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg"
}
```

- 説明  

| JSON Key  | 型     | サイズ | 必須 | 検索条件 | 値の説明 | フォーマット                                                                           |
|:----------|:-------|:-------|:-----|:---------|:---------|:---------------------------------------------------------------------------------------|
| image_key | string | 1000   | yes  | N/A      | 画像キー | 画像のキー（ex.tmp/IM00001/2019-04-17T03:09:29Z/2304x1440x83aba78a828c9b7a0a0070.jpg） |

**Response failure:**

| HTTP Status | Title                        |
|:------------|:-----------------------------|
| 400         | BadRequestException          |
| 401         | UnauthorizedException        |
| 403         | ForbiddenException           |
| 404         | NotFoundException            |
| 500         | InternalServerErrorException |

### GET /clientProperty/:id

| Name                   | Method | Token    | Content-Type                   |
|:-----------------------|:-------|:---------|:-------------------------------|
| 物件情報（加盟店）取得 | GET    | required | application/json;charset=UTF-8 |

**Process:**  
1. 加盟店物件IDを検索条件の引数として渡し、一致する加盟店物件情報を加盟店物件情報テーブルから取得する。

**Path parameters:**  

| PathString Key | 型     | サイズ | 必須 | 検索条件 | 値の説明     | フォーマット           |
|:---------------|:-------|:-------|:-----|:---------|:-------------|:-----------------------|
| id             | string | N/A    | yes  | 完全一致 | 加盟店物件ID | "cl_" + 半角10桁の数値 |

**Querystring:**  
- N/A

**Request body:**  
- N/A

**Response success:**

- サンプルオブジェクト

```JSON
{
    "clientProperty": {
        "id": "cl_0000000134",
        "delete_flg": 0,
        "updated_at": "2019-04-05T05:24:46.000Z",
        "update_user": "",
        "created_at": "2019-04-05T05:24:46.000Z",
        "create_user": "system",
        "store_id": "IM00022",
        "type": 1,
        "sale_status": 2,
        "ad_comment": "",
        "name": "★ラスト1棟◎！窮屈感の無い立地｜予約受付中☆｜白沢町2期",
        "sale_term": "",
        "sale_housiki": null,
        "sale_start": null,
        "sale_start_year": null,
        "sale_start_month": null,
        "sale_start_timing": null,
        "sentyaku_year_month_day": "2019-04-22",
        "sentyaku_start": null,
        "sentyaku_start_year": null,
        "sentyaku_start_month": null,
        "sentyaku_start_part": null,
        "sentyaku_start_year_month_day": "2019-04-22",
        "regist_type": null,
        "regist_year": null,
        "regist_month": null,
        "regist_part": null,
        "regist_start_year_month_day": "2019-04-22",
        "regist_end_year_month_day": "2019-04-22",
        "tyuusen_type": null,
        "tyuusen_year": null,
        "tyuusen_month": null,
        "tyuusen_part": null,
        "tyuusen_year_month_day": null,
        "sale_schedule": "登録受付時期：2019年04月22日～2019年04月22日～\r\n抽選時期：",
        "sale_schedule_comment": "",
        "postal_code_first": null,
        "postal_code_last": null,
        "prefectures_list": 9,
        "city_list": 9201,
        "town_list": 92010113,
        "jityou_list": null,
        "tiban": "",
        "gouban": "",
        "address_last": "",
        "street_num": "",
        "latitude": 36.62954947,
        "longitude": 139.93959746,
        "access": "JR東北本線「岡本駅」徒歩45分",
        "access_list": {
            "main": {
                "access": 1,
                "ensen_name": 11319,
                "ensen_name_text": "JR東北本線",
                "station_name": 1131924,
                "station_name_text": "岡本駅",
                "access_type": 1,
                "walk_time": 45,
                "bus_time": null,
                "getoff_busstop": "",
                "getoff_busstop_walk_time": null,
                "running_distance_1": null,
                "running_distance_2": null,
                "bus_company": "",
                "busstop": ""
            },
            "others_1": {
                "access": null,
                "ensen_name": null,
                "ensen_name_text": "",
                "station_name": null,
                "station_name_text": "",
                "access_type": null,
                "walk_time": null,
                "bus_time": null,
                "getoff_busstop": "",
                "getoff_busstop_walk_time": null,
                "running_distance_1": null,
                "running_distance_2": null,
                "bus_company": "",
                "busstop": ""
            },
            "others_2": {
                "access": null,
                "ensen_name": null,
                "ensen_name_text": "",
                "station_name": null,
                "station_name_text": "",
                "access_type": null,
                "walk_time": null,
                "bus_time": null,
                "getoff_busstop": "",
                "getoff_busstop_walk_time": null,
                "running_distance_1": null,
                "running_distance_2": null,
                "bus_company": "",
                "busstop": ""
            }
        },
        "kukaku": [
            {
                "updated_at": "2019-04-05T05:08:58.000Z",
                "update_user": "",
                "created_at": "2019-04-05T05:08:58.000Z",
                "create_user": "system",
                "delete_flg": 0,
                "price_1": null,
                "room_num_1": 0,
                "madori_type_1": 0,
                "service_rule_1": null,
                "madori_image": "",
                "madori_comment": "",
                "whole_kukaku_image": "",
                "caption": "",
                "kukaku_name": "",
                "kukaku_madori": "",
                "land_area": "0.0ｍ²",
                "madori_type_1_text": "",
                "service_rule_1_text": null,
                "price_1_text": null,
                "building_area": "0.0ｍ²"
            }
        ],
        "whole_kukaku_image": "",
        "caption": "",
        "kukaku_name": "",
        "price_setting": 1,
        "price_1": 2480,
        "price_handle": null,
        "price_2": null,
        "price_range_handle": 1,
        "price_hosoku": "",
        "most_price_1": null,
        "most_price_2": null,
        "most_price_3": null,
        "most_price_4": null,
        "most_price_5": null,
        "most_price_hosoku": "",
        "most_house_num": null,
        "tyoukaihi_type": null,
        "tyoukaihi_presense": null,
        "tyoukaihi_price": null,
        "tyoukaihi_payment_type": null,
        "yuusen_fee_type": null,
        "yuusen_fee_frontend_type": null,
        "yuusen_fee_frontend_price": null,
        "yuusen_fixed_fee_type": null,
        "yuusen_fixed_fee_price": null,
        "yuusen_fixed_fee_payment_type": null,
        "internet_fee_type": null,
        "internet_frontend_fee_type": null,
        "internet_frontend_fee_payment": null,
        "internet_fixed_fee_type": null,
        "internet_fixed_fee_price": null,
        "internet_fixed_fee_payment": null,
        "catv_fee_type": null,
        "catv_frontend_fee_type": null,
        "catv_frontend_fee_payment": null,
        "catv_fixed_fee_type": null,
        "catv_fixed_fee_price": null,
        "catv_fixed_fee_payment": null,
        "spa_fee_type": null,
        "spa_frontend_fee_type": null,
        "spa_frontend_fee_payment": null,
        "spa_fixed_fee_type": null,
        "spa_type": "",
        "others_fee_name_1": "",
        "others_fee_price_1": null,
        "others_fee_payment_type_1": null,
        "others_fee_name_2": "",
        "others_fee_price_2": null,
        "others_fee_payment_type_2": null,
        "others_note": "",
        "site_right_type": 1,
        "leasehold_type": null,
        "rent_type": null,
        "rent_price": null,
        "rent_payment_type": null,
        "leasehold_term_type": null,
        "leasehold_term_year": null,
        "leasehold_term_month": null,
        "leasehold_term_rate": null,
        "premium_type": null,
        "premium_price": null,
        "land_security_deposit_type": null,
        "land_security_deposit_handle": null,
        "land_security_deposit_min": null,
        "land_security_deposit_price_handle": null,
        "land_security_deposit_max_handle": null,
        "tenancy_type": null,
        "tenancy_charge_revision_time_type": null,
        "tenancy_charge_revision_time": null,
        "tenancy_charge_revision_price_type": null,
        "tenancy_transfer_sublease_type": null,
        "consent_type": null,
        "consent_charge_type": null,
        "consenter_type": null,
        "land_area_type": null,
        "land_area_1": 226,
        "land_area_under_decimal_1": 64,
        "land_area_handle": 0,
        "land_area_2": 0,
        "land_area_under_decimal_2": null,
        "land_area_others": "",
        "coverage_ratio_ratio_floor_area_ratio": "",
        "front_road_type_1": 7,
        "front_road_width_1": 8,
        "front_road_width_under_decimal_1": 8,
        "frontage_width_1": null,
        "frontage_width_under_decimal_1": null,
        "front_road_type_2": 6,
        "front_road_width_2": 11,
        "front_road_width_under_decimal_2": 8,
        "frontage_width_2": null,
        "frontage_width_under_decimal_2": null,
        "front_road_type_3": null,
        "front_road_width_3": null,
        "front_road_width_under_decimal_3": null,
        "frontage_width_3": null,
        "frontage_width_under_decimal_3": null,
        "front_road_type_4": null,
        "front_road_width_4": null,
        "front_road_width_under_decimal_4": null,
        "frontage_width_4": null,
        "frontage_width_under_decimal_4": null,
        "remarks": "",
        "shidoufutan_type": 0,
        "shidoumenseki": null,
        "shidoumenseki_decimal": null,
        "share_ratio": null,
        "share_ratio_under_decimal": null,
        "overall_ratio": null,
        "overall_ratio_under_decimal": null,
        "easement_type": null,
        "easement": null,
        "easement_under_decimal": null,
        "waterrwork_type": 1,
        "sewerage_type": 1,
        "gas_oar_electrification_type": 3,
        "setback_type": null,
        "setback_area": null,
        "setback_area_under_decimal": null,
        "zoning_1": 6,
        "zoning_2": null,
        "zoning_3": null,
        "zoning_4": null,
        "land_category": "",
        "planning_permission_reason_type": null,
        "building_area_type": 0,
        "building_area_1": 108,
        "building_area_under_decimal_1": 74,
        "building_area_handle": 0,
        "building_area_2": 0,
        "building_area_under_decimal_2": null,
        "building_area_others": "",
        "sale_house_num_type": 1,
        "sale_house_num": 1,
        "total_house_num": 3,
        "room_num_1": 4,
        "layout_type_1": 4,
        "service_rule_type_1": null,
        "layout_handle": 0,
        "room_num_2": 0,
        "layout_type_2": 0,
        "service_rule_type_2": null,
        "complete_time_type": 2,
        "complete_time_select": 1,
        "complete_year": 2018,
        "complete_month": 12,
        "complete_decade": 0,
        "comlete_year_month_day": "2019-01-01",
        "complete_after_contract": null,
        "entering_time_type": 1,
        "entering_time_select": null,
        "entering_year": null,
        "entering_month": null,
        "entering_decade": null,
        "entering_year_month_day": "2019-01-01",
        "entering_after_contract": null,
        "building_situation": 1,
        "main_structure_type": 3,
        "main_structure_others": "",
        "part_structure_type": null,
        "part_structure_others": "",
        "method_type": 3,
        "method_others": "",
        "above_ground_floors_num": 2,
        "under_ground_floors_num": null,
        "parking_type": 3,
        "construction_company": "",
        "structure_method_floor_num_others": "",
        "building_certification_num": "第18UDI1W建05787号　平成30年8月27日",
        "exterior_type": 0,
        "exterior_reform_year": null,
        "exterior_reform_month": null,
        "exterior_reform_point_type": "",
        "exterior_reform_others": "",
        "interior_type": 0,
        "interior_reform_year": null,
        "interior_reform_month": null,
        "interior_reform_point_type": "",
        "interior_reform_others": "",
        "interior_reform_others_memo": "ガス：個別LPG／水道：公営水道／排水：公共下水／雑排水：公共下水\r\n一部都市計画道路、計画道路有（3・4・103 　宇都宮白沢線 　計画決定：昭和47年3月1日）都市計画法第53条届出済\r\n（2号棟：宇都宮指令　都　第13.-31号）81.95m2 \r\n \r\n ※売主直接の販売は行っておりません。お問い合わせはケイアイネットクラウド株式会社はなまるハウス宇都宮展示場までご連絡ください。 \r\n",
        "other_memo": "ガス：個別LPG／水道：公営水道／排水：公共下水／雑排水：公共下水\r\n一部都市計画道路、計画道路有（3・4・103 　宇都宮白沢線 　計画決定：昭和47年3月1日）都市計画法第53条届出済\r\n（2号棟：宇都宮指令　都　第13.-31号）81.95m2 \r\n \r\n ※売主直接の販売は行っておりません。お問い合わせはケイアイネットクラウド株式会社はなまるハウス宇都宮展示場までご連絡ください。 \r\n",
        "sale_house_num_text": "1戸",
        "complete_time": "2018年12月未選択",
        "landyouto": "２種住居",
        "advertiser_company_trade_aspect_type": 9,
        "trade_aspect_1": "",
        "mototsuki_traders_name": "",
        "area_code": null,
        "local_code": null,
        "subscriber_num": null,
        "person_in_charge": "",
        "advertiser_company_trade_aspect_type_2": null,
        "company_postalcode_2": null,
        "company_area_num_2": null,
        "company_address_2": "",
        "position_group_name_2": "",
        "license_num_2": "",
        "company_name_2": "",
        "advertiser_company_trade_aspect_type_3": null,
        "company_postalcode_3": null,
        "company_area_num_3": null,
        "company_address_3": "",
        "position_group_name_3": "",
        "license_num_3": "",
        "company_name_3": "",
        "statutory_limit_type_1": "",
        "statutory_limit_type_2": "",
        "statutory_limit_type_3": "",
        "limitation_others_type_1": "",
        "kijunhou_43zyou_1kou_acceptance_type": null,
        "acceptance_reason": "",
        "limitation_others": "",
        "limit_memo": "",
        "object_performance_select": "",
        "building_inspection_select": "",
        "house_hitory_select": "",
        "vacant_house_bank_select": "",
        "location_land_character_select": "3,7,8",
        "station_conveniene_selecct": "1,2",
        "dwelling_unit_floors_num_select": "[2]",
        "lighting_ventication_select": "[1,2,6]",
        "character_madori_selecet": "[2]",
        "character_inner_room_equipment_select": "2,5",
        "storing_select": "1",
        "kitchen_concerned_facilities_select": "[1,2,8]",
        "bathroom_select": "1,3,6",
        "toilet_lavatory_concerned_equipment_select": "1,2,3",
        "balcony_terrace_select": "1,3",
        "garden_select": "2,3",
        "eco_concerned_select": "5,6,7",
        "tv_communication_select": "",
        "parking_select": "[1]",
        "common_use_space_select": "",
        "management_security_select": "",
        "reform_renovation_select": "",
        "surroundings_select": "",
        "view_natural_environments_select": "",
        "expense_delivery_entering_condition_select": "",
        "charcter_others_selcet": "",
        "character": "土地50坪以上/閑静な住宅地/前道6ｍ以上/2沿線以上利用可/検索駅まで平坦/陽当り良好/自然素材使用/リビング階段/全居室収納/対面式キッチン/浴室乾燥機/浴室1坪以上/浴室に窓/シャワー付洗面化粧台/トイレ2ヶ所/温水洗浄便座/ワイドバルコニー/南面バルコニー/庭/南庭/省エネ給湯器/複層ガラス/全居室複層ガラスか複層サッシ",
        "property_details_appeal": "☆2号棟再販☆\r\n ＼家具付で販売スタート／\r\n 2/17家具付き写真更新!!\r\n プロのコーディネーターが室内のカラー・間取りに合わせてバランスよく家具を設置しました☆\r\n \r\n 宇都宮市の環状線外側で静かな暮らしのできる住環境☆\r\n \r\n 敷地はゆとりの約68坪！\r\n 歩道もあり、お子様の通学に安心の立地です☆ \r\n ---物件おすすめPoint--- \r\n 【充実した周辺環境】 \r\n ◇バス停『河内地域自治センター』→徒歩2分（約120ｍ）！通勤・通学や車が使えない時に大変便利♪ \r\n ◇ローソン宇都宮白沢店→徒歩4分（約300ｍ）！お散歩がてらにちょっと買い足し☆に大変便利♪ \r\n ◇にしだ歯科クリニック→徒歩5分（約400ｍ）！歯の健康は体の健康！定期歯科検診も歩いて行けちゃいます♪ \r\n ◇医療法人慈啓会白沢病院→徒歩8分（約600ｍ）！病気・ケガにも対応！ \r\n ◇宇都宮河内総合運動公園→徒歩6分（約450ｍ）！コンビネーション遊具・アスレチック遊具があり、休日にお子様と一緒に遊びに行くのもいいですね♪夏には水遊び場で遊べます♪ ",
        "event_category_type": "",
        "event_details_summary": "",
        "inner": {
            "main_inner": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_2": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_3": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_4": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_5": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_6": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_7": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_8": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_9": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_10": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_11": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_12": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_13": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_14": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_15": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_16": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_17": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_18": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_19": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            },
            "sub_inner_20": {
                "inner_image": "",
                "inner_category": null,
                "inner_category_text": "null",
                "inner_image_caption": ""
            }
        },
        "outer": {
            "main_outer": {
                "outer_image": "/image/cl_0000000134/main.jpg",
                "outer_category": 14,
                "outer_category_text": "その他",
                "outer_image_caption": ""
            },
            "sub_outer_2": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_3": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_4": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_5": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_6": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_7": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_8": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_9": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_10": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_11": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_12": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_13": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_14": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_15": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_16": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_17": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_18": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_19": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            },
            "sub_outer_20": {
                "outer_image": "",
                "outer_category": null,
                "outer_category_text": "null",
                "outer_image_caption": ""
            }
        },
        "surroundings_info": {
            "surroundings_info_1": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_2": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_3": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_4": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_5": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_6": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_7": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_8": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_9": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_10": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_11": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_12": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_13": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_14": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_15": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_16": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_17": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_18": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_19": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            },
            "surroundings_info_20": {
                "surroundings_info_image": "",
                "surroundings_info_category": null,
                "surroundings_info_category_text": "null",
                "surroundings_info_facility_name": "",
                "surroundings_info_distance": null,
                "surroundings_info_caption": ""
            }
        },
        "equipment": {
            "equipment_1": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_2": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_3": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_4": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_5": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_6": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_7": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_8": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_9": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            },
            "equipment_10": {
                "equipment_image": "",
                "equipment_category": null,
                "equipment_category_text": "null",
                "equipment_image_name": "",
                "equipment_image_caption": ""
            }
        },
        "kouzou": {
            "kouzou_1": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_2": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_3": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_4": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_5": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_6": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_7": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_8": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_9": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            },
            "kouzou_10": {
                "kouzou_image": "",
                "kouzou_category": null,
                "kouzou_category_text": "null",
                "kouzou_image_name": "",
                "kouzou_image_caption": ""
            }
        },
        "internal_memo": "",
        "handled_store_name": "IM00022",
        "posting_priority_value": 1,
        "jimukyoku_memo": "DB直入",
        "rakuten_tel": 37,
        "unvisible_flag": 1,
        "release_date_pre": "2019-04-05",
        "release_date_now": "2019-04-05",
        "new_label": "新着",
        "status_label": "新築一戸建て",
        "price": "2,480万円",
        "price_text": "2,480万円\r\n",
        "most_prices_text": null,
        "land_area_type_text": null,
        "building_area_type_text": "",
        "land_area_text": "226㎡",
        "building_area_text": "108㎡",
        "leasehold_type_text": null,
        "parking_type_text": "カースペース",
        "shidoufutan_text": "−",
        "kenpeiritsu_text": null,
        "yosekiritsu_text": null,
        "sales_number_text": null,
        "advertiser_company_trade_aspect_type_text_1": "仲介（一般媒介）",
        "advertiser_company_trade_aspect_type_text_2": null,
        "advertiser_company_trade_aspect_type_text_3": null,
        "madori_all": "4LDK",
        "madori": "4LDK",
        "land_area": "226.64ｍ²",
        "building_area": "108.74ｍ²",
        "address": "栃木県宇都宮市白沢町",
        "offer_date": "2019年04月05日",
        "site_right_type_text": "所有権のみ",
        "entering_time": "即入居可",
        "kouzou_kouhou": "地上2階建\r\n主要：木造\r\n工法：軸組工法"
    },
    "storeOverview": {
        "id": "IM00022",
        "category": 2,
        "status": 1,
        "name": "ケイアイネットクラウド株式会社 はなまるハウス宇都宮展示場",
        "comment": "",
        "postal_code": "3210954",
        "address": "栃木県宇都宮市元今泉7-3-11",
        "building": "（仮テキスト）",
        "latitude": 36.5630463,
        "longitude": 139.9140036,
        "main_access": 1,
        "ensen_name": 11319,
        "ensen_name_text": "JR東北本線",
        "station_name": 1131923,
        "station_name_text": "宇都宮駅",
        "access_type": 1,
        "walk_time": 19,
        "bus_time": null,
        "getoff_busstop": "",
        "getoff_busstop_walk_time": null,
        "running_distance_1": null,
        "running_distance_2": null,
        "bus_company": "",
        "busstop": "",
        "start_business_hours": "10:00",
        "end_business_hours": "20:00",
        "businesshours_memo": "",
        "regular_holiday": "水曜日",
        "character": "「無理しない。でも、妥協しない。」をコンセプトにお客様の住まい探しのご提案をさせていただいている「はなまるハウス」。お客様からのヒアリング情報を基に新築戸建はもちろん、中古住宅など幅広い選択肢からお客様一人一人に合った住まい探しをご提案しています。宇都宮市・上三川町・鹿沼市で家を探すなら「はなまるハウス宇都宮展示場」へご相談ください。",
        "property_type": "3,4,5",
        "realestate_registration_number": "国土交通大臣（１）第9280号",
        "tel": null,
        "rakuten_tel": null,
        "inquiry_mail_admin": 1,
        "inquiry_mail_staff": 0,
        "inquiry_mail_other": 0,
        "open_mail_admin": 1,
        "open_mail_staff": 0,
        "open_mail_other": 0,
        "memo": "",
        "score": 1,
        "provider_memo": "",
        "near_station": "JR東北本線「宇都宮駅」徒歩19分"
    }
}
```

- 説明

| JSON Key                                                                  | 型     | サイズ | 必須 | 検索条件 | 値の説明                        | フォーマット                                                                                                                                              |
|:--------------------------------------------------------------------------|:-------|:-------|:-----|:---------|:--------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------|
| clientProperty                                                            | object | N/A    | yes  | N/A      | 加盟店物件情報                  | JSON object                                                                                                                                               |
| clientProperty.id                                                         | string | 13     | yes  | 完全一致 | 加盟店物件ID                    | "cl_" + 半角10桁の数値                                                                                                                                    |
| clientProperty.delete_flg                                                 | number | 1      | yes  | 0 固定   | 論理削除フラグ                  | 1:削除、0:未削除                                                                                                                                          |
| clientProperty.updated_at                                                 | string | 24     | ???  | N/A      | 更新日時                        | UTC                                                                                                                                                       |
| clientProperty.update_user                                                | string | ???    | no   | N/A      | 更新者                          | 任意の文字列                                                                                                                                              |
| clientProperty.created_at                                                 | string | 24     | ???  | N/A      | 作成日時                        | UTC                                                                                                                                                       |
| clientProperty.create_user                                                | string | ???    | no   | N/A      | 作成者                          | 任意の文字列                                                                                                                                              |
| clientProperty.store_id                                                   | string | 7      | yes  | N/A      | 店舗ID                          | 半角英数記号                                                                                                                                              |
| clientProperty.type                                                       | number | 1      | yes  | N/A      | 物件種別                        | 1=一戸建て、2=二戸建て以上                                                                                                                                |
| clientProperty.sale_status                                                | number | 1      | yes  | N/A      | 物件ステータス                  | 0=下書き、1=販売予告中、2=販売中、3=成約済、4=掲載止                                                                                                      |
| clientProperty.ad_comment                                                 | string | 1000   | no   | N/A      | 予告広告補足コメント            | 任意の文字列                                                                                                                                              |
| clientProperty.name                                                       | string | 100    | yes  | N/A      | 物件名                          | 任意の文字列                                                                                                                                              |
| clientProperty.sale_term                                                  | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.sale_housiki                                               | number | 1      | no   | N/A      | 販売方式                        | 最低限機能verの場合は **0** 固定<br>（0=未定、1=先着順、2=登録抽選）                                                                                      |
| clientProperty.sale_start                                                 | number | 1      | no   | N/A      | 販売開始方式区分                | 1=年月、2=年月日                                                                                                                                          |
| clientProperty.sale_start_year                                            | number | 4      | no   | N/A      | 販売開始年                      | 4桁の数値（ex.2019）                                                                                                                                      |
| clientProperty.sale_start_month                                           | number | 2      | no   | N/A      | 販売開始月                      | 1 - 12                                                                                                                                                    |
| clientProperty.sale_start_timing                                          | number | 1      | no   | N/A      | 販売開始旬                      | 1=初旬、2=上旬、3=中旬、4=下旬、5=末、0=未選択                                                                                                            |
| clientProperty.sentyaku_year_month_day                                    | string | 10     | no   | N/A      | 販売開始年月日参照表示欄        | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.sentyaku_start                                             | number | 1      | no   | N/A      | 先約開始方式区分                | 1=年月、2=年月日                                                                                                                                          |
| clientProperty.sentyaku_start_year                                        | number | 4      | no   | N/A      | 先約開始年                      | 4桁の数値（ex.2019）                                                                                                                                      |
| clientProperty.sentyaku_start_month                                       | number | 2      | no   | N/A      | 先約開始月                      | 1 - 12                                                                                                                                                    |
| clientProperty.sentyaku_start_part                                        | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.sentyaku_start_year_month_day                              | string | 10     | no   | N/A      | ???                             | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.regist_type                                                | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.regist_year                                                | number | 4      | no   | N/A      | ???                             | 4桁の数値（ex.2019）                                                                                                                                      |
| clientProperty.regist_month                                               | number | 2      | no   | N/A      | ???                             | 1 - 12                                                                                                                                                    |
| clientProperty.regist_part                                                | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.regist_start_year_month_day                                | string | 10     | no   | N/A      | ???                             | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.regist_end_year_month_day                                  | string | 10     | no   | N/A      | ???                             | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.tyuusen_type                                               | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyuusen_year                                               | number | 4      | no   | N/A      | ???                             | 4桁の数値（ex.2019）                                                                                                                                      |
| clientProperty.tyuusen_month                                              | number | 2      | no   | N/A      | ???                             | 1 - 12                                                                                                                                                    |
| clientProperty.tyuusen_part                                               | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyuusen_year_month_day                                     | string | 10     | no   | N/A      | ???                             | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.sale_schedule                                              | string | ???    | no   | N/A      | 販売スケジュール                | 任意の文字列                                                                                                                                              |
| clientProperty.sale_schedule                                              | string | 1000   | no   | N/A      | 販売スケジュールコメント入力欄  | 任意の文字列                                                                                                                                              |
| clientProperty.postal_code_first                                          | string | 5      | no   | N/A      | 郵便区分番号                    | 半角数字（0始まり含む）                                                                                                                                   |
| clientProperty.postal_code_last                                           | string | 4      | no   | N/A      | 町域番号                        | 半角数字（0始まり含む）                                                                                                                                   |
| clientProperty.prefectures_list                                           | number | 2      | yes  | N/A      | 都道府県選択リスト              | 半角数字                                                                                                                                                  |
| clientProperty.city_list                                                  | number | 11     | yes  | N/A      | 市区郡選択リスト                | 数値                                                                                                                                                      |
| clientProperty.town_list                                                  | number | N/A    | yes  | N/A      | 町村郡選択リスト                | 数値                                                                                                                                                      |
| clientProperty.jityou_list                                                | string | 50     | no   | N/A      | 字丁選択リスト                  | 任意の文字列（ex.1丁目）                                                                                                                                  |
| clientProperty.tiban                                                      | string | 10     | no   | N/A      | 地番入力欄                      | 任意の文字列（ex.1番地）                                                                                                                                  |
| clientProperty.gouban                                                     | string | 10     | no   | N/A      | 号番入力欄                      | 任意の文字列（ex.7号）                                                                                                                                    |
| clientProperty.address_last                                               | string | 50     | no   | N/A      | 住所末尾入力欄                  | 任意の文字列（ex.青山外苑ビル9F）                                                                                                                         |
| clientProperty.street_num                                                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.latitude                                                   | number | 11     | yes  | N/A      | 緯度                            | 浮動小数点を含む数値                                                                                                                                      |
| clientProperty.longitude                                                  | number | 12     | yes  | N/A      | 経度                            | 浮動小数点を含む数値                                                                                                                                      |
| clientProperty.access                                                     | string | ???    | no   | N/A      | アクセス情報                    | 任意の文字列                                                                                                                                              |
| clientProperty.access_list                                                | object | N/A    | yes  | N/A      | アクセス情報リスト              | JSON object                                                                                                                                               |
| clientProperty.access_list.&lowast;                                       | object | N/A    | yes  | N/A      | アクセス情報リスト              | JSON object<br>"&lowast;"には `main / others_1 / others_2` が存在する                                                                                     |
| clientProperty.access_list.&lowast;.access                                | number | ???    | no   | N/A      | アクセス                        | ???                                                                                                                                                       |
| clientProperty.access_list.&lowast;.ensen_name                            | number | 10     | no   | N/A      | 路線参照表示欄                  | 半角数字                                                                                                                                                  |
| clientProperty.access_list.&lowast;.ensen_name_text                       | string | ???    | no   | N/A      | 路線参照表示欄テキスト          | 任意の文字列                                                                                                                                              |
| clientProperty.access_list.&lowast;.station_name                          | number | 10     | no   | N/A      | 駅参照表示欄                    | 半角数字                                                                                                                                                  |
| clientProperty.access_list.&lowast;.station_name_text                     | string | ???    | no   | N/A      | 駅参照表示欄テキスト            | 任意の文字列                                                                                                                                              |
| clientProperty.access_list.&lowast;.access_type                           | number | 1      | no   | N/A      | 交通手段区分                    | 1=徒歩、2=バス、3=車                                                                                                                                      |
| clientProperty.access_list.&lowast;.walk_time                             | number | 2      | no   | N/A      | 徒歩時間入力欄                  | 半角数字（単位：分）                                                                                                                                      |
| clientProperty.access_list.&lowast;.bus_time                              | number | 2      | no   | N/A      | バス乗車時間入力欄              | 半角数字（単位：分）                                                                                                                                      |
| clientProperty.access_list.&lowast;.getoff_busstop                        | string | 25     | no   | N/A      | 下車バス停入力欄                | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| clientProperty.access_list.&lowast;.getoff_busstop_walk_time              | number | 2      | no   | N/A      | 下車バス停_徒歩時間入力         | 半角数字（単位：分）                                                                                                                                      |
| clientProperty.access_list.&lowast;.running_distance_1                    | number | 5      | no   | N/A      | 走行距離1                       | 半角数字（単位：km）                                                                                                                                      |
| clientProperty.access_list.&lowast;.running_distance_2                    | number | 2      | no   | N/A      | 走行距離2                       | 半角数字                                                                                                                                                  |
| clientProperty.access_list.&lowast;.bus_company                           | string | 50     | no   | N/A      | バス会社入力欄                  | 任意の文字列（ex.東急バス）                                                                                                                               |
| clientProperty.access_list.&lowast;.busstop                               | string | 50     | no   | N/A      | バス停入力欄                    | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| clientProperty.kukaku[]                                                   | array  | N/A    | no   | N/A      | 区画                            | JSON array                                                                                                                                                |
| clientProperty.kukaku[].updated_at                                        | string | 24     | ???  | N/A      | 更新日時                        | UTC                                                                                                                                                       |
| clientProperty.kukaku[].update_user                                       | string | ???    | no   | N/A      | 更新者                          | 任意の文字列                                                                                                                                              |
| clientProperty.kukaku[].created_at                                        | string | 24     | ???  | N/A      | 作成日時                        | UTC                                                                                                                                                       |
| clientProperty.kukaku[].create_user                                       | string | ???    | no   | N/A      | 作成者                          | 任意の文字列                                                                                                                                              |
| clientProperty.kukaku[].delete_flg                                        | number | 1      | yes  | 0 固定   | 論理削除フラグ                  | 1:削除、0:未削除                                                                                                                                          |
| clientProperty.kukaku[].price_1                                           | number | 6      | yes  | N/A      | 価格入力欄1                     | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.kukaku[].room_num_1                                        | number | ???    | no   | N/A      | 部屋番号1                       | 半角数字                                                                                                                                                  |
| clientProperty.kukaku[].madori_type_1                                     | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].service_rule_1                                    | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].madori_image                                      | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].madori_comment                                    | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].whole_kukaku_image                                | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].caption                                           | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].kukaku_name                                       | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].kukaku_madori                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].land_area                                         | string | ???    | no   | N/A      | ???                             | "0.0ｍ²"形式                                                                                                                                              |
| clientProperty.kukaku[].madori_type_1_text                                | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].service_rule_1_text                               | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].price_1_text                                      | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].building_area                                     | string | ???    | no   | N/A      | ???                             | "0.0ｍ²"形式                                                                                                                                              |
| clientProperty.whole_kukaku_image                                         | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.caption                                                    | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku_name                                                | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.price_setting                                              | number | 1      | yes  | N/A      | 価格設定                        | 0=未定、1=予定・確定                                                                                                                                      |
| clientProperty.price_1                                                    | number | 6      | yes  | N/A      | 価格入力欄1                     | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.price_handle                                               | number | 1      | yes  | N/A      | 価格入力欄の取り扱い            | 1="～"、2="・"（中点）                                                                                                                                    |
| clientProperty.price_2                                                    | number | 6      | yes  | N/A      | 価格入力欄2                     | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.price_range_handle                                         | number | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.price_hosoku                                               | string | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.most_price_1                                               | number | 6      | yes  | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_2                                               | number | 6      | yes  | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_3                                               | number | 6      | yes  | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_4                                               | number | 6      | yes  | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_5                                               | number | 6      | yes  | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_hosoku                                          | string | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.most_house_num                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyoukaihi_type                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyoukaihi_presense                                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyoukaihi_price                                            | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyoukaihi_payment_type                                     | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fee_type                                            | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fee_frontend_type                                   | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fee_frontend_price                                  | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fixed_fee_type                                      | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fixed_fee_price                                     | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fixed_fee_payment_type                              | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_fee_type                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_frontend_fee_type                                 | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_frontend_fee_payment                              | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_fixed_fee_type                                    | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_fixed_fee_price                                   | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_fixed_fee_payment                                 | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_fee_type                                              | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_frontend_fee_type                                     | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_frontend_fee_payment                                  | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_fixed_fee_type                                        | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_fixed_fee_price                                       | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_fixed_fee_payment                                     | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_fee_type                                               | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_frontend_fee_type                                      | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_frontend_fee_payment                                   | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_fixed_fee_type                                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_type                                                   | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_name_1                                          | string | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_price_1                                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_payment_type_1                                  | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_name_2                                          | string | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_price_2                                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_payment_type_2                                  | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_note                                                | string | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.site_right_type                                            | number | 1      | yes  | N/A      | 敷地権利区分                    | 1=所有権のみ、2=借地権のみ、3=所有権・借地権混在                                                                                                          |
| clientProperty.leasehold_type                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.rent_type                                                  | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.rent_price                                                 | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.rent_payment_type                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.leasehold_term_type                                        | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.leasehold_term_year                                        | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.leasehold_term_month                                       | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.leasehold_term_rate                                        | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.premium_type                                               | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.premium_price                                              | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_type                                 | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_handle                               | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_min                                  | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_price_handle                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_max_handle                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_type                                               | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_charge_revision_time_type                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_charge_revision_time                               | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_charge_revision_price_type                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_transfer_sublease_type                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.consent_type                                               | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.consent_charge_type                                        | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.consenter_type                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_area_type                                             | number | 1      | yes  | N/A      | 土地面積区分                    | 1=登記、2=実測、0=未選択                                                                                                                                  |
| clientProperty.land_area_1                                                | number | 6      | yes  | N/A      | 土地面積数1入力欄               | 半角数字                                                                                                                                                  |
| clientProperty.land_area_under_decimal_1                                  | number | 2      | yes  | N/A      | 土地面積数（小数点以下）1入力欄 | 半角数字                                                                                                                                                  |
| clientProperty.land_area_handle                                           | number | 1      | yes  | N/A      | 土地面積数欄の取り扱い          | 1="～"、2="."（小数点）                                                                                                                                   |
| clientProperty.land_area_2                                                | number | 6      | yes  | N/A      | 土地面積数2入力欄               | 半角数字                                                                                                                                                  |
| clientProperty.land_area_under_decimal_2                                  | number | 2      | yes  | N/A      | 土地面積数（小数点以下）2入力欄 | 半角数字                                                                                                                                                  |
| clientProperty.land_area_others                                           | string | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.coverage_ratio_ratio_floor_area_ratio                      | string | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_type_1                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_1                                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_under_decimal_1                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_1                                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_under_decimal_1                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_type_2                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_2                                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_under_decimal_2                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_2                                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_under_decimal_2                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_type_3                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_3                                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_under_decimal_3                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_3                                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_under_decimal_3                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_type_4                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_4                                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_under_decimal_4                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_4                                           | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_under_decimal_4                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.remarks                                                    | string | ???    | yes  | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.shidoufutan_type                                           | number | 1      | no   | N/A      | 私道負担区分                    | 0=無、1=有、2=共有                                                                                                                                        |
| clientProperty.shidoumenseki                                              | number | 4      | no   | N/A      | 私道面積数入力欄                | 半角数字                                                                                                                                                  |
| clientProperty.shidoumenseki_decimal                                      | number | 2      | no   | N/A      | 私道面積数（小数点以下）入力欄  | 半角数字                                                                                                                                                  |
| clientProperty.share_ratio                                                | number | 5      | no   | N/A      | 持分比率入力欄                  | 半角数字（単位：％）                                                                                                                                      |
| clientProperty.share_ratio_under_decimal                                  | number | 2      | no   | N/A      | 持分比率（小数点以下）入力欄    | 半角数字                                                                                                                                                  |
| clientProperty.overall_ratio                                              | number | 9      | no   | N/A      | 全体比率入力欄                  | 半角数字                                                                                                                                                  |
| clientProperty.overall_ratio_under_decimal                                | number | 2      | no   | N/A      | 全体比率（小数点以下）入力欄    | 半角数字                                                                                                                                                  |
| clientProperty.building_area_type                                         | number | 1      | yes  | N/A      | 建物面積区分                    | 1=登記、2=実測、0=未選択                                                                                                                                  |
| clientProperty.building_area_1                                            | number | 4      | yes  | N/A      | 建物面積数1入力欄               | 半角数字（単位：㎡）                                                                                                                                      |
| clientProperty.building_area_under_decimal_1                              | number | 2      | no   | N/A      | 建物面積数（小数点以下）1入力欄 | 半角数字                                                                                                                                                  |
| clientProperty.building_area_handle                                       | number | 1      | yes  | N/A      | 建物面積数欄の取り扱い          | 1="～"、2="."（小数点）                                                                                                                                   |
| clientProperty.building_area_2                                            | number | 4      | yes  | N/A      | 建物面積数2入力欄               | 半角数字（単位：㎡）                                                                                                                                      |
| clientProperty.building_area_under_decimal_2                              | number | 2      | no   | N/A      | 建物面積数（小数点以下）2入力欄 | 半角数字                                                                                                                                                  |
| clientProperty.sale_house_num_type                                        | number | 1      | yes  | N/A      | 販売戸数設定                    | 0= 未定、1= 予定・確定                                                                                                                                    |
| clientProperty.sale_house_num                                             | number | 5      | yes  | N/A      | 販売戸数入力欄                  | 半角数字（単位：戸）                                                                                                                                      |
| clientProperty.total_house_num                                            | number | 5      | no   | N/A      | 総戸数入力欄                    | 半角数字（単位：戸）                                                                                                                                      |
| clientProperty.room_num_1                                                 | number | 2      | yes  | N/A      | 部屋数入力欄1                   | 半角数字：（単位：部屋）                                                                                                                                  |
| clientProperty.layout_type_1                                              | number | 2      | yes  | N/A      | 間取りタイプ設定1               | 0=選択してください、1=ワンルーム、2=K<br>3=DK、4=LDK、5=LK、6=KK、7=DKK、8=LKK<br>9=DDKK、10=LLKK、11=LDKK、12=LDDKK、13=LLDKK                            |
| clientProperty.service_rule_type_1                                        | number | 1      | no   | N/A      | サービスルール設定1             | 0=選択してください、1=+S、2=+2S、3=+3S                                                                                                                    |
| clientProperty.layout_handle                                              | number | 1      | no   | N/A      | 間取りの取り扱い                | 1="～"、2="."（小数点）                                                                                                                                   |
| clientProperty.room_num_2                                                 | number | 2      | no   | N/A      | 部屋数入力欄2                   | 半角数字：（単位：部屋）                                                                                                                                  |
| clientProperty.layout_type_2                                              | number | 2      | yes  | N/A      | 間取りタイプ設定2               | 0=選択してください、1=ワンルーム、2=K<br>3=DK、4=LDK、5=LK、6=KK、7=DKK、8=LKK<br>9=DDKK、10=LLKK、11=LDKK、12=LDDKK、13=LLDKK                            |
| clientProperty.service_rule_type_2                                        | number | 1      | no   | N/A      | サービスルール設定2             | 0=選択してください、1=+S、2=+2S、3=+3S                                                                                                                    |
| clientProperty.complete_time_type                                         | number | 1      | yes  | N/A      | 完成時期区分                    | 0= 未選択、1= 完成予定、2= 完成済、3= 契約後                                                                                                              |
| clientProperty.complete_time_select                                       | number | 1      | yes  | N/A      | 完成時期方式                    | 1= 年月、2= 年月日                                                                                                                                        |
| clientProperty.complete_year                                              | number | 4      | no   | N/A      | 完成年                          | 半角数字：（単位：年）                                                                                                                                    |
| clientProperty.complete_month                                             | number | 2      | no   | N/A      | 完成月                          | 半角数字：（単位：月）                                                                                                                                    |
| clientProperty.complete_decade                                            | number | 1      | no   | N/A      | 完成旬                          | 0= 未選択、1= 初旬、2= 上旬、3= 中旬、4= 下旬、5= 末                                                                                                      |
| clientProperty.comlete_year_month_day                                     | string | 10     | no   | N/A      | 完成年月日参照表示欄            | YYYY-MM-DD                                                                                                                                                |
| clientProperty.complete_after_contract                                    | number | 2      | no   | N/A      | 完成時期契約後入力欄            | 半角数字                                                                                                                                                  |
| clientProperty.entering_time_type                                         | number | 1      | yes  | N/A      | ???                             | 0= 未選択、1= 完成予定、2= 完成済、3= 契約後                                                                                                              |
| clientProperty.entering_time_select                                       | number | 1      | yes  | N/A      | ???                             | 1= 年月、2= 年月日                                                                                                                                        |
| clientProperty.entering_year                                              | number | 4      | no   | N/A      | ???                             | 半角数字：（単位：年）                                                                                                                                    |
| clientProperty.entering_month                                             | number | 2      | no   | N/A      | ???                             | 半角数字：（単位：月）                                                                                                                                    |
| clientProperty.entering_decade                                            | number | 1      | no   | N/A      | ???                             | 0= 未選択、1= 初旬、2= 上旬、3= 中旬、4= 下旬、5= 末                                                                                                      |
| clientProperty.entering_year_month_day                                    | string | 10     | no   | N/A      | ???                             | YYYY-MM-DD                                                                                                                                                |
| clientProperty.entering_after_contract                                    | number | 2      | no   | N/A      | ???                             | 半角数字                                                                                                                                                  |
| clientProperty.building_situation                                         | number | 1      | yes  | N/A      | 建物状況区分                    | 1= 新築、2= 未入居、3= 中古                                                                                                                               |
| clientProperty.main_structure_type                                        | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.main_structure_others                                      | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.part_structure_type                                        | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.part_structure_others                                      | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.method_type                                                | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.method_others                                              | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.above_ground_floors_num"                                   | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.under_ground_floors_num                                    | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.parking_type                                               | number | 1      | no   | N/A      | 駐車場区分                      | 1= 車庫、2= 地下車庫、3= カースペース<br>4= カーポート、5= 無、0= 未選択                                                                                  |
| clientProperty.construction_company                                       | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.structure_method_floor_num_others                          | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.building_certification_num                                 | string | ???    | no   | N/A      | ???                             | 第18UDI1W建05787号　平成30年8月27日                                                                                                                       |
| clientProperty.exterior_type                                              | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.exterior_reform_year                                       | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.exterior_reform_month                                      | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.exterior_reform_point_type                                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.exterior_reform_others                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_type                                              | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_year                                       | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_month                                      | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_point_type                                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_others                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_others_memo                                | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.sale_house_num_text                                        | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.complete_time                                              | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.2018年12月未選択）                                                                                                                       |
| clientProperty.landyouto                                                  | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.２種住居）                                                                                                                               |
| clientProperty.advertiser_company_trade_aspect_type                       | number | 2      | yes  | N/A      | 広告主（貴社）会社取引態様区分  | **See &lowast;1**                                                                                                                                         |
| clientProperty.trade_aspect_1                                             | string | 20     | no   | N/A      | 取引態様1入力欄                 | 任意の文字列                                                                                                                                              |
| clientProperty.mototsuki_traders_name                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.area_code                                                  | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.local_code                                                 | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.subscriber_num                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.person_in_charge                                           | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.advertiser_company_trade_aspect_type_2                     | number | 2      | no   | N/A      | 会社2取引態様区分               | **See &lowast;2**                                                                                                                                         |
| clientProperty.company_postalcode_2                                       | string | 5      | no   | N/A      | 会社2郵便区分番号入力欄         | 半角数字                                                                                                                                                  |
| clientProperty.company_area_num_2                                         | string | 4      | no   | N/A      | 会社2町域番号入力欄             | 半角数字                                                                                                                                                  |
| clientProperty.company_address_2                                          | string | 150    | no   | N/A      | 住所2入力欄                     | 任意の文字列                                                                                                                                              |
| clientProperty.position_group_name_2                                      | string | 200    | no   | N/A      | 所属団体名2入力欄               | 任意の文字列                                                                                                                                              |
| clientProperty.license_num_2                                              | string | 30     | no   | N/A      | 免許番号2入力欄                 | 任意の文字列                                                                                                                                              |
| clientProperty.company_name_2                                             | string | 150    | no   | N/A      | 社名2入力欄                     | 任意の文字列                                                                                                                                              |
| clientProperty.advertiser_company_trade_aspect_type_3                     | number | 2      | no   | N/A      | 会社3取引態様区分               | **See &lowast;2**                                                                                                                                         |
| clientProperty.company_postalcode_3                                       | string | 5      | no   | N/A      | 会社3郵便区分番号入力欄         | 半角数字                                                                                                                                                  |
| clientProperty.company_area_num_3                                         | string | 4      | no   | N/A      | 会社3町域番号入力欄             | 半角数字                                                                                                                                                  |
| clientProperty.company_address_3                                          | string | 150    | no   | N/A      | 住所3入力欄                     | 任意の文字列                                                                                                                                              |
| clientProperty.position_group_name_3                                      | string | 200    | no   | N/A      | 所属団体名3入力欄               | 任意の文字列                                                                                                                                              |
| clientProperty.license_num_3                                              | string | 30     | no   | N/A      | 免許番号3入力欄                 | 任意の文字列                                                                                                                                              |
| clientProperty.company_name_3                                             | string | 150    | no   | N/A      | 社名3入力欄                     | 任意の文字列                                                                                                                                              |
| clientProperty.statutory_limit_type_1                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.statutory_limit_type_2                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.statutory_limit_type_3                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.limitation_others_type_1                                   | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kijunhou_43zyou_1kou_acceptance_type                       | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.acceptance_reason                                          | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.limitation_others                                          | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.limit_memo                                                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.object_performance_select                                  | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.building_inspection_select                                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.house_hitory_select                                        | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.vacant_house_bank_select                                   | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.vacant_house_bank_select                                   | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.location_land_character_select                             | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.station_conveniene_selecct                                 | array  | N/A    | no   | N/A      | 駅利便性設定                    | number in array（複数可能）<br>1= 2沿線以上利用可、2= 検索駅まで平坦、3= 始発駅                                                                           |
| clientProperty.dwelling_unit_floors_num_select                            | array  | N/A    | no   | N/A      | 住戸・階数設定                  | number in array（複数可能）<br>1= 平屋、2= 2階建て、3= 3階建て以上（複数可能）                                                                            |
| clientProperty.lighting_ventication_select                                | array  | N/A    | no   | N/A      | 陽当り・採光・通風設定          | number in array（複数可能）<br>**See &lowast;3**                                                                                                          |
| clientProperty.character_madori_selecet                                   | array  | N/A    | no   | N/A      | 間取り設定                      | number in array（複数可能）<br>**See &lowast;4**                                                                                                          |
| clientProperty.character_inner_room_equipment_select                      | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.storing_select                                             | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kitchen_concerned_facilities_select                        | array  | N/A    | no   | N/A      | キッチン・関連設備設定          | number in array（複数可能）<br>**See &lowast;5**                                                                                                          |
| clientProperty.bathroom_select                                            | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.toilet_lavatory_concerned_equipment_select                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.balcony_terrace_select                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.garden_select                                              | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.eco_concerned_select                                       | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tv_communication_select                                    | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.parking_select                                             | array  | N/A    | no   | N/A      | 駐車・駐輪設定                  | number in array（複数可能）<br>1= 駐車2台可、2= 駐車3台以上可、3= ハイルーフ駐車場<br>4= EV車充電設備、5= シャッター車庫、6= ビルトインガレージ           |
| clientProperty.common_use_space_select                                    | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.management_security_select                                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.reform_renovation_select                                   | array  | N/A    | no   | N/A      | ﾘﾌｫｰﾑ・ﾘﾉﾍﾞｰｼｮﾝ設定             | number in array（複数可能）<br>1= 適合リノベーション、2= 内外装リフォーム、3= 内装リフォーム<br>4= 外装リフォーム、5= フローリング張替、6= リノベーション |
| clientProperty.surroundings_select                                        | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.view_natural_environments_select                           | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.expense_delivery_entering_condition_select                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.charcter_others_selcet                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.character                                                  | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.property_details_appeal                                    | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.event_category_type                                        | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.event_details_summary                                      | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.inner                                                      | object | N/A    | yes  | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.inner.&lowast;                                             | object | N/A    | yes  | N/A      | ???                             | JSON object<br>&lowast;は`main_inner,sub_inner_2 - sub_inner_20`まで存在する                                                                              |
| clientProperty.inner.&lowast;.inner_image                                 | string | ???    | no   | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.inner.&lowast;.inner_category                              | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.inner.&lowast;.inner_category_text                         | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.inner.&lowast;.inner_image_caption                         | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.outer                                                      | object | N/A    | yes  | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.outer.&lowast;                                             | object | N/A    | yes  | N/A      | ???                             | JSON object<br>&lowast;は`main_outer,sub_outer_2 - sub_outer_20`まで存在する                                                                              |
| clientProperty.outer.&lowast;.outer_image                                 | string | ???    | no   | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.outer.&lowast;.outer_category                              | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.outer.&lowast;.outer_category_text                         | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.outer.&lowast;.outer_image_caption                         | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info                                          | object | N/A    | yes  | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.surroundings_info.&lowast;                                 | object | N/A    | yes  | N/A      | ???                             | JSON object<br>&lowast;は`surroundings_info_1 - surroundings_info_20`まで存在する                                                                         |
| clientProperty.surroundings_info.&lowast;.surroundings_info_image         | string | ???    | no   | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.surroundings_info.&lowast;.surroundings_info_category      | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info.&lowast;.surroundings_info_category_text | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info.&lowast;.surroundings_info_facility_name | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info.&lowast;.surroundings_info_distance      | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info.&lowast;.surroundings_info_caption       | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment                                                  | object | N/A    | yes  | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.equipment.&lowast;                                         | object | N/A    | no   | N/A      | ???                             | JSON object<br>&lowast;は`equipment_1 - equipment_10`まで存在する                                                                                         |
| clientProperty.equipment.&lowast;.equipment_image                         | string | N/A    | no   | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.equipment.&lowast;.equipment_category                      | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.equipment_category_text                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.equipment_image_name                    | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.equipment_image_caption                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kouzou                                                     | object | N/A    | yes  | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.kouzou.&lowast;                                            | object | N/A    | yes  | N/A      | ???                             | JSON object<br>&lowast;は`kouzou_1 - kouzou_10`まで存在する                                                                                               |
| clientProperty.equipment.&lowast;.kouzou_image                            | string | N/A    | no   | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.equipment.&lowast;.kouzou_category                         | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.kouzou_category_text                    | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.kouzou_image_name                       | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.kouzou_image_caption                    | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internal_memo                                              | string | 2000   | no   | N/A      | 社内メモ入力欄                  | 任意の文字列                                                                                                                                              |
| clientProperty.handled_store_name                                         | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.IM00022）                                                                                                                                |
| clientProperty.posting_priority_value                                     | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.jimukyoku_memo                                             | string | 2000   | no   | N/A      | いえまるこ事務局メモ入力欄      | 任意の文字列                                                                                                                                              |
| clientProperty.rakuten_tel                                                | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.unvisible_flag                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.release_date_pre                                           | string | 10     | no   | N/A      | ???                             | YYYY-MM-DD                                                                                                                                                |
| clientProperty.release_date_now                                           | string | 10     | no   | N/A      | ???                             | YYYY-MM-DD                                                                                                                                                |
| clientProperty.new_label                                                  | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.新着）                                                                                                                                   |
| clientProperty.status_label                                               | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.新築一戸建て）                                                                                                                           |
| clientProperty.price                                                      | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.2,480万円）                                                                                                                              |
| clientProperty.price_text                                                 | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.2,480万円\r\n）                                                                                                                          |
| clientProperty.most_prices_text                                           | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_area_type_text                                        | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.building_area_type_text                                    | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_area_text                                             | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.226㎡）                                                                                                                                  |
| clientProperty.building_area_text                                         | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.108㎡）                                                                                                                                  |
| clientProperty.leasehold_type_text                                        | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.parking_type_text                                          | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.shidoufutan_text                                           | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kenpeiritsu_text                                           | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yosekiritsu_text                                           | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.sales_number_text                                          | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.advertiser_company_trade_aspect_type_text_1                | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.advertiser_company_trade_aspect_type_text_2                | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.advertiser_company_trade_aspect_type_text_3                | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.madori_all                                                 | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.madori                                                     | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_area                                                  | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.226.64ｍ²）                                                                                                                              |
| clientProperty.building_area                                              | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.108.74ｍ²）                                                                                                                              |
| clientProperty.address                                                    | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.栃木県宇都宮市白沢町）                                                                                                                   |
| clientProperty.offer_date                                                 | string | 11     | no   | N/A      | ???                             | YYYY年YY月DD日                                                                                                                                            |
| clientProperty.site_right_type_text                                       | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.entering_time                                              | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.kouzou_kouhou                                              | string | ???    | no   | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| storeOverview                                                             | object | N/A    | yes  | N/A      | ???                             | JSON object                                                                                                                                               |
| storeOverview.id                                                          | string | 7      | ???  | N/A      | ???                             | "IM" + 半角数字5桁                                                                                                                                        |
| storeOverview.category                                                    | number | ???    | ???  | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.status                                                      | number | ???    | ???  | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.name                                                        | string | ???    | ???  | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.comment                                                     | string | ???    | ???  | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.postal_code                                                 | string | 7      | ???  | N/A      | 郵便番号                        | ???                                                                                                                                                       |
| storeOverview.address                                                     | string | ???    | ???  | N/A      | 住所                            | 任意の文字列（ex.栃木県宇都宮市元今泉7-3-11）                                                                                                             |
| storeOverview.building                                                    | string | ???    | ???  | N/A      | ビル名                          | 任意の文字列                                                                                                                                              |
| storeOverview.latitude                                                    | number | 11     | yes  | N/A      | 緯度                            | 浮動小数点を含む数値                                                                                                                                      |
| storeOverview.longitude                                                   | number | 12     | yes  | N/A      | 経度                            | 浮動小数点を含む数値                                                                                                                                      |
| storeOverview.main_access                                                 | number | 1      | yes  | N/A      | 主要交通区分                    | 1=電車、2=バス                                                                                                                                            |
| storeOverview.ensen_name                                                  | number | 10     | no   | N/A      | 路線参照表示欄                  | 半角数字                                                                                                                                                  |
| storeOverview.ensen_name_text                                             | string | ???    | no   | N/A      | 路線参照表示欄テキスト          | 任意の文字列                                                                                                                                              |
| storeOverview.station_name                                                | number | 10     | no   | N/A      | 駅参照表示欄                    | 半角数字                                                                                                                                                  |
| storeOverview.station_name_text                                           | string | ???    | no   | N/A      | 駅参照表示欄テキスト            | 任意の文字列                                                                                                                                              |
| storeOverview.access_type                                                 | number | 1      | no   | N/A      | 交通手段区分                    | 1=徒歩、2=バス、3=車                                                                                                                                      |
| storeOverview.walk_time                                                   | number | 2      | no   | N/A      | 徒歩時間入力欄                  | 半角数字（単位：分）                                                                                                                                      |
| storeOverview.bus_time                                                    | number | 2      | no   | N/A      | バス乗車時間入力欄              | 半角数字（単位：分）                                                                                                                                      |
| storeOverview.getoff_busstop                                              | string | 25     | no   | N/A      | 下車バス停入力欄                | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| storeOverview.getoff_busstop_walk_time                                    | number | 2      | no   | N/A      | 下車バス停_徒歩時間入力         | 半角数字（単位：分）                                                                                                                                      |
| storeOverview.running_distance_1                                          | number | 5      | no   | N/A      | 走行距離1                       | 半角数字（単位：km）                                                                                                                                      |
| storeOverview.running_distance_2                                          | number | 2      | no   | N/A      | 走行距離2                       | 半角数字                                                                                                                                                  |
| storeOverview.bus_company                                                 | string | 50     | no   | N/A      | バス会社入力欄                  | 任意の文字列（ex.東急バス）                                                                                                                               |
| storeOverview.busstop                                                     | string | 50     | no   | N/A      | バス停入力欄                    | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| storeOverview.start_business_hours                                        | string | 5      | no   | N/A      | 営業開始時間                    | HH:MM                                                                                                                                                     |
| storeOverview.end_business_hours                                          | string | 5      | no   | N/A      | 営業終了時間                    | HH:MM                                                                                                                                                     |
| storeOverview.businesshours_memo                                          | string | ???    | no   | N/A      | 営業時間メモ                    | ???                                                                                                                                                       |
| storeOverview.regular_holiday                                             | string | 3      | no   | N/A      | 定休日                          | 月曜日 - 日曜日                                                                                                                                           |
| storeOverview.character                                                   | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.property_type                                               | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.realestate_registration_number                              | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.国土交通大臣（１）第9280号）                                                                                                             |
| storeOverview.tel                                                         | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.rakuten_tel                                                 | ???    | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.inquiry_mail_admin                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.inquiry_mail_staff                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.inquiry_mail_other                                          | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.open_mail_admin                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.open_mail_staff                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.open_mail_other                                             | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.memo                                                        | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.score                                                       | number | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.provider_memo                                               | string | ???    | no   | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.near_station                                                | string | ???    | no   | N/A      | ???                             | 任意の文字列（ex.JR東北本線「宇都宮駅」徒歩19分）                                                                                                         |

- **&lowast;1**：advertiser_company_trade_aspect_type

| Value | Meaning                  |
|:------|:-------------------------|
| 0     | 選択してください         |
| 1     | 売主                     |
| 2     | 建物売主                 |
| 3     | 土地売主                 |
| 4     | 土地貸主                 |
| 5     | 土地転貸主               |
| 6     | 販売提携(代理)           |
| 7     | 販売提携(媒介)           |
| 8     | 販売提携(復代理)         |
| 9     | 仲介(一般媒介)           |
| 10    | 仲介(専任媒介)           |
| 11    | 仲介(専属専任)           |
| 12    | 先物                     |
| 13    | 事業主・売主             |
| 14    | 事業主・建物売主         |
| 15    | 事業主・土地売主         |
| 16    | 事業主・販売提携（代理） |
| 17    | 事業主・販売提携（媒介） |

- **&lowast;2**：advertiser_company_trade_aspect_type_2 / advertiser_company_trade_aspect_type_3

| Value | Meaning          |
|:------|:-----------------|
| 0     | 選択してください |
| 1     | 売主             |
| 2     | 建物売主         |
| 3     | 土地売主         |
| 4     | 土地貸主         |
| 5     | 土地転貸主       |
| 6     | 販売提携(代理)   |
| 7     | 販売提携(復代理) |
| 8     | 販売提携(媒介)   |
| 9     | 仲介             |

- **&lowast;3**：lighting_ventication_select

| Value | Meaning      |
|:------|:-------------|
| 1     | 南向き       |
| 2     | 陽当り良好   |
| 3     | 3面採光      |
| 4     | 東南向き     |
| 5     | 全室南向き   |
| 6     | 通風良好     |
| 7     | 南西向き     |
| 8     | 全室2面採光  |
| 9     | 全室南西向き |
| 10    | 全室東南向き |

- **&lowast;4**：character_madori_selecet

| Value | Meaning       |
|:------|:--------------|
| 0     | LDK20畳以上   |
| 1     | LDK18畳以上   |
| 2     | LDK15畳以上   |
| 3     | 和室          |
| 4     | ロフト        |
| 5     | 吹抜け        |
| 6     | 全居室6畳以上 |
| 7     | 2世帯住宅     |
| 8     | 可動間仕切り  |

- **&lowast;**5：kitchen_concerned_facilities_select

| Value | Meaning                            |
|:------|:-----------------------------------|
| 1     | システムキッチン                   |
| 2     | 対面式キッチン                     |
| 3     | アイランドキッチン                 |
| 4     | パントリー（食器・食品の収納庫）   |
| 5     | IHクッキングヒーター               |
| 6     | 食器洗乾燥機                       |
| 7     | ディスポーザー（生ごみ粉砕処理器） |
| 8     | 浄水器                             |

- **&lowast;**6：main_inner_image_category_1

| Value | Meaning                |
|:------|:-----------------------|
| 0     | 選択してください       |
| 1     | リビング               |
| 2     | リビング以外の居室     |
| 3     | 洗面台・洗面所         |
| 4     | キッチン               |
| 5     | 収納                   |
| 6     | 浴室                   |
| 7     | トイレ                 |
| 8     | バルコニー             |
| 9     | 庭                     |
| 10    | 玄関                   |
| 11    | その他内観             |
| 12    | 同仕様写真(リビング）  |
| 13    | 同仕様写真(キッチン）  |
| 14    | 同仕様写真(浴室）      |
| 15    | 同仕様写真(その他内観) |
| 16    | 完成予想図(内観)       |
| 17    | モデルハウス写真       |
| 18    | 展示場/ショウルーム    |
| 19    | その他                 |

**Response failure:**

| HTTP Status | Title                        |
|:------------|:-----------------------------|
| 400         | BadRequestException          |
| 401         | UnauthorizedException        |
| 403         | ForbiddenException           |
| 500         | InternalServerErrorException |

| KEY                                                                       | FORMAT | SIZE | MANDATORY |          |                                 |                                                                                                                                                           |
|---------------------------------------------------------------------------|--------|------|-----------|----------|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| clientProperty                                                            | object | N/A  | yes       | N/A      | 加盟店物件情報                  | JSON object                                                                                                                                               |
| clientProperty.id                                                         | string | 13   | yes       | 完全一致 | 加盟店物件ID                    | "cl_" + 半角10桁の数値                                                                                                                                    |
| clientProperty.delete_flg                                                 | number | 1    | yes       | 0 固定   | 論理削除フラグ                  | 1:削除、0:未削除                                                                                                                                          |
| clientProperty.updated_at                                                 | string | 24   | no        | N/A      | 更新日時                        | UTC                                                                                                                                                       |
| clientProperty.update_user                                                | ???    | ???  | no        | N/A      | 更新者                          | 任意の文字列                                                                                                                                              |
| clientProperty.created_at                                                 | string | 24   | no        | N/A      | 作成日時                        | UTC                                                                                                                                                       |
| clientProperty.create_user                                                | ???    | ???  | no        | N/A      | 作成者                          | 任意の文字列                                                                                                                                              |
| clientProperty.store_id                                                   | string | 7    | yes       | N/A      | 店舗ID                          | 半角英数記号                                                                                                                                              |
| clientProperty.type                                                       | number | 1    | yes       | N/A      | 物件種別                        | 1=一戸建て、2=二戸建て以上                                                                                                                                |
| clientProperty.sale_status                                                | number | 1    | yes       | N/A      | 物件ステータス                  | 0=下書き、1=販売予告中、2=販売中、3=成約済、4=掲載止                                                                                                      |
| clientProperty.ad_comment                                                 | string | 1000 | no        | N/A      | 予告広告補足コメント            | 任意の文字列                                                                                                                                              |
| clientProperty.name                                                       | string | 100  | yes       | N/A      | 物件名                          | 任意の文字列                                                                                                                                              |
| clientProperty.sale_term                                                  | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.sale_housiki                                               | number | 1    | no        | N/A      | 販売方式                        | 最低限機能verの場合は **0** 固定<br>（0=未定、1=先着順、2=登録抽選）                                                                                      |
| clientProperty.sale_start                                                 | number | 1    | no        | N/A      | 販売開始方式区分                | 1=年月、2=年月日                                                                                                                                          |
| clientProperty.sale_start_year                                            | number | 4    | no        | N/A      | 販売開始年                      | 4桁の数値（ex.2019）                                                                                                                                      |
| clientProperty.sale_start_month                                           | number | 2    | no        | N/A      | 販売開始月                      | 1 - 12                                                                                                                                                    |
| clientProperty.sale_start_timing                                          | number | 1    | no        | N/A      | 販売開始旬                      | 1=初旬、2=上旬、3=中旬、4=下旬、5=末、0=未選択                                                                                                            |
| clientProperty.sentyaku_year_month_day                                    | string | 10   | no        | N/A      | 販売開始年月日参照表示欄        | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.sentyaku_start                                             | number | 1    | no        | N/A      | 先約開始方式区分                | 1=年月、2=年月日                                                                                                                                          |
| clientProperty.sentyaku_start_year                                        | number | 4    | no        | N/A      | 先約開始年                      | 4桁の数値（ex.2019）                                                                                                                                      |
| clientProperty.sentyaku_start_month                                       | number | 2    | no        | N/A      | 先約開始月                      | 1 - 12                                                                                                                                                    |
| clientProperty.sentyaku_start_part                                        | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.sentyaku_start_year_month_day                              | string | 10   | no        | N/A      | ???                             | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.regist_type                                                | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.regist_year                                                | number | 4    | no        | N/A      | ???                             | 4桁の数値（ex.2019）                                                                                                                                      |
| clientProperty.regist_month                                               | number | 2    | no        | N/A      | ???                             | 1 - 12                                                                                                                                                    |
| clientProperty.regist_part                                                | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.regist_start_year_month_day                                | string | 10   | no        | N/A      | ???                             | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.regist_end_year_month_day                                  | string | 10   | no        | N/A      | ???                             | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.tyuusen_type                                               | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyuusen_year                                               | number | 4    | no        | N/A      | ???                             | 4桁の数値（ex.2019）                                                                                                                                      |
| clientProperty.tyuusen_month                                              | number | 2    | no        | N/A      | ???                             | 1 - 12                                                                                                                                                    |
| clientProperty.tyuusen_part                                               | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyuusen_year_month_day                                     | string | 10   | no        | N/A      | ???                             | 数値 + ハイフン（ex.2019-01-01）                                                                                                                          |
| clientProperty.sale_schedule                                              | string | N/A  | no        | N/A      | 販売スケジュール                | 任意の文字列                                                                                                                                              |
| clientProperty.sale_schedule                                              | string | 1000 | no        | N/A      | 販売スケジュールコメント入力欄  | 任意の文字列                                                                                                                                              |
| clientProperty.postal_code_first                                          | string | 5    | no        | N/A      | 郵便区分番号                    | 半角数字（0始まり含む）                                                                                                                                   |
| clientProperty.postal_code_last                                           | string | 4    | no        | N/A      | 町域番号                        | 半角数字（0始まり含む）                                                                                                                                   |
| clientProperty.prefectures_list                                           | number | 2    | yes       | N/A      | 都道府県選択リスト              | 半角数字                                                                                                                                                  |
| clientProperty.city_list                                                  | number | 11   | yes       | N/A      | 市区郡選択リスト                | 数値                                                                                                                                                      |
| clientProperty.town_list                                                  | number | N/A  | yes       | N/A      | 町村郡選択リスト                | 数値                                                                                                                                                      |
| clientProperty.jityou_list                                                | string | 50   | no        | N/A      | 字丁選択リスト                  | 任意の文字列（ex.1丁目）                                                                                                                                  |
| clientProperty.tiban                                                      | string | 10   | no        | N/A      | 地番入力欄                      | 任意の文字列（ex.1番地）                                                                                                                                  |
| clientProperty.gouban                                                     | string | 10   | no        | N/A      | 号番入力欄                      | 任意の文字列（ex.7号）                                                                                                                                    |
| clientProperty.address_last                                               | string | 50   | no        | N/A      | 住所末尾入力欄                  | 任意の文字列（ex.青山外苑ビル9F）                                                                                                                         |
| clientProperty.street_num                                                 | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.latitude                                                   | number | 11   | yes       | N/A      | 緯度                            | 浮動小数点を含む数値                                                                                                                                      |
| clientProperty.longitude                                                  | number | 12   | yes       | N/A      | 経度                            | 浮動小数点を含む数値                                                                                                                                      |
| clientProperty.access                                                     | string | N/A  | no        | N/A      | アクセス情報                    | 任意の文字列                                                                                                                                              |
| clientProperty.access_list                                                | object | N/A  | yes       | N/A      | アクセス情報リスト              | JSON object                                                                                                                                               |
| clientProperty.access_list.&lowast;                                       | object | N/A  | yes       | N/A      | アクセス情報リスト              | JSON object<br>"&lowast;"には `main / others_1 / others_2` が存在する                                                                                     |
| clientProperty.access_list.&lowast;.access                                | number | ???  | no        | N/A      | アクセス                        | ???                                                                                                                                                       |
| clientProperty.access_list.&lowast;.ensen_name                            | number | 10   | no        | N/A      | 路線参照表示欄                  | 半角数字                                                                                                                                                  |
| clientProperty.access_list.&lowast;.ensen_name_text                       | string | 11   | no        | N/A      | 路線参照表示欄テキスト          | 任意の文字列                                                                                                                                              |
| clientProperty.access_list.&lowast;.station_name                          | number | 10   | no        | N/A      | 駅参照表示欄                    | 半角数字                                                                                                                                                  |
| clientProperty.access_list.&lowast;.station_name_text                     | string | 11   | no        | N/A      | 駅参照表示欄テキスト            | 任意の文字列                                                                                                                                              |
| clientProperty.access_list.&lowast;.access_type                           | number | 1    | no        | N/A      | 交通手段区分                    | 1=徒歩、2=バス、3=車                                                                                                                                      |
| clientProperty.access_list.&lowast;.walk_time                             | number | 2    | no        | N/A      | 徒歩時間入力欄                  | 半角数字（単位：分）                                                                                                                                      |
| clientProperty.access_list.&lowast;.bus_time                              | number | 2    | no        | N/A      | バス乗車時間入力欄              | 半角数字（単位：分）                                                                                                                                      |
| clientProperty.access_list.&lowast;.getoff_busstop                        | string | 25   | no        | N/A      | 下車バス停入力欄                | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| clientProperty.access_list.&lowast;.getoff_busstop_walk_time              | number | 2    | no        | N/A      | 下車バス停_徒歩時間入力         | 半角数字（単位：分）                                                                                                                                      |
| clientProperty.access_list.&lowast;.running_distance_1                    | number | 5    | no        | N/A      | 走行距離1                       | 半角数字（単位：km）                                                                                                                                      |
| clientProperty.access_list.&lowast;.running_distance_2                    | number | 2    | no        | N/A      | 走行距離2                       | 半角数字                                                                                                                                                  |
| clientProperty.access_list.&lowast;.bus_company                           | string | 50   | no        | N/A      | バス会社入力欄                  | 任意の文字列（ex.東急バス）                                                                                                                               |
| clientProperty.access_list.&lowast;.busstop                               | string | 50   | no        | N/A      | バス停入力欄                    | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| clientProperty.kukaku[]                                                   | array  | N/A  | no        | N/A      | 区画                            | JSON array                                                                                                                                                |
| clientProperty.kukaku[].updated_at                                        | string | 24   | no        | N/A      | 更新日時                        | UTC                                                                                                                                                       |
| clientProperty.kukaku[].update_user                                       | string | 20   | no        | N/A      | 更新者                          | 任意の文字列                                                                                                                                              |
| clientProperty.kukaku[].created_at                                        | string | 24   | no        | N/A      | 作成日時                        | UTC                                                                                                                                                       |
| clientProperty.kukaku[].create_user                                       | string | 20   | no        | N/A      | 作成者                          | 任意の文字列                                                                                                                                              |
| clientProperty.kukaku[].delete_flg                                        | number | 1    | yes       | 0 固定   | 論理削除フラグ                  | 1:削除、0:未削除                                                                                                                                          |
| clientProperty.kukaku[].price_1                                           | number | 6    | yes       | N/A      | 価格入力欄1                     | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.kukaku[].room_num_1                                        | number | 11   | no        | N/A      | 部屋番号1                       | 半角数字                                                                                                                                                  |
| clientProperty.kukaku[].madori_type_1                                     | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].service_rule_1                                    | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].madori_image                                      | string | 255  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].madori_comment                                    | string | 150  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].whole_kukaku_image                                | string | 255  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].caption                                           | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].kukaku_name                                       | string | 25   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].kukaku_madori                                     | string | ???  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].land_area                                         | string | 11   | no        | N/A      | ???                             | "0.0ｍ²"形式                                                                                                                                              |
| clientProperty.kukaku[].madori_type_1_text                                | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].service_rule_1_text                               | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].price_1_text                                      | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku[].building_area                                     | string | 11   | no        | N/A      | ???                             | "0.0ｍ²"形式                                                                                                                                              |
| clientProperty.whole_kukaku_image                                         | string | 255  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.caption                                                    | string | ???  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kukaku_name                                                | string | 25   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.price_setting                                              | number | 1    | yes       | N/A      | 価格設定                        | 0=未定、1=予定・確定                                                                                                                                      |
| clientProperty.price_1                                                    | number | 6    | yes       | N/A      | 価格入力欄1                     | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.price_handle                                               | number | 1    | yes       | N/A      | 価格入力欄の取り扱い            | 1="～"、2="・"（中点）                                                                                                                                    |
| clientProperty.price_2                                                    | number | 6    | yes       | N/A      | 価格入力欄2                     | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.price_range_handle                                         | number | 11   | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.price_hosoku                                               | string | N/A  | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.most_price_1                                               | number | 6    | yes       | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_2                                               | number | 6    | yes       | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_3                                               | number | 6    | yes       | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_4                                               | number | 6    | yes       | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_5                                               | number | 6    | yes       | N/A      | ???                             | 半角数字（単位：JPY）                                                                                                                                     |
| clientProperty.most_price_hosoku                                          | string | 50   | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.most_house_num                                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyoukaihi_type                                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyoukaihi_presense                                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyoukaihi_price                                            | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tyoukaihi_payment_type                                     | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fee_type                                            | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fee_frontend_type                                   | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fee_frontend_price                                  | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fixed_fee_type                                      | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fixed_fee_price                                     | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yuusen_fixed_fee_payment_type                              | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_fee_type                                          | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_frontend_fee_type                                 | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_frontend_fee_payment                              | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_fixed_fee_type                                    | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_fixed_fee_price                                   | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internet_fixed_fee_payment                                 | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_fee_type                                              | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_frontend_fee_type                                     | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_frontend_fee_payment                                  | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_fixed_fee_type                                        | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_fixed_fee_price                                       | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.catv_fixed_fee_payment                                     | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_fee_type                                               | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_frontend_fee_type                                      | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_frontend_fee_payment                                   | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_fixed_fee_type                                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.spa_type                                                   | number | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_name_1                                          | string | 50   | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_price_1                                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_payment_type_1                                  | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_name_2                                          | string | 50   | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_price_2                                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_fee_payment_type_2                                  | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.others_note                                                | string | N/A  | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.site_right_type                                            | number | 1    | yes       | N/A      | 敷地権利区分                    | 1=所有権のみ、2=借地権のみ、3=所有権・借地権混在                                                                                                          |
| clientProperty.leasehold_type                                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.rent_type                                                  | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.rent_price                                                 | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.rent_payment_type                                          | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.leasehold_term_type                                        | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.leasehold_term_year                                        | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.leasehold_term_month                                       | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.leasehold_term_rate                                        | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.premium_type                                               | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.premium_price                                              | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_type                                 | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_handle                               | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_min                                  | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_price_handle                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_security_deposit_max_handle                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_type                                               | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_charge_revision_time_type                          | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_charge_revision_time                               | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_charge_revision_price_type                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tenancy_transfer_sublease_type                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.consent_type                                               | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.consent_charge_type                                        | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.consenter_type                                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_area_type                                             | number | 1    | yes       | N/A      | 土地面積区分                    | 1=登記、2=実測、0=未選択                                                                                                                                  |
| clientProperty.land_area_1                                                | number | 6    | yes       | N/A      | 土地面積数1入力欄               | 半角数字                                                                                                                                                  |
| clientProperty.land_area_under_decimal_1                                  | number | 2    | yes       | N/A      | 土地面積数（小数点以下）1入力欄 | 半角数字                                                                                                                                                  |
| clientProperty.land_area_handle                                           | number | 1    | yes       | N/A      | 土地面積数欄の取り扱い          | 1="～"、2="."（小数点）                                                                                                                                   |
| clientProperty.land_area_2                                                | number | 6    | yes       | N/A      | 土地面積数2入力欄               | 半角数字                                                                                                                                                  |
| clientProperty.land_area_under_decimal_2                                  | number | 2    | yes       | N/A      | 土地面積数（小数点以下）2入力欄 | 半角数字                                                                                                                                                  |
| clientProperty.land_area_others                                           | string | 200  | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.coverage_ratio_ratio_floor_area_ratio                      | string | 100  | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_type_1                                          | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_1                                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_under_decimal_1                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_1                                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_under_decimal_1                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_type_2                                          | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_2                                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_under_decimal_2                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_2                                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_under_decimal_2                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_type_3                                          | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_3                                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_under_decimal_3                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_3                                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_under_decimal_3                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_type_4                                          | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_4                                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.front_road_width_under_decimal_4                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_4                                           | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.frontage_width_under_decimal_4                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.remarks                                                    | string | 200  | yes       | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.shidoufutan_type                                           | number | 1    | no        | N/A      | 私道負担区分                    | 0=無、1=有、2=共有                                                                                                                                        |
| clientProperty.shidoumenseki                                              | number | 4    | no        | N/A      | 私道面積数入力欄                | 半角数字                                                                                                                                                  |
| clientProperty.shidoumenseki_decimal                                      | number | 2    | no        | N/A      | 私道面積数（小数点以下）入力欄  | 半角数字                                                                                                                                                  |
| clientProperty.share_ratio                                                | number | 5    | no        | N/A      | 持分比率入力欄                  | 半角数字（単位：％）                                                                                                                                      |
| clientProperty.share_ratio_under_decimal                                  | number | 2    | no        | N/A      | 持分比率（小数点以下）入力欄    | 半角数字                                                                                                                                                  |
| clientProperty.overall_ratio                                              | number | 9    | no        | N/A      | 全体比率入力欄                  | 半角数字                                                                                                                                                  |
| clientProperty.overall_ratio_under_decimal                                | number | 2    | no        | N/A      | 全体比率（小数点以下）入力欄    | 半角数字                                                                                                                                                  |
| clientProperty.building_area_type                                         | number | 1    | yes       | N/A      | 建物面積区分                    | 1=登記、2=実測、0=未選択                                                                                                                                  |
| clientProperty.building_area_1                                            | number | 4    | yes       | N/A      | 建物面積数1入力欄               | 半角数字（単位：㎡）                                                                                                                                      |
| clientProperty.building_area_under_decimal_1                              | number | 2    | no        | N/A      | 建物面積数（小数点以下）1入力欄 | 半角数字                                                                                                                                                  |
| clientProperty.building_area_handle                                       | number | 1    | yes       | N/A      | 建物面積数欄の取り扱い          | 1="～"、2="."（小数点）                                                                                                                                   |
| clientProperty.building_area_2                                            | number | 4    | yes       | N/A      | 建物面積数2入力欄               | 半角数字（単位：㎡）                                                                                                                                      |
| clientProperty.building_area_under_decimal_2                              | number | 2    | no        | N/A      | 建物面積数（小数点以下）2入力欄 | 半角数字                                                                                                                                                  |
| clientProperty.sale_house_num_type                                        | number | 1    | yes       | N/A      | 販売戸数設定                    | 0= 未定、1= 予定・確定                                                                                                                                    |
| clientProperty.sale_house_num                                             | number | 5    | yes       | N/A      | 販売戸数入力欄                  | 半角数字（単位：戸）                                                                                                                                      |
| clientProperty.total_house_num                                            | number | 5    | no        | N/A      | 総戸数入力欄                    | 半角数字（単位：戸）                                                                                                                                      |
| clientProperty.room_num_1                                                 | number | 2    | yes       | N/A      | 部屋数入力欄1                   | 半角数字：（単位：部屋）                                                                                                                                  |
| clientProperty.layout_type_1                                              | number | 2    | yes       | N/A      | 間取りタイプ設定1               | 0=選択してください、1=ワンルーム、2=K<br>3=DK、4=LDK、5=LK、6=KK、7=DKK、8=LKK<br>9=DDKK、10=LLKK、11=LDKK、12=LDDKK、13=LLDKK                            |
| clientProperty.service_rule_type_1                                        | number | 1    | no        | N/A      | サービスルール設定1             | 0=選択してください、1=+S、2=+2S、3=+3S                                                                                                                    |
| clientProperty.layout_handle                                              | number | 1    | no        | N/A      | 間取りの取り扱い                | 1="～"、2="."（小数点）                                                                                                                                   |
| clientProperty.room_num_2                                                 | number | 2    | no        | N/A      | 部屋数入力欄2                   | 半角数字：（単位：部屋）                                                                                                                                  |
| clientProperty.layout_type_2                                              | number | 2    | yes       | N/A      | 間取りタイプ設定2               | 0=選択してください、1=ワンルーム、2=K<br>3=DK、4=LDK、5=LK、6=KK、7=DKK、8=LKK<br>9=DDKK、10=LLKK、11=LDKK、12=LDDKK、13=LLDKK                            |
| clientProperty.service_rule_type_2                                        | number | 1    | no        | N/A      | サービスルール設定2             | 0=選択してください、1=+S、2=+2S、3=+3S                                                                                                                    |
| clientProperty.complete_time_type                                         | number | 1    | yes       | N/A      | 完成時期区分                    | 0= 未選択、1= 完成予定、2= 完成済、3= 契約後                                                                                                              |
| clientProperty.complete_time_select                                       | number | 1    | yes       | N/A      | 完成時期方式                    | 1= 年月、2= 年月日                                                                                                                                        |
| clientProperty.complete_year                                              | number | 4    | no        | N/A      | 完成年                          | 半角数字：（単位：年）                                                                                                                                    |
| clientProperty.complete_month                                             | number | 2    | no        | N/A      | 完成月                          | 半角数字：（単位：月）                                                                                                                                    |
| clientProperty.complete_decade                                            | number | 1    | no        | N/A      | 完成旬                          | 0= 未選択、1= 初旬、2= 上旬、3= 中旬、4= 下旬、5= 末                                                                                                      |
| clientProperty.comlete_year_month_day                                     | string | 10   | no        | N/A      | 完成年月日参照表示欄            | YYYY-MM-DD                                                                                                                                                |
| clientProperty.complete_after_contract                                    | number | 2    | no        | N/A      | 完成時期契約後入力欄            | 半角数字                                                                                                                                                  |
| clientProperty.entering_time_type                                         | number | 1    | yes       | N/A      | ???                             | 0= 未選択、1= 完成予定、2= 完成済、3= 契約後                                                                                                              |
| clientProperty.entering_time_select                                       | number | 1    | yes       | N/A      | ???                             | 1= 年月、2= 年月日                                                                                                                                        |
| clientProperty.entering_year                                              | number | 4    | no        | N/A      | ???                             | 半角数字：（単位：年）                                                                                                                                    |
| clientProperty.entering_month                                             | number | 2    | no        | N/A      | ???                             | 半角数字：（単位：月）                                                                                                                                    |
| clientProperty.entering_decade                                            | number | 1    | no        | N/A      | ???                             | 0= 未選択、1= 初旬、2= 上旬、3= 中旬、4= 下旬、5= 末                                                                                                      |
| clientProperty.entering_year_month_day                                    | string | 10   | no        | N/A      | ???                             | YYYY-MM-DD                                                                                                                                                |
| clientProperty.entering_after_contract                                    | number | 2    | no        | N/A      | ???                             | 半角数字                                                                                                                                                  |
| clientProperty.building_situation                                         | number | 1    | yes       | N/A      | 建物状況区分                    | 1= 新築、2= 未入居、3= 中古                                                                                                                               |
| clientProperty.main_structure_type                                        | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.main_structure_others                                      | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.part_structure_type                                        | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.part_structure_others                                      | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.method_type                                                | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.method_others                                              | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.above_ground_floors_num"                                   | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.under_ground_floors_num                                    | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.parking_type                                               | number | 1    | no        | N/A      | 駐車場区分                      | 1= 車庫、2= 地下車庫、3= カースペース<br>4= カーポート、5= 無、0= 未選択                                                                                  |
| clientProperty.construction_company                                       | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.structure_method_floor_num_others                          | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.building_certification_num                                 | string | 100  | no        | N/A      | ???                             | 第18UDI1W建05787号　平成30年8月27日                                                                                                                       |
| clientProperty.exterior_type                                              | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.exterior_reform_year                                       | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.exterior_reform_month                                      | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.exterior_reform_point_type                                 | string | 20   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.exterior_reform_others                                     | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_type                                              | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_year                                       | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_month                                      | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_point_type                                 | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_others                                     | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.interior_reform_others_memo                                | string | N/A  | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.sale_house_num_text                                        | string | 11   | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.complete_time                                              | string | ???  | no        | N/A      | ???                             | 任意の文字列（ex.2018年12月未選択）                                                                                                                       |
| clientProperty.landyouto                                                  | string | 1    | no        | N/A      | ???                             | 任意の文字列（ex.２種住居）                                                                                                                               |
| clientProperty.advertiser_company_trade_aspect_type                       | number | 2    | yes       | N/A      | 広告主（貴社）会社取引態様区分  | **See &lowast;1**                                                                                                                                         |
| clientProperty.trade_aspect_1                                             | string | 20   | no        | N/A      | 取引態様1入力欄                 | 任意の文字列                                                                                                                                              |
| clientProperty.mototsuki_traders_name                                     | string | 150  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.area_code                                                  | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.local_code                                                 | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.subscriber_num                                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.person_in_charge                                           | string | 10   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.advertiser_company_trade_aspect_type_2                     | number | 2    | no        | N/A      | 会社2取引態様区分               | **See &lowast;2**                                                                                                                                         |
| clientProperty.company_postalcode_2                                       | string | 5    | no        | N/A      | 会社2郵便区分番号入力欄         | 半角数字                                                                                                                                                  |
| clientProperty.company_area_num_2                                         | string | 4    | no        | N/A      | 会社2町域番号入力欄             | 半角数字                                                                                                                                                  |
| clientProperty.company_address_2                                          | string | 150  | no        | N/A      | 住所2入力欄                     | 任意の文字列                                                                                                                                              |
| clientProperty.position_group_name_2                                      | string | 200  | no        | N/A      | 所属団体名2入力欄               | 任意の文字列                                                                                                                                              |
| clientProperty.license_num_2                                              | string | 30   | no        | N/A      | 免許番号2入力欄                 | 任意の文字列                                                                                                                                              |
| clientProperty.company_name_2                                             | string | 150  | no        | N/A      | 社名2入力欄                     | 任意の文字列                                                                                                                                              |
| clientProperty.advertiser_company_trade_aspect_type_3                     | number | 2    | no        | N/A      | 会社3取引態様区分               | **See &lowast;2**                                                                                                                                         |
| clientProperty.company_postalcode_3                                       | string | 5    | no        | N/A      | 会社3郵便区分番号入力欄         | 半角数字                                                                                                                                                  |
| clientProperty.company_area_num_3                                         | string | 4    | no        | N/A      | 会社3町域番号入力欄             | 半角数字                                                                                                                                                  |
| clientProperty.company_address_3                                          | string | 150  | no        | N/A      | 住所3入力欄                     | 任意の文字列                                                                                                                                              |
| clientProperty.position_group_name_3                                      | string | 200  | no        | N/A      | 所属団体名3入力欄               | 任意の文字列                                                                                                                                              |
| clientProperty.license_num_3                                              | string | 30   | no        | N/A      | 免許番号3入力欄                 | 任意の文字列                                                                                                                                              |
| clientProperty.company_name_3                                             | string | 150  | no        | N/A      | 社名3入力欄                     | 任意の文字列                                                                                                                                              |
| clientProperty.statutory_limit_type_1                                     | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.statutory_limit_type_2                                     | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.statutory_limit_type_3                                     | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.limitation_others_type_1                                   | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kijunhou_43zyou_1kou_acceptance_type                       | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.acceptance_reason                                          | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.limitation_others                                          | string | 150  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.limit_memo                                                 | string | 1    | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.object_performance_select                                  | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.building_inspection_select                                 | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.house_hitory_select                                        | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.vacant_house_bank_select                                   | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.vacant_house_bank_select                                   | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.location_land_character_select                             | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.station_conveniene_selecct                                 | array  | N/A  | no        | N/A      | 駅利便性設定                    | number in array（複数可能）<br>1= 2沿線以上利用可、2= 検索駅まで平坦、3= 始発駅                                                                           |
| clientProperty.dwelling_unit_floors_num_select                            | array  | N/A  | no        | N/A      | 住戸・階数設定                  | number in array（複数可能）<br>1= 平屋、2= 2階建て、3= 3階建て以上（複数可能）                                                                            |
| clientProperty.lighting_ventication_select                                | array  | N/A  | no        | N/A      | 陽当り・採光・通風設定          | number in array（複数可能）<br>**See &lowast;3**                                                                                                          |
| clientProperty.character_madori_selecet                                   | array  | N/A  | no        | N/A      | 間取り設定                      | number in array（複数可能）<br>**See &lowast;4**                                                                                                          |
| clientProperty.character_inner_room_equipment_select                      | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.storing_select                                             | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kitchen_concerned_facilities_select                        | array  | N/A  | no        | N/A      | キッチン・関連設備設定          | number in array（複数可能）<br>**See &lowast;5**                                                                                                          |
| clientProperty.bathroom_select                                            | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.toilet_lavatory_concerned_equipment_select                 | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.balcony_terrace_select                                     | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.garden_select                                              | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.eco_concerned_select                                       | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.tv_communication_select                                    | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.parking_select                                             | array  | N/A  | no        | N/A      | 駐車・駐輪設定                  | number in array（複数可能）<br>1= 駐車2台可、2= 駐車3台以上可、3= ハイルーフ駐車場<br>4= EV車充電設備、5= シャッター車庫、6= ビルトインガレージ           |
| clientProperty.common_use_space_select                                    | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.management_security_select                                 | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.reform_renovation_select                                   | array  | N/A  | no        | N/A      | ﾘﾌｫｰﾑ・ﾘﾉﾍﾞｰｼｮﾝ設定             | number in array（複数可能）<br>1= 適合リノベーション、2= 内外装リフォーム、3= 内装リフォーム<br>4= 外装リフォーム、5= フローリング張替、6= リノベーション |
| clientProperty.surroundings_select                                        | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.view_natural_environments_select                           | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.expense_delivery_entering_condition_select                 | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.charcter_others_selcet                                     | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.character                                                  | string | ???  | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.property_details_appeal                                    | string | N/A  | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.event_category_type                                        | string | 30   | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.event_details_summary                                      | string | N/A  | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.inner                                                      | object | N/A  | yes       | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.inner.&lowast;                                             | object | N/A  | yes       | N/A      | ???                             | JSON object<br>&lowast;は`main_inner,sub_inner_2 - sub_inner_20`まで存在する                                                                              |
| clientProperty.inner.&lowast;.inner_image                                 | string | 255  | no        | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.inner.&lowast;.inner_category                              | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.inner.&lowast;.inner_category_text                         | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.inner.&lowast;.inner_image_caption                         | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.outer                                                      | object | N/A  | yes       | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.outer.&lowast;                                             | object | N/A  | yes       | N/A      | ???                             | JSON object<br>&lowast;は`main_outer,sub_outer_2 - sub_outer_20`まで存在する                                                                              |
| clientProperty.outer.&lowast;.outer_image                                 | string | 255  | no        | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.outer.&lowast;.outer_category                              | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.outer.&lowast;.outer_category_text                         | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.outer.&lowast;.outer_image_caption                         | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info                                          | object | N/A  | yes       | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.surroundings_info.&lowast;                                 | object | N/A  | yes       | N/A      | ???                             | JSON object<br>&lowast;は`surroundings_info_1 - surroundings_info_20`まで存在する                                                                         |
| clientProperty.surroundings_info.&lowast;.surroundings_info_image         | string | 255  | no        | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.surroundings_info.&lowast;.surroundings_info_category      | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info.&lowast;.surroundings_info_category_text | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info.&lowast;.surroundings_info_facility_name | string | 50   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info.&lowast;.surroundings_info_distance      | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.surroundings_info.&lowast;.surroundings_info_caption       | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment                                                  | object | N/A  | yes       | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.equipment.&lowast;                                         | object | N/A  | no        | N/A      | ???                             | JSON object<br>&lowast;は`equipment_1 - equipment_10`まで存在する                                                                                         |
| clientProperty.equipment.&lowast;.equipment_image                         | string | N/A  | no        | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.equipment.&lowast;.equipment_category                      | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.equipment_category_text                 | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.equipment_image_name                    | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.equipment_image_caption                 | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kouzou                                                     | object | N/A  | yes       | N/A      | ???                             | JSON object                                                                                                                                               |
| clientProperty.kouzou.&lowast;                                            | object | N/A  | yes       | N/A      | ???                             | JSON object<br>&lowast;は`kouzou_1 - kouzou_10`まで存在する                                                                                               |
| clientProperty.equipment.&lowast;.kouzou_image                            | string | N/A  | no        | N/A      | ???                             | 画像のパス（ex./image/cl_0000000134/main.jpg）                                                                                                            |
| clientProperty.equipment.&lowast;.kouzou_category                         | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.kouzou_category_text                    | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.kouzou_image_name                       | string | 100  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.equipment.&lowast;.kouzou_image_caption                    | string | 200  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.internal_memo                                              | string | 2000 | no        | N/A      | 社内メモ入力欄                  | 任意の文字列                                                                                                                                              |
| clientProperty.handled_store_name                                         | string | 13   | no        | N/A      | ???                             | 任意の文字列（ex.IM00022）                                                                                                                                |
| clientProperty.posting_priority_value                                     | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.jimukyoku_memo                                             | string | 2000 | no        | N/A      | いえまるこ事務局メモ入力欄      | 任意の文字列                                                                                                                                              |
| clientProperty.rakuten_tel                                                | number | 15   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.unvisible_flag                                             | number | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.release_date_pre                                           | string | 10   | no        | N/A      | ???                             | YYYY-MM-DD                                                                                                                                                |
| clientProperty.release_date_now                                           | string | 10   | no        | N/A      | ???                             | YYYY-MM-DD                                                                                                                                                |
| clientProperty.new_label                                                  | string | ???  | no        | N/A      | ???                             | 任意の文字列（ex.新着）                                                                                                                                   |
| clientProperty.status_label                                               | string | ???  | no        | N/A      | ???                             | 任意の文字列（ex.新築一戸建て）                                                                                                                           |
| clientProperty.price                                                      | string | 11   | no        | N/A      | ???                             | 任意の文字列（ex.2,480万円）                                                                                                                              |
| clientProperty.price_text                                                 | string | 11   | no        | N/A      | ???                             | 任意の文字列（ex.2,480万円\r\n）                                                                                                                          |
| clientProperty.most_prices_text                                           | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_area_type_text                                        | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.building_area_type_text                                    | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_area_text                                             | string | 11   | no        | N/A      | ???                             | 任意の文字列（ex.226㎡）                                                                                                                                  |
| clientProperty.building_area_text                                         | string | 11   | no        | N/A      | ???                             | 任意の文字列（ex.108㎡）                                                                                                                                  |
| clientProperty.leasehold_type_text                                        | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.parking_type_text                                          | string | 11   | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.shidoufutan_text                                           | string | 1    | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.kenpeiritsu_text                                           | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.yosekiritsu_text                                           | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.sales_number_text                                          | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.advertiser_company_trade_aspect_type_text_1                | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.advertiser_company_trade_aspect_type_text_2                | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.advertiser_company_trade_aspect_type_text_3                | string | 11   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.madori_all                                                 | string | 30   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.madori                                                     | string | 1    | no        | N/A      | ???                             | ???                                                                                                                                                       |
| clientProperty.land_area                                                  | string | 11   | no        | N/A      | ???                             | 任意の文字列（ex.226.64ｍ²）                                                                                                                              |
| clientProperty.building_area                                              | string | 11   | no        | N/A      | ???                             | 任意の文字列（ex.108.74ｍ²）                                                                                                                              |
| clientProperty.address                                                    | string | N/A  | no        | N/A      | ???                             | 任意の文字列（ex.栃木県宇都宮市白沢町）                                                                                                                   |
| clientProperty.offer_date                                                 | string | 11   | no        | N/A      | ???                             | YYYY年YY月DD日                                                                                                                                            |
| clientProperty.site_right_type_text                                       | string | 11   | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.entering_time                                              | string | 11   | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| clientProperty.kouzou_kouhou                                              | string | 1    | no        | N/A      | ???                             | 任意の文字列                                                                                                                                              |
| storeOverview                                                             | object | N/A  | yes       | N/A      | ???                             | JSON object                                                                                                                                               |
| storeOverview.id                                                          | string | 7    | no        | N/A      | ???                             | "IM" + 半角数字5桁                                                                                                                                        |
| storeOverview.category                                                    | number | ???  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.status                                                      | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.name                                                        | string | 42   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.comment                                                     | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.postal_code                                                 | string | 7    | no        | N/A      | 郵便番号                        | ???                                                                                                                                                       |
| storeOverview.address                                                     | string | N/A  | no        | N/A      | 住所                            | 任意の文字列（ex.栃木県宇都宮市元今泉7-3-11）                                                                                                             |
| storeOverview.building                                                    | string | N/A  | no        | N/A      | ビル名                          | 任意の文字列                                                                                                                                              |
| storeOverview.latitude                                                    | number | 11   | yes       | N/A      | 緯度                            | 浮動小数点を含む数値                                                                                                                                      |
| storeOverview.longitude                                                   | number | 12   | yes       | N/A      | 経度                            | 浮動小数点を含む数値                                                                                                                                      |
| storeOverview.main_access                                                 | number | 1    | yes       | N/A      | 主要交通区分                    | 1=電車、2=バス                                                                                                                                            |
| storeOverview.ensen_name                                                  | number | 10   | no        | N/A      | 路線参照表示欄                  | 半角数字                                                                                                                                                  |
| storeOverview.ensen_name_text                                             | string | ???  | no        | N/A      | 路線参照表示欄テキスト          | 任意の文字列                                                                                                                                              |
| storeOverview.station_name                                                | number | 10   | no        | N/A      | 駅参照表示欄                    | 半角数字                                                                                                                                                  |
| storeOverview.station_name_text                                           | string | ???  | no        | N/A      | 駅参照表示欄テキスト            | 任意の文字列                                                                                                                                              |
| storeOverview.access_type                                                 | number | 1    | no        | N/A      | 交通手段区分                    | 1=徒歩、2=バス、3=車                                                                                                                                      |
| storeOverview.walk_time                                                   | number | 2    | no        | N/A      | 徒歩時間入力欄                  | 半角数字（単位：分）                                                                                                                                      |
| storeOverview.bus_time                                                    | number | 2    | no        | N/A      | バス乗車時間入力欄              | 半角数字（単位：分）                                                                                                                                      |
| storeOverview.getoff_busstop                                              | string | 25   | no        | N/A      | 下車バス停入力欄                | 任意の文字列（ex.六本木駅前）                                                                                                                             |
| storeOverview.getoff_busstop_walk_time                                    | number | 2    | no        | N/A      | 下車バス停_徒歩時間入力         | 半角数字（単位：分）                                                                                                                                      |
| storeOverview.running_distance_1                                          | number | 5    | no        | N/A      | 走行距離1                       | 半角数字（単位：km）                                                                                                                                      |
| storeOverview.running_distance_2                                          | number | 2    | no        | N/A      | 走行距離2                       | 半角数字                                                                                                                                                  |
| storeOverview.bus_company                                                 | string | 50   | no        | N/A      | バス会社入力欄                  | 任意の文字列（ex.東急バス）                                                                                                                               |
| storeOverview.busstop                                                     | string | 50   | no        | N/A      | バス停入力欄                    | 任意の文字列（ex.新宿駅前）                                                                                                                               |
| storeOverview.start_business_hours                                        | string | 5    | no        | N/A      | 営業開始時間                    | HH:MM                                                                                                                                                     |
| storeOverview.end_business_hours                                          | string | 5    | no        | N/A      | 営業終了時間                    | HH:MM                                                                                                                                                     |
| storeOverview.businesshours_memo                                          | string | 100  | no        | N/A      | 営業時間メモ                    | ???                                                                                                                                                       |
| storeOverview.regular_holiday                                             | string | 3    | no        | N/A      | 定休日                          | 月曜日 - 日曜日                                                                                                                                           |
| storeOverview.character                                                   | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.property_type                                               | string | 10   | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.realestate_registration_number                              | string | 60   | no        | N/A      | ???                             | 任意の文字列（ex.国土交通大臣（１）第9280号）                                                                                                             |
| storeOverview.tel                                                         | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.rakuten_tel                                                 | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.inquiry_mail_admin                                          | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.inquiry_mail_staff                                          | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.inquiry_mail_other                                          | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.open_mail_admin                                             | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.open_mail_staff                                             | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.open_mail_other                                             | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.memo                                                        | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.score                                                       | number | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.provider_memo                                               | string | N/A  | no        | N/A      | ???                             | ???                                                                                                                                                       |
| storeOverview.near_station                                                | string | ???  | no        | N/A      | ???                             | 任意の文字列（ex.JR東北本線「宇都宮駅」徒歩19分）                                                                                                         |