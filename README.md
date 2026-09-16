# BillApp

记账 App。第一个模块为「美食」，支持随手记：记录店名、地图标记位置、备注（好吃的菜），下次可直接打开高德地图导航。

## 功能模块

- **美食**（已完成）
  - 随手记列表
  - 新增记录：店名、位置（地图选点）、备注
  - 地图选点：嵌入高德地图，点击/拖拽标记位置，自动逆地理编码获取地址
  - 详情页：查看记录，一键打开高德地图导航 / 查看位置
  - 本地存储（Room），无需后端
- **记账**、**我的**：占位，后续扩展

## 技术栈

- Kotlin + Jetpack Compose
- Room 数据库
- 高德地图 3D SDK + 搜索 SDK
- Material 3

## 使用前配置

### 1. 高德地图 API Key

本项目使用高德地图 SDK，需要你申请 API Key 并替换占位符：

1. 登录 [高德开放平台](https://lbs.amap.com/)，创建「Android 应用」
2. 获取 **SHA1 签名** 和 **包名**（`com.billapp`），生成 Key
3. 打开 [app/src/main/AndroidManifest.xml](app/src/main/AndroidManifest.xml)，将 `YOUR_AMAP_API_KEY_HERE` 替换为你的 Key

```xml
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="你的高德API Key" />
```

### 2. 构建

用 Android Studio 打开项目，或在命令行执行：

```bash
./gradlew assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

> 注意：未配置正确的高德 API Key 时，地图将无法正常显示。

## 项目结构

```
app/src/main/java/com/billapp/
├── BillApp.kt              # Application，初始化数据库与高德隐私合规
├── MainActivity.kt         # 入口 Activity
├── data/                   # Room 数据层
│   ├── FoodRecord.kt       # 美食记录实体
│   ├── FoodRecordDao.kt
│   ├── AppDatabase.kt
│   └── FoodRepository.kt
└── ui/
    ├── theme/              # Compose 主题
    ├── navigation/         # 底部导航 + 路由
    ├── placeholder/        # 占位模块
    └── food/               # 美食模块
        ├── FoodScreen.kt           # 列表页
        ├── AddFoodScreen.kt        # 添加/编辑页
        ├── FoodDetailScreen.kt     # 详情页（高德导航）
        ├── MapPickerScreen.kt      # 高德地图选点
        └── FoodViewModel.kt
```
